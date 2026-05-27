package com.zcl.repository;

import com.zcl.entity.OptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 规格组数据访问层
 */
@Repository
public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {
}
