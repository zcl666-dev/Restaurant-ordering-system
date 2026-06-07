package com.zcl.dao;

import com.zcl.entity.ProductOptionRelation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductOptionRelationDao extends BaseDao<ProductOptionRelation, Long> {

    @SuppressWarnings("unchecked")
    public List<ProductOptionRelation> findByProductId(Long productId) {
        return getCurrentSession()
                .createQuery("FROM ProductOptionRelation WHERE product.id = :productId")
                .setParameter("productId", productId)
                .list();
    }
}
