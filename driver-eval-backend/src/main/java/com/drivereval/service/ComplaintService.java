package com.drivereval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.drivereval.entity.Complaint;

public interface ComplaintService extends IService<Complaint> {

    void submitComplaint(Long orderId, Long passengerId, String content, String images, Integer isAnonymous);

    void reviewComplaint(Long complaintId, Long adminId, Integer status, String remark);

    IPage<Complaint> getDriverComplaints(Long driverId, Page<Complaint> page);

    IPage<Complaint> getPassengerComplaints(Long passengerId, Page<Complaint> page);

    IPage<Complaint> getAllComplaints(Integer status, Page<Complaint> page);
}
