package com.seu.campus.event.registration.service;

import com.seu.campus.event.registration.model.User;

/**
 * 用户业务逻辑层接口
 * 职能：处理登录、注册等具体业务规则
 */
public interface UserService {

    /**
     * 用户注册业务
     *
     * @param user 用户填写的注册信息
     * @return 注册结果消息 (例如："SUCCESS" 表示成功，其他字符串表示具体的错误原因)
     */
    String register(User user);

    /**
     * 用户登录业务
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果消息 (例如："SUCCESS" 登录成功，其他字符串表示具体的错误原因)
     */
    User login(String username, String password);

}
