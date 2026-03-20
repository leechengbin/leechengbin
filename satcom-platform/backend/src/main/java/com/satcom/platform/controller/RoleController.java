package com.satcom.platform.controller;

import com.satcom.platform.common.ApiResponse;
import com.satcom.platform.entity.Role;
import com.satcom.platform.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<Role>> list() {
        List<Role> roles = roleService.list();
        return ApiResponse.ok(roles);
    }

    @PostMapping
    public ApiResponse<Role> create(@RequestBody Role role) {
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleService.save(role);
        return ApiResponse.ok("角色创建成功", role);
    }

    @PutMapping("/{id}")
    public ApiResponse<Role> update(@PathVariable Long id, @RequestBody Role role) {
        Role existingRole = roleService.getById(id);
        if (existingRole == null) {
            return ApiResponse.fail("角色不存在");
        }
        
        role.setId(id);
        role.setUpdatedAt(LocalDateTime.now());
        roleService.updateById(role);
        return ApiResponse.ok("角色更新成功", role);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return ApiResponse.ok("角色删除成功");
    }

    @GetMapping("/{id}/menus")
    public ApiResponse<List<Long>> getRoleMenus(@PathVariable Long id) {
        List<Long> menuIds = roleService.getMenuIdsByRoleId(id);
        return ApiResponse.ok(menuIds);
    }

    @PostMapping("/{id}/menus")
    public ApiResponse<String> setRoleMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.setRoleMenus(id, menuIds);
        return ApiResponse.ok("菜单权限设置成功");
    }
}
