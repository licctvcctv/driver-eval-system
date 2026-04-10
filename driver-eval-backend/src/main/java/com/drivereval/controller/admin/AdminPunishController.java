package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Constants;
import com.drivereval.common.Result;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.DriverPunish;
import com.drivereval.entity.SysUser;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.DriverPunishMapper;
import com.drivereval.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
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

    @Autowired
    private DriverInfoMapper driverInfoMapper;

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

    @PostMapping("/manual")
    public Result<?> manualPunish(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long adminId = getUserId(request);
        Long driverId = Long.valueOf(params.get("driverId").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : "管理员手动处罚";
        Integer days = params.get("days") != null ? Integer.valueOf(params.get("days").toString()) : 3;

        // Check driver exists
        DriverInfo driverInfo = driverInfoMapper.selectOne(
            new LambdaQueryWrapper<DriverInfo>().eq(DriverInfo::getUserId, driverId));
        if (driverInfo == null) return Result.error("司机不存在");
        if (driverInfo.getOnlineStatus() == Constants.PUNISHED) return Result.error("该司机已在处罚中");

        // Create punishment record
        DriverPunish punish = new DriverPunish();
        punish.setDriverId(driverId);
        punish.setPunishReason(reason);
        punish.setPunishDays(days);
        punish.setPunishStart(LocalDateTime.now());
        punish.setPunishEnd(LocalDateTime.now().plusDays(days));
        punish.setStatus(1); // active
        punish.setWeekComplaints(driverInfo.getWeekComplaints() != null ? driverInfo.getWeekComplaints() : 0);
        driverPunishMapper.insert(punish);

        // Update driver status
        driverInfo.setOnlineStatus(Constants.PUNISHED);
        driverInfo.setPunishEndTime(punish.getPunishEnd());
        driverInfoMapper.updateById(driverInfo);

        // Update user status
        SysUser user = new SysUser();
        user.setId(driverId);
        user.setStatus(Constants.PUNISHED);
        sysUserMapper.updateById(user);

        return Result.success("处罚成功");
    }

    @PostMapping("/lift")
    public Result<?> liftPunish(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long punishId = Long.valueOf(params.get("punishId").toString());

        DriverPunish punish = driverPunishMapper.selectById(punishId);
        if (punish == null) return Result.error("处罚记录不存在");
        if (punish.getStatus() != 1) return Result.error("该处罚已过期");

        // Mark punishment as expired
        punish.setStatus(2);
        driverPunishMapper.updateById(punish);

        // Restore driver status
        DriverInfo driverInfo = driverInfoMapper.selectOne(
            new LambdaQueryWrapper<DriverInfo>().eq(DriverInfo::getUserId, punish.getDriverId()));
        if (driverInfo != null) {
            driverInfo.setOnlineStatus(Constants.OFFLINE);
            driverInfo.setPunishEndTime(null);
            driverInfoMapper.updateById(driverInfo);
        }

        // Restore user status
        SysUser user = new SysUser();
        user.setId(punish.getDriverId());
        user.setStatus(1); // active
        sysUserMapper.updateById(user);

        return Result.success("已解除处罚");
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
