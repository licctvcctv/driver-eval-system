package com.drivereval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.drivereval.entity.Announcement;

import java.util.List;

public interface AnnouncementService extends IService<Announcement> {

    IPage<Announcement> pageList(Integer type, Page<Announcement> page);

    void addAnnouncement(Announcement announcement);

    void updateAnnouncement(Announcement announcement);

    void deleteAnnouncement(Long id);

    List<Announcement> publishedList(Integer type);
}
