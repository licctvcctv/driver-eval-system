package com.drivereval.controller.driver;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drivereval.common.Result;
import com.drivereval.entity.Complaint;
import com.drivereval.entity.Appeal;
import com.drivereval.mapper.ComplaintMapper;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.service.AppealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/driver/appeal")
public class DriverAppealController extends BaseController {

    @Autowired
    private AppealService appealService;

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @PostMapping("/submit")
    public Result<?> submitAppeal(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        Long complaintId = Long.valueOf(params.get("complaintId").toString());
        String content = (String) params.get("content");

        appealService.submitAppeal(complaintId, userId, content);
        return Result.success("申诉提交成功");
    }

    @GetMapping("/list")
    public Result<?> getAppeals(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Appeal> page = new Page<>(pageNum, pageSize);
        IPage<Appeal> appealPage = appealService.getDriverAppeals(userId, page);

        List<Map<String, Object>> records = appealPage.getRecords().stream().map(item -> {
            Complaint complaint = complaintMapper.selectById(item.getComplaintId());
            Map<String, Object> order = complaint != null ? orderInfoMapper.selectOrderDetailById(complaint.getOrderId()) : null;

            Map<String, Object> view = new HashMap<>();
            view.put("id", item.getId());
            view.put("complaintId", item.getComplaintId());
            view.put("orderId", complaint != null ? complaint.getOrderId() : null);
            view.put("orderNo", order != null ? order.get("orderNo") : null);
            view.put("complaintContent", complaint != null ? complaint.getContent() : null);
            view.put("driverId", item.getDriverId());
            view.put("content", item.getContent());
            view.put("appealContent", item.getContent());
            view.put("status", item.getStatus());
            view.put("adminRemark", item.getAdminRemark());
            view.put("reviewTime", item.getReviewTime());
            view.put("createTime", item.getCreateTime());
            return view;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> result = new Page<>(appealPage.getCurrent(), appealPage.getSize());
        result.setTotal(appealPage.getTotal());
        result.setRecords(records);
        return Result.success(result);
    }
}
