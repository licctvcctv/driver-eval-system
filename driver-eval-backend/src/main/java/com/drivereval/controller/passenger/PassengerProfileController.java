package com.drivereval.controller.passenger;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.drivereval.common.Result;
import com.drivereval.entity.SysUser;
import com.drivereval.mapper.SysUserMapper;
import com.drivereval.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/passenger/profile")
public class PassengerProfileController extends BaseController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/info")
    public Result<?> getProfile(HttpServletRequest request) {
        Long userId = getUserId(request);
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("phone", user.getPhone());
        data.put("avatar", user.getAvatar());
        data.put("role", user.getRole());
        data.put("createTime", user.getCreateTime());
        return Result.success(data);
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
        String avatar = params.get("avatar");
        String username = params.get("username");

        if (username != null) {
            String value = username.trim();
            if (value.length() < 3 || value.length() > 30) {
                return Result.error("用户名长度必须在3-30个字符之间");
            }
            SysUser existing = sysUserMapper.selectOne(
                    new QueryWrapper<SysUser>().eq("username", value).ne("id", userId));
            if (existing != null) {
                return Result.error("用户名已存在");
            }
            user.setUsername(value);
        }
        if (realName != null) {
            user.setRealName(realName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        sysUserMapper.updateById(user);

        return Result.success("更新成功");
    }
}
