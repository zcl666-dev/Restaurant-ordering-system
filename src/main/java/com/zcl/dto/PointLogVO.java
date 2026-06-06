package com.zcl.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PointLogVO {
    private Long id;
    private Integer type;         // 1获得 2扣除
    private Integer pointsChange; // 变动积分（正数）
    private Integer balanceAfter; // 变动后余额
    private String remark;        // 描述（积分赠送/积分兑换）
    private String orderNo;       // 关联订单号
    private LocalDateTime createdAt;
}
