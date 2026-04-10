package com.drivereval.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.drivereval.common.Result;
import com.drivereval.common.util.SensitiveWordUtil;
import com.drivereval.entity.Announcement;
import com.drivereval.entity.SensitiveWord;
import com.drivereval.entity.EvalTag;
import com.drivereval.entity.VehicleType;
import com.drivereval.mapper.AnnouncementMapper;
import com.drivereval.mapper.EvalTagMapper;
import com.drivereval.mapper.SensitiveWordMapper;
import com.drivereval.mapper.VehicleTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
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

    @Autowired
    private EvalTagMapper evalTagMapper;

    @Value("${file.upload-path}")
    private String uploadPath;

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
        // 用绝对路径写文件，避免 Tomcat 临时目录问题
        File dir = new File(uploadPath).getAbsoluteFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            File dest = new File(dir, newFilename);
            // 用流写入，彻底绕过 MultipartFile.transferTo 的相对路径陷阱
            try (java.io.InputStream in = file.getInputStream();
                 java.io.FileOutputStream out = new java.io.FileOutputStream(dest)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
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
        List<VehicleType> types = vehicleTypeMapper.selectList(new QueryWrapper<VehicleType>().orderByAsc("id"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (VehicleType type : types) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", type.getId());
            item.put("typeName", type.getTypeName());
            item.put("name", type.getTypeName());
            item.put("description", type.getDescription());
            item.put("createTime", type.getCreateTime());
            item.put("updateTime", type.getUpdateTime());
            result.add(item);
        }
        return Result.success(result);
    }

    @GetMapping("/tags")
    public Result<?> getTagList() {
        List<EvalTag> tags = evalTagMapper.selectList(
            new QueryWrapper<EvalTag>().eq("is_deleted", 0).orderByAsc("sort_order"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (EvalTag tag : tags) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", tag.getId());
            item.put("name", tag.getTagName());
            item.put("tagName", tag.getTagName());
            item.put("tagType", tag.getTagType());
            item.put("tagTypeLabel", tag.getTagType() != null && tag.getTagType() == 1 ? "好评" : "差评");
            item.put("sortOrder", tag.getSortOrder());
            result.add(item);
        }
        return Result.success(result);
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
