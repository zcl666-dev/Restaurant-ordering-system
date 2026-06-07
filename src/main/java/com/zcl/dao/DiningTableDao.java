package com.zcl.dao;

import com.zcl.entity.DiningTable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DiningTableDao extends BaseDao<DiningTable, Long> {

    /**
     * 根据桌号查找桌台
     */
    public DiningTable findByTableNo(String tableNo) {
        String hql = "FROM DiningTable WHERE tableNo = ?1";
        return findOneByHql(hql, tableNo);
    }

    /**
     * 分页查询桌台
     */
    @SuppressWarnings("unchecked")
    public List<DiningTable> findByPage(int page, int size, String tableNo, String tableName, Integer status) {
        StringBuilder hql = new StringBuilder("FROM DiningTable WHERE 1=1");
        if (tableNo != null && !tableNo.isEmpty()) {
            hql.append(" AND tableNo LIKE ?1");
        }
        if (tableName != null && !tableName.isEmpty()) {
            hql.append(" AND tableName LIKE ?2");
        }
        if (status != null) {
            hql.append(" AND status = ?3");
        }
        hql.append(" ORDER BY id ASC");

        var query = getCurrentSession().createQuery(hql.toString());
        int paramIndex = 1;
        if (tableNo != null && !tableNo.isEmpty()) {
            query.setParameter(paramIndex++, "%" + tableNo + "%");
        }
        if (tableName != null && !tableName.isEmpty()) {
            query.setParameter(paramIndex++, "%" + tableName + "%");
        }
        if (status != null) {
            query.setParameter(paramIndex++, status);
        }

        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.list();
    }

    /**
     * 统计符合条件的桌台数量
     */
    public long countByCondition(String tableNo, String tableName, Integer status) {
        StringBuilder hql = new StringBuilder("SELECT COUNT(*) FROM DiningTable WHERE 1=1");
        if (tableNo != null && !tableNo.isEmpty()) {
            hql.append(" AND tableNo LIKE ?1");
        }
        if (tableName != null && !tableName.isEmpty()) {
            hql.append(" AND tableName LIKE ?2");
        }
        if (status != null) {
            hql.append(" AND status = ?3");
        }

        var query = getCurrentSession().createQuery(hql.toString());
        int paramIndex = 1;
        if (tableNo != null && !tableNo.isEmpty()) {
            query.setParameter(paramIndex++, "%" + tableNo + "%");
        }
        if (tableName != null && !tableName.isEmpty()) {
            query.setParameter(paramIndex++, "%" + tableName + "%");
        }
        if (status != null) {
            query.setParameter(paramIndex++, status);
        }

        return (long) query.uniqueResult();
    }

    /**
     * 查找所有二维码为空的桌台
     */
    @SuppressWarnings("unchecked")
    public List<DiningTable> findWithoutQrCode() {
        String hql = "FROM DiningTable WHERE qrCodeUrl IS NULL OR qrCodeUrl = ''";
        return getCurrentSession().createQuery(hql).list();
    }
}
