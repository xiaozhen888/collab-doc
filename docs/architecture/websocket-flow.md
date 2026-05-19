# WebSocket 实时协作流程图

## 连接建立流程

\`\`\`mermaid
sequenceDiagram
participant U as 用户
participant F as 前端(Vue)
participant B as 后端(Spring)
participant R as 房间管理器

    U->>F: 打开文档页面
    F->>B: WebSocket 连接 (带 userId)
    B->>R: 建立连接
    R-->>B: 返回连接成功
    B-->>F: 连接成功
    F->>B: 发送 join 消息 (docId)
    B->>R: 用户加入房间
    R->>R: 更新在线人数
    B-->>F: 发送 init 消息 (当前文档内容)
    B-->>F: 发送 presence 消息 (在线人数)
    F-->>U: 显示文档内容
\`\`\`

## 编辑同步流程

\`\`\`mermaid
sequenceDiagram
participant U as 用户A
participant F1 as 前端A
participant B as 后端
participant R as 房间管理器
participant F2 as 前端B

    U->>F1: 输入文字
    F1->>B: 发送 update 消息
    B->>B: 保存到数据库
    B->>R: 广播给房间内其他用户
    R->>F2: 转发 update 消息
    F2-->>U2: 实时更新显示
\`\`\`

## 断线重连流程

\`\`\`mermaid
sequenceDiagram
participant F as 前端
participant B as 后端

    Note over F,B: 正常连接状态
    F--xB: 网络断开
    F->>F: 检测到连接关闭
    F->>F: 启动重连定时器(3秒)
    F->>B: 重新建立 WebSocket 连接
    B-->>F: 连接成功
    F->>B: 重新发送 join 消息
    B-->>F: 发送当前文档内容
\`\`\`