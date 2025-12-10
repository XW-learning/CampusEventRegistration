/**
 * static/js/index.js
 * 首页的核心逻辑
 * 已将所有原生 alert/confirm 替换为 showToast/showConfirm (依赖 common.js)
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
 * 更新头部 UI - 已登录状态 (美化版)
 * @param {Object} user 用户信息 (包含 role 字段)
 */
function updateHeaderLoggedIn(user) {
    const userArea = $('#user-area');
    const displayName = user.realName || user.username;
    // 取名字的第一个字作为头像内容
    const avatarLetter = displayName.charAt(0).toUpperCase();

    // 1. 生成发布按钮 HTML (组织者专属)
    let publishBtnHtml = '';
    if (user.role === 'organizer') {
        publishBtnHtml = `
            <a href="publish.html" class="group flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white text-sm font-medium rounded-full shadow-sm hover:shadow-md transition-all duration-200 transform hover:-translate-y-0.5">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                <span>发布活动</span>
            </a>
            <div class="h-6 w-px bg-gray-200 mx-2"></div>
        `;
    }

    // 2. 生成用户信息区域 HTML
    const html = `
        <div class="flex items-center gap-2">
            ${publishBtnHtml}
            
            <div class="flex items-center gap-3 pl-2 group">
                <div class="relative w-9 h-9 p-[2px] rounded-full bg-gradient-to-tr from-blue-400 to-purple-400">
                    <div class="w-full h-full rounded-full bg-white flex items-center justify-center text-blue-600 font-bold text-sm shadow-inner">
                        ${avatarLetter}
                    </div>
                    <span class="absolute bottom-0 right-0 block h-2.5 w-2.5 rounded-full bg-green-500 ring-2 ring-white"></span>
                </div>

                <div class="flex flex-col">
                    <span class="text-sm font-bold text-gray-700 leading-tight group-hover:text-blue-600 transition-colors cursor-default">
                        ${displayName}
                    </span>
                    <span class="text-[10px] font-medium text-gray-400 bg-gray-50 px-1.5 py-0.5 rounded mt-0.5 w-fit border border-gray-100">
                        ${user.role === 'organizer' ? '✨活动组织者' : '🎓学生成员'}
                    </span>
                </div>
            </div>

            <div class="flex items-center gap-1 ml-2">
                <a href="profile.html" class="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-full transition-all duration-200" title="个人中心">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                    </svg>
                </a>
                
                <button onclick="doLogout()" class="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-full transition-all duration-200" title="退出登录">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                    </svg>
                </button>
            </div>
        </div>
    `;

    userArea.html(html);
}

/**
 * 执行退出登录
 * 🟢 替换 confirm 为 showConfirm
 */
function doLogout() {
    // <-- 修改在这里：使用 showConfirm 替换原生 confirm
    showConfirm("确定要退出登录吗？", function() {
        // 确认后执行 AJAX
        $.ajax({
            url: USER_API_URL,
            type: 'POST',
            data: {action: 'logout'},
            dataType: 'json',
            success: function (res) {
                if (res.status === 'success') {
                    // 退出成功，给个 Toast 提示并刷新页面
                    showToast("退出成功，页面即将刷新...", "success");
                    setTimeout(function() {
                        window.location.reload();
                    }, 500);
                } else {
                    // ❌ 替换 alert (虽然原来只有成功逻辑，但加上错误处理是好习惯)
                    showToast("退出失败！" + res.message, "error");
                }
            }
        });
    });
}

/**
 * 加载活动列表 (支持筛选参数)
 * @param {Object} params - 可选，筛选条件 { keyword, category, ... }
 */
function loadEventList(params = {}) {
    const container = $('#event-container');

    // 如果是筛选模式，给个不同的提示
    const isFilterMode = Object.keys(params).length > 0;
    const loadingText = isFilterMode ? '🔍 正在筛选活动...' : '正在加载精彩活动...';

    container.html(`<p class="text-gray-500 text-center col-span-full py-10">${loadingText}</p>`);

    // 合并基础参数 action='list' 和传入的筛选 params
    const requestData = {
        action: 'list',
        ...params
    };

    $.ajax({
        url: EVENT_API_URL,
        type: 'GET',
        data: requestData,
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success' && res.data && res.data.length > 0) {
                renderEvents(res.data);
            } else {
                // 如果没查到数据
                const emptyText = isFilterMode
                    ? '没有找到符合条件的活动，试着调整一下筛选条件？'
                    : '暂无活动信息';
                container.html(`
                    <div class="col-span-full text-center py-16">
                        <div class="text-6xl mb-4">🍃</div>
                        <p class="text-gray-500 text-lg">${emptyText}</p>
                        ${isFilterMode ? '<button onclick="resetFilter()" class="mt-4 text-blue-600 hover:underline">清空筛选条件</button>' : ''}
                    </div>
                `);
            }
        },
        error: function () {
            container.html('<p class="text-red-500 text-center col-span-full py-10">加载失败，请检查网络</p>');
        }
    });
}

/**
 * 将数据渲染为 HTML 卡片 (取消图片动画版)
 */
function renderEvents(events) {
    const container = $('#event-container');
    container.empty();

    if (events.length === 0) {
        // ... (省略无活动提示，保持不变) ...
        container.html(`
            <div class="col-span-full flex flex-col items-center justify-center py-20 text-center">
                <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                    <svg class="w-10 h-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
                </div>
                <h3 class="text-xl font-medium text-gray-600">暂无活动发布</h3>
                <p class="text-gray-400 mt-2">快去发布第一个精彩活动吧！</p>
            </div>
        `);
        return;
    }

    events.forEach(function (event) {
        // 1. 数据处理
        const deadlineStr = event.regDeadline ? event.regDeadline.substring(0, 16) : '待定';
        const dateObj = parseDateForCard(event.startTime);
        const imageUrl = `https://picsum.photos/seed/${event.eventId}/800/600`;

        const html = `
            <div class="group relative isolate z-0 flex flex-col h-full bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 border border-gray-100 overflow-hidden">
                
                <div class="relative h-48 w-full overflow-hidden">
                    <img src="${imageUrl}" alt="活动封面" class="w-full h-full object-cover">
                    
                    <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-80"></div>

                    <div class="absolute top-3 left-3 bg-white/95 backdrop-blur-md rounded-lg p-2 flex flex-col items-center shadow-lg min-w-[3rem] text-center border border-white/50">
                        <span class="text-[9px] font-bold text-red-500 uppercase tracking-widest leading-none mb-0.5">${dateObj.month}</span>
                        <span class="text-lg font-black text-gray-800 leading-none font-sans">${dateObj.day}</span>
                    </div>

                    <span class="absolute top-3 right-3 bg-black/40 backdrop-blur-md text-white text-[10px] font-medium px-2 py-1 rounded-full border border-white/10 flex items-center gap-1">
                        <span class="w-1.5 h-1.5 rounded-full bg-green-400"></span>
                        ${event.category || '活动'}
                    </span>
                </div>

                <div class="p-4 flex flex-col flex-grow relative">
                    
                    <h3 class="text-lg font-bold text-gray-900 mb-2 line-clamp-2 leading-snug group-hover:text-blue-600 transition-colors" title="${event.title}">
                        ${event.title}
                    </h3>
                    
                    <div class="flex items-center text-gray-500 text-xs mb-4">
                        <svg class="w-3.5 h-3.5 mr-1 flex-shrink-0 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                        <span class="truncate" title="${event.location}">${event.location}</span>
                    </div>

                    <div class="mt-auto pt-3 border-t border-dashed border-gray-100 flex items-center justify-between gap-2">
                        
                        <div class="flex flex-col">
                            <span class="text-[9px] text-gray-400 uppercase tracking-wide font-semibold">截止</span>
                            <div class="flex items-center text-xs font-medium text-red-500 mt-0.5">
                                ${deadlineStr.split(' ')[0]}
                            </div>
                        </div>

                        <button onclick="openRegModal(${event.eventId})" 
                            class="group/btn relative overflow-hidden rounded-md bg-blue-50 px-3 py-1.5 text-blue-600 font-bold text-xs transition-all duration-300 hover:bg-blue-600 hover:text-white hover:shadow-md active:scale-95">
                            <span class="relative z-10 flex items-center gap-1">
                                立即报名
                                <svg class="w-3 h-3 transition-transform duration-300 group-hover/btn:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6"></path></svg>
                            </span>
                        </button>
                    </div>
                </div>
            </div>
        `;
        container.append(html);
    });
}

function openRegModal(eventId) {
    // A. 检查是否登录
    if (!currentUser) {
        // <-- 修改在这里：使用 showConfirm 替换原生 confirm
        showConfirm("您需要登录后才能报名活动。\n是否立即跳转到登录页面？", function() {
            window.location.href = 'login.html';
        });
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
        // <-- 修正 alert 为 showToast
        showToast("请务必填写真实姓名和联系电话，以便通知！", 'error');
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
                // <-- 修正 alert 为 showToast，并加上后端消息
                showToast("🎉 报名成功！" + (res.message ? res.message : ""), "success");
                // 可选：刷新列表或更改按钮状态
            } else {
                // <-- 修正 alert 为 showToast，并带上后端错误信息
                showToast("❌ 报名失败：" + res.message, "error");
                if (res.code === 'NOT_LOGIN') {
                    // 延迟跳转，让用户看清提示
                    setTimeout(function() {
                        window.location.href = 'login.html';
                    }, 500);
                }
            }
        },
        error: function () {
            // <-- 修正 alert 为 showToast
            showToast("服务器网络错误，请稍后重试。", "error");
        }
    });
}

/**
 * 辅助函数：解析日期字符串用于卡片展示
 */
function parseDateForCard(dateStr) {
    const monthNames = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"];

    if (!dateStr) return {month: "TBD", day: "--"};

    let date = new Date(dateStr.replace(" ", "T"));

    if (isNaN(date.getTime())) {
        const parts = dateStr.split('-');
        if (parts.length >= 3) {
            const m = parseInt(parts[1]) - 1;
            const d = parts[2].split(' ')[0];
            return {
                month: monthNames[m] || "UNK",
                day: d
            };
        }
        return {month: "???", day: "??"};
    }

    return {
        month: monthNames[date.getMonth()],
        day: String(date.getDate()).padStart(2, '0')
    };
}

// --- 5. 筛选弹窗交互逻辑 ---

function toggleFilterModal() {
    const modal = $('#filter-modal');
    const backdrop = $('#filter-backdrop');
    const panel = $('#filter-panel');

    if (modal.hasClass('hidden')) {
        // 打开
        modal.removeClass('hidden');
        setTimeout(() => {
            backdrop.removeClass('opacity-0');
            panel.removeClass('scale-95 opacity-0');
        }, 10);
    } else {
        // 关闭 (先淡出，再隐藏)
        backdrop.addClass('opacity-0');
        panel.addClass('scale-95 opacity-0');
        setTimeout(() => {
            modal.addClass('hidden');
        }, 300);
    }
}

// 占位函数：应用筛选
function applyFilter() {
    const params = {};

    const keyword = $('#f-keyword').val().trim();
    if (keyword) params.keyword = keyword;

    const category = $('#f-category').val();
    if (category) params.category = category;

    const location = $('#f-location').val().trim();
    if (location) params.location = location;

    const startDate = $('#f-startDate').val();
    if (startDate) params.startDate = startDate;

    const endDate = $('#f-endDate').val();
    if (endDate) params.endDate = endDate;

    toggleFilterModal();
    loadEventList(params);
}

// ==========================================
// ➕ 新增：按钮点击与搜索交互逻辑
// ==========================================

/**
 * 🔍 搜索框查询
 * 对应 HTML: onkeypress="if(event.keyCode===13) filterByKeyword(this.value)"
 */
function filterByKeyword(value) {
    // 1. 获取输入框的值 (去除首尾空格)
    const keyword = value ? value.trim() : "";

    // 2. 调用现有的加载函数 (它会发 AJAX 给后端)
    // 后端 EventServlet 会接收 keyword 参数并拼接 SQL LIKE %keyword%
    loadEventList({ keyword: keyword });

    // 3. 给用户一个反馈
    if (keyword) {
        showToast(`🔍 正在搜索：${keyword}`, "info");
    }
}

/**
 * 🏷️ 分类按钮点击
 * 对应 HTML: onclick="filterByCategory('文艺汇演')"
 */
function filterByCategory(categoryName) {
    loadEventList({ category: categoryName });
    updateCategoryButtonStyle(categoryName);
}

/**
 * 🎨 辅助函数：专门负责切换按钮颜色
 * (这样写是为了让逻辑更清晰)
 */
function updateCategoryButtonStyle(targetCategory) {
    // 定义两种样式集合 (基于你的 HTML 和 Tailwind 类名)
    // 🔵 选中态：蓝色背景、白字
    const activeClasses = "bg-blue-600 text-white shadow-sm shadow-blue-200 border-transparent";
    // ⚪️ 未选中态：白色背景、灰字、带边框
    const inactiveClasses = "bg-white text-gray-600 border-gray-200 hover:text-blue-600 hover:border-blue-200";

    // 1. 先把所有按钮都重置为“未选中”
    $('.category-btn').removeClass(activeClasses).addClass(inactiveClasses);

    // 2. 找到当前被点击的按钮，设为“选中”
    // 💡 技巧：因为你的HTML里没写 data-category，我们用 jQuery 的 filter 功能
    // 通过匹配 onclick 属性里的文字来找到对应的按钮
    let $targetBtn;

    if (!targetCategory) {
        // 如果是“全部”，找 data-category="" 或者 onclick 包含 '' 的
        $targetBtn = $('.category-btn[data-category=""]');
        if ($targetBtn.length === 0) {
            // 备用方案：找 onclick 里包含 "''" 的
            $targetBtn = $('.category-btn').filter((i, el) => $(el).attr('onclick').indexOf("''") !== -1);
        }
    } else {
        // 找 onclick 属性中包含 '分类名' 的按钮
        $targetBtn = $('.category-btn').filter(function() {
            // 获取这个按钮的 onclick 属性内容，比如 "filterByCategory('文艺汇演')"
            const onClickStr = $(this).attr('onclick');
            return onClickStr && onClickStr.indexOf(`'${targetCategory}'`) !== -1;
        });
    }

    // 3. 高亮找到的按钮
    $targetBtn.removeClass(inactiveClasses).addClass(activeClasses);
}

function resetFilter() {
    // 1. 清空输入框 UI
    $('#f-keyword').val('');
    $('#f-category').val('');
    $('#f-location').val('');
    $('#f-startDate').val('');
    $('#f-endDate').val('');

    // 2. 重新加载所有数据 (传空对象)
    loadEventList({});
}