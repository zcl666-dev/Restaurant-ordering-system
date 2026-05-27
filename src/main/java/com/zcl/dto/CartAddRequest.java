package com.zcl.dto;

import lombok.Data;

/**
 * 加入购物车请求DTO
 */
@Data
public class CartAddRequest {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 规格快照JSON（可选）
     */
    private String optionSnapshot;

    /**
     * 数量（默认为1）
     */
    private Integer quantity = 1;
}
