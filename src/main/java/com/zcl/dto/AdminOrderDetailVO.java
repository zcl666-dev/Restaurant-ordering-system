package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderDetailVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String userNickName;
    private String userAvatarUrl;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Integer pointsEarned;
    private String diningType;
    private String tableNumber;
    private Integer orderStatus;
    private Integer paymentStatus;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private String remark;
    private LocalDateTime createdAt;
    private List<OrderItemVO> items;
}
