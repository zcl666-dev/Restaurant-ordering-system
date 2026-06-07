package com.zcl.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiningTableDTO {
    private Long id;
    private String tableNo;
    private String tableName;
    private Integer seatCount;
    private Integer status;
    private String qrCodeUrl;
    private LocalDateTime createTime;
}
