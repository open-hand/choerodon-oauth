# Oauth Server

这个服务是猪齿鱼微服务框架的权限认证中心，它主要保证用户权限与用户认证。

## 特性

- 完整的OAuth2协议实现
- 密码找回功能
- Token 管理功能
- 基于Ldap的用户认证

## 功能介绍

1. 用户登录认证：
    - 用户使用用户名与密码完成oauth认证。
    - Oauth将会基于用户与已认证的客户端生成一个`access_token`，并将其存放在`tokenStore`中。
2. 用户访问资源服务认证：
    - 用户请求中带有`access_token`。在oauth完成检验后，请求会由网关转发到相应的资源服务
    - 对于给用户的非法请求，返回一个401状态码并且跳转到登录页面等待重新认证。

## 服务配置

- `application.yml`

  ```yaml
    spring:
      application:
        name: hzero-oauth
      datasource:
        url: ${SPRING_DATASOURCE_URL:jdbc:mysql://db.hzero.org:3306/hzero_platform?useUnicode=true&characterEncoding=utf-8&useSSL=false}
        username: ${SPRING_DATASOURCE_USERNAME:hzero}
        password: ${SPRING_DATASOURCE_PASSWORD:hzero}
        hikari:
          # 连接池最小空闲连接数
          minimum-idle: ${SPRING_DATASOURCE_MINIMUM_IDLE:20}
          # 连接池允许的最大连接数
          maximum-pool-size: ${SPRING_DATASOURCE_MAXIMUM_POOL_SIZE:200}
          # 等待连接池分配连接的最大时长（毫秒）
          connection-timeout: ${SPRING_DATASOURCE_CONNECTION_TIMEOUT:30000}
      redis:
        host: ${SPRING_REDIS_HOST:redis.hzero.org}
        port: ${SPRING_REDIS_PORT:6379}
        database: ${SPRING_REDIS_DATABASE:3}
        lettuce:
          pool:
            # 资源池中最大连接数
            # 默认8，-1表示无限制；可根据服务并发redis情况及服务端的支持上限调整
            max-active: ${SPRING_REDIS_POOL_MAX_ACTIVE:50}
            # 资源池运行最大空闲的连接数
            # 默认8，-1表示无限制；可根据服务并发redis情况及服务端的支持上限调整，一般建议和max-active保持一致，避免资源伸缩带来的开销
            max-idle: ${SPRING_REDIS_POOL_MAX_IDLE:50}
            # 当资源池连接用尽后，调用者的最大等待时间(单位为毫秒)
            # 默认 -1 表示永不超时，设置5秒
            max-wait: ${SPRING_REDIS_POOL_MAX_WAIT:5000}
      resources:
        # 资源缓存时间，单位秒
        cache:
          period: 3600
        # 开启gzip压缩
        chain.gzipped: true
        # 启用缓存
        chain.cache: true
      mvc:
        throw-exception-if-no-handler-found: true
      thymeleaf:
        mode: LEGACYHTML5
    
    server:
      undertow:
        # 设置IO线程数, 它主要执行非阻塞的任务,它们会负责多个连接
        # 默认值为8，建议设置每个CPU核心一个线程
        io-threads: ${SERVER_UNDERTOW_IO_THREADS:4}
        # 阻塞任务线程池, 当执行类似servlet请求阻塞操作, undertow会从这个线程池中取得线程
        # 默认等于 io-threads*8，它的值设置取决于系统的负载，可适当调大该值
        worker-threads: ${SERVER_UNDERTOW_WORKER_THREADS:128}
        # 每块buffer的空间大小，越小空间被利用越充分
        # 不要设置太大，以免影响其他应用，合适即可
        buffer-size: ${SERVER_UNDERTOW_BUFFER_SIZE:1024}
        # 是否分配的直接内存(NIO直接分配的堆外内存)
        # 默认false
        direct-buffers: true
        # HTTP POST请求最大的大小
        # 默认0，无限制，可设置10M
        max-http-post-size: 10485760
    
    feign:
      hystrix:
        enabled: true
    
    hystrix:
      threadpool:
        default:
          # 执行命令线程池的核心线程数，也是命令执行的最大并发量
          # 默认10
          coreSize: 1000
          # 最大执行线程数
          maximumSize: 1000
      command:
        default:
          execution:
            isolation:
              thread:
                # HystrixCommand 执行的超时时间，超时后进入降级处理逻辑。一个接口，理论的最佳响应速度应该在200ms以内，或者慢点的接口就几百毫秒。
                # 默认 1000 毫秒，最高设置 2000足矣。如果超时，首先看能不能优化接口相关业务、SQL查询等，不要盲目加大超时时间，否则会导致线程堆积过多，hystrix 线程池卡死，最终服务不可用。
                timeoutInMilliseconds: ${HYSTRIX_COMMAND_TIMEOUT_IN_MILLISECONDS:40000}
    
    ribbon:
      # 客户端读取超时时间，超时时间要小于Hystrix的超时时间，否则重试机制就无意义了
      ReadTimeout: ${RIBBON_READ_TIMEOUT:30000}
      # 客户端连接超时时间
      ConnectTimeout: ${RIBBON_CONNECT_TIMEOUT:3000}
      # 访问实例失败(超时)，允许自动重试，设置重试次数，失败后会更换实例访问，请一定确保接口的幂等性，否则重试可能导致数据异常。
      OkToRetryOnAllOperations: true
      MaxAutoRetries: 1
      MaxAutoRetriesNextServer: 1
    
    mybatis:
      mapperLocations: classpath*:/mapper/*.xml
      configuration:
        mapUnderscoreToCamelCase: true
    
    hzero:
      user:
        enable-root: true
      captcha:
        # 是否启用验证码
        enable: true
      oauth:
        # 认证服务器 自定义资源匹配器
        custom-resource-matcher: ${HZERO_OAUTH_CUSTOM_RESOURCE_MATCHER:false}
        # 验证 client 时不检查 client 的一致性
        not-check-client-equals: ${HZERO_OAUTH_NOT_CHECK_CLIENT_EQUALS:false}
        # 始终开启图形验证码校验，默认否
        enable-always-captcha: ${HZERO_OAUTH_ENABLE_ALWAYS_CAPTCHA:false}
        # client_credentials 模式是否返回 refresh_token
        credentials-allow-refresh: ${HZERO_OAUTH_CREDENTIALS_ALLOW_REFRESH:false}
    
        login:
          page: ${HZERO_OAUTH_LOGIN_PAGE:/choerodon/login}
          #网关是否启用https
          enable-https: ${HZERO_OAUTH_LOGIN_ENABLE_HTTPS:false}
          # 跳转到默认登录成功地址时使用的客户端ID
          default-client-id: ${HZERO_OAUTH_LOGIN_DEFAULT_CLIENT_ID:choerodonparent}
          # 允许使用的登录名，默认有 用户名、邮箱、手机号
          support-fields: ${HZERO_OAUTH_LOGIN_SUPPORT_FIELDS:username,email,phone}
          # 前端默认模板
          default-template: ${HZERO_OAUTH_LOGIN_DEFAULT_TEMPLATE:main}
          # 默认登录成功跳转地址
          success-url: ${HZERO_OAUTH_LOGIN_SUCCESS_URL:http://api.example/com}
        social:
          # 三方登录启用 https
          enable-https: ${HZERO_OAUTH_LOGIN_ENABLE_HTTPS:false}
        logout:
          # 退出时是否清理token
          clear-token: ${HZERO_OAUTH_LOGOUT_CLEAR_TOKEN:true}
        sso:
          # 启用二级域名单点登录
          enabled: ${HZERO_OAUTH_SSO_ENABLED:false}
          provider:
            # 用于cas
            key: ${HZERO_OAUTH_SSO_PROVIDER_KEY:hzero}
          service: 
            baseUrl: ${hzero.oauth.login.gateway-domain}
          saml:
            entity_id: hzero:org:sp
            passphrase: secret
            private_key: MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDQRsCf6qU0DYkoFIJUhNlMxJFKMPsEvK+u3rcaBIZcyWX4Cv5OU3xtcCAg6mqRwMRFYFDNdGgR0XLTaHAOcJpR7cXYnYc0Wa6Kh8KSstgZrCl+WdqCtuUS6bMrrAdSq6HpoAPwo1JyOqyC9ccRZ9ysjhYdWQS1ELSjUHjEuxxRoEgwKfrF7kxbo89dixQ7oF9E9CgeWtftJfMtDxGtMhmtaIurHjjcfOPWR8TND0b1Lp1pLkzPn6GdI2aRWqV3tNsljKoXP9omDInhP9xrzoLiXISbekfnLrfFQW+rmylCBiu6ZqKv0weg1V6b7B6rAyV3nIcXInLuKXSlQx0nnPOdAgMBAAECggEAOrAlKSqyYIeL5XpZ+zzwCly9X/2LThtpGcpyJ+esgMrTa+CVJjcKMcBNnVjQrL93zuDEBBDQHm05gO7F3JvIMFviyxYgehTnROvaXQH+OMW1b4AcPYcR55Foxl6UNaxdVHqdgZpT6hI0eDaPYI02tnzXKG/kDq1laTuMvErJQQp6Cd611yyAhBvpX1ibpAYvex10sfTkj0GRKmOrGqwVXibN29szaRei7Xeg/RStdVBgrYJoR5/4++dkGapa27oRdOh4VJUChRfXuJtH6pyxC7uay1fMRcmo2u6NcWAT6qMOvxLcuesnNFrbSlPoZaxWNiZRX/SVqeieyRAA0WS7IQKBgQDoywh4DkdL+SPrkA/sB0rOQF3kJjlzWibk9OM17In1P+obQk37kSRYKfBvsk48VWdG1fN33Up05Pxe+f36F//AZ8mp7uTmBtd6CAoR/005WxwkCSihF6LaDiB3VtxlpcfRA/TUZ10PMud43w0AeG30AG0KpCokfIiY87OpyTjJWQKBgQDlCgsgZ9rL3Wm7FbEDZ4f2uTB5rlT0Vz80paV0OOJdUQECrZW1PjemQpqIJocr8yoNupkrZKPSi4mbNoMFF1wXIydOjLq6iQ6KWIKRdsvmeXL++tWg6TiD8nDpBxuKzjRhwMcQN2lakb/SusoXnmG8qq12PCFUvpbhoZRqRPWv5QKBgQC8jUasxxPka0U21RawXC+w4t2pn3RFBC4goGEwGgibxkr+DTRQoHzJlB6Uud04bQwbicuLuIdIKvhmjSGzYaDa3LWwmDh6P+xjgQN3FEweOreOUITCBfz3lR2iy430HtS7bPLu31G2r8pgUnmbee/FBFtNlS41I1EYYbuRt9Pw8QKBgD6aPSpRWKtqTHD3X9e3X6FfQtGvhcb3Ze5E7HFU7wJklqsduRK9+8X05HocVcv8fd0cyKrkqiZtP2JuRueIWAJ2+FJvAsbjmVbVFHMgDmFjhrwM4YFG3cyq4pO+/pc0/3pMj9xt2N0Jg23c4koMX1iLKjhr/QxFv8XSPVfCm4jFAoGALfejdx4PpFgTWpbm5ZWRxukhZRhmfCIAWifYeJbsGTB5y7bheVxKmTpP9mKEqGL+gh3cLVPcZ557HWpc4d6NetdyrHffEhWULh4NWYDKC5BRCr9HjLKydBUQUMCFeJs3XZQTtN+CZORcuaI2ISH2QvfYki9ns4ujeH8OjzfHpvI=
            certificate: MIIDEzCCAfugAwIBAgIJAKoK/heBjcOYMA0GCSqGSIb3DQEBBQUAMCAxHjAcBgNVBAoMFU9yZ2FuaXphdGlvbiwgQ049T0lEQzAeFw0xNTExMTExMDEyMTVaFw0yNTExMTAxMDEyMTVaMCAxHjAcBgNVBAoMFU9yZ2FuaXphdGlvbiwgQ049T0lEQzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANBGwJ/qpTQNiSgUglSE2UzEkUow+wS8r67etxoEhlzJZfgK/k5TfG1wICDqapHAxEVgUM10aBHRctNocA5wmlHtxdidhzRZroqHwpKy2BmsKX5Z2oK25RLpsyusB1KroemgA/CjUnI6rIL1xxFn3KyOFh1ZBLUQtKNQeMS7HFGgSDAp+sXuTFujz12LFDugX0T0KB5a1+0l8y0PEa0yGa1oi6seONx849ZHxM0PRvUunWkuTM+foZ0jZpFapXe02yWMqhc/2iYMieE/3GvOguJchJt6R+cut8VBb6ubKUIGK7pmoq/TB6DVXpvsHqsDJXechxcicu4pdKVDHSec850CAwEAAaNQME4wHQYDVR0OBBYEFK7RqjoodSYVXGTVEdLf3kJflP/sMB8GA1UdIwQYMBaAFK7RqjoodSYVXGTVEdLf3kJflP/sMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEFBQADggEBADNZkxlFXh4F45muCbnQd+WmaXlGvb9tkUyAIxVL8AIu8J18F420vpnGpoUAE+Hy3evBmp2nkrFAgmr055fAjpHeZFgDZBAPCwYd3TNMDeSyMta3Ka+oS7GRFDePkMEm+kH4/rITNKUF1sOvWBTSowk9TudEDyFqgGntcdu/l/zRxvx33y3LMG5USD0x4X4IKjRrRN1BbcKgi8dq10C3jdqNancTuPoqT3WWzRvVtB/q34B7F74/6JzgEoOCEHufBMp4ZFu54P0yEGtWfTwTzuoZobrChVVBt4w/XZagrRtUCDNwRpHNbpjxYudbqLqpi1MQpV9oht/BpTHVJG2i0ro=
      gateway:
        url: http://api.example.com
    
      reset-password:
        resetUrlExpireMinutes: 10
    
    choerodon:
      default:
         icp: ""
    logging:
      level: 
        org.hzero: ${LOG_LEVEL:info}
        io.choerodon: ${LOG_LEVEL:info}
        org.springframework.security: ${LOG_LEVEL:info}
    

  ```

- `bootstrap.yml`

  ```yaml
  server:
    port: 8020
    servlet:
      context-path: /oauth
  management:
    server:
      port: 8021
    endpoints:
      web:
        exposure:
          include: '*'
  
  spring:
    profiles:
      active: ${SPRING_PROFILES_ACTIVE:default}
    cloud:
      config:
        fail-fast: false
        # 是否启用配置中心
        enabled: ${SPRING_CLOUD_CONFIG_ENABLED:false}
        # 配置中心地址
        uri: ${SPRING_CLOUD_CONFIG_URI:http://dev.hzero.org:8010}
        retry:
          # 最大重试次数
          maxAttempts: 6
          multiplier: 1.1
          # 重试间隔时间
          maxInterval: 2000
        # 标签
        label: ${SPRING_CLOUD_CONFIG_LABEL:}
  
  eureka:
    instance:
      # 以IP注册到注册中心
      preferIpAddress: ${EUREKA_INSTANCE_PREFER_IP_ADDRESS:true}
      leaseRenewalIntervalInSeconds: 10
      leaseExpirationDurationInSeconds: 30
      # 服务的一些元数据信息
      metadata-map:
        VERSION: 1.3.0.RELEASE
        CONTEXT: /oauth
    client:
      serviceUrl:
        # 注册中心地址
        defaultZone: ${EUREKA_DEFAULT_ZONE:http://dev.hzero.org:8000/eureka}
      registryFetchIntervalSeconds: 10
      disable-delta: true

  ```

## 环境需求

- mysql 5.6+
- redis 3.0+
- `oauth-server` 服务依赖于 hzero-platform 服务的数据库, 所以请确保 `hzero-platform` 服务的数据库已经创建并初始化。
- 该项目是一个 Eureka Client 项目，启动后需要注册到 `hzero-register`

## 安装和启动步骤

- 运行 `hzero-register`

- 本地启动 redis-server

- 启动项目，项目根目录下执行如下命令：

  ```sh
   mvn spring-boot:run
  ```

## 更新日志

- [更新日志](./CHANGELOG.zh-CN.md)

## 如何参与

- 欢迎参与我们的项目，了解更多有关如何[参与贡献](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md)的信息。


