package com.drivereval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drivereval.common.exception.BusinessException;
import com.drivereval.entity.SysUser;
import com.drivereval.mapper.SysUserMapper;
import com.drivereval.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public SysUser getUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 排除密码
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateProfile(Long userId, String realName, String phone, String avatar) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(realName)) {
            user.setRealName(realName);
        }
        if (StringUtils.hasText(phone)) {
            user.setPhone(phone);
        }
        if (StringUtils.hasText(avatar)) {
            user.setAvatar(avatar);
        }
        sysUserMapper.updateById(user);
    }

    @Override
    public IPage<SysUser> adminPageList(Integer role, String keyword, Page<SysUser> page) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(role != null, SysUser::getRole, role);
        wrapper.and(StringUtils.hasText(keyword), w ->
                w.like(SysUser::getUsername, keyword)
                        .or()
                        .like(SysUser::getRealName, keyword));
        wrapper.orderByDesc(SysUser::getCreateTime);
        // 排除密码字段
        wrapper.select(SysUser::getId, SysUser::getUsername, SysUser::getRealName,
                SysUser::getPhone, SysUser::getAvatar, SysUser::getRole,
                SysUser::getStatus, SysUser::getCreateTime, SysUser::getUpdateTime);
        return sysUserMapper.selectPage(page, wrapper);
    }

    @Override
    public void toggleStatus(Long userId, Integer status) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
    }
}
