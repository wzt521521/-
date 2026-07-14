"""Unit tests for the data-pipeline transformations.

These tests deliberately exercise only in-process code.  Redis and MySQL
coverage lives in ``test_pipeline_integration.py`` and is selected explicitly
with ``pytest -m integration``.
"""
from __future__ import annotations

import sys
from pathlib import Path

import pytest


PIPELINE_DIR = Path(__file__).resolve().parent
if str(PIPELINE_DIR) not in sys.path:
    sys.path.insert(0, str(PIPELINE_DIR))

from etl_clean import (  # noqa: E402
    check_duplicate,
    compute_md5,
    extract_skills,
    insert_position,
    normalize_city,
    normalize_education,
    normalize_experience,
    normalize_salary,
)
from import_data import parse_salary, row_to_json  # noqa: E402


class RecordingCursor:
    """Minimal DB-API cursor double for query-shape unit tests."""

    def __init__(self, result=(0,), lastrowid=42):
        self.result = result
        self.lastrowid = lastrowid
        self.calls = []

    def execute(self, statement, params=()):
        self.calls.append((statement, params))

    def fetchone(self):
        return self.result


@pytest.mark.parametrize(
    ("salary", "expected"),
    [
        (None, {"min": None, "max": None}),
        ({"min": 15, "max": 25}, {"min": 15, "max": 25}),
        ({"min": 25, "max": 15}, {"min": 15, "max": 25}),
        ({"min": 200, "max": 400}, {"min": 16, "max": 33}),
        ({"min": "invalid", "max": "data"}, {"min": None, "max": None}),
    ],
)
def test_normalize_salary(salary, expected):
    assert normalize_salary(salary) == expected


@pytest.mark.parametrize(
    ("raw", "expected"),
    [
        ("fresh graduate", "\u5e94\u5c4a"),
        ("5-10 years", "5-10\u5e74"),
        ("14 years of experience", "10\u5e74\u4ee5\u4e0a"),
        ("4 years", "3-5\u5e74"),
        (None, None),
    ],
)
def test_normalize_experience(raw, expected):
    assert normalize_experience(raw) == expected


@pytest.mark.parametrize(
    ("raw", "expected"),
    [
        ("master", "\u7855\u58eb"),
        ("bachelor", "\u672c\u79d1"),
        ("high school", "\u4e0d\u9650"),
        ("unknown", None),
        (None, None),
    ],
)
def test_normalize_education(raw, expected):
    assert normalize_education(raw) == expected


def test_normalize_city_uses_mapping_and_preserves_unknown_city():
    city_map = {"Shanghai": {"province": "Shanghai", "tier": "tier-1"}}

    assert normalize_city("Shanghai", None, city_map) == ("Shanghai", "Shanghai", "tier-1")
    assert normalize_city("Unmapped", None, city_map) == ("Unmapped", None, "\u5176\u4ed6")
    assert normalize_city(None, None, city_map) == (None, None, None)


def test_extract_skills_normalizes_aliases_and_case():
    skills = extract_skills(
        "Python engineer",
        "Build services with Spring Boot and SQL.",
        ["PYTHON"],
        {"python", "sql"},
        {"spring boot": "Spring Boot"},
    )

    assert skills == ["Python", "Spring Boot", "sql"]


def test_compute_md5_is_stable_for_a_source_identity():
    assert compute_md5("job-1", "https://example.test/jobs/1") == "222e34b46928b431e844780cb5ad86bc"
    assert compute_md5("job-1", "https://example.test/jobs/1") != compute_md5(
        "job-2", "https://example.test/jobs/1"
    )


@pytest.mark.parametrize(
    ("raw", "expected"),
    [
        ("8K-15K", {"min": 8, "max": 15}),
        ("15000", {"min": 15, "max": 15}),
        ("negotiable", {"min": 0, "max": 0}),
        ("not-a-salary", {"min": None, "max": None}),
    ],
)
def test_parse_salary(raw, expected):
    assert parse_salary(raw) == expected


def test_row_to_json_maps_a_source_row_without_io():
    row = {
        "Job Title": "Data Engineer",
        "Company": "Example Co",
        "Location": "Shanghai",
        "Salary": "12K-20K",
        "Experience": "3-5 years",
        "Qualification": "bachelor",
        "Skills": "Python|SQL",
        "source_url": "https://example.test/jobs/42",
    }
    city_map = {"Shanghai": {"province": "Shanghai", "tier": "tier-1"}}

    job = row_to_json(row, 42, city_map)

    assert job["jobId"].endswith("000042")
    assert job["title"] == "Data Engineer"
    assert job["company"]["name"] == "Example Co"
    assert job["salary"] == {"min": 12, "max": 20}
    assert job["city"] == "Shanghai"
    assert job["province"] == "Shanghai"
    assert job["cityTier"] == "tier-1"
    assert job["skills"] == ["Python", "SQL"]


def test_database_helpers_use_bound_parameters_and_json_payloads():
    cursor = RecordingCursor(result=(0,))
    job = {
        "jobId": "job-1",
        "title": "Data Engineer",
        "company": {"name": "Example Co"},
        "salary": {"min": 15, "max": 25},
        "skills": ["Python"],
        "welfare": ["Remote"],
        "sourceUrl": "https://example.test/jobs/1",
    }

    assert check_duplicate(cursor, "source-md5") is False
    insert_position(cursor, job, 42, "source-md5")

    select_statement, select_params = cursor.calls[0]
    insert_statement, insert_params = cursor.calls[1]
    assert "source_md5 = %s" in select_statement
    assert select_params == ("source-md5",)
    assert "INSERT INTO job_position" in insert_statement
    assert insert_params[0:3] == ("job-1", "Data Engineer", 42)
    assert insert_params[-1] == "source-md5"
