package com.zcl.dao;

import com.zcl.entity.Cart;
import com.zcl.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public class CartDao extends BaseDao<Cart, Long> {

    public Cart findByUser(User user) {
        return findOneByHql("FROM Cart WHERE user = ?1", user);
    }

    public Cart findByUserId(Long userId) {
        return findOneByHql("FROM Cart WHERE user.id = ?1", userId);
    }
}
