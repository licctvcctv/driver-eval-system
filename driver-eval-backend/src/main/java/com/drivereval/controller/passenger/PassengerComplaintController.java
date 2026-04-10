package com.drivereval.controller.passenger;

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
@RequestMapping("/api/passenger/complaint")
public class PassengerComplaintController extends BaseController {

    @Autowired
    private ComplaintService complaintService;

    @PostMapping("/submit")
    public Result<?> submitComplaint(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String content = (String) params.get("content");
        String images = params.get("images") != null ? params.get("images").toString() : "";
        Integer isAnonymous = params.get("isAnonymous") != null ? Integer.valueOf(params.get("isAnonymous").toString()) : 0;

        complaintService.submitComplaint(orderId, userId, content, images, isAnonymous);
        return Result.success("投诉提交成功");
    }

    @GetMapping("/list")
    public Result<?> getMyComplaints(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Complaint> page = new Page<>(pageNum, pageSize);
        return Result.success(complaintService.getPassengerComplaints(userId, page));
    }
}
