package com.zcl.dao;

import com.zcl.entity.OptionGroup;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OptionGroupDao extends BaseDao<OptionGroup, Long> {

    @SuppressWarnings("unchecked")
    public List<OptionGroup> findByProductId(Long productId) {
        return getCurrentSession()
                .createQuery("SELECT por.group FROM ProductOptionRelation por WHERE por.product.id = :productId ORDER BY por.sortOrder")
                .setParameter("productId", productId)
                .list();
    }
}
