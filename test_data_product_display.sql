-- =========================================
-- 商品展示接口测试数据（嵌套结构）
-- =========================================

-- 1. 插入商品分类测试数据（status = 1 表示启用）
INSERT INTO `product_category` (`category_name`, `icon`, `sort_order`, `status`) VALUES
('热销', '/static/icons/hot.png', 1, 1),
('主食套餐', '/static/icons/main.png', 2, 1),
('小吃甜品', '/static/icons/snack.png', 3, 1),
('饮料饮品', '/static/icons/drink.png', 4, 1),
('测试分类-禁用', '/static/icons/test.png', 5, 0);  -- 这个不会被查询出来

-- 2. 插入商品测试数据（status = 1 表示上架）
INSERT INTO `product` (`category_id`, `product_name`, `product_image`, `description`, `price`, `stock`, `sales_count`, `product_type`, `has_options`, `is_recommend`, `is_hot`, `status`) VALUES
(1, '红烧肉', '/static/p1.jpg', '肥瘦相间，入口即化', 48.00, 100, 50, 0, 0, 1, 1, 1),
(1, '宫保鸡丁', '/static/p2.jpg', '经典川菜，香辣可口', 38.00, 150, 80, 0, 0, 1, 0, 1),
(2, '红烧牛肉面', '/static/p3.jpg', '大块牛肉，汤浓味美', 32.00, 80, 120, 0, 0, 0, 1, 1),
(2, '扬州炒饭', '/static/p4.jpg', '粒粒分明，口感丰富', 22.00, 120, 95, 0, 0, 0, 0, 1),
(3, '蛋挞', '/static/p5.jpg', '酥脆可口，奶香浓郁', 8.00, 200, 150, 0, 0, 1, 0, 1),
(3, '提拉米苏', '/static/p6.jpg', '意式经典，入口即化', 25.00, 50, 60, 0, 0, 0, 0, 1),
(4, '可乐', '/static/p7.jpg', '冰爽畅饮', 5.00, 300, 200, 0, 0, 0, 0, 1),
(4, '果汁', '/static/p8.jpg', '新鲜榨取，营养健康', 12.00, 100, 70, 0, 0, 0, 0, 1),
(1, '测试商品-下架', '/static/p9.jpg', '这个商品不会被查询出来', 99.00, 0, 0, 0, 0, 0, 0, 0);  -- status = 0，不会显示

-- 说明：
-- 1. product_category 表中 status = 1 的分类才会被查询出来
-- 2. product 表中 status = 1 的商品才会被查询出来
-- 3. 分类按照 sort_order 升序排列
-- 4. 前端调用 GET /api/product/display 接口即可获取这些数据
-- 5. 返回的数据结构为：每个分类下包含该分类的商品列表（嵌套结构）
