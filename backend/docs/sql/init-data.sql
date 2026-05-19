-- 插入测试用户
INSERT INTO user (id, username, password, email, create_time) VALUES
('user1', 'testuser1', '123456', 'test1@tjufe.edu.cn', NOW()),  
('user2', 'testuser2', '123456', 'test2@tjufe.edu.cn', NOW()),  
('user3', 'testuser3', '123456', 'test3@tjufe.edu.cn', NOW());  

-- 插入测试文档
INSERT INTO document (id, title, content, owner_id, create_time, update_time) VALUES
('doc1', '压测文档1', '初始内容', 'user1', NOW(), NOW()),
('doc2', '压测文档2', '初始内容', 'user1', NOW(), NOW()),
('doc3', '压测文档3', '初始内容', 'user2', NOW(), NOW());

-- 插入权限
INSERT INTO permission (id, doc_id, user_id, permission, create_time) VALUES
(1, 'doc1', 'user1', 'manage', NOW()),
(2, 'doc1', 'user2', 'write', NOW()),
(3, 'doc1', 'user3', 'read', NOW()),
(4, 'doc2', 'user1', 'manage', NOW()),
(5, 'doc3', 'user2', 'manage', NOW());