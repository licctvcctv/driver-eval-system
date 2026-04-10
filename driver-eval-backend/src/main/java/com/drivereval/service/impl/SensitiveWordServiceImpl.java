package com.drivereval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drivereval.common.exception.BusinessException;
import com.drivereval.common.util.SensitiveWordUtil;
import com.drivereval.entity.SensitiveWord;
import com.drivereval.mapper.SensitiveWordMapper;
import com.drivereval.service.SensitiveWordService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord>
        implements SensitiveWordService {

    @PostConstruct
    public void init() {
        initFilter();
    }

    @Override
    public List<SensitiveWord> loadAll() {
        return list(new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getIsDeleted, 0));
    }

    @Override
    public void initFilter() {
        List<SensitiveWord> words = loadAll();
        List<String> wordList = words.stream()
                .map(SensitiveWord::getWord)
                .collect(Collectors.toList());
        SensitiveWordUtil.init(wordList);
    }

    @Override
    public boolean checkText(String text) {
        return SensitiveWordUtil.contains(text);
    }

    @Override
    public IPage<SensitiveWord> pageList(String keyword, Page<SensitiveWord> page) {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), SensitiveWord::getWord, keyword);
        wrapper.orderByDesc(SensitiveWord::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public void addWord(String word) {
        if (!StringUtils.hasText(word)) {
            throw new BusinessException("敏感词不能为空");
        }
        // 检查是否已存在
        long count = count(new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getWord, word));
        if (count > 0) {
            throw new BusinessException("该敏感词已存在");
        }
        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(word);
        save(sensitiveWord);
        // 重新初始化过滤器
        initFilter();
    }

    @Override
    public void deleteWord(Long id) {
        removeById(id);
        // 重新初始化过滤器
        initFilter();
    }
}
