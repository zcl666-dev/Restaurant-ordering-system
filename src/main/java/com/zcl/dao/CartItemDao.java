package com.zcl.dao;

import com.zcl.entity.Cart;
import com.zcl.entity.CartItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CartItemDao extends BaseDao<CartItem, Long> {

    @SuppressWarnings("unchecked")
    public List<CartItem> findByCart(Cart cart) {
        return getCurrentSession()
                .createQuery("FROM CartItem WHERE cart = :cart")
                .setParameter("cart", cart)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<CartItem> findByCartId(Long cartId) {
        return getCurrentSession()
                .createQuery("FROM CartItem WHERE cart.id = :cartId")
                .setParameter("cartId", cartId)
                .list();
    }

    public void deleteByCart(Cart cart) {
        getCurrentSession()
                .createQuery("DELETE FROM CartItem WHERE cart = :cart")
                .setParameter("cart", cart)
                .executeUpdate();
    }
}
