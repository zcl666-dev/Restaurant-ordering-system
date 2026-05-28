package com.zcl.dto;

import lombok.Data;

@Data
public class CategoryCreateRequest {
    private String categoryName;
    private String icon;
    private Integer sortOrder;
    private Integer status;
}
