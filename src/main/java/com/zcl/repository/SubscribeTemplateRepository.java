package com.zcl.repository;

import com.zcl.entity.SubscribeTemplate;
import com.zcl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscribeTemplateRepository extends JpaRepository<SubscribeTemplate, Long> {

    List<SubscribeTemplate> findByUser(User user);

    Optional<SubscribeTemplate> findByUserAndTemplateId(User user, String templateId);

    List<SubscribeTemplate> findByUserAndStatus(User user, Integer status);
}
