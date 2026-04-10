package com.drivereval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_info")
public class OrderInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long passengerId;

    private Long driverId;

    private Long vehicleId;

    private String departure;

    private BigDecimal departureLng;

    private BigDecimal departureLat;

    private String destination;

    private BigDecimal destLng;

    private BigDecimal destLat;

    private BigDecimal distance;

    private BigDecimal price;

    private BigDecimal dispatchScore;

    private Integer status;

    private BigDecimal driverScore;

    private Integer driverLevel;

    private LocalDateTime completeTime;

    private LocalDateTime cancelTime;

    private String cancelReason;

    private Integer isEvaluated;

    private Integer isComplained;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
