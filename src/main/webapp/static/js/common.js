/**
 * static/js/common.js
 * 全站通用的 UI 组件与工具函数
 * 自动注入 Toast 和 Confirm 的 HTML 结构，无需手动修改每个 HTML 文件
 */

$(document).ready(function () {
    // 1. 自动注入 HTML 结构 (如果页面上没有的话)
    injectCommonUI();
});

function injectCommonUI() {
    const body = $('body');

    // 防止重复注入
    if ($('#toast-container').length > 0) return;

    const commonHtml = `
        <div id="toast-container" class="fixed top-4 right-4 z-[9999] flex flex-col gap-2 pointer-events-none"></div>

        <div id="custom-confirm-modal" class="fixed inset-0 z-[9999] hidden">
            <div class="absolute inset-0 bg-gray-900/50 backdrop-blur-sm transition-opacity" onclick="closeConfirmModal()"></div>
            
            <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-sm bg-white rounded-2xl shadow-2xl p-6 transform transition-all scale-100 animate-fade-in-up">
                <div class="flex flex-col items-center text-center">
                    <div class="w-12 h-12 bg-yellow-100 text-yellow-600 rounded-full flex items-center justify-center mb-4">
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                    </div>
                    <h3 class="text-lg font-bold text-gray-800 mb-2">操作确认</h3>
                    <p class="text-sm text-gray-600 mb-6" id="confirm-message">确定要执行此操作吗？</p>
                    <div class="flex gap-3 w-full">
                        <button onclick="closeConfirmModal()" class="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-600 py-2.5 rounded-xl font-medium transition active:scale-95">取消</button>
                        <button id="btn-confirm-yes" class="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-2.5 rounded-xl font-bold shadow-md transition active:scale-95">确定</button>
                    </div>
                </div>
            </div>
        </div>
    `;

    body.append(commonHtml);
}

/* ================= 全局工具函数 ================= */

/**
 * 显示 Toast 消息
 * @param {string} message 消息内容
 * @param {string} type 'success' | 'error' | 'info'
 */
function showToast(message, type = 'info') {
    const container = $('#toast-container');

    let bgClass, iconHtml;
    if (type === 'success') {
        bgClass = 'bg-white border-l-4 border-green-500 text-green-700';
        iconHtml = '<svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>';
    } else if (type === 'error') {
        bgClass = 'bg-white border-l-4 border-red-500 text-red-700';
        iconHtml = '<svg class="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>';
    } else {
        bgClass = 'bg-white border-l-4 border-blue-500 text-blue-700';
        iconHtml = '<svg class="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>';
    }

    const toastHtml = `
        <div class="toast-item pointer-events-auto w-72 p-4 rounded-lg shadow-lg flex items-start gap-3 transform translate-x-full transition-all duration-300 ease-out ${bgClass}">
            <div class="flex-shrink-0 mt-0.5">${iconHtml}</div>
            <div class="text-sm font-medium break-words leading-tight">${message}</div>
        </div>
    `;

    const $toast = $(toastHtml);
    container.append($toast);

    setTimeout(() => { $toast.removeClass('translate-x-full'); }, 10);
    setTimeout(() => {
        $toast.addClass('opacity-0 translate-x-full');
        setTimeout(() => $toast.remove(), 300);
    }, 3000);
}

/**
 * 显示 Confirm 确认框
 * @param {string} message 提示信息
 * @param {Function} onConfirm 点击确定后的回调函数
 */
function showConfirm(message, onConfirm) {
    const modal = $('#custom-confirm-modal');
    const btnYes = $('#btn-confirm-yes');

    // 把换行符转为 <br> 标签，支持多行显示
    $('#confirm-message').html(message.replace(/\n/g, '<br>'));

    btnYes.off('click');
    btnYes.on('click', function() {
        onConfirm();
        closeConfirmModal();
    });

    modal.removeClass('hidden');
}

function closeConfirmModal() {
    $('#custom-confirm-modal').addClass('hidden');
}