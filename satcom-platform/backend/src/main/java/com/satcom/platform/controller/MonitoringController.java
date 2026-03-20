package com.satcom.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.satcom.platform.common.ApiResponse;
import com.satcom.platform.entity.Equipment;
import com.satcom.platform.entity.MonitoringStatus;
import com.satcom.platform.service.EquipmentService;
import com.satcom.platform.service.MonitoringStatusService;
import com.satcom.platform.service.UserEquipmentService;
import com.satcom.platform.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 监控管理控制器
 * <p>提供设备监控数据展示和统计分析功能</p>
 *
 * @author satcom
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringStatusService monitoringService;
    private final EquipmentService equipmentService;
    private final UserEquipmentService userEquipmentService;
    private final UserService userService;

    public MonitoringController(MonitoringStatusService monitoringService, EquipmentService equipmentService, UserEquipmentService userEquipmentService, UserService userService) {
        this.monitoringService = monitoringService;
        this.equipmentService = equipmentService;
        this.userEquipmentService = userEquipmentService;
        this.userService = userService;
    }

    /**
     * 获取用户授权的设备ID列表
     *
     * @return 授权设备ID列表
     */
    private List<Long> getAuthorizedEquipmentIds() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? String.valueOf(authentication.getPrincipal()) : null;
            
            if (username == null || "anonymousUser".equals(username)) {
                return null; // 返回null表示不限制
            }
            
            Long userId = userService.lambdaQuery()
                    .eq(com.satcom.platform.entity.User::getUsername, username)
                    .select(com.satcom.platform.entity.User::getId)
                    .oneOpt()
                    .map(com.satcom.platform.entity.User::getId)
                    .orElse(null);
            
            if (userId == null) {
                return null;
            }
            
            return userEquipmentService.getAuthorizedEquipmentIds(userId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取最新监控数据
     *
     * @param equipmentId 设备ID（可选）
     * @return 监控数据列表
     */
    @GetMapping("/latest")
    public ApiResponse<List<MonitoringStatus>> latest(@RequestParam(required = false) Long equipmentId) {
        LambdaQueryWrapper<MonitoringStatus> wrapper = new LambdaQueryWrapper<>();
        if (equipmentId != null) {
            wrapper.eq(MonitoringStatus::getEquipmentId, equipmentId);
        }
        wrapper.orderByDesc(MonitoringStatus::getCollectedAt).last("limit 20");
        return ApiResponse.ok(monitoringService.list(wrapper));
    }

    /**
     * 获取设备质量统计分布数据（用于热量分布图）
     *
     * @return 质量分布统计
     */
    @GetMapping("/statistics/quality-distribution")
    public ApiResponse<QualityDistribution> getQualityDistribution() {
        // 获取用户授权的设备ID列表，null表示不限制
        List<Long> authorizedEquipmentIds = getAuthorizedEquipmentIds();
        
        LambdaQueryWrapper<MonitoringStatus> wrapper = new LambdaQueryWrapper<>();
        if (authorizedEquipmentIds != null && !authorizedEquipmentIds.isEmpty()) {
            wrapper.in(MonitoringStatus::getEquipmentId, authorizedEquipmentIds);
        }
        wrapper.orderByDesc(MonitoringStatus::getCollectedAt).last("limit 1000");
        List<MonitoringStatus> statuses = monitoringService.list(wrapper);
        
        QualityDistribution distribution = new QualityDistribution();
        
        // 按设备分组，取最新数据
        Map<Long, MonitoringStatus> latestByEquipment = new HashMap<>();
        for (MonitoringStatus status : statuses) {
            latestByEquipment.putIfAbsent(status.getEquipmentId(), status);
        }
        
        // 计算各指标分布
        List<Double> signalQualities = new ArrayList<>();
        List<Double> snrs = new ArrayList<>();
        List<Double> powers = new ArrayList<>();
        List<Double> packetLossRates = new ArrayList<>();
        List<Double> latencys = new ArrayList<>();
        
        for (MonitoringStatus status : latestByEquipment.values()) {
            if (status.getSignalQuality() != null) signalQualities.add(status.getSignalQuality());
            if (status.getSnr() != null) snrs.add(status.getSnr());
            if (status.getPower() != null) powers.add(status.getPower());
            if (status.getPacketLossRate() != null) packetLossRates.add(status.getPacketLossRate());
            if (status.getLatencyMs() != null) latencys.add(status.getLatencyMs());
        }
        
        // 信号质量分布
        distribution.setSignalQualityAvg(calculateAvg(signalQualities));
        distribution.setSignalQualityMin(calculateMin(signalQualities));
        distribution.setSignalQualityMax(calculateMax(signalQualities));
        distribution.setSignalQualityDistribution(calculateDistribution(signalQualities, 10));
        
        // 信噪比分布
        distribution.setSnrAvg(calculateAvg(snrs));
        distribution.setSnrMin(calculateMin(snrs));
        distribution.setSnrMax(calculateMax(snrs));
        distribution.setSnrDistribution(calculateDistribution(snrs, 10));
        
        // 功率分布
        distribution.setPowerAvg(calculateAvg(powers));
        distribution.setPowerMin(calculateMin(powers));
        distribution.setPowerMax(calculateMax(powers));
        distribution.setPowerDistribution(calculateDistribution(powers, 10));
        
        // 丢包率分布
        distribution.setPacketLossRateAvg(calculateAvg(packetLossRates));
        distribution.setPacketLossRateMin(calculateMin(packetLossRates));
        distribution.setPacketLossRateMax(calculateMax(packetLossRates));
        distribution.setPacketLossRateDistribution(calculateDistribution(packetLossRates, 10));
        
        // 延迟分布
        distribution.setLatencyAvg(calculateAvg(latencys));
        distribution.setLatencyMin(calculateMin(latencys));
        distribution.setLatencyMax(calculateMax(latencys));
        distribution.setLatencyDistribution(calculateDistribution(latencys, 10));
        
        // 设备健康状态统计
        Map<String, Long> healthStats = new HashMap<>();
        long onlineCount = 0;
        long warningCount = 0;
        long alertCount = 0;
        long offlineCount = latestByEquipment.size();
        
        for (MonitoringStatus status : latestByEquipment.values()) {
            offlineCount--;
            String healthLevel = buildHealthLevel(status);
            if ("OK".equals(healthLevel)) {
                onlineCount++;
            } else if ("WARN".equals(healthLevel)) {
                warningCount++;
            } else if ("ALERT".equals(healthLevel)) {
                alertCount++;
            }
        }
        
        healthStats.put("ONLINE", onlineCount);
        healthStats.put("WARN", warningCount);
        healthStats.put("ALERT", alertCount);
        healthStats.put("OFFLINE", Math.max(0, offlineCount));
        distribution.setHealthStats(healthStats);
        
        distribution.setTotalDevices((long) latestByEquipment.size());
        
        return ApiResponse.ok(distribution);
    }

    /**
     * 获取设备质量热量数据（带位置信息）
     *
     * @return 设备质量热量数据列表
     */
    @GetMapping("/heatmap")
    public ApiResponse<List<HeatmapPoint>> getHeatmapData() {
        List<Long> authorizedEquipmentIds = getAuthorizedEquipmentIds();
        
        LambdaQueryWrapper<MonitoringStatus> wrapper = new LambdaQueryWrapper<>();
        if (authorizedEquipmentIds != null && !authorizedEquipmentIds.isEmpty()) {
            wrapper.in(MonitoringStatus::getEquipmentId, authorizedEquipmentIds);
        }
        wrapper.orderByDesc(MonitoringStatus::getCollectedAt).last("limit 500");
        List<MonitoringStatus> statuses = monitoringService.list(wrapper);
        
        // 获取最新数据按设备
        Map<Long, MonitoringStatus> latestByEquipment = new HashMap<>();
        for (MonitoringStatus status : statuses) {
            latestByEquipment.putIfAbsent(status.getEquipmentId(), status);
        }
        
        // 获取设备列表
        List<Equipment> equipments;
        if (authorizedEquipmentIds != null && !authorizedEquipmentIds.isEmpty()) {
            equipments = equipmentService.lambdaQuery()
                    .in(Equipment::getId, authorizedEquipmentIds)
                    .list();
        } else {
            equipments = equipmentService.list();
        }
        
        Map<Long, Equipment> equipmentMap = equipments.stream()
                .collect(Collectors.toMap(Equipment::getId, e -> e));
        
        List<HeatmapPoint> points = new ArrayList<>();
        for (Map.Entry<Long, MonitoringStatus> entry : latestByEquipment.entrySet()) {
            Long equipmentId = entry.getKey();
            MonitoringStatus status = entry.getValue();
            Equipment equipment = equipmentMap.get(equipmentId);
            
            if (equipment == null) continue;
            
            HeatmapPoint point = new HeatmapPoint();
            point.setEquipmentId(equipmentId);
            point.setAssetCode(equipment.getAssetCode());
            point.setLatitude(status.getLatitude() != null ? status.getLatitude() : equipment.getLatitude());
            point.setLongitude(status.getLongitude() != null ? status.getLongitude() : equipment.getLongitude());
            
            // 计算综合质量分数 (0-100)
            double qualityScore = calculateQualityScore(status);
            point.setQualityScore(qualityScore);
            
            // 质量等级
            point.setQualityLevel(getQualityLevel(qualityScore));
            
            // 各项指标
            point.setSignalQuality(status.getSignalQuality());
            point.setSnr(status.getSnr());
            point.setPower(status.getPower());
            point.setPacketLossRate(status.getPacketLossRate());
            point.setLatencyMs(status.getLatencyMs());
            
            points.add(point);
        }
        
        return ApiResponse.ok(points);
    }

    /**
     * 根据健康状态获取设备列表
     *
     * @param healthLevel 健康状态：ONLINE, WARN, ALERT, OFFLINE
     * @return 设备列表
     */
    @GetMapping("/devices-by-health")
    public ApiResponse<List<HeatmapPoint>> getDevicesByHealth(@RequestParam String healthLevel) {
        List<Long> authorizedEquipmentIds = getAuthorizedEquipmentIds();
        
        LambdaQueryWrapper<MonitoringStatus> wrapper = new LambdaQueryWrapper<>();
        if (authorizedEquipmentIds != null && !authorizedEquipmentIds.isEmpty()) {
            wrapper.in(MonitoringStatus::getEquipmentId, authorizedEquipmentIds);
        }
        wrapper.orderByDesc(MonitoringStatus::getCollectedAt).last("limit 500");
        List<MonitoringStatus> statuses = monitoringService.list(wrapper);
        
        // 获取最新数据按设备
        Map<Long, MonitoringStatus> latestByEquipment = new HashMap<>();
        for (MonitoringStatus status : statuses) {
            latestByEquipment.putIfAbsent(status.getEquipmentId(), status);
        }
        
        // 获取设备列表
        List<Equipment> equipments;
        if (authorizedEquipmentIds != null && !authorizedEquipmentIds.isEmpty()) {
            equipments = equipmentService.lambdaQuery()
                    .in(Equipment::getId, authorizedEquipmentIds)
                    .list();
        } else {
            equipments = equipmentService.list();
        }
        
        Map<Long, Equipment> equipmentMap = equipments.stream()
                .collect(Collectors.toMap(Equipment::getId, e -> e));
        
        List<HeatmapPoint> points = new ArrayList<>();
        for (Map.Entry<Long, MonitoringStatus> entry : latestByEquipment.entrySet()) {
            Long equipmentId = entry.getKey();
            MonitoringStatus status = entry.getValue();
            Equipment equipment = equipmentMap.get(equipmentId);
            
            if (equipment == null) continue;
            
            double qualityScore = calculateQualityScore(status);
            String qualityLevel = getQualityLevel(qualityScore);
            
            // 根据请求的健康状态过滤
            boolean match = false;
            String healthLevelFromStatus = buildHealthLevel(status);
            // 兼容前端传入的 ONLINE/OFFLINE
            String requestedLevel = healthLevel;
            if ("ONLINE".equals(healthLevel)) {
                requestedLevel = "OK";
            }
            if ("OFFLINE".equals(healthLevel)) {
                match = status == null;
            } else {
                match = requestedLevel.equals(healthLevelFromStatus);
            }
            
            if (!match) continue;
            
            HeatmapPoint point = new HeatmapPoint();
            point.setEquipmentId(equipmentId);
            point.setAssetCode(equipment.getAssetCode());
            point.setLatitude(status != null && status.getLatitude() != null ? status.getLatitude() : equipment.getLatitude());
            point.setLongitude(status != null && status.getLongitude() != null ? status.getLongitude() : equipment.getLongitude());
            point.setQualityScore(qualityScore);
            point.setQualityLevel(qualityLevel);
            
            if (status != null) {
                point.setSignalQuality(status.getSignalQuality());
                point.setSnr(status.getSnr());
                point.setPower(status.getPower());
                point.setPacketLossRate(status.getPacketLossRate());
                point.setLatencyMs(status.getLatencyMs());
            }
            
            points.add(point);
        }
        
        return ApiResponse.ok(points);
    }

    private double calculateQualityScore(MonitoringStatus status) {
        double score = 100;
        
        // 信号质量扣分 (-30)
        if (status.getSignalQuality() != null) {
            if (status.getSignalQuality() < 30) score -= 30;
            else if (status.getSignalQuality() < 50) score -= 20;
            else if (status.getSignalQuality() < 70) score -= 10;
        }
        
        // 信噪比扣分 (-25)
        if (status.getSnr() != null) {
            if (status.getSnr() < 5) score -= 25;
            else if (status.getSnr() < 10) score -= 15;
            else if (status.getSnr() < 15) score -= 5;
        }
        
        // 丢包率扣分 (-25)
        if (status.getPacketLossRate() != null) {
            if (status.getPacketLossRate() > 10) score -= 25;
            else if (status.getPacketLossRate() > 5) score -= 15;
            else if (status.getPacketLossRate() > 1) score -= 5;
        }
        
        // 延迟扣分 (-20)
        if (status.getLatencyMs() != null) {
            if (status.getLatencyMs() > 1000) score -= 20;
            else if (status.getLatencyMs() > 500) score -= 10;
            else if (status.getLatencyMs() > 200) score -= 5;
        }
        
        return Math.max(0, score);
    }

    private String getQualityLevel(double score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 60) return "GOOD";
        if (score >= 40) return "FAIR";
        if (score >= 20) return "POOR";
        return "CRITICAL";
    }

    private double calculateAvg(List<Double> values) {
        if (values == null || values.isEmpty()) return 0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double calculateMin(List<Double> values) {
        if (values == null || values.isEmpty()) return 0;
        return values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    }

    private double calculateMax(List<Double> values) {
        if (values == null || values.isEmpty()) return 0;
        return values.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    private Map<String, Long> calculateDistribution(List<Double> values, int buckets) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyMap();
        }
        
        double min = calculateMin(values);
        double max = calculateMax(values);
        double range = max - min;
        if (range == 0) range = 1;
        
        Map<String, Long> distribution = new LinkedHashMap<>();
        double bucketSize = range / buckets;
        
        for (int i = 0; i < buckets; i++) {
            double bucketStart = min + i * bucketSize;
            double bucketEnd = bucketStart + bucketSize;
            String label = String.format("%.1f-%.1f", bucketStart, bucketEnd);
            final double bs = bucketStart;
            final double be = bucketEnd;
            long count = values.stream()
                    .filter(v -> v >= bs && v < be)
                    .count();
            distribution.put(label, count);
        }
        
        return distribution;
    }

    @GetMapping("/map")
    public ApiResponse<List<MonitoringMapPoint>> mapLatest() {
        LambdaQueryWrapper<MonitoringStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(MonitoringStatus::getCollectedAt).last("limit 500");
        List<MonitoringStatus> statuses = monitoringService.list(wrapper);

        Map<Long, MonitoringStatus> latestStatusMap = new HashMap<>();
        for (MonitoringStatus status : statuses) {
            latestStatusMap.putIfAbsent(status.getEquipmentId(), status);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? String.valueOf(authentication.getPrincipal()) : null;
        if (username == null || "anonymousUser".equals(username)) {
            return ApiResponse.fail("未登录");
        }

        Long userId = userService.lambdaQuery()
                .eq(com.satcom.platform.entity.User::getUsername, username)
                .select(com.satcom.platform.entity.User::getId)
                .oneOpt()
                .map(com.satcom.platform.entity.User::getId)
                .orElse(null);

        if (userId == null) {
            return ApiResponse.fail("用户不存在");
        }

        List<Long> equipmentIds = userEquipmentService.getAuthorizedEquipmentIds(userId);
        if (equipmentIds.isEmpty()) {
            return ApiResponse.ok(List.of());
        }

        List<Equipment> equipments = equipmentService.lambdaQuery()
                .in(Equipment::getId, equipmentIds)
                .list();
        List<MonitoringMapPoint> points = equipments.stream().map(equipment -> {
            MonitoringStatus status = latestStatusMap.get(equipment.getId());
            MonitoringMapPoint point = new MonitoringMapPoint();
            point.setEquipmentId(equipment.getId());
            point.setAssetCode(equipment.getAssetCode());
            point.setModel(equipment.getModel());
            point.setStatus(equipment.getStatus());
            point.setLatitude(status != null && status.getLatitude() != null ? status.getLatitude() : equipment.getLatitude());
            point.setLongitude(status != null && status.getLongitude() != null ? status.getLongitude() : equipment.getLongitude());
            point.setSignalQuality(status != null ? status.getSignalQuality() : null);
            point.setSnr(status != null ? status.getSnr() : null);
            point.setPacketLossRate(status != null ? status.getPacketLossRate() : null);
            point.setDiagnosis(status != null ? status.getDiagnosis() : null);
            point.setCollectedAt(status != null ? status.getCollectedAt() : null);
            point.setHealthLevel(status != null ? buildHealthLevel(status) : "OFFLINE");
            return point;
        }).toList();

        return ApiResponse.ok(points);
    }

    private String buildHealthLevel(MonitoringStatus status) {
        if (status == null) {
            return "OFFLINE";
        }
        if (status.getPacketLossRate() != null && status.getPacketLossRate() > 5) {
            return "ALERT";
        }
        if (status.getSnr() != null && status.getSnr() < 6) {
            return "WARN";
        }
        return "OK";
    }

    /**
     * 上报监控数据
     *
     * @param status 监控数据
     * @return 操作结果
     */
    @PostMapping
    public ApiResponse<MonitoringStatus> report(@RequestBody MonitoringStatus status) {
        status.setCollectedAt(LocalDateTime.now());
        monitoringService.save(status);
        return ApiResponse.ok("已上报", status);
    }

    // 内部类
    public static class MonitoringMapPoint {
        private Long equipmentId;
        private String assetCode;
        private String model;
        private String status;
        private Double latitude;
        private Double longitude;
        private Double signalQuality;
        private Double snr;
        private Double packetLossRate;
        private String diagnosis;
        private LocalDateTime collectedAt;
        private String healthLevel;

        public Long getEquipmentId() { return equipmentId; }
        public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
        public String getAssetCode() { return assetCode; }
        public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public Double getSignalQuality() { return signalQuality; }
        public void setSignalQuality(Double signalQuality) { this.signalQuality = signalQuality; }
        public Double getSnr() { return snr; }
        public void setSnr(Double snr) { this.snr = snr; }
        public Double getPacketLossRate() { return packetLossRate; }
        public void setPacketLossRate(Double packetLossRate) { this.packetLossRate = packetLossRate; }
        public String getDiagnosis() { return diagnosis; }
        public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
        public LocalDateTime getCollectedAt() { return collectedAt; }
        public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
        public String getHealthLevel() { return healthLevel; }
        public void setHealthLevel(String healthLevel) { this.healthLevel = healthLevel; }
    }

    // 质量分布统计类
    public static class QualityDistribution {
        private Long totalDevices;
        private Double signalQualityAvg;
        private Double signalQualityMin;
        private Double signalQualityMax;
        private Map<String, Long> signalQualityDistribution;
        private Double snrAvg;
        private Double snrMin;
        private Double snrMax;
        private Map<String, Long> snrDistribution;
        private Double powerAvg;
        private Double powerMin;
        private Double powerMax;
        private Map<String, Long> powerDistribution;
        private Double packetLossRateAvg;
        private Double packetLossRateMin;
        private Double packetLossRateMax;
        private Map<String, Long> packetLossRateDistribution;
        private Double latencyAvg;
        private Double latencyMin;
        private Double latencyMax;
        private Map<String, Long> latencyDistribution;
        private Map<String, Long> healthStats;

        // Getters and Setters
        public Long getTotalDevices() { return totalDevices; }
        public void setTotalDevices(Long totalDevices) { this.totalDevices = totalDevices; }
        public Double getSignalQualityAvg() { return signalQualityAvg; }
        public void setSignalQualityAvg(Double signalQualityAvg) { this.signalQualityAvg = signalQualityAvg; }
        public Double getSignalQualityMin() { return signalQualityMin; }
        public void setSignalQualityMin(Double signalQualityMin) { this.signalQualityMin = signalQualityMin; }
        public Double getSignalQualityMax() { return signalQualityMax; }
        public void setSignalQualityMax(Double signalQualityMax) { this.signalQualityMax = signalQualityMax; }
        public Map<String, Long> getSignalQualityDistribution() { return signalQualityDistribution; }
        public void setSignalQualityDistribution(Map<String, Long> signalQualityDistribution) { this.signalQualityDistribution = signalQualityDistribution; }
        public Double getSnrAvg() { return snrAvg; }
        public void setSnrAvg(Double snrAvg) { this.snrAvg = snrAvg; }
        public Double getSnrMin() { return snrMin; }
        public void setSnrMin(Double snrMin) { this.snrMin = snrMin; }
        public Double getSnrMax() { return snrMax; }
        public void setSnrMax(Double snrMax) { this.snrMax = snrMax; }
        public Map<String, Long> getSnrDistribution() { return snrDistribution; }
        public void setSnrDistribution(Map<String, Long> snrDistribution) { this.snrDistribution = snrDistribution; }
        public Double getPowerAvg() { return powerAvg; }
        public void setPowerAvg(Double powerAvg) { this.powerAvg = powerAvg; }
        public Double getPowerMin() { return powerMin; }
        public void setPowerMin(Double powerMin) { this.powerMin = powerMin; }
        public Double getPowerMax() { return powerMax; }
        public void setPowerMax(Double powerMax) { this.powerMax = powerMax; }
        public Map<String, Long> getPowerDistribution() { return powerDistribution; }
        public void setPowerDistribution(Map<String, Long> powerDistribution) { this.powerDistribution = powerDistribution; }
        public Double getPacketLossRateAvg() { return packetLossRateAvg; }
        public void setPacketLossRateAvg(Double packetLossRateAvg) { this.packetLossRateAvg = packetLossRateAvg; }
        public Double getPacketLossRateMin() { return packetLossRateMin; }
        public void setPacketLossRateMin(Double packetLossRateMin) { this.packetLossRateMin = packetLossRateMin; }
        public Double getPacketLossRateMax() { return packetLossRateMax; }
        public void setPacketLossRateMax(Double packetLossRateMax) { this.packetLossRateMax = packetLossRateMax; }
        public Map<String, Long> getPacketLossRateDistribution() { return packetLossRateDistribution; }
        public void setPacketLossRateDistribution(Map<String, Long> packetLossRateDistribution) { this.packetLossRateDistribution = packetLossRateDistribution; }
        public Double getLatencyAvg() { return latencyAvg; }
        public void setLatencyAvg(Double latencyAvg) { this.latencyAvg = latencyAvg; }
        public Double getLatencyMin() { return latencyMin; }
        public void setLatencyMin(Double latencyMin) { this.latencyMin = latencyMin; }
        public Double getLatencyMax() { return latencyMax; }
        public void setLatencyMax(Double latencyMax) { this.latencyMax = latencyMax; }
        public Map<String, Long> getLatencyDistribution() { return latencyDistribution; }
        public void setLatencyDistribution(Map<String, Long> latencyDistribution) { this.latencyDistribution = latencyDistribution; }
        public Map<String, Long> getHealthStats() { return healthStats; }
        public void setHealthStats(Map<String, Long> healthStats) { this.healthStats = healthStats; }
    }

    // 热量图数据点类
    public static class HeatmapPoint {
        private Long equipmentId;
        private String assetCode;
        private Double latitude;
        private Double longitude;
        private Double qualityScore;
        private String qualityLevel;
        private Double signalQuality;
        private Double snr;
        private Double power;
        private Double packetLossRate;
        private Double latencyMs;

        public Long getEquipmentId() { return equipmentId; }
        public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
        public String getAssetCode() { return assetCode; }
        public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public Double getQualityScore() { return qualityScore; }
        public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }
        public String getQualityLevel() { return qualityLevel; }
        public void setQualityLevel(String qualityLevel) { this.qualityLevel = qualityLevel; }
        public Double getSignalQuality() { return signalQuality; }
        public void setSignalQuality(Double signalQuality) { this.signalQuality = signalQuality; }
        public Double getSnr() { return snr; }
        public void setSnr(Double snr) { this.snr = snr; }
        public Double getPower() { return power; }
        public void setPower(Double power) { this.power = power; }
        public Double getPacketLossRate() { return packetLossRate; }
        public void setPacketLossRate(Double packetLossRate) { this.packetLossRate = packetLossRate; }
        public Double getLatencyMs() { return latencyMs; }
        public void setLatencyMs(Double latencyMs) { this.latencyMs = latencyMs; }
    }
}
