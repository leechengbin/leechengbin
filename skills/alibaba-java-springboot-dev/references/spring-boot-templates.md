# Spring Boot 项目模板和代码示例

## 一、项目启动类

```java
package com.company.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 项目启动类
 * <p>配置说明：</p>
 * <ul>
 *     <li>@SpringBootApplication: Spring Boot 核心注解</li>
 *     <li>@EnableAsync: 启用异步任务支持</li>
 *     <li>@EnableScheduling: 启用定时任务支持</li>
 * </ul>
 *
 * @author zhangsan
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectApplication.class, args);
    }
}
```

## 二、统一返回结果封装

```java
package com.company.project.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一 API 响应结果封装
 *
 * @param <T> 数据类型
 * @author zhangsan
 * @version 1.0.0
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应
     *
     * @param data 数据
     * @return 响应结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 失败响应
     *
     * @param message 错误消息
     * @return 响应结果
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
}
```

## 三、业务异常类

```java
package com.company.project.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author zhangsan
 * @version 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 构造方法
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造方法
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}
```

## 四、全局异常处理器

```java
package com.company.project.common.exception;

import lombok.extern.slf4j.Slf4j;
import com.company.project.common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author zhangsan
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    /**
     * 处理系统异常
     *
     * @param e 运行时异常
     * @return 响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return Result.error("系统繁忙，请稍后再试");
    }
}
```

## 五、实体类示例

```java
package com.company.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author zhangsan
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@TableName("t_user")
public class User {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
```

## 六、数据传输对象 DTO

```java
package com.company.project.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户数据传输对象
 *
 * @author zhangsan
 * @version 1.0.0
 */
@Data
public class UserDTO {

    /**
     * 用户 ID（更新时使用）
     */
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
```

## 七、视图对象 VO

```java
package com.company.project.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户视图对象
 *
 * @author zhangsan
 * @version 1.0.0
 */
@Data
public class UserVO {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
```

## 八、Service 接口与实现

### Service 接口

```java
package com.company.project.service;

import com.company.project.dto.UserDTO;
import com.company.project.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author zhangsan
 * @version 1.0.0
 */
public interface UserService {

    /**
     * 根据 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    UserVO getUserById(Long id);

    /**
     * 创建用户
     *
     * @param userDTO 用户信息
     * @return 用户 ID
     */
    Long createUser(UserDTO userDTO);

    /**
     * 更新用户
     *
     * @param userDTO 用户信息
     */
    void updateUser(UserDTO userDTO);

    /**
     * 删除用户
     *
     * @param id 用户 ID
     */
    void deleteUser(Long id);
}
```

### Service 实现

```java
package com.company.project.service.impl;

import com.company.project.dto.UserDTO;
import com.company.project.entity.User;
import com.company.project.exception.UserNotFoundException;
import com.company.project.repository.UserRepository;
import com.company.project.service.UserService;
import com.company.project.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 *
 * @author zhangsan
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserVO getUserById(Long id) {
        log.info("查询用户信息，id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("用户不存在，id=" + id));
        
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserDTO userDTO) {
        log.info("创建用户，username={}", userDTO.getUsername());
        
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        User savedUser = userRepository.save(user);
        log.info("用户创建成功，id={}", savedUser.getId());
        
        return savedUser.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserDTO userDTO) {
        log.info("更新用户，id={}", userDTO.getId());
        
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new UserNotFoundException("用户不存在，id=" + userDTO.getId()));
        
        BeanUtils.copyProperties(userDTO, user, "id");
        userRepository.save(user);
        
        log.info("用户更新成功，id={}", userDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        log.info("删除用户，id={}", id);
        
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("用户不存在，id=" + id);
        }
        
        userRepository.deleteById(id);
        log.info("用户删除成功，id={}", id);
    }
}
```

## 九、Controller 示例

```java
package com.company.project.controller;

import com.company.project.dto.UserDTO;
import com.company.project.result.Result;
import com.company.project.service.UserService;
import com.company.project.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author zhangsan
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据 ID 获取用户信息
     * GET /api/users/{id}
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    /**
     * 创建新用户
     * POST /api/users
     *
     * @param userDTO 用户信息
     * @return 用户 ID
     */
    @PostMapping
    public Result<Long> createUser(@RequestBody @Validated UserDTO userDTO) {
        return Result.success(userService.createUser(userDTO));
    }

    /**
     * 更新用户信息
     * PUT /api/users
     *
     * @param userDTO 用户信息
     * @return 操作结果
     */
    @PutMapping
    public Result<Void> updateUser(@RequestBody @Validated UserDTO userDTO) {
        userService.updateUser(userDTO);
        return Result.success(null);
    }

    /**
     * 删除用户
     * DELETE /api/users/{id}
     *
     * @param id 用户 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success(null);
    }
}
```

## 十、Repository 示例

```java
package com.company.project.repository;

import com.company.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 *
 * @author zhangsan
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    Optional<User> findByPhone(String phone);
}
```

## 十一、配置文件示例

### application.yml

```yaml
spring:
  application:
    name: project-name
  
  profiles:
    active: dev
  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/project_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
  
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.company.project.entity
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false

logging:
  level:
    root: INFO
    com.company.project: DEBUG
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n'

server:
  port: 8080
  servlet:
    context-path: /
```

### application-dev.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/project_dev?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
  
logging:
  level:
    com.company.project: DEBUG
```

### application-prod.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://prod-db-host:3306/project_prod?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
  
logging:
  level:
    root: WARN
    com.company.project: INFO
  
server:
  port: 80
```
