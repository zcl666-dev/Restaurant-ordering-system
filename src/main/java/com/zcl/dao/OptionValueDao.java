package com.zcl.dao;

import com.zcl.entity.OptionValue;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OptionValueDao extends BaseDao<OptionValue, Long> {

    @SuppressWarnings("unchecked")
    public List<OptionValue> findByGroupId(Long groupId) {
        return getCurrentSession()
                .createQuery("FROM OptionValue WHERE optionGroup.id = :groupId")
                .setParameter("groupId", groupId)
                .list();
    }
}
