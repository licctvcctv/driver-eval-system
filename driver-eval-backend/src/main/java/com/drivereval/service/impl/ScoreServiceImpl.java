package com.drivereval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drivereval.common.Constants;
import com.drivereval.common.exception.BusinessException;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.DriverScoreLog;
import com.drivereval.entity.OrderInfo;
import com.drivereval.mapper.*;
import com.drivereval.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private EvaluationTagRelationMapper evaluationTagRelationMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private DriverScoreLogMapper driverScoreLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateScore(Long driverId) {
        DriverInfo driverInfo = driverInfoMapper.selectOne(
                new LambdaQueryWrapper<DriverInfo>()
                        .eq(DriverInfo::getUserId, driverId));
        if (driverInfo == null) {
            throw new BusinessException("司机信息不存在");
        }

        // 查询已完成订单数
        long completedOrders = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getDriverId, driverId)
                .eq(OrderInfo::getStatus, Constants.ORDER_COMPLETED));

        // 不满5单，保持默认80分
        if (completedOrders < 5) {
            if (driverInfo.getLevel() == null || driverInfo.getLevel() != Constants.LEVEL_NORMAL) {
                driverInfo.setLevel(Constants.LEVEL_NORMAL);
                driverInfoMapper.updateById(driverInfo);
            }
            return;
        }

        // 1. 平均星级评分 * 0.40（AVG(star_rating) * 20 映射到0-100）
        Double avgStar = evaluationMapper.avgStarByDriverId(driverId);
        double starScore = (avgStar != null ? avgStar : 5.0) * 20.0;

        // 2. 投诉率得分 * 0.25（(1 - 已通过投诉数/总完成订单数) * 100）
        int approvedComplaints = complaintMapper.countApprovedByDriver(driverId);
        double complaintScore = (1.0 - (double) approvedComplaints / completedOrders) * 100.0;

        // 3. 完单率 * 0.20（完成订单 / (完成订单 + 司机取消订单) * 100）
        long cancelledByDriver = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getDriverId, driverId)
                .eq(OrderInfo::getStatus, Constants.ORDER_CANCELLED_DRIVER));
        double completionRate = (double) completedOrders / (completedOrders + cancelledByDriver) * 100.0;

        // 4. 好评标签占比 * 0.15（正面标签数 / 总标签数 * 100）
        int positiveTags = evaluationTagRelationMapper.countPositiveByDriver(driverId);
        int allTags = evaluationTagRelationMapper.countAllByDriver(driverId);
        double tagScore = allTags > 0 ? (double) positiveTags / allTags * 100.0 : 50.0;

        // 加权计算总分
        double totalScore = starScore * 0.40
                + complaintScore * 0.25
                + completionRate * 0.20
                + tagScore * 0.15;

        // 限制在 [0, 100]
        totalScore = Math.max(0, Math.min(100, totalScore));

        BigDecimal newScore = BigDecimal.valueOf(totalScore).setScale(2, RoundingMode.HALF_UP);
        BigDecimal oldScore = driverInfo.getScore();

        // 确定等级
        int level;
        if (totalScore >= 90) {
            level = Constants.LEVEL_GOLD;
        } else if (totalScore >= 75) {
            level = Constants.LEVEL_SILVER;
        } else {
            level = Constants.LEVEL_NORMAL;
        }

        // 更新司机信息
        driverInfo.setScore(newScore);
        driverInfo.setLevel(level);
        driverInfoMapper.updateById(driverInfo);

        // 记录分数变更日志
        DriverScoreLog scoreLog = new DriverScoreLog();
        scoreLog.setDriverId(driverId);
        scoreLog.setOldScore(oldScore);
        scoreLog.setNewScore(newScore);
        scoreLog.setChangeReason("系统动态评分重算：星级=" + String.format("%.2f", starScore)
                + " 投诉=" + String.format("%.2f", complaintScore)
                + " 完单率=" + String.format("%.2f", completionRate)
                + " 标签=" + String.format("%.2f", tagScore));
        driverScoreLogMapper.insert(scoreLog);
    }
}
