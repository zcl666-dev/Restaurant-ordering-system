package com.zcl.dao;

import com.zcl.entity.PointsMall;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PointsMallDao extends BaseDao<PointsMall, Long> {

    @SuppressWarnings("unchecked")
    public List<PointsMall> findByStatus(Integer status) {
        return getCurrentSession()
                .createQuery("FROM PointsMall WHERE status = :status ORDER BY createdAt DESC")
                .setParameter("status", status)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<PointsMall> findAllWithPaging(int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM PointsMall ORDER BY createdAt DESC")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    public PointsMall findByProductId(Long productId) {
        return findOneByHql("FROM PointsMall WHERE product.id = ?1", productId);
    }
}
