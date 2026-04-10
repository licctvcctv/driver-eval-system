package com.drivereval.controller.driver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.Appeal;
import com.drivereval.entity.Complaint;
import com.drivereval.mapper.AppealMapper;
import com.drivereval.mapper.ComplaintMapper;
import com.drivereval.mapper.OrderInfoMapper;
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
@RequestMapping("/api/driver/complaint")
public class DriverComplaintController extends BaseController {

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private AppealMapper appealMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

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

        IPage<Complaint> complaintPage = complaintMapper.selectPage(page, wrapper);
        List<Map<String, Object>> records = complaintPage.getRecords().stream().map(item -> {
            Map<String, Object> order = orderInfoMapper.selectOrderDetailById(item.getOrderId());
            long appealCount = appealMapper.selectCount(
                    new QueryWrapper<Appeal>().eq("complaint_id", item.getId()));

            boolean anonymous = item.getIsAnonymous() != null && item.getIsAnonymous() == 1;

            Map<String, Object> view = new HashMap<>();
            view.put("id", item.getId());
            view.put("orderId", item.getOrderId());
            view.put("orderNo", order != null ? order.get("orderNo") : null);
            view.put("passengerId", anonymous ? null : item.getPassengerId());
            view.put("passengerName", anonymous ? "匿名用户" : (order != null ? order.get("passengerName") : null));
            view.put("driverId", item.getDriverId());
            view.put("driverName", order != null ? order.get("driverName") : null);
            view.put("content", item.getContent());
            view.put("images", normalizeImages(item.getImages()));
            view.put("isAnonymous", item.getIsAnonymous());
            view.put("anonymous", item.getIsAnonymous());
            view.put("status", item.getStatus());
            view.put("adminRemark", item.getAdminRemark());
            view.put("hasAppeal", appealCount > 0);
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
