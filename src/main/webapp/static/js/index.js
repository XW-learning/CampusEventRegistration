/**
 * static/js/index.js
 * 首页的核心逻辑
 */
const USER_API_URL = 'user'; // 对应 UserServlet
const EVENT_API_URL = 'event-action'; // 对应 EventServlet (用于加载活动列表)

$(document).ready(function() {
    // 1. 检查登录状态
    checkLoginStatus();

    // 2. 加载活动列表 (这是你上一个功能做的，保留在这里)
    loadEventList();
});

/**
 * 检查用户是否已登录，并更新右上角按钮
 */
function checkLoginStatus() {
    $.ajax({
        url: USER_API_URL,
        type: 'POST',
        data: { action: 'check_login' },
        dataType: 'json',
        success: function(res) {
            if (res.status === 'success' && res.data) {
                // 已登录：显示欢迎信息和退出按钮
                updateHeaderLoggedIn(res.data);
            } else {
                // 未登录：保持默认显示 (登录/注册按钮)
                // 也可以显式调用 updateHeaderGuest() 确保状态正确
            }
        },
        error: function(xhr) {
            console.log("检查登录状态失败，视为未登录");
        }
    });
}

/**
 * 更新头部 UI - 已登录状态
 * @param {Object} user 用户信息
 */
function updateHeaderLoggedIn(user) {
    const userArea = $('#user-area');
    const displayName = user.realName || user.username;

    // 使用我们刚刚在 index.css 中定义的 class
    const html = `
        <div class="flex items-center">
            <a href="publish.html" class="hidden md:inline-block px-5 py-2 bg-blue-500 hover:bg-blue-600 text-white text-sm font-bold rounded-full transition shadow-md mr-6">
                + 发布活动
            </a>
            
            <div class="user-logged-in-box">
                <span class="welcome-text">
                    欢迎您，<span class="username-highlight">${displayName}</span>
                </span>
                <button onclick="doLogout()" class="logout-btn">
                    退出系统
                </button>
            </div>
        </div>
    `;

    userArea.html(html);
}

/**
 * 执行退出登录
 */
function doLogout() {
    if(!confirm("确定要退出登录吗？")) return;

    $.ajax({
        url: USER_API_URL,
        type: 'POST',
        data: { action: 'logout' },
        dataType: 'json',
        success: function(res) {
            if (res.status === 'success') {
                // 退出成功，刷新页面，恢复到未登录状态
                window.location.reload();
            } else {
                alert("退出失败：" + res.message);
            }
        }
    });
}

// ... 下面是你之前写的加载活动列表的 loadEventList 等函数 ...
// 请把你之前在 index.js 里写的 loadEventList 代码粘贴在下面
// 如果没有，就先留个空的:
function loadEventList() {
    // 复用你之前写的活动加载逻辑
    // ...
}