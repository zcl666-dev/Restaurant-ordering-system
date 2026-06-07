package com.zcl.dao;

import com.zcl.entity.UserCoupon;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserCouponDao extends BaseDao<UserCoupon, Long> {

    @SuppressWarnings("unchecked")
    public List<UserCoupon> findByUserId(Long userId) {
        return getCurrentSession()
                .createQuery("FROM UserCoupon WHERE user.id = :userId ORDER BY createdAt DESC")
                .setParameter("userId", userId)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<UserCoupon> findUnusedByUserId(Long userId) {
        return getCurrentSession()
                .createQuery("FROM UserCoupon WHERE user.id = :userId AND status = 0 ORDER BY createdAt DESC")
                .setParameter("userId", userId)
                .list();
    }
}
