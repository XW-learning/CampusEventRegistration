package com.seu.campus.event.registration.service.impl;

import com.seu.campus.event.registration.mapper.UserMapper;
import com.seu.campus.event.registration.mapper.impl.UserMapperImpl;
import com.seu.campus.event.registration.model.User;
import com.seu.campus.event.registration.service.UserService;

/**
 * 用户模块服务实现类
 * 主要负责处理用户相关的业务逻辑，包括：
 * 1. 用户注册（包含用户名唯一性校验、密码验证、默认角色设置等）
 * 2. 用户登录验证
 *
 * @author XW
 */

public class UserServiceImpl implements UserService {
    private final UserMapper userMapper = new UserMapperImpl();

    @Override
    public String register(User user) {
        // 1. 业务检查：关键字段不能为空
        if (user.getUsername() == null || "".equals(user.getUsername())) {
            return "用户名不能为空";
        }
        if (user.getPassword() == null || "".equals(user.getPassword())) {
            return "密码不能为空";
        }
        // 2. 业务检查：用户名是否已存在 (调用 Mapper 查询)
        User existUser = userMapper.findByUsername(user.getUsername());
        if (existUser != null) {
            return "注册失败：用户名 '" + user.getUsername() + "' 已被使用";
        }
        // 3. 这里的默认角色设置 (防止前端没传)
        if (user.getRole() == null || "".equals(user.getRole())) {
            user.setRole("student");
        }
        // 4. 执行保存
        int rows = userMapper.save(user);
        // 5. 根据结果返回消息
        if (rows > 0) {
            return "SUCCESS";
        } else {
            return "注册失败：数据库系统错误";
        }
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @Override
    public String updateProfile(User user) {
        if (user.getUserId() == null) {
            return "用户ID不能为空";
        }
        int rows = userMapper.update(user);
        return rows > 0 ? "SUCCESS" : "更新失败：数据库操作无响应";
    }
}
