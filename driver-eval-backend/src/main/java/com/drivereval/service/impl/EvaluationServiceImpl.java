package com.drivereval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drivereval.common.Constants;
import com.drivereval.common.exception.BusinessException;
import com.drivereval.entity.*;
import com.drivereval.mapper.EvalTagMapper;
import com.drivereval.mapper.EvaluationMapper;
import com.drivereval.mapper.EvaluationTagRelationMapper;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.service.EvaluationService;
import com.drivereval.service.ScoreService;
import com.drivereval.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation>
        implements EvaluationService {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private EvaluationTagRelationMapper evaluationTagRelationMapper;

    @Autowired
    private EvalTagMapper evalTagMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private ScoreService scoreService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitEvaluation(Long orderId, Long passengerId, Integer starRating,
                                 String content, Integer isAnonymous, List<Long> tagIds) {
        // 校验订单
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getPassengerId().equals(passengerId)) {
            throw new BusinessException("无权评价此订单");
        }
        if (order.getStatus() != Constants.ORDER_COMPLETED) {
            throw new BusinessException("只能评价已完成的订单");
        }
        if (order.getIsEvaluated() != null && order.getIsEvaluated() == 1) {
            throw new BusinessException("该订单已评价");
        }

        // 敏感词检测
        if (StringUtils.hasText(content) && sensitiveWordService.checkText(content)) {
            throw new BusinessException("评价内容包含敏感词，请修改后重新提交");
        }

        // 创建评价记录
        Evaluation evaluation = new Evaluation();
        evaluation.setOrderId(orderId);
        evaluation.setPassengerId(passengerId);
        evaluation.setDriverId(order.getDriverId());
        evaluation.setStarRating(starRating);
        evaluation.setContent(content);
        evaluation.setIsAnonymous(isAnonymous);
        evaluationMapper.insert(evaluation);

        // 创建评价标签关联
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                EvaluationTagRelation relation = new EvaluationTagRelation();
                relation.setEvaluationId(evaluation.getId());
                relation.setTagId(tagId);
                relation.setDriverId(order.getDriverId());
                evaluationTagRelationMapper.insert(relation);
            }
        }

        // 更新订单已评价状态
        order.setIsEvaluated(1);
        orderInfoMapper.updateById(order);

        // 重新计算司机评分
        scoreService.recalculateScore(order.getDriverId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void driverReply(Long evaluationId, Long driverId, String reply) {
        Evaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw new BusinessException("评价不存在");
        }
        if (!evaluation.getDriverId().equals(driverId)) {
            throw new BusinessException("无权回复此评价");
        }
        if (StringUtils.hasText(evaluation.getDriverReply())) {
            throw new BusinessException("已回复过此评价");
        }

        // 敏感词检测
        if (StringUtils.hasText(reply) && sensitiveWordService.checkText(reply)) {
            throw new BusinessException("回复内容包含敏感词，请修改后重新提交");
        }

        evaluation.setDriverReply(reply);
        evaluation.setReplyTime(LocalDateTime.now());
        evaluationMapper.updateById(evaluation);
    }

    @Override
    public IPage<Evaluation> getDriverEvaluations(Long driverId, Page<Evaluation> page) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getDriverId, driverId);
        wrapper.orderByDesc(Evaluation::getCreateTime);
        return evaluationMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Evaluation> getPassengerEvaluations(Long passengerId, Page<Evaluation> page) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getPassengerId, passengerId);
        wrapper.orderByDesc(Evaluation::getCreateTime);
        return evaluationMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Evaluation> getAllEvaluations(Page<Evaluation> page) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Evaluation::getCreateTime);
        return evaluationMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Map<String, Object>> getDriverTagStats(Long driverId) {
        // 查询该司机所有的标签关联
        List<EvaluationTagRelation> relations = evaluationTagRelationMapper.selectList(
                new LambdaQueryWrapper<EvaluationTagRelation>()
                        .eq(EvaluationTagRelation::getDriverId, driverId));

        // 按标签分组统计
        Map<Long, Long> tagCountMap = relations.stream()
                .collect(Collectors.groupingBy(EvaluationTagRelation::getTagId, Collectors.counting()));

        // 查询所有标签名称
        List<EvalTag> allTags = evalTagMapper.selectList(null);
        Map<Long, EvalTag> tagMap = allTags.stream()
                .collect(Collectors.toMap(EvalTag::getId, t -> t));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : tagCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            EvalTag tag = tagMap.get(entry.getKey());
            item.put("tagId", entry.getKey());
            item.put("tagName", tag != null ? tag.getTagName() : "未知标签");
            item.put("tagType", tag != null ? tag.getTagType() : null);
            item.put("count", entry.getValue());
            result.add(item);
        }

        // 按数量降序排列
        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        return result;
    }

    @Override
    public List<Map<String, Object>> getDriverStarStats(Long driverId) {
        // 查询该司机所有评价
        List<Evaluation> evaluations = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getDriverId, driverId)
                        .orderByAsc(Evaluation::getCreateTime));

        // 按月分组，计算平均星级
        Map<String, List<Integer>> monthStarsMap = new java.util.LinkedHashMap<>();
        for (Evaluation eval : evaluations) {
            String month = eval.getCreateTime().getYear() + "-"
                    + String.format("%02d", eval.getCreateTime().getMonthValue());
            monthStarsMap.computeIfAbsent(month, k -> new ArrayList<>()).add(eval.getStarRating());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : monthStarsMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", entry.getKey());
            double avg = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0);
            item.put("avgStar", Math.round(avg * 100.0) / 100.0);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllDriverTagStats() {
        // 查询所有标签关联
        List<EvaluationTagRelation> relations = evaluationTagRelationMapper.selectList(null);

        // 按标签分组统计
        Map<Long, Long> tagCountMap = relations.stream()
                .collect(Collectors.groupingBy(EvaluationTagRelation::getTagId, Collectors.counting()));

        // 查询所有标签名称
        List<EvalTag> allTags = evalTagMapper.selectList(null);
        Map<Long, EvalTag> tagMap = allTags.stream()
                .collect(Collectors.toMap(EvalTag::getId, t -> t));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : tagCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            EvalTag tag = tagMap.get(entry.getKey());
            item.put("tagId", entry.getKey());
            item.put("tagName", tag != null ? tag.getTagName() : "未知标签");
            item.put("tagType", tag != null ? tag.getTagType() : null);
            item.put("count", entry.getValue());
            result.add(item);
        }

        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        return result;
    }
}
