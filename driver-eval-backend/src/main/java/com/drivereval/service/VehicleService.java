package com.drivereval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.entity.VehicleInfo;
import com.drivereval.entity.VehicleType;

import java.util.List;

public interface VehicleService {

    VehicleInfo getDriverVehicle(Long driverId);

    void addVehicle(VehicleInfo vehicleInfo);

    void updateVehicle(VehicleInfo vehicleInfo);

    void deleteVehicle(Long id);

    IPage<VehicleInfo> pageList(Page<VehicleInfo> page);

    List<VehicleType> allVehicleTypes();

    void addVehicleType(VehicleType vehicleType);

    void updateVehicleType(VehicleType vehicleType);

    void deleteVehicleType(Long id);
}
