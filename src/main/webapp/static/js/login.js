// src/main/webapp/static/js/login.js
const LOGIN_API_URL = 'user'
/**
 * 切换视图模式
 * @param mode 'login' | 'register'
 */
function switchMode(mode) {
    if (mode === 'register') {
        $('#login-box').fadeOut(200, function () {
            $('#register-box').fadeIn(200);
        });
    } else {
        $('#register-box').fadeOut(200, function () {
            $('#login-box').fadeIn(200);
        });
    }
}

/**
 * 执行登录逻辑
 */
function doLogin() {
    const username = $('#login-username').val().trim();
    const password = $('#login-password').val().trim();

    if (!username || !password) {
        alert("请填写完整的账号和密码！");
        return;
    }

    $.ajax({
        url: LOGIN_API_URL,
        type: 'POST',
        data: {
            action: 'login',
            username: username,
            password: password
        },
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success') {
                window.location.href = 'index.html';
            } else {
                alert("登录失败：" + res.message);
            }
        },
        error: function (xhr, status, error) {
            console.error(error);
            alert("服务器连接错误。");
        }
    });
}

/**
 * 执行注册逻辑
 */
function doRegister() {
    const username = $('#reg-username').val().trim();
    const realName = $('#reg-realname').val().trim();
    const phone = $('#reg-phone').val().trim();
    const password = $('#reg-password').val().trim();
    const role = $('input[name="role"]:checked').val();

    if (!username || !password || !realName) {
        alert("带 * 号的字段不能为空！");
        return;
    }

    $.ajax({
        url: LOGIN_API_URL,
        type: 'POST',
        data: {
            action: 'register',
            username: username,
            password: password,
            realName: realName,
            phone: phone,
            role: role
        },
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success') {
                alert("🎉 注册成功！请使用新账号登录。");
                $('#register-form')[0].reset();
                switchMode('login');
            } else {
                alert("注册失败：" + res.message);
            }
        },
        error: function () {
            alert("服务器连接错误。");
        }
    });
}