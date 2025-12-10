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
        // <-- 修改在这里：修正提示语，使用 error 类型
        showToast("请填写完整的账号和密码！", "error");
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
                // <-- 修改在这里：成功提示后延迟跳转
                showToast("🎉 登录成功！正在跳转...", "success");
                setTimeout(function() {
                    window.location.href = 'index.html';
                }, 500);
            } else {
                // <-- 修改在这里：修正为 "登录失败：" + 后端返回的具体错误信息
                showToast("登录失败：" + res.message, "error");
            }
        },
        error: function (xhr, status, error) {
            console.error(error);
            // <-- 修改在这里：服务器连接错误提示
            showToast("服务器连接错误，请稍后重试。", "error");
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
        // <-- 修改在这里：修正提示语
        showToast("带 * 号的字段不能为空！", "error");
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
                // <-- 修改在这里：注册成功提示
                showToast("🎉 注册成功！请使用新账号登录。", "success");
                $('#register-form')[0].reset();
                switchMode('login');
            } else {
                // <-- 修改在这里：修正为 "注册失败：" + 后端返回的具体错误信息
                showToast("注册失败：" + res.message, "error");
            }
        },
        error: function () {
            // <-- 修改在这里：服务器连接错误提示
            showToast("服务器连接错误，请稍后重试。", "error");
        }
    });
}