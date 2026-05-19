# WebSocket 消息协议文档

## 连接地址

- 开发环境: `ws://localhost:8080/collab`
- 生产环境: `wss://your-domain.com/collab`

## 消息格式

所有消息均为 JSON 格式。

---

## 客户端 → 服务端

### 1. 加入房间 (join)

用户进入文档时发送。

\`\`\`json
{
"type": "join",
"docId": "文档ID"
}
\`\`\`

### 2. 编辑内容 (update)

用户编辑文档时发送。

\`\`\`json
{
"type": "update",
"docId": "文档ID",
"content": "文档完整内容"
}
\`\`\`

---

## 服务端 → 客户端

### 1. 初始化 (init)

用户加入房间后，服务端返回当前文档内容。

\`\`\`json
{
"type": "init",
"content": "文档内容"
}
\`\`\`

### 2. 内容更新 (update)

其他用户编辑时，广播给房间内其他用户。

\`\`\`json
{
"type": "update",
"content": "新内容"
}
\`\`\`

### 3. 在线状态 (presence)

用户加入或离开时，广播在线人数。

\`\`\`json
{
"type": "presence",
"connectionCount": 2,
"userCount": 1
}
\`\`\`

---

## 错误处理

| 状态码 | 含义 | 处理方式 |
|--------|------|---------|
| 403 | 无权限 | 跳转登录页 |
| 500 | 服务器错误 | 重试或提示用户 |