package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品分类及商品列表 VO（嵌套结构）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithProductsVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 该分类下的商品列表
     */
    private List<ProductVO> products;
}
