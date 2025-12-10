package com.seu.campus.event.registration.controller;

import com.google.gson.Gson;
import com.seu.campus.event.registration.mapper.EventMapper;
import com.seu.campus.event.registration.mapper.impl.EventMapperImpl;
import com.seu.campus.event.registration.model.Event;
import com.seu.campus.event.registration.model.Registration;
import com.seu.campus.event.registration.model.User;
import com.seu.campus.event.registration.service.RegistrationService;
import com.seu.campus.event.registration.service.impl.RegistrationServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 活动报名管理
 * 处理路径: /registration-action
 * 请求参数:  action
 *
 * @author XW
 */
@WebServlet(name = "RegistrationServlet", value = "/registration-action")
public class RegistrationServlet extends HttpServlet {
    private final RegistrationService registrationService = new RegistrationServiceImpl();
    private final EventMapper eventMapper = new EventMapperImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");

        // TODO: 在此处添加你的 doGet 逻辑
        String action = req.getParameter("action");
        if ("list_by_event".equals(action)) {
            resp.setContentType("application/json;charset=utf-8");
            doListByEvent(req, resp);
        } else if ("export".equals(action)) {
            // 导出不需要 json content type
            doExport(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");

        // TODO: 在此处添加你的 doPost 逻辑
        // 获取请求参数action
        String action = req.getParameter("action");
        switch (action) {
            case "register":
                doRegister(req, resp);
                break;
            case "audit":
                doAudit(req, resp);
                break;
            case "cancel":
                doCancel(req, resp);
                break;
            case "checkin":
                doCheckin(req, resp);
                break;
            case "reJoin":
                doReJoin(req, resp);
                break;
            default:
                throw new RuntimeException("无效的请求参数");
        }
    }

    private void doListByEvent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> result = new HashMap<>();

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            result.put("status", "fail");
            result.put("message", "未登录");
            writeJson(resp, result);
            return;
        }

        Integer eventId = Integer.parseInt(req.getParameter("eventId"));
        List<Registration> list = registrationService.getRegistrationList(currentUser.getUserId(), eventId);

        if (list != null) {
            result.put("status", "success");
            result.put("data", list);
        } else {
            result.put("status", "fail");
            result.put("message", "权限不足或活动不存在");
        }
        writeJson(resp, result);
    }

    // 🟢 新增：导出 Excel (CSV格式)
    private void doExport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            resp.sendError(403, "请先登录");
            return;
        }

        Integer eventId = Integer.parseInt(req.getParameter("eventId"));
        List<Registration> list = registrationService.getRegistrationList(currentUser.getUserId(), eventId);
        Event event = eventMapper.findById(eventId);

        // 1. 校验数据是否存在
        if (list == null || list.isEmpty() || event == null) {
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().print("<script>alert('该活动暂无报名信息，无需导出！');history.back();</script>");
            return;
        }

        // 2. 设置响应头
        String fileName = event.getTitle() + "_报名名单.csv";
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        resp.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        resp.setContentType("text/csv; charset=UTF-8");

        PrintWriter out = resp.getWriter();
        out.write('\ufeff'); // BOM 头

        // 🟢 修改表头：增加 "签到状态"
        out.println("报名ID,联系人姓名,联系电话,报名时间,状态,签到状态");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Registration reg : list) {
            String time = reg.getRegTime() != null ? sdf.format(reg.getRegTime()) : "";

            // 报名状态描述
            String statusDesc = "待审核";
            if ("approved".equals(reg.getStatus())) {
                statusDesc = "报名成功";
            } else if ("rejected".equals(reg.getStatus())) {
                statusDesc = "已拒绝";
            } else if ("cancelled".equals(reg.getStatus())) { // 增加已取消的判断
                statusDesc = "已取消";
            }

            // 🟢 新增：签到状态描述
            String checkinDesc = "未签到";
            if (reg.getCheckinStatus() != null && reg.getCheckinStatus() == 1) {
                checkinDesc = "✅ 已签到";
                // 如果你想显示签到时间，可以在这里拼上去，例如: "已签到 (" + sdf.format(reg.getCheckinTime()) + ")"
            }

            out.println(
                    reg.getRegId() + "," +
                            safeCsv(reg.getContactName()) + "," +
                            "\t" + safeCsv(reg.getContactPhone()) + "," +
                            time + "," +
                            statusDesc + "," +
                            checkinDesc // 🟢 追加最后一列
            );
        }
        out.flush();
        out.close();
    }

    // 处理 CSV 特殊字符
    private String safeCsv(String input) {
        if (input == null) {
            return "";
        }
        return input.replace(",", "，").replace("\n", " ");
    }

    private void doRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> result = new HashMap<>();

        // 1. 权限校验
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (user == null) {
            result.put("status", "fail");
            result.put("code", "NOT_LOGIN");
            result.put("message", "请先登录");
            writeJson(resp, result);
            return;
        }

        try {
            // 2. 获取参数
            Integer eventId = Integer.parseInt(req.getParameter("eventId"));
            String contactName = req.getParameter("contactName");
            String contactPhone = req.getParameter("contactPhone");

            // 3. 调用业务层
            String msg = registrationService.register(user.getUserId(), eventId, contactName, contactPhone);

            if ("SUCCESS".equals(msg)) {
                result.put("status", "success");
                result.put("message", "报名成功！请准时参加活动。");
            } else {
                result.put("status", "fail");
                result.put("message", msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "系统错误: " + e.getMessage());
        }
        writeJson(resp, result);
    }

    private void doAudit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> result = new HashMap<>();
        // ... (Session 检查代码保持不变) ...

        try {
            String regIds = req.getParameter("regIds");
            // 前端还是传 1 或 2，我们在这里转义
            int statusInt = Integer.parseInt(req.getParameter("status"));

            // 🔴 核心修改：数字转字符串
            String statusStr = "pending";
            if (statusInt == 1) {
                statusStr = "approved";
            }
            if (statusInt == 2) {
                statusStr = "rejected";
            }

            String msg = registrationService.audit(regIds, statusStr);

            if ("SUCCESS".equals(msg)) {
                result.put("status", "success");
                result.put("message", "操作成功");
            } else {
                result.put("status", "fail");
                result.put("message", msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "参数错误");
        }
        writeJson(resp, result);
    }

    private void doCancel(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> result = new HashMap<>();

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            result.put("status", "fail");
            result.put("message", "未登录");
            writeJson(resp, result);
            return;
        }

        try {
            Integer eventId = Integer.parseInt(req.getParameter("eventId"));
            String reason = req.getParameter("reason");

            String msg = registrationService.cancel(currentUser.getUserId(), eventId, reason);

            if ("SUCCESS".equals(msg)) {
                result.put("status", "success");
                result.put("message", "报名已取消");
            } else {
                result.put("status", "fail");
                result.put("message", msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "参数错误");
        }
        writeJson(resp, result);
    }

    private void doCheckin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> result = new HashMap<>();

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            result.put("status", "fail");
            result.put("message", "未登录");
            writeJson(resp, result);
            return;
        }

        try {
            Integer eventId = Integer.parseInt(req.getParameter("eventId"));
            String inputCode = req.getParameter("inputCode");

            if (inputCode == null || inputCode.trim().isEmpty()) {
                result.put("status", "fail");
                result.put("message", "请输入签到码");
                writeJson(resp, result);
                return;
            }

            // 调用 Service (需要在 RegistrationService 中实现 verifyCheckin)
            String msg = registrationService.verifyCheckin(currentUser.getUserId(), eventId, inputCode);

            if ("SUCCESS".equals(msg)) {
                result.put("status", "success");
                result.put("message", "签到成功！");
            } else {
                result.put("status", "fail");
                result.put("message", msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "系统繁忙");
        }
        writeJson(resp, result);
    }

    private void doReJoin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> result = new HashMap<>();

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            result.put("status", "fail");
            result.put("message", "未登录");
            writeJson(resp, result);
            return;
        }

        try {
            // 前端传 eventId 即可
            Integer eventId = Integer.parseInt(req.getParameter("eventId"));

            String msg = registrationService.reJoin(currentUser.getUserId(), eventId);

            if ("SUCCESS".equals(msg)) {
                result.put("status", "success");
                result.put("message", "重新报名成功！");
            } else {
                result.put("status", "fail");
                result.put("message", msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "系统繁忙");
        }
        writeJson(resp, result);
    }

    private void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(obj));
        out.flush();
        out.close();
    }
}
