package com.zcl.repository;

import com.zcl.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品分类数据访问层
 */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    /**
     * 查询所有启用的商品分类，按排序字段升序排列
     */
    List<ProductCategory> findByStatusOrderBySortOrderAsc(Integer status);
}
