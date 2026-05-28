package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserVO {
    private Long id;
    private String nickName;
    private String avatarUrl;
    private BigDecimal balance;
    private Integer pointsBalance;
    private BigDecimal totalSpentAmount;
    private Integer totalOrderCount;
    private Integer status;
    private LocalDateTime createdAt;
}
