package com.zcl.repository;

import com.zcl.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品数据访问层
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 查询所有上架的商品
     */
    List<Product> findByStatus(Integer status);
}
