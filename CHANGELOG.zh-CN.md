# Changelog

这个项目的所有显著变化都将被记录在这个文件中。

## [0.8.1] - 2018-07-26

### 修改

- 修改`choerodon.oauth.loginPage.domain`为`choerodon.oauth.login.domain` 。

## [0.8.0] - 2018-07-19

### 新增

- 添加`choerodon.oauth.loginPage.domain` 配置，改配置可以指定`/login` 页面的地址，支持`https`。eg.:`https://api.example.com/oauth/login`
- 添加用户登出接口，并移除用户redis里的session
- 添加https支持
 
### 修改

- 修改了移动端登陆页面
- security放开管理端口url
- 优化了点击登陆后密码位数变长的问题
- 优化登陆页面为不使用缓存



## [0.7.0] - 2018-06-22

### 修改

- 升级了chart中dbtool的版本为0.5.2。
- 登录页面用户被锁定时不再显示验证码。
- 修改前端传输密码方式为密文传输。
- 修改登录用户名或密码为空时的报错信息。
- 修复登录错误次数计数错误的问题。
- 升级choerodon-starter依赖版本为0.5.3.RELEASE。

## [0.6.0] - 2018-06-08

### 新增

- 添加redis作为存储登录session，用于保证认证服务开启多实例时的用户会话。
- customUserDetails中设置了is_admin的值。

### 修改

- Ldap 用户登录逻辑修改。