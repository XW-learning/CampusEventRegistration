package com.seu.campus.event.registration.service.impl;

import com.seu.campus.event.registration.mapper.EventMapper;
import com.seu.campus.event.registration.model.Event;
import com.seu.campus.event.registration.model.PageBean;
import com.seu.campus.event.registration.service.EventService;
import com.seu.campus.event.registration.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 活动服务实现类（封装 SqlSession 后）
 *
 * @author XW
 */
public class EventServiceImpl implements EventService {

    @Override
    public String publishEvent(Event event) {
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            return "活动标题不能为空";
        }
        event.setTitle(event.getTitle().trim());
        if (event.getCategory() != null) event.setCategory(event.getCategory().trim());
        if (event.getLocation() != null) event.setLocation(event.getLocation().trim());
        if (event.getStartTime() != null && event.getEndTime() != null
                && event.getEndTime().before(event.getStartTime())) {
            return "结束时间不能早于开始时间";
        }
        event.setIsActive(1);

        try (SqlSession session = MyBatisUtil.openSession()) {
            EventMapper mapper = session.getMapper(EventMapper.class);
            int rows = mapper.save(event);
            session.commit();
            return rows > 0 ? "SUCCESS" : "发布失败";
        }
    }

    @Override
    public List<Event> findAllActiveEvents() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            return session.getMapper(EventMapper.class).findAllActive();
        }
    }

    @Override
    public List<Event> findMyEvents(Integer userId, String type) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EventMapper mapper = session.getMapper(EventMapper.class);
            if ("published".equals(type)) {
                return mapper.findByPublisherId(userId);
            } else if ("joined".equals(type)) {
                return mapper.findRegisteredByUserId(userId);
            }
            return Collections.emptyList();
        }
    }

    @Override
    public PageBean<Event> searchEvents(String keyword, String category, String location,
                                        String startDateStr, String endDateStr,
                                        int page, int pageSize) {

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
        } catch (Exception ignored) {
        }

        int offset = (page - 1) * pageSize;

        try (SqlSession session = MyBatisUtil.openSession()) {
            EventMapper mapper = session.getMapper(EventMapper.class);
            List<Event> list = mapper.searchByPage(
                    keyword, category, location, startDate, endDate, offset, pageSize);
            int total = mapper.countSearch(
                    keyword, category, location, startDate, endDate);
            return new PageBean<>(page, pageSize, total, list);
        }
    }

    @Override
    public String setCheckinCode(Integer userId, Integer eventId, String code) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EventMapper mapper = session.getMapper(EventMapper.class);
            Event event = mapper.findById(eventId);
            if (event == null || !event.getPublisherId().equals(userId)) {
                return "权限不足";
            }
            mapper.updateCheckinCode(eventId, code);
            session.commit();
            return "SUCCESS";
        }
    }
}
