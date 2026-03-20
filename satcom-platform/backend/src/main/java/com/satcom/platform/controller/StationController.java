package com.satcom.platform.controller;

import com.satcom.platform.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/station")
public class StationController {

    @GetMapping("/monitoring")
    public ApiResponse<Map<String, Object>> getStationMonitoring() {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> station = new HashMap<>();
        station.put("deviceName", "凯睿星通-KeyCsatPS035-TDA200-250250");
        station.put("model", "KRXT-PS035-TDA200-250250");
        station.put("online", true);
        station.put("beam", "9b（西安）");
        station.put("networkType", "ORCA");
        station.put("longitude", 119.549);
        station.put("latitude", 25.705);
        station.put("masterWeather", "小雨");
        station.put("masterRain", "降雨量: 5mm");
        station.put("slaveWeather", "晴");
        station.put("slaveCloud", "少云");
        station.put("localTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        station.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        Map<String, Object> realtime = new HashMap<>();
        realtime.put("uploadSpeed", String.format("%.2f", Math.random() * 10));
        realtime.put("downloadSpeed", String.format("%.2f", Math.random() * 10));
        realtime.put("totalRate", String.format("%.2f", Math.random() * 20));
        realtime.put("uploadTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        realtime.put("downloadTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        realtime.put("delay", String.format("%.2f", 3 + Math.random() * 10));
        realtime.put("packetLoss", String.format("%.2f", Math.random() * 2));
        realtime.put("jitter", String.format("%.2f", Math.random() * 2));
        realtime.put("jitterTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        realtime.put("cnTx", String.format("%.2f", 2 + Math.random() * 5));
        realtime.put("cnRx", String.format("%.2f", 2 + Math.random() * 5));
        
        result.put("station", station);
        result.put("realtime", realtime);
        
        return ApiResponse.ok(result);
    }

    @GetMapping("/history")
    public ApiResponse<List<Map<String, Object>>> getHistoryData(
            @RequestParam(required = false) String metric,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false, defaultValue = "all") String granularity) {
        
        List<Map<String, Object>> data = new ArrayList<>();
        
        int points = 54;
        if ("2h".equals(granularity)) {
            points = 60;
        } else if ("1d".equals(granularity)) {
            points = 96;
        } else if ("1w".equals(granularity)) {
            points = 168;
        }
        
        LocalDateTime baseTime = LocalDateTime.now();
        for (int i = points - 1; i >= 0; i--) {
            Map<String, Object> point = new HashMap<>();
            LocalDateTime time = baseTime.minusMinutes(i);
            point.put("time", time.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            point.put("upload", String.format("%.2f", Math.random() * 10));
            point.put("download", String.format("%.2f", Math.random() * 10));
            point.put("total", String.format("%.2f", Math.random() * 20));
            data.add(point);
        }
        
        return ApiResponse.ok(data);
    }
}
