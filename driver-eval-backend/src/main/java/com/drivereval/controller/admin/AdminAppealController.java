package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drivereval.common.Result;
import com.drivereval.entity.Complaint;
import com.drivereval.entity.Appeal;
import com.drivereval.entity.DriverInfo;
import com.drivereval.mapper.ComplaintMapper;
import com.drivereval.mapper.DriverInfoMapper;
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
@RequestMapping("/api/admin/appeal")
public class AdminAppealController extends BaseController {

    @Autowired
    private AppealService appealService;

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @GetMapping("/list")
    public Result<?> appealList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
        HttpServletRequest request) {

        Page<Appeal> page = new Page<>(pageNum, pageSize);
        IPage<Appeal> appealPage = appealService.getAllAppeals(status, page);

        // Batch-fetch complaints and order details to avoid N+1
        List<Long> complaintIds = appealPage.getRecords().stream()
                .map(Appeal::getComplaintId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, Complaint> complaintMap = new HashMap<>();
        if (!complaintIds.isEmpty()) {
            complaintMapper.selectBatchIds(complaintIds).forEach(c -> complaintMap.put(c.getId(), c));
        }
        List<Long> orderIds = complaintMap.values().stream()
                .map(Complaint::getOrderId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, Map<String, Object>> orderMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            for (Map<String, Object> od : orderInfoMapper.selectOrderDetailsByIds(orderIds)) {
                Object idObj = od.get("id");
                if (idObj != null) orderMap.put(Long.valueOf(idObj.toString()), od);
            }
        }

        // Batch-fetch driver info
        List<Long> driverIds = appealPage.getRecords().stream()
                .map(Appeal::getDriverId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, DriverInfo> driverInfoMap = new HashMap<>();
        if (!driverIds.isEmpty()) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DriverInfo> diWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            diWrapper.in(DriverInfo::getUserId, driverIds);
            driverInfoMapper.selectList(diWrapper).forEach(di -> driverInfoMap.put(di.getUserId(), di));
        }

        List<Map<String, Object>> records = appealPage.getRecords().stream().map(item -> {
            Complaint complaint = complaintMap.get(item.getComplaintId());
            Map<String, Object> order = complaint != null ? orderMap.get(complaint.getOrderId()) : null;
            DriverInfo driverInfo = driverInfoMap.get(item.getDriverId());

            Map<String, Object> view = new HashMap<>();
            view.put("id", item.getId());
            view.put("complaintId", item.getComplaintId());
            view.put("orderId", complaint != null ? complaint.getOrderId() : null);
            view.put("orderNo", order != null ? order.get("orderNo") : null);
            view.put("driverId", item.getDriverId());
            view.put("driverName", order != null ? order.get("driverName") : null);
            view.put("complaintContent", complaint != null ? complaint.getContent() : null);
            view.put("appealContent", item.getContent());
            view.put("content", item.getContent());
            view.put("images", item.getImages());
            view.put("status", item.getStatus());
            view.put("adminRemark", item.getAdminRemark());
            view.put("reviewTime", item.getReviewTime());
            view.put("createTime", item.getCreateTime());
            // Driver context
            view.put("driverScore", driverInfo != null ? driverInfo.getScore() : null);
            view.put("driverWeekComplaints", driverInfo != null ? driverInfo.getWeekComplaints() : null);
            view.put("driverTotalComplaints", driverInfo != null ? driverInfo.getTotalComplaints() : null);
            view.put("driverOnlineStatus", driverInfo != null ? driverInfo.getOnlineStatus() : null);
            // Order details
            view.put("departure", order != null ? order.get("departure") : null);
            view.put("destination", order != null ? order.get("destination") : null);
            return view;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> result = new Page<>(appealPage.getCurrent(), appealPage.getSize());
        result.setTotal(appealPage.getTotal());
        result.setRecords(records);
        return Result.success(result);
    }

    @PostMapping("/review")
    public Result<?> reviewAppeal(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long adminId = getUserId(request);
        Long appealId = Long.valueOf(params.get("appealId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";

        appealService.reviewAppeal(appealId, adminId, status, remark);
        return Result.success("审核完成");
    }
}
