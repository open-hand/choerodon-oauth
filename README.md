# choerodon-oauth
统一身份认证

## Introduction
这个服务是猪齿鱼微服务框架的权限认证中心，它主要保证用户权限与用户认证。此服务是对[hzero-oauth](https://github.com/open-hand/hzero-oauth.git)的二开，定制化了统一登录界面与找回密码功能。


## Features

- 统一登录界面
- 账户、手机、邮箱登录
- 短信登录
- 第三方登录
- 完整的OAuth2协议实现
- 密码找回功能
- Token 管理功能
- 基于Ldap的用户认证

## Architecture

![](http://file.open.hand-china.com/hsop-doc/doc_classify/0/7af865a0de5f4ad2847ad69018ede20e/image.png)

## 功能介绍

1. 用户登录认证：
    - 用户使用用户名与密码完成oauth认证。
    - Oauth将会基于用户与已认证的客户端生成一个`access_token`，并将其存放在`tokenStore`中。
2. 用户访问资源服务认证：
    - 用户请求中带有`access_token`。在oauth完成检验后，请求会由网关转发到相应的资源服务
    - 对于给用户的非法请求，返回一个401状态码并且跳转到登录页面等待重新认证。


## Documentation
- 更多详情请参考`hzero-oauth`[中文文档](http://open.hand-china.com/document-center/doc/application/10033/10154?doc_id=4843)

## Dependencies

* 服务依赖

```xml
<dependency>
    <groupId>org.hzero</groupId>
    <artifactId>hzero-oauth</artifactId>
    <version>${hzero.service.version}</version>
</dependency>
```


## Changelog

- [更新日志](./CHANGELOG.zh-CN.md)

## Contributing

欢迎参与项目贡献！比如提交PR修复一个bug，或者新建Issue讨论新特性或者变更。

Copyright (c) 2020-present, HZERO


