package com.drivereval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.drivereval.entity.SysUser;

public interface UserService extends IService<SysUser> {

    SysUser getUserInfo(Long userId);

    void updateProfile(Long userId, String realName, String phone, String avatar);

    IPage<SysUser> adminPageList(Integer role, String keyword, Page<SysUser> page);

    void toggleStatus(Long userId, Integer status);
}
