package com.drivereval.controller.driver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.EvalTag;
import com.drivereval.entity.Evaluation;
import com.drivereval.entity.EvaluationTagRelation;
import com.drivereval.mapper.EvalTagMapper;
import com.drivereval.mapper.EvaluationMapper;
import com.drivereval.mapper.EvaluationTagRelationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/driver/eval")
public class DriverEvalController extends BaseController {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private EvaluationTagRelationMapper evaluationTagRelationMapper;

    @Autowired
    private EvalTagMapper evalTagMapper;

    @GetMapping("/list")
    public Result<?> getPassengerEvaluations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Evaluation> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Evaluation> wrapper = new QueryWrapper<Evaluation>()
                .eq("driver_id", userId)
                .orderByDesc("create_time");

        return Result.success(evaluationMapper.selectPage(page, wrapper));
    }

    @PostMapping("/reply")
    public Result<?> reply(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);

        Long evaluationId = Long.valueOf(params.get("evaluationId").toString());
        String content = (String) params.get("content");

        Evaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            return Result.error("评价不存在");
        }
        if (!evaluation.getDriverId().equals(userId)) {
            return Result.error("无权回复此评价");
        }
        if (evaluation.getDriverReply() != null && !evaluation.getDriverReply().isEmpty()) {
            return Result.error("已回复过此评价");
        }

        evaluation.setDriverReply(content);
        evaluation.setReplyTime(LocalDateTime.now());
        evaluationMapper.updateById(evaluation);

        return Result.success("回复成功");
    }

    @GetMapping("/tag-stats")
    public Result<?> getTagStats(HttpServletRequest request) {
        Long userId = getUserId(request);

        // 查询该司机所有标签关联
        List<EvaluationTagRelation> relations = evaluationTagRelationMapper.selectList(
                new QueryWrapper<EvaluationTagRelation>().eq("driver_id", userId));

        // 统计每个标签的数量
        Map<Long, Long> tagCountMap = relations.stream()
                .collect(Collectors.groupingBy(EvaluationTagRelation::getTagId, Collectors.counting()));

        // 查询标签名称
        List<Map<String, Object>> result = new ArrayList<>();
        if (!tagCountMap.isEmpty()) {
            List<EvalTag> tags = evalTagMapper.selectBatchIds(tagCountMap.keySet());
            Map<Long, String> tagNameMap = tags.stream()
                    .collect(Collectors.toMap(EvalTag::getId, EvalTag::getTagName));

            for (Map.Entry<Long, Long> entry : tagCountMap.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("tagId", entry.getKey());
                item.put("tagName", tagNameMap.getOrDefault(entry.getKey(), "未知标签"));
                item.put("count", entry.getValue());
                result.add(item);
            }
        }

        // 按数量降序排列
        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));

        return Result.success(result);
    }

    @GetMapping("/star-stats")
    public Result<?> getStarStats(HttpServletRequest request) {
        Long userId = getUserId(request);

        // 查询该司机所有评价
        List<Evaluation> evaluations = evaluationMapper.selectList(
                new QueryWrapper<Evaluation>().eq("driver_id", userId).orderByAsc("create_time"));

        // 按星级分组统计
        Map<Integer, Long> starCountMap = evaluations.stream()
                .collect(Collectors.groupingBy(Evaluation::getStarRating, Collectors.counting()));

        // 构建折线图数据（按时间排列的星级趋势）
        List<Map<String, Object>> trendData = new ArrayList<>();
        for (Evaluation eval : evaluations) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", eval.getCreateTime());
            item.put("starRating", eval.getStarRating());
            trendData.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("starCount", starCountMap);
        result.put("trend", trendData);

        return Result.success(result);
    }
}
