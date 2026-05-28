package com.zcl.repository;

import com.zcl.entity.OrderItem;
import com.zcl.entity.Orders;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Orders order);

    @Query("SELECT oi.product.id, oi.productNameSnapshot, SUM(oi.quantity), SUM(oi.subtotalAmount) " +
            "FROM OrderItem oi GROUP BY oi.product.id, oi.productNameSnapshot ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopProducts(Pageable pageable);
}
