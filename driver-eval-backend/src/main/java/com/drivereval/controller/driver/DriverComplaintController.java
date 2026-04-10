package com.drivereval.controller.driver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.Complaint;
import com.drivereval.mapper.ComplaintMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/driver/complaint")
public class DriverComplaintController {

    @Autowired
    private ComplaintMapper complaintMapper;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Integer getRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    @GetMapping("/list")
    public Result<?> getComplaints(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Complaint> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Complaint> wrapper = new QueryWrapper<Complaint>()
                .eq("driver_id", userId)
                .orderByDesc("create_time");

        return Result.success(complaintMapper.selectPage(page, wrapper));
    }
}
