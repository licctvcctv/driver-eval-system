package com.drivereval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.drivereval.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService extends IService<SensitiveWord> {

    List<SensitiveWord> loadAll();

    void initFilter();

    boolean checkText(String text);

    IPage<SensitiveWord> pageList(String keyword, Page<SensitiveWord> page);

    void addWord(String word);

    void deleteWord(Long id);
}
