# 阿里巴巴 Java 编码规范

## 一、命名风格

### 1.1 通用命名规则
- 【强制】代码中的命名均不能以下划线或美元符号开始，也不能以下划线或美元符号结束
- 【强制】代码中的命名严禁使用任何中文拼音组合
- 【推荐】方法名、参数名、成员变量、局部变量都统一使用 lowerCamelCase 风格
- 【推荐】常量命名全部大写，单词间用下划线隔开

### 1.2 类名命名
- 【强制】类名使用 UpperCamelCase 风格
- 【强制】抽象类命名使用 Abstract 或 Base 开头
- 【强制】异常类命名以 Exception 结尾
- 【强制】测试类命名以它要测试的类的名称开始，以 Test 结尾

### 1.3 包名命名
- 【强制】包名统一使用小写，点分隔符之间有且仅有一个自然语义的英语单词
- 【推荐】Spring Boot 项目包名结构：`com.{公司}.{项目}.{模块}`

## 二、注释规范

### 2.1 文档注释
- 【强制】类、方法必须使用 Javadoc 格式注释
- 【强制】文档注释使用中文编写
- 【强制】注释应该单独成行，位于被注释代码上方
- 【推荐】方法注释应包含@param、@return、@throws 等标签

示例：
```java
/**
 * 用户服务接口实现类
 * <p>提供用户相关的业务逻辑处理功能</p>
 *
 * @author zhangsan
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class UserServiceImpl implements UserService {
    
    /**
     * 根据 ID 查询用户信息
     *
     * @param userId 用户 ID，不能为空
     * @return 用户信息对象
     * @throws UserNotFoundException 当用户不存在时抛出
     */
    @Override
    public User getUserById(Long userId) {
        // 实现代码
    }
}
```

### 2.2 行内注释
- 【推荐】简短注释使用 `// 注释内容` 格式，位于代码上方
- 【推荐】多段注释使用 `/* ` 开头和 ` */` 结尾
- 【强制】注释与代码对齐，保持整洁

示例：
```java
// 计算订单总金额
BigDecimal totalAmount = calculateTotalAmount(order);

/*
 * 批量处理订单数据
 * 1. 验证订单状态
 * 2. 更新库存数量
 * 3. 发送通知消息
 */
processOrders(orderList);
```

## 三、代码格式

### 3.1 缩进和对齐
- 【强制】采用 4 个空格缩进，禁止使用 Tab 字符
- 【强制】运算符之间必须留有空格
- 【推荐】方法内的变量声明尽量前置

### 3.2 空行规则
- 【推荐】类的方法之间空一行
- 【推荐】方法内部不同逻辑块之间空一行
- 【推荐】注释与被注释代码之间不空行

## 四、OOP 规约

### 4.1 类设计
- 【强制】所有的覆写方法，必须加@Override 注解
- 【强制】外部正在调用或者二方库依赖的接口，不允许修改方法签名
- 【推荐】类的属性顺序：static final → static → 实例变量 → 构造方法 → 公有方法 → 受保护方法 → 私有方法

### 4.2 集合处理
- 【强制】关于 hashCode 和 equals 的处理，遵循 OOP 规约
- 【强制】不要在 foreach 循环里进行元素的 remove/add 操作
- 【推荐】使用集合转数组的方法，必须使用集合的 toArray(T[] array)

## 五、并发处理

### 5.1 线程池
- 【强制】线程池不允许使用 Executors 去创建，推荐通过 ThreadPoolExecutor 方式
- 【强制】SimpleDateFormat 是线程不安全的类，不要定义为 static 变量

### 5.2 锁机制
- 【推荐】对多个资源、数据库表、对象同时加锁时，需要保持一致的获取顺序
- 【强制】必须在 finally 代码块中释放 lock 资源

## 六、Spring Boot 规范

### 6.1 项目结构
```
project-name/
├── src/main/java/com/company/project/
│   ├── ProjectApplication.java      # 启动类
│   ├── controller/                   # 控制器层
│   ├── service/                      # 服务层
│   │   └── impl/                     # 服务实现
│   ├── repository/                   # 数据访问层
│   ├── entity/                       # 实体类
│   ├── dto/                          # 数据传输对象
│   ├── vo/                           # 视图对象
│   ├── config/                       # 配置类
│   ├── common/                       # 公共类
│   │   ├── exception/                # 异常处理
│   │   ├── result/                   # 统一返回结果
│   │   └── util/                     # 工具类
│   └── aspect/                       # 切面类
├── src/main/resources/
│   ├── application.yml               # 配置文件
│   ├── application-dev.yml           # 开发环境配置
│   ├── application-prod.yml          # 生产环境配置
│   └── mapper/                       # MyBatis 映射文件
└── src/test/java/                    # 测试代码
```

### 6.2 Controller 规范
- 【强制】RESTful API 使用标准 HTTP 方法（GET/POST/PUT/DELETE）
- 【强制】返回统一的结果封装类 Result<T>
- 【推荐】路径变量使用驼峰命名

示例：
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
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
     * @return 创建的用户 ID
     */
    @PostMapping
    public Result<Long> createUser(@RequestBody @Validated UserDTO userDTO) {
        return Result.success(userService.createUser(userDTO));
    }
}
```

### 6.3 Service 规范
- 【强制】服务接口与实现分离
- 【强制】事务控制在 Service 层
- 【推荐】使用@Transactional 注解管理事务

### 6.4 配置规范
- 【推荐】使用 YAML 格式配置文件
- 【强制】敏感信息必须加密或使用环境变量
- 【推荐】按环境拆分配置文件（dev/test/prod）

## 七、异常处理

### 7.1 异常分类
- 【强制】业务异常使用自定义 BusinessException
- 【强制】系统异常使用 RuntimeException
- 【推荐】定义统一的异常枚举类

### 7.2 异常处理
- 【强制】捕获异常后必须处理，禁止吞掉异常
- 【强制】日志记录必须包含异常堆栈信息
- 【推荐】使用全局异常处理器@ControllerAdvice

## 八、单元测试

### 8.1 测试规范
- 【强制】好的单元测试必须遵守 AIR 原则
  - A: Automatic（自动化）
  - I: Independent（独立）
  - R: Repeatable（可重复）
- 【推荐】单元测试覆盖率不低于 80%
- 【推荐】使用 JUnit 5 + Mockito 框架

### 8.2 测试用例
- 【强制】测试类名 = 被测试类名 + Test
- 【强制】测试方法名 = test+ 被测方法名 + 特性描述
- 【推荐】每个测试用例只测试一个功能点
