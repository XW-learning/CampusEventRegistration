package com.seu.campus.event.registration.service.impl;

import com.seu.campus.event.registration.mapper.UserMapper;
import com.seu.campus.event.registration.model.User;
import com.seu.campus.event.registration.service.UserService;
import com.seu.campus.event.registration.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

/**
 * 用户服务实现类（封装 SqlSession 后）
 *
 * @author XW
 */
public class UserServiceImpl implements UserService {

    @Override
    public String register(User user) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            // 检查用户名是否存在
            if (mapper.findByUsername(user.getUsername()) != null) {
                return "用户名已存在";
            }

            int rows = mapper.save(user);
            session.commit();
            return rows > 0 ? "SUCCESS" : "注册失败";
        }
    }

    @Override
    public User login(String username, String password) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            User user = mapper.findByUsername(username);
            if (user != null && user.getPassword() != null
                    && user.getPassword().equals(password)) {
                return user;
            }
            return null;
        }
    }

    @Override
    public String updateProfile(User user) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            int rows = mapper.update(user);
            session.commit();
            return rows > 0 ? "SUCCESS" : "更新失败";
        }
    }
}
