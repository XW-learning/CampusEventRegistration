package com.seu.campus.event.registration.service.impl;

import com.seu.campus.event.registration.mapper.EventMapper;
import com.seu.campus.event.registration.model.Event;
import com.seu.campus.event.registration.model.PageBean;
import com.seu.campus.event.registration.service.EventService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 活动服务实现类（修正版）
 * 修复了 SqlSession 长期持有导致的缓存不刷新和线程安全问题
 *
 * @author XW
 */
public class EventServiceImpl implements EventService {

    // 1. Factory 是线程安全的，全局只需要一份
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

    @Override
    public String publishEvent(Event event) {
        // 校验逻辑
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            return "活动标题不能为空";
        }
        event.setTitle(event.getTitle().trim());
        if (event.getCategory() != null) {
            event.setCategory(event.getCategory().trim());
        }
        if (event.getLocation() != null) {
            event.setLocation(event.getLocation().trim());
        }
        if (event.getStartTime() != null && event.getEndTime() != null && event.getEndTime().before(event.getStartTime())) {
            return "结束时间不能早于开始时间";
        }
        event.setIsActive(1);

        // 2. 在方法内部获取 Session，自动关闭 (try-with-resources)
        try (SqlSession session = SQL_SESSION_FACTORY.openSession()) {
            EventMapper mapper = session.getMapper(EventMapper.class);
            int rows = mapper.save(event);
            session.commit();
            return rows > 0 ? "SUCCESS" : "发布失败";
        }
    }

    @Override
    public List<Event> findAllActiveEvents() {
        try (SqlSession session = SQL_SESSION_FACTORY.openSession()) {
            EventMapper mapper = session.getMapper(EventMapper.class);
            return mapper.findAllActive();
        }
    }

    @Override
    public List<Event> findMyEvents(Integer userId, String type) {
        try (SqlSession session = SQL_SESSION_FACTORY.openSession()) {
            EventMapper mapper = session.getMapper(EventMapper.class);
            if ("published".equals(type)) {
                return mapper.findByPublisherId(userId);
            } else if ("joined".equals(type)) {
                // 这里每次都会打开新 Session，强制查库，不会遭遇一级缓存不刷新的问题
                return mapper.findRegisteredByUserId(userId);
            }
            return Collections.emptyList();
        }
    }

    @Override
    public PageBean<Event> searchEvents(String keyword, String category, String location, String startDateStr, String endDateStr, int page, int pageSize) {
        Date startDate = null;
        Date endDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = sdf.parse(startDateStr + " 00:00:00");
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = sdf.parse(endDateStr + " 23:59:59");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int offset = (page - 1) * pageSize;

        try (SqlSession session = SQL_SESSION_FACTORY.openSession()) {
            EventMapper mapper = session.getMapper(EventMapper.class);
            List<Event> list = mapper.searchByPage(keyword, category, location, startDate, endDate, offset, pageSize);
            int totalCount = mapper.countSearch(keyword, category, location, startDate, endDate);
            return new PageBean<>(page, pageSize, totalCount, list);
        }
    }

    @Override
    public String setCheckinCode(Integer userId, Integer eventId, String code) {
        try (SqlSession session = SQL_SESSION_FACTORY.openSession()) {
            EventMapper mapper = session.getMapper(EventMapper.class);
            Event event = mapper.findById(eventId);
            if (event == null || !event.getPublisherId().equals(userId)) {
                return "权限不足";
            }
            mapper.updateCheckinCode(eventId, code);
            session.commit(); // 提交事务
            return "SUCCESS";
        }
    }
}