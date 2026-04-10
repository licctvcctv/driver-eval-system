package com.drivereval.controller.driver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.EvalTag;
import com.drivereval.entity.Evaluation;
import com.drivereval.entity.EvaluationTagRelation;
import com.drivereval.mapper.EvalTagMapper;
import com.drivereval.mapper.EvaluationMapper;
import com.drivereval.mapper.EvaluationTagRelationMapper;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @GetMapping("/list")
    public Result<?> getPassengerEvaluations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Evaluation> page = new Page<>(pageNum, pageSize);
        IPage<Evaluation> evaluationPage = evaluationService.getDriverEvaluations(userId, page);

        List<Map<String, Object>> records = evaluationPage.getRecords().stream().map(item -> {
            Map<String, Object> order = orderInfoMapper.selectOrderDetailById(item.getOrderId());
            List<EvaluationTagRelation> relations = evaluationTagRelationMapper.selectList(
                    new QueryWrapper<EvaluationTagRelation>().eq("evaluation_id", item.getId()));
            List<Long> tagIds = relations.stream().map(EvaluationTagRelation::getTagId).collect(Collectors.toList());
            List<String> tagNames = new ArrayList<>();
            if (!tagIds.isEmpty()) {
                List<EvalTag> tags = evalTagMapper.selectBatchIds(tagIds);
                Map<Long, String> tagNameMap = tags.stream().collect(Collectors.toMap(EvalTag::getId, EvalTag::getTagName));
                for (Long tagId : tagIds) {
                    tagNames.add(tagNameMap.getOrDefault(tagId, "未知标签"));
                }
            }

            Map<String, Object> view = new HashMap<>();
            view.put("id", item.getId());
            view.put("orderId", item.getOrderId());
            view.put("orderNo", order != null ? order.get("orderNo") : null);
            view.put("passengerId", item.getPassengerId());
            view.put("passengerName", order != null ? order.get("passengerName") : null);
            view.put("driverId", item.getDriverId());
            view.put("starRating", item.getStarRating());
            view.put("content", item.getContent());
            view.put("isAnonymous", item.getIsAnonymous());
            view.put("anonymous", item.getIsAnonymous());
            view.put("tags", tagNames);
            view.put("driverReply", item.getDriverReply());
            view.put("replyTime", item.getReplyTime());
            view.put("createTime", item.getCreateTime());
            return view;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> result = new Page<>(evaluationPage.getCurrent(), evaluationPage.getSize());
        result.setTotal(evaluationPage.getTotal());
        result.setRecords(records);
        return Result.success(result);
    }

    @PostMapping("/reply")
    public Result<?> reply(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        Long evaluationId = Long.valueOf(params.get("evaluationId").toString());
        String content = (String) params.get("content");

        evaluationService.driverReply(evaluationId, userId, content);
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
                EvalTag tag = tags.stream().filter(t -> t.getId().equals(entry.getKey())).findFirst().orElse(null);
                item.put("tagType", tag != null ? tag.getTagType() : null);
                item.put("tagTypeText", tag != null
                        ? (tag.getTagType() != null && tag.getTagType() == 1 ? "好评" : "差评")
                        : null);
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
