package com.zcl.dao;

import com.zcl.entity.Product;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductDao extends BaseDao<Product, Long> {

    @SuppressWarnings("unchecked")
    public List<Product> findByProductNameContaining(String keyword, int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM Product WHERE productName LIKE :keyword ORDER BY createdAt DESC")
                .setParameter("keyword", "%" + keyword + "%")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<Product> findByCategoryId(Long categoryId, int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM Product WHERE category.id = :categoryId ORDER BY createdAt DESC")
                .setParameter("categoryId", categoryId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<Product> findByStatus(Integer status, int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM Product WHERE status = :status ORDER BY createdAt DESC")
                .setParameter("status", status)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<Product> findByCategoryIdAndStatus(Long categoryId, Integer status, int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM Product WHERE category.id = :categoryId AND status = :status ORDER BY createdAt DESC")
                .setParameter("categoryId", categoryId)
                .setParameter("status", status)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<Product> findAllWithPaging(int offset, int limit) {
        return getCurrentSession()
                .createQuery("FROM Product ORDER BY createdAt DESC")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    public long countByCategoryId(Long categoryId) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM Product WHERE category.id = :categoryId")
                .setParameter("categoryId", categoryId)
                .uniqueResult();
    }

    public long countByStatus(Integer status) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM Product WHERE status = :status")
                .setParameter("status", status)
                .uniqueResult();
    }

    public long countByProductNameContaining(String keyword) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM Product WHERE productName LIKE :keyword")
                .setParameter("keyword", "%" + keyword + "%")
                .uniqueResult();
    }

    public long countByCategoryIdAndStatus(Long categoryId, Integer status) {
        return (long) getCurrentSession()
                .createQuery("SELECT COUNT(*) FROM Product WHERE category.id = :categoryId AND status = :status")
                .setParameter("categoryId", categoryId)
                .setParameter("status", status)
                .uniqueResult();
    }
}
