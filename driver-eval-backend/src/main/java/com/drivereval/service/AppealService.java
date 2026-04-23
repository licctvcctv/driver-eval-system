package com.drivereval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.drivereval.entity.Appeal;

public interface AppealService extends IService<Appeal> {

    void submitAppeal(Long complaintId, Long driverId, String content, String images);

    void reviewAppeal(Long appealId, Long adminId, Integer status, String remark);

    IPage<Appeal> getDriverAppeals(Long driverId, Page<Appeal> page);

    IPage<Appeal> getAllAppeals(Integer status, Page<Appeal> page);
}
