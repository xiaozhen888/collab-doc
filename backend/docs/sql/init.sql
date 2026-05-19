-- ============================================
-- 创建表
-- ============================================

-- 用户表
CREATE TABLE user (
    id VARCHAR(50) PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    email VARCHAR(100) COMMENT '邮箱',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 文档表
CREATE TABLE document (
    id VARCHAR(50) PRIMARY KEY COMMENT '文档ID',
    title VARCHAR(100) NOT NULL COMMENT '文档标题',
    content LONGTEXT COMMENT '文档内容',
    owner_id VARCHAR(50) NOT NULL COMMENT '所有者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档表';

-- 权限表
CREATE TABLE permission (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    doc_id VARCHAR(50) NOT NULL COMMENT '文档ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    permission VARCHAR(20) NOT NULL COMMENT '权限类型：manage/write/read',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_doc_user (doc_id, user_id),
    INDEX idx_doc_id (doc_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档权限表';

-- 文档历史记录表
CREATE TABLE doc_history (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '历史ID',
    doc_id VARCHAR(50) NOT NULL COMMENT '文档ID',
    content LONGTEXT COMMENT '文档内容快照',
    operator_id VARCHAR(50) COMMENT '操作人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_doc_id (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档历史记录表';

-- 分享链接表
CREATE TABLE share_link (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '链接ID',
    doc_id VARCHAR(50) NOT NULL COMMENT '文档ID',
    link_token VARCHAR(100) NOT NULL UNIQUE COMMENT '分享令牌',
    permission VARCHAR(20) DEFAULT 'read' COMMENT '分享权限',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_doc_id (doc_id),
    INDEX idx_link_token (link_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享链接表';