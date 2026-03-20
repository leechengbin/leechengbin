package com.satcom.platform.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户创建 DTO
 * <p>用于创建新用户时接收参数</p>
 *
 * @author satcom
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class UserCreateDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;

    /**
     * 显示名称
     */
    @NotBlank(message = "显示名称不能为空")
    @Size(max = 100, message = "显示名称长度不能超过100")
    private String displayName;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色不能为空")
    private String roleCode;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 初始密码（可选，默认123456）
     */
    private String password;
}
