const {Input, Button, Form, Icon, message} = window['choerodon-ui.min'];

// const server = 'http://api.staging.saas.hand-china.com'; //本地测试的时候打开此注释
const server = '';
const keyStr = "ABCDEFGHIJKLMNOP" + "QRSTUVWXYZabcdef" + "ghijklmnopqrstuv"
  + "wxyz0123456789+/" + "=";
const formItemLayout = {
  labelCol: {
    xs: {span: 24},
    sm: {span: 100},
  },
  wrapperCol: {
    xs: {span: 24},
    sm: {span: 9},
  },
};
const FormItem = Form.Item;

class App extends window.React.Component {
  state = {
    // 邮箱名
    currentUsername: '',
    step: 1,
    account: {},
    currentVCode: '',
    vCode: {},
    confirmDirty: false,
    passwdPolicy: {},
    policyPassed: false,
    passwdCheckedResult: '',
    errorMsg: '',
    errorState: false,
    captchaCD: 0,
  };

  componentDidMount() {
    if ($("#is_success").val() === 'false') {
      this.setState({
        step: 3
      })
    }
  }

  checkPassword = (passwordPolicy, value, userName) => {
    let compareResult = '';
    if (passwordPolicy) {
      const {
        enablePassword: check, minLength, maxLength,
        uppercaseCount: upcount, specialCharCount: spcount,
        lowercaseCount: lowcount, notUsername: notEqualsUsername,
        regularExpression: regexCheck, digitsCount
      } = passwordPolicy;
      if (value && (check)) {
        let len = 0;
        let rs = '';
        let sp;
        let up = 0;
        let low = 0;
        let numLen = 0;
        let space = 0;
        for (let i = 0; i < value.length; i += 1) {
          const a = value.charAt(i);
          if (a.match(/[^\x00-\xff]/ig) != null) {
          len += 2;
          } else {
            len += 1;
          }
        }
        const pattern = new RegExp('[-~`@#$%^&*_=+|/()<>,.;:!]');
        for (let i = 0; i < value.length; i += 1) {
          rs += value.substr(i, 1).replace(pattern, '');
          sp = value.length - rs.length;
        }
        if(/[\d]/.test(value)) {
          const num = value.match(/\d/g);
          numLen = num ? num.length : 0;
        }
        if (/[A-Z]/i.test(value)) {
          const ups = value.match(/[A-Z]/g);
          up = ups ? ups.length : 0;
        }
        if (/[a-z]/i.test(value)) {
          const lows = value.match(/[a-z]/g);
          low = lows ? lows.length : 0;
        }
        if (minLength && (len < minLength)) {
          compareResult = `密码长度至少为${minLength}`;
        }
        else if (maxLength && (len > maxLength)) {
          compareResult = `密码长度最多为${maxLength}`;
        }
        else if (upcount && (up < upcount)) {
          compareResult = `大写字母至少为${upcount}`;
        }
        else if (lowcount && (low < lowcount)) {
          compareResult = `小写字母至少为${lowcount}`;
        }
        else if (notEqualsUsername && value === userName) {
          compareResult = '密码不能与账号相同';
        }
        else if (digitsCount && (numLen < digitsCount)) {
          compareResult = `数字至少为${digitsCount}个`;
        }
        else if (spcount && (sp < spcount)) {
          compareResult = `特殊字符至少为${spcount}`;
        }
        else if (regexCheck) {
          const regex = new RegExp(regexCheck);
          if (!regex.test(value)) {
            compareResult = '正则不匹配';
          }
        }
      }
    }
    return compareResult;
  };

  handleButtonClick = () => {
    const {form} = this.props;
    const {step, policyPassed} = this.state;
    const url = window.location.pathname;
    const token = url.substring(url.lastIndexOf('/') + 1, url.length)
    if (step === 1) {
      form.validateFields(['password'], {force: true});
      form.validateFields(['password1'], {force: true});
    }
    if (step === 1 && form.getFieldValue('password') === form.getFieldValue('password1') && policyPassed) {
      form.validateFields(['password'], {force: true});
      form.validateFields(['password1'], {force: true});
      $.post(`${server}/oauth/choerodon/password/update_password`,{
        token: token,
        password: this.encryptPwd(form.getFieldValue('password'))
      } ,(results) => {
      console.log(results)
        if (results && results.success === true) {
          this.setState({
            step: 2,
            email: results.user.email
          });
        } else if(results.failed === true || results.success === false) {
          this.setState({
            errorMsg: results.message,
            errorState: true,
          });
          form.validateFields(['password'], {force: true});
        }
      }).fail(err => {
        message.error('服务器请求失败');
      });
    }

    if (step === 2) {
      const { email } = this.state;
      // const encodePasswd = this.encode(password);
      // $.post(`${server}/oauth/login?username=${currentUsername}&password=${encodePasswd}`)
      window.location.href = `/oauth/choerodon/login?username=${email}`;
    }

  }
  encryptPwd = (password) => {
    var publickey = $('#templateData').data('publickey');
    /* 有公钥 使用 rsa 加密, 否则使用 md5 加密 */
    if (publickey) {
      // 初始化加密器
      var encrypt = new JSEncrypt(); // 设置公钥

      encrypt.setPublicKey(publickey); // 加密

      return encrypt.encrypt(password);
    } else {
      return this.encryptMd5(password);
    }
  }
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
      enc2 = (chr1 & 3) << 4 | chr2 >> 4;
      enc3 = (chr2 & 15) << 2 | chr3 >> 6;
      enc4 = chr3 & 63;

      if (isNaN(chr2)) {
        enc3 = enc4 = 64;
      } else if (isNaN(chr3)) {
        enc4 = 64;
      }

      output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2) + keyStr.charAt(enc3) + keyStr.charAt(enc4);
      chr1 = chr2 = chr3 = "";
      enc1 = enc2 = enc3 = enc4 = "";
    } while (i < password.length);

    return output;
  }

  encode = (password) => {
    var output = "";
    var chr1, chr2, chr3 = "";
    var enc1, enc2, enc3, enc4 = "";
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
      output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2)
        + keyStr.charAt(enc3) + keyStr.charAt(enc4);
      chr1 = chr2 = chr3 = "";
      enc1 = enc2 = enc3 = enc4 = "";
    } while (i < password.length);
    return output;
  }

  compareToFirstPassword = (rule, value, callback) => {
    const form = this.props.form;
    if (value && value !== form.getFieldValue('password')) {
      callback('您输入的密码与确认密码不一致!');
    } else {
      callback();
    }
  };

  validateToNextPassword = (rule, value, callback) => {
    const form = this.props.form;
    const { passwdPolicy, currentUsername, errorState, errorMsg } = this.state;
    let checkPasswdMsg = this.checkPassword(passwdPolicy, value, currentUsername);
    if (/ /.test(value)) {
      callback('密码中不能包含空格')
    }
    if (value && this.state.confirmDirty) {
      form.validateFields(['password1'], {force: true});
    }
    if(checkPasswdMsg || errorState) {
      this.setState({
        policyPassed: false,
        errorState: false,
      });
      callback(checkPasswdMsg ? checkPasswdMsg : errorMsg);
    }
    else {
      this.setState({
        policyPassed: true,
      });
      callback();
    }
  }

  handleConfirmBlur = (e) => {
    const value = e.target.value;
    this.setState({confirmDirty: this.state.confirmDirty || !!value});
  }

  renderStep1 = () => {
    const {form} = this.props;
    const {getFieldDecorator} = form;
    return (
      <div>
        <span className="loginSpan">修改默认密码</span>
        <Form layout="vertical" className="form-vertical login-form">
          <FormItem
            {...formItemLayout}
            label="新密码"
          >
            {getFieldDecorator('password', {
              rules: [{
                required: true, message: '请输入新密码',
              }, {
                validator: this.validateToNextPassword,
              }],
            })(
              <Input showPasswordEye label="新密码" type="password"/>
            )}
          </FormItem>
          <FormItem
            {...formItemLayout}
            label="确认密码"
          >
            {getFieldDecorator('password1', {
              rules: [{
                required: true, message: '请输入确认密码'
              },{
                validator: this.compareToFirstPassword,
              }],
            })(
              <Input showPasswordEye label="确认新密码" type="password" onBlur={this.handleConfirmBlur}/>
            )}
          </FormItem>
          <Button type="primary" funcType="raised" className="btn" onClick={this.handleButtonClick} loading={this.state.loading}
                  style={{paddingTop: '4px', marginTop: '38px'}}><span>确定</span></Button>
        </Form>
      </div>
    )
  }

  renderStep2 = () => {
    const { email } = this.state;
    return (
      <div>
        <div className="congratulation"><Icon type="done"
                                              style={{fontSize: 30, color: '#3F51B5', marginRight: '23.8px'}}/>恭喜
        </div>
        <div className="change-password-success">{`您的账号“${email}”修改密码成功`}</div>
        <Button type="primary" funcType="raised" className="btn" onClick={this.handleButtonClick} loading={this.state.loading}
                style={{paddingTop: '4px', marginTop: '80px'}}><span>直接登录</span></Button>
      </div>
    )
  }

  render() {
    const {step} = this.state;
    switch (step) {
      case 1:
        return this.renderStep1();
      case 2:
        return this.renderStep2();
    }
  }
}

App = Form.create({})(App);

ReactDOM.render(
  <App/>,
  document.getElementById('app'));