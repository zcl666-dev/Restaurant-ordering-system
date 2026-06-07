package com.zcl.dao;

import com.zcl.entity.SubscribeTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubscribeTemplateDao extends BaseDao<SubscribeTemplate, Long> {

    public SubscribeTemplate findByTemplateId(String templateId) {
        return findOneByHql("FROM SubscribeTemplate WHERE templateId = ?1", templateId);
    }

    @SuppressWarnings("unchecked")
    public List<SubscribeTemplate> findByUserId(Long userId) {
        return getCurrentSession()
                .createQuery("FROM SubscribeTemplate WHERE user.id = :userId")
                .setParameter("userId", userId)
                .list();
    }
}
