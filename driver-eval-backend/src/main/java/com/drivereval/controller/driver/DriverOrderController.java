package com.drivereval.controller.driver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.OrderInfo;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.OrderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/order")
public class DriverOrderController {

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

    @GetMapping("/dispatch")
    public Result<?> getDispatchedOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<OrderInfo> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<OrderInfo>()
                .eq("driver_id", userId)
                .eq("status", 1)
                .orderByDesc("create_time");

        return Result.success(orderInfoMapper.selectPage(page, wrapper));
    }

    @GetMapping("/completed")
    public Result<?> getCompletedOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<OrderInfo> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<OrderInfo>()
                .eq("driver_id", userId)
                .eq("status", 3)
                .orderByDesc("create_time");

        return Result.success(orderInfoMapper.selectPage(page, wrapper));
    }

    @GetMapping("/cancelled")
    public Result<?> getCancelledOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<OrderInfo> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<OrderInfo>()
                .eq("driver_id", userId)
                .eq("status", 4)
                .orderByDesc("create_time");

        return Result.success(orderInfoMapper.selectPage(page, wrapper));
    }

    @PostMapping("/complete/{orderId}")
    public Result<?> completeOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = getUserId(request);

        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getDriverId().equals(userId)) {
            return Result.error("无权操作此订单");
        }
        if (order.getStatus() != 1) {
            return Result.error("订单状态不允许完成");
        }

        order.setStatus(3); // 已完成
        order.setCompleteTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);

        // 更新司机总订单数
        DriverInfo driverInfo = driverInfoMapper.selectOne(
                new QueryWrapper<DriverInfo>().eq("user_id", userId));
        if (driverInfo != null) {
            driverInfo.setTotalOrders(driverInfo.getTotalOrders() + 1);
            driverInfoMapper.updateById(driverInfo);
        }

        return Result.success("订单完成");
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
        if (!order.getDriverId().equals(userId)) {
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
