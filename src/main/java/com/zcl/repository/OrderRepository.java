package com.zcl.repository;

import com.zcl.entity.Orders;
import com.zcl.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long>, JpaSpecificationExecutor<Orders> {

    List<Orders> findByUserOrderByCreatedAtDesc(User user);

    Page<Orders> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Orders> findByOrderStatus(Integer orderStatus, Pageable pageable);

    @Query("SELECT o FROM Orders o WHERE o.createdAt BETWEEN :start AND :end ORDER BY o.createdAt DESC")
    Page<Orders> findByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT o FROM Orders o WHERE o.paymentStatus = 1 AND o.createdAt BETWEEN :start AND :end")
    List<Orders> findPaidOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.payAmount), 0) FROM Orders o WHERE o.paymentStatus = 1")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.payAmount), 0) FROM Orders o WHERE o.paymentStatus = 1 AND o.createdAt BETWEEN :start AND :end")
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByOrderStatus(Integer orderStatus);

    @Query("SELECT COUNT(o) FROM Orders o WHERE o.createdAt BETWEEN :start AND :end")
    long countBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o.orderStatus, COUNT(o) FROM Orders o GROUP BY o.orderStatus")
    List<Object[]> countGroupByStatus();
}
