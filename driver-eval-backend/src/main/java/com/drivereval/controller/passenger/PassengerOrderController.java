package com.drivereval.controller.passenger;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.OrderInfo;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.OrderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/passenger/order")
public class PassengerOrderController {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Integer getRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    @PostMapping("/create")
    public Result<?> createOrder(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);

        String departure = (String) params.get("departure");
        BigDecimal departureLng = new BigDecimal(params.get("departureLng").toString());
        BigDecimal departureLat = new BigDecimal(params.get("departureLat").toString());
        String destination = (String) params.get("destination");
        BigDecimal destLng = new BigDecimal(params.get("destLng").toString());
        BigDecimal destLat = new BigDecimal(params.get("destLat").toString());

        // 查找在线司机（简单匹配：取第一个在线且未被处罚的司机）
        List<DriverInfo> onlineDrivers = driverInfoMapper.selectList(
                new QueryWrapper<DriverInfo>()
                        .eq("online_status", 1)
                        .and(w -> w.isNull("punish_end_time").or().lt("punish_end_time", LocalDateTime.now()))
        );

        OrderInfo order = new OrderInfo();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        order.setPassengerId(userId);
        order.setDeparture(departure);
        order.setDepartureLng(departureLng);
        order.setDepartureLat(departureLat);
        order.setDestination(destination);
        order.setDestLng(destLng);
        order.setDestLat(destLat);
        order.setIsEvaluated(0);
        order.setIsComplained(0);

        if (!onlineDrivers.isEmpty()) {
            // 选择评分最高的司机进行派单
            DriverInfo bestDriver = onlineDrivers.stream()
                    .max((a, b) -> a.getScore().compareTo(b.getScore()))
                    .orElse(onlineDrivers.get(0));

            order.setDriverId(bestDriver.getUserId());
            order.setDriverScore(bestDriver.getScore());
            order.setDriverLevel(bestDriver.getLevel());
            order.setDispatchScore(bestDriver.getScore());
            order.setStatus(1); // 已派单
        } else {
            order.setStatus(0); // 待派单
        }

        // 计算距离和价格
        double dist = com.drivereval.common.util.DistanceUtil.haversine(
                departureLat.doubleValue(), departureLng.doubleValue(),
                destLat.doubleValue(), destLng.doubleValue());
        order.setDistance(new BigDecimal(String.format("%.2f", dist)));
        order.setPrice(new BigDecimal(String.format("%.2f", dist * 3 + 8))); // 基础8元 + 3元/公里

        orderInfoMapper.insert(order);
        return Result.success(order);
    }

    @GetMapping("/list")
    public Result<?> getOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<OrderInfo> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<OrderInfo>()
                .eq("passenger_id", userId)
                .orderByDesc("create_time");

        if (status != null) {
            wrapper.eq("status", status);
        }

        return Result.success(orderInfoMapper.selectPage(page, wrapper));
    }

    @PostMapping("/cancel/{orderId}")
    public Result<?> cancelOrder(@PathVariable Long orderId,
                                 @RequestBody Map<String, String> params,
                                 HttpServletRequest request) {
        Long userId = getUserId(request);
        String reason = params.get("reason");

        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getPassengerId().equals(userId)) {
            return Result.error("无权操作此订单");
        }
        if (order.getStatus() >= 3) {
            return Result.error("订单状态不允许取消");
        }

        order.setStatus(4); // 已取消
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        orderInfoMapper.updateById(order);

        return Result.success("取消成功");
    }
}
