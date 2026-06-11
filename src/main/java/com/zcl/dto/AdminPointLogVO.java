package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理端积分流水 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPointLogVO {
    private Long id;
    private Long userId;
    private String userNickname;
    private Long orderId;
    private String orderNo;
    private Integer type;           // 1获得 2扣除
    private Integer pointsChange;
    private Integer balanceAfter;
    private String remark;
    private LocalDateTime createdAt;
}
