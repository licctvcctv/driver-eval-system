package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.DriverScoreLog;
import com.drivereval.entity.SysUser;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.DriverScoreLogMapper;
import com.drivereval.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/score")
public class AdminScoreController extends BaseController {

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @Autowired
    private DriverScoreLogMapper driverScoreLogMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/list")
    public Result<?> driverScoreList(
            @RequestParam(required = false) Integer level,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<DriverInfo> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DriverInfo> wrapper = new QueryWrapper<DriverInfo>()
                .orderByDesc("score");

        if (level != null) {
            wrapper.eq("level", level);
        }

        Page<DriverInfo> pageData = driverInfoMapper.selectPage(page, wrapper);
        Set<Long> userIds = pageData.getRecords().stream()
                .map(DriverInfo::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            for (SysUser user : users) {
                userMap.put(user.getId(), user);
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (DriverInfo info : pageData.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            SysUser user = userMap.get(info.getUserId());
            item.put("id", info.getId());
            item.put("driverId", info.getUserId());
            item.put("driverName", user != null ? (user.getRealName() == null || user.getRealName().isEmpty() ? user.getUsername() : user.getRealName()) : "-");
            item.put("score", info.getScore());
            item.put("level", info.getLevel());
            item.put("totalOrders", info.getTotalOrders());
            item.put("totalComplaints", info.getTotalComplaints());
            item.put("weekComplaints", info.getWeekComplaints());
            item.put("onlineStatusCode", info.getOnlineStatus());
            item.put("onlineStatus", onlineStatusText(info.getOnlineStatus()));
            item.put("createTime", info.getCreateTime());
            item.put("updateTime", info.getUpdateTime());
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

    @GetMapping("/detail/{driverId}")
    public Result<?> driverScoreDetail(@PathVariable Long driverId, HttpServletRequest request) {
        DriverInfo driverInfo = driverInfoMapper.selectOne(new QueryWrapper<DriverInfo>().eq("user_id", driverId));
        Long userId = driverId;
        if (driverInfo == null) {
            driverInfo = driverInfoMapper.selectById(driverId);
            if (driverInfo != null) {
                userId = driverInfo.getUserId();
            }
        }
        SysUser user = userId != null ? sysUserMapper.selectById(userId) : null;

        List<DriverScoreLog> scoreLogs = driverScoreLogMapper.selectList(
                new QueryWrapper<DriverScoreLog>()
                        .eq("driver_id", userId)
                        .orderByDesc("create_time"));

        Map<String, Object> data = new HashMap<>();
        data.put("driverInfo", driverInfo);
        data.put("user", user);
        List<Map<String, Object>> mappedLogs = new ArrayList<>();
        for (DriverScoreLog log : scoreLogs) {
            Map<String, Object> item = new HashMap<>();
            BigDecimal oldScore = log.getOldScore() != null ? log.getOldScore() : BigDecimal.ZERO;
            BigDecimal newScore = log.getNewScore() != null ? log.getNewScore() : BigDecimal.ZERO;
            item.put("id", log.getId());
            item.put("driverId", log.getDriverId());
            item.put("oldScore", log.getOldScore());
            item.put("newScore", log.getNewScore());
            item.put("changeTime", log.getCreateTime());
            item.put("changeType", "系统重算");
            item.put("changeValue", newScore.subtract(oldScore));
            item.put("remark", log.getChangeReason());
            mappedLogs.add(item);
        }
        data.put("scoreLogs", mappedLogs);

        return Result.success(data);
    }

    private String onlineStatusText(Integer onlineStatus) {
        if (onlineStatus == null) {
            return "离线";
        }
        switch (onlineStatus) {
            case 1:
                return "在线";
            case 2:
                return "处罚中";
            default:
                return "离线";
        }
    }
}
