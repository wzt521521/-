# 数据分析前端

张博源负责的数据大屏和岗位分析页面，使用 Vue 3、Element Plus 与 ECharts。

```bash
npm ci
npm run dev
npm run build
```

开发服务器默认运行在 `http://localhost:5173`，并将 `/api` 代理到 `http://localhost:8080`。
登录模块合并后，将其签发的 `accessToken` 写入 `localStorage`；请求拦截器会自动添加 Bearer Token。

同学 B 的 JWT 模块尚未合并时，可只在本地进程中设置 `VITE_DEV_USERNAME` 和
`VITE_DEV_PASSWORD`，使用 Spring Boot 临时 HTTP Basic 账号联调。生产构建不会启用该回退。
