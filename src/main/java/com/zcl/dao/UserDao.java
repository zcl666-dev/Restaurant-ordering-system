package com.zcl.dao;

import com.zcl.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao extends BaseDao<User, Long> {

    @SuppressWarnings("unchecked")
    public List<User> findByNickNameContaining(String keyword, int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM User WHERE nickName LIKE :keyword ORDER BY createdAt DESC")
                .setParameter("keyword", "%" + keyword + "%")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<User> findAllWithPaging(int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM User ORDER BY createdAt DESC")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    public long countByNickNameContaining(String keyword) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM User WHERE nickName LIKE :keyword")
                .setParameter("keyword", "%" + keyword + "%")
                .uniqueResult();
    }

    public User findByOpenid(String openid) {
        return findOneByHql("FROM User WHERE openid = ?1", openid);
    }
}
