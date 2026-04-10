package com.drivereval.controller.passenger;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Constants;
import com.drivereval.common.Result;
import com.drivereval.entity.Evaluation;
import com.drivereval.entity.EvaluationTagRelation;
import com.drivereval.entity.OrderInfo;
import com.drivereval.mapper.EvaluationMapper;
import com.drivereval.mapper.EvaluationTagRelationMapper;
import com.drivereval.mapper.OrderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passenger/eval")
public class PassengerEvalController extends BaseController {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private EvaluationTagRelationMapper evaluationTagRelationMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @PostMapping("/submit")
    public Result<?> submitEvaluation(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);

        Long orderId = Long.valueOf(params.get("orderId").toString());
        Integer starRating = Integer.valueOf(params.get("starRating").toString());
        String content = (String) params.get("content");
        Integer isAnonymous = params.get("isAnonymous") != null ? Integer.valueOf(params.get("isAnonymous").toString()) : 0;

        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getPassengerId().equals(userId)) {
            return Result.error("无权评价此订单");
        }
        if (order.getStatus() != Constants.ORDER_COMPLETED) {
            return Result.error("只能评价已完成的订单");
        }
        if (order.getIsEvaluated() != null && order.getIsEvaluated() == 1) {
            return Result.error("该订单已评价");
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setOrderId(orderId);
        evaluation.setPassengerId(userId);
        evaluation.setDriverId(order.getDriverId());
        evaluation.setStarRating(starRating);
        evaluation.setContent(content);
        evaluation.setIsAnonymous(isAnonymous);
        evaluationMapper.insert(evaluation);

        // 保存评价标签关联
        @SuppressWarnings("unchecked")
        List<Object> tagIds = (List<Object>) params.get("tagIds");
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Object tagIdObj : tagIds) {
                Long tagId = Long.valueOf(tagIdObj.toString());
                EvaluationTagRelation relation = new EvaluationTagRelation();
                relation.setEvaluationId(evaluation.getId());
                relation.setTagId(tagId);
                relation.setDriverId(order.getDriverId());
                evaluationTagRelationMapper.insert(relation);
            }
        }

        // 更新订单为已评价
        order.setIsEvaluated(1);
        orderInfoMapper.updateById(order);

        return Result.success("评价成功");
    }

    @GetMapping("/list")
    public Result<?> getMyEvaluations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Evaluation> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Evaluation> wrapper = new QueryWrapper<Evaluation>()
                .eq("passenger_id", userId)
                .orderByDesc("create_time");

        return Result.success(evaluationMapper.selectPage(page, wrapper));
    }
}
