package com.satcom.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.satcom.platform.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {
    List<Long> getMenuIdsByRoleId(Long roleId);
    void setRoleMenus(Long roleId, List<Long> menuIds);
}