package com.zcl.dao;

import com.zcl.entity.Admin;
import org.springframework.stereotype.Repository;

@Repository
public class AdminDao extends BaseDao<Admin, Long> {

    public Admin findByUsername(String username) {
        return findOneByHql("FROM Admin WHERE username = ?1", username);
    }
}
