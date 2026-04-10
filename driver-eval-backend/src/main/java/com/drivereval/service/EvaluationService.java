package com.drivereval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.drivereval.entity.Evaluation;

import java.util.List;
import java.util.Map;

public interface EvaluationService extends IService<Evaluation> {

    void submitEvaluation(Long orderId, Long passengerId, Integer starRating,
                          String content, Integer isAnonymous, List<Long> tagIds);

    void driverReply(Long evaluationId, Long driverId, String reply);

    IPage<Evaluation> getDriverEvaluations(Long driverId, Page<Evaluation> page);

    IPage<Evaluation> getPassengerEvaluations(Long passengerId, Page<Evaluation> page);

    IPage<Evaluation> getAllEvaluations(Page<Evaluation> page);

    List<Map<String, Object>> getDriverTagStats(Long driverId);

    List<Map<String, Object>> getDriverStarStats(Long driverId);

    List<Map<String, Object>> getAllDriverTagStats();
}
