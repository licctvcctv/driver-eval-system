package com.drivereval.controller.admin;

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
@RequestMapping("/api/admin/appeal")
public class AdminAppealController extends BaseController {

    @Autowired
    private AppealService appealService;

    @GetMapping("/list")
    public Result<?> appealList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<Appeal> page = new Page<>(pageNum, pageSize);
        return Result.success(appealService.getAllAppeals(status, page));
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
