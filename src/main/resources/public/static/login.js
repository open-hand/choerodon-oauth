const { Input, Button, Tabs, notification } = window["choerodon-ui.min"];
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
    };
  }
  componentWillMount() {
    this.init();
  }
  componentDidMount() {
    if (this.state.messageInfo) {
      notification.warning({
        message: "警告",
        description: this.state.messageInfo,
        placement: "bottomLeft",
      });
    }
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
  }

  tabOnChange(key) {
    if (key === this.state.activeKey) {
      return;
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
  phoneLabel = (
    <span>
      手机号
      <span
        style={{
          display: "inline-block",
          lineHeight: 1,
          marginLeft: ".04rem",
          color: "rgb(247, 103, 118)",
          width: ".08rem",
          verticalAlign: "middle",
          content: " ",
          fontFamily: "SimSun",
        }}
      >
        *
      </span>
    </span>
  );
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
  pswLabel = (
    <span>
      密码
      <span
        style={{
          display: "inline-block",
          lineHeight: 1,
          marginLeft: ".04rem",
          color: "rgb(247, 103, 118)",
          width: ".08rem",
          verticalAlign: "middle",
          content: " ",
          fontFamily: "SimSun",
        }}
      >
        *
      </span>
    </span>
  );

  getContent() {
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
            label="登录名/邮箱"
            name="username"
            required
            placeholder="登录名/邮箱"
          />
        )}
        {this.state.activeKey === "1" && (
          <Password
            id="pswinput"
            // autoComplete="new-password"
            colSpan={3}
            newLine
            label={this.pswLabel}
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
            label={this.phoneLabel}
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
              注册
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
            忘记密码
          </a>
        </div>
        {/* 登录 */}
        <div newLine colSpan={3} style={{marginTop: 5}}>
          <Button
            htmlType="submit"
            type="primary"
            className="btn"
            funcType="raised"
            loading={this.state.loading}
            htmlStyle="padding-top:4px"
            onClick={this.submitBtnClick.bind(this)}
          >
            <span>{this.state.loading ? "登录中" : "登录"}</span>
          </Button>
        </div>
      </Form>
    );
  }

  render() {
    return (
      <div className="container-page">
        <div className="container-des">
          <img src="../static/choerodon-logo-02.svg" alt="" />
        </div>

        <div className="page-left">
          <div id="content">
            <span className="loginSpan">登录猪齿鱼</span>
            <div id="form-content">
              <Tabs
                activeKey={this.state.activeKey}
                onChange={this.tabOnChange.bind(this)}
              >
                <TabPane tab="账号密码登录" key="1">
                  {this.state.activeKey === "1" && this.getContent(1)}
                </TabPane>
                <TabPane tab="手机验证登录" key="2">
                  {this.state.activeKey === "2" && this.getContent(2)}
                </TabPane>
              </Tabs>
            </div>
          </div>
          <img src='../static/1.svg' alt="" className="btm-img" />
          <div className="btm-desc">
            <p>
              © Copyright Hand China Co.,Ltd. All Rights Reserved
              上海汉得信息技术股份有限公司
              <span>沪ICP备14039535号-18</span>
            </p>
          </div>
        </div>

        <div className="page-right">
          <div
            className="container-font"
            style={{ backgroundImage: `url(../static/2.svg)` }}
          >
            <div className="font">
              传递体系化方法论，提供协作、测试、DevOps及容器工具，让团队效能提升更快更稳更简单
            </div>
          </div>
          <img src='../static/3.svg' alt="" className="right-img2" />
        </div>
      </div>
    );
  }
}
ReactDOM.render(<Content />, document.getElementById("root"));
