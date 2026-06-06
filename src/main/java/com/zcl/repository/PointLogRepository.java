package com.zcl.repository;

import com.zcl.entity.PointLog;
import com.zcl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointLogRepository extends JpaRepository<PointLog, Long> {

    List<PointLog> findByUserOrderByCreatedAtDesc(User user);
}
