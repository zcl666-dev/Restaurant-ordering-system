package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品展示 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 商品描述
     */
    private String desc;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 月销量
     */
    private Integer salesCount;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 是否有规格 0无 1有
     */
    private Integer hasOptions;
}
