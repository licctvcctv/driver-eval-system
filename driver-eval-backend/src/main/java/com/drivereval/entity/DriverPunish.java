package com.drivereval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("driver_punish")
public class DriverPunish {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long driverId;

    private String punishReason;

    private Integer punishDays;

    private LocalDateTime punishStart;

    private LocalDateTime punishEnd;

    private Integer status;

    private Integer weekComplaints;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
