package com.zcl.repository;

import com.zcl.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStatus(Integer status);

    Page<Product> findByStatus(Integer status, Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);

    Page<Product> findByProductNameContaining(String keyword, Pageable pageable);

    long countByStatus(Integer status);

    long countByCategoryId(Long categoryId);
}
