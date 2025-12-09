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
 * @author XW
 */
@WebServlet(name = "RegistrationServlet", value = "/registration-action")
public class RegistrationServlet extends HttpServlet {
    private final RegistrationService registrationService = new RegistrationServiceImpl();
    private final EventMapper eventMapper = new EventMapperImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");

        // TODO: 在此处添加你的 doPost 逻辑
        // 获取请求参数action
        String action = req.getParameter("action");
        switch (action) {
            case "register":
                doRegister(req, resp);
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
        // 🟢 新增逻辑：如果 list 为空，返回一段 JS 脚本弹窗提示，而不是文件流
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
        out.write('\ufeff');

        out.println("报名ID,联系人姓名,联系电话,报名时间,状态");

        // 3. 🟢 准备日期格式化工具
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Registration reg : list) {
            // 🟢 修复 Bug：使用 sdf 格式化日期
            String time = reg.getRegTime() != null ? sdf.format(reg.getRegTime()) : "";

            String status = Objects.equals(reg.getStatus(), "1") ? "报名成功" : "待审核";

            out.println(
                    reg.getRegId() + "," +
                            safeCsv(reg.getContactName()) + "," +
                            // 强制让 Excel 把电话当字符串处理（加制表符或单引号），防止科学计数法
                            "\t" + safeCsv(reg.getContactPhone()) + "," +
                            time + "," +
                            status
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

    private void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(obj));
        out.flush();
        out.close();
    }
}
