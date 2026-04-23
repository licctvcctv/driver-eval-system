package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Constants;
import com.drivereval.common.Result;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.OrderInfo;
import com.drivereval.entity.SysUser;
import com.drivereval.entity.VehicleInfo;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.mapper.SysUserMapper;
import com.drivereval.mapper.VehicleInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController extends BaseController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private VehicleInfoMapper vehicleInfoMapper;

    @GetMapping("/list")
    public Result<?> pageList(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<SysUser> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysUser> wrapper = new QueryWrapper<SysUser>()
                .orderByDesc("create_time");

        Integer roleCode = parseRole(role);
        if (roleCode != null) {
            wrapper.eq("role", roleCode);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("username", keyword)
                    .or().like("real_name", keyword)
                    .or().like("phone", keyword));
        }

        Page<SysUser> pageData = sysUserMapper.selectPage(page, wrapper);
        java.util.List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (SysUser user : pageData.getRecords()) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", user.getId());
            item.put("username", user.getUsername());
            item.put("realName", user.getRealName());
            item.put("phone", user.getPhone());
            item.put("avatar", user.getAvatar());
            item.put("roleCode", user.getRole());
            item.put("role", roleText(user.getRole()));
            item.put("statusCode", user.getStatus());
            item.put("status", statusText(user.getStatus()));
            item.put("idCardImg", user.getIdCardImg());
            item.put("createTime", user.getCreateTime());
            item.put("updateTime", user.getUpdateTime());
            records.add(item);
        }

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("records", records);
        data.put("total", pageData.getTotal());
        data.put("current", pageData.getCurrent());
        data.put("size", pageData.getSize());
        data.put("pages", pageData.getPages());
        return Result.success(data);
    }

    @PostMapping("/toggle-status")
    public Result<?> toggleStatus(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Object userIdObj = params.get("userId") != null ? params.get("userId") : params.get("id");
        if (userIdObj == null) {
            return Result.error("用户ID不能为空");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        Integer status = parseStatus(params.get("status"));
        if (status == null) {
            return Result.error("状态值不合法");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 处罚中的用户不能通过启用/禁用绕过，需走处罚管理解除
        if (user.getStatus() == Constants.PUNISHED && status == Constants.STATUS_APPROVED) {
            return Result.error("该用户处于处罚中，请通过处罚管理解除处罚");
        }

        user.setStatus(status);
        sysUserMapper.updateById(user);

        // 同步司机的 DriverInfo 在线状态
        if (user.getRole() == Constants.ROLE_DRIVER) {
            DriverInfo driverInfo = driverInfoMapper.selectOne(
                    new QueryWrapper<DriverInfo>().eq("user_id", userId));
            if (driverInfo != null && status == 0) {
                // 禁用时强制下线
                driverInfo.setOnlineStatus(Constants.OFFLINE);
                driverInfoMapper.updateById(driverInfo);
            }
        }

        return Result.success("操作成功");
    }

    @PostMapping("/update")
    public Result<?> updateUser(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Object userIdObj = params.get("id");
        if (userIdObj == null) {
            return Result.error("用户ID不能为空");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        String username = params.get("username") != null ? params.get("username").toString() : null;
        String realName = params.get("realName") != null ? params.get("realName").toString() : null;
        String phone = params.get("phone") != null ? params.get("phone").toString() : null;

        if (username != null && !username.isEmpty()) {
            // 检查用户名唯一性（排除自身）
            SysUser existing = sysUserMapper.selectOne(
                    new QueryWrapper<SysUser>().eq("username", username).ne("id", userId));
            if (existing != null) {
                return Result.error("用户名已存在");
            }
            user.setUsername(username);
        }
        if (realName != null) {
            user.setRealName(realName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }

        sysUserMapper.updateById(user);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 不允许删除管理员
        if (user.getRole() == Constants.ROLE_ADMIN) {
            return Result.error("不能删除管理员账号");
        }
        if (hasLinkedOrders(id, user.getRole())) {
            return Result.error("该用户存在关联订单，不能删除");
        }
        if (user.getRole() == Constants.ROLE_DRIVER && hasDriverLinks(id)) {
            return Result.error("该司机存在关联司机信息或车辆信息，不能删除");
        }
        sysUserMapper.deleteById(id);
        return Result.success("删除成功");
    }

    private boolean hasLinkedOrders(Long userId, Integer role) {
        if (role == null || orderInfoMapper == null) {
            return false;
        }
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        if (role == Constants.ROLE_PASSENGER) {
            wrapper.eq(OrderInfo::getPassengerId, userId);
        } else if (role == Constants.ROLE_DRIVER) {
            wrapper.eq(OrderInfo::getDriverId, userId);
        } else {
            return false;
        }
        Long count = orderInfoMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    private boolean hasDriverLinks(Long userId) {
        DriverInfo driverInfo = driverInfoMapper.selectOne(
                new QueryWrapper<DriverInfo>().eq("user_id", userId));
        if (driverInfo != null) {
            return true;
        }
        Long vehicleCount = vehicleInfoMapper.selectCount(
                new LambdaQueryWrapper<VehicleInfo>().eq(VehicleInfo::getDriverId, userId));
        return vehicleCount != null && vehicleCount > 0;
    }

    private Integer parseRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return null;
        }
        String value = role.trim();
        if ("乘客".equals(value) || "PASSENGER".equalsIgnoreCase(value)) {
            return 1;
        }
        if ("司机".equals(value) || "DRIVER".equalsIgnoreCase(value)) {
            return 2;
        }
        if ("管理员".equals(value) || "ADMIN".equalsIgnoreCase(value)) {
            return 3;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Integer parseStatus(Object status) {
        if (status == null) {
            return null;
        }
        String value = status.toString().trim();
        if (value.isEmpty()) {
            return null;
        }
        if ("正常".equals(value)) {
            return 1;
        }
        if ("禁用".equals(value)) {
            return 0;
        }
        if ("处罚中".equals(value)) {
            return 2;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String roleText(Integer role) {
        if (role == null) {
            return "未知";
        }
        switch (role) {
            case 1:
                return "乘客";
            case 2:
                return "司机";
            case 3:
                return "管理员";
            default:
                return "未知";
        }
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "正常";
            case 0:
                return "禁用";
            case 2:
                return "处罚中";
            default:
                return "未知";
        }
    }
}
