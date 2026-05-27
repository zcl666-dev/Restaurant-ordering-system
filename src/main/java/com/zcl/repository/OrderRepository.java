package com.zcl.repository;

import com.zcl.entity.Orders;
import com.zcl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

    List<Orders> findByUserOrderByCreatedAtDesc(User user);
}