package com.zcl.repository;

import com.zcl.entity.ProductOptionRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品规格关联数据访问层
 */
@Repository
public interface ProductOptionRelationRepository extends JpaRepository<ProductOptionRelation, Long> {

    /**
     * 根据商品ID查询所有关联的规格组，按排序字段升序
     */
    List<ProductOptionRelation> findByProduct_IdAndIsVisibleOrderBySortOrderAsc(Long productId, Integer isVisible);
}
