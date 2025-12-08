package com.seu.campus.event.registration.mapper;

import com.seu.campus.event.registration.model.User;

/**
 * 用户数据访问层接口
 * 职能：定义针对 t_user 表的操作规范
 */
public interface UserMapper {

    /**
     * 保存用户
     *
     * @param user 用户对象
     * @return 影响行数
     */
    int save(User user);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User findByUsername(String username);
}
