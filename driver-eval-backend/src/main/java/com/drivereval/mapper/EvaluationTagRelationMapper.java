package com.drivereval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drivereval.entity.EvaluationTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EvaluationTagRelationMapper extends BaseMapper<EvaluationTagRelation> {

    int countPositiveByDriver(@Param("driverId") Long driverId);

    int countAllByDriver(@Param("driverId") Long driverId);
}
