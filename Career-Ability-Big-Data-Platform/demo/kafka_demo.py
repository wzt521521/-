"""答辩演示脚本 1：Kafka 消息队列的生产与消费。

从 CSV 读取 10 条岗位数据 → 发送到 Kafka Topic → 消费回来 → 打印对比。
演示后可对比 Kafka（分布式持久化）与 Redis List（内存队列）的架构差异。
"""
from __future__ import annotations

import json
import sys
import time
from pathlib import Path

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
CSV_FILE = DATA_DIR / "kaggle_jobs_500.csv"

# 尝试导入 Kafka
try:
    from kafka import KafkaConsumer, KafkaProducer
    from kafka.admin import KafkaAdminClient, NewTopic

    KAFKA_AVAILABLE = True
except ImportError:
    print("[WARN] kafka-python 未安装，请先执行: pip install kafka-python")
    KAFKA_AVAILABLE = False

BOOTSTRAP_SERVERS = "localhost:9092"
TOPIC = "demo-raw-jobs"
NUM_MESSAGES = 10


def create_topic():
    """幂等创建演示 Topic。"""
    try:
        admin = KafkaAdminClient(bootstrap_servers=BOOTSTRAP_SERVERS)
        existing = admin.list_topics()
        if TOPIC not in existing:
            admin.create_topics([NewTopic(TOPIC, num_partitions=1, replication_factor=1)])
            print(f"[INFO] Topic '{TOPIC}' 已创建")
        else:
            print(f"[INFO] Topic '{TOPIC}' 已存在")
        admin.close()
    except Exception as e:
        print(f"[WARN] 无法创建 Topic: {e}")


def produce_sample():
    """读 CSV 前 10 条，发送到 Kafka。"""
    import pandas as pd

    if not CSV_FILE.exists():
        print(f"[ERROR] 数据文件不存在: {CSV_FILE}")
        return 0

    df = pd.read_csv(CSV_FILE, encoding="utf-8-sig")
    sample = df.head(NUM_MESSAGES)

    producer = KafkaProducer(
        bootstrap_servers=BOOTSTRAP_SERVERS,
        value_serializer=lambda v: json.dumps(v, ensure_ascii=False).encode("utf-8"),
    )

    count = 0
    for _, row in sample.iterrows():
        payload = {
            "jobId": str(row.get("job_id", "")),
            "title": str(row.get("title", "")),
            "city": str(row.get("city", "")),
            "salary_min": str(row.get("salary_min", "")),
            "salary_max": str(row.get("salary_max", "")),
        }
        producer.send(TOPIC, payload)
        count += 1

    producer.flush()
    producer.close()
    print(f"[INFO] 已发送 {count} 条岗位数据到 Kafka Topic '{TOPIC}'")
    return count


def consume_sample(expected: int):
    """消费并展示 Kafka 消息。"""
    consumer = KafkaConsumer(
        TOPIC,
        bootstrap_servers=BOOTSTRAP_SERVERS,
        auto_offset_reset="earliest",
        value_deserializer=lambda v: json.loads(v.decode("utf-8")),
        consumer_timeout_ms=10000,
    )

    print(f"\n{'='*60}")
    print(f"  Kafka 消费结果（共 {expected} 条）")
    print(f"{'='*60}")
    print(f"  {'岗位名称':<20s} {'城市':<10s} {'薪资范围':<15s}")
    print(f"  {'-'*45}")

    received = 0
    for msg in consumer:
        job = msg.value
        title = job.get("title", "-")[:18]
        city = job.get("city", "-")
        salary = f"{job.get('salary_min', '?')}-{job.get('salary_max', '?')}K"
        print(f"  {title:<20s} {city:<10s} {salary:<15s}")
        received += 1
        if received >= expected:
            break

    consumer.close()
    print(f"{'='*60}")
    print(f"[INFO] Kafka 消费完成: {received}/{expected} 条")


def main():
    if not KAFKA_AVAILABLE:
        print("[SKIP] Kafka 不可用，跳过演示（这不影响系统功能——Redis 兜底通道照常工作）")
        return

    print("=" * 60)
    print("  演示：Kafka 分布式消息队列")
    print("=" * 60)
    print()
    print("架构对比：")
    print("  主通道: CSV → Kafka → Spark ETL → MySQL")
    print("  兜底:   CSV → Redis Queue → Python ETL → MySQL")
    print()

    create_topic()
    count = produce_sample()
    if count > 0:
        time.sleep(1)
        consume_sample(count)

    print()
    print("结论：Kafka 替代 Redis List 后，消息持久化到磁盘、")
    print("支持多消费者组，故障恢复不丢数据。")
    print("若 Kafka 不可用，import_data.py 自动降级到 Redis 通道。")


if __name__ == "__main__":
    main()
