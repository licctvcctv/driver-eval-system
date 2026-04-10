package com.drivereval.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.DriverPunish;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.DriverPunishMapper;
import com.drivereval.mapper.SysUserMapper;
import com.drivereval.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投诉处罚定时任务
 */
@Slf4j
@Component
public class PunishScheduleTask {

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @Autowired
    private DriverPunishMapper driverPunishMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 每周一00:05 检查周投诉>=5的司机，自动处罚下线3天
     */
    @Scheduled(cron = "0 5 0 ? * MON")
    public void weeklyComplaintCheck() {
        log.info("=== 开始执行每周投诉检查任务 ===");
        List<DriverInfo> drivers = driverInfoMapper.selectList(
                new LambdaQueryWrapper<DriverInfo>()
                        .ge(DriverInfo::getWeekComplaints, 5)
                        .ne(DriverInfo::getOnlineStatus, 2)
        );

        for (DriverInfo driver : drivers) {
            // 创建处罚记录
            DriverPunish punish = new DriverPunish();
            punish.setDriverId(driver.getUserId());
            punish.setPunishReason("每周投诉次数达到" + driver.getWeekComplaints() + "次，系统自动处罚");
            punish.setPunishDays(3);
            punish.setPunishStart(LocalDateTime.now());
            punish.setPunishEnd(LocalDateTime.now().plusDays(3));
            punish.setStatus(1);
            punish.setWeekComplaints(driver.getWeekComplaints());
            driverPunishMapper.insert(punish);

            // 更新司机状态为处罚中
            driver.setOnlineStatus(2);
            driver.setPunishEndTime(punish.getPunishEnd());
            driverInfoMapper.updateById(driver);

            // 更新用户状态
            SysUser user = new SysUser();
            user.setId(driver.getUserId());
            user.setStatus(2);
            sysUserMapper.updateById(user);

            log.info("司机[userId={}]因周投诉{}次被自动处罚3天", driver.getUserId(), driver.getWeekComplaints());
        }
        log.info("=== 每周投诉检查任务完成，处罚{}名司机 ===", drivers.size());
    }

    /**
     * 每周一00:10 重置所有司机的周投诉计数
     */
    @Scheduled(cron = "0 10 0 ? * MON")
    public void weeklyResetComplaints() {
        log.info("=== 开始重置周投诉计数 ===");
        driverInfoMapper.resetAllWeekComplaints();
        log.info("=== 周投诉计数重置完成 ===");
    }

    /**
     * 每天00:01 检查处罚到期的记录，恢复司机状态
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void dailyPunishExpireCheck() {
        log.info("=== 开始检查处罚到期记录 ===");
        List<DriverPunish> expired = driverPunishMapper.selectList(
                new LambdaQueryWrapper<DriverPunish>()
                        .eq(DriverPunish::getStatus, 1)
                        .le(DriverPunish::getPunishEnd, LocalDateTime.now())
        );

        for (DriverPunish p : expired) {
            // 标记处罚已过期
            p.setStatus(2);
            driverPunishMapper.updateById(p);

            // 恢复司机状态
            DriverInfo driver = driverInfoMapper.selectOne(
                    new LambdaQueryWrapper<DriverInfo>().eq(DriverInfo::getUserId, p.getDriverId())
            );
            if (driver != null) {
                driver.setOnlineStatus(0); // 恢复为离线，需司机手动上线
                driver.setPunishEndTime(null);
                driverInfoMapper.updateById(driver);
            }

            // 恢复用户状态
            SysUser user = new SysUser();
            user.setId(p.getDriverId());
            user.setStatus(1);
            sysUserMapper.updateById(user);

            log.info("司机[userId={}]处罚到期，已恢复正常状态", p.getDriverId());
        }
        log.info("=== 处罚到期检查完成，恢复{}名司机 ===", expired.size());
    }
}
