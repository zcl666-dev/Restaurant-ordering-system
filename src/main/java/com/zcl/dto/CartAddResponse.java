package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 加入购物车响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartAddResponse {

    /**
     * 购物车ID
     */
    private Long cartId;

    /**
     * 商品总数量
     */
    private Integer totalQuantity;

    /**
     * 商品总金额
     */
    private BigDecimal totalAmount;
}
