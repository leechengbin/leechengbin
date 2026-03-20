package com.satcom.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.satcom.platform.common.ApiResponse;
import com.satcom.platform.common.PasswordUtils;
import com.satcom.platform.entity.User;
import com.satcom.platform.entity.UserEquipment;
import com.satcom.platform.service.UserEquipmentService;
import com.satcom.platform.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserEquipmentService userEquipmentService;

    public UserController(UserService userService, UserEquipmentService userEquipmentService) {
        this.userService = userService;
        this.userEquipmentService = userEquipmentService;
    }

    // 获取当前登录用户信息
    @GetMapping("/current")
    public ApiResponse<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        if (username == null || "anonymousUser".equals(username)) {
            return ApiResponse.fail("未登录");
        }
        
        User user = userService.lambdaQuery()
                .eq(User::getUsername, username)
                .oneOpt()
                .orElse(null);
        
        if (user == null) {
            return ApiResponse.fail("用户不存在");
        }
        
        return ApiResponse.ok(user);
    }

    // 修改当前用户密码
    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(@RequestBody Map<String, String> passwordData) {
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return ApiResponse.fail("参数不完整");
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        if (username == null || "anonymousUser".equals(username)) {
            return ApiResponse.fail("未登录");
        }
        
        User user = userService.lambdaQuery()
                .eq(User::getUsername, username)
                .oneOpt()
                .orElse(null);
        
        if (user == null) {
            return ApiResponse.fail("用户不存在");
        }
        
        // 验证旧密码
        if (!PasswordUtils.matches(oldPassword, user.getPasswordHash())) {
            return ApiResponse.fail("原密码错误");
        }
        
        // 更新新密码
        user.setPasswordHash(PasswordUtils.encodePassword(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userService.updateById(user);
        
        return ApiResponse.ok("密码修改成功");
    }

    @GetMapping
    public ApiResponse<List<User>> list(@RequestParam(required = false) String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null && !role.isBlank()) {
            wrapper.eq(User::getRoleCode, role);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return ApiResponse.ok(userService.list(wrapper));
    }

    @PostMapping
    public ApiResponse<User> create(@RequestBody User user) {
        // 密码加密
        user.setPasswordHash(PasswordUtils.encodePassword("123456")); // 默认密码
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userService.save(user);
        return ApiResponse.ok("账号已创建，默认密码：123456", user);
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable Long id, @RequestBody User user) {
        User existingUser = userService.getById(id);
        if (existingUser == null) {
            return ApiResponse.fail("用户不存在");
        }
        
        existingUser.setDisplayName(user.getDisplayName());
        existingUser.setRoleCode(user.getRoleCode());
        existingUser.setEnabled(user.getEnabled());
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        userService.updateById(existingUser);
        return ApiResponse.ok("用户信息已更新", existingUser);
    }

    @PutMapping("/{id}/password")
    public ApiResponse<String> resetPassword(@PathVariable Long id, @RequestParam String password) {
        User user = userService.getById(id);
        if (user == null) {
            return ApiResponse.fail("用户不存在");
        }
        
        user.setPasswordHash(PasswordUtils.encodePassword(password));
        user.setUpdatedAt(LocalDateTime.now());
        userService.updateById(user);
        return ApiResponse.ok("密码已重置");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        userService.removeById(id);
        return ApiResponse.ok("用户已删除");
    }

    // 用户装备权限管理
    @GetMapping("/{userId}/equipment")
    public ApiResponse<List<Long>> getAuthorizedEquipmentIds(@PathVariable Long userId) {
        List<Long> equipmentIds = userEquipmentService.getAuthorizedEquipmentIds(userId);
        return ApiResponse.ok(equipmentIds);
    }

    @PostMapping("/{userId}/equipment")
    public ApiResponse<String> grantEquipmentPermissions(
            @PathVariable Long userId,
            @RequestBody EquipmentPermissionRequest request) {
        userEquipmentService.grantPermissions(
                userId,
                request.getEquipmentIds(),
                request.isViewPermission(),
                request.isEditPermission()
        );
        return ApiResponse.ok("装备权限已设置");
    }

    @DeleteMapping("/{userId}/equipment")
    public ApiResponse<String> revokeEquipmentPermissions(
            @PathVariable Long userId,
            @RequestBody List<Long> equipmentIds) {
        userEquipmentService.revokePermissions(userId, equipmentIds);
        return ApiResponse.ok("装备权限已撤销");
    }

    // 获取用户装备权限详情
    @GetMapping("/{userId}/equipment-permissions")
    public ApiResponse<List<UserEquipment>> getEquipmentPermissions(@PathVariable Long userId) {
        List<UserEquipment> permissions = userEquipmentService.getPermissionsByUserId(userId);
        return ApiResponse.ok(permissions);
    }

    // 保存用户装备权限
    @PostMapping("/{userId}/equipment-permissions")
    public ApiResponse<String> saveEquipmentPermissions(
            @PathVariable Long userId,
            @RequestBody List<UserEquipmentService.EquipmentPermissionItem> permissions) {
        userEquipmentService.savePermissions(userId, permissions);
        return ApiResponse.ok("装备权限已保存");
    }

    // 权限请求DTO
    @lombok.Data
    public static class EquipmentPermissionRequest {
        private List<Long> equipmentIds;
        private boolean viewPermission;
        private boolean editPermission;
    }
}

