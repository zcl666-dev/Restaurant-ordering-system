package com.zcl.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public abstract class BaseDao<T, ID extends Serializable> {

    @Autowired
    protected SessionFactory sessionFactory;

    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public BaseDao() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @SuppressWarnings("unchecked")
    public T findById(ID id) {
        return getCurrentSession().get(entityClass, id);
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        return getCurrentSession().createQuery("FROM " + entityClass.getSimpleName()).list();
    }

    public void save(T entity) {
        getCurrentSession().saveOrUpdate(entity);
    }

    public void delete(T entity) {
        getCurrentSession().delete(entity);
    }

    public void deleteById(ID id) {
        T entity = findById(id);
        if (entity != null) {
            delete(entity);
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> findByHql(String hql, Object... params) {
        var query = getCurrentSession().createQuery(hql);
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]); // JPA-style uses 1-based index
        }
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public T findOneByHql(String hql, Object... params) {
        var query = getCurrentSession().createQuery(hql);
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]); // JPA-style uses 1-based index
        }
        return (T) query.uniqueResult();
    }

    public long count() {
        return (long) getCurrentSession().createQuery("SELECT COUNT(*) FROM " + entityClass.getSimpleName())
                .uniqueResult();
    }
}
