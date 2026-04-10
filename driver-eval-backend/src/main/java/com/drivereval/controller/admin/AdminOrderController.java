package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.mapper.OrderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController extends BaseController {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @GetMapping("/list")
    public Result<?> orderList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String statusList,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Integer statusValue = parseStatus(status);
        List<Integer> statusValues = parseStatusList(statusList);
        if (statusValue == null && statusValues.isEmpty()) {
            statusValues = parseStatusList(status);
        } else if (statusValue != null) {
            statusValues.clear();
        }

        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        Page<Map<String, Object>> result = orderInfoMapper.selectOrderWithDetails(
                page,
                null,
                null,
                statusValue,
                statusValues.isEmpty() ? null : statusValues
        );
        for (Map<String, Object> row : result.getRecords()) {
            Integer code = parseStatusValue(row.get("status"));
            row.put("statusCode", code);
            row.put("status", orderStatusText(code));
        }
        return Result.success(result);
    }

    private Integer parseStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private List<Integer> parseStatusList(String value) {
        List<Integer> values = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return values;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) {
                continue;
            }
            try {
                values.add(Integer.valueOf(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return values;
    }

    private Integer parseStatusValue(Object status) {
        if (status == null) {
            return null;
        }
        if (status instanceof Number) {
            return ((Number) status).intValue();
        }
        try {
            return Integer.valueOf(status.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String orderStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待派单";
            case 1:
                return "已派单";
            case 2:
                return "已接单";
            case 3:
                return "进行中";
            case 4:
                return "已完成";
            case 5:
                return "乘客取消";
            case 6:
                return "司机取消";
            default:
                return "未知";
        }
    }
}
