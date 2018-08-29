const {Input, Button} = window['choerodon-ui'];

class CheckButton extends window.React.Component {
  state = {
    loading: false,
  };

  constructor(props) {
    super(props);
  }

  handleButtonClickTest = (e) => {

  };


  render() {
    return (
      <Button className="btn" onClick={this.handleButtonClickTest} loading={this.state.loading}
              htmlStyle="padding-top:4px"><span>下一步</span></Button>
    )
  }
}

class UsernameInupt extends window.React.Component {
  state = {
    currentUsername: '',
  };

  componentDidMount() {
    this.setState({
      currentUsername: '',
    })
  }

  onValueChange = (e) => {
    this.setState({
      currentUsername: e.target.value
    })
  }

  render() {
    return (
      <Input autoFocus autoComplete="off" label="登陆账号*" name="username" id="username"
             onChange={e => this.onValueChange(e)} placeholder="登录名/邮箱" value={this.state.currentUsername}/>
    )
  }
}

ReactDOM.render(
  <UsernameInupt/>,
  document.getElementById('usernameInupt'));
ReactDOM.render(
  <div>
    <Input type="text" style={{width: '241px'}} autoComplete="off" label="验证码*" id="password" placeholder="请输入验证码"/>
    <Button funcType="raised" style={{ marginLeft: '33px'}}>发送验证码</Button>
  </div>,
  document.getElementById('vertifyCodeInupt'));
ReactDOM.render(
  <CheckButton/>,
  document.getElementById('loginButton'));