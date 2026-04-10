package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.Complaint;
import com.drivereval.mapper.ComplaintMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/complaint")
public class AdminComplaintController {

    @Autowired
    private ComplaintMapper complaintMapper;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Integer getRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    @GetMapping("/list")
    public Result<?> complaintList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<Complaint> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Complaint> wrapper = new QueryWrapper<Complaint>()
                .orderByDesc("create_time");

        if (status != null) {
            wrapper.eq("status", status);
        }

        return Result.success(complaintMapper.selectPage(page, wrapper));
    }

    @PostMapping("/review")
    public Result<?> reviewComplaint(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long adminId = getUserId(request);

        Long complaintId = Long.valueOf(params.get("complaintId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;

        Complaint complaint = complaintMapper.selectById(complaintId);
        if (complaint == null) {
            return Result.error("投诉不存在");
        }

        complaint.setStatus(status);
        complaint.setAdminRemark(remark);
        complaint.setReviewTime(LocalDateTime.now());
        complaint.setReviewerId(adminId);
        complaintMapper.updateById(complaint);

        return Result.success("审核完成");
    }
}
