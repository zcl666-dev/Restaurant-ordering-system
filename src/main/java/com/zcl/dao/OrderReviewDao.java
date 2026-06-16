package com.zcl.dao;

import com.zcl.entity.OrderReview;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderReviewDao extends BaseDao<OrderReview, Long> {

    /**
     * 根据订单ID查询评价
     */
    public OrderReview findByOrderId(Long orderId) {
        return findOneByHql("FROM OrderReview WHERE order.id = ?1", orderId);
    }

    /**
     * 分页查询所有评价
     */
    @SuppressWarnings("unchecked")
    public List<OrderReview> findAllWithPaging(int page, int size) {
        return getCurrentSession()
                .createQuery("FROM OrderReview ORDER BY createdAt DESC")
                .setFirstResult(page * size)
                .setMaxResults(size)
                .list();
    }

    /**
     * 评价总数
     */
    public long countAll() {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM OrderReview")
                .uniqueResult();
    }

    /**
     * 按评分筛选查询
     */
    @SuppressWarnings("unchecked")
    public List<OrderReview> findByRating(Integer rating, int page, int size) {
        return getCurrentSession()
                .createQuery("FROM OrderReview WHERE rating = ?1 ORDER BY createdAt DESC")
                .setParameter(1, rating)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .list();
    }

    /**
     * 按评分筛选总数
     */
    public long countByRating(Integer rating) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM OrderReview WHERE rating = ?1")
                .setParameter(1, rating)
                .uniqueResult();
    }
}
