package com.seu.campus.event.registration.controller;

import com.google.gson.Gson;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author XW
 */
@WebServlet(name = "RegistrationServlet", value = "/registration-action")
public class RegistrationServlet extends HttpServlet {
    private final RegistrationService registrationService = new RegistrationServiceImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");

        // TODO: 在此处添加你的 doGet 逻辑
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
