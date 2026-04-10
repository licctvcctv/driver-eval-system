package com.drivereval.controller.driver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.drivereval.common.Result;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.SysUser;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/driver/profile")
public class DriverProfileController extends BaseController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @GetMapping("/info")
    public Result<?> getProfile(HttpServletRequest request) {
        Long userId = getUserId(request);

        SysUser user = sysUserMapper.selectById(userId);
        DriverInfo driverInfo = driverInfoMapper.selectOne(
                new QueryWrapper<DriverInfo>().eq("user_id", userId));

        Map<String, Object> data = new HashMap<>();
        if (user != null) {
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("realName", user.getRealName());
            data.put("phone", user.getPhone());
            data.put("avatar", user.getAvatar());
            data.put("role", user.getRole());
            data.put("status", user.getStatus());
        }
        if (driverInfo != null) {
            data.put("driverInfoId", driverInfo.getId());
            data.put("score", driverInfo.getScore());
            data.put("level", driverInfo.getLevel());
            data.put("totalOrders", driverInfo.getTotalOrders());
            data.put("totalComplaints", driverInfo.getTotalComplaints());
            data.put("weekComplaints", driverInfo.getWeekComplaints());
            data.put("onlineStatus", driverInfo.getOnlineStatus());
            data.put("latitude", driverInfo.getLatitude());
            data.put("longitude", driverInfo.getLongitude());
            data.put("punishEndTime", driverInfo.getPunishEndTime());
        }

        return Result.success(data);
    }

    @PostMapping("/online")
    public Result<?> goOnline(HttpServletRequest request) {
        Long userId = getUserId(request);

        DriverInfo driverInfo = driverInfoMapper.selectOne(
                new QueryWrapper<DriverInfo>().eq("user_id", userId));
        if (driverInfo == null) {
            return Result.error("司机信息不存在");
        }

        // 设置随机北京附近经纬度（用于演示）
        Random random = new Random();
        double lat = 39.9 + (random.nextDouble() - 0.5) * 0.2;
        double lng = 116.4 + (random.nextDouble() - 0.5) * 0.2;

        driverInfo.setOnlineStatus(1);
        driverInfo.setLatitude(new BigDecimal(String.format("%.6f", lat)));
        driverInfo.setLongitude(new BigDecimal(String.format("%.6f", lng)));
        driverInfoMapper.updateById(driverInfo);

        return Result.success("上线成功");
    }

    @PostMapping("/offline")
    public Result<?> goOffline(HttpServletRequest request) {
        Long userId = getUserId(request);

        DriverInfo driverInfo = driverInfoMapper.selectOne(
                new QueryWrapper<DriverInfo>().eq("user_id", userId));
        if (driverInfo == null) {
            return Result.error("司机信息不存在");
        }

        driverInfo.setOnlineStatus(0);
        driverInfoMapper.updateById(driverInfo);

        return Result.success("下线成功");
    }

    @PostMapping("/update")
    public Result<?> updateProfile(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = getUserId(request);

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        String realName = params.get("realName");
        String phone = params.get("phone");

        if (realName != null) {
            user.setRealName(realName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        sysUserMapper.updateById(user);

        return Result.success("更新成功");
    }
}
