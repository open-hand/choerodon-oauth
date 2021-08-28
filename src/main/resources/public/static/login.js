const { Input, Button, message } = window["choerodon-ui.min"];
const { Form, TextField, Password } = window["choerodon-ui-pro.min"];
let timer = null;
class Content extends window.React.Component {
  constructor(props) {
    super(props);

    this.state = {
      action: "/oauth/choerodon/login",
      activeKey: "1",
      loading: false,
      text: "获取验证码",
      time: 0,
      captchaKey: "",
      publicKey: $("#publicKeyTemplateData").data("publicKey"),
      isNeedCaptcha: $("#isNeedCaptchaTemplateData").data("isNeedCaptcha"),
      registerUrl: $("#isNeedCaptchaTemplateData").data("registerUrl"),
      // isNeedCaptcha: true,
      imgSrc: "/oauth/public/captcha",
    };
  }

  tabOnChange(key) {
    if (key === this.state.activeKey) {
      return;
    }
    let list = document.getElementsByClassName("c7n-pro-validation-message");
    for (let i = 0; i < list.length; i++) {
      list[i].style.display = "none";
    }
    let obj = {
      1: "/oauth/choerodon/login",
      2: "/oauth/choerodon/login/sms",
    };
    this.setState({
      action: obj[key],
    });
    this.setState({
      activeKey: key,
    });
    for (let i = 0; i < document.getElementsByClassName("tabs").length; i++) {
      document
        .getElementsByClassName("tabs")
        [i].classList.remove("tabs-active");
    }
    document
      .getElementsByClassName("tabs")
      [+key - 1].classList.add("tabs-active");
  }
  refreshImg() {
    let timestamp = new Date().valueOf();
    this.setState({
      imgSrc: "/oauth/public/captcha?code=" + timestamp,
    });
  }
  forTime(time) {
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
    let phone = document.getElementById("phoneInput").value;
    if (!phone) {
      return;
    }
    if (this.state.time > 0) {
      return;
    }
    this.forTime(60);
    fetch(
      `http://172.23.16.154:30094/oauth/public/send-phone-captcha?phone=${phone}`
    )
      .then((response) => {
        return response.json();
      })
      .then((res) => {
        console.log(res);
        if (res.success) {
          message.success(res.message);
          this.setState({
            captchaKey: res.captchaKey,
          });
        } else {
          clearInterval(timer);
          this.forTime(res.interval);
          message.warning(res.message);
        }
      });
  }

  render() {
    return (
      <div>
        <div id="loginTypeTabs">
          <span
            className="tabs tabs-active"
            onClick={() => {
              this.tabOnChange("1");
            }}
            style={{ marginRight: 32 }}
          >
            账号密码
          </span>
          <span
            className="tabs"
            onClick={() => {
              this.tabOnChange("2");
            }}
          >
            手机验证码
          </span>
        </div>

        <div>
          <Form
            target="_self"
            className={+this.state.activeKey === 2 ? "phone-login-form" : ""}
            id="myForm"
            method="post"
            action={this.state.action}
            columns={3}
            labelLayout="float"
          >
            {/* 登录名/邮箱登录 */}
            {this.state.activeKey === "1" && (
              <TextField
                key={1}
                colSpan={3}
                width="100%"
                label="登录名/邮箱"
                name="username"
                required
                placeholder="登录名/邮箱"
              />
            )}
            {this.state.activeKey === "1" && (
              <Password
                colSpan={3}
                newLine
                label="密码"
                name="password"
                required
              />
            )}

            {/* 手机验证登陆 */}
            {this.state.activeKey === "2" && (
              <TextField
                key={2}
                id="phoneInput"
                pattern="1[3-9]\d{9}"
                colSpan={3}
                label="手机号"
                name="phone"
                required
                placeholder="手机号"
              />
            )}

            {this.state.activeKey === "2" && (
              <div colSpan={3} style={{ position: "relative" }}>
                <TextField
                  style={{ width: "100%" }}
                  validator={(value) => {
                    const reg = /^\d{6}$/;
                    if (reg.test(value)) {
                      return true;
                    }
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
                    top: 7,
                    color: "#5365EA",
                    zIndex: 10,
                    cursor: "pointer",
                  }}
                  onClick={this.getVerificationCode.bind(this)}
                >
                  {this.state.time === 0 && <span>获取验证码</span>}
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
                        height: 34,
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

            {/* 注册 忘记密码 */}

            <div
              newLine
              style={{ color: "#5365EA", textAlign: "right" }}
              colSpan={3}
            >
              {this.state.registerUrl && (
                <a href={this.state.registerUrl}>注册</a>
              )}
              {this.state.registerUrl && (
                <span
                  style={{
                    marginLeft: 6,
                    marginRight: 6,
                  }}
                >
                  <span>|</span>
                </span>
              )}
              <a href="./password/find">忘记密码</a>
            </div>
            {/* 登录 */}
            <div newLine colSpan={3}>
              <Button
                htmlType="submit"
                type="primary"
                className="btn"
                funcType="raised"
                loading={this.state.loading}
                htmlStyle="padding-top:4px"
              >
                <span>{this.state.loading ? "登录中" : "登录"}</span>
              </Button>
            </div>
          </Form>
        </div>
      </div>
    );
  }
}
ReactDOM.render(<Content />, document.getElementById("form-content"));
