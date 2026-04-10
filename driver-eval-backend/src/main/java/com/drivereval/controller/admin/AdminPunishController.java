package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.DriverPunish;
import com.drivereval.mapper.DriverPunishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/punish")
public class AdminPunishController {

    @Autowired
    private DriverPunishMapper driverPunishMapper;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Integer getRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    @GetMapping("/list")
    public Result<?> punishList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<DriverPunish> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DriverPunish> wrapper = new QueryWrapper<DriverPunish>()
                .orderByDesc("create_time");

        if (status != null) {
            wrapper.eq("status", status);
        }

        return Result.success(driverPunishMapper.selectPage(page, wrapper));
    }
}
