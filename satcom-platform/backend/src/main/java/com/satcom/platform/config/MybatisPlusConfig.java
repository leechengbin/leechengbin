package com.satcom.platform.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.satcom.platform.mapper")
public class MybatisPlusConfig {
}
