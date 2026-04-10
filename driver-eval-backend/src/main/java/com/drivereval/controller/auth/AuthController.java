package com.drivereval.controller.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.drivereval.common.Result;
import com.drivereval.common.util.JwtUtil;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.SysUser;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }

        SysUser user = sysUserMapper.selectOne(
                new QueryWrapper<SysUser>().eq("username", username));

        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole(), user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("avatar", user.getAvatar());
        data.put("user", userInfo);

        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String realName = params.get("realName");
        String phone = params.get("phone");
        String roleStr = params.get("role");
        String idCardImg = params.get("idCardImg");

        if (username == null || password == null || roleStr == null) {
            return Result.error("用户名、密码和角色不能为空");
        }

        int role = Integer.parseInt(roleStr);

        // 校验用户名唯一性
        SysUser existing = sysUserMapper.selectOne(
                new QueryWrapper<SysUser>().eq("username", username));
        if (existing != null) {
            return Result.error("用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName != null ? realName : "");
        user.setPhone(phone != null ? phone : "");
        user.setRole(role);
        user.setStatus(1);
        if (idCardImg != null) {
            user.setIdCardImg(idCardImg);
        }
        sysUserMapper.insert(user);

        // 如果是司机角色，创建司机扩展信息
        if (role == 2) {
            DriverInfo driverInfo = new DriverInfo();
            driverInfo.setUserId(user.getId());
            driverInfo.setScore(new BigDecimal("80.00"));
            driverInfo.setLevel(1);
            driverInfo.setTotalOrders(0);
            driverInfo.setTotalComplaints(0);
            driverInfo.setWeekComplaints(0);
            driverInfo.setOnlineStatus(0);
            driverInfoMapper.insert(driverInfo);
        }

        return Result.success("注册成功");
    }
}
