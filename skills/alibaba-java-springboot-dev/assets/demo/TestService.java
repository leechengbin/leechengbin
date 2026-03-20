package com.company.project.service;

/**
 * 测试服务接口
 *
 * @author zhangsan
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface TestService {

    /**
     * 获取当前时间
     *
     * @return 格式化后的当前时间字符串
     */
    String getCurrentTime();

    /**
     * 生成问候语
     *
     * @param name 姓名
     * @return 问候语
     */
    String sayHello(String name);
}
