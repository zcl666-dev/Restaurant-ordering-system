package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String productName;
    private String productImage;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer salesCount;
    private Integer productType;
    private Integer hasOptions;
    private Integer isRecommend;
    private Integer isHot;
    private Integer isExchangeable;
    private Integer status;
    private List<Long> optionGroupIds; // 关联的规格组 ID 列表
}
