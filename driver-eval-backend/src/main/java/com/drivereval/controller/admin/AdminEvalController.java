package com.drivereval.controller.admin;

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

    @GetMapping("/list")
    public Result<?> evaluationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<Evaluation> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Evaluation> wrapper = new QueryWrapper<Evaluation>()
                .orderByDesc("create_time");

        return Result.success(evaluationMapper.selectPage(page, wrapper));
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
                item.put("count", entry.getValue());
                result.add(item);
            }
        }

        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));

        return Result.success(result);
    }
}
