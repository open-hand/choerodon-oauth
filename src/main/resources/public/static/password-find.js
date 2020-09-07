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
    this.setState({
      currentUsername: '',
      currentVCode: '',
    })
  }

  handleValueChange = (e) => {
    this.setState({
      currentUsername: e.target.value
    })
    const p = /^\w+((.\w+)|(-\w+))@[A-Za-z0-9]+((.|-)[A-Za-z0-9]+).[A-Za-z0-9]+$/;
    if (e.target.value && !p.test(e.target.value)) {
      this.setState({
        account: {
          validateStatus: 'error',
          errorMsg: '请输入正确的邮箱格式',
        },
      })
    } else {
      this.setState({
        account: {},
      })
    }
  }

  handleButtonClick = () => {
    const {form} = this.props;
    const {step, currentUsername, policyPassed} = this.state;
    if (step === 1) {
      this.setState({ loading: true });
      form.validateFields(['username'], {force: true});
      $.post(`${server}/oauth/choerodon/password/send_reset_email`, {
        emailAddress: currentUsername,
      }, (results) => {
        if (results && results.success === true) {
          this.setState({
            step: 2,
          });
        } else if (results){
          this.setState({
            account: {
              validateStatus: 'error',
              errorMsg: results.message,
            },
          })
        }
        this.setState({ loading: false });
      });
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

  renderStep1 = () => {
    const {form} = this.props;
    const {account, currentUsername, loading} = this.state;
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
                whitespace: true,
                message: '请输入邮箱',
              }],
            })(
              <Input autoComplete="off" label="登录邮箱" name="username" id="username"
                     onChange={e => this.handleValueChange(e)} placeholder="请输入邮箱" value={currentUsername}
              />
            )}
          </FormItem>
          <FormItem style={{marginTop: '60px'}}>
            <Button type="primary" funcType="raised" className="btn" onClick={this.handleButtonClick} loading={loading}
                    style={{width: '120px',float: 'right', paddingTop: '4px'} } htmlType="submit"
                    disabled={account.validateStatus === 'error' || !currentUsername}><span>下一步</span></Button>
            <a className="back-to-login" href="/oauth/choerodon/login" style={{float: 'left'}}>返回登录</a>
          </FormItem>
        </Form>
      </div>
    )
  }

  renderStep2 = () => {
    return (
      <div>
        <div className="congratulation"><Icon type="done"
                                              style={{fontSize: 30, color: '#3F51B5', marginRight: '23.8px'}}/>发送成功
        </div>
        <div className="change-password-success">重置密码的链接已发送至您的邮箱，请尽快前往查收。该链接30分钟内有效。</div>
        <Button type="primary" funcType="raised" className="btn" href="/oauth/choerodon/login"  loading={this.state.loading}
                style={{width: '120px',float: 'right',paddingTop: '4px', marginTop: '80px'}}><span>我知道了</span></Button>
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
