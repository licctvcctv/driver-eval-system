package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.DriverPunish;
import com.drivereval.entity.SysUser;
import com.drivereval.mapper.DriverPunishMapper;
import com.drivereval.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/punish")
public class AdminPunishController extends BaseController {

    @Autowired
    private DriverPunishMapper driverPunishMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/list")
    public Result<?> punishList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<DriverPunish> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DriverPunish> wrapper = new QueryWrapper<DriverPunish>()
                .orderByDesc("create_time");

        Integer statusCode = parseStatus(status);
        if (statusCode != null) {
            wrapper.eq("status", statusCode);
        }

        Page<DriverPunish> pageData = driverPunishMapper.selectPage(page, wrapper);
        Set<Long> driverIds = pageData.getRecords().stream()
                .map(DriverPunish::getDriverId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!driverIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(driverIds);
            for (SysUser user : users) {
                userMap.put(user.getId(), user);
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (DriverPunish punish : pageData.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            SysUser user = userMap.get(punish.getDriverId());
            item.put("id", punish.getId());
            item.put("driverId", punish.getDriverId());
            item.put("driverName", user != null ? (user.getRealName() == null || user.getRealName().isEmpty() ? user.getUsername() : user.getRealName()) : "-");
            item.put("punishReason", punish.getPunishReason());
            item.put("punishDays", punish.getPunishDays());
            item.put("punishStart", punish.getPunishStart());
            item.put("punishEnd", punish.getPunishEnd());
            item.put("weekComplaints", punish.getWeekComplaints());
            item.put("statusCode", punish.getStatus());
            item.put("status", punish.getStatus() != null && punish.getStatus() == 1 ? "生效中" : "已过期");
            item.put("createTime", punish.getCreateTime());
            item.put("updateTime", punish.getUpdateTime());
            records.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", pageData.getTotal());
        data.put("current", pageData.getCurrent());
        data.put("size", pageData.getSize());
        data.put("pages", pageData.getPages());
        return Result.success(data);
    }

    private Integer parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        String value = status.trim();
        if ("生效中".equals(value)) {
            return 1;
        }
        if ("已过期".equals(value)) {
            return 2;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
