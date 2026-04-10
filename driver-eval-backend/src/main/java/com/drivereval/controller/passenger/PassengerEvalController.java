package com.drivereval.controller.passenger;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drivereval.common.Result;
import com.drivereval.entity.Evaluation;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/passenger/eval")
public class PassengerEvalController extends BaseController {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @PostMapping("/submit")
    public Result<?> submitEvaluation(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Integer starRating = Integer.valueOf(params.get("starRating").toString());
        String content = params.get("content") != null ? params.get("content").toString() : "";
        Integer isAnonymous = params.get("isAnonymous") != null ? Integer.valueOf(params.get("isAnonymous").toString()) : 0;

        @SuppressWarnings("unchecked")
        List<Object> tagIdObjs = (List<Object>) params.get("tagIds");
        List<Long> tagIds = new ArrayList<>();
        if (tagIdObjs != null) {
            for (Object obj : tagIdObjs) {
                tagIds.add(Long.valueOf(obj.toString()));
            }
        }

        evaluationService.submitEvaluation(orderId, userId, starRating, content, isAnonymous, tagIds);
        return Result.success("评价成功");
    }

    @GetMapping("/list")
    public Result<?> getMyEvaluations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);

        Page<Evaluation> page = new Page<>(pageNum, pageSize);
        IPage<Evaluation> evaluationPage = evaluationService.getPassengerEvaluations(userId, page);

        List<Map<String, Object>> records = evaluationPage.getRecords().stream().map(item -> {
            Map<String, Object> order = orderInfoMapper.selectOrderDetailById(item.getOrderId());
            Map<String, Object> view = new HashMap<>();
            view.put("id", item.getId());
            view.put("orderId", item.getOrderId());
            view.put("orderNo", order != null ? order.get("orderNo") : null);
            view.put("passengerId", item.getPassengerId());
            view.put("driverId", item.getDriverId());
            view.put("starRating", item.getStarRating());
            view.put("content", item.getContent());
            view.put("isAnonymous", item.getIsAnonymous());
            view.put("anonymous", item.getIsAnonymous());
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
}
