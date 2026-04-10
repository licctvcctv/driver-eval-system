package com.drivereval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("driver_info")
public class DriverInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal score;

    private Integer level;

    private Integer totalOrders;

    private Integer totalComplaints;

    private Integer weekComplaints;

    private Integer onlineStatus;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private LocalDateTime punishEndTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
