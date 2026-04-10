package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.Complaint;
import com.drivereval.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/complaint")
public class AdminComplaintController extends BaseController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/list")
    public Result<?> complaintList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<Complaint> page = new Page<>(pageNum, pageSize);
        return Result.success(complaintService.getAllComplaints(status, page));
    }

    @PostMapping("/review")
    public Result<?> reviewComplaint(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long adminId = getUserId(request);
        Long complaintId = Long.valueOf(params.get("complaintId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";

        complaintService.reviewComplaint(complaintId, adminId, status, remark);
        return Result.success("审核完成");
    }
}
