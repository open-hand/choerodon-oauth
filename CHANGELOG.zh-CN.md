# Changelog

这个项目的所有显著变化都将被记录在这个文件中。

## [0.6.0] - 2018-06-08

### 新增

- 添加redis作为存储登录session，用于保证认证服务开启多实例时的用户会话。
- customUserDetails中设置了is_admin的值。

### 修改

- Ldap 用户登录逻辑修改。