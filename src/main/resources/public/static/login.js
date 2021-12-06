const { Input, Button, Tabs, notification, Icon } = window["choerodon-ui.min"];
const { Form, TextField, Password } = window["choerodon-ui-pro.min"];
let timer = null;
const TabPane = Tabs.TabPane;
const keyStr =
  "ABCDEFGHIJKLMNOP" +
  "QRSTUVWXYZabcdef" +
  "ghijklmnopqrstuv" +
  "wxyz0123456789+/" +
  "=";
class Content extends window.React.Component {
  constructor(props) {
    super(props);
    this.state = {
      action: "/oauth/choerodon/login",
      activeKey: "1",
      defaultValue_username: "",
      defaultValue_phone: "",
      loading: false,
      text: "获取验证码",
      time: 0,
      phoneValidateSuccess: false,
      captchaValidateSuccess: false,
      dropDownVisible: false,
      currentLanguage: "zh_CN",
      captchaKey: document
        .getElementById("captchaKeyTemplateData")
        .getAttribute("data-captchaKey"),
      publicKey: document
        .getElementById("publicKeyTemplateData")
        .getAttribute("data-publicKey"),
      isNeedCaptcha: document
        .getElementById("isNeedCaptchaTemplateData")
        .getAttribute("data-isNeedCaptcha")
        ? JSON.parse(
            document
              .getElementById("isNeedCaptchaTemplateData")
              .getAttribute("data-isNeedCaptcha")
          )
        : false,
      registerUrl: document
        .getElementById("registerUrlTemplateData")
        .getAttribute("data-registerUrl"),
      messageInfo: document
        .getElementById("messageTemplateData")
        .getAttribute("data-messageTemplateData"),
      imgSrc: "/oauth/public/captcha",

      languageZH_CN: {
        signInzcy: "登录猪齿鱼",
        accountLogin: "账号密码",
        mobileLogin: "手机验证码",
        loginAccountEmail: "登录名/邮箱",
        password: "密码",
        register: "注册",
        forgotPassword: "忘记密码",
        signIn: "登录",
        desc:
          "传递体系化方法论，提供协作、测试、DevOps及容器工具，让团队效能提升更快更稳更简单",
      },
      languageEN_US: {
        signInzcy: "Sign in",
        accountLogin: "Account Login",
        mobileLogin: "Mobile Login",
        loginAccountEmail: "Login Account/Email",
        password: "Password",
        register: "Register",
        forgotPassword: "Forgot Password",
        signIn: "Sign in",
        desc:
          "Deliver a systematic methodology that provides collaboration, testing, DevOps and container tools to make team performance faster, more stable and easier.",
      },
    };
  }

  languageRequest() {
    let formData = new FormData();
    formData.append("lang", this.state.currentLanguage);
    fetch("/oauth/login/lang", {
      method: "post",
      body: formData,
    }).then((res) => {
      console.log(res);
    });
  }

  componentWillMount() {
    this.init();
  }
  init() {
    let arr = window.location.href.split("?");
    if (arr[1]) {
      let paramsObj = {};
      arr[1].replace(/([^=&]+)=([^&]*)/g, (m, key, value) => {
        paramsObj[decodeURIComponent(key)] = decodeURIComponent(value);
      });
      if (paramsObj.type && paramsObj.type === "sms") {
        this.setState({
          defaultValue_phone: paramsObj.phone,
          phoneValidateSuccess: true,
        });
        this.tabOnChange("2");
      } else if (paramsObj.type && paramsObj.type === "account") {
        this.setState({
          defaultValue_username: paramsObj.username,
        });
      }
    }

    if (this.state.messageInfo) {
      notification.warning({
        message: "警告",
        description: this.state.messageInfo,
        placement: "bottomLeft",
      });
    }

    const language = localStorage.getItem("language") || "zh_CN";
    this.setState(
      {
        currentLanguage: language,
      },
      () => {
        this.tabBarInit();
        this.languageRequest();
      }
    );
  }

  tabOnChange(key) {
    if (key === this.state.activeKey) {
      return;
    }
    // tab  底部线样式
    if (key === "1") {
      document
        .getElementsByClassName("c7n-tabs-ink-bar")[0]
        .setAttribute("id", "");
    } else if (key === "2") {
      document
        .getElementsByClassName("c7n-tabs-ink-bar")[0]
        .setAttribute("id", `c7n-tabs-ink-bar-${this.state.currentLanguage}-2`);
    }

    let obj = {
      1: "/oauth/choerodon/login",
      2: "/oauth/choerodon/login/sms",
    };
    this.setState({
      action: obj[key],
      activeKey: key,
    });
    if (key === "2" && $.cookie("getVerificationCodeTime")) {
      let timeDifference =
        new Date().getTime() - +$.cookie("getVerificationCodeTime");
      if (timeDifference < 60000) {
        this.forTime(Math.ceil(60 - timeDifference / 1000));
      }
    }
  }
  refreshImg() {
    let timestamp = new Date().valueOf();
    this.setState({
      imgSrc: "/oauth/public/captcha?code=" + timestamp,
    });
  }
  forTime(time) {
    clearTimeout(timer);
    this.setState(
      {
        time: time,
      },
      () => {
        timer = setInterval(() => {
          if (this.state.time - 1 >= 0) {
            this.setState({
              time: this.state.time - 1,
            });
          } else {
            clearTimeout(timer);
          }
        }, 1000);
      }
    );
  }
  getVerificationCode() {
    if (!this.state.phoneValidateSuccess) {
      return;
    }
    let phone = document.getElementById("phoneInput").value;
    if (!phone) {
      return;
    }
    if (this.state.time > 0) {
      return;
    }
    this.forTime(60);
    fetch(`/oauth/choerodon/public/send-phone-captcha?phone=${phone}`)
      .then((response) => {
        return response.json();
      })
      .then((res) => {
        console.log(res);
        if (res.success) {
          notification.success({
            message: "成功",
            description: res.message,
            placement: "bottomLeft",
          });
          this.setState({
            captchaKey: res.captchaKey,
          });
          $.cookie("getVerificationCodeTime", new Date().getTime()); // 成功获取验证码的时间
        } else {
          clearInterval(timer);
          this.forTime(res.interval);
          notification.warning({
            message: "警告",
            description: res.message,
            placement: "bottomLeft",
          });
        }
      });
  }

  submitBtnClick() {
    if (this.state.activeKey === "1") {
      let inputEncrypt = document.getElementById("encryptPassword");
      let input = document.getElementById("pswinput");
      let encrypt = new JSEncrypt();
      encrypt.setPublicKey(this.state.publicKey); // 加密
      inputEncrypt.value = encrypt.encrypt(input.value);
    }
    if (!this.state.captchaKey && this.state.activeKey === "2") {
      if (
        this.state.phoneValidateSuccess &&
        this.state.captchaValidateSuccess
      ) {
        notification.warning({
          message: "警告",
          description: "请先获取验证码",
          placement: "bottomLeft",
        });
      }
    }
  }
  formSubmit() {
    if (this.state.activeKey === "1") {
      let inputEncrypt = document.getElementById("encryptPassword");
      inputEncrypt.setAttribute("value", inputEncrypt.value);
    }
    $("#myForm").submit();
  }

  phoneInput = (e) => {
    // 输入不用点一下失焦才能点获取验证码
    if (/^1[3456789]\d{9}$/.test(e.target.value)) {
      this.setState({
        phoneValidateSuccess: true,
      });
    } else {
      this.setState({
        phoneValidateSuccess: false,
      });
    }
  };

  getlabelRequired = (text) => {
    return (
      <span>
        {text}
        <span className="label-required">*</span>
      </span>
    );
  };

  handleLanguageMenu = () => {
    if (this.state.dropDownVisible) {
      document
        .getElementsByClassName("menu-list")[0]
        .setAttribute("style", "height: 0");
      document.getElementsByClassName("icon-expand_more")[0].style.transform =
        "rotate(0deg)";
    } else {
      document
        .getElementsByClassName("menu-list")[0]
        .setAttribute("style", "height: 86px");
      document.getElementsByClassName("icon-expand_more")[0].style.transform =
        "rotate(180deg)";
    }
    this.setState({
      dropDownVisible: !this.state.dropDownVisible,
    });
  };

  tabBarInit = () => {
    const elment = document.getElementsByClassName("c7n-tabs-ink-bar")[0];
    elment.setAttribute("id", "");
    elment.classList.remove(
      `c7n-tabs-ink-bar-zh_CN-1`,
      `c7n-tabs-ink-bar-en_US-1`
    );
    elment.classList.add(`c7n-tabs-ink-bar-${this.state.currentLanguage}-1`);
  };

  languageSwitch = (i) => {
    localStorage.setItem("language", i);
    document
      .getElementsByClassName("menu-list")[0]
      .setAttribute("style", "height: 0");
      document.getElementsByClassName("icon-expand_more")[0].style.transform =
      "rotate(0deg)";
    this.setState(
      {
        dropDownVisible: !this.state.dropDownVisible,
        activeKey: "1",
        currentLanguage: i,
      },
      () => {
        this.tabBarInit();
        this.languageRequest();
      }
    );
  };

  getTabPaneContent() {
    const language =
      this.state.currentLanguage === "zh_CN"
        ? this.state.languageZH_CN
        : this.state.languageEN_US;
    return (
      <Form
        // target="_self"
        className={
          +this.state.activeKey === 2 ? "phone-login-form" : "psw-login-form"
        }
        id="myForm"
        onSubmit={this.formSubmit.bind(this)}
        method="post"
        action={this.state.action}
        columns={3}
        labelLayout="float"
      >
        {/* 登录名/邮箱登录 */}
        {this.state.activeKey === "1" && (
          <TextField
            defaultValue={this.state.defaultValue_username}
            // autoComplete="off"
            key={1}
            colSpan={5}
            width="100%"
            label={language.loginAccountEmail}
            name="username"
            required
            placeholder={language.loginAccountEmail}
            validator={(value) => {
              let reg = /^[^\u4e00-\u9fa5]+$/;
              if (!reg.test(value)) {
                return "登录名不能含有中文";
              }
              return true;
            }}
          />
        )}
        {this.state.activeKey === "1" && (
          <Password
            id="pswinput"
            // autoComplete="new-password"
            colSpan={3}
            newLine
            label={this.getlabelRequired(language.password)}
            validator={(value) => {
              let passwordValue = document.getElementById("pswinput").value;
              if (!passwordValue) {
                return "请输入密码";
              }
              return true;
            }}
          />
        )}
        {this.state.activeKey === "1" && (
          <input type="hidden" name="password" id="encryptPassword" />
        )}

        {/* 手机验证登陆 */}
        {this.state.activeKey === "2" && (
          <TextField
            defaultValue={this.state.defaultValue_phone}
            maxLength={11}
            // autoComplete="off"
            key={2}
            id="phoneInput"
            label={this.getlabelRequired("手机号")}
            onInput={this.phoneInput}
            validator={(value) => {
              if (!value) {
                this.setState({
                  phoneValidateSuccess: false,
                });
                return "请输入手机号";
              }
              if (!/^1[3456789]\d{9}$/.test(value)) {
                this.setState({
                  phoneValidateSuccess: false,
                });
                return "请填写正确的手机号";
              }
              this.setState({
                phoneValidateSuccess: true,
              });
              return true;
            }}
            colSpan={3}
            name="phone"
          />
        )}

        {this.state.activeKey === "2" && (
          <div colSpan={3} style={{ position: "relative" }}>
            <TextField
              maxLength={6}
              // autoComplete="off"
              style={{ width: "100%" }}
              validator={(value) => {
                const reg = /^\d{6}$/;
                if (reg.test(value)) {
                  this.setState({
                    captchaValidateSuccess: true,
                  });
                  return true;
                }
                this.setState({
                  captchaValidateSuccess: false,
                });
                return "验证码应为6位数字";
              }}
              label="验证码"
              name="captcha"
              required
              placeholder="请输入验证码"
            />
            <span
              style={{
                position: "absolute",
                right: 13,
                top: 15,
                color: "#5365EA",
                zIndex: 10,
                cursor: "pointer",
              }}
              onClick={this.getVerificationCode.bind(this)}
            >
              {this.state.time === 0 && (
                <span
                  style={
                    !this.state.phoneValidateSuccess
                      ? { color: "#D9D9D9", cursor: "not-allowed" }
                      : {}
                  }
                >
                  获取验证码
                </span>
              )}
              {this.state.time !== 0 && (
                <span style={{ color: "#0F1358" }}>
                  {this.state.time}s后重新获取
                </span>
              )}
            </span>
          </div>
        )}
        {this.state.activeKey === "2" && (
          <div
            className="c-captchaKeyInput"
            style={{ display: "none", height: "0px !important" }}
          >
            <TextField
              // autoComplete="off"
              value={this.state.captchaKey}
              id="captchaKeyInput"
              colSpan={3}
              name="captchaKey"
              required
            />
          </div>
        )}

        {/* 连续输入多次错误验证码 */}

        {this.state.isNeedCaptcha && this.state.activeKey !== "2" && (
          <div newLine colSpan={3}>
            <div className="line-container-captcha">
              <div>
                <TextField
                  // autoComplete="off"
                  label="验证码"
                  // colSpan={3}
                  name="captcha"
                  required
                />
              </div>
              <div>
                <img
                  id="imgObj"
                  src={this.state.imgSrc}
                  style={{
                    border: "1px solid #ccc",
                    width: 88,
                    height: 32,
                  }}
                  onClick={this.refreshImg.bind(this)}
                />
                <span
                  style={{
                    marginLeft: 6,
                    width: 16,
                    height: 32,
                    border: "1px solid #ccc",
                    display: "inline-block",
                    position: "relative",
                    // top: 2,
                    cursor: "pointer",
                  }}
                  onClick={this.refreshImg.bind(this)}
                >
                  <img
                    style={{
                      width: "100%",
                      height: "100%",
                    }}
                    src="../static/refresh.svg"
                  />
                </span>
              </div>
            </div>
          </div>
        )}

        {/* 注册 | 忘记密码 */}

        <div
          newLine
          style={{ color: "#5365EA", textAlign: "right" }}
          colSpan={3}
        >
          {this.state.registerUrl && (
            <a style={{ color: "#5365EA" }} href={this.state.registerUrl}>
              {language.register}
            </a>
          )}
          {this.state.registerUrl && (
            <span
              style={{
                color: "#5365EA",
                marginLeft: 6,
                marginRight: 6,
              }}
            >
              <span>|</span>
            </span>
          )}
          <a style={{ color: "#5365EA" }} href="./password/find">
            {language.forgotPassword}
          </a>
        </div>
        {/* 登录 */}
        <div newLine colSpan={3} style={{ marginTop: 5 }}>
          <Button
            htmlType="submit"
            type="primary"
            className="btn"
            funcType="raised"
            loading={this.state.loading}
            htmlStyle="padding-top:4px"
            onClick={this.submitBtnClick.bind(this)}
          >
            <span>{this.state.loading ? "登录中" : language.signIn}</span>
          </Button>
        </div>
      </Form>
    );
  }

  render() {
    const language =
      this.state.currentLanguage === "zh_CN"
        ? this.state.languageZH_CN
        : this.state.languageEN_US;
    return (
      <div className="container-page">
        <div className="container-des">
          <img src="../static/choerodon-logo-02.svg" alt="" />
        </div>

        <div className="page-left">
          <div id="content">
            <div className="info-email-phone">
              <span className="info-phone">
                <img
                  style={{ position: "relative", top: -1 }}
                  src="../static/call_black_24dp.svg"
                  alt=""
                />
                <span className="info-email-phone-font">400-168-4263</span>
              </span>
              <span>
                <img src="../static/email_black_24dp.svg" alt="" />
                <span className="info-email-phone-font">
                  zhuchiyu@vip.hand-china.com
                </span>
              </span>
            </div>

            <span className="loginSpan">{language.signInzcy}</span>
            <div id="form-content">
              <Tabs
                activeKey={this.state.activeKey}
                onChange={this.tabOnChange.bind(this)}
              >
                <TabPane tab={language.accountLogin} key="1">
                  {this.state.activeKey === "1" && this.getTabPaneContent(1)}
                </TabPane>
                <TabPane tab={language.mobileLogin} key="2">
                  {this.state.activeKey === "2" && this.getTabPaneContent(2)}
                </TabPane>
              </Tabs>
            </div>
          </div>
          <div className="c-btn-img">
            <img src="../static/1.svg" alt="" className="btm-img" />
          </div>
          <div className="btm-desc">
            <p>
              © Copyright Hand China Co.,Ltd. All Rights Reserved
              上海汉得信息技术股份有限公司
              <span>沪ICP备14039535号-18</span>
            </p>
          </div>

          <div className="language-switch">
            <span
              onClick={this.handleLanguageMenu}
              className="language-switch-currentLanguage"
            >
              <Icon
                type="language"
                style={{ position: "relative", top: -1, marginRight: 6 }}
              />
              {this.state.currentLanguage === "zh_CN"
                ? "简体中文"
                : "English (US)"}
              <Icon
                className="icon-expand_more"
                type="expand_less"
                style={{ position: "relative", top: -1, marginLeft: 6 }}
              />
            </span>
            <div className="menu-list">
              <div
                className="menu-list-item"
                onClick={() => {
                  this.languageSwitch("zh_CN");
                }}
                style={
                  this.state.currentLanguage === "zh_CN"
                    ? { background: "#F1F3FF", color: "#5365EA" }
                    : {}
                }
              >
                简体中文
              </div>
              <div
                className="menu-list-item"
                onClick={() => {
                  this.languageSwitch("en_US");
                }}
                // style={{background:'red'}}
                style={
                  this.state.currentLanguage === "en_US"
                    ? { background: "#F1F3FF", color: "#5365EA" }
                    : {}
                }
              >
                English (US)
              </div>
            </div>
          </div>
        </div>

        <div className="page-right">
          <div
            className="container-font"
            style={{ backgroundImage: `url(../static/2.svg)` }}
          >
            <div className="font">{language.desc}</div>
          </div>
          <img src="../static/3.svg" alt="" className="right-img2" />
        </div>
      </div>
    );
  }
}
ReactDOM.render(<Content />, document.getElementById("root"));
