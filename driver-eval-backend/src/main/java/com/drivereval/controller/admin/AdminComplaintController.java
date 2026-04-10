package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drivereval.common.Result;
import com.drivereval.entity.Complaint;
import com.drivereval.entity.DriverInfo;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/complaint")
public class AdminComplaintController extends BaseController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @GetMapping("/list")
    public Result<?> complaintList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
        HttpServletRequest request) {

        Page<Complaint> page = new Page<>(pageNum, pageSize);
        IPage<Complaint> complaintPage = complaintService.getAllComplaints(status, page);

        // Batch-fetch order details to avoid N+1
        List<Long> orderIds = complaintPage.getRecords().stream()
                .map(Complaint::getOrderId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, Map<String, Object>> orderMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<Map<String, Object>> orderDetails = orderInfoMapper.selectOrderDetailsByIds(orderIds);
            for (Map<String, Object> od : orderDetails) {
                Object idObj = od.get("id");
                if (idObj != null) orderMap.put(Long.valueOf(idObj.toString()), od);
            }
        }

        // Batch-fetch driver info to avoid N+1
        List<Long> driverIds = complaintPage.getRecords().stream()
                .map(Complaint::getDriverId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, DriverInfo> driverInfoMap = new HashMap<>();
        if (!driverIds.isEmpty()) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DriverInfo> diWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            diWrapper.in(DriverInfo::getUserId, driverIds);
            driverInfoMapper.selectList(diWrapper).forEach(di -> driverInfoMap.put(di.getUserId(), di));
        }

        List<Map<String, Object>> records = complaintPage.getRecords().stream().map(item -> {
            Map<String, Object> order = orderMap.get(item.getOrderId());
            DriverInfo driverInfo = driverInfoMap.get(item.getDriverId());
            Map<String, Object> view = new HashMap<>();
            view.put("id", item.getId());
            view.put("orderId", item.getOrderId());
            view.put("orderNo", order != null ? order.get("orderNo") : null);
            view.put("passengerId", item.getPassengerId());
            view.put("passengerName", order != null ? order.get("passengerName") : null);
            view.put("driverId", item.getDriverId());
            view.put("driverName", order != null ? order.get("driverName") : null);
            view.put("content", item.getContent());
            view.put("images", normalizeImages(item.getImages()));
            view.put("isAnonymous", item.getIsAnonymous());
            view.put("anonymous", item.getIsAnonymous());
            view.put("status", item.getStatus());
            view.put("adminRemark", item.getAdminRemark());
            view.put("reviewTime", item.getReviewTime());
            view.put("createTime", item.getCreateTime());
            view.put("driverScore", driverInfo != null ? driverInfo.getScore() : null);
            view.put("driverWeekComplaints", driverInfo != null ? driverInfo.getWeekComplaints() : null);
            view.put("driverTotalComplaints", driverInfo != null ? driverInfo.getTotalComplaints() : null);
            view.put("driverLevel", driverInfo != null ? driverInfo.getLevel() : null);
            return view;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> result = new Page<>(complaintPage.getCurrent(), complaintPage.getSize());
        result.setTotal(complaintPage.getTotal());
        result.setRecords(records);
        return Result.success(result);
    }

    private List<String> normalizeImages(String images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
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
