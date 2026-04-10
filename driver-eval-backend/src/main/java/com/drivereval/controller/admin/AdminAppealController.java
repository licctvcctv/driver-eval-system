package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.Appeal;
import com.drivereval.mapper.AppealMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/appeal")
public class AdminAppealController extends BaseController {

    @Autowired
    private AppealMapper appealMapper;

    @GetMapping("/list")
    public Result<?> appealList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<Appeal> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Appeal> wrapper = new QueryWrapper<Appeal>()
                .orderByDesc("create_time");

        if (status != null) {
            wrapper.eq("status", status);
        }

        return Result.success(appealMapper.selectPage(page, wrapper));
    }

    @PostMapping("/review")
    public Result<?> reviewAppeal(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long adminId = getUserId(request);

        Long appealId = Long.valueOf(params.get("appealId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;

        Appeal appeal = appealMapper.selectById(appealId);
        if (appeal == null) {
            return Result.error("申诉不存在");
        }

        appeal.setStatus(status);
        appeal.setAdminRemark(remark);
        appeal.setReviewTime(LocalDateTime.now());
        appeal.setReviewerId(adminId);
        appealMapper.updateById(appeal);

        return Result.success("审核完成");
    }
}
