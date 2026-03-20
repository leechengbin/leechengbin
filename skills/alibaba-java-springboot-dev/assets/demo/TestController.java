package com.company.project.controller;

import com.company.project.common.result.Result;
import com.company.project.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 * <p>提供简单的测试接口用于验证系统功能</p>
 *
 * @author zhangsan
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    /**
     * 健康检查接口
     * GET /api/test/health
     *
     * @return 健康检查结果
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("System is running");
    }

    /**
     * 获取当前时间
     * GET /api/test/time
     *
     * @return 当前时间信息
     */
    @GetMapping("/time")
    public Result<String> getTime() {
        return Result.success(testService.getCurrentTime());
    }

    /**
     * 简单的问候接口
     * GET /api/test/hello
     *
     * @param name 姓名，可选参数，默认为"World"
     * @return 问候语
     */
    @GetMapping("/hello")
    public Result<String> hello(String name) {
        return Result.success(testService.sayHello(name));
    }
}
