package com.satcom.platform.test;

import com.satcom.platform.entity.Equipment;
import com.satcom.platform.service.EquipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class EquipmentTestDataGenerator {

    @Autowired
    private EquipmentService equipmentService;

    // 全国主要大中型城市列表，包含城市名称、纬度、经度
    private static final City[] CITIES = {
        new City("北京", 39.9042, 116.4074),
        new City("上海", 31.2304, 121.4737),
        new City("广州", 23.1291, 113.2644),
        new City("深圳", 22.5431, 114.0579),
        new City("杭州", 30.2741, 120.1551),
        new City("成都", 30.5728, 104.0668),
        new City("武汉", 30.5928, 114.3055),
        new City("西安", 34.3416, 108.9398),
        new City("南京", 32.0603, 118.7969),
        new City("重庆", 29.4316, 106.9123),
        new City("天津", 39.0842, 117.2010),
        new City("苏州", 31.2989, 120.5853),
        new City("郑州", 34.7466, 113.6253),
        new City("长沙", 28.2278, 112.9388),
        new City("沈阳", 41.8057, 123.4315),
        new City("青岛", 36.0611, 120.3826),
        new City("宁波", 29.8683, 121.5440),
        new City("东莞", 23.0418, 113.7522),
        new City("无锡", 31.5593, 120.3326),
        new City("济南", 36.6512, 117.1201),
        new City("大连", 38.9140, 121.6147),
        new City("福州", 26.0745, 119.2965),
        new City("厦门", 24.4798, 118.0819),
        new City("哈尔滨", 45.8038, 126.5349),
        new City("昆明", 25.0389, 102.7183),
        new City("合肥", 31.8639, 117.2808),
        new City("南宁", 22.8170, 108.3665),
        new City("南昌", 28.6820, 115.8579),
        new City("贵阳", 26.5783, 106.7135),
        new City("太原", 37.8706, 112.5489)
    };

    // 设备型号列表
    private static final String[] MODELS = {
        "SAT-5000A", "SAT-5000B", "SAT-6000A", "SAT-6000B", "SAT-7000A",
        "SAT-7000B", "SAT-8000A", "SAT-8000B", "SAT-9000A", "SAT-9000B"
    };

    // 设备状态列表
    private static final String[] STATUSES = {
        "正常", "维护中", "故障", "待激活", "停用"
    };

    // 所属单位列表
    private static final String[] OWNER_UNITS = {
        "中国移动", "中国联通", "中国电信", "中国铁塔", "广电网络",
        "军队系统", "公安系统", "交通系统", "能源系统", "教育系统"
    };

    // 固件版本列表
    private static final String[] FIRMWARE_VERSIONS = {
        "v1.0.0", "v1.1.0", "v1.2.0", "v2.0.0", "v2.1.0"
    };

    @Test
    public void generateTestData() {
        List<Equipment> equipmentList = new ArrayList<>();
        Random random = new Random();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= 100; i++) {
            Equipment equipment = new Equipment();
            
            // 资产编码
            equipment.setAssetCode("SAT" + String.format("%06d", i));
            
            // 随机选择型号
            equipment.setModel(MODELS[random.nextInt(MODELS.length)]);
            
            // 随机选择状态
            equipment.setStatus(STATUSES[random.nextInt(STATUSES.length)]);
            
            // 随机选择所属单位
            equipment.setOwnerUnit(OWNER_UNITS[random.nextInt(OWNER_UNITS.length)]);
            
            // 随机选择固件版本
            equipment.setFirmwareVersion(FIRMWARE_VERSIONS[random.nextInt(FIRMWARE_VERSIONS.length)]);
            
            // 随机锁定状态
            equipment.setLocked(random.nextBoolean());
            
            // 随机选择城市并设置坐标
            City city = CITIES[random.nextInt(CITIES.length)];
            // 添加一点随机偏移，使坐标更真实
            double latOffset = (random.nextDouble() - 0.5) * 0.1;
            double lngOffset = (random.nextDouble() - 0.5) * 0.1;
            equipment.setLatitude(city.latitude + latOffset);
            equipment.setLongitude(city.longitude + lngOffset);
            
            // 随机设置最后维护时间
            equipment.setLastMaintenanceAt(now.minusDays(random.nextInt(90)));
            
            // 设置创建和更新时间
            equipment.setCreatedAt(now);
            equipment.setUpdatedAt(now);
            
            equipmentList.add(equipment);
        }

        // 批量保存设备数据
        equipmentService.saveBatch(equipmentList);
        System.out.println("成功生成100条设备测试数据！");
    }

    // 城市类
    private static class City {
        private final String name;
        private final double latitude;
        private final double longitude;

        public City(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}