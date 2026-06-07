package com.zcl.service;

import com.zcl.dao.UserDao;
import com.zcl.entity.User;
import com.zcl.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户信息服务类
 * 用于获取当前登录用户的信息
 */
@Service
@Transactional(readOnly = true)
public class UserInfoService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDao userDao;

    /**
     * 根据用户ID获取用户信息
     */
    public User getUserById(Long userId) {
        return userDao.findById(userId);
    }

    /**
     * 根据openId获取用户信息
     */
    public User getUserByOpenId(String openId) {
        return userDao.findByOpenid(openId);
    }
}
