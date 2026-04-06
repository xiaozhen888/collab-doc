# 实时协作文档编辑器

基于 Yjs + Spring Boot + Vue3 的实时多人协作文档系统，支持多人同时编辑、光标同步、版本历史。

## 功能特性

- [x] 实时协同编辑（基于 Yjs CRDT 算法）
- [x] 多人光标位置同步
- [x] 在线用户列表
- [x] 文档权限管理（只读/编辑/所有者）
- [ ] 版本历史与回滚（开发中）
- [ ] 评论与批注（开发中）

## 技术架构
前端 (Vue3 + Yjs)  ←WebSocket→  后端 (Spring Boot)  ←→  Redis + MySQL

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3, Yjs, Quill, Axios |
| 后端 | Spring Boot, WebSocket, MyBatis-Plus |
| 数据 | MySQL 8, Redis 6 |
| 部署 | Docker, Docker Compose |

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0
- Redis 6.0

### 启动步骤

```bash
# 1. 克隆项目
git clone https://github.com/xiaozhen888/collab-doc.git

# 2. 启动基础设施
cd docs/deploy
docker-compose up -d

# 3. 启动后端
cd backend
mvn spring-boot:run

# 4. 启动前端
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173

## 项目截图
（待补充）

## 许可证
MIT
