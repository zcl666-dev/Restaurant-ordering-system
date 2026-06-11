package com.zcl.dao;

import com.zcl.entity.PointLog;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PointLogDao extends BaseDao<PointLog, Long> {

    @SuppressWarnings("unchecked")
    public List<PointLog> findByUserId(Long userId) {
        return getCurrentSession()
                .createQuery("FROM PointLog WHERE user.id = :userId ORDER BY createdAt DESC")
                .setParameter("userId", userId)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<PointLog> findByUserIdWithPaging(Long userId, int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM PointLog WHERE user.id = :userId ORDER BY createdAt DESC")
                .setParameter("userId", userId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    public long countByUserId(Long userId) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM PointLog WHERE user.id = :userId")
                .setParameter("userId", userId)
                .uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<PointLog> findAllWithPaging(int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM PointLog ORDER BY createdAt DESC")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<PointLog> findWithPaging(String keyword, int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM PointLog WHERE user.nickName LIKE :kw OR remark LIKE :kw ORDER BY createdAt DESC")
                .setParameter("kw", "%" + keyword + "%")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    public long countByKeyword(String keyword) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM PointLog WHERE user.nickName LIKE :kw OR remark LIKE :kw")
                .setParameter("kw", "%" + keyword + "%")
                .uniqueResult();
    }
}
