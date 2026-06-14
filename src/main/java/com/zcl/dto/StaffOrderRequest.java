package com.zcl.dto;

import lombok.Data;

import java.util.List;

/**
 * 代客点餐请求DTO
 */
@Data
public class StaffOrderRequest {

    /**
     * 顾客用户ID（可选，不传则归属系统默认用户）
     */
    private Long userId;

    /**
     * 就餐方式：堂食/外带
     */
    private String diningType;

    /**
     * 桌号
     */
    private String tableNumber;

    /**
     * 备注
     */
    private String remark;

    /**
     * 商品列表
     */
    private List<OrderItem> items;

    @Data
    public static class OrderItem {
        /**
         * 商品ID
         */
        private Long productId;

        /**
         * 数量
         */
        private Integer quantity = 1;

        /**
         * 规格快照JSON（可选）
         */
        private String optionSnapshot;
    }
}
