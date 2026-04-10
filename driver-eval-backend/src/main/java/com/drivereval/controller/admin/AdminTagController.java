package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.drivereval.common.Result;
import com.drivereval.entity.EvalTag;
import com.drivereval.mapper.EvalTagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.drivereval.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tag")
public class AdminTagController extends BaseController {

    @Autowired
    private EvalTagMapper evalTagMapper;

    @GetMapping("/list")
    public Result<?> tagList(HttpServletRequest request) {
        return Result.success(evalTagMapper.selectList(
                new QueryWrapper<EvalTag>().orderByAsc("sort_order")));
    }

    @PostMapping("/save")
    public Result<?> saveTag(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String tagName = (String) params.get("tagName");
        Integer tagType = params.get("tagType") != null ? Integer.valueOf(params.get("tagType").toString()) : null;
        Integer sortOrder = params.get("sortOrder") != null ? Integer.valueOf(params.get("sortOrder").toString()) : 0;

        EvalTag tag = new EvalTag();
        tag.setTagName(tagName);
        tag.setTagType(tagType);
        tag.setSortOrder(sortOrder);
        evalTagMapper.insert(tag);

        return Result.success("保存成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteTag(@PathVariable Long id, HttpServletRequest request) {
        evalTagMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
