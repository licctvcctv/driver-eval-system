package com.drivereval.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.drivereval.common.Constants;
import com.drivereval.entity.OrderInfo;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.service.DispatchService;
import com.drivereval.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时定时任务
 */
@Slf4j
@Component
public class OrderScheduleTask {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private OrderService orderService;

    /**
     * 每30秒检查一次：已派单(ORDER_DISPATCHED=1)超过2分钟未接单的订单，
     * 重置为派单中(ORDER_DISPATCHING=0)并清空司机，尝试重新派单。
     */
    @Scheduled(fixedRate = 30000)
    public void checkDispatchedTimeout() {
        log.debug("=== 开始检查已派单超时订单 ===");
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);

        List<OrderInfo> timeoutOrders = orderInfoMapper.selectList(
                new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getStatus, Constants.ORDER_DISPATCHED)
                        .le(OrderInfo::getUpdateTime, threshold)
        );

        if (timeoutOrders.isEmpty()) {
            log.debug("=== 已派单超时检查完成，无需处理 ===");
            return;
        }

        log.info("=== 开始处理已派单超时订单，共{}个 ===", timeoutOrders.size());
        int redispatchedCount = 0;
        for (OrderInfo order : timeoutOrders) {
            log.info("订单[id={}, orderNo={}]已派单超过2分钟未被接单，重置为派单中状态",
                    order.getId(), order.getOrderNo());

            // 重置订单状态为派单中，清空司机信息
            orderInfoMapper.update(null,
                    new LambdaUpdateWrapper<OrderInfo>()
                            .eq(OrderInfo::getId, order.getId())
                            .eq(OrderInfo::getStatus, Constants.ORDER_DISPATCHED)
                            .set(OrderInfo::getStatus, Constants.ORDER_DISPATCHING)
                            .set(OrderInfo::getDriverId, null)
                            .set(OrderInfo::getVehicleId, null)
                            .set(OrderInfo::getDispatchScore, null)
                            .set(OrderInfo::getDriverScore, null)
                            .set(OrderInfo::getDriverLevel, null)
            );

            // 尝试重新派单
            try {
                boolean dispatched = dispatchService.dispatch(order.getId());
                if (dispatched) {
                    log.info("订单[id={}]重新派单成功", order.getId());
                } else {
                    log.info("订单[id={}]重新派单未匹配到司机，保持派单中状态", order.getId());
                }
            } catch (Exception e) {
                log.warn("订单[id={}]重新派单异常，保持派单中状态: {}", order.getId(), e.getMessage());
            }

            redispatchedCount++;
        }
        log.info("=== 已派单超时检查完成，处理{}个订单 ===", redispatchedCount);
    }

    /**
     * 每30秒检查一次：派单中(ORDER_DISPATCHING=0)超过10分钟的订单，
     * 自动取消，防止订单永久滞留。
     */
    @Scheduled(fixedRate = 30000)
    public void checkDispatchingTimeout() {
        log.debug("=== 开始检查派单中超时订单 ===");
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);

        List<OrderInfo> stuckOrders = orderInfoMapper.selectList(
                new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getStatus, Constants.ORDER_DISPATCHING)
                        .le(OrderInfo::getUpdateTime, threshold)
        );

        if (stuckOrders.isEmpty()) {
            log.debug("=== 派单中超时检查完成，无需处理 ===");
            return;
        }

        log.info("=== 开始处理派单中超时订单，共{}个 ===", stuckOrders.size());
        int cancelledCount = 0;
        for (OrderInfo order : stuckOrders) {
            log.info("订单[id={}, orderNo={}]派单中超过10分钟无司机接单，系统自动取消",
                    order.getId(), order.getOrderNo());

            orderInfoMapper.update(null,
                    new LambdaUpdateWrapper<OrderInfo>()
                            .eq(OrderInfo::getId, order.getId())
                            .eq(OrderInfo::getStatus, Constants.ORDER_DISPATCHING)
                            .set(OrderInfo::getStatus, Constants.ORDER_CANCELLED_PASSENGER)
                            .set(OrderInfo::getCancelTime, LocalDateTime.now())
                            .set(OrderInfo::getCancelReason, "系统自动取消：超过10分钟未匹配到司机")
            );

            cancelledCount++;
        }
        log.info("=== 派单中超时检查完成，取消{}个订单 ===", cancelledCount);
    }

    /**
     * 每30秒检查一次：进行中(ORDER_IN_PROGRESS=3)超过5分钟的订单，
     * 自动完成（Demo模式，模拟短途行程自动结束）。
     */
    @Scheduled(fixedRate = 30000)
    public void autoCompleteOrders() {
        log.debug("=== 开始检查自动完成订单 ===");
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);

        List<OrderInfo> inProgressOrders = orderInfoMapper.selectList(
                new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getStatus, Constants.ORDER_IN_PROGRESS)
                        .le(OrderInfo::getUpdateTime, threshold)
        );

        if (inProgressOrders.isEmpty()) {
            log.debug("=== 自动完成检查完成，无需处理 ===");
            return;
        }

        log.info("=== 开始自动完成订单，共{}个 ===", inProgressOrders.size());
        int completedCount = 0;
        for (OrderInfo order : inProgressOrders) {
            try {
                orderService.completeOrder(order.getId(), order.getDriverId());
                log.info("订单[id={}, orderNo={}]已自动完成", order.getId(), order.getOrderNo());
                completedCount++;
            } catch (Exception e) {
                log.warn("订单[id={}]自动完成失败: {}", order.getId(), e.getMessage());
            }
        }
        log.info("=== 自动完成检查完成，完成{}个订单 ===", completedCount);
    }
}
