package com.satcom.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("equipment_record")
public class EquipmentRecord {
    private Long id;
    private Long equipmentId;
    private String recordType;
    private String description;
    private LocalDateTime recordAt;
    private LocalDateTime createdAt;
}
