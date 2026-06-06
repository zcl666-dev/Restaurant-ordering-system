package com.zcl.service;

import com.zcl.dto.PointLogVO;
import com.zcl.dto.PointsDetailVO;
import com.zcl.entity.PointLog;
import com.zcl.entity.User;
import com.zcl.repository.PointLogRepository;
import com.zcl.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PointsService {

    @Autowired
    private PointLogRepository pointLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInfoService userInfoService;

    public PointsDetailVO getPointsDetail() {
        Long userId = userInfoService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<PointLog> logs = pointLogRepository.findByUserOrderByCreatedAtDesc(user);

        // 按月分组
        Map<String, List<PointLogVO>> grouped = new LinkedHashMap<>();
        for (PointLog log : logs) {
            String monthKey = log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy年M月"));
            PointLogVO vo = PointLogVO.builder()
                    .id(log.getId())
                    .type(log.getType())
                    .pointsChange(Math.abs(log.getPointsChange()))
                    .balanceAfter(log.getBalanceAfter())
                    .remark(log.getRemark() != null ? log.getRemark() : "")
                    .orderNo(log.getOrder() != null ? log.getOrder().getOrderNo() : "")
                    .createdAt(log.getCreatedAt())
                    .build();
            grouped.computeIfAbsent(monthKey, k -> new java.util.ArrayList<>()).add(vo);
        }

        return PointsDetailVO.builder()
                .pointsBalance(user.getPointsBalance())
                .records(grouped)
                .build();
    }
}
