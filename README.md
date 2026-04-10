# 网约车平台司机评价信息管理系统

基于 Spring Boot + Vue 3 的网约车平台管理系统，支持乘客下单、司机接单派单、行程评价、投诉申诉、处罚管理、公告管理等完整业务流程。

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.x, MyBatis-Plus |
| 前端框架 | Vue 3 + Vite, Element Plus, Pinia |
| 数据库 | MySQL 8.x |
| 认证 | JWT |
| API 文档 | Knife4j (Swagger) |

## 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+

## 快速启动

### 1. 初始化数据库

```bash
mysql -u root -p < driver-eval-backend/src/main/resources/schema.sql
```

该脚本会自动创建 `driver_eval` 数据库、所有表和测试数据。

### 2. 启动后端

```bash
cd driver-eval-backend
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8088`，API 文档地址 `http://localhost:8088/doc.html`。

可通过环境变量覆盖敏感配置：

```bash
export DB_PASSWORD=your_password
export JWT_SECRET=your_secret
```

### 3. 启动前端

```bash
cd driver-eval-frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`。

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 系统管理员 |
| 乘客 | passenger1 | 123456 | 张三 |
| 乘客 | passenger2 | 123456 | 李四 |
| 司机 | driver1 | 123456 | 王师傅 |
| 司机 | driver2 | 123456 | 赵师傅 |
| 司机 | driver3 | 123456 | 刘师傅 |

## 项目结构

```
├── driver-eval-backend/          # Spring Boot 后端
│   └── src/main/java/com/drivereval/
│       ├── controller/           # REST 接口 (admin / driver / passenger / auth)
│       ├── service/              # 业务逻辑
│       ├── mapper/               # MyBatis-Plus 数据访问
│       ├── entity/               # 实体类
│       ├── common/               # 通用工具、枚举、异常处理
│       ├── config/               # 安全、跨域、Swagger 配置
│       └── task/                 # 定时任务
├── driver-eval-frontend/         # Vue 3 前端
│   └── src/
│       ├── views/                # 页面组件 (admin / driver / passenger)
│       ├── api/                  # 后端 API 调用
│       ├── router/               # 路由配置
│       ├── layout/               # 布局组件
│       ├── components/           # 公共组件
│       └── utils/                # 工具函数
└── 需求文档.docx                  # 项目需求文档
```

## 核心功能

**乘客端** -- 创建订单、查看行程、评价司机、投诉管理

**司机端** -- 接单/派单、行程管理、评价查看、申诉处理、评分统计、车辆管理

**管理后台** -- 用户管理、订单管理、评价审核、投诉/申诉处理、处罚管理、敏感词过滤、标签管理、标签统计、车辆/车型管理、系统公告、数据看板

## 系统架构

```
┌──────────────┐       HTTP/JSON       ┌──────────────────┐
│   Vue 3 SPA  │  ◄──────────────────► │  Spring Boot API │
│  Element Plus│       (JWT Auth)      │  MyBatis-Plus    │
│    Vite      │                       │  Knife4j         │
└──────────────┘                       └────────┬─────────┘
                                                │
                                       ┌────────▼─────────┐
                                       │     MySQL 8      │
                                       │   driver_eval    │
                                       └──────────────────┘
```
