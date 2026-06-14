package com.zcl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 桌台实体类
 */
@Entity
@Table(name = "dining_table")
@Data
@NoArgsConstructor
public class DiningTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_no", nullable = false, length = 20)
    private String tableNo;

    @Column(name = "table_name", nullable = false, length = 50)
    private String tableName;

    @Column(name = "seat_count")
    private Integer seatCount = 4;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 0停用 1启用

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "qr_code_url", columnDefinition = "MEDIUMTEXT")
    private String qrCodeUrl;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }
}
