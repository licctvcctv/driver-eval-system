package com.drivereval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drivereval.entity.Evaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {

    Double avgStarByDriverId(@Param("driverId") Long driverId);
}
