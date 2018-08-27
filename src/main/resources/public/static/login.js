const { Input, Button } = window['choerodon-ui'];

class LoginButton extends window.React.Component{
    state = {
        loading: false,
    };

    constructor(props) {
        super(props);
    }
    handleButtonClickTest = (e) => {
        this.setState({ loading: true })

        const promise = new Promise((resolve, reject) => {
            setTimeout(resolve, 2000)
        });

        promise.then(() => {
            this.setState({ loading: false })
        })
    };

    render() {
        return (
            <Button htmlType="button" className="btn" onClick={this.handleButtonClickTest} loading={this.state.loading}><span>{this.state.loading ? '登陆中' : '登陆'}</span></Button>
        )
    }
}

class UsernameInupt extends window.React.Component{
    state = {
        currentUsername: '',
    };

    componentDidMount() {
        this.setState({
            currentUsername: this.getUrlParams('username'),
        })
    }
    getUrlParams = (name) => {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i"); //定义正则表达式
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }
    onValueChange = (e) => {
        this.setState({
            currentUsername: e.target.value
        })
    }

    render() {
        return (
            <Input autoFocus autoComplete="off" label="登陆账号" name="username" id="username" onChange={e => this.onValueChange(e)} placeholder="登录名/邮箱" value={this.state.currentUsername}/>
        )
    }
}

function getParam(paramName) {
    let paramValue = "";
    let isFound = !1;
    if (this.location.search.indexOf("?") == 0 && this.location.search.indexOf("=") > 1) {
        let arrSource = unescape(this.location.search).substring(1, this.location.search.length).split("&"), i = 0;
        while (i < arrSource.length && !isFound) arrSource[i].indexOf("=") > 0 && arrSource[i].split("=")[0].toLowerCase() == paramName.toLowerCase() && (paramValue = arrSource[i].split("=")[1], isFound = !0), i++
    }
    return paramValue == "" && (paramValue = null), paramValue
}

/**
 * 渲染账号和密码的输入框
 */
ReactDOM.render(
    <UsernameInupt />,
    document.getElementById('usernameInupt'));
ReactDOM.render(
<div>
<Input type="password" autoComplete="off" label="密码" id="password" placeholder="请输入密码"/>
    </div>,
    document.getElementById('passwordInupt'));
ReactDOM.render(
<LoginButton/>,
    document.getElementById('loginButton'));