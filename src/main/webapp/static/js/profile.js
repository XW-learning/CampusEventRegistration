/**
 * static/js/profile.js
 * 个人中心页面的逻辑
 */
const USER_API_URL = 'user';
const EVENT_API_URL = 'event-action';
const REG_API_URL = 'registration-action';
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
        // 1. 活动本身的时间状态
        const now = new Date().getTime();
        const end = new Date(event.endTime).getTime();
        const isEventFinished = now > end;

        const timeBadge = isEventFinished
            ? '<span class="text-gray-400 bg-gray-100 px-2 py-1 rounded text-xs">已结束</span>'
            : '<span class="text-blue-600 bg-blue-50 px-2 py-1 rounded text-xs">进行中</span>';

        let actionArea = '';

        if (type === 'published') {
            // --- 组织者视图 ---
            actionArea = `
                <button onclick="viewRegistrations(${event.eventId})" class="text-blue-600 hover:text-blue-800 text-sm font-medium border border-blue-200 hover:border-blue-600 px-3 py-1 rounded transition-colors">
                    📋 查看报名名单
                </button>
            `;
        } else {
            // --- 学生视图 (我的报名) ---
            // 获取后端传来的报名状态
            const status = event.registrationStatus || 'pending';

            // A. 生成状态标签
            let myStatusBadge = '';
            if (status === 'pending') {
                myStatusBadge = '<span class="text-yellow-600 bg-yellow-50 border border-yellow-200 px-2 py-1 rounded text-xs font-medium">⏳ 待审核</span>';
            } else if (status === 'approved') {
                myStatusBadge = '<span class="text-green-600 bg-green-50 border border-green-200 px-2 py-1 rounded text-xs font-medium">✅ 报名成功</span>';
            } else if (status === 'rejected') {
                myStatusBadge = '<span class="text-red-600 bg-red-50 border border-red-200 px-2 py-1 rounded text-xs font-medium">🚫 未通过</span>';
            } else if (status === 'cancelled') {
                myStatusBadge = '<span class="text-gray-400 bg-gray-100 border border-gray-200 px-2 py-1 rounded text-xs font-medium">⚪ 已取消</span>';
            }

            // B. 生成操作按钮 (只有未结束 且 未取消/未拒绝 的活动才能取消)
            let cancelBtn = '';
            if (!isEventFinished && status !== 'cancelled' && status !== 'rejected') {
                cancelBtn = `
                    <button onclick="cancelRegistration(${event.eventId})" class="text-red-500 hover:text-white border border-red-200 hover:bg-red-500 hover:border-red-500 text-xs px-3 py-1 rounded transition-all shadow-sm ml-2">
                        取消报名
                    </button>
                `;
            }

            // 组合显示
            actionArea = `
                <div class="flex items-center gap-2">
                    ${myStatusBadge}
                    ${cancelBtn}
                </div>
            `;
        }

        html += `
            <div class="event-item-row group hover:border-blue-200 transition-colors">
                <div class="w-32 h-24 bg-gray-100 rounded-lg flex-shrink-0 mr-4 overflow-hidden relative">
                    <img src="https://picsum.photos/seed/${event.eventId}/200/150" class="w-full h-full object-cover">
                </div>
                <div class="flex-grow min-w-0">
                    <h4 class="font-bold text-gray-800 text-lg truncate group-hover:text-blue-600 transition-colors">${event.title}</h4>
                    <div class="text-sm text-gray-500 mt-2 space-y-1">
                        <div class="flex items-center gap-2">
                            ${timeBadge}
                            <span class="text-xs text-gray-400">|</span>
                            <span>📅 ${event.startTime ? event.startTime.substring(0, 16) : '待定'}</span>
                        </div>
                        <p>📍 ${event.location}</p>
                    </div>
                </div>
                <div class="flex flex-col items-end justify-center ml-4 h-24">
                    ${actionArea}
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
    const modal = $('#list-modal');
    const tbody = $('#registration-table-body');
    const exportBtn = $('#export-btn');

    // 🟢 关键：把 eventId 存到隐藏域，供刷新使用
    $('#list-current-eventId').val(eventId);

    // 显示弹窗
    modal.removeClass('hidden');
    tbody.html('<tr><td colspan="5" class="text-center py-10 text-gray-400">正在加载数据...</td></tr>');

    // 绑定导出按钮
    exportBtn.off('click').click(function () {
        alert("数据加载中...");
    }).addClass('opacity-50 cursor-not-allowed');

    // 加载数据
    $.ajax({
        url: REG_API_URL,
        type: 'GET',
        data: {
            action: 'list_by_event',
            eventId: eventId
        },
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success') {
                const list = res.data;
                renderRegistrationList(list);

                // 更新导出按钮状态
                exportBtn.off('click').removeClass('opacity-50 cursor-not-allowed');
                if (list && list.length > 0) {
                    exportBtn.click(function () {
                        window.location.href = `${REG_API_URL}?action=export&eventId=${eventId}`;
                    });
                } else {
                    exportBtn.click(function () {
                        alert("暂无数据");
                    });
                }
            } else {
                tbody.html(`<tr><td colspan="5" class="text-center py-10 text-red-500">${res.message}</td></tr>`);
            }
        },
        error: function () {
            tbody.html('<tr><td colspan="5" class="text-center py-10 text-red-500">加载失败</td></tr>');
        }
    });
}

function renderRegistrationList(list) {
    const tbody = $('#registration-table-body');
    tbody.empty();
    $('#checkbox-all').prop('checked', false);

    if (!list || list.length === 0) {
        tbody.html('<tr><td colspan="5" class="text-center py-10 text-gray-400 flex flex-col items-center"><span class="text-2xl mb-2">🍃</span><span>暂无学生报名</span></td></tr>');
        return;
    }

    list.forEach(reg => {
        // 时间格式化
        let regTimeStr = '-';
        if (reg.regTime) {
            let date = new Date(reg.regTime.replace('T', ' ').replace(/-/g, '/'));
            if (!isNaN(date.getTime())) {
                const y = date.getFullYear();
                const m = (date.getMonth() + 1).toString().padStart(2, '0');
                const d = date.getDate().toString().padStart(2, '0');
                const h = date.getHours().toString().padStart(2, '0');
                const min = date.getMinutes().toString().padStart(2, '0');
                regTimeStr = `${y}年${m}月${d}日 ${h}:${min}`;
            } else {
                regTimeStr = reg.regTime.substring(0, 16);
            }
        }

        // 状态样式
        let statusHtml = '';
        if (reg.status === 'pending') {
            statusHtml = '<span class="inline-flex items-center gap-1 bg-yellow-50 text-yellow-700 text-xs font-semibold px-2.5 py-1 rounded-full border border-yellow-100"><span class="w-1.5 h-1.5 rounded-full bg-yellow-500"></span>待审核</span>';
        } else if (reg.status === 'approved') {
            statusHtml = '<span class="inline-flex items-center gap-1 bg-green-50 text-green-700 text-xs font-semibold px-2.5 py-1 rounded-full border border-green-100"><span class="w-1.5 h-1.5 rounded-full bg-green-500"></span>已通过</span>';
        } else if (reg.status === 'rejected') {
            statusHtml = '<span class="inline-flex items-center gap-1 bg-red-50 text-red-700 text-xs font-semibold px-2.5 py-1 rounded-full border border-red-100"><span class="w-1.5 h-1.5 rounded-full bg-red-500"></span>已拒绝</span>';
        } else {
            statusHtml = `<span class="text-gray-400 text-xs">${reg.status}</span>`;
        }

        // 🟢 核心修改：移除 disabled 逻辑，所有状态都允许勾选
        const html = `
            <tr class="bg-white border-b border-gray-50 hover:bg-gray-50/80 transition-colors">
                <td class="w-4 p-4">
                    <div class="flex items-center justify-center">
                        <input type="checkbox" class="reg-checkbox w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500 cursor-pointer" 
                               value="${reg.regId}">
                    </div>
                </td>
                <td class="px-6 py-4 font-medium text-gray-900 text-sm">${reg.contactName || '未知'}</td>
                <td class="px-6 py-4 text-gray-700 text-base font-bold font-mono tracking-wide">${reg.contactPhone || '-'}</td>
                <td class="px-6 py-4 text-gray-500 text-xs">${regTimeStr}</td>
                <td class="px-6 py-4 text-center">${statusHtml}</td>
            </tr>
        `;
        tbody.append(html);
    });
}

// 🟢 新增：全选/反选逻辑
function toggleSelectAll(source) {
    // 现在选中所有复选框，不管它是什么状态
    $('.reg-checkbox').prop('checked', source.checked);
}

// 🟢 新增：提交审核
// status: 1=通过, 2=拒绝
function submitAudit(status) {
    // 1. 收集 ID
    const selectedIds = [];
    $('.reg-checkbox:checked').each(function () {
        selectedIds.push($(this).val());
    });

    if (selectedIds.length === 0) {
        alert("请先勾选需要操作的学生！");
        return;
    }

    const actionText = status === 1 ? "通过" : "拒绝";

    // ⚠️ 弹出确认框
    if (!confirm(`【再次确认】\n\n您确定要批量【${actionText}】选中的 ${selectedIds.length} 位同学吗？`)) {
        return; // 用户点了取消
    }

    // 2. 发送请求
    $.ajax({
        url: REG_API_URL,
        type: 'POST',
        data: {
            action: 'audit',
            regIds: selectedIds.join(','),
            status: status
        },
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success') {
                // 刷新列表
                const currentEventId = $('#list-current-eventId').val();
                if (currentEventId) {
                    viewRegistrations(currentEventId);
                }
                // 提示成功 (可选)
                // alert("操作成功");
            } else {
                alert("操作失败：" + res.message);
            }
        },
        error: function () {
            alert("网络请求失败");
        }
    });
}

function closeListModal() {
    $('#list-modal').addClass('hidden');
}

function cancelRegistration(eventId) {
    // 1. 确认提示
    if (!confirm("⚠️ 确定要取消这个活动的报名吗？\n取消后名额可能被他人抢占。")) {
        return;
    }

    // 2. 输入原因 (必填)
    let reason = prompt("请输入取消报名的原因 (必填):");

    // 如果点击取消，或者输入为空
    if (reason === null) return; // 点了取消
    if (reason.trim() === "") {
        alert("取消原因不能为空！");
        return;
    }

    // 3. 发送请求
    $.ajax({
        url: REG_API_URL,
        type: 'POST',
        data: {
            action: 'cancel',
            eventId: eventId,
            reason: reason
        },
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success') {
                alert("✅ 报名已取消");
                // 刷新列表 (从列表中移除该活动)
                loadJoinedEvents();
            } else {
                alert("❌ 操作失败：" + res.message);
            }
        },
        error: function () {
            alert("服务器网络错误");
        }
    });
}