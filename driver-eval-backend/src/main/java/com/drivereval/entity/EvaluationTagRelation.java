package com.drivereval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_tag_relation")
public class EvaluationTagRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long evaluationId;

    private Long tagId;

    private Long driverId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
