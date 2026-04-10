package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.VehicleInfo;
import com.drivereval.entity.VehicleType;
import com.drivereval.mapper.VehicleInfoMapper;
import com.drivereval.mapper.VehicleTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/vehicle")
public class AdminVehicleController extends BaseController {

    @Autowired
    private VehicleInfoMapper vehicleInfoMapper;

    @Autowired
    private VehicleTypeMapper vehicleTypeMapper;

    @GetMapping("/list")
    public Result<?> vehicleList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<VehicleInfo> page = new Page<>(pageNum, pageSize);
        QueryWrapper<VehicleInfo> wrapper = new QueryWrapper<VehicleInfo>()
                .orderByDesc("create_time");

        return Result.success(vehicleInfoMapper.selectPage(page, wrapper));
    }

    @GetMapping("/types")
    public Result<?> vehicleTypeList(HttpServletRequest request) {
        return Result.success(vehicleTypeMapper.selectList(
                new QueryWrapper<VehicleType>().orderByAsc("id")));
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
