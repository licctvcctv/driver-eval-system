package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.drivereval.common.Result;
import com.drivereval.entity.*;
import com.drivereval.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private DriverPunishMapper driverPunishMapper;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Integer getRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    @GetMapping("/stats")
    public Result<?> getDashboardStats(HttpServletRequest request) {
        Map<String, Object> stats = new HashMap<>();

        // 乘客总数 (role=1)
        long passengerCount = sysUserMapper.selectCount(
                new QueryWrapper<SysUser>().eq("role", 1));
        stats.put("totalPassengers", passengerCount);

        // 司机总数 (role=2)
        long driverCount = sysUserMapper.selectCount(
                new QueryWrapper<SysUser>().eq("role", 2));
        stats.put("totalDrivers", driverCount);

        // 订单总数
        long orderCount = orderInfoMapper.selectCount(
                new QueryWrapper<OrderInfo>());
        stats.put("totalOrders", orderCount);

        // 投诉总数
        long complaintCount = complaintMapper.selectCount(
                new QueryWrapper<Complaint>());
        stats.put("totalComplaints", complaintCount);

        // 生效中的处罚数
        long activePunishCount = driverPunishMapper.selectCount(
                new QueryWrapper<DriverPunish>()
                        .eq("status", 1)
                        .gt("punish_end", LocalDateTime.now()));
        stats.put("activePunishments", activePunishCount);

        // 今日订单数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long todayOrderCount = orderInfoMapper.selectCount(
                new QueryWrapper<OrderInfo>()
                        .between("create_time", todayStart, todayEnd));
        stats.put("todayOrders", todayOrderCount);

        return Result.success(stats);
    }
}
