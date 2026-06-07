package com.zcl.dao;

import com.zcl.entity.OrderItem;
import com.zcl.entity.Orders;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemDao extends BaseDao<OrderItem, Long> {

    @SuppressWarnings("unchecked")
    public List<OrderItem> findByOrder(Orders order) {
        return getCurrentSession()
                .createQuery("FROM OrderItem WHERE order = :order")
                .setParameter("order", order)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findTopProducts(int limit) {
        return getCurrentSession()
                .createQuery("SELECT oi.product.id, oi.productNameSnapshot, " +
                        "SUM(oi.quantity) as totalQty, SUM(oi.subtotalAmount) as totalRev " +
                        "FROM OrderItem oi GROUP BY oi.product.id, oi.productNameSnapshot " +
                        "ORDER BY totalQty DESC")
                .setMaxResults(limit)
                .list();
    }
}
