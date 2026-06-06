package com.zcl.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class PointsDetailVO {
    private Integer pointsBalance;  // 当前积分
    private Map<String, List<PointLogVO>> records; // 按月分组的积分记录
}
