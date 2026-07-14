"""Opt-in Redis/MySQL integration coverage for the data pipeline.

Run this file only through ``python -m pytest -m integration``.  The test
uses per-run Redis keys and removes only the row/company it created; it never
clears shared queues or truncates application tables.
"""
from __future__ import annotations

import json
import os
import re
import sys
import uuid
from pathlib import Path

import pytest


PIPELINE_DIR = Path(__file__).resolve().parent
if str(PIPELINE_DIR) not in sys.path:
    sys.path.insert(0, str(PIPELINE_DIR))

from config import (  # noqa: E402
    CLEANED_QUEUE,
    MYSQL_HOST,
    MYSQL_PASSWORD,
    MYSQL_PORT,
    MYSQL_USER,
    RAW_QUEUE,
    REDIS_DB,
    REDIS_HOST,
    REDIS_PORT,
)
from etl_clean import (  # noqa: E402
    check_duplicate,
    compute_md5,
    extract_skills,
    get_or_create_company,
    insert_position,
    normalize_city,
    normalize_education,
    normalize_experience,
    normalize_salary,
)


pytestmark = pytest.mark.integration


def _test_database_name():
    database = os.getenv("PIPELINE_TEST_MYSQL_DATABASE")
    if not database:
        pytest.fail(
            "Set PIPELINE_TEST_MYSQL_DATABASE to a dedicated database before running integration tests."
        )
    if not re.fullmatch(r"[A-Za-z0-9_]+", database):
        pytest.fail("PIPELINE_TEST_MYSQL_DATABASE must contain only letters, digits, and underscores.")
    return database


def _connect_test_database(database):
    import pymysql

    admin_connection = pymysql.connect(
        host=MYSQL_HOST,
        port=MYSQL_PORT,
        user=MYSQL_USER,
        password=MYSQL_PASSWORD,
        charset="utf8mb4",
    )
    try:
        with admin_connection.cursor() as cursor:
            cursor.execute(
                f"CREATE DATABASE IF NOT EXISTS `{database}` "
                "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
            )
        admin_connection.commit()
    finally:
        admin_connection.close()

    connection = pymysql.connect(
        host=MYSQL_HOST,
        port=MYSQL_PORT,
        user=MYSQL_USER,
        password=MYSQL_PASSWORD,
        database=database,
        charset="utf8mb4",
    )
    with connection.cursor() as cursor:
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS job_company (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                company_name VARCHAR(200) NOT NULL,
                company_size VARCHAR(50) DEFAULT NULL,
                industry VARCHAR(100) DEFAULT NULL,
                company_type VARCHAR(50) DEFAULT NULL,
                UNIQUE KEY uq_job_company_name (company_name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """
        )
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS job_position (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                job_id VARCHAR(100) NOT NULL UNIQUE,
                title VARCHAR(200) NOT NULL,
                company_id BIGINT DEFAULT NULL,
                salary_min INT DEFAULT NULL,
                salary_max INT DEFAULT NULL,
                city VARCHAR(50) DEFAULT NULL,
                province VARCHAR(50) DEFAULT NULL,
                city_tier VARCHAR(20) DEFAULT NULL,
                education VARCHAR(20) DEFAULT NULL,
                experience VARCHAR(20) DEFAULT NULL,
                skills JSON DEFAULT NULL,
                welfare JSON DEFAULT NULL,
                description TEXT DEFAULT NULL,
                publish_date DATE DEFAULT NULL,
                source_url VARCHAR(500) DEFAULT NULL,
                source_md5 VARCHAR(32) DEFAULT NULL,
                KEY idx_job_position_source_md5 (source_md5),
                CONSTRAINT fk_job_position_company
                    FOREIGN KEY (company_id) REFERENCES job_company(id) ON DELETE SET NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """
        )
    connection.commit()
    return connection


def test_redis_to_mysql_pipeline_round_trip():
    import redis

    test_database = _test_database_name()
    run_id = uuid.uuid4().hex
    raw_queue = f"{RAW_QUEUE}:pytest:{run_id}"
    cleaned_queue = f"{CLEANED_QUEUE}:pytest:{run_id}"
    company_name = f"pytest-company-{run_id}"
    job_id = f"pytest-job-{run_id}"
    source_url = f"https://pipeline.integration.test/jobs/{run_id}"
    source_md5 = compute_md5(job_id, source_url)

    redis_client = redis.Redis(
        host=REDIS_HOST,
        port=REDIS_PORT,
        db=REDIS_DB,
        decode_responses=True,
    )
    connection = None
    redis_ready = False

    try:
        redis_client.ping()
        redis_ready = True
        connection = _connect_test_database(test_database)

        job = {
            "jobId": job_id,
            "title": "Data Engineer",
            "company": {"name": company_name, "size": "50-99"},
            "salary": {"min": 180, "max": 300},
            "city": "Shanghai",
            "province": None,
            "education": "bachelor",
            "experience": "5-10 years",
            "skills": ["Python"],
            "welfare": ["Remote"],
            "description": "Python, SQL, and Spring Boot data pipeline.",
            "publishDate": "2026-07-14T00:00:00",
            "sourceUrl": source_url,
        }
        redis_client.lpush(raw_queue, json.dumps(job))
        _, payload = redis_client.brpop(raw_queue, timeout=5)
        job = json.loads(payload)

        job["salary"] = normalize_salary(job["salary"])
        job["city"], job["province"], job["cityTier"] = normalize_city(
            job["city"], job["province"], {"Shanghai": {"province": "Shanghai", "tier": "tier-1"}}
        )
        job["education"] = normalize_education(job["education"])
        job["experience"] = normalize_experience(job["experience"])
        job["skills"] = extract_skills(
            job["title"],
            job["description"],
            job["skills"],
            {"python", "sql"},
            {"spring boot": "Spring Boot"},
        )
        job["sourceMd5"] = source_md5

        with connection.cursor() as cursor:
            assert check_duplicate(cursor, source_md5) is False
            company_id = get_or_create_company(cursor, job["company"])
            insert_position(cursor, job, company_id, source_md5)
            connection.commit()

            assert check_duplicate(cursor, source_md5) is True
            cursor.execute(
                "SELECT job_id, title, salary_min, salary_max, city, source_md5 "
                "FROM job_position WHERE source_md5 = %s",
                (source_md5,),
            )
            assert cursor.fetchone() == (
                job_id,
                "Data Engineer",
                15,
                25,
                "Shanghai",
                source_md5,
            )

        redis_client.lpush(cleaned_queue, json.dumps(job))
        _, cleaned_payload = redis_client.brpop(cleaned_queue, timeout=5)
        assert json.loads(cleaned_payload)["sourceMd5"] == source_md5
    finally:
        if redis_ready:
            redis_client.delete(raw_queue, cleaned_queue)
        redis_client.close()
        if connection is not None:
            try:
                with connection.cursor() as cursor:
                    cursor.execute("DELETE FROM job_position WHERE source_md5 = %s", (source_md5,))
                    cursor.execute("DELETE FROM job_company WHERE company_name = %s", (company_name,))
                connection.commit()
            finally:
                connection.close()
