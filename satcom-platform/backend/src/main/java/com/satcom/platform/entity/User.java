package com.satcom.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * <p>系统用户信息，包含登录账号、角色、状态等</p>
 *
 * @author satcom
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@TableName("sys_user")
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String displayName;
    private String roleCode;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
