package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情 VO（包含规格信息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailVO {

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
    private String description;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 销量
     */
    private Integer salesCount;

    /**
     * 状态 0下架 1上架 2售罄
     */
    private Integer status;

    /**
     * 商品类型 0普通商品 1可兑换商品
     */
    private Integer productType;

    /**
     * 规格组列表
     */
    private List<OptionGroupVO> optionGroups;
}
