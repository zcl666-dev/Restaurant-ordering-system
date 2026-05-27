package com.zcl.service;

import com.zcl.dto.SubscribeSaveRequest;
import com.zcl.entity.SubscribeTemplate;
import com.zcl.entity.User;
import com.zcl.repository.SubscribeTemplateRepository;
import com.zcl.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SubscribeService {

    @Autowired
    private SubscribeTemplateRepository subscribeTemplateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInfoService userInfoService;

    @Transactional(rollbackFor = Exception.class)
    public void save(SubscribeSaveRequest request) {
        Long userId = userInfoService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Optional<SubscribeTemplate> existing = subscribeTemplateRepository
                .findByUserAndTemplateId(user, request.getTemplateId());

        if (existing.isPresent()) {
            SubscribeTemplate record = existing.get();
            record.setStatus(request.getStatus());
            record.setAcceptedAt(LocalDateTime.now());
            subscribeTemplateRepository.save(record);
        } else {
            SubscribeTemplate record = new SubscribeTemplate();
            record.setUser(user);
            record.setTemplateId(request.getTemplateId());
            record.setStatus(request.getStatus());
            record.setAcceptedAt(LocalDateTime.now());
            subscribeTemplateRepository.save(record);
        }
    }
}
