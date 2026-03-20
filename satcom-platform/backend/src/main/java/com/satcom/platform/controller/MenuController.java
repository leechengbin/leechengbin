package com.satcom.platform.controller;

import com.satcom.platform.common.ApiResponse;
import com.satcom.platform.entity.Menu;
import com.satcom.platform.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ApiResponse<List<Menu>> list() {
        List<Menu> menus = menuService.list();
        return ApiResponse.ok(menus);
    }

    @PostMapping
    public ApiResponse<Menu> create(@RequestBody Menu menu) {
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        menuService.save(menu);
        return ApiResponse.ok("菜单创建成功", menu);
    }

    @PutMapping("/{id}")
    public ApiResponse<Menu> update(@PathVariable Long id, @RequestBody Menu menu) {
        Menu existingMenu = menuService.getById(id);
        if (existingMenu == null) {
            return ApiResponse.fail("菜单不存在");
        }
        
        menu.setId(id);
        menu.setUpdatedAt(LocalDateTime.now());
        menuService.updateById(menu);
        return ApiResponse.ok("菜单更新成功", menu);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        menuService.removeById(id);
        return ApiResponse.ok("菜单删除成功");
    }

    @GetMapping("/tree")
    public ApiResponse<List<Menu>> getMenuTree() {
        List<Menu> menuTree = menuService.getMenuTree();
        return ApiResponse.ok(menuTree);
    }

    @GetMapping("/role/{roleCode}")
    public ApiResponse<List<Menu>> getMenusByRoleCode(@PathVariable String roleCode) {
        List<Menu> menus = menuService.getMenusByRoleCode(roleCode);
        return ApiResponse.ok(menus);
    }
}
