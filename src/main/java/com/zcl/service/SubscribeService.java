package com.zcl.service;

import com.zcl.dao.SubscribeTemplateDao;
import com.zcl.dao.UserDao;
import com.zcl.dto.SubscribeSaveRequest;
import com.zcl.entity.SubscribeTemplate;
import com.zcl.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class SubscribeService {

    @Autowired
    private SubscribeTemplateDao subscribeTemplateDao;

    @Autowired
    private UserDao userDao;

    public void saveSubscribe(Long userId, SubscribeSaveRequest request) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        SubscribeTemplate existing = subscribeTemplateDao.findByTemplateId(request.getTemplateId());

        if (existing != null) {
            existing.setStatus(request.getStatus());
            existing.setAcceptedAt(LocalDateTime.now());
            subscribeTemplateDao.save(existing);
        } else {
            SubscribeTemplate record = new SubscribeTemplate();
            record.setUser(user);
            record.setTemplateId(request.getTemplateId());
            record.setStatus(request.getStatus());
            record.setAcceptedAt(LocalDateTime.now());
            subscribeTemplateDao.save(record);
        }
    }
}
