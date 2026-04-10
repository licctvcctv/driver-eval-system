package com.drivereval.controller.driver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Constants;
import com.drivereval.common.Result;
import com.drivereval.entity.Appeal;
import com.drivereval.entity.Complaint;
import com.drivereval.mapper.AppealMapper;
import com.drivereval.mapper.ComplaintMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/appeal")
public class DriverAppealController extends BaseController {

    @Autowired
    private AppealMapper appealMapper;

    @Autowired
    private ComplaintMapper complaintMapper;

    @PostMapping("/submit")
    public Result<?> submitAppeal(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);

        Long complaintId = Long.valueOf(params.get("complaintId").toString());
        String content = (String) params.get("content");

        Complaint complaint = complaintMapper.selectById(complaintId);
        if (complaint == null) {
            return Result.error("投诉不存在");
        }
        if (!complaint.getDriverId().equals(userId)) {
            return Result.error("无权对此投诉进行申诉");
        }

        // 检查是否已申诉
        Appeal existing = appealMapper.selectOne(
                new QueryWrapper<Appeal>()
                        .eq("complaint_id", complaintId)
                        .eq("driver_id", userId));
        if (existing != null) {
            return Result.error("已对此投诉提交过申诉");
        }

        Appeal appeal = new Appeal();
        appeal.setComplaintId(complaintId);
        appeal.setDriverId(userId);
        appeal.setContent(content);
        appeal.setStatus(Constants.STATUS_PENDING); // 待审核
        appealMapper.insert(appeal);

        return Result.success("申诉提交成功");
    }

    @GetMapping("/list")
    public Result<?> getAppeals(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Appeal> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Appeal> wrapper = new QueryWrapper<Appeal>()
                .eq("driver_id", userId)
                .orderByDesc("create_time");

        return Result.success(appealMapper.selectPage(page, wrapper));
    }
}
