package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.Announcement;
import com.drivereval.mapper.AnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/announcement")
public class AdminAnnouncementController {

    @Autowired
    private AnnouncementMapper announcementMapper;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Integer getRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    @GetMapping("/list")
    public Result<?> announcementList(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<Announcement> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Announcement> wrapper = new QueryWrapper<Announcement>()
                .orderByDesc("create_time");

        if (type != null) {
            wrapper.eq("type", type);
        }

        return Result.success(announcementMapper.selectPage(page, wrapper));
    }

    @PostMapping("/save")
    public Result<?> saveAnnouncement(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        Integer type = params.get("type") != null ? Integer.valueOf(params.get("type").toString()) : null;
        Integer status = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : null;

        if (id != null) {
            // 更新
            Announcement announcement = announcementMapper.selectById(id);
            if (announcement == null) {
                return Result.error("公告不存在");
            }
            if (title != null) announcement.setTitle(title);
            if (content != null) announcement.setContent(content);
            if (type != null) announcement.setType(type);
            if (status != null) announcement.setStatus(status);
            announcementMapper.updateById(announcement);
            return Result.success("更新成功");
        } else {
            // 新增
            Announcement announcement = new Announcement();
            announcement.setTitle(title);
            announcement.setContent(content);
            announcement.setType(type);
            announcement.setStatus(status != null ? status : 1);
            announcementMapper.insert(announcement);
            return Result.success("保存成功");
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Long id, HttpServletRequest request) {
        announcementMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
