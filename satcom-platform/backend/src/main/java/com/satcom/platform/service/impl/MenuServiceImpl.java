package com.satcom.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.satcom.platform.entity.Menu;
import com.satcom.platform.entity.Role;
import com.satcom.platform.entity.RoleMenu;
import com.satcom.platform.mapper.MenuMapper;
import com.satcom.platform.mapper.RoleMapper;
import com.satcom.platform.mapper.RoleMenuMapper;
import com.satcom.platform.service.MenuService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    private final RoleMenuMapper roleMenuMapper;
    private final RoleMapper roleMapper;

    public MenuServiceImpl(RoleMenuMapper roleMenuMapper, RoleMapper roleMapper) {
        this.roleMenuMapper = roleMenuMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public List<Menu> getMenuTree() {
        List<Menu> allMenus = this.list(new LambdaQueryWrapper<Menu>()
                .orderByAsc(Menu::getOrderNum));
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public List<Menu> getMenusByRoleCode(String roleCode) {
        // 根据角色编码查询角色信息
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, roleCode));
        
        if (role == null) {
            return List.of();
        }
        
        // 根据角色ID查询角色菜单关联关系
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(new LambdaQueryWrapper<RoleMenu>()
                .eq(RoleMenu::getRoleId, role.getId()));
        
        if (roleMenus.isEmpty()) {
            return List.of();
        }
        
        // 提取菜单ID列表
        List<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());
        
        // 根据菜单ID列表查询菜单信息
        return this.list(new LambdaQueryWrapper<Menu>()
                .in(Menu::getId, menuIds)
                .orderByAsc(Menu::getOrderNum));
    }

    private List<Menu> buildMenuTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .map(menu -> {
                    List<Menu> children = buildMenuTree(menus, menu.getId());
                    if (!children.isEmpty()) {
                        // 这里可以添加children属性，需要在Menu实体类中添加
                    }
                    return menu;
                })
                .collect(Collectors.toList());
    }
}