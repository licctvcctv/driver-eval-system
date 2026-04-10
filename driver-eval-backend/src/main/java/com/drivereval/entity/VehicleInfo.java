package com.drivereval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vehicle_info")
public class VehicleInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long driverId;

    private String plateNumber;

    private String brand;

    private String model;

    private String color;

    private Long vehicleTypeId;

    private Integer seats;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
