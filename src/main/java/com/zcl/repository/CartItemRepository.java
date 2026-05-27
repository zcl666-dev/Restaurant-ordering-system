package com.zcl.repository;

import com.zcl.entity.Cart;
import com.zcl.entity.CartItem;
import com.zcl.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 购物车明细数据访问层
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * 查询购物车中的所有商品项
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * 根据购物车和商品查询所有商品项（用于在Java层比较optionSnapshot）
     */
    List<CartItem> findByCartAndProduct(Cart cart, Product product);
}
