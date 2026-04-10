package com.drivereval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("driver_score_log")
public class DriverScoreLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long driverId;

    private BigDecimal oldScore;

    private BigDecimal newScore;

    private String changeReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
