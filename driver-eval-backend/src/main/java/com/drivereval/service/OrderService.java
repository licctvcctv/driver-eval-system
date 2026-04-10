package com.drivereval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.drivereval.entity.OrderInfo;

import java.math.BigDecimal;

public interface OrderService extends IService<OrderInfo> {

    OrderInfo createOrder(Long passengerId, String departure, BigDecimal depLng, BigDecimal depLat,
                          String destination, BigDecimal destLng, BigDecimal destLat);

    void cancelByPassenger(Long orderId, Long passengerId, String reason);

    void cancelByDriver(Long orderId, Long driverId, String reason);

    void acceptOrder(Long orderId, Long driverId);

    void completeOrder(Long orderId, Long driverId);

    IPage<OrderInfo> getPassengerOrders(Long passengerId, Integer status, Page<OrderInfo> page);

    IPage<OrderInfo> getDriverOrders(Long driverId, Integer status, Page<OrderInfo> page);

    IPage<OrderInfo> getAllOrders(Integer status, Page<OrderInfo> page);
}
