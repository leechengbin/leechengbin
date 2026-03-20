package com.satcom.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备实体类
 * <p>卫星通信设备信息，包含资产编号、型号、状态等</p>
 *
 * @author satcom
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@TableName("equipment")
public class Equipment {
    private Long id;
    private String assetCode;
    private String model;
    private String status;
    private String ownerUnit;
    private String firmwareVersion;
    private Boolean locked;
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastMaintenanceAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
