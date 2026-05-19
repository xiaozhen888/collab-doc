# 系统架构图

\`\`\`mermaid
graph TB
subgraph "前端 (Vue 3)"
A[Vue Router] --> B[Views]
B --> C[Components]
C --> D[Pinia Store]
D --> E[API Service]
end

    subgraph "后端 (Spring Boot)"
        F[Controller Layer] --> G[Service Layer]
        G --> H[Mapper Layer]
        H --> I[(MySQL)]
        G --> J[WebSocket Handler]
        J --> K[Room Manager]
    end

    subgraph "基础设施"
        L[Redis] --> G
        M[Docker] --> I
        M --> L
    end

    E -->|HTTP/WebSocket| F
    J -->|实时通信| A

    style A fill:#42b983,color:#fff
    style F fill:#6db33f,color:#fff
    style I fill:#4479a1,color:#fff
    style L fill:#dc382d,color:#fff
\`\`\`