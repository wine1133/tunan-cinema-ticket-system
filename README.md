# 兔南电影购票系统

一个可直接运行和公网演示的全栈电影购票系统，包含用户购票端、影院管理端、平台管理端和 MySQL 数据库。

## 技术栈

- 前端：Vue 3、Vite、Vue Router、Element Plus、Axios、ECharts
- 后端：Java 17、Spring Boot 3、MyBatis、Hutool、JWT、AI 客服混合问答
- 数据库：MySQL 8.0
- 部署：Spring Boot 内嵌静态前端、Cloudflare Tunnel 临时公网访问、Docker Compose

## 一键启动

在 Windows 上双击 **一键启动.bat**，或在 PowerShell 中运行：

```powershell
powershell -ExecutionPolicy Bypass -File .\start.ps1
```

脚本会自动完成：检查 MySQL、创建数据库、构建前端、构建后端、启动本地服务、创建公网地址。

只启动本地服务：

```powershell
powershell -ExecutionPolicy Bypass -File .\start.ps1 -NoTunnel
```

停止服务：

```powershell
powershell -ExecutionPolicy Bypass -File .\stop.ps1
```

如果本机 MySQL 不是 `root/root`，先设置环境变量：

```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = "你的密码"
.\start.ps1
```

## Docker 启动

```bash
docker compose up --build -d
```

启动后访问 http://localhost:8080。MySQL 数据保存在 Docker volume 中。

## 演示账号

| 角色 | 账号 | 密码 | 入口 |
| --- | --- | --- | --- |
| 影迷用户 | `tunan` | `123456` | 前台购票 |
| 影院管理员 | `cinema` | `123456` | 影院管理端 |
| 平台管理员 | `admin` | `123456` | 平台管理端 |

## 功能

### 用户端

- 首页 30 秒冬日先导片轮播、热映、待映、票房榜、评分榜、今日票房
- 多条件电影筛选、电影详情、演职人员、预告片
- 多影院多场次排片与在线选座
- 模拟支付、取票码、订单查询和取消
- 收藏电影、评分短评、个人资料与密码修改
- 悬浮 AI 客服：常见问题即时回复，复杂问题可选接入大模型，对话写入 MySQL

### 影院端

- 影院资料维护与平台认证状态
- 8 × 8 座位影厅管理
- 排片申请、修改、删除
- 本影院订单查询与取票核销

### 平台端

- 票房、电影、影院、用户数据总览
- ECharts 七日票房趋势、类型分布、类型票房
- 电影、类型、演职人员、公告、用户与管理员管理
- 影院认证审批、排片审批、全站订单

## 目录结构

```text
电影购票系统/
├─ backend/          Spring Boot 后端与打包后的静态前端
├─ frontend/         Vue 3 前端源码
├─ database/         MySQL 建表与初始化数据
├─ docker-compose.yml
├─ Dockerfile
├─ start.ps1         一键启动并创建公网地址
├─ stop.ps1
└─ 一键启动.bat
```

## 数据库

默认数据库名：`tunan_cinema`。

`database/schema.sql` 负责建表，`database/data.sql` 负责初始化演示数据。后端启动时也会自动执行这两份脚本，重复启动不会重复插入初始化数据。

## AI 客服配置

客服采用“知识库 + 大模型”的混合模式：

- 未配置密钥时，系统使用内置知识库自动回答购票、选座、支付、取票、取消订单、影院地址和影片推荐等常见问题。
- 配置 OpenAI 兼容接口后，知识库无法回答的问题会交给大模型；大模型不可用时会自动回退到知识库。
- 每次用户提问和客服回复都会写入 MySQL 的 `customer_service_messages` 表。

可选环境变量：

```powershell
$env:OPENAI_API_KEY = "你的服务端 API Key"
$env:OPENAI_BASE_URL = "https://api.openai.com/v1"
$env:OPENAI_MODEL = "gpt-4o-mini"
```

API Key 只放在服务端环境变量中，不会发送到浏览器，也不要提交到 GitHub。
## 公网部署说明

`start.ps1` 使用 Cloudflare Quick Tunnel 创建免登录的临时 HTTPS 地址，适合课程演示和验收。该地址在服务停止后会失效。如需长期固定域名，可将项目部署到云服务器，并配置 Nginx、HTTPS 和正式 Cloudflare Tunnel。

## 素材说明

电影海报使用对应影片的公开海报素材，仅用于课程设计与功能演示，版权归原片方所有。

## 安全说明

演示系统默认密码仅用于本地验收。正式上线前请修改数据库密码、`JWT_SECRET`、管理员密码，并按需关闭公网数据库端口。
