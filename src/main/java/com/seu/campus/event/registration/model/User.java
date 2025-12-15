package com.seu.campus.event.registration.model;

/**
 * 用户实体类
 * 对应数据库表: t_user
 *
 * @author XW
 */
public class User {
    // 1. 私有属性 (对应 DB 字段)
    private Integer userId;
    private String username;
    private String password;
    private String realName;
    private String role;
    private String email;
    private String phone;

    // 2. 无参构造函数 (反射必须)
    public User() {
    }

    // 3. 全参构造函数 (可选，方便手动创建对象)
    public User(Integer userId, String username, String password, String realName, String role, String email, String phone) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.role = role;
        this.email = email;
        this.phone = phone;
    }

    // 4. Getter 和 Setter 方法
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // 方便调试打印
    @Override
    public String toString() {
        return "User{userId=" + userId + ", username='" + username + "', realName='" + realName + "'}";
    }
}