package com.seu.campus.event.registration.controller;

import com.google.gson.Gson;
import com.seu.campus.event.registration.model.User;
import com.seu.campus.event.registration.service.UserService;
import com.seu.campus.event.registration.service.impl.UserServiceImpl;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户模块控制器
 * 处理路径: /user
 * 请求参数: action (register/login)
 *
 * @author XW
 */
@WebServlet(name = "user", value = "/user")
public class UserServlet extends HttpServlet {
    // 业务层
    private final UserService userService = new UserServiceImpl();
    // json工具
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1. 设置请求和响应编码 (防止中文乱码)
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1. 设置请求和响应编码 (防止中文乱码)
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
        // 获取操作指令
        String action = req.getParameter("action");
        // 路由分发
        switch (action) {
            case "register":
                // 处理注册
                doRegister(req, resp);
                break;
            case "login":
                // 处理登录
                doLogin(req, resp);
                break;
            case "logout":
                // 处理登出
                doLogout(req, resp);
                break;
            case "check_login":
                // 处理检查登录状态
                doCheckLogin(req, resp);
                break;
            case "update_profile":
                // 处理更新用户信息
                doUpdateProfile(req, resp);
                break;
            default:
                // 如果 action 不对，返回错误
                Map<String, Object> result = new HashMap<>();
                result.put("status", "error");
                result.put("message", "未知的 action 指令");
                writeJson(resp, result);
                break;
        }

    }

    private void doRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 获取参数
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String realName = req.getParameter("realName");
        String role = req.getParameter("role");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        // 封装对象
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(realName);
        user.setRole(role);
        user.setEmail(email);
        user.setPhone(phone);
        // 调用业务层
        String msg = userService.register(user);
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        if ("SUCCESS".equals(msg)) {
            result.put("status", "success");
            result.put("message", "注册成功");
        } else {
            result.put("status", "fail");
            result.put("message", msg);
        }
        // 返回结果
        writeJson(resp, result);
    }

    private void doLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        User user = userService.login(username, password);
        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            HttpSession session = req.getSession();
            session.setAttribute("currentUser", user);
            result.put("status", "success");
            result.put("message", "登录成功");
            result.put("data", user);
        } else {
            result.put("status", "fail");
            result.put("message", "用户名或密码错误");
        }
        writeJson(resp, result);
    }

    private void doLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 销毁 Session
        req.getSession().invalidate();

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "已退出登录");
        writeJson(resp, result);
    }

    // 检查登录状态
    private void doCheckLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        Map<String, Object> result = new HashMap<>();

        if (currentUser != null) {
            result.put("status", "success");
            result.put("message", "已登录");
            // 为了安全，不要把密码传回前端，重新封装一个简单的 Map
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", currentUser.getUserId());
            userInfo.put("username", currentUser.getUsername());
            userInfo.put("realName", currentUser.getRealName());
            userInfo.put("role", currentUser.getRole());
            userInfo.put("phone", currentUser.getPhone());
            userInfo.put("email", currentUser.getEmail());

            result.put("data", userInfo);
        } else {
            result.put("status", "fail");
            result.put("message", "未登录");
        }
        writeJson(resp, result);
    }

    private void doUpdateProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取当前登录用户 (为了安全，ID必须从Session取，不能信前端传的)
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            result.put("status", "fail");
            result.put("message", "未登录");
            writeJson(resp, result);
            return;
        }

        // 2. 获取参数
        String realName = req.getParameter("realName");
        String phone = req.getParameter("phone");
        String email = req.getParameter("email");

        // 3. 更新内存中的 currentUser 对象 (用于传给 Service)
        currentUser.setRealName(realName);
        currentUser.setPhone(phone);
        currentUser.setEmail(email);

        // 4. 调用业务层
        String msg = userService.updateProfile(currentUser);

        if ("SUCCESS".equals(msg)) {
            // 🟢 关键：数据库更新成功后，也要更新 Session 里的对象，否则刷新页面还是旧数据
            session.setAttribute("currentUser", currentUser);

            result.put("status", "success");
            result.put("message", "个人信息修改成功！");
        } else {
            result.put("status", "fail");
            result.put("message", msg);
        }
        writeJson(resp, result);
    }

    // 辅助方法：把对象转为json字符串
    private void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        PrintWriter out = resp.getWriter();
        // 转换为json字符串
        String json = gson.toJson(obj);
        out.print(json);
        out.flush();
        out.close();
    }
}
