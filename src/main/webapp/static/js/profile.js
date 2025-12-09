/**
 * static/js/profile.js
 * 个人中心页面的逻辑
 */
const USER_API_URL = 'user';
const EVENT_API_URL = 'event-action';
// 全局变量存储当前用户
let currentUser = null;

$(document).ready(function () {
    // 1. 检查登录并初始化页面
    checkLoginAndInit();
});

function checkLoginAndInit() {
    $.ajax({
        url: USER_API_URL,
        type: 'POST',
        data: {action: 'check_login'},
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success' && res.data) {
                currentUser = res.data;

                // A. 渲染顶部导航的用户信息 (复用 index.js 的逻辑，这里简单写一下或引入 index.js)
                renderHeader(currentUser);

                // B. 渲染左侧个人信息表单
                renderProfileForm(currentUser);

                // C. 根据角色处理 Tab
                if (currentUser.role === 'organizer') {
                    $('#tab-published').removeClass('hidden');
                }

                // D. 默认加载“我报名的”列表 (这里先留空，等后端接口)
                loadJoinedEvents();

            } else {
                alert("请先登录！");
                window.location.href = 'login.html';
            }
        }
    });
}

// 渲染左侧表单
function renderProfileForm(user) {
    // 头像文字
    const firstChar = (user.realName || user.username).charAt(0).toUpperCase();
    $('#avatar-text').text(firstChar);

    // 基本信息
    $('#display-name').text(user.realName || user.username);
    $('#role-badge').text(user.role === 'organizer' ? '活动组织者' : '在校学生');

    // 表单填值
    $('#p-username').val(user.username);
    $('#p-realName').val(user.realName);
    $('#p-phone').val(user.phone);
    $('#p-email').val(user.email);
}

// 简单的头部渲染 (因为 profile.html 没引 index.js)
function renderHeader(user) {
    const html = `
        <div class="flex items-center text-sm">
            <span class="mr-4 text-gray-600">欢迎，<span class="font-bold text-blue-600">${user.realName || user.username}</span></span>
            <a href="index.html" class="text-blue-500 hover:underline">返回首页</a>
        </div>
    `;
    $('#user-area').html(html);
}

// 切换 Tab 逻辑
function switchTab(type) {
    // 1. 处理按钮样式
    $('.tab-btn').removeClass('active');
    if (type === 'joined') {
        $('.tab-btn:first-child').addClass('active');
        $('#content-joined').removeClass('hidden');
        $('#content-published').addClass('hidden');
        loadJoinedEvents(); // 重新加载数据
    } else {
        $('#tab-published').addClass('active');
        $('#content-joined').addClass('hidden');
        $('#content-published').removeClass('hidden');
        loadPublishedEvents(); // 重新加载数据
    }
}

// 保存修改 (前端逻辑准备好了，等待后端 update_profile 接口)
function updateProfile() {
    const realName = $('#p-realName').val().trim();
    const phone = $('#p-phone').val().trim();
    const email = $('#p-email').val().trim();

    if (!realName) {
        alert("真实姓名不能为空");
        return;
    }

    $.ajax({
        url: USER_API_URL,
        type: 'POST',
        data: {
            action: 'update_profile',
            realName: realName,
            phone: phone,
            email: email
        },
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success') {
                alert("✅ " + res.message);
                // 更新页面上的显示名字
                $('#display-name').text(realName);
                // 更新头像字
                $('#avatar-text').text(realName.charAt(0).toUpperCase());
            } else {
                alert("❌ " + res.message);
            }
        }
    });
}

// 2. 加载列表通用函数
function loadEvents(type, containerId) {
    const container = $('#' + containerId);
    container.html('<p class="text-center text-gray-500 py-10">正在加载数据...</p>');

    $.ajax({
        url: EVENT_API_URL,
        type: 'GET',
        data: {
            action: 'my_events',
            type: type
        },
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success' && res.data && res.data.length > 0) {
                renderEventList(res.data, container, type);
            } else {
                container.html('<div class="text-center py-10 text-gray-400 bg-white rounded-lg border border-dashed border-gray-200">暂无相关活动记录</div>');
            }
        },
        error: function () {
            container.html('<p class="text-center text-red-500 py-10">加载失败，请刷新重试</p>');
        }
    });
}

// 3. 渲染列表 HTML
function renderEventList(events, container, type) {
    let html = '';
    events.forEach(event => {
        // 简单的状态判断 (根据时间)
        const now = new Date().getTime();
        const end = new Date(event.endTime).getTime();
        const isFinished = now > end;
        const statusBadge = isFinished
            ? '<span class="text-gray-400 bg-gray-100 px-2 py-1 rounded text-xs">已结束</span>'
            : '<span class="text-green-600 bg-green-50 px-2 py-1 rounded text-xs">进行中</span>';

        // 针对组织者的操作按钮 (查看报名名单)
        let actionBtns = '';
        if (type === 'published') {
            // 注意：这里留了一个 onclick 接口，后面我们会实现查看名单的功能
            actionBtns = `
                <button onclick="viewRegistrations(${event.eventId})" class="text-blue-600 hover:text-blue-800 text-sm font-medium border border-blue-200 hover:border-blue-600 px-3 py-1 rounded transition-colors">
                    📋 查看报名名单
                </button>
            `;
        } else {
            // 针对报名者的按钮
            actionBtns = `<span class="text-gray-400 text-sm">已报名</span>`;
        }

        html += `
            <div class="event-item-row group hover:border-blue-200 transition-colors">
                <div class="w-32 h-24 bg-gray-100 rounded-lg flex-shrink-0 mr-4 overflow-hidden relative">
                    <img src="https://picsum.photos/seed/${event.eventId}/200/150" class="w-full h-full object-cover">
                </div>
                <div class="flex-grow min-w-0"> <h4 class="font-bold text-gray-800 text-lg truncate group-hover:text-blue-600 transition-colors">${event.title}</h4>
                    <div class="text-sm text-gray-500 mt-2 space-y-1">
                        <p>📅 ${event.startTime ? event.startTime.substring(0, 16) : '待定'}</p>
                        <p>📍 ${event.location}</p>
                    </div>
                </div>
                <div class="flex flex-col items-end justify-between ml-4 py-1 h-24">
                    ${statusBadge}
                    ${actionBtns}
                </div>
            </div>
        `;
    });
    container.html(html);
}

function loadJoinedEvents() {
    loadEvents('joined', 'content-joined');
}

function loadPublishedEvents() {
    loadEvents('published', 'content-published');
}

// 5. 预留：查看报名名单功能 (给下一个功能模块用)
function viewRegistrations(eventId) {
    alert("正在开发中... 即将跳转到活动 [" + eventId + "] 的报名名单页");
}