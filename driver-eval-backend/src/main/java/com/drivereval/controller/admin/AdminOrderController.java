package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.OrderInfo;
import com.drivereval.mapper.OrderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Integer getRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    @GetMapping("/list")
    public Result<?> orderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<OrderInfo> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<OrderInfo>()
                .orderByDesc("create_time");

        if (status != null) {
            wrapper.eq("status", status);
        }

        return Result.success(orderInfoMapper.selectPage(page, wrapper));
    }
}
