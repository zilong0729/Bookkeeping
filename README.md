# 微信小程序个人记账系统（Java后端）

## 项目简介

本项目是为微信小程序端个人用户提供人情往来、收支记录记账服务的Java后端服务。参考"小李记"小程序的UI与业务逻辑，提供完整的记账功能。

## 技术栈

- JDK 17
- Spring Boot 3.2.x
- MySQL 8.x
- Redis 6.x
- MyBatis-Plus 3.5.x
- Knife4j 4.x（Swagger增强版）
- JWT（Token认证）

## 项目结构

```
bookkeeping/
├── pom.xml                          # Maven配置文件
├── README.md                        # 项目说明文档
├── sql/
│   └── init.sql                     # 数据库初始化脚本
└── src/main/java/com/bookkeeping/
    ├── BookkeepingApplication.java  # 启动类
    ├── common/                      # 通用类
    │   ├── Result.java              # 统一返回结果
    │   └── PageResult.java          # 分页返回结果
    ├── config/                      # 配置类
    │   ├── Knife4jConfig.java       # Knife4j配置
    │   ├── MyBatisPlusConfig.java   # MyBatis-Plus配置
    │   ├── RedisConfig.java         # Redis配置
    │   ├── WebMvcConfig.java        # Web MVC配置
    │   └── WxMiniAppConfig.java     # 微信小程序配置
    ├── controller/                  # 控制器层
    │   ├── UserController.java      # 用户管理接口
    │   ├── CategoryController.java  # 类别管理接口
    │   └── RecordController.java    # 账单管理接口
    ├── dto/                         # 数据传输对象
    │   ├── WxLoginDTO.java          # 微信登录请求
    │   ├── UpdateUserDTO.java       # 更新用户信息请求
    │   ├── CategoryDTO.java         # 类别请求
    │   ├── RecordDTO.java           # 账单请求
    │   └── RecordQueryDTO.java      # 账单查询请求
    ├── entity/                      # 实体类
    │   ├── User.java                # 用户实体
    │   ├── Category.java            # 类别实体
    │   └── Record.java              # 账单实体
    ├── exception/                   # 异常处理
    │   ├── BusinessException.java   # 业务异常
    │   └── GlobalExceptionHandler.java # 全局异常处理器
    ├── interceptor/                 # 拦截器
    │   └── AuthInterceptor.java     # 认证拦截器
    ├── mapper/                      # Mapper接口
    │   ├── UserMapper.java
    │   ├── CategoryMapper.java
    │   └── RecordMapper.java
    ├── service/                     # 服务层
    │   ├── UserService.java
    │   ├── CategoryService.java
    │   ├── RecordService.java
    │   └── impl/                    # 服务实现
    │       ├── UserServiceImpl.java
    │       ├── CategoryServiceImpl.java
    │       └── RecordServiceImpl.java
    ├── utils/                       # 工具类
    │   ├── JwtUtil.java             # JWT工具
    │   ├── RedisUtil.java           # Redis工具
    │   └── UserContext.java         # 用户上下文
    └── vo/                          # 视图对象
        ├── LoginVO.java             # 登录返回
        ├── UserVO.java              # 用户信息
        ├── WxSessionVO.java         # 微信Session
        ├── CategoryVO.java          # 类别信息
        ├── RecordVO.java            # 账单信息
        └── StatisticsVO.java        # 统计信息
```

## 快速启动

### 1. 环境准备

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 2. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source sql/init.sql
```

### 3. 配置文件修改

编辑 `src/main/resources/application.yml`：

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bookkeeping?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password

  redis:
    host: localhost
    port: 6379
    password: your_password  # 如果没有密码留空

# 微信小程序配置（需要替换为实际的appId和appSecret）
wx:
  miniapp:
    app-id: your-wx-app-id
    app-secret: your-wx-app-secret

# JWT密钥（建议修改为随机生成的复杂字符串）
jwt:
  secret: your-secret-key-here-must-be-at-least-256-bits-long-for-security
```

### 4. 编译运行

```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run

# 或者打包后运行
mvn clean package -DskipTests
java -jar target/bookkeeping-server-1.0.0.jar
```

### 5. 访问接口文档

启动成功后，访问 Knife4j 接口文档：

```
http://localhost:8080/api/doc.html
```

## 接口调用示例

### 1. 用户登录

```bash
# 微信小程序登录
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "code": "wx_login_code_from_miniapp",
    "nickname": "张三",
    "avatarUrl": "https://example.com/avatar.jpg"
  }'

# 响应示例
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 604800,
    "userInfo": {
      "id": 123456789,
      "nickname": "张三",
      "avatarUrl": "https://example.com/avatar.jpg",
      "phone": null,
      "createTime": "2024-01-15T10:30:00"
    }
  }
}
```

### 2. 获取用户信息

```bash
curl -X GET http://localhost:8080/api/user/info \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

### 3. 获取类别列表

```bash
# 获取所有类别
curl -X GET http://localhost:8080/api/category/list \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."

# 按类型筛选（1-收入，2-支出）
curl -X GET "http://localhost:8080/api/category/list?type=1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

### 4. 创建账单

```bash
curl -X POST http://localhost:8080/api/record \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "categoryId": 1,
    "type": 1,
    "amount": 500.00,
    "contactName": "李四",
    "recordDate": "2024-01-15",
    "remark": "婚宴礼金"
  }'
```

### 5. 查询账单列表

```bash
# 基础查询
curl -X GET "http://localhost:8080/api/record/list?current=1&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."

# 带筛选条件
curl -X GET "http://localhost:8080/api/record/list?type=1&startDate=2024-01-01&endDate=2024-01-31&current=1&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

### 6. 统计账单

```bash
# 统计指定日期范围
curl -X GET "http://localhost:8080/api/record/statistics?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."

# 响应示例
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalIncome": 3500.00,
    "totalExpense": 1200.00,
    "balance": 2300.00
  }
}
```

### 7. 更新账单

```bash
curl -X PUT http://localhost:8080/api/record/123456789 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "categoryId": 1,
    "type": 1,
    "amount": 600.00,
    "contactName": "李四",
    "recordDate": "2024-01-15",
    "remark": "婚宴礼金（更新）"
  }'
```

### 8. 删除账单

```bash
curl -X DELETE http://localhost:8080/api/record/123456789 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

### 9. 用户登出

```bash
curl -X POST http://localhost:8080/api/user/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

## 核心功能说明

### 1. 用户体系

- 微信小程序授权登录（code换取openid）
- JWT Token认证机制
- Token存储于Redis，支持后端强制下线
- 用户数据严格隔离

### 2. 账单管理

- 支持收入/支出两种类型
- 预设类别：婚宴、乔迁、添丁、春节红包、生日、升学等
- 支持自定义类别
- 账单字段：金额、往来对象、日期、备注

### 3. 统计功能

- 按日期范围统计收入/支出总额
- 默认统计当月数据
- 支持自定义日期筛选

### 4. 数据安全

- 所有接口需携带有效Token
- 用户只能操作自己的数据
- 逻辑删除保护数据

## 微信小程序对接说明

### 登录流程

1. 小程序前端调用 `wx.login()` 获取 `code`
2. 将 `code` 发送到后端 `/api/user/login` 接口
3. 后端用 `code` 换取微信 `openid`
4. 后端生成 JWT Token 返回给小程序
5. 小程序存储 Token，后续请求携带在 Header 中

### 小程序请求示例

```javascript
// 登录
wx.login({
  success: (res) => {
    wx.request({
      url: 'http://your-server/api/user/login',
      method: 'POST',
      data: {
        code: res.code,
        nickname: '用户昵称',
        avatarUrl: '头像URL'
      },
      success: (response) => {
        const { token } = response.data.data;
        wx.setStorageSync('token', token);
      }
    });
  }
});

// 携带Token请求
wx.request({
  url: 'http://your-server/api/record/list',
  header: {
    'Authorization': 'Bearer ' + wx.getStorageSync('token')
  },
  success: (res) => {
    console.log(res.data);
  }
});
```

## 注意事项

1. **微信小程序配置**：需要在 `application.yml` 中配置正确的 `app-id` 和 `app-secret`
2. **JWT密钥**：生产环境请使用随机生成的复杂字符串作为密钥
3. **数据库连接**：根据实际情况修改数据库连接配置
4. **Redis配置**：如果使用密码，需要在配置中指定
5. **跨域问题**：小程序对接时需要配置CORS或使用Nginx反向代理

## 许可证

Apache License 2.0
