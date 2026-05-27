package com.zcl.repository;

import com.zcl.entity.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 规格值数据访问层
 */
@Repository
public interface OptionValueRepository extends JpaRepository<OptionValue, Long> {

    /**
     * 根据规格组ID查询所有启用的规格值，按排序字段升序
     */
    List<OptionValue> findByGroup_IdAndStatusOrderBySortOrderAsc(Long groupId, Integer status);
}
