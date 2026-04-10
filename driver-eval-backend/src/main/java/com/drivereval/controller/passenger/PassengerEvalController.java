package com.drivereval.controller.passenger;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.Evaluation;
import com.drivereval.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passenger/eval")
public class PassengerEvalController extends BaseController {

    @Autowired
    private EvaluationService evaluationService;

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
        return Result.success(evaluationService.getPassengerEvaluations(userId, page));
    }
}
