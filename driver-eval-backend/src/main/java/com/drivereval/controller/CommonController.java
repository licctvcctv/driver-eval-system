package com.drivereval.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.drivereval.common.Result;
import com.drivereval.common.util.SensitiveWordUtil;
import com.drivereval.entity.Announcement;
import com.drivereval.entity.SensitiveWord;
import com.drivereval.entity.VehicleType;
import com.drivereval.mapper.AnnouncementMapper;
import com.drivereval.mapper.SensitiveWordMapper;
import com.drivereval.mapper.VehicleTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/common")
public class CommonController extends BaseController {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private VehicleTypeMapper vehicleTypeMapper;

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String newFilename = UUID.randomUUID().toString().replace("-", "") + suffix;
        String uploadDir = System.getProperty("user.dir") + "/uploads/";

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(uploadDir + newFilename));
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }

        String url = "/uploads/" + newFilename;
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        data.put("filename", newFilename);

        return Result.success(data);
    }

    @GetMapping("/announcements")
    public Result<?> announcements(
            @RequestParam(required = false) Integer type,
            HttpServletRequest request) {

        QueryWrapper<Announcement> wrapper = new QueryWrapper<Announcement>()
                .eq("status", 1) // 已发布
                .orderByDesc("create_time");

        if (type != null) {
            wrapper.eq("type", type);
        }

        return Result.success(announcementMapper.selectList(wrapper));
    }

    @GetMapping("/vehicle-types")
    public Result<?> vehicleTypes(HttpServletRequest request) {
        return Result.success(vehicleTypeMapper.selectList(
                new QueryWrapper<VehicleType>().orderByAsc("id")));
    }

    @PostMapping("/check-sensitive")
    public Result<?> checkSensitive(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String text = params.get("text");
        if (text == null || text.isEmpty()) {
            return Result.success(false);
        }

        // 加载敏感词库
        List<SensitiveWord> words = sensitiveWordMapper.selectList(new QueryWrapper<>());
        List<String> wordList = words.stream()
                .map(SensitiveWord::getWord)
                .collect(Collectors.toList());
        SensitiveWordUtil.init(wordList);

        boolean contains = SensitiveWordUtil.contains(text);

        Map<String, Object> data = new HashMap<>();
        data.put("contains", contains);
        data.put("filtered", SensitiveWordUtil.filter(text));

        return Result.success(data);
    }
}
