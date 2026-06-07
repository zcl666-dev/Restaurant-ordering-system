package com.zcl.dto;

import lombok.Data;

@Data
public class DiningTableCreateRequest {
    private String tableNo;
    private String tableName;
    private Integer seatCount;
    private Integer status;
}
