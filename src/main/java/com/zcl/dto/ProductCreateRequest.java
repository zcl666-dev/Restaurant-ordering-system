package com.zcl.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateRequest {
    private Long id;
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
    private Integer isExchangeable;
    private Integer status;
    private List<Long> optionGroupIds; // 关联的规格组 ID 列表
}
