package com.zcl.service;

import com.zcl.dao.UserDao;
import com.zcl.entity.User;
import com.zcl.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 用户信息服务类
 * 用于获取和更新当前登录用户的信息
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

    /**
     * 更新用户个人信息
     */
    @Transactional
    public User updateProfile(Long userId, String nickName, String avatarUrl,
                              String phone, Integer gender, LocalDate birthday) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (nickName != null) {
            user.setNickName(nickName);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (gender != null) {
            user.setGender(gender);
        }
        if (birthday != null) {
            user.setBirthday(birthday);
        }
        userDao.save(user);
        return user;
    }
}
