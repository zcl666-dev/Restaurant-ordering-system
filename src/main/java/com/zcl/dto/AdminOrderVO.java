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
public class AdminOrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String userNickName;
    private BigDecimal payAmount;
    private Integer orderStatus;
    private Integer paymentStatus;
    private String diningType;
    private String tableNumber;
    private Integer itemCount;
    private LocalDateTime cancelDeadline; // 可取消截止时间
    private LocalDateTime createdAt;
}
