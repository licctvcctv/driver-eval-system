package com.drivereval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.exception.BusinessException;
import com.drivereval.entity.VehicleInfo;
import com.drivereval.entity.VehicleType;
import com.drivereval.mapper.VehicleInfoMapper;
import com.drivereval.mapper.VehicleTypeMapper;
import com.drivereval.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleInfoMapper vehicleInfoMapper;

    @Autowired
    private VehicleTypeMapper vehicleTypeMapper;

    @Override
    public VehicleInfo getDriverVehicle(Long driverId) {
        return vehicleInfoMapper.selectOne(
                new LambdaQueryWrapper<VehicleInfo>()
                        .eq(VehicleInfo::getDriverId, driverId)
                        .last("LIMIT 1"));
    }

    @Override
    public void addVehicle(VehicleInfo vehicleInfo) {
        vehicleInfoMapper.insert(vehicleInfo);
    }

    @Override
    public void updateVehicle(VehicleInfo vehicleInfo) {
        if (vehicleInfo.getId() == null) {
            throw new BusinessException("车辆ID不能为空");
        }
        vehicleInfoMapper.updateById(vehicleInfo);
    }

    @Override
    public void deleteVehicle(Long id) {
        vehicleInfoMapper.deleteById(id);
    }

    @Override
    public IPage<VehicleInfo> pageList(Page<VehicleInfo> page) {
        LambdaQueryWrapper<VehicleInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(VehicleInfo::getCreateTime);
        return vehicleInfoMapper.selectPage(page, wrapper);
    }

    @Override
    public List<VehicleType> allVehicleTypes() {
        return vehicleTypeMapper.selectList(
                new LambdaQueryWrapper<VehicleType>()
                        .orderByAsc(VehicleType::getId));
    }

    @Override
    public void addVehicleType(VehicleType vehicleType) {
        vehicleTypeMapper.insert(vehicleType);
    }

    @Override
    public void updateVehicleType(VehicleType vehicleType) {
        if (vehicleType.getId() == null) {
            throw new BusinessException("车型ID不能为空");
        }
        vehicleTypeMapper.updateById(vehicleType);
    }

    @Override
    public void deleteVehicleType(Long id) {
        vehicleTypeMapper.deleteById(id);
    }
}
