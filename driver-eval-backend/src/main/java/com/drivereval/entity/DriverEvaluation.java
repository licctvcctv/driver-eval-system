package com.drivereval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("driver_evaluation")
public class DriverEvaluation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long driverId;
    private Long passengerId;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
