package com.drivereval.controller.driver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Constants;
import com.drivereval.common.Result;
import com.drivereval.common.util.SensitiveWordUtil;
import com.drivereval.entity.DriverEvaluation;
import com.drivereval.entity.OrderInfo;
import com.drivereval.mapper.DriverEvaluationMapper;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/order")
public class DriverOrderController extends BaseController {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DriverEvaluationMapper driverEvaluationMapper;

    @GetMapping("/dispatch")
    public Result<?> getDispatchedOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return Result.success(orderInfoMapper.selectOrderWithDetails(page, null, userId, null,
                Arrays.asList(Constants.ORDER_DISPATCHED, Constants.ORDER_IN_PROGRESS)));
    }

    @GetMapping("/completed")
    public Result<?> getCompletedOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return Result.success(orderInfoMapper.selectOrderWithDetails(page, null, userId, Constants.ORDER_COMPLETED, null));
    }

    @GetMapping("/cancelled")
    public Result<?> getCancelledOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return Result.success(orderInfoMapper.selectOrderWithDetails(page, null, userId, Constants.ORDER_CANCELLED_DRIVER, null));
    }

    @PostMapping("/accept/{orderId}")
    public Result<?> acceptOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            orderService.acceptOrder(orderId, userId);
            return Result.success("接单成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/complete/{orderId}")
    public Result<?> completeOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = getUserId(request);

        try {
            orderService.completeOrder(orderId, userId);
            return Result.success("订单完成");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/cancel/{orderId}")
    public Result<?> cancelOrder(@PathVariable Long orderId,
                                 @RequestBody Map<String, String> params,
                                 HttpServletRequest request) {
        Long userId = getUserId(request);
        String reason = params.get("reason");

        try {
            orderService.cancelByDriver(orderId, userId, reason);
            return Result.success("取消成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/evaluate-passenger")
    public Result<?> evaluatePassenger(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String content = params.get("content") != null ? params.get("content").toString() : "";

        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!order.getDriverId().equals(userId)) return Result.error("无权评价此订单");
        if (order.getStatus() != Constants.ORDER_COMPLETED) return Result.error("只能评价已完成的订单");

        // 检查是否已评价
        DriverEvaluation existing = driverEvaluationMapper.selectOne(
            new QueryWrapper<DriverEvaluation>().eq("order_id", orderId));
        if (existing != null) return Result.error("已评价过该乘客");

        // 敏感词检查
        if (SensitiveWordUtil.contains(content)) {
            return Result.error("评价内容包含敏感词，请修改后重新提交");
        }

        DriverEvaluation eval = new DriverEvaluation();
        eval.setOrderId(orderId);
        eval.setDriverId(userId);
        eval.setPassengerId(order.getPassengerId());
        eval.setContent(content);
        driverEvaluationMapper.insert(eval);

        return Result.success("评价成功");
    }
}
