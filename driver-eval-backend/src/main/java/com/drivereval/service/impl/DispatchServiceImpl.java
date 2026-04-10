package com.drivereval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drivereval.common.Constants;
import com.drivereval.common.exception.BusinessException;
import com.drivereval.common.util.DistanceUtil;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.OrderInfo;
import com.drivereval.entity.VehicleInfo;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.mapper.VehicleInfoMapper;
import com.drivereval.service.DispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DispatchServiceImpl implements DispatchService {

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private VehicleInfoMapper vehicleInfoMapper;

    private static final double MAX_DISPATCH_DISTANCE = 10.0; // 10km

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dispatch(Long orderId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 查询所有在线司机
        List<DriverInfo> onlineDrivers = driverInfoMapper.selectList(
                new LambdaQueryWrapper<DriverInfo>()
                        .eq(DriverInfo::getOnlineStatus, Constants.ONLINE));

        if (onlineDrivers.isEmpty()) {
            return false;
        }

        // Get all driver IDs that have active orders (status in 0,1,2,3)
        List<Object> busyDriverIds = orderInfoMapper.selectObjs(
                new LambdaQueryWrapper<OrderInfo>()
                        .select(OrderInfo::getDriverId)
                        .in(OrderInfo::getStatus, Constants.ORDER_DISPATCHING, Constants.ORDER_DISPATCHED, Constants.ORDER_ACCEPTED, Constants.ORDER_IN_PROGRESS)
                        .isNotNull(OrderInfo::getDriverId)
                        .groupBy(OrderInfo::getDriverId)
        );
        Set<Long> busySet = busyDriverIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(Collectors.toSet());

        List<DriverInfo> availableDrivers = new ArrayList<>();
        for (DriverInfo driver : onlineDrivers) {
            if (busySet.contains(driver.getUserId())) continue;
            availableDrivers.add(driver);
        }

        if (availableDrivers.isEmpty()) {
            return false;
        }

        // 计算每个司机到出发点的距离
        double depLat = order.getDepartureLat().doubleValue();
        double depLng = order.getDepartureLng().doubleValue();

        List<DriverInfo> nearbyDrivers = new ArrayList<>();
        List<Double> distances = new ArrayList<>();

        for (DriverInfo driver : availableDrivers) {
            if (driver.getLatitude() == null || driver.getLongitude() == null) {
                continue;
            }
            double dist = DistanceUtil.haversine(
                    driver.getLatitude().doubleValue(),
                    driver.getLongitude().doubleValue(),
                    depLat, depLng);
            if (dist <= MAX_DISPATCH_DISTANCE) {
                nearbyDrivers.add(driver);
                distances.add(dist);
            }
        }

        if (nearbyDrivers.isEmpty()) {
            return false;
        }

        // 找出最小和最大距离，用于归一化
        double minDist = distances.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxDist = distances.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        // 计算综合分数，选出最优司机
        double bestScore = -1;
        int bestIndex = -1;

        for (int i = 0; i < nearbyDrivers.size(); i++) {
            DriverInfo driver = nearbyDrivers.get(i);
            double dist = distances.get(i);

            // 归一化距离得分：距离越近分越高
            double distanceScore;
            if (maxDist == minDist) {
                distanceScore = 100.0;
            } else {
                distanceScore = (1.0 - (dist - minDist) / (maxDist - minDist)) * 100.0;
            }

            // 综合分数 = 距离分 * 0.6 + 司机评分 * 0.4
            double driverScore = driver.getScore() != null ? driver.getScore().doubleValue() : 80.0;
            double compositeScore = distanceScore * 0.6 + driverScore * 0.4;

            if (compositeScore > bestScore) {
                bestScore = compositeScore;
                bestIndex = i;
            }
        }

        DriverInfo selectedDriver = nearbyDrivers.get(bestIndex);

        // 查询司机车辆
        VehicleInfo vehicle = vehicleInfoMapper.selectOne(
                new LambdaQueryWrapper<VehicleInfo>()
                        .eq(VehicleInfo::getDriverId, selectedDriver.getUserId())
                        .last("LIMIT 1"));

        // 更新订单信息
        order.setDriverId(selectedDriver.getUserId());
        order.setVehicleId(vehicle != null ? vehicle.getId() : null);
        order.setStatus(Constants.ORDER_DISPATCHED);
        order.setDriverScore(selectedDriver.getScore());
        order.setDriverLevel(selectedDriver.getLevel());
        order.setDispatchScore(BigDecimal.valueOf(bestScore).setScale(2, RoundingMode.HALF_UP));
        orderInfoMapper.updateById(order);

        return true;
    }
}
