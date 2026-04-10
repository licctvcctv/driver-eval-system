package com.drivereval.controller.passenger;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Constants;
import com.drivereval.common.Result;
import com.drivereval.entity.Complaint;
import com.drivereval.entity.OrderInfo;
import com.drivereval.mapper.ComplaintMapper;
import com.drivereval.mapper.OrderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/passenger/complaint")
public class PassengerComplaintController extends BaseController {

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @PostMapping("/submit")
    public Result<?> submitComplaint(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);

        Long orderId = Long.valueOf(params.get("orderId").toString());
        String content = (String) params.get("content");
        String images = params.get("images") != null ? params.get("images").toString() : null;
        Integer isAnonymous = params.get("isAnonymous") != null ? Integer.valueOf(params.get("isAnonymous").toString()) : 0;

        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getPassengerId().equals(userId)) {
            return Result.error("无权投诉此订单");
        }
        if (order.getIsComplained() != null && order.getIsComplained() == 1) {
            return Result.error("该订单已投诉");
        }

        Complaint complaint = new Complaint();
        complaint.setOrderId(orderId);
        complaint.setPassengerId(userId);
        complaint.setDriverId(order.getDriverId());
        complaint.setContent(content);
        complaint.setImages(images);
        complaint.setIsAnonymous(isAnonymous);
        complaint.setStatus(Constants.STATUS_PENDING); // 待审核
        complaintMapper.insert(complaint);

        // 更新订单为已投诉
        order.setIsComplained(1);
        orderInfoMapper.updateById(order);

        return Result.success("投诉提交成功");
    }

    @GetMapping("/list")
    public Result<?> getMyComplaints(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Complaint> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Complaint> wrapper = new QueryWrapper<Complaint>()
                .eq("passenger_id", userId)
                .orderByDesc("create_time");

        return Result.success(complaintMapper.selectPage(page, wrapper));
    }
}
