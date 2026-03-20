package com.satcom.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.satcom.platform.entity.Role;
import com.satcom.platform.entity.RoleMenu;
import com.satcom.platform.mapper.RoleMapper;
import com.satcom.platform.mapper.RoleMenuMapper;
import com.satcom.platform.service.RoleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMenuMapper roleMenuMapper;

    public RoleServiceImpl(RoleMenuMapper roleMenuMapper) {
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId)
        );
        return roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    @Override
    public void setRoleMenus(Long roleId, List<Long> menuIds) {
        // 先删除现有的角色菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId)
        );

        // 添加新的角色菜单关联
        LocalDateTime now = LocalDateTime.now();
        List<RoleMenu> roleMenus = menuIds.stream()
                .map(menuId -> {
                    RoleMenu roleMenu = new RoleMenu();
                    roleMenu.setRoleId(roleId);
                    roleMenu.setMenuId(menuId);
                    roleMenu.setCreatedAt(now);
                    roleMenu.setUpdatedAt(now);
                    return roleMenu;
                })
                .collect(Collectors.toList());

        if (!roleMenus.isEmpty()) {
            for (RoleMenu roleMenu : roleMenus) {
                roleMenuMapper.insert(roleMenu);
            }
        }
    }
}