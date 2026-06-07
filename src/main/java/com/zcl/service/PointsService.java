package com.zcl.service;

import com.zcl.dao.PointLogDao;
import com.zcl.dao.UserDao;
import com.zcl.dto.PointLogVO;
import com.zcl.dto.PointsDetailVO;
import com.zcl.entity.PointLog;
import com.zcl.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PointsService {

    @Autowired
    private PointLogDao pointLogDao;

    @Autowired
    private UserDao userDao;

    public PointsDetailVO getPointsDetail(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        List<PointLog> logs = pointLogDao.findByUserId(userId);

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
            grouped.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(vo);
        }

        return PointsDetailVO.builder()
                .pointsBalance(user.getPointsBalance())
                .records(grouped)
                .build();
    }
}
