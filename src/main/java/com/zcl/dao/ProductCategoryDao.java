package com.zcl.dao;

import com.zcl.entity.ProductCategory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductCategoryDao extends BaseDao<ProductCategory, Long> {

    @SuppressWarnings("unchecked")
    public List<ProductCategory> findAllOrderBySortOrder() {
        return getCurrentSession()
                .createQuery("FROM ProductCategory ORDER BY sortOrder ASC")
                .list();
    }
}
