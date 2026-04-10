package com.drivereval.controller.admin;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/eval")
public class AdminEvalController extends BaseController {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private EvaluationTagRelationMapper evaluationTagRelationMapper;

    @Autowired
    private EvalTagMapper evalTagMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @GetMapping("/list")
    public Result<?> evaluationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
        HttpServletRequest request) {

        Page<Evaluation> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Evaluation> wrapper = new QueryWrapper<Evaluation>()
                .orderByDesc("create_time");

        IPage<Evaluation> evaluationPage = evaluationMapper.selectPage(page, wrapper);

        // Batch-fetch order details, tag relations and tag names to avoid N+1
        List<Long> orderIds = evaluationPage.getRecords().stream()
                .map(Evaluation::getOrderId).filter(id -> id != null).distinct().collect(Collectors.toList());
        Map<Long, Map<String, Object>> orderMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            for (Map<String, Object> od : orderInfoMapper.selectOrderDetailsByIds(orderIds)) {
                Object idObj = od.get("id");
                if (idObj != null) orderMap.put(Long.valueOf(idObj.toString()), od);
            }
        }

        List<Long> evalIds = evaluationPage.getRecords().stream().map(Evaluation::getId).collect(Collectors.toList());
        Map<Long, List<Long>> evalTagMap = new HashMap<>();
        Set<Long> allTagIds = new HashSet<>();
        if (!evalIds.isEmpty()) {
            List<EvaluationTagRelation> allRelations = evaluationTagRelationMapper.selectList(
                    new QueryWrapper<EvaluationTagRelation>().in("evaluation_id", evalIds));
            for (EvaluationTagRelation r : allRelations) {
                evalTagMap.computeIfAbsent(r.getEvaluationId(), k -> new ArrayList<>()).add(r.getTagId());
                allTagIds.add(r.getTagId());
            }
        }
        Map<Long, String> tagNameMap = new HashMap<>();
        if (!allTagIds.isEmpty()) {
            evalTagMapper.selectBatchIds(allTagIds).forEach(t -> tagNameMap.put(t.getId(), t.getTagName()));
        }

        List<Map<String, Object>> records = evaluationPage.getRecords().stream().map(item -> {
            Map<String, Object> order = orderMap.get(item.getOrderId());
            List<Long> tagIds = evalTagMap.getOrDefault(item.getId(), Collections.emptyList());
            List<String> tagNames = tagIds.stream()
                    .map(tid -> tagNameMap.getOrDefault(tid, "未知标签"))
                    .collect(Collectors.toList());

            Map<String, Object> view = new HashMap<>();
            view.put("id", item.getId());
            view.put("orderId", item.getOrderId());
            view.put("orderNo", order != null ? order.get("orderNo") : null);
            view.put("passengerId", item.getPassengerId());
            view.put("passengerName", order != null ? order.get("passengerName") : null);
            view.put("driverId", item.getDriverId());
            view.put("driverName", order != null ? order.get("driverName") : null);
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

    @GetMapping("/tag-stats")
    public Result<?> allDriverTagStats(HttpServletRequest request) {
        // 查询所有标签关联
        List<EvaluationTagRelation> relations = evaluationTagRelationMapper.selectList(
                new QueryWrapper<EvaluationTagRelation>());

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

        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));

        return Result.success(result);
    }
}
