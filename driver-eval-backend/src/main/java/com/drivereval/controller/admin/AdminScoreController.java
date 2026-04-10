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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/score")
public class AdminScoreController {

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @Autowired
    private DriverScoreLogMapper driverScoreLogMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Integer getRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

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

        return Result.success(driverInfoMapper.selectPage(page, wrapper));
    }

    @GetMapping("/detail/{driverId}")
    public Result<?> driverScoreDetail(@PathVariable Long driverId, HttpServletRequest request) {
        DriverInfo driverInfo = driverInfoMapper.selectOne(
                new QueryWrapper<DriverInfo>().eq("user_id", driverId));

        SysUser user = sysUserMapper.selectById(driverId);

        List<DriverScoreLog> scoreLogs = driverScoreLogMapper.selectList(
                new QueryWrapper<DriverScoreLog>()
                        .eq("driver_id", driverId)
                        .orderByDesc("create_time"));

        Map<String, Object> data = new HashMap<>();
        data.put("driverInfo", driverInfo);
        data.put("user", user);
        data.put("scoreLogs", scoreLogs);

        return Result.success(data);
    }
}
