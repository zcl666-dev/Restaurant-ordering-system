package com.zcl.repository;

import com.zcl.entity.Cart;
import com.zcl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 购物车数据访问层
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * 查询用户的活跃购物车
     */
    Optional<Cart> findByUserAndCartStatus(User user, String cartStatus);
}
