/**
 * static/js/publish.js
 * 活动发布页面的逻辑
 * 已将所有原生 alert 替换为 showToast (依赖 common.js)
 */
const PUBLISH_API_URL = 'event-action';

// 1. 页面加载完成后，初始化 Flatpickr
$(document).ready(function() {
    flatpickr(".date-picker", {
        enableTime: true,       // 启用时间选择
        dateFormat: "Y-m-d H:i", // 格式：2023-12-26 14:30 (注意中间是空格，不再是 T)
        time_24hr: true,        // 24小时制
        locale: "zh",           // 中文语言包
        minuteIncrement: 10,    // 分钟步长，选时间更方便
        allowInput: false       // 禁止手机键盘弹出，强制用选择器
    });
});
// 提交发布请求
function submitEvent() {
    // 获取数据
    const title = $('#p-title').val().trim();
    const category = $('#p-category').val();
    const location = $('#p-location').val().trim();

    // Flatpickr 会把值填入 input，直接 val() 获取即可
    const startTime = $('#p-startTime').val();
    const endTime = $('#p-endTime').val();
    const regDeadline = $('#p-regDeadline').val();
    const detail = $('#p-detail').val().trim();

    // 2. 简单的前端校验
    if (!title || !location || !startTime || !endTime) {
        // <-- 修改在这里：使用 showToast 替代 alert
        showToast("请填写完整的活动必填信息（标题、地点、时间）！", "error");
        return;
    }

    // 3. 发送 AJAX 请求
    $.ajax({
        url: PUBLISH_API_URL, // 对应后端 EventServlet 的路径
        type: 'POST',
        data: {
            action: 'publish', // 路由指令
            title: title,
            category: category,
            location: location,
            startTime: startTime,
            endTime: endTime,
            regDeadline: regDeadline,
            detail: detail
        },
        dataType: 'json',
        success: function (res) {
            if (res.status === 'success') {
                // <-- 修改在这里：使用 showToast 替代 alert
                showToast("🎉 活动发布成功！即将返回首页...", "success");

                // 延迟跳转，确保用户能看到 Toast 提示
                setTimeout(function() {
                    window.location.href = 'index.html';
                }, 1000);

            } else {
                // <-- 修改在这里：使用 showToast 替代 alert
                showToast("发布失败：" + res.message, "error");

                // 如果是因为未登录，跳转去登录页
                if (res.message.includes("登录")) {
                    setTimeout(function() {
                        window.location.href = 'login.html';
                    }, 500);
                }
            }
        },
        error: function (xhr, status, error) {
            console.error(error);
            // <-- 修改在这里：使用 showToast 替代 alert
            showToast("服务器连接错误，请检查网络或控制台日志。", "error");
        }
    });
}