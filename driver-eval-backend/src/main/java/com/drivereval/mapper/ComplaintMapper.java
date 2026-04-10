package com.drivereval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drivereval.entity.Complaint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ComplaintMapper extends BaseMapper<Complaint> {

    int countApprovedByDriver(@Param("driverId") Long driverId);
}
