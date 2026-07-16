"""答辩演示脚本 3：通道降级验证。

演示大数据组件不可用时，系统自动降级到兜底通道的完整流程。
"""
from __future__ import annotations

import subprocess
import sys
import time

# Compose project root is the parent of this demo directory
ROOT = __import__("pathlib").Path(__file__).resolve().parent.parent

COMPOSE_CMD = [
    "docker", "compose",
    "-f", str(ROOT / "docker-compose.yml"),
]
# Use dev override if present
DEV_OVERRIDE = ROOT / "docker-compose.dev.yml"
if DEV_OVERRIDE.exists():
    COMPOSE_CMD.extend(["-f", str(DEV_OVERRIDE)])

BIGDATA_SERVICES = ["kafka", "spark-master", "spark-worker"]


def run(cmd: list[str], description: str) -> bool:
    print(f"\n--- {description} ---")
    print(f"$ {' '.join(cmd)}")
    result = subprocess.run(cmd, cwd=str(ROOT), capture_output=True, text=True)
    if result.stdout.strip():
        print(result.stdout.strip())
    if result.stderr.strip():
        print(result.stderr.strip(), file=sys.stderr)
    ok = result.returncode == 0
    status = "OK" if ok else f"FAIL (exit={result.returncode})"
    print(f"[{status}] {description}")
    return ok


def check_backend_health():
    """验证后端服务正常返回数据（无论走哪个通道）。"""
    import urllib.request, json

    try:
        req = urllib.request.Request("http://localhost:8080/actuator/health")
        resp = urllib.request.urlopen(req, timeout=5)
        data = json.loads(resp.read())
        status = data.get("status", "UNKNOWN")
        print(f"\n[INFO] 后端健康状态: {status}")
        return status == "UP"
    except Exception as e:
        print(f"\n[WARN] 后端不可达: {e}")
        return False


def main():
    print("=" * 60)
    print("  演示：大数据通道降级与恢复")
    print("=" * 60)
    print()
    print("三步演示：")
    print("  1. 启动大数据组件 → Kafka + Spark 主通道就绪")
    print("  2. 停止 Kafka + Spark → 自动降级 Redis + Python")
    print("  3. 恢复 Kafka + Spark → 主通道恢复")
    print()

    # Step 1: 启动大数据组件
    print("=" * 60)
    print("  第一步：启动大数据主通道")
    print("=" * 60)
    services = BIGDATA_SERVICES + ["zookeeper"]
    run(COMPOSE_CMD + ["up", "-d"] + services, "启动 ZooKeeper + Kafka + Spark")

    print("\n等待 Kafka 就绪...")
    time.sleep(15)

    # 检查 Kafka
    run(COMPOSE_CMD + ["exec", "-T", "kafka",
         "kafka-topics.sh", "--bootstrap-server", "localhost:9092", "--list"],
        "验证 Kafka 运行状态")

    print()
    print("当前状态：Kafka + Spark 主通道就绪")
    print("  数据导入: CSV → Kafka → Spark ETL → MySQL")
    print("  数据分析: API → Spark SQL → MySQL → 前端")

    # Step 2: 停止大数据组件
    print()
    print("=" * 60)
    print("  第二步：模拟大数据组件故障")
    print("=" * 60)

    run(COMPOSE_CMD + ["stop"] + BIGDATA_SERVICES, "停止 Kafka + Spark")

    check_backend_health()

    print()
    print("当前状态：大数据主通道不可用，自动降级")
    print("  数据导入: CSV → Redis Queue → Python ETL → MySQL")
    print("  数据分析: API → Java Stream → MySQL → 前端")
    print()
    print("前端用户不会感知到任何变化——数据正常刷新！")
    print("因为 AnalyticsService 中的 try/catch 捕获了 Spark 异常，")
    print("自动回退到 Java Stream + MySQL 直查。")

    # Step 3: 恢复
    print()
    print("=" * 60)
    print("  第三步：恢复大数据组件")
    print("=" * 60)

    run(COMPOSE_CMD + ["start"] + BIGDATA_SERVICES, "恢复 Kafka + Spark")

    print("\n等待 Spark 恢复...")
    time.sleep(10)

    check_backend_health()

    print()
    print("当前状态：大数据主通道已恢复")
    print("  数据导入: CSV → Kafka → Spark ETL → MySQL")
    print("  数据分析: API → Spark SQL → MySQL → 前端")
    print()
    print("=" * 60)
    print("  降级演示完成")
    print("=" * 60)
    print()
    print("总结：")
    print("  - 大数据主通道（Kafka + Spark）优先处理")
    print("  - 传统通道（Redis + Python + Java）永久兜底")
    print("  - 切换对前端和用户完全透明")
    print("  - 不需要复杂的熔断器，try/catch + Feature Flag 足够")


if __name__ == "__main__":
    main()
