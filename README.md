# Oauth Server

这个服务是猪齿鱼微服务框架的权限认证中心，它主要保证用户权限与用户认证。

## 特性

增加了额外的认证登录方法（如：微信登录等）

## 准备

- MySQL - iam-service database
- redis - sessions & cache

`oauth-server` 服务依赖于 [iam-service](https://github.com/choerodon/iam-service) 服务的数据库, 所以请确保 `iam-service` 服务的数据库在使用前已经被初始化并投入使用。

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

