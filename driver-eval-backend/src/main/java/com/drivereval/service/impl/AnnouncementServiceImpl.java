package com.drivereval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drivereval.common.exception.BusinessException;
import com.drivereval.entity.Announcement;
import com.drivereval.mapper.AnnouncementMapper;
import com.drivereval.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public IPage<Announcement> pageList(Integer type, Page<Announcement> page) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(type != null, Announcement::getType, type);
        wrapper.orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectPage(page, wrapper);
    }

    @Override
    public void addAnnouncement(Announcement announcement) {
        announcementMapper.insert(announcement);
    }

    @Override
    public void updateAnnouncement(Announcement announcement) {
        if (announcement.getId() == null) {
            throw new BusinessException("公告ID不能为空");
        }
        announcementMapper.updateById(announcement);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    @Override
    public List<Announcement> publishedList(Integer type) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1); // 已发布
        wrapper.eq(type != null, Announcement::getType, type);
        wrapper.orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectList(wrapper);
    }
}
