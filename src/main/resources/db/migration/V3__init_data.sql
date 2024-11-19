-- 添加管理员用户
INSERT INTO users (
    username, 
    email, 
    password, 
    roles, 
    credit_score, 
    created_at
) VALUES (
    'admin',
    'admin@p2p-platform.com',
    -- 密码: admin123
    '$2a$10$rK6Hy0P2pUbHF.h9BX.mAOEkFq3YhrgFU9LxZXn8HO8tYiFgJ.bGi',
    ARRAY['ADMIN'],
    5.0,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- 添加系统配置
INSERT INTO system_configs (
    config_key,
    config_value,
    description,
    created_at
) VALUES 
    ('MIN_CREDIT_SCORE', '3.0', '最低信用評分要求', CURRENT_TIMESTAMP),
    ('MAX_ACTIVE_ORDERS', '5', '每個用戶最大進行中訂單數', CURRENT_TIMESTAMP),
    ('ORDER_EXPIRY_DAYS', '7', '訂單過期天數', CURRENT_TIMESTAMP),
    ('RATING_WINDOW_HOURS', '72', '評分時間窗口（小時）', CURRENT_TIMESTAMP),
    ('SYSTEM_CURRENCY', 'HKD', '系統默認貨幣', CURRENT_TIMESTAMP)
ON CONFLICT (config_key) DO NOTHING;

-- 添加系统通知模板
INSERT INTO notification_templates (
    template_code,
    template_title,
    template_content,
    created_at
) VALUES 
    ('ORDER_CREATED', '新訂單創建通知', '您的訂單 #{orderId} 已成功創建', CURRENT_TIMESTAMP),
    ('ORDER_ACCEPTED', '訂單已接受通知', '您的訂單 #{orderId} 已被接受', CURRENT_TIMESTAMP),
    ('ORDER_COMPLETED', '訂單完成通知', '您的訂單 #{orderId} 已完成', CURRENT_TIMESTAMP),
    ('PAYMENT_RECEIVED', '收到付款通知', '已收到訂單 #{orderId} 的付款', CURRENT_TIMESTAMP),
    ('NEW_RATING', '新評分通知', '您收到了一個新的評分', CURRENT_TIMESTAMP)
ON CONFLICT (template_code) DO NOTHING;

-- 添加默认关键字类别
INSERT INTO keyword_categories (
    category_name,
    description,
    created_at
) VALUES 
    ('電子產品', '手機、電腦、相機等電子產品', CURRENT_TIMESTAMP),
    ('美妝護膚', '化妝品、護膚品、香水等', CURRENT_TIMESTAMP),
    ('服飾鞋包', '服裝、鞋子、包包等', CURRENT_TIMESTAMP),
    ('食品零食', '零食、飲料、特產等', CURRENT_TIMESTAMP),
    ('母嬰用品', '嬰兒用品、玩具、童裝等', CURRENT_TIMESTAMP)
ON CONFLICT (category_name) DO NOTHING; 