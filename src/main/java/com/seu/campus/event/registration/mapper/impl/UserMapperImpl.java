package com.seu.campus.event.registration.mapper.impl;

import com.seu.campus.event.registration.mapper.UserMapper;
import com.seu.campus.event.registration.model.User;
import com.seu.campus.event.registration.util.DBUtil;

import java.util.List;

/**
 * @author XW
 */
public class UserMapperImpl implements UserMapper {
    @Override
    public int save(User user) {
        String sql = "INSERT INTO t_user (username, password, real_name, role, email, phone) VALUES (?, ?, ?, ?, ?, ?)";
        return DBUtil.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                user.getRole(),
                user.getEmail(),
                user.getPhone()
        );
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM t_user WHERE username = ?";
        List<User> list = DBUtil.query(sql, User.class, username);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
