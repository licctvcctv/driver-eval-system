package com.drivereval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drivereval.common.Constants;
import com.drivereval.common.exception.BusinessException;
import com.drivereval.entity.Complaint;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.OrderInfo;
import com.drivereval.mapper.ComplaintMapper;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.service.ComplaintService;
import com.drivereval.service.ScoreService;
import com.drivereval.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class ComplaintServiceImpl extends ServiceImpl<ComplaintMapper, Complaint>
        implements ComplaintService {

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private ScoreService scoreService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitComplaint(Long orderId, Long passengerId, String content, String images, Integer isAnonymous) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != Constants.ORDER_COMPLETED) {
            throw new BusinessException("只能投诉已完成的订单");
        }
        if (!order.getPassengerId().equals(passengerId)) {
            throw new BusinessException("无权投诉此订单");
        }
        if (order.getIsComplained() != null && order.getIsComplained() == 1) {
            throw new BusinessException("该订单已投诉");
        }

        // 敏感词检测
        if (StringUtils.hasText(content) && sensitiveWordService.checkText(content)) {
            throw new BusinessException("投诉内容包含敏感词，请修改后重新提交");
        }

        Complaint complaint = new Complaint();
        complaint.setOrderId(orderId);
        complaint.setPassengerId(passengerId);
        complaint.setDriverId(order.getDriverId());
        complaint.setContent(content);
        complaint.setImages(images);
        complaint.setIsAnonymous(isAnonymous);
        complaint.setStatus(Constants.STATUS_PENDING);
        complaintMapper.insert(complaint);

        // 更新订单已投诉状态
        order.setIsComplained(1);
        orderInfoMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewComplaint(Long complaintId, Long adminId, Integer status, String remark) {
        Complaint complaint = complaintMapper.selectById(complaintId);
        if (complaint == null) {
            throw new BusinessException("投诉不存在");
        }
        if (complaint.getStatus() != Constants.STATUS_PENDING) {
            throw new BusinessException("该投诉已审核");
        }

        complaint.setStatus(status);
        complaint.setAdminRemark(remark);
        complaint.setReviewTime(LocalDateTime.now());
        complaint.setReviewerId(adminId);
        complaintMapper.updateById(complaint);

        // 如果投诉通过，增加司机投诉次数并重新计算评分
        if (status == Constants.STATUS_APPROVED) {
            DriverInfo driverInfo = driverInfoMapper.selectById(complaint.getDriverId());
            if (driverInfo != null) {
                driverInfo.setWeekComplaints(driverInfo.getWeekComplaints() + 1);
                driverInfo.setTotalComplaints(driverInfo.getTotalComplaints() + 1);
                driverInfoMapper.updateById(driverInfo);
                scoreService.recalculateScore(complaint.getDriverId());
            }
        }
    }

    @Override
    public IPage<Complaint> getDriverComplaints(Long driverId, Page<Complaint> page) {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Complaint::getDriverId, driverId);
        wrapper.orderByDesc(Complaint::getCreateTime);
        return complaintMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Complaint> getPassengerComplaints(Long passengerId, Page<Complaint> page) {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Complaint::getPassengerId, passengerId);
        wrapper.orderByDesc(Complaint::getCreateTime);
        return complaintMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Complaint> getAllComplaints(Integer status, Page<Complaint> page) {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Complaint::getStatus, status);
        wrapper.orderByDesc(Complaint::getCreateTime);
        return complaintMapper.selectPage(page, wrapper);
    }
}
