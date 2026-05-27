-- =========================================
-- 商品详情接口测试数据
-- =========================================

-- 1. 插入规格组测试数据
INSERT INTO `option_group` (`group_name`, `status`) VALUES
('辣度', 1),
('份量', 1),
('温度', 1);

-- 2. 插入规格值测试数据
-- 辣度规格值
INSERT INTO `option_value` (`group_id`, `value_name`, `is_default`, `sort_order`, `status`) VALUES
(1, '不辣', 1, 1, 1),
(1, '微辣', 0, 2, 1),
(1, '中辣', 0, 3, 1),
(1, '特辣', 0, 4, 1);

-- 份量规格值
INSERT INTO `option_value` (`group_id`, `value_name`, `is_default`, `sort_order`, `status`) VALUES
(2, '小份', 0, 1, 1),
(2, '中份', 1, 2, 1),
(2, '大份', 0, 3, 1);

-- 温度规格值
INSERT INTO `option_value` (`group_id`, `value_name`, `is_default`, `sort_order`, `status`) VALUES
(3, '常温', 1, 1, 1),
(3, '加冰', 0, 2, 1),
(3, '去冰', 0, 3, 1);

-- 3. 插入商品规格关联数据（假设商品ID=1）
INSERT INTO `product_option_relation` (`product_id`, `group_id`, `sort_order`, `is_visible`) VALUES
(1, 1, 1, 1),  -- 商品1关联辣度规格组
(1, 2, 2, 1);  -- 商品1关联份量规格组

-- 说明：
-- 1. option_group 表中 status = 1 表示启用
-- 2. option_value 表中 status = 1 表示启用
-- 3. product_option_relation 表中 is_visible = 1 表示该规格组在商品详情页显示
-- 4. 前端调用 GET /api/product/1 接口即可获取商品详情及规格信息
