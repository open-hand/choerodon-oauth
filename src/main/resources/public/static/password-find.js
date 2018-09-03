const {Input, Button, Form, Icon} = window['choerodon-ui'];

const server = 'http://api.staging.saas.hand-china.com'; //本地测试的时候打开此注释
//const server = '';
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
    currentUsername: '',
    step: 1,
    account: {},
    currentVCode: '',
    vCode: {},
    confirmDirty: false,
    captchaCD: 0,
  };

  componentDidMount() {
    this.setState({
      currentUsername: '',
      currentVCode: '',
    })
  }


  handleCaptchaButtonClick = () => {
    const {currentUsername, captchaCD} = this.state;

    if (currentUsername === '') {
      this.setState({
        account: {
          ...this.validateAccount(false),
        },
      });
      return;
    }
    $.post(`${server}/oauth/password/check_disable?emailAddress=${currentUsername}`, (results) => {
      this.setState({
        account: {
          ...this.validateAccount(results),
        },
      });
      if (results.success === true) {
        $.post(`${server}/oauth/password/send?emailAddress=${currentUsername}`, (results2) => {
          this.setState({
            account: {
              ...this.validateAccount(results2),
            },
          });
        });
      } else if (results.disableTime !== null){
        this.setState({
          captchaCD: results.disableTime - Math.round(new Date() / 1000),
        },() => {
          const timer = setInterval(() => {
            this.setState({
              captchaCD: this.state.captchaCD - 1,
            }, () => {
              if(this.state.captchaCD <=0 )
                this.clearTimer(timer)
            });
          }, 1000)
        });
      }
    });
  }

  clearTimer = (timer) => {
    this.setState({
      captchaCD: 0,
    })
    clearInterval(timer)
  }

  handleValueChange = (e) => {
    this.setState({
      currentUsername: e.target.value
    })
  }

  handleCodeChange = (e) => {
    this.setState({
      currentVCode: e.target.value
    })
  }

  componentDidUpdate() {

  }

  validateAccount = (results) => {
    const { captchaCD : CD } = this.state;
    if (!results) {
      return {
        validateStatus: 'error',
        errorMsg: '请输入用户邮箱',
      };
    }
    if (results.success && results.user) {
      this.setState({
        captchaCD: 60,
      },() => {
        const timer = setInterval(() => {
          this.setState({
            captchaCD: this.state.captchaCD - 1,
          }, () => {
            if(this.state.captchaCD <=0 )
              this.clearTimer(timer)
          });
        }, 1000)
      });
      return {
        validateStatus: 'success',
        errorMsg: '验证码发送成功',
      };
    }
    return {
      validateStatus: 'error',
      errorMsg: results.msg,
    };
  }

  validateCode = (results) => {
    if (!results) {
      return {
        validateStatus: 'error',
        errorMsg: '',
      };
    }
    if (results.success) {
      this.setState({
        step: 2,
      })
      return {
        validateStatus: 'success',
        errorMsg: 'passed',
      };
    }
    return {
      validateStatus: 'error',
      errorMsg: '验证码错误',
    };
  }

  handleButtonClick = () => {
    const {form} = this.props;
    const {step, currentUsername, currentVCode, userId} = this.state;
    if (step === 1) {
      $.post(`${server}/oauth/password/check?emailAddress=${currentUsername}&captcha=${currentVCode}`, (results) => {
        this.setState({
          vCode: {
            ...this.validateCode(results),
          },
          userId: results.user.id,
          loginName: results.user.loginName,
        });
      });
    }
    if (step === 2 && form.getFieldValue('password') === form.getFieldValue('password1')) {
      $.post(`${server}/oauth/password/reset?userId=${userId}&emailAddress=${currentUsername}&captcha=${currentVCode}&password=${form.getFieldValue('password')}&password1=${form.getFieldValue('password1')}`, (results) => {
        if (results && results.success === true) {
          this.setState({
            step: 3,
            password: form.getFieldValue('password'),
          });
        }
      });
    }

    if (step === 3) {
      const { loginName } = this.state;
      // const encodePasswd = this.encode(password);
      // $.post(`${server}/oauth/login?username=${currentUsername}&password=${encodePasswd}`)
      window.location.href = `/oauth/login?username=${loginName}`;
    }

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
  }

  validateToNextPassword = (rule, value, callback) => {
    const form = this.props.form;
    if (value && this.state.confirmDirty) {
      form.validateFields(['password1'], {force: true});
    }
    callback();
  }

  handleConfirmBlur = (e) => {
    const value = e.target.value;
    this.setState({confirmDirty: this.state.confirmDirty || !!value});
  }


  renderStep1 = () => {
    const {form} = this.props;
    const {account, vCode, captchaCD} = this.state;
    const {getFieldDecorator} = form;
    return (
      <div>
        <span className="loginSpan">忘记密码</span>
        <Form layout="vertical" className="form-vertical login-form">
          <FormItem
            {...formItemLayout}
            validateStatus={account.validateStatus}
            help={account.errorMsg}
          >
            {getFieldDecorator('username', {
              rules: [{
                required: true,
                message: '请输入邮箱',
              }],
            })(
              <Input autoFocus autoComplete="off" label="登录邮箱" name="username" id="username"
                     onChange={e => this.handleValueChange(e)} placeholder="请输入邮箱" value={this.state.currentUsername}
              />
            )}
          </FormItem>
          <FormItem
            {...formItemLayout}
            validateStatus={vCode.validateStatus}
            help={vCode.errorMsg}
          >
            {getFieldDecorator('captchaInput', {
              rules: [{
                required: true,
                message: '请输入验证码',
              }],
            })(
              <div>
                <Input type="text" style={{width: '237px'}} autoComplete="off" label="验证码" id="captchaInput"
                       onChange={e => this.handleCodeChange(e)} placeholder="请输入验证码" value={this.state.currentVCode}
                />
                <Button funcType="raised" onClick={this.handleCaptchaButtonClick} loading={captchaCD > 0}
                        style={{float: 'right'}}>{captchaCD === 0 ? '发送验证码' : `${captchaCD}秒后重试`}</Button>
              </div>
            )}
          </FormItem>
          <Button className="btn" onClick={this.handleButtonClick} loading={this.state.loading}
                  style={{paddingTop: '4px', marginTop: '38px'}}><span>下一步</span></Button>
        </Form>
      </div>
    )
  }

  renderStep2 = () => {
    const {form} = this.props;
    const {getFieldDecorator} = form;
    return (
      <div>
        <span className="loginSpan">忘记密码</span>
        <Form layout="vertical" className="form-vertical login-form">
          <FormItem
            {...formItemLayout}
            label="新密码"
          >
            {getFieldDecorator('password', {
              rules: [{
                required: true, message: '请输入密码',
              },{
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
                required: true, message: '请确认!',
              }, {
                validator: this.compareToFirstPassword,
              }],
            })(
              <Input showPasswordEye label="确认新密码" type="password" onBlur={this.handleConfirmBlur}/>
            )}
          </FormItem>
          <Button className="btn" onClick={this.handleButtonClick} loading={this.state.loading}
                  style={{paddingTop: '4px', marginTop: '38px'}}><span>下一步</span></Button>
        </Form>
      </div>
    )
  }

  renderStep3 = () => {
    const { loginName } = this.state;
    return (
      <div>
        <div className="congratulation"><Icon type="done"
                                              style={{fontSize: 30, color: '#3F51B5', marginRight: '23.8px'}}/>恭喜
        </div>
        <div className="change-password-success">{`您的账号“${loginName}”重置密码成功`}</div>
        <Button className="btn" onClick={this.handleButtonClick} loading={this.state.loading}
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
      case 3:
        return this.renderStep3();
    }
  }
}

App = Form.create({})(App);

ReactDOM.render(
  <App/>,
  document.getElementById('app'));