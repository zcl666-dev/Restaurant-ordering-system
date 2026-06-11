package com.zcl.dao;

import com.zcl.entity.UserExchangeVoucher;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UserExchangeVoucherDao extends BaseDao<UserExchangeVoucher, Long> {

    @SuppressWarnings("unchecked")
    public List<UserExchangeVoucher> findByUserId(Long userId) {
        return getCurrentSession()
                .createQuery("FROM UserExchangeVoucher WHERE user.id = :userId ORDER BY createdAt DESC")
                .setParameter("userId", userId)
                .list();
    }

    public long countByUserIdAndStatus(Long userId, Integer status) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM UserExchangeVoucher WHERE user.id = :userId AND status = :status")
                .setParameter("userId", userId)
                .setParameter("status", status)
                .uniqueResult();
    }

    public UserExchangeVoucher findByVoucherCode(String voucherCode) {
        return findOneByHql("FROM UserExchangeVoucher WHERE voucherCode = ?1", voucherCode);
    }

    @SuppressWarnings("unchecked")
    public UserExchangeVoucher findUnusedByUserIdAndProductId(Long userId, Long productId) {
        List<UserExchangeVoucher> list = getCurrentSession()
                .createQuery("SELECT v FROM UserExchangeVoucher v JOIN v.pointsMall m " +
                        "WHERE v.user.id = :userId AND m.product.id = :productId " +
                        "AND v.status = 0 AND v.expireTime > :now " +
                        "ORDER BY v.createdAt ASC")
                .setParameter("userId", userId)
                .setParameter("productId", productId)
                .setParameter("now", LocalDateTime.now())
                .setMaxResults(1)
                .list();
        return list.isEmpty() ? null : list.get(0);
    }
}
