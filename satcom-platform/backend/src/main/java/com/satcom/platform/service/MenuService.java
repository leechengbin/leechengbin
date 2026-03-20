package com.satcom.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.satcom.platform.entity.Menu;

import java.util.List;

public interface MenuService extends IService<Menu> {
    List<Menu> getMenuTree();
    List<Menu> getMenusByRoleCode(String roleCode);
}