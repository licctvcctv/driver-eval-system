package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.mapper.OrderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController extends BaseController {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @GetMapping("/list")
    public Result<?> orderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return Result.success(orderInfoMapper.selectOrderWithDetails(page, null, null, status, null));
    }
}
