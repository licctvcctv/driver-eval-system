package com.drivereval.controller.passenger;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.OrderInfo;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/passenger/order")
public class PassengerOrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @PostMapping("/create")
    public Result<?> createOrder(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        String departure = (String) params.get("departure");
        BigDecimal departureLng = new BigDecimal(params.get("departureLng").toString());
        BigDecimal departureLat = new BigDecimal(params.get("departureLat").toString());
        String destination = (String) params.get("destination");
        BigDecimal destLng = new BigDecimal(params.get("destLng").toString());
        BigDecimal destLat = new BigDecimal(params.get("destLat").toString());

        OrderInfo order = orderService.createOrder(userId, departure, departureLng, departureLat,
                                                    destination, destLng, destLat);
        return Result.success(order);
    }

    @GetMapping("/list")
    public Result<?> getOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return Result.success(orderInfoMapper.selectOrderWithDetails(page, userId, null, status, null));
    }

    @PostMapping("/cancel/{orderId}")
    public Result<?> cancelOrder(@PathVariable Long orderId,
                                 @RequestBody Map<String, String> params,
                                 HttpServletRequest request) {
        Long userId = getUserId(request);
        String reason = params.get("reason");

        try {
            orderService.cancelByPassenger(orderId, userId, reason);
            return Result.success("取消成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
