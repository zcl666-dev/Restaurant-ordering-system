package com.zcl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_image", length = 500)
    private String productImage;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "sales_count", nullable = false)
    private Integer salesCount = 0;

    @Column(name = "product_type", nullable = false)
    private Integer productType = 0; // 0普通商品 1可兑换商品

    @Column(name = "has_options", nullable = false)
    private Integer hasOptions = 0; // 0无规格 1有规格

    @Column(name = "is_recommend", nullable = false)
    private Integer isRecommend = 0; // 是否推荐

    @Column(name = "is_hot", nullable = false)
    private Integer isHot = 0; // 是否热销

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 0下架 1上架 2售罄

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
