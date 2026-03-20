package com.satcom.platform.test;

import com.satcom.platform.entity.Equipment;
import com.satcom.platform.entity.MonitoringStatus;
import com.satcom.platform.service.EquipmentService;
import com.satcom.platform.service.MonitoringStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class MonitoringStatusTestDataGenerator {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private MonitoringStatusService monitoringStatusService;

    @Test
    public void generateMonitoringStatusData() {
        List<Equipment> equipments = equipmentService.list();
        if (equipments.isEmpty()) {
            System.out.println("未找到设备数据，请先生成设备测试数据。");
            return;
        }

        Random random = new Random();
        LocalDateTime now = LocalDateTime.now();
        List<MonitoringStatus> statuses = new ArrayList<>();

        for (Equipment equipment : equipments) {
            if (random.nextDouble() < 0.1) {
                continue;
            }

            int samples = 3 + random.nextInt(3);
            for (int i = 0; i < samples; i++) {
                MonitoringStatus status = new MonitoringStatus();
                status.setEquipmentId(equipment.getId());
                status.setLatitude(equipment.getLatitude());
                status.setLongitude(equipment.getLongitude());
                status.setCollectedAt(now.minusMinutes(10L * i + random.nextInt(10)));
                fillMetrics(status, random, i);
                status.setDiagnosis(buildDiagnosis(status));
                statuses.add(status);
            }
        }

        monitoringStatusService.saveBatch(statuses);
        System.out.println("成功生成" + statuses.size() + "条监控状态测试数据！");
    }

    private void fillMetrics(MonitoringStatus status, Random random, int index) {
        double baseSignal = 60 + random.nextDouble() * 40;
        double baseSnr = 6 + random.nextDouble() * 6;
        double basePacketLoss = random.nextDouble() * 4;

        if (index % 3 == 1) {
            baseSnr = 3.5 + random.nextDouble() * 2;
        } else if (index % 3 == 2) {
            basePacketLoss = 6 + random.nextDouble() * 6;
        }

        status.setSignalQuality(baseSignal);
        status.setSnr(baseSnr);
        status.setPower(10 + random.nextDouble() * 20);
        status.setPacketLossRate(basePacketLoss);
        status.setLatencyMs(40 + random.nextDouble() * 120);
    }

    private String buildDiagnosis(MonitoringStatus status) {
        if (status.getPacketLossRate() != null && status.getPacketLossRate() > 5) {
            return "丢包率偏高";
        }
        if (status.getSnr() != null && status.getSnr() < 6) {
            return "信噪比偏低";
        }
        return "运行正常";
    }
}
