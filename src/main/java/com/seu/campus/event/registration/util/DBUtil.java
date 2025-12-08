package com.seu.campus.event.registration.util;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC 通用工具类
 * 功能：封装数据库连接、资源关闭、通用增删改、通用查询
 */
public class DBUtil {

    // ================= 配置区域 (请修改这里) =================
    // 1. 数据库连接 URL (MySQL 8.0 标准写法，包含时区和编码设置)
    private static final String URL = "jdbc:mysql://localhost:3306/db_campus_event?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&useSSL=false";
    // 2. 数据库用户名
    private static final String USER = "root";
    // 3. 数据库密码 (TODO: 请替换为你自己的密码)
    private static final String PASSWORD = "123456";

    // 驱动类名 (MySQL 8.0+)
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // ================= 核心代码 =================

    // 静态代码块：类加载时注册驱动 (只执行一次)
    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("错误：未找到 MySQL 驱动程序！请检查 pom.xml 依赖。");
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 通用增删改方法 (INSERT, UPDATE, DELETE)
     * @param sql    SQL 语句 (其中参数用 ? 占位)
     * @param params 参数数组，对应 SQL 中的 ?
     * @return 受影响的行数
     */
    public static int update(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        int rows = 0;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);

            // 填充参数
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            // 执行更新
            rows = ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("SQL执行异常: " + sql);
            e.printStackTrace();
        } finally {
            close(null, ps, conn);
        }
        return rows;
    }

    /**
     * 通用查询方法 (SELECT) - 利用反射封装结果集
     * @param sql    SQL 语句
     * @param clazz  实体类的 Class 对象 (例如 User.class)
     * @param params SQL 参数
     * @param <T>    泛型
     * @return 查询结果列表 List<T>
     */
    public static <T> List<T> query(String sql, Class<T> clazz, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<T> list = new ArrayList<>();

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            rs = ps.executeQuery();

            // 获取结果集的元数据 (这就好比拿到了表的结构信息)
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                // 1. 通过反射创建一个空的实例对象，例如 new User()
                T obj = clazz.getDeclaredConstructor().newInstance();

                // 2. 遍历这一行的每一列数据
                for (int i = 1; i <= columnCount; i++) {
                    // 获取列名 (例如 "user_id", "real_name")
                    String columnLabel = metaData.getColumnLabel(i);
                    // 获取列的值
                    Object value = rs.getObject(i);

                    // 3. 将数据库的下划线列名转为 Java 的驼峰属性名 (user_id -> userId)
                    String fieldName = toCamelCase(columnLabel);

                    // 4. 将值注入到对象的属性中
                    try {
                        Field field = clazz.getDeclaredField(fieldName);
                        field.setAccessible(true); // 允许访问 private 属性

                        // 特殊处理：数据库 Timestamp -> Java Date
                        if (value instanceof java.sql.Timestamp && field.getType() == java.util.Date.class) {
                            value = new java.util.Date(((java.sql.Timestamp) value).getTime());
                        }

                        field.set(obj, value);
                    } catch (NoSuchFieldException e) {
                        // 如果 Java 类里没有这个属性，就忽略 (比如 select * 查出了多余字段)
                    }
                }
                list.add(obj);
            }

        } catch (Exception e) {
            System.err.println("查询异常: " + sql);
            e.printStackTrace();
        } finally {
            close(rs, ps, conn);
        }
        return list;
    }

    /**
     * 辅助工具：下划线转驼峰命名
     * user_id -> userId
     * title -> title
     */
    private static String toCamelCase(String snakeCase) {
        if (snakeCase == null || !snakeCase.contains("_")) {
            return snakeCase;
        }
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 释放资源 (遵循后打开的先关闭原则)
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}