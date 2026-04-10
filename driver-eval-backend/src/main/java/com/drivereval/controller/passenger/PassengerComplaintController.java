package com.drivereval.controller.passenger;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drivereval.common.Result;
import com.drivereval.entity.Complaint;
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
@RequestMapping("/api/passenger/complaint")
public class PassengerComplaintController extends BaseController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

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
        IPage<Complaint> complaintPage = complaintService.getPassengerComplaints(userId, page);

        List<Map<String, Object>> records = complaintPage.getRecords().stream().map(item -> {
            Map<String, Object> order = orderInfoMapper.selectOrderDetailById(item.getOrderId());
            Map<String, Object> view = new HashMap<>();
            view.put("id", item.getId());
            view.put("orderId", item.getOrderId());
            view.put("orderNo", order != null ? order.get("orderNo") : null);
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
}
