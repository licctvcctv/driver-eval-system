package com.drivereval.controller.driver;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.Appeal;
import com.drivereval.service.AppealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/appeal")
public class DriverAppealController extends BaseController {

    @Autowired
    private AppealService appealService;

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
        return Result.success(appealService.getDriverAppeals(userId, page));
    }
}
