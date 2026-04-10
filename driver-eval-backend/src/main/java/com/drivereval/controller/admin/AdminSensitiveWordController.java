package com.drivereval.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Result;
import com.drivereval.entity.SensitiveWord;
import com.drivereval.mapper.SensitiveWordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sensitive-word")
public class AdminSensitiveWordController {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Integer getRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    @GetMapping("/list")
    public Result<?> wordList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Page<SensitiveWord> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SensitiveWord> wrapper = new QueryWrapper<SensitiveWord>()
                .orderByDesc("create_time");

        return Result.success(sensitiveWordMapper.selectPage(page, wrapper));
    }

    @PostMapping("/save")
    public Result<?> saveWord(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String word = params.get("word");
        if (word == null || word.isEmpty()) {
            return Result.error("敏感词不能为空");
        }

        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(word);
        sensitiveWordMapper.insert(sensitiveWord);

        return Result.success("保存成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteWord(@PathVariable Long id, HttpServletRequest request) {
        sensitiveWordMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
