下面给你一个**适用于大型项目的 `SKILL.md` 模板**，基于你给出的技术栈，并结合**阿里巴巴 Java 开发规范 + 大型系统架构实践**，同时包含：

- 后端技术规范
- 前端规范
- 消息队列处理
- TCP / UDP 网络服务框架
- 项目结构规范
- 开发流程规范

你可以直接作为 **项目仓库的 `SKILL.md` 或 `ARCHITECTURE.md` 使用**。

------

# SKILL.md

大型系统开发规范与技术栈说明

------

# 一、技术栈总览

## 1. 后端技术栈

| 技术             | 说明                |
| ---------------- | ------------------- |
| Java 21          | 核心开发语言        |
| Spring Boot 3.x  | 微服务/单体应用框架 |
| MyBatis-Plus     | ORM框架             |
| MySQL 8+         | 主数据库            |
| Redis            | 缓存                |
| RabbitMQ / Kafka | 消息队列            |
| Netty            | TCP/UDP网络通信     |
| Nginx            | 网关与负载均衡      |
| Docker           | 容器化部署          |

------

## 2. 前端技术栈

| 技术         | 说明     |
| ------------ | -------- |
| Vue 3        | Web框架  |
| TypeScript   | 类型安全 |
| Element Plus | UI组件   |
| Vite         | 构建工具 |
| Pinia        | 状态管理 |
| Axios        | HTTP请求 |

------

# 二、后端开发规范

遵循：

**阿里巴巴Java开发手册**

核心规范：

### 1 命名规范

| 类型   | 规范       |
| ------ | ---------- |
| 类名   | PascalCase |
| 方法名 | camelCase  |
| 常量   | UPPER_CASE |
| 包名   | 全小写     |

示例：

```
com.company.project.module.service
```

------

### 2 分层架构

严格分层：

```
Controller
   ↓
Service
   ↓
 Mapper
   ↓
Database
```

禁止：

```
Controller → Mapper
```

------

### 3 DTO / VO / Entity

必须分离

```
entity
dto
vo
query
```

示例：

```
UserEntity
UserDTO
UserVO
UserQuery
```

------

### 4 统一返回结构

```
Result<T>
```

示例：

```java
public class Result<T> {

    private int code;
    private String message;
    private T data;

}
```

------

# 三、项目结构规范

大型项目推荐结构

```
project
│
├─ backend
│
│  ├─ common
│  │   ├─ config
│  │   ├─ constant
│  │   ├─ exception
│  │   ├─ utils
│  │
│  ├─ modules
│  │   ├─ user
│  │   │   ├─ controller
│  │   │   ├─ service
│  │   │   ├─ serviceImpl
│  │   │   ├─ mapper
│  │   │   ├─ entity
│  │   │   ├─ dto
│  │   │   ├─ vo
│  │   │
│  │   ├─ order
│
│  ├─ mq
│  │   ├─ producer
│  │   ├─ consumer
│
│  ├─ network
│  │   ├─ tcp
│  │   ├─ udp
│
│
├─ frontend
│
│  ├─ src
│  │   ├─ api
│  │   ├─ components
│  │   ├─ views
│  │   ├─ router
│  │   ├─ store
│  │   ├─ utils
```

------

# 四、MyBatis-Plus规范

Mapper接口：

```java
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

}
```

Service：

```java
public interface UserService {

    UserVO getUser(Long id);

}
```

ServiceImpl：

```java
@Service
public class UserServiceImpl implements UserService {

}
```

------

# 五、消息队列架构

推荐：

| 场景     | 技术           |
| -------- | -------------- |
| 异步任务 | RabbitMQ       |
| 大数据流 | Kafka          |
| 延迟任务 | RabbitMQ Delay |
| 日志     | Kafka          |

------

## RabbitMQ结构

```
Producer
   ↓
Exchange
   ↓
Queue
   ↓
Consumer
```

------

## 生产者示例

```java
@Component
public class MessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String msg) {
        rabbitTemplate.convertAndSend("exchange", "routingKey", msg);
    }
}
```

------

## 消费者示例

```java
@RabbitListener(queues = "queueName")
public void receive(String msg){
    System.out.println(msg);
}
```

------

# 六、TCP / UDP 网络服务框架

推荐使用：

```
Netty
```

原因：

- 高性能
- 支持TCP
- 支持UDP
- 高并发
- 异步

------

# TCP服务器框架

```
NettyServer
   │
   ├── ChannelInitializer
   │
   ├── Decoder
   ├── Encoder
   │
   └── Handler
```

------

示例：

```java
public class TcpServer {

    public void start() throws Exception {

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new TcpInitializer());

        bootstrap.bind(9000).sync();
    }
}
```

------

# UDP服务器框架

```
NettyUDPServer
```

示例：

```java
public class UdpServer {

    public void start() throws Exception {

        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(new NioEventLoopGroup())
                .channel(NioDatagramChannel.class)
                .handler(new UdpHandler());

        bootstrap.bind(9001).sync();
    }
}
```

------

# 七、缓存规范

推荐：

```
Redis
```

缓存层：

```
Service
   ↓
Cache
   ↓
Database
```

避免：

```
Cache穿透
Cache雪崩
Cache击穿
```

解决方案：

- BloomFilter
- 随机TTL
- 分布式锁

------

# 八、日志规范

日志框架：

```
logback
```

日志级别：

| 级别  | 用途     |
| ----- | -------- |
| ERROR | 系统异常 |
| WARN  | 潜在问题 |
| INFO  | 业务日志 |
| DEBUG | 调试     |

禁止：

```
System.out.println
```

------

# 九、前端开发规范

项目结构：

```
src
│
├─ api
├─ assets
├─ components
├─ layout
├─ router
├─ store
├─ utils
├─ views
```

------

## API封装

```ts
import request from '@/utils/request'

export function getUser(id:number){
    return request.get(`/user/${id}`)
}
```

------

## 状态管理

使用：

```
Pinia
```

示例：

```ts
export const useUserStore = defineStore('user',{
  state:()=>({
    user:null
  })
})
```

------

# 十、安全规范

必须实现：

- JWT认证
- RBAC权限
- SQL注入防护
- XSS防护
- CSRF防护

------

# 十一、部署架构

推荐架构：

```
用户
 ↓
Nginx
 ↓
SpringBoot
 ↓
Redis
 ↓
MySQL
```

消息架构：

```
Service
   ↓
RabbitMQ
   ↓
Consumer
```

------

# 十二、CI/CD

推荐：

```
Gitlab CI
Jenkins
Docker
K8S
```

流程：

```
Git Push
   ↓
CI Build
   ↓
Docker Image
   ↓
Deploy
```

------

# 十三、代码质量

工具：

```
SonarQube
CheckStyle
SpotBugs
```

代码覆盖率：

```
> 80%
```

------

# 十四、文档规范

所有接口必须提供：

```
Swagger / OpenAPI
```

文档：

```
/docs
```
