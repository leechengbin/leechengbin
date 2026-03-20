# MyBatis-Plus 依赖配置和使用指南

## 一、Maven 依赖配置

在 `pom.xml` 中添加以下依赖：

```xml
<dependencies>
    <!-- MyBatis-Plus Spring Boot Starter -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.3.1</version>
    </dependency>
    
    <!-- MySQL 驱动 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

## 二、Gradle 依赖配置

在 `build.gradle` 中添加：

```groovy
dependencies {
    implementation 'com.baomidou:mybatis-plus-boot-starter:3.5.3.1'
    runtimeOnly 'com.mysql:mysql-connector-java:8.0.33'
}
```

## 三、application.yml 配置

```yaml
mybatis-plus:
  # Mapper XML 文件位置
  mapper-locations: classpath*:/mapper/**/*.xml
  
  # 实体类包路径
  type-aliases-package: com.company.project.entity
  
  # 全局配置
  global-config:
    db-config:
      # 主键类型：ASSIGN_ID(雪花算法)
      id-type: ASSIGN_ID
      
      # 逻辑删除配置
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  
  # 原生 MyBatis 配置
  configuration:
    # 开启驼峰命名转换
    map-underscore-to-camel-case: true
    
    # 关闭二级缓存
    cache-enabled: false
    
    # 开启 SQL 日志
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

## 四、MyBatis-Plus 配置类

```java
package com.company.project.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 *
 * @author zhangsan
 * @version 1.0.0
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 插件拦截器
     *
     * @return 拦截器对象
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        return interceptor;
    }
}
```

## 五、自动填充处理器

```java
package com.company.project.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * <p>自动填充创建时间和更新时间</p>
 *
 * @author zhangsan
 * @version 1.0.0
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

## 六、使用示例

### 6.1 实体类

```java
@Data
@TableName("t_user")
public class User {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    private String username;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
```

### 6.2 Mapper 接口

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承 BaseMapper 后，无需编写 XML 即可获得 CRUD 方法
}
```

### 6.3 Service 层

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    // 使用 LambdaQueryWrapper 构建类型安全的查询
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.getOne(wrapper);
    }
}
```

### 6.4 Controller 层

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }
    
    @PostMapping
    public Result<Long> createUser(@RequestBody User user) {
        return Result.success(userService.createUser(user));
    }
}
```

## 七、常用方法速查

### BaseMapper 提供的方法

| 方法 | 说明 | 示例 |
|------|------|------|
| selectById | 根据 ID 查询 | `userMapper.selectById(1L)` |
| selectList | 查询所有 | `userMapper.selectList(null)` |
| insert | 插入记录 | `userMapper.insert(user)` |
| updateById | 根据 ID 更新 | `userMapper.updateById(user)` |
| deleteById | 根据 ID 删除 | `userMapper.deleteById(1L)` |
| selectCount | 查询总数 | `userMapper.selectCount(null)` |

### IService 提供的方法

| 方法 | 说明 | 示例 |
|------|------|------|
| getById | 根据 ID 查询 | `userService.getById(1L)` |
| list | 查询所有 | `userService.list()` |
| save | 保存 | `userService.save(user)` |
| updateById | 根据 ID 更新 | `userService.updateById(user)` |
| removeById | 根据 ID 删除 | `userService.removeById(1L)` |
| count | 查询总数 | `userService.count()` |
| getOne | 查询一条 | `userService.getOne(queryWrapper)` |
| list | 查询列表 | `userService.list(queryWrapper)` |
| page | 分页查询 | `userService.page(page, queryWrapper)` |

### QueryWrapper 常用方法

```java
// 等于
wrapper.eq(User::getUsername, "zhangsan");

// 不等于
wrapper.ne(User::getStatus, 0);

// 大于
wrapper.gt(User::getAge, 18);

// 小于
wrapper.lt(User::getAge, 60);

// 模糊查询
wrapper.like(User::getUsername, "张");

// 在...之间
wrapper.between(User::getAge, 18, 60);

// 在...之中
wrapper.in(User::getStatus, Arrays.asList(1, 2, 3));

// 排序
wrapper.orderByDesc(User::getCreateTime);
```

## 八、优势总结

使用 MyBatis-Plus 的优势：

1. **减少代码量**：无需编写 Mapper XML 和 DAO 实现类
2. **通用 CRUD**：继承 BaseMapper 和 IService 即可获得完整 CRUD 能力
3. **Lambda 表达式**：使用 LambdaQueryWrapper 避免硬编码字段名
4. **自动填充**：支持创建时间、更新时间等字段自动填充
5. **逻辑删除**：简单的注解配置即可实现逻辑删除
6. **分页插件**：内置分页功能，无需手动编写分页 SQL
7. **主键策略**：支持多种主键生成策略（雪花算法、UUID 等）
