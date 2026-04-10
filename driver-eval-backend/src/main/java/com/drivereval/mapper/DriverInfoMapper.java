package com.drivereval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drivereval.entity.DriverInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DriverInfoMapper extends BaseMapper<DriverInfo> {

    void resetAllWeekComplaints();
}
