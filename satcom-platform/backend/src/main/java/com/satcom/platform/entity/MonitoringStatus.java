package com.satcom.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("monitoring_status")
public class MonitoringStatus {
    private Long id;
    private Long equipmentId;
    private Double signalQuality;
    private Double snr;
    private Double power;
    private Double packetLossRate;
    private Double latencyMs;
    private Double latitude;
    private Double longitude;
    private String diagnosis;
    private LocalDateTime collectedAt;
}
