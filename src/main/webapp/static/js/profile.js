/**
 * static/js/profile.js
 * 个人中心页面的逻辑
 */
const USER_API_URL = 'user';

$(document).ready(function() {
    // 1. 检查登录并初始化页面
    checkLoginAndInit();
});

// 全局变量存储当前用户
let currentUser = null;

function checkLoginAndInit() {
    $.ajax({
        url: USER_API_URL,
        type: 'POST',
        data: { action: 'check_login' },
        dataType: 'json',
        success: function(res) {
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
    const realName = $('#p-realName').val();
    const phone = $('#p-phone').val();
    const email = $('#p-email').val();

    // 简单校验
    if(!realName) { alert("真实姓名不能为空"); return; }

    // 模拟提交
    alert("正在保存修改...\n(后端接口暂未实现，数据仅前端展示)");
    // TODO: 下一步实现 UserServlet 的 update action
}

// 加载我报名的活动 (占位)
function loadJoinedEvents() {
    const container = $('#content-joined');
    // 暂时用静态 HTML 演示效果
    container.html(`
        <div class="event-item-row">
            <div class="w-32 h-24 bg-gray-200 rounded flex-shrink-0 mr-4 flex items-center justify-center text-gray-400 text-xs">活动封面</div>
            <div class="flex-grow">
                <h4 class="font-bold text-gray-800 text-lg">示例：Java 编程大赛</h4>
                <p class="text-sm text-gray-500 mt-1">📅 2025-10-24 14:00</p>
                <p class="text-sm text-gray-500">📍 计算机学院报告厅</p>
            </div>
            <div class="flex flex-col items-end justify-center ml-4">
                <span class="bg-green-100 text-green-600 text-xs px-2 py-1 rounded mb-2">已报名</span>
                <button class="text-gray-400 text-sm hover:text-red-500 underline">取消报名</button>
            </div>
        </div>
    `);
}

// 加载我发布的活动 (占位)
function loadPublishedEvents() {
    const container = $('#content-published');
    container.html(`
        <div class="event-item-row">
            <div class="w-32 h-24 bg-blue-50 rounded flex-shrink-0 mr-4 flex items-center justify-center text-blue-300 text-xs">活动封面</div>
            <div class="flex-grow">
                <h4 class="font-bold text-gray-800 text-lg">示例：组织者发布的测试活动</h4>
                <p class="text-sm text-gray-500 mt-1">报名人数：<span class="text-blue-600 font-bold">12</span> / 50</p>
                <p class="text-sm text-gray-500">状态：<span class="text-green-600">进行中</span></p>
            </div>
            <div class="flex flex-col items-end justify-center ml-4 space-y-2">
                <button class="bg-blue-50 text-blue-600 text-xs px-3 py-1.5 rounded hover:bg-blue-100">管理名单</button>
                <button class="text-gray-400 text-xs hover:text-red-500">删除</button>
            </div>
        </div>
    `);
}