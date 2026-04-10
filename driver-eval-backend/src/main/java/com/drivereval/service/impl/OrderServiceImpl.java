package com.drivereval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drivereval.common.Constants;
import com.drivereval.common.util.DistanceUtil;
import com.drivereval.common.exception.BusinessException;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.OrderInfo;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.service.DispatchService;
import com.drivereval.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @Autowired
    private DispatchService dispatchService;

    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderInfo createOrder(Long passengerId, String departure, BigDecimal depLng, BigDecimal depLat,
                                 String destination, BigDecimal destLng, BigDecimal destLat) {
        OrderInfo order = new OrderInfo();
        // 生成订单号：ORD + yyyyMMddHHmmss + 4位随机数
        String orderNo = "ORD" + LocalDateTime.now().format(ORDER_NO_FORMAT)
                + String.format("%04d", RANDOM.nextInt(10000));
        order.setOrderNo(orderNo);
        order.setPassengerId(passengerId);
        order.setDeparture(departure);
        order.setDepartureLng(depLng);
        order.setDepartureLat(depLat);
        order.setDestination(destination);
        order.setDestLng(destLng);
        order.setDestLat(destLat);

        // 计算距离(km)和预估价格
        double distKm = DistanceUtil.haversine(
                depLat.doubleValue(), depLng.doubleValue(),
                destLat.doubleValue(), destLng.doubleValue());
        order.setDistance(BigDecimal.valueOf(distKm).setScale(1, java.math.RoundingMode.HALF_UP));
        double price = Constants.PRICE_BASE + distKm * Constants.PRICE_PER_KM;
        order.setPrice(BigDecimal.valueOf(price).setScale(2, java.math.RoundingMode.HALF_UP));

        order.setStatus(Constants.ORDER_DISPATCHING);
        order.setIsEvaluated(0);
        order.setIsComplained(0);
        orderInfoMapper.insert(order);

        // 尝试派单
        boolean dispatched = dispatchService.dispatch(order.getId());
        if (!dispatched) {
            // 派单失败，保持待派单状态
            order.setStatus(Constants.ORDER_DISPATCHING);
        } else {
            // 重新查询订单获取最新状态
            order = orderInfoMapper.selectById(order.getId());
        }

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelByPassenger(Long orderId, Long passengerId, String reason) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getPassengerId().equals(passengerId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() >= Constants.ORDER_COMPLETED) {
            throw new BusinessException("订单状态不允许取消");
        }
        order.setStatus(Constants.ORDER_CANCELLED_PASSENGER);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        orderInfoMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelByDriver(Long orderId, Long driverId, String reason) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getDriverId().equals(driverId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() >= Constants.ORDER_COMPLETED) {
            throw new BusinessException("订单状态不允许取消");
        }
        order.setStatus(Constants.ORDER_CANCELLED_DRIVER);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        orderInfoMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(Long orderId, Long driverId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getDriverId().equals(driverId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != Constants.ORDER_DISPATCHED) {
            throw new BusinessException("订单状态不允许接单");
        }
        // Demo simplification: skip ORDER_ACCEPTED (status=2) and go directly to IN_PROGRESS (status=3).
        // In this demo flow, the driver clicks "accept" which immediately starts the ride.
        // ORDER_ACCEPTED is reserved for a future two-step flow: accept -> start ride.
        order.setStatus(Constants.ORDER_IN_PROGRESS);
        orderInfoMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId, Long driverId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getDriverId().equals(driverId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != Constants.ORDER_IN_PROGRESS) {
            throw new BusinessException("订单状态不允许完成");
        }
        order.setStatus(Constants.ORDER_COMPLETED);
        order.setCompleteTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);

        // 增加司机完成订单数
        DriverInfo driverInfo = driverInfoMapper.selectOne(
                new LambdaQueryWrapper<DriverInfo>()
                        .eq(DriverInfo::getUserId, order.getDriverId()));
        if (driverInfo != null) {
            int totalOrders = driverInfo.getTotalOrders() == null ? 0 : driverInfo.getTotalOrders();
            driverInfo.setTotalOrders(totalOrders + 1);
            driverInfoMapper.updateById(driverInfo);
        }
    }

    @Override
    public IPage<OrderInfo> getPassengerOrders(Long passengerId, Integer status, Page<OrderInfo> page) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getPassengerId, passengerId);
        wrapper.eq(status != null, OrderInfo::getStatus, status);
        wrapper.orderByDesc(OrderInfo::getCreateTime);
        return orderInfoMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<OrderInfo> getDriverOrders(Long driverId, Integer status, Page<OrderInfo> page) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getDriverId, driverId);
        wrapper.eq(status != null, OrderInfo::getStatus, status);
        wrapper.orderByDesc(OrderInfo::getCreateTime);
        return orderInfoMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<OrderInfo> getAllOrders(Integer status, Page<OrderInfo> page) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, OrderInfo::getStatus, status);
        wrapper.orderByDesc(OrderInfo::getCreateTime);
        return orderInfoMapper.selectPage(page, wrapper);
    }
}
