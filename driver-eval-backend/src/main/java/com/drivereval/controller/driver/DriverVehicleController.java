package com.drivereval.controller.driver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.drivereval.common.Result;
import com.drivereval.entity.VehicleInfo;
import com.drivereval.mapper.VehicleInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/vehicle")
public class DriverVehicleController {

    @Autowired
    private VehicleInfoMapper vehicleInfoMapper;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Integer getRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    @GetMapping("/info")
    public Result<?> getMyVehicle(HttpServletRequest request) {
        Long userId = getUserId(request);

        VehicleInfo vehicle = vehicleInfoMapper.selectOne(
                new QueryWrapper<VehicleInfo>().eq("driver_id", userId));

        return Result.success(vehicle);
    }

    @PostMapping("/save")
    public Result<?> saveOrUpdateVehicle(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);

        String plateNumber = (String) params.get("plateNumber");
        String brand = (String) params.get("brand");
        String model = (String) params.get("model");
        String color = (String) params.get("color");
        Long vehicleTypeId = params.get("vehicleTypeId") != null ? Long.valueOf(params.get("vehicleTypeId").toString()) : null;
        Integer seats = params.get("seats") != null ? Integer.valueOf(params.get("seats").toString()) : null;

        VehicleInfo existing = vehicleInfoMapper.selectOne(
                new QueryWrapper<VehicleInfo>().eq("driver_id", userId));

        if (existing != null) {
            if (plateNumber != null) existing.setPlateNumber(plateNumber);
            if (brand != null) existing.setBrand(brand);
            if (model != null) existing.setModel(model);
            if (color != null) existing.setColor(color);
            if (vehicleTypeId != null) existing.setVehicleTypeId(vehicleTypeId);
            if (seats != null) existing.setSeats(seats);
            vehicleInfoMapper.updateById(existing);
            return Result.success("更新成功");
        } else {
            VehicleInfo vehicle = new VehicleInfo();
            vehicle.setDriverId(userId);
            vehicle.setPlateNumber(plateNumber);
            vehicle.setBrand(brand);
            vehicle.setModel(model);
            vehicle.setColor(color);
            vehicle.setVehicleTypeId(vehicleTypeId);
            vehicle.setSeats(seats);
            vehicle.setStatus(1);
            vehicleInfoMapper.insert(vehicle);
            return Result.success("保存成功");
        }
    }
}
