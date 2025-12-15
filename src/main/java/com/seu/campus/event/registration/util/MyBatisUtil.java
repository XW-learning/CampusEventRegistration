package com.seu.campus.event.registration.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * MyBatis 工具类
 * 负责 SqlSessionFactory 的创建与 SqlSession 的获取
 * @author XW
 */
public class MyBatisUtil {

    private static final SqlSessionFactory SQL_SESSION_FACTORY;

    static {
        try {
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SQL_SESSION_FACTORY = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("初始化 MyBatis 失败", e);
        }
    }

    /**
     * 获取 SqlSession（手动提交事务）
     */
    public static SqlSession openSession() {
        return SQL_SESSION_FACTORY.openSession();
    }
}
