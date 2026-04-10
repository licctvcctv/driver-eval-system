package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.SysUser;
import com.drivereval.entity.VehicleInfo;
import com.drivereval.entity.VehicleType;
import com.drivereval.mapper.SysUserMapper;
import com.drivereval.mapper.VehicleInfoMapper;
import com.drivereval.mapper.VehicleTypeMapper;
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
@RequestMapping("/api/admin/vehicle")
public class AdminVehicleController extends BaseController {

    @Autowired
    private VehicleInfoMapper vehicleInfoMapper;

    @Autowired
    private VehicleTypeMapper vehicleTypeMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/list")
    public Result<?> vehicleList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<VehicleInfo> page = new Page<>(pageNum, pageSize);
        QueryWrapper<VehicleInfo> wrapper = new QueryWrapper<VehicleInfo>()
                .orderByDesc("create_time");

        Page<VehicleInfo> pageData = vehicleInfoMapper.selectPage(page, wrapper);
        Set<Long> driverIds = pageData.getRecords().stream()
                .map(VehicleInfo::getDriverId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));
        Set<Long> typeIds = pageData.getRecords().stream()
                .map(VehicleInfo::getVehicleTypeId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));

        Map<Long, SysUser> userMap = new HashMap<>();
        if (!driverIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(driverIds);
            for (SysUser user : users) {
                userMap.put(user.getId(), user);
            }
        }
        Map<Long, VehicleType> typeMap = new HashMap<>();
        if (!typeIds.isEmpty()) {
            List<VehicleType> types = vehicleTypeMapper.selectBatchIds(typeIds);
            for (VehicleType type : types) {
                typeMap.put(type.getId(), type);
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (VehicleInfo vehicle : pageData.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            SysUser user = userMap.get(vehicle.getDriverId());
            VehicleType type = typeMap.get(vehicle.getVehicleTypeId());
            item.put("id", vehicle.getId());
            item.put("driverId", vehicle.getDriverId());
            item.put("driverName", user != null ? (user.getRealName() == null || user.getRealName().isEmpty() ? user.getUsername() : user.getRealName()) : "-");
            item.put("plateNumber", vehicle.getPlateNumber());
            item.put("brand", vehicle.getBrand());
            item.put("model", vehicle.getModel());
            item.put("color", vehicle.getColor());
            item.put("vehicleTypeId", vehicle.getVehicleTypeId());
            item.put("vehicleType", type != null ? type.getTypeName() : "-");
            item.put("seats", vehicle.getSeats());
            item.put("statusCode", vehicle.getStatus());
            item.put("status", vehicle.getStatus() != null && vehicle.getStatus() == 1 ? "正常" : "停用");
            item.put("createTime", vehicle.getCreateTime());
            item.put("updateTime", vehicle.getUpdateTime());
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

    @GetMapping("/types")
    public Result<?> vehicleTypeList(HttpServletRequest request) {
        List<VehicleType> types = vehicleTypeMapper.selectList(new QueryWrapper<VehicleType>().orderByAsc("id"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (VehicleType type : types) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", type.getId());
            item.put("typeName", type.getTypeName());
            item.put("name", type.getTypeName());
            item.put("description", type.getDescription());
            item.put("createTime", type.getCreateTime());
            item.put("updateTime", type.getUpdateTime());
            result.add(item);
        }
        return Result.success(result);
    }

    @PostMapping("/type/save")
    public Result<?> saveVehicleType(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String typeName = params.get("typeName");
        String description = params.get("description");

        VehicleType vehicleType = new VehicleType();
        vehicleType.setTypeName(typeName);
        vehicleType.setDescription(description);
        vehicleTypeMapper.insert(vehicleType);

        return Result.success("保存成功");
    }

    @DeleteMapping("/type/{id}")
    public Result<?> deleteVehicleType(@PathVariable Long id, HttpServletRequest request) {
        vehicleTypeMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
