package com.seu.campus.event.registration.service.impl;

import com.seu.campus.event.registration.mapper.UserMapper;
import com.seu.campus.event.registration.model.User;
import com.seu.campus.event.registration.service.UserService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * 用户服务实现类（修正版）
 * 核心修改：修复 SqlSession 生命周期管理，解决缓存不刷新和线程安全问题
 *
 * @author XW
 */
public class UserServiceImpl implements UserService {

    // 1. 全局唯一的 Factory（线程安全）
    private static final SqlSessionFactory SQL_SESSION_FACTORY;

    // 2. 静态代码块初始化 Factory
    static {
        try {
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SQL_SESSION_FACTORY = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("初始化 MyBatis 失败", e);
        }
    }

    @Override
    public String register(User user) {
        // 3. 方法级 Session，自动关闭
        try (SqlSession session = SQL_SESSION_FACTORY.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            // 检查用户名是否已存在
            User exist = mapper.findByUsername(user.getUsername());
            if (exist != null) {
                return "用户名已存在";
            }

            int rows = mapper.save(user);
            session.commit();
            return rows > 0 ? "SUCCESS" : "注册失败";
        }
    }

    @Override
    public User login(String username, String password) {
        try (SqlSession session = SQL_SESSION_FACTORY.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            // 先查用户
            User user = mapper.findByUsername(username);

            // 再比对密码 (注意判空)
            if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
                return user;
            }
            return null;
        }
    }

    @Override
    public String updateProfile(User user) {
        try (SqlSession session = SQL_SESSION_FACTORY.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            int rows = mapper.update(user);
            session.commit();

            return rows > 0 ? "SUCCESS" : "更新失败";
        }
    }
}