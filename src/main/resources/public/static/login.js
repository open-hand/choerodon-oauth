const { Input, Button } = window["choerodon-ui.min"];
const { Form, TextField, Password } = window["choerodon-ui-pro.min"];

class LoginTypeTabs extends window.React.Component {
  constructor(props) {
    super(props);
  }
  onChange(key) {
    if (key === activeKey) {
      return;
    }
    for (let i = 0; i < document.getElementsByClassName("tabs").length; i++) {
      document
        .getElementsByClassName("tabs")
        [i].classList.remove("tabs-active");
    }
    document
      .getElementsByClassName("tabs")
      [+key - 1].classList.add("tabs-active");
    activeKey = key;
    ReactDOM.render(
      <UsernameInupt />,
      document.getElementById("usernameInupt")
    );
    ReactDOM.render(
      <PasswordInput />,
      document.getElementById("passwordInupt")
    );
  }
  render() {
    return (
      <div>
        <span
          className="tabs tabs-active"
          onClick={() => {
            this.onChange("1");
          }}
          style={{ marginRight: 32 }}
        >
          账号密码
        </span>
        <span
          className="tabs"
          onClick={() => {
            this.onChange("2");
          }}
        >
          手机验证码
        </span>
      </div>
    );
  }
}
class LoginButton extends window.React.Component {
  state = {
    loading: false,
  };

  constructor(props) {
    super(props);
  }

  handleButtonClickTest = (e) => {
    this.setState({ loading: true });

    $("#usernameIsNullMsg").css("display", "none");
    $("#passwordIsNullMsg").css("display", "none");
    $("#usernameOrPasswordNotFoundMsg").html("");
    let valueFirstLine, valueSecondLine;
    if (activeKey === "1") {
      valueFirstLine = $.trim($("#username").val());
      valueSecondLine = $.trim($("#password").val());
    } else {
      valueFirstLine = $.trim($("#phone").val());
      valueSecondLine = $.trim($("#verificationCode").val());
    }

    if (valueFirstLine == "") {
      $("#usernameIsNullMsg").css("display", "block");
      if (activeKey === "1") {
        $("#usernameIsNullMsg").html("请输入登录账号");
      } else {
        $("#usernameIsNullMsg").html("请输入手机号");
      }
      this.setState({ loading: false });
      return;
    }
    if (valueSecondLine == "") {
      $("#passwordIsNullMsg").css("display", "block");
      if (activeKey === "1") {
        $("#passwordIsNullMsg").html("请输入密码");
      } else {
        $("#passwordIsNullMsg").html("请输入验证码");
      }
      this.setState({ loading: false });
      return;
    }
    if (activeKey === "1") {
      $(".login-form").attr("action", "login");
      $("#md5_password").val(this.encryptPwd(valueSecondLine));
    } else {
      $(".login-form").attr("action", "login/sms");
    }
    $(".login-form").submit();
  };

  encryptPwd = (password) => {
    var publickey = $("#templateData").data("publickey");
    publickey = 123;
    console.log(publickey, "publickey");
    /* 有公钥 使用 rsa 加密, 否则使用 md5 加密 */
    if (publickey) {
      // 初始化加密器
      var encrypt = new JSEncrypt(); // 设置公钥

      encrypt.setPublicKey(publickey); // 加密

      return encrypt.encrypt(password);
    } else {
      return this.encryptMd5(password);
    }
  };
  encryptMd5 = (password) => {
    var output = "";
    var chr1,
      chr2,
      chr3 = "";
    var enc1,
      enc2,
      enc3,
      enc4 = "";
    var i = 0;

    do {
      chr1 = password.charCodeAt(i++);
      chr2 = password.charCodeAt(i++);
      chr3 = password.charCodeAt(i++);
      enc1 = chr1 >> 2;
      enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
      enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
      enc4 = chr3 & 63;

      if (isNaN(chr2)) {
        enc3 = enc4 = 64;
      } else if (isNaN(chr3)) {
        enc4 = 64;
      }

      output =
        output +
        keyStr.charAt(enc1) +
        keyStr.charAt(enc2) +
        keyStr.charAt(enc3) +
        keyStr.charAt(enc4);
      chr1 = chr2 = chr3 = "";
      enc1 = enc2 = enc3 = enc4 = "";
    } while (i < password.length);

    return output;
  };
  encode = (password) => {
    var output = "";
    var chr1,
      chr2,
      chr3 = "";
    var enc1,
      enc2,
      enc3,
      enc4 = "";
    var i = 0;
    do {
      chr1 = password.charCodeAt(i++);
      chr2 = password.charCodeAt(i++);
      chr3 = password.charCodeAt(i++);
      enc1 = chr1 >> 2;
      enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
      enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
      enc4 = chr3 & 63;
      if (isNaN(chr2)) {
        enc3 = enc4 = 64;
      } else if (isNaN(chr3)) {
        enc4 = 64;
      }
      output =
        output +
        keyStr.charAt(enc1) +
        keyStr.charAt(enc2) +
        keyStr.charAt(enc3) +
        keyStr.charAt(enc4);
      chr1 = chr2 = chr3 = "";
      enc1 = enc2 = enc3 = enc4 = "";
    } while (i < password.length);
    return output;
  };

  render() {
    return (
      <Button
        type="primary"
        className="btn"
        onClick={this.handleButtonClickTest}
        funcType="raised"
        loading={this.state.loading}
        htmlStyle="padding-top:4px"
      >
        <span>{this.state.loading ? "登录中" : "登录"}</span>
      </Button>
    );
  }
}

class UsernameInupt extends window.React.Component {
  constructor(props) {
    super(props);
  }

  state = {
    currentUsername: "",
  };

  componentDidMount() {
    this.setState({
      currentUsername: this.getUrlParams("username"),
    });
    // let elem = document.createElement("div");
    // elem.className = 'ant-input-label';
    // elem.innerHTML = '登录账号*';
    // document.getElementById('username').parentNode.appendChild(elem);
  }

  getUrlParams = (name) => {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i"); //定义正则表达式
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]);
    return null;
  };
  onValueChange = (e) => {
    this.setState({
      currentUsername: e.target.value,
    });
  };

  render() {
    return (
      <div>
        {activeKey === "1" && (
          <Input
            autoFocus
            autoComplete="off"
            name="username"
            id="username"
            onChange={(e) => this.onValueChange(e)}
            label="登录名/邮箱"
            placeholder="登录名/邮箱"
            defaultValue=" "
            value={this.state.currentUsername}
          />
        )}
        {activeKey === "2" && (
          <Input
            autoFocus
            autoComplete="off"
            name="phone"
            id="phone"
            label="手机号"
            placeholder="手机号"
            defaultValue=" "
          />
        )}
      </div>
    );
  }
}

class PasswordInput extends window.React.Component {
  constructor(props) {
    super(props);
  }

  onValueChange = (e) => {
    this.setState({
      currentPassword: e.target.value,
    });
  };
  state = {
    currentPassword: "",
  };

  componentDidMount() {
    this.setState({
      currentPassword: "",
    });
    // let elem = document.createElement("div");
    // elem.className = 'ant-input-label';
    // elem.innerHTML = '密码*';
    // document.getElementById('password').parentNode.appendChild(elem);
  }
  getVerificationCode() {
    let phone = $.trim($("#phone").val());
    console.log(phone);
    fetch(
      `http://172.23.16.154:30094/oauth/public/send-phone-captcha?phone=${phone}`
    )
      .then(function (response) {
        return response.json();
      })
      .then(function (res) {
        console.log(res);
        let myform = $(".login-form"); //得到form对象
        let tmpInput = $("<input type='text' name='captchaKey'/>");
        tmpInput.style.display = "none";
        tmpInput.attr("value", res.captchaKey);
        myform.append(tmpInput);
      });
  }

  render() {
    return (
      <div style={{ position: "relative" }}>
        {activeKey === "1" && (
          <Input
            labelLayout="float"
            type="password"
            onChange={(e) => this.onValueChange(e)}
            autoComplete="off"
            label="密码"
            id="password"
            placeholder="请输入密码"
            defaultValue=""
            showPasswordEye
          />
        )}
        {activeKey === "2" && (
          <Input
            labelLayout="float"
            onChange={(e) => this.onValueChange(e)}
            autoComplete="off"
            label="验证码"
            id="verificationCode"
            placeholder="请输入验证码"
            defaultValue=""
            showPasswordEye
          />
        )}
        {activeKey === "2" && (
          <span
            onClick={this.getVerificationCode}
            style={{
              position: "absolute",
              right: 15,
              top: 9,
              cursor: "pointer",
              color: "#5365EA",
            }}
          >
            获取验证码
          </span>
        )}
      </div>
    );
  }
}

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
    };
  }

  onChange(key) {
    if (key === this.state.activeKey) {
      return;
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
  submit() {
    document.getElementById("myForm").submit();
  }
  getVerificationCode() {
    if (this.state.time > 0) {
      return;
    }
    this.setState(
      {
        time: 5,
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
    let phone = document.getElementById("phoneInput").value;
    fetch(
      `http://172.23.16.154:30094/oauth/public/send-phone-captcha?phone=${phone}`
    )
      .then(function (response) {
        return response.json();
      })
      .then(function (res) {
        console.log(res);
        let captchaKeyInput = $("#captchaKeyInput");
        captchaKeyInput.attr("value", res.captchaKey);
      });
  }
  render() {
    return (
      <div>
        <div id="loginTypeTabs">
          <span
            className="tabs tabs-active"
            onClick={() => {
              this.onChange("1");
            }}
            style={{ marginRight: 32 }}
          >
            账号密码
          </span>
          <span
            className="tabs"
            onClick={() => {
              this.onChange("2");
            }}
          >
            手机验证码
          </span>
        </div>

        <div>
          <Form
            className={+this.state.activeKey === 2 ? "phone-login-form" : ""}
            id="myForm"
            method="post"
            action={this.state.action}
            columns={3}
            labelLayout="float"
          >
            {this.state.activeKey === "1" && (
              <TextField
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

            {this.state.activeKey === "2" && (
              <TextField
                id="phoneInput"
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
                    <span>{this.state.time}s后重新获取</span>
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
                  id="captchaKeyInput"
                  colSpan={3}
                  name="captchaKey"
                  required
                />
              </div>
            )}
            {/* 注册 忘记密码 */}

            <div
              newLine
              style={{ color: "#5365EA", textAlign: "right" }}
              colSpan={3}
            >
              <a href="./password/find">注册</a>
              <span
                style={{
                  marginLeft: 6,
                  marginRight: 6,
                }}
              >
                |
              </span>
              <a href="./password/find">忘记密码</a>
            </div>

            <div newLine colSpan={3}>
              <Button
                type="primary"
                className="btn"
                onClick={this.submit}
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
/**
 * 渲染账号和密码的输入框
 */
// ReactDOM.render(<LoginTypeTabs />, document.getElementById("loginTypeTabs"));
// ReactDOM.render(<UsernameInupt />, document.getElementById("usernameInupt"));
// ReactDOM.render(<PasswordInput />, document.getElementById("passwordInupt"));
// ReactDOM.render(<LoginButton />, document.getElementById("loginButton"));
ReactDOM.render(<Content />, document.getElementById("form-content"));
if (document.getElementById("captchaInupt"))
  ReactDOM.render(
    <Input
      label="验证码"
      type="text"
      style={{ width: "200px" }}
      name="captcha"
    />,
    document.getElementById("captchaInupt")
  );
