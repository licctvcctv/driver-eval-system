package com.drivereval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drivereval.common.Constants;
import com.drivereval.common.exception.BusinessException;
import com.drivereval.entity.Appeal;
import com.drivereval.entity.Complaint;
import com.drivereval.entity.DriverInfo;
import com.drivereval.mapper.AppealMapper;
import com.drivereval.mapper.ComplaintMapper;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.service.AppealService;
import com.drivereval.service.ScoreService;
import com.drivereval.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AppealServiceImpl extends ServiceImpl<AppealMapper, Appeal> implements AppealService {

    @Autowired
    private AppealMapper appealMapper;

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private ScoreService scoreService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAppeal(Long complaintId, Long driverId, String content) {
        Complaint complaint = complaintMapper.selectById(complaintId);
        if (complaint == null) {
            throw new BusinessException("投诉不存在");
        }
        if (complaint.getStatus() != Constants.STATUS_APPROVED) {
            throw new BusinessException("只能对已通过的投诉进行申诉");
        }
        if (!complaint.getDriverId().equals(driverId)) {
            throw new BusinessException("无权申诉此投诉");
        }

        // 检查是否已存在申诉
        long existCount = appealMapper.selectCount(
                new LambdaQueryWrapper<Appeal>()
                        .eq(Appeal::getComplaintId, complaintId));
        if (existCount > 0) {
            throw new BusinessException("该投诉已申诉，请勿重复提交");
        }

        // 敏感词检测
        if (StringUtils.hasText(content) && sensitiveWordService.checkText(content)) {
            throw new BusinessException("申诉内容包含敏感词，请修改后重新提交");
        }

        Appeal appeal = new Appeal();
        appeal.setComplaintId(complaintId);
        appeal.setDriverId(driverId);
        appeal.setContent(content);
        appeal.setStatus(Constants.STATUS_PENDING);
        appealMapper.insert(appeal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewAppeal(Long appealId, Long adminId, Integer status, String remark) {
        Appeal appeal = appealMapper.selectById(appealId);
        if (appeal == null) {
            throw new BusinessException("申诉不存在");
        }
        if (appeal.getStatus() != Constants.STATUS_PENDING) {
            throw new BusinessException("该申诉已审核");
        }

        appeal.setStatus(status);
        appeal.setAdminRemark(remark);
        appeal.setReviewTime(LocalDateTime.now());
        appeal.setReviewerId(adminId);
        appealMapper.updateById(appeal);

        // 如果申诉通过，减少司机投诉次数并重新计算评分
        if (status == Constants.STATUS_APPROVED) {
            DriverInfo driverInfo = driverInfoMapper.selectById(appeal.getDriverId());
            if (driverInfo != null) {
                driverInfo.setWeekComplaints(Math.max(0, driverInfo.getWeekComplaints() - 1));
                driverInfo.setTotalComplaints(Math.max(0, driverInfo.getTotalComplaints() - 1));
                driverInfoMapper.updateById(driverInfo);
                scoreService.recalculateScore(appeal.getDriverId());
            }
        }
    }

    @Override
    public IPage<Appeal> getDriverAppeals(Long driverId, Page<Appeal> page) {
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appeal::getDriverId, driverId);
        wrapper.orderByDesc(Appeal::getCreateTime);
        return appealMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Appeal> getAllAppeals(Integer status, Page<Appeal> page) {
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Appeal::getStatus, status);
        wrapper.orderByDesc(Appeal::getCreateTime);
        return appealMapper.selectPage(page, wrapper);
    }
}
