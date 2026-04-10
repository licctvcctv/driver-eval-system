package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.drivereval.common.Result;
import com.drivereval.entity.EvalTag;
import com.drivereval.mapper.EvalTagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tag")
public class AdminTagController extends BaseController {

    @Autowired
    private EvalTagMapper evalTagMapper;

    @GetMapping("/list")
    public Result<?> tagList(HttpServletRequest request) {
        List<EvalTag> tags = evalTagMapper.selectList(new QueryWrapper<EvalTag>().orderByAsc("sort_order"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (EvalTag tag : tags) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", tag.getId());
            item.put("tagName", tag.getTagName());
            item.put("tagTypeCode", tag.getTagType());
            item.put("tagType", tagTypeText(tag.getTagType()));
            item.put("sortOrder", tag.getSortOrder());
            item.put("createTime", tag.getCreateTime());
            item.put("updateTime", tag.getUpdateTime());
            result.add(item);
        }
        return Result.success(result);
    }

    @PostMapping("/save")
    public Result<?> saveTag(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
        String tagName = (String) params.get("tagName");
        Integer tagType = parseTagType(params.get("tagType"));
        Integer sortOrder = params.get("sortOrder") != null ? Integer.valueOf(params.get("sortOrder").toString()) : 0;

        EvalTag tag;
        if (id != null) {
            tag = evalTagMapper.selectById(id);
            if (tag == null) {
                tag = new EvalTag();
            }
        } else {
            tag = new EvalTag();
        }

        tag.setTagName(tagName);
        tag.setTagType(tagType);
        tag.setSortOrder(sortOrder);

        if (tag.getId() == null) {
            evalTagMapper.insert(tag);
        } else {
            evalTagMapper.updateById(tag);
        }

        return Result.success("保存成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteTag(@PathVariable Long id, HttpServletRequest request) {
        evalTagMapper.deleteById(id);
        return Result.success("删除成功");
    }

    private Integer parseTagType(Object tagType) {
        if (tagType == null) {
            return null;
        }
        String value = tagType.toString().trim();
        if (value.isEmpty()) {
            return null;
        }
        if ("好评".equals(value)) {
            return 1;
        }
        if ("差评".equals(value)) {
            return 2;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String tagTypeText(Integer tagType) {
        if (tagType == null) {
            return "未知";
        }
        return tagType == 1 ? "好评" : "差评";
    }
}
