package com.satcom.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 * <p>用于返回给前端的用户信息，脱敏处理</p>
 *
 * @author satcom
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 显示名称
     */
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
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
