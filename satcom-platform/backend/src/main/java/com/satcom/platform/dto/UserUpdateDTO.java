package com.satcom.platform.dto;

import lombok.Data;

import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 用户更新 DTO
 * <p>用于更新用户信息时接收参数</p>
 *
 * @author satcom
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class UserUpdateDTO {

    /**
     * 显示名称
     */
    @Size(max = 100, message = "显示名称长度不能超过100")
    private String displayName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 授权设备ID列表
     */
    private List<Long> equipmentIds;

    /**
     * 设备查看权限
     */
    private Boolean viewPermission;

    /**
     * 设备编辑权限
     */
    private Boolean editPermission;
}
