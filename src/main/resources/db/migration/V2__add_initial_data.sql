-- 添加管理員用戶
INSERT INTO users (username, email, password, roles, credit_score, created_at)
VALUES (
    'admin',
    'admin@example.com',
    '$2a$10$rK6Hy0P2pUbHF.h9BX.mAOEkFq3YhrgFU9LxZXn8HO8tYiFgJ.bGi', -- password: admin123
    ARRAY['ADMIN'],
    5.0,
    CURRENT_TIMESTAMP
);

-- 添加測試用戶
INSERT INTO users (username, email, password, roles, credit_score, created_at)
VALUES 
    ('test_initiator', 'initiator@example.com', 
     '$2a$10$rK6Hy0P2pUbHF.h9BX.mAOEkFq3YhrgFU9LxZXn8HO8tYiFgJ.bGi',
     ARRAY['INITIATOR'], 5.0, CURRENT_TIMESTAMP),
    ('test_purchaser', 'purchaser@example.com',
     '$2a$10$rK6Hy0P2pUbHF.h9BX.mAOEkFq3YhrgFU9LxZXn8HO8tYiFgJ.bGi',
     ARRAY['PURCHASER'], 5.0, CURRENT_TIMESTAMP);

-- 添加示例訂單
INSERT INTO orders (
    initiator_id,
    product_name,
    product_price,
    quantity,
    description,
    status,
    delivery_address,
    delivery_country,
    deadline,
    created_at
)
SELECT 
    u.id,
    '示例商品',
    999.99,
    1,
    '這是一個示例訂單',
    'PENDING',
    '香港示例地址',
    'HK',
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    CURRENT_TIMESTAMP
FROM users u
WHERE u.username = 'test_initiator';

-- 添加示例關鍵字訂閱
INSERT INTO keyword_subscriptions (user_id, keyword, created_at)
SELECT 
    u.id,
    '示例關鍵字',
    CURRENT_TIMESTAMP
FROM users u
WHERE u.username = 'test_purchaser';

-- 添加示例聊天室
INSERT INTO chat_rooms (
    name,
    order_id,
    type,
    created_at
)
SELECT 
    '示例聊天室',
    o.id,
    'ORDER',
    CURRENT_TIMESTAMP
FROM orders o
LIMIT 1;

-- 添加示例聊天消息
INSERT INTO chat_messages (
    chat_room_id,
    sender_id,
    content,
    type,
    created_at
)
SELECT 
    cr.id,
    u.id,
    '歡迎使用P2P代購平台',
    'SYSTEM',
    CURRENT_TIMESTAMP
FROM chat_rooms cr
CROSS JOIN users u
WHERE u.username = 'admin'
LIMIT 1;

-- 添加示例通知
INSERT INTO notifications (
    user_id,
    type,
    content,
    created_at
)
SELECT 
    u.id,
    'SYSTEM',
    '歡迎加入P2P代購平台',
    CURRENT_TIMESTAMP
FROM users u
WHERE u.username != 'admin'; 