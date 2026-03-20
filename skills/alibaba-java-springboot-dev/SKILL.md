---
name: alibaba-java-springboot-dev
description: 基于阿里巴巴 Java 编码规范的 Spring Boot 项目开发技能。用于创建符合阿里规范的 Spring Boot 项目结构、生成标准代码、提供代码审查指导。使用场景：(1) 初始化新的 Spring Boot 项目，(2) 生成符合规范的实体类/Service/Controller 等代码，(3) 代码审查和规范检查，(4) 解答编码规范相关问题。
---

# Alibaba Java Spring Boot Dev

## Overview

本技能提供基于阿里巴巴 Java 编码规范的 Spring Boot 项目开发指导，包括项目结构搭建、标准代码生成、代码审查规范等功能。

## 核心能力

### 1. Spring Boot 项目初始化

当用户需要创建新的 Spring Boot 项目时，按照以下标准结构组织：

**触发示例**:
- "创建一个 Spring Boot 项目"
- "帮我搭建一个 Spring Boot 项目结构"
- "初始化一个新的 Spring Boot 工程"

**操作流程**:

1. **创建标准目录结构**

参考 `references/spring-boot-templates.md` 中的项目结构说明，创建以下目录：
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

2. **生成基础代码文件**

根据用户需求生成以下标准组件：
- 启动类（带必要注解）
- 统一返回结果封装类 Result<T>
- 业务异常类 BusinessException
- 全局异常处理器 GlobalExceptionHandler

详细代码模板见 `references/spring-boot-templates.md`

### 2. 符合规范的代码生成

当用户需要生成 Java 代码时，严格遵循阿里巴巴 Java 编码规范：

**触发示例**:
- "创建一个 User 实体类"
- "生成用户管理的 Service 和 Controller"
- "帮我写一个查询用户的方法"

**命名规范要求**:

- **类名**: UpperCamelCase 风格 (如：UserService, UserController)
- **方法名/变量名**: lowerCamelCase 风格 (如：getUserById, userName)
- **常量名**: 全部大写，单词间用下划线隔开 (如：MAX_RETRY_COUNT)
- **包名**: 全小写，点分隔符之间有且仅有一个自然语义的英语单词

**注释规范要求**:

- **文档注释**: 使用 Javadoc 格式，中文编写，包含@author、@version、@since
- **方法注释**: 必须包含@param、@return、@throws 标签
- **行内注释**: 简短注释使用 `// 注释内容`，位于代码上方

**代码示例**:

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

详细规范见 `references/alibaba-java-coding-guidelines.md`

### 3. 代码审查与规范检查

当用户请求代码审查或规范检查时，按照以下维度进行检查：

**触发示例**:
- "检查这段代码是否符合规范"
- "帮我 review 一下这个类"
- "代码有什么需要改进的地方吗"

**检查清单**:

#### 命名规范检查
- [ ] 类名是否使用 UpperCamelCase
- [ ] 方法名/变量名是否使用 lowerCamelCase
- [ ] 常量名是否全部大写并用下划线分隔
- [ ] 包名是否全小写且语义清晰
- [ ] 是否使用了中文拼音组合

#### 注释规范检查
- [ ] 类是否有 Javadoc 注释
- [ ] 方法是否有完整的@param、@return 注释
- [ ] 注释是否使用中文
- [ ] 注释是否单独成行位于代码上方

#### 代码格式检查
- [ ] 是否使用 4 个空格缩进
- [ ] 运算符之间是否有空格
- [ ] 方法之间是否空一行
- [ ] 不同逻辑块之间是否空一行

#### Spring Boot 规范检查
- [ ] Controller 是否使用@RestController 和@RequestMapping
- [ ] Service 是否使用@Service 和@Transactional
- [ ] 是否使用统一返回结果 Result<T>
- [ ] 异常处理是否使用全局异常处理器
- [ ] 配置文件是否按环境拆分

#### 最佳实践检查
- [ ] 是否避免在 foreach 循环中 remove/add 元素
- [ ] 线程池是否通过 ThreadPoolExecutor 创建
- [ ] 是否正确释放 lock 资源
- [ ] 是否捕获并处理了所有异常

### 4. 规范咨询与答疑

当用户询问编码规范相关问题时，提供准确的规范说明：

**触发示例**:
- "Spring Boot 项目应该怎么组织目录结构"
- "Java 注释应该怎么写才符合规范"
- "事务应该控制在哪个层次"
- "如何正确处理异常"

**回答原则**:

1. **引用规范原文**: 优先引用 `references/alibaba-java-coding-guidelines.md` 中的具体条款
2. **提供代码示例**: 给出符合规范的正确示例和不符合规范的反面示例
3. **解释原因**: 说明规范背后的设计考虑和最佳实践

## 工作流程决策树

根据用户请求类型选择相应的工作流程：

```
用户请求
├── 创建新项目 → 项目初始化流程
│   ├── 创建标准目录结构
│   ├── 生成基础代码文件
│   └── 配置项目依赖
│
├── 生成代码 → 代码生成流程
│   ├── 确定代码类型 (Entity/DTO/VO/Service/Controller)
│   ├── 遵循命名规范
│   ├── 添加完整注释
│   └── 应用设计模式
│
├── 代码审查 → 规范检查流程
│   ├── 命名规范检查
│   ├── 注释规范检查
│   ├── 代码格式检查
│   └── 最佳实践检查
│
└── 规范咨询 → 答疑流程
    ├── 引用规范条款
    ├── 提供代码示例
    └── 解释设计原因
```

## Resources

本技能包含以下参考资源：

### references/

- **alibaba-java-coding-guidelines.md**: 阿里巴巴 Java 编码规范完整指南
  - 命名风格规范
  - 注释规范
  - 代码格式规范
  - OOP 规约
  - 并发处理规范
  - Spring Boot 规范
  - 异常处理规范
  - 单元测试规范

- **spring-boot-templates.md**: Spring Boot 项目模板和代码示例
  - 项目启动类模板
  - 统一返回结果封装
  - 全局异常处理器
  - Entity/DTO/VO 模板
  - Service 接口与实现模板
  - Controller 模板
  - Repository 模板
  - 配置文件模板

## 使用示例

### 示例 1: 创建 Spring Boot 项目

**用户**: "帮我创建一个用户管理系统的 Spring Boot 项目"

**响应**:
1. 创建标准目录结构
2. 生成 ProjectApplication.java 启动类
3. 生成 Result<T>统一返回类
4. 生成 BusinessException 业务异常类
5. 生成 GlobalExceptionHandler 全局异常处理器
6. 生成 application.yml 配置文件

### 示例 2: 生成实体类和服务层代码

**用户**: "创建一个 User 实体类和对应的 Service"

**响应**:
1. 生成 User 实体类（包含 Javadoc 注释、MyBatis Plus 注解）
2. 生成 UserRepository 数据访问接口
3. 生成 UserService 服务接口
4. 生成 UserServiceImpl 服务实现类（包含@Transactional 注解）

### 示例 3: 代码审查

**用户**: "帮我检查这个 Controller 是否符合规范"

**响应**:
按照检查清单逐项检查：
- 命名是否规范
- 注释是否完整
- 是否使用统一返回结果
- 异常处理是否恰当
- 参数校验是否完善

提供具体的改进建议和修改后的代码

### 示例 4: 规范咨询

**用户**: "Spring Boot 项目中事务应该加在哪一层？"

**响应**:
- 引用规范：事务应控制在 Service 层
- 解释原因：保证业务操作的原子性，Controller 层只负责调用
- 提供示例：展示@Service + @Transactional 的正确用法
