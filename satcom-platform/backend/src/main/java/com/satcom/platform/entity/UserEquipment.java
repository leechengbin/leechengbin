package com.satcom.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user_equipment")
public class UserEquipment {
    private Long id;
    private Long userId;
    private Long equipmentId;
    private Boolean viewPermission;
    private Boolean editPermission;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
