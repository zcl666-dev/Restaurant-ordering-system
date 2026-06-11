package com.zcl.dao;

import com.zcl.entity.Orders;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OrderDao extends BaseDao<Orders, Long> {

    @SuppressWarnings("unchecked")
    public List<Orders> findAllWithPaging(int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM Orders ORDER BY createdAt DESC")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<Orders> findByStatusWithPaging(Integer status, int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM Orders WHERE orderStatus = :status ORDER BY createdAt DESC")
                .setParameter("status", status)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<Orders> findByDateRangeWithPaging(LocalDateTime startDate, LocalDateTime endDate,
                                                   int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM Orders WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<Orders> findByStatusAndDateRangeWithPaging(Integer status, LocalDateTime startDate,
                                                            LocalDateTime endDate, int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM Orders WHERE orderStatus = :status AND createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
                .setParameter("status", status)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    public long countByStatus(Integer status) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM Orders WHERE orderStatus = :status")
                .setParameter("status", status)
                .uniqueResult();
    }

    public long countBetween(LocalDateTime start, LocalDateTime end) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM Orders WHERE createdAt BETWEEN :start AND :end")
                .setParameter("start", start)
                .setParameter("end", end)
                .uniqueResult();
    }

    public BigDecimal sumTotalRevenue() {
        Object result = getCurrentSession()
                .createQuery("SELECT COALESCE(SUM(payAmount), 0) FROM Orders WHERE paymentStatus = 1")
                .uniqueResult();
        if (result instanceof BigDecimal) {
            return (BigDecimal) result;
        } else if (result instanceof Double) {
            return BigDecimal.valueOf((Double) result);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal sumRevenueBetween(LocalDateTime start, LocalDateTime end) {
        Object result = getCurrentSession()
                .createQuery("SELECT COALESCE(SUM(payAmount), 0) FROM Orders WHERE paymentStatus = 1 AND createdAt BETWEEN :start AND :end")
                .setParameter("start", start)
                .setParameter("end", end)
                .uniqueResult();
        if (result instanceof BigDecimal) {
            return (BigDecimal) result;
        } else if (result instanceof Double) {
            return BigDecimal.valueOf((Double) result);
        }
        return BigDecimal.ZERO;
    }

    @SuppressWarnings("unchecked")
    public List<Orders> findPaidOrdersBetween(LocalDateTime start, LocalDateTime end) {
        return getCurrentSession()
                .createQuery("FROM Orders WHERE paymentStatus = 1 AND createdAt BETWEEN :start AND :end ORDER BY createdAt")
                .setParameter("start", start)
                .setParameter("end", end)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> countGroupByStatus() {
        return getCurrentSession()
                .createQuery("SELECT orderStatus, COUNT(*) FROM Orders GROUP BY orderStatus")
                .list();
    }

    public Orders findByOrderNo(String orderNo) {
        return findOneByHql("FROM Orders WHERE orderNo = ?1", orderNo);
    }

    public long countByOrderStatus(int status) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM Orders WHERE orderStatus = :status")
                .setParameter("status", status)
                .uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<Orders> findByUserId(Long userId) {
        return getCurrentSession()
                .createQuery("FROM Orders WHERE user.id = :userId ORDER BY createdAt DESC")
                .setParameter("userId", userId)
                .list();
    }

    /**
     * 查询 id > lastId 的已支付新订单（用于轮询新订单提醒）
     */
    @SuppressWarnings("unchecked")
    public List<Orders> findPaidOrdersAfterId(Long lastId) {
        return getCurrentSession()
                .createQuery("FROM Orders WHERE id > :lastId AND paymentStatus = 1 ORDER BY id ASC")
                .setParameter("lastId", lastId)
                .list();
    }

    /**
     * 查询已支付订单中最大的 id（用于初始化 lastOrderId）
     */
    public Long findMaxPaidOrderId() {
        Object result = getCurrentSession()
                .createQuery("SELECT COALESCE(MAX(id), 0) FROM Orders WHERE paymentStatus = 1")
                .uniqueResult();
        return result instanceof Long ? (Long) result : ((Number) result).longValue();
    }

    /**
     * 按关键词搜索（订单号或桌号模糊匹配）
     */
    @SuppressWarnings("unchecked")
    public List<Orders> findByKeywordWithPaging(String keyword, int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM Orders WHERE orderNo LIKE :keyword OR tableNumber LIKE :keyword ORDER BY createdAt DESC")
                .setParameter("keyword", "%" + keyword + "%")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    /**
     * 按关键词统计数量
     */
    public long countByKeyword(String keyword) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM Orders WHERE orderNo LIKE :keyword OR tableNumber LIKE :keyword")
                .setParameter("keyword", "%" + keyword + "%")
                .uniqueResult();
    }
}
