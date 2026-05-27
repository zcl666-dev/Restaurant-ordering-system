CREATE DATABASE restaurant_order_system;


-- =========================================
-- 1. 用户表（user）
-- =========================================
CREATE TABLE `user` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',

    `openid` VARCHAR(100) NOT NULL UNIQUE COMMENT '微信openid',
    `nickname` VARCHAR(100) COMMENT '微信昵称',
    `avatar_url` VARCHAR(500) COMMENT '微信头像',

    `points_balance` INT NOT NULL DEFAULT 0 COMMENT '当前积分',

    `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额',

    `total_spent_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
    `total_order_count` INT NOT NULL DEFAULT 0 COMMENT '累计订单数',

    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- =========================================
-- 2. 商品分类表（product_category）
-- =========================================
CREATE TABLE `product_category` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',

    `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(500) COMMENT '分类图标',

    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',

    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';


-- =========================================
-- 3. 商品表（product）
-- =========================================
CREATE TABLE `product` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',

    `category_id` BIGINT NOT NULL COMMENT '分类ID',

    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `product_image` VARCHAR(500) COMMENT '商品图片',
    `description` TEXT COMMENT '商品描述',

    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品价格',

    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
    `sales_count` INT NOT NULL DEFAULT 0 COMMENT '销量',

    `product_type` TINYINT NOT NULL DEFAULT 0 COMMENT '0普通商品 1可兑换商品',

    `has_options` TINYINT NOT NULL DEFAULT 0 COMMENT '0无规格 1有规格',

    `is_recommend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐',
    `is_hot` TINYINT NOT NULL DEFAULT 0 COMMENT '是否热销',

    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0下架 1上架 2售罄',

    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT `fk_product_category`
    FOREIGN KEY (`category_id`) REFERENCES `product_category`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';


-- =========================================
-- 4. 规格组表（option_group）
-- =========================================
CREATE TABLE `option_group` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规格组ID',

    `group_name` VARCHAR(50) NOT NULL COMMENT '规格组名称',

    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1启用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规格组表';


-- =========================================
-- 5. 规格值表（option_value）
-- =========================================
CREATE TABLE `option_value` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规格值ID',

    `group_id` BIGINT NOT NULL COMMENT '规格组ID',

    `value_name` VARCHAR(50) NOT NULL COMMENT '规格值名称',

    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认',

    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',

    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',

    CONSTRAINT `fk_option_value_group`
    FOREIGN KEY (`group_id`) REFERENCES `option_group`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规格值表';


-- =========================================
-- 6. 商品规格关联表（product_option_relation）
-- =========================================
CREATE TABLE `product_option_relation` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `group_id` BIGINT NOT NULL COMMENT '规格组ID',

    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '显示排序',

    `is_visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否显示',

    CONSTRAINT `fk_relation_product`
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`),

    CONSTRAINT `fk_relation_group`
    FOREIGN KEY (`group_id`) REFERENCES `option_group`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格关联表';


-- =========================================
-- 7. 购物车表（cart）
-- =========================================
CREATE TABLE `cart` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车ID',

    `user_id` BIGINT NOT NULL COMMENT '用户ID',

    `cart_status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT 'active活跃 converted已转订单',

    `dining_type` VARCHAR(20) COMMENT '堂食/外带',

    `table_number` VARCHAR(20) COMMENT '桌号',

    `remark` VARCHAR(500) COMMENT '整单备注',

    `total_quantity` INT NOT NULL DEFAULT 0 COMMENT '商品总数量',

    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品总金额',

    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT `fk_cart_user`
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';


-- =========================================
-- 8. 购物车明细表（cart_item）
-- =========================================
CREATE TABLE `cart_item` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车商品ID',

    `cart_id` BIGINT NOT NULL COMMENT '购物车ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',

    `product_name_snapshot` VARCHAR(100) NOT NULL COMMENT '商品名称快照',
    `product_image_snapshot` VARCHAR(500) COMMENT '商品图片快照',

    `option_snapshot` JSON COMMENT '规格快照JSON',

    `quantity` INT NOT NULL DEFAULT 1 COMMENT '商品数量',

    `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品单价',

    `subtotal_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '小计金额',

    `coupon_id` BIGINT COMMENT '使用的兑换券ID',

    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT `fk_cart_item_cart`
    FOREIGN KEY (`cart_id`) REFERENCES `cart`(`id`),

    CONSTRAINT `fk_cart_item_product`
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车明细表';


-- =========================================
-- 9. 订单主表（orders）
-- =========================================
CREATE TABLE `orders` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',

    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',

    `user_id` BIGINT NOT NULL COMMENT '用户ID',

    `cart_id` BIGINT COMMENT '来源购物车ID',

    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',

    `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',

    `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',

    `points_earned` INT NOT NULL DEFAULT 0 COMMENT '获得积分',

    `dining_type` VARCHAR(20) COMMENT '堂食/外带',

    `table_number` VARCHAR(20) COMMENT '桌号',

    `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2制作中 3待取餐 4已完成 5已取消 6已退款',

    `payment_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未支付 1已支付 2已退款',

    `payment_method` VARCHAR(50) COMMENT '支付方式',

    `payment_time` DATETIME COMMENT '支付时间',

    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT `fk_orders_user`
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';


-- =========================================
-- 10. 订单明细表（order_item）
-- =========================================
CREATE TABLE `order_item` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单商品ID',

    `order_id` BIGINT NOT NULL COMMENT '订单ID',

    `product_id` BIGINT NOT NULL COMMENT '商品ID',

    `product_name_snapshot` VARCHAR(100) NOT NULL COMMENT '商品名称快照',

    `product_image_snapshot` VARCHAR(500) COMMENT '商品图片快照',

    `option_snapshot` JSON COMMENT '规格快照JSON',

    `quantity` INT NOT NULL DEFAULT 1 COMMENT '商品数量',

    `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品单价',

    `subtotal_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '小计金额',

    CONSTRAINT `fk_order_item_order`
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`),

    CONSTRAINT `fk_order_item_product`
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';


-- =========================================
-- 11. 积分流水表（point_log）
-- =========================================
CREATE TABLE `point_log` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '积分流水ID',

    `user_id` BIGINT NOT NULL COMMENT '用户ID',

    `order_id` BIGINT COMMENT '来源订单ID',

    `user_coupon_id` BIGINT COMMENT '兑换券ID',

    `type` TINYINT NOT NULL COMMENT '1获得 2扣除',

    `points_change` INT NOT NULL COMMENT '积分变动值',

    `balance_after` INT NOT NULL COMMENT '变动后积分',

    `remark` VARCHAR(255) COMMENT '备注',

    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    CONSTRAINT `fk_point_log_user`
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';


-- =========================================
-- 12. 积分商城表（points_mall）
-- =========================================
CREATE TABLE `points_mall` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '积分商城ID',

    `product_id` BIGINT NOT NULL COMMENT '商品ID',

    `points_required` INT NOT NULL COMMENT '兑换所需积分',

    `expire_days` INT NOT NULL DEFAULT 7 COMMENT '兑换券有效天数',

    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',

    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT `fk_points_mall_product`
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商城表';


-- =========================================
-- 13. 用户兑换券表（user_coupon）
-- =========================================
CREATE TABLE `user_coupon` (
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户兑换券ID',

    `user_id` BIGINT NOT NULL COMMENT '用户ID',

    `mall_id` BIGINT NOT NULL COMMENT '积分商城ID',

    `product_id` BIGINT NOT NULL COMMENT '商品ID',

    `coupon_name_snapshot` VARCHAR(100) NOT NULL COMMENT '兑换券名称快照',

    `coupon_image_snapshot` VARCHAR(500) COMMENT '兑换券图片快照',

    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1未使用 2已使用 3已过期',

    `acquired_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',

    `expire_at` DATETIME COMMENT '过期时间',

    `used_at` DATETIME COMMENT '使用时间',

    `order_id` BIGINT COMMENT '使用订单ID',

    CONSTRAINT `fk_user_coupon_user`
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),

    CONSTRAINT `fk_user_coupon_mall`
    FOREIGN KEY (`mall_id`) REFERENCES `points_mall`(`id`),

    CONSTRAINT `fk_user_coupon_product`
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户兑换券表';