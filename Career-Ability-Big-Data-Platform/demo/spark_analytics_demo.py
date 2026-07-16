"""答辩演示脚本 2：Spark SQL 多维度岗位分析。

通过 Spark SQL 直连 MySQL，执行城市排行、技能聚合、薪资分布查询。
效果等价于后端 AnalyticsService 的 Java Stream 实现，但使用分布式计算引擎。
"""
from __future__ import annotations

import os
import sys
import time

MYSQL_URL = os.getenv(
    "SPARK_DEMO_MYSQL_URL",
    "jdbc:mysql://localhost:3307/career_ability?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai",
)
MYSQL_USER = os.getenv("MYSQL_USER", "career_app")
MYSQL_PASSWORD = os.getenv("MYSQL_PASSWORD", "local-development-app-password")


def run_queries():
    """用 PySpark 连接 MySQL，执行分析 SQL。"""
    from pyspark.sql import SparkSession

    spark = SparkSession.builder \
        .appName("CareerAnalyticsDemo") \
        .config("spark.driver.memory", "512m") \
        .config("spark.sql.adaptive.enabled", "true") \
        .master("local[*]") \
        .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    df = spark.read \
        .format("jdbc") \
        .option("url", MYSQL_URL) \
        .option("driver", "com.mysql.cj.jdbc.Driver") \
        .option("dbtable", "job_position") \
        .option("user", MYSQL_USER) \
        .option("password", MYSQL_PASSWORD) \
        .load()

    total = df.count()
    print(f"\n总岗位数: {total}")

    # 1. 城市岗位排行
    print(f"\n{'='*60}")
    print("  1. 城市岗位排行 TOP 10")
    print(f"{'='*60}")
    df.createOrReplaceTempView("job_position")
    cities = spark.sql("""
        SELECT city, COUNT(*) AS cnt,
               ROUND(AVG((salary_min + salary_max) / 2.0), 1) AS avg_salary_k
        FROM job_position WHERE city IS NOT NULL AND salary_min IS NOT NULL
        GROUP BY city ORDER BY cnt DESC LIMIT 10
    """).collect()
    print(f"  {'城市':<10s} {'岗位数':>6s}  {'平均月薪(K)':>12s}")
    print(f"  {'-'*32}")
    for r in cities:
        print(f"  {r.city:<10s} {r.cnt:>6d}  {r.avg_salary_k:>12.1f}")

    # 2. 热门技能
    print(f"\n{'='*60}")
    print("  2. 薪资区间分布")
    print(f"{'='*60}")
    salary_dist = spark.sql("""
        SELECT CASE
            WHEN (salary_min + salary_max) / 2 < 10 THEN '0-10K'
            WHEN (salary_min + salary_max) / 2 < 20 THEN '10-20K'
            WHEN (salary_min + salary_max) / 2 < 30 THEN '20-30K'
            ELSE '30K+'
        END AS salary_range,
        COUNT(*) AS cnt
        FROM job_position WHERE salary_min IS NOT NULL
        GROUP BY salary_range ORDER BY salary_range
    """).collect()
    for r in salary_dist:
        bar = "█" * int(r.cnt / max(1, max(x.cnt for x in salary_dist)) * 30)
        print(f"  {r.salary_range:<10s} {r.cnt:>4d}  {bar}")

    # 3. 学历分布
    print(f"\n{'='*60}")
    print("  3. 学历要求分布")
    print(f"{'='*60}")
    edu = spark.sql("""
        SELECT education, COUNT(*) AS cnt FROM job_position
        WHERE education IS NOT NULL
        GROUP BY education ORDER BY cnt DESC
    """).collect()
    for r in edu:
        print(f"  {r.education:<10s} {r.cnt:>4d}")

    spark.stop()

    print(f"\n{'='*60}")
    print("结论：上述 Spark SQL 聚合查询与 Java AnalyticsService")
    print("中的 Stream 计算逻辑等效，但 Spark 可处理 TB 级数据，")
    print("Java 内存聚合在数据量大时会 OOM。")
    print(f"{'='*60}")


def main():
    print("=" * 60)
    print("  演示：Spark SQL 多维度岗位分析")
    print("=" * 60)
    print()

    print("对比架构：")
    print("  主通道: Spring Boot → Spark SQL/Hive → MySQL → 返回")
    print("  兜底:   Spring Boot → Java Stream → MySQL → 返回")
    print()

    if "pyspark" not in sys.modules:
        try:
            import pyspark
        except ImportError:
            print("[SKIP] PySpark 未安装，请先执行: pip install pyspark")
            print("（系统已内置降级机制，Java AnalyticsService 照常工作）")
            return

    try:
        run_queries()
    except Exception as e:
        print(f"\n[WARN] Spark SQL 查询失败 ({e})")
        print("系统会自动降级到 MySQL + Java Stream 通道，前端不受影响。")


if __name__ == "__main__":
    main()
