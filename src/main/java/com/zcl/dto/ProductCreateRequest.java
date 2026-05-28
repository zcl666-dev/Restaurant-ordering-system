package com.zcl.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequest {
    private Long categoryId;
    private String productName;
    private String productImage;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer productType;
    private Integer hasOptions;
    private Integer isRecommend;
    private Integer isHot;
    private Integer status;
}
