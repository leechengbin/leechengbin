package com.company.project.service.impl;

import com.company.project.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 测试服务实现类
 * <p>提供简单的测试业务逻辑</p>
 *
 * @author zhangsan
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class TestServiceImpl implements TestService {

    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String getCurrentTime() {
        log.info("获取当前时间");
        LocalDateTime now = LocalDateTime.now();
        return now.format(FORMATTER);
    }

    @Override
    public String sayHello(String name) {
        // 如果姓名为空，使用默认值
        if (name == null || name.trim().isEmpty()) {
            name = "World";
        }
        
        log.info("生成问候语，name={}", name);
        return "Hello, " + name + "!";
    }
}
