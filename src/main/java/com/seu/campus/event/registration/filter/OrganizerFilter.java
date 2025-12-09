package com.seu.campus.event.registration.filter;

import com.seu.campus.event.registration.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 权限过滤器
 * 作用：拦截对 publish.html 的直接访问
 * 规则：只有已登录且角色为 'organizer' 的用户才能访问
 */
// 🟢 关键注解：urlPatterns 指定要拦截的路径
@WebFilter(filterName = "OrganizerFilter", urlPatterns = {"/publish.html"})
public class OrganizerFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 过滤器初始化，通常留空
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 1. 强转为 HTTP 协议对象，方便操作 Session 和 重定向
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 2. 获取当前用户
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;

        // 3. 权限判断逻辑
        if (user == null) {
            // 情况 A: 根本没登录 -> 踢回登录页
            resp.sendRedirect("login.html");
        } else if (!"organizer".equals(user.getRole())) {
            // 情况 B: 登录了，但是个学生 (权限不足) -> 踢回首页
            // (也可以做一个 error.html 页面提示权限不足，这里简单处理直接回首页)
            resp.sendRedirect("index.html");
        } else {
            // 情况 C: 身份尊贵，可以通过 -> 放行 (让 Tomcat 继续处理 HTML)
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // 销毁时调用，通常留空
    }
}