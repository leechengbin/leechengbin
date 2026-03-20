package com.satcom.platform.controller;

import com.satcom.platform.common.ApiResponse;
import com.satcom.platform.common.JwtUtils;
import com.satcom.platform.common.PasswordUtils;
import com.satcom.platform.entity.User;
import com.satcom.platform.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 查找用户
        User user = userService.lambdaQuery()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getEnabled, true)
                .one();

        if (user == null || !PasswordUtils.matches(request.getPassword(), user.getPasswordHash())) {
            return ApiResponse.fail("用户名或密码错误");
        }

        // 生成 JWT token
        String token = jwtUtils.generateToken(user.getUsername(), user.getRoleCode());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRole(user.getRoleCode());
        response.setDisplayName(user.getDisplayName());
        return ApiResponse.ok("登录成功", response);
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String role;
        private String displayName;
    }

    // 初始化管理员账户
    @PostMapping("/init-admin")
    public ApiResponse<String> initAdmin() {
        // 检查是否已存在管理员账户
        long count = userService.lambdaQuery()
                .eq(User::getUsername, "admin")
                .count();

        if (count > 0) {
            return ApiResponse.ok("管理员账户已存在");
        }

        // 创建管理员账户
        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(PasswordUtils.encodePassword("123456"));
        admin.setDisplayName("系统管理员");
        admin.setRoleCode("ADMIN");
        admin.setEnabled(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        userService.save(admin);
        return ApiResponse.ok("管理员账户初始化成功");
    }
}
