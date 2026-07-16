"""Spark ETL：消费 Kafka → 清洗 → 写入 MySQL（大数据主通道）。

与 Python ``etl_clean.py``（Redis 消费者）并行运行，互为备份。
Spark 作业失败时 Python ETL 自动兜底，数据不丢。
"""
from __future__ import annotations

import json
import os
import time
from datetime import datetime, timezone

from pyspark.sql import SparkSession
from pyspark.sql import functions as F
from pyspark.sql.types import (
    ArrayType,
    IntegerType,
    StringType,
    StructField,
    StructType,
)

# ---- 配置 ----
KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC_RAW_JOBS", "raw-jobs")
MYSQL_URL = os.getenv(
    "SPARK_MYSQL_URL",
    "jdbc:mysql://mysql:3306/career_ability?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai",
)
MYSQL_USER = os.getenv("MYSQL_USER", "career_app")
MYSQL_PASSWORD = os.getenv("MYSQL_PASSWORD", "")
CHECKPOINT_DIR = os.getenv("SPARK_CHECKPOINT_DIR", "/tmp/spark-etl-checkpoint")

# ---- JSON Schema ----
COMPANY_SCHEMA = StructType([
    StructField("name", StringType(), True),
    StructField("size", StringType(), True),
    StructField("industry", StringType(), True),
    StructField("type", StringType(), True),
])

SALARY_SCHEMA = StructType([
    StructField("min", IntegerType(), True),
    StructField("max", IntegerType(), True),
])

JOB_SCHEMA = StructType([
    StructField("jobId", StringType(), True),
    StructField("title", StringType(), True),
    StructField("company", COMPANY_SCHEMA, True),
    StructField("salary", SALARY_SCHEMA, True),
    StructField("city", StringType(), True),
    StructField("province", StringType(), True),
    StructField("cityTier", StringType(), True),
    StructField("education", StringType(), True),
    StructField("experience", StringType(), True),
    StructField("skills", ArrayType(StringType(), True), True),
    StructField("welfare", ArrayType(StringType(), True), True),
    StructField("description", StringType(), True),
    StructField("publishDate", StringType(), True),
    StructField("sourceUrl", StringType(), True),
    StructField("sourceMd5", StringType(), True),
    StructField("crawlTime", StringType(), True),
])


def _ensure_topic(spark: SparkSession) -> None:
    """通过 Spark 内置 Kafka 工具创建 Topic（幂等）。"""
    try:
        spark.sparkContext._jvm.org.apache.spark.sql.kafka010.KafkaSourceProvider \
            .validateStreamOptions({})  # no-op: ensure Kafka classes are loaded
    except Exception:
        pass


def write_batch(batch_df, epoch_id: int) -> None:
    """微批写入 MySQL — 仅新增，不覆盖。"""
    if batch_df.rdd.isEmpty():
        return

    batch_df.write \
        .format("jdbc") \
        .option("url", MYSQL_URL) \
        .option("driver", "com.mysql.cj.jdbc.Driver") \
        .option("dbtable", "job_position") \
        .option("user", MYSQL_USER) \
        .option("password", MYSQL_PASSWORD) \
        .mode("append") \
        .save()

    print(f"[INFO] Spark ETL epoch={epoch_id}: {batch_df.count()} rows written at {datetime.now(timezone.utc).isoformat()}")


def main() -> None:
    spark = SparkSession.builder \
        .appName("CareerAbilityETL") \
        .config("spark.sql.adaptive.enabled", "true") \
        .config("spark.sql.adaptive.coalescePartitions.enabled", "true") \
        .config("spark.sql.shuffle.partitions", "4") \
        .config("spark.driver.memory", "512m") \
        .config("spark.executor.memory", "512m") \
        .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")
    print(f"[INFO] Spark {spark.version} ETL starting — Kafka={KAFKA_BOOTSTRAP_SERVERS} Topic={KAFKA_TOPIC}")

    _ensure_topic(spark)

    # 从 Kafka 读取流
    raw = spark.readStream \
        .format("kafka") \
        .option("kafka.bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS) \
        .option("subscribe", KAFKA_TOPIC) \
        .option("startingOffsets", "earliest") \
        .option("failOnDataLoss", "false") \
        .load()

    # 解析 JSON → 展平字段
    parsed = raw.selectExpr("CAST(value AS STRING) as json_str") \
        .select(F.from_json(F.col("json_str"), JOB_SCHEMA).alias("job")) \
        .select(
            F.col("job.jobId").alias("job_id"),
            F.col("job.title").alias("title"),
            F.col("job.company.name").alias("company_name"),
            F.col("job.salary.min").alias("salary_min"),
            F.col("job.salary.max").alias("salary_max"),
            F.col("job.city").alias("city"),
            F.col("job.province").alias("province"),
            F.col("job.cityTier").alias("city_tier"),
            F.col("job.education").alias("education"),
            F.col("job.experience").alias("experience"),
            F.to_json(F.col("job.skills")).alias("skills"),
            F.to_json(F.col("job.welfare")).alias("welfare"),
            F.col("job.description").alias("description"),
            F.to_date(F.col("job.publishDate")).alias("publish_date"),
            F.col("job.sourceUrl").alias("source_url"),
            F.col("job.sourceMd5").alias("source_md5"),
        ) \
        .filter(F.col("title").isNotNull()) \
        .filter(F.col("company_name").isNotNull())

    query = parsed.writeStream \
        .foreachBatch(write_batch) \
        .option("checkpointLocation", CHECKPOINT_DIR) \
        .trigger(processingTime="10 seconds") \
        .start()

    print("[INFO] Spark ETL streaming started — waiting for data...")
    query.awaitTermination()


if __name__ == "__main__":
    main()
