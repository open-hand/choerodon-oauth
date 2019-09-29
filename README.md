# Oauth Server

这个服务是猪齿鱼微服务框架的权限认证中心，它主要保证用户权限与用户认证。

## 特性

- 完整的OAuth2协议实现
- 密码找回功能
- Token 管理功能
- 基于Ldap的用户认证

## 准备

- MySQL - iam-service database
- redis - sessions & cache

`oauth-server` 服务依赖于 [iam-service](https://github.com/choerodon/iam-service) 服务的数据库, 所以请确保 `iam-service` 服务的数据库在使用前已经被初始化并投入使用。

## 配置文件
application.yml
```application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost/iam_service?useUnicode=true&characterEncoding=utf-8&useSSL=false&useInformationSchema=true&remarks=true
    username: choerodon
    password: 123456
  redis:
    host: localhost
    port: 6379
    database: 1
eureka:
  instance:
    preferIpAddress: true
    leaseRenewalIntervalInSeconds: 10
    leaseExpirationDurationInSeconds: 30
    metadata-map:
      CONTEXT-PATH: oauth
  client:
    serviceUrl:
      defaultZone: ${EUREKA_DEFAULT_ZONE:http://localhost:8000/eureka/}
    registryFetchIntervalSeconds: 10
mybatis:
  mapperLocations: classpath*:/mapper/*.xml
  configuration:
    mapUnderscoreToCamelCase: true
feign:
  hystrix:
    enabled: true
hystrix:
  command:
    default:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 10000
ribbon:
  ReadTimeout: 10000
  ConnectTimeout: 10000
choerodon:
  redisHttpSession:
    enabled: true
  oauth:
    clear-token: true
    enabled-single-login: false
    access-token-validity-seconds: 86400
    login:
      field: mail,phone
      path: /login
      ssl: false
    loginPage.title: Choerodon
  reset-password:
    check: true
  default:
    redirect:
      url: http://localhost:8080/manager/swagger-ui.html
hook:
  token: abc
db:
  type: mysql
```
bootstrap.yml
```bootstrap.yml
server:
  port: 8020
  servlet:
    contextPath: /oauth
spring:
  application:
    name: oauth-server
  cloud:
    config:
      uri: http://localhost:8010/
      enabled: false
      fail-fast: true
      retry:
        max-attempts: 6
        max-interval: 2000
        multiplier: 1.1
management:
  endpoint:
    health:
      show-details: ALWAYS
  server:
    port: 8021
  endpoints:
    web:
      exposure:
        include: '*'
```

## 安装和启动步骤

* 数据库：
已经投入使用的猪齿鱼微服务框架中`iam-service`服务的数据库。

* 然后在根目录下运行这个项目：

```sh
mvn spring-boot:run
```

## 使用方法

1. 用户登录认证：
    * 用户使用用户名与密码完成oauth认证。
    * Oauth将会基于用户与已认证的客户端生成一个`access_token`，并将其存放在`tokenStore`中。
2. 用户访问资源服务认证：
    * 用户请求中带有`access_token`。在oauth完成检验后，请求会由网关转发到相应的资源服务
    * 对于给用户的非法请求，返回一个401状态码并且跳转到登录页面等待重新认证。

## 链接

* [更新日志](./CHANGELOG.zh-CN.md)
    
## 如何
欢迎提出想法! 欲知更多信息请关注 [贡献说明](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md)。

