/**
 * static/js/profile.js
 * 个人中心页面的逻辑
 * 已将所有原生 alert/confirm 替换为 showToast/showConfirm (依赖 common.js)
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
                renderHeader(currentUser);
                renderProfileForm(currentUser);

                if (currentUser.role === 'organizer') {
                    $('#tab-published').removeClass('hidden');
                }
                loadJoinedEvents();
            } else {
                // ❌ 替换 alert 为 showToast (登录跳转使用 timeout 延迟，提升用户体验)
                showToast("请先登录！正在跳转...", "info");
                setTimeout(function () {
                    window.location.href = 'login.html';
                }, 800);
            }
        }
    });
}

function renderProfileForm(user) {
    const firstChar = (user.realName || user.username).charAt(0).toUpperCase();
    $('#avatar-text').text(firstChar);
    $('#display-name').text(user.realName || user.username);
    $('#role-badge').text(user.role === 'organizer' ? '活动组织者' : '在校学生');
    $('#p-username').val(user.username);
    $('#p-realName').val(user.realName);
    $('#p-phone').val(user.phone);
    $('#p-email').val(user.email);
}

function renderHeader(user) {
    const html = `
        <div class="flex items-center text-sm">
            <span class="mr-4 text-gray-600">欢迎，<span class="font-bold text-blue-600">${user.realName || user.username}</span></span>
            <a href="index.html" class="text-blue-500 hover:underline">返回首页</a>
        </div>
    `;
    $('#user-area').html(html);
}

function switchTab(type) {
    $('.tab-btn').removeClass('active');
    if (type === 'joined') {
        $('.tab-btn:first-child').addClass('active');
        $('#content-joined').removeClass('hidden');
        $('#content-published').addClass('hidden');
        loadJoinedEvents();
    } else {
        $('#tab-published').addClass('active');
        $('#content-joined').addClass('hidden');
        $('#content-published').removeClass('hidden');
        loadPublishedEvents();
    }
}

function updateProfile() {
    const realName = $('#p-realName').val().trim();
    const phone = $('#p-phone').val().trim();
    const email = $('#p-email').val().trim();

    if (!realName) {
        // ❌ 替换 alert
        showToast("真实姓名不能为空", "error");
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
                // ❌ 替换 alert
                showToast("✅ " + res.message, "success");
                $('#display-name').text(realName);
                $('#avatar-text').text(realName.charAt(0).toUpperCase());
            } else {
                // ❌ 替换 alert
                showToast("❌ " + res.message, "error");
            }
        }
    });
}

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
            // ❌ 替换 alert
            container.html('<p class="text-center text-red-500 py-10">加载失败，请刷新重试</p>');
            showToast("活动列表加载失败，请检查网络", "error");
        }
    });
}

function renderEventList(events, container, type) {
    let html = '';
    events.forEach(event => {
        const now = new Date().getTime();
        const end = new Date(event.endTime).getTime();
        const isEventFinished = now > end;
        const timeBadge = isEventFinished
            ? '<span class="text-gray-400 bg-gray-100 px-2 py-1 rounded text-xs">已结束</span>'
            : '<span class="text-blue-600 bg-blue-50 px-2 py-1 rounded text-xs">进行中</span>';

        let actionArea = '';

        if (type === 'published') {
            // --- 👮 组织者视图 ---
            actionArea = `
                <div class="flex gap-2">
                    <button onclick="openSetCodeModal(${event.eventId}, '${event.checkinCode || ''}')" class="text-gray-600 hover:text-blue-600 text-xs font-medium border border-gray-200 hover:border-blue-600 px-3 py-1 rounded transition-colors flex items-center gap-1">
                        <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"></path></svg>
                        ${event.checkinCode ? '修改签到码' : '设置签到码'}
                    </button>
                    <button onclick="viewRegistrations(${event.eventId})" class="text-blue-600 hover:text-blue-800 text-xs font-medium border border-blue-200 hover:border-blue-600 px-3 py-1 rounded transition-colors">
                        📋 查看名单
                    </button>
                </div>
            `;
        } else {
            // --- 🎓 学生视图 ---
            const status = event.registrationStatus || 'pending';
            const checkinStatus = event.checkinStatus || 0;
            const hasCode = event.hasCheckinCode === true;

            // 1. 签到按钮
            let checkinBtn = '';
            if (status === 'approved' && !isEventFinished) {
                if (checkinStatus === 1) {
                    checkinBtn = '<span class="text-green-600 bg-green-50 px-2 py-1 rounded text-xs font-bold border border-green-200">✅ 已签到</span>';
                } else if (hasCode) {
                    checkinBtn = `<button onclick="openCheckinModal(${event.eventId})" class="bg-blue-600 hover:bg-blue-700 text-white text-xs px-3 py-1 rounded shadow-sm transition animate-pulse">📍 签到</button>`;
                } else {
                    checkinBtn = `<button disabled class="bg-gray-100 text-gray-400 text-xs px-3 py-1 rounded cursor-not-allowed border border-gray-200" title="组织者暂未开启签到">签到未开启</button>`;
                }
            }

            // 2. 取消按钮
            let cancelBtn = '';
            if (!isEventFinished && status !== 'cancelled' && status !== 'rejected') {
                cancelBtn = `
                    <button onclick="cancelRegistration(${event.eventId})" class="text-red-400 hover:text-red-600 text-xs hover:underline ml-2">
                        取消
                    </button>
                `;
            }

            // 3. 重新报名按钮
            let reJoinBtn = '';
            if (status === 'cancelled' && !isEventFinished) {
                reJoinBtn = `
                    <button onclick="reJoinEvent(${event.eventId})" class="ml-2 flex items-center gap-1 text-blue-600 hover:text-blue-800 text-xs font-medium border border-blue-200 hover:border-blue-500 px-2 py-1 rounded transition-colors bg-blue-50/50">
                        <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
                        重新报名
                    </button>
                 `;
            }

            // 状态标签
            let myStatusBadge = '';
            if (status === 'pending') myStatusBadge = '<span class="text-yellow-600 text-xs">⏳ 待审核</span>';
            else if (status === 'rejected') myStatusBadge = '<span class="text-red-500 text-xs">🚫 未通过</span>';
            else if (status === 'cancelled') myStatusBadge = '<span class="text-gray-400 text-xs">⚪ 已取消</span>';

            actionArea = `
                <div class="flex items-center gap-2">
                    ${myStatusBadge}
                    ${checkinBtn}
                    ${reJoinBtn} ${cancelBtn}
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

// 🟢 重新报名交互逻辑 (已替换 alert/confirm)
function reJoinEvent(eventId) {
    // ❌ 替换 confirm
    showConfirm("⚠️ 确定要重新报名吗？\n\n注意：每人只有 3 次重新报名的机会。\n请确认您真的可以参加。", function () {
        // 用户点击确定后执行 AJAX
        $.ajax({
            url: REG_API_URL,
            type: 'POST',
            data: {
                action: 'reJoin',
                eventId: eventId
            },
            dataType: 'json',
            success: function (res) {
                if (res.status === 'success') {
                    // ❌ 替换 alert
                    showToast("✅ " + res.message, "success");
                    loadJoinedEvents();
                } else {
                    // ❌ 替换 alert
                    showToast("❌ 失败：" + res.message, "error");
                }
            },
            error: function () {
                // ❌ 替换 alert
                showToast("系统繁忙，请稍后重试", "error");
            }
        });
    });
}

function viewRegistrations(eventId) {
    const modal = $('#list-modal');
    const tbody = $('#registration-table-body');
    const exportBtn = $('#export-btn');
    $('#list-current-eventId').val(eventId);
    modal.removeClass('hidden');

    // ✅ 修改 1：将 colspan="5" 改为 6
    tbody.html('<tr><td colspan="6" class="text-center py-10 text-gray-400">正在加载数据...</td></tr>');

    exportBtn.off('click').click(function () {
        showToast("数据加载中...", "info");
    }).addClass('opacity-50 cursor-not-allowed');

    $.ajax({
        url: REG_API_URL,
        type: 'GET',
        data: {action: 'list_by_event', eventId: eventId},
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success') {
                const list = res.data;
                renderRegistrationList(list);
                exportBtn.off('click').removeClass('opacity-50 cursor-not-allowed');
                if (list && list.length > 0) {
                    exportBtn.click(function () {
                        window.location.href = `${REG_API_URL}?action=export&eventId=${eventId}`;
                    });
                } else {
                    exportBtn.click(function () {
                        showToast("暂无数据可导出", "info");
                    });
                }
            } else {
                // ✅ 修改 2：将 colspan="5" 改为 6
                tbody.html(`<tr><td colspan="6" class="text-center py-10 text-red-500">${res.message}</td></tr>`);
            }
        },
        error: function () {
            showToast("名单加载失败，请检查网络", "error");
            // ✅ 修改 3：将 colspan="5" 改为 6
            tbody.html('<tr><td colspan="6" class="text-center py-10 text-red-500">加载失败</td></tr>');
        }
    });
}

function renderRegistrationList(list) {
    const tbody = $('#registration-table-body');
    tbody.empty();
    $('#checkbox-all').prop('checked', false);

    if (!list || list.length === 0) {
        // ✅ 核心修改：
        // 1. colspan="6"
        // 2. 移除 td 上的 flex 类，改为在内部嵌套一个 div 来居中
        tbody.html(`
            <tr>
                <td colspan="6" class="py-12">
                    <div class="flex flex-col items-center justify-center text-gray-400">
                        <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-3">
                            <span class="text-3xl">🍃</span>
                        </div>
                        <span class="text-sm">暂无学生报名</span>
                    </div>
                </td>
            </tr>
        `);
        return;
    }

    list.forEach(reg => {
        // ... (后续渲染代码保持不变) ...
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

        let checkinHtml = '<span class="text-gray-400 text-xs">未签到</span>';
        if (reg.checkinStatus === 1) {
            checkinHtml = `<span class="inline-flex items-center gap-1 bg-blue-50 text-blue-700 text-xs font-bold px-2.5 py-1 rounded-full border border-blue-100">
                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                已签到
            </span>`;
        }

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
                <td class="px-6 py-4 text-center">${checkinHtml}</td>
            </tr>
        `;
        tbody.append(html);
    });
}

function toggleSelectAll(source) {
    $('.reg-checkbox').prop('checked', source.checked);
}

// 🟢 提交审核 (已替换 alert/confirm)
function submitAudit(status) {
    const selectedIds = [];
    $('.reg-checkbox:checked').each(function () {
        selectedIds.push($(this).val());
    });

    if (selectedIds.length === 0) {
        // ❌ 替换 alert
        showToast("请先勾选需要操作的学生！", "info");
        return;
    }

    const actionText = status === 1 ? "通过" : "拒绝";

    // ❌ 替换 confirm
    showConfirm(`【再次确认】\n\n您确定要批量【${actionText}】选中的 ${selectedIds.length} 位同学吗？`, function () {
        // 用户点击确定后执行 AJAX
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
                    // ❌ 替换 alert (可选，因为列表刷新已经有反馈，但加个 Toast 更友好)
                    showToast(`✅ 批量${actionText}成功！共${selectedIds.length}条记录`, "success");
                } else {
                    // ❌ 替换 alert
                    showToast("操作失败：" + res.message, "error");
                }
            },
            error: function () {
                // ❌ 替换 alert
                showToast("网络请求失败", "error");
            }
        });
    });
}

function closeListModal() {
    $('#list-modal').addClass('hidden');
}

// 🟢 取消报名 (已替换 alert/confirm)
function cancelRegistration(eventId) {
    // ❌ 替换 confirm
    showConfirm("⚠️ 确定要取消这个活动的报名吗？\n取消后名额可能被他人抢占。", function () {

        let reason = prompt("请输入取消报名的原因 (必填):");
        if (reason === null) return;
        if (reason.trim() === "") {
            // ❌ 替换 alert
            showToast("取消原因不能为空！", "error");
            return;
        }

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
                    // ❌ 替换 alert
                    showToast("✅ 报名已取消", "success");
                    loadJoinedEvents();
                } else {
                    // ❌ 替换 alert
                    showToast("❌ 操作失败：" + res.message, "error");
                }
            },
            error: function () {
                // ❌ 替换 alert
                showToast("服务器网络错误", "error");
            }
        });
    });
}

function openSetCodeModal(eventId, currentCode) {
    $('#set-code-eventId').val(eventId);
    $('#admin-checkin-code').val(currentCode);
    $('#set-code-modal').removeClass('hidden');
}

function closeSetCodeModal() {
    $('#set-code-modal').addClass('hidden');
}

// 🟢 提交签到码 (已替换 alert)
function submitCheckinCode() {
    const eventId = $('#set-code-eventId').val();
    const code = $('#admin-checkin-code').val().trim();
    if (!code) {
        // ❌ 替换 alert
        showToast("签到码不能为空", "error");
        return;
    }
    $.ajax({
        url: EVENT_API_URL,
        type: 'POST',
        data: {action: 'set_checkin_code', eventId: eventId, code: code},
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success') {
                // ❌ 替换 alert
                showToast(`✅ 设置成功！\n请将签到码 [${code}] 告知现场学生。`, "success");
                closeSetCodeModal();
                loadPublishedEvents();
            } else {
                // ❌ 替换 alert
                showToast("❌ " + res.message, "error");
            }
        }
    });
}

function openCheckinModal(eventId) {
    $('#checkin-eventId').val(eventId);
    $('#student-input-code').val('');
    $('#student-checkin-modal').removeClass('hidden');
}

function closeCheckinModal() {
    $('#student-checkin-modal').addClass('hidden');
}

// 🟢 学生签到 (已替换 alert)
function submitStudentCheckin() {
    const eventId = $('#checkin-eventId').val();
    const code = $('#student-input-code').val().trim();
    if (!code) {
        // ❌ 替换 alert
        showToast("请输入签到码", "error");
        return;
    }
    $.ajax({
        url: REG_API_URL,
        type: 'POST',
        data: {action: 'checkin', eventId: eventId, inputCode: code},
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success') {
                // ❌ 替换 alert
                showToast("🎉 签到成功！", "success");
                closeCheckinModal();
                loadJoinedEvents();
            } else {
                // ❌ 替换 alert
                showToast("❌ " + res.message, "error");
            }
        }
    });
}