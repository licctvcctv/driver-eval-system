package com.drivereval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    Page<Map<String, Object>> selectOrderWithDetails(Page<?> page,
        @Param("passengerId") Long passengerId,
        @Param("driverId") Long driverId,
        @Param("status") Integer status,
        @Param("statusList") List<Integer> statusList);

    Map<String, Object> selectOrderDetailById(@Param("orderId") Long orderId);

    List<Map<String, Object>> selectOrderDetailsByIds(@Param("orderIds") List<Long> orderIds);
}
