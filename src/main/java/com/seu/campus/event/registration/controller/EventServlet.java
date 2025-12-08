package com.seu.campus.event.registration.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seu.campus.event.registration.model.Event;
import com.seu.campus.event.registration.model.User;
import com.seu.campus.event.registration.service.EventService;
import com.seu.campus.event.registration.service.impl.EventServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动模块控制器
 * 处理路径: /event-action
 */
@WebServlet(name = "EventServlet", value = "/event-action")
public class EventServlet extends HttpServlet {
    private final EventService eventService = new EventServiceImpl();
    // 修改 Gson 的初始化，设置日期格式，否则传给前端的是怪异的字符串
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");

        String action = req.getParameter("action");

        switch (action) {
            case "list":
                doList(req, resp); // <-- 处理列表请求
                break;
            default:
                writeJson(resp, Map.of("status", "error", "message", "未知的 GET action"));
                break;
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");

        String action = req.getParameter("action");

        switch (action) {
            case "publish":
                doPublish(req, resp);
                break;
            default:
                writeJson(resp, Map.of("status", "error", "message", "未知指令"));
                break;
        }
    }


    private void doList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Event> events = eventService.findAllActiveEvents();
            result.put("status", "success");
            result.put("data", events); // 将 List 放进 data 字段
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "查询失败");
        }
        writeJson(resp, result);
    }

    private void doPublish(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> result = new HashMap<>();

        // 1. 权限检查：必须登录才能发布
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("status", "fail");
            result.put("message", "请先登录！");
            writeJson(resp, result);
            return;
        }

        try {
            // 2. 获取参数
            String title = req.getParameter("title");
            String category = req.getParameter("category");
            String location = req.getParameter("location");
            String detail = req.getParameter("detail");

            // 3. 日期处理 (前端 input type="datetime-local" 传来的格式是 yyyy-MM-dd'T'HH:mm)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            Event event = new Event();
            event.setTitle(title);
            event.setCategory(category);
            event.setLocation(location);
            event.setDetail(detail);
            // 关联当前登录用户为发布者
            event.setPublisherId(currentUser.getUserId());

            // 转换时间，可能抛出 ParseException
            String startStr = req.getParameter("startTime");
            String endStr = req.getParameter("endTime");
            String deadlineStr = req.getParameter("regDeadline");

            if(startStr != null && !startStr.isEmpty()) event.setStartTime(sdf.parse(startStr));
            if(endStr != null && !endStr.isEmpty()) event.setEndTime(sdf.parse(endStr));
            if(deadlineStr != null && !deadlineStr.isEmpty()) event.setRegDeadline(sdf.parse(deadlineStr));

            // 4. 调用业务层
            String msg = eventService.publishEvent(event);

            if ("SUCCESS".equals(msg)) {
                result.put("status", "success");
                result.put("message", "活动发布成功！");
            } else {
                result.put("status", "fail");
                result.put("message", msg);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            result.put("status", "fail");
            result.put("message", "时间格式错误");
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