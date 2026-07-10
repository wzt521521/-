# Redis 本地运行

Docker 不可用时，可安装 Windows 便携版 Redis：

```powershell
winget install --id taizod1024.redis-windows-fork --exact --scope user
```

从项目目录运行：

```powershell
.\scripts\start-redis.ps1
.\scripts\stop-redis.ps1
```

Windows 配置仅绑定 `127.0.0.1` 并开启保护模式，数据与日志写入工作区的 `.runtime/redis`。
Docker 环境继续使用 `redis/redis.conf`。
