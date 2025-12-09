/**
 * static/js/index.js
 * 首页的核心逻辑
 */
const USER_API_URL = 'user';
const EVENT_API_URL = 'event-action';
const REG_API_URL = 'registration-action';
// 全局变量：记录当前登录用户
let currentUser = null;

$(document).ready(function () {
    // 1. 检查登录状态
    checkLoginStatus();
    // 2. 加载活动列表
    loadEventList();
});

/**
 * 检查用户是否已登录，并更新右上角按钮
 */
function checkLoginStatus() {
    $.ajax({
        url: USER_API_URL,
        type: 'POST',
        data: {action: 'check_login'},
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success' && res.data) {
                currentUser = res.data; // ★ 核心：保存用户信息到全局变量
                updateHeaderLoggedIn(res.data);
                // 自动预填弹窗里的姓名 (优化体验)
                $('#reg-name').val(currentUser.realName || currentUser.username);
            }
        }
    });
}

/**
 * 更新头部 UI - 已登录状态
 * @param {Object} user 用户信息 (包含 role 字段)
 */
function updateHeaderLoggedIn(user) {
    const userArea = $('#user-area');
    const displayName = user.realName || user.username;

    // 🟢 关键修改：根据角色判断是否生成“发布活动”按钮
    // 只有当 user.role 是 'organizer' 时，才生成这个按钮的 HTML
    let publishBtnHtml = '';

    // 注意：这里要跟数据库里存的字符串完全一致 (比如 'organizer')
    if (user.role === 'organizer') {
        publishBtnHtml = `
            <a href="publish.html" class="hidden md:inline-block px-5 py-2 bg-blue-500 hover:bg-blue-600 text-white text-sm font-bold rounded-full transition shadow-md mr-6">
                + 发布活动
            </a>
        `;
    }

    // 拼接最终 HTML
    const html = `
        <div class="flex items-center">
            ${publishBtnHtml}
            
            <div class="user-logged-in-box">
                <span class="welcome-text">
                    欢迎您，<span class="username-highlight">${displayName}</span>
                    <span class="text-xs text-gray-400 bg-gray-100 px-2 py-0.5 rounded ml-1">
                        ${user.role === 'organizer' ? '组织者' : '学生'}
                    </span>
                </span>
                <button onclick="doLogout()" class="logout-btn ml-2">
                    退出
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
    if (!confirm("确定要退出登录吗？")) return;

    $.ajax({
        url: USER_API_URL,
        type: 'POST',
        data: {action: 'logout'},
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success') {
                // 退出成功，刷新页面，恢复到未登录状态
                window.location.reload();
            } else {
                alert("退出失败：" + res.message);
            }
        }
    });
}

/**
 * 加载活动列表并渲染
 */
function loadEventList() {
    const container = $('#event-container');

    // 显示加载中提示
    container.html('<p class="text-gray-500 text-center col-span-3 py-10">正在加载精彩活动...</p>');

    $.ajax({
        url: EVENT_API_URL,
        type: 'GET',
        data: {action: 'list'}, // 告诉后端我们要 list
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success' && res.data) {
                renderEvents(res.data);
            } else {
                container.html('<p class="text-gray-500 text-center col-span-3 py-10">暂无活动信息</p>');
            }
        },
        error: function () {
            container.html('<p class="text-red-500 text-center col-span-3 py-10">加载失败，请检查网络</p>');
        }
    });
}

/**
 * 将数据渲染为 HTML 卡片
 */
function renderEvents(events) {
    const container = $('#event-container');
    container.empty();

    if (events.length === 0) {
        container.html('<p class="text-gray-500 text-center col-span-3 py-10">还没有发布任何活动，快去发布一个吧！</p>');
        return;
    }

    events.forEach(function (event) {
        // 1. 格式化三个时间字段 (截取前16位: "yyyy-MM-dd HH:mm")
        const startStr = event.startTime ? event.startTime.substring(0, 16) : '待定';
        const endStr = event.endTime ? event.endTime.substring(0, 16) : '待定';
        const deadlineStr = event.regDeadline ? event.regDeadline.substring(0, 16) : '待定';

        // 随机封面图
        const imageUrl = `https://picsum.photos/seed/${event.eventId}/400/250`;

        const html = `
            <div class="transform-gpu bg-white rounded-xl shadow-sm hover:shadow-lg transition-all duration-300 hover:-translate-y-1 overflow-hidden group border border-gray-100">
                
                <div class="h-48 w-full relative overflow-hidden">
                    <img src="${imageUrl}" alt="活动封面" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110">
                    <span class="absolute top-2 right-2 bg-white/90 backdrop-blur-sm text-blue-600 text-xs font-bold px-2 py-1 rounded shadow-sm">
                        ${event.category || '活动'}
                    </span>
                </div>

                <div class="p-5">
                    <h3 class="text-lg font-bold text-gray-800 mb-3 truncate" title="${event.title}">
                        ${event.title}
                    </h3>
                    
                    <div class="space-y-2 text-sm text-gray-600">
                        <p class="flex items-center">
                            <span class="mr-2 text-blue-500">📅</span> 
                            <span class="font-medium mr-1">活动开始时间：</span> ${startStr}
                        </p>
                        <p class="flex items-center">
                            <span class="mr-2 text-blue-500">🏁</span> 
                            <span class="font-medium mr-1">活动结束时间：</span> ${endStr}
                        </p>
                        <p class="flex items-center text-red-500">
                            <span class="mr-2">⏰</span> 
                            <span class="font-medium mr-1">截止报名时间：</span> ${deadlineStr}
                        </p>
                        <p class="flex items-center" title="${event.location}">
                            <span class="mr-2">📍</span>
                            <span class="font-medium mr-1">活动地点：</span> ${event.location}
                        </p>
                    </div>
                    <button onclick="openRegModal(${event.eventId})" class="w-full mt-4 bg-gray-50 text-blue-600 py-2 rounded-lg font-medium hover:bg-blue-600 hover:text-white transition-all duration-200 border border-blue-100 hover:border-blue-600 hover:shadow-md">
                        立即报名
                    </button>
                </div>
            </div>
        `;
        container.append(html);
    });
}

function openRegModal(eventId) {
    // A. 检查是否登录
    if (!currentUser) {
        if (confirm("您需要登录后才能报名活动。\n是否立即跳转到登录页面？")) {
            window.location.href = 'login.html';
        }
        return;
    }

    // B. 显示弹窗
    $('#reg-eventId').val(eventId); // 把活动ID存入隐藏域
    $('#reg-modal').removeClass('hidden'); // 显示 Modal

    // 给弹窗主体加个小动画 (如果你加了 css 的话)
    $('#reg-modal > div:last-child').addClass('fade-in');
}

// --- 3. 关闭弹窗 ---
function closeRegModal() {
    $('#reg-modal').addClass('hidden');
}

// --- 4. 提交报名 ---
function submitRegistration() {
    const eventId = $('#reg-eventId').val();
    const name = $('#reg-name').val().trim();
    const phone = $('#reg-phone').val().trim();

    // 简单校验
    if (!name || !phone) {
        alert("请务必填写真实姓名和联系电话，以便通知！");
        return;
    }

    // 发送请求
    $.ajax({
        url: REG_API_URL,
        type: 'POST',
        data: {
            action: 'register',
            eventId: eventId,
            contactName: name,   // 传给后端
            contactPhone: phone  // 传给后端
        },
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success') {
                closeRegModal();
                alert("🎉 " + res.message);
                // 可选：刷新列表或更改按钮状态
            } else {
                alert("❌ 报名失败：" + res.message);
                if (res.code === 'NOT_LOGIN') window.location.href = 'login.html';
            }
        },
        error: function () {
            alert("服务器网络错误，请稍后重试。");
        }
    });
}