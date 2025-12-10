package com.seu.campus.event.registration.mapper.impl;

import com.seu.campus.event.registration.mapper.EventMapper;
import com.seu.campus.event.registration.model.Event;
import com.seu.campus.event.registration.util.DBUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author XW
 */
public class EventMapperImpl implements EventMapper {
    @Override
    public int save(Event event) {
        String sql = "INSERT INTO t_event " +
                "(title, category, location, start_time, end_time, reg_deadline, detail, publisher_id, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return DBUtil.update(sql,
                event.getTitle(),
                event.getCategory(),
                event.getLocation(),
                event.getStartTime(),
                event.getEndTime(),
                event.getRegDeadline(),
                event.getDetail(),
                event.getPublisherId(),
                event.getIsActive());
    }

    @Override
    public List<Event> findAllActive() {
        String sql = "SELECT * FROM t_event WHERE is_active = 1 ORDER BY start_time DESC";
        return DBUtil.query(sql, Event.class);
    }

    @Override
    public Event findById(Integer eventId) {
        String sql = "SELECT * FROM t_event WHERE event_id = ?";
        List<Event> list = DBUtil.query(sql, Event.class, eventId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Event> findByPublisherId(Integer publisherId) {
        String sql = "SELECT * FROM t_event WHERE publisher_id = ? ORDER BY start_time DESC";
        return DBUtil.query(sql, Event.class, publisherId);
    }

    @Override
    public List<Event> findRegisteredByUserId(Integer userId) {
        // 🟢 修改 SQL：增加 r.status AS registration_status
        // 利用 DBUtil 的驼峰映射，registration_status 会自动注入到 Event 对象的 registrationStatus 字段
        String sql = "SELECT e.*, r.status AS registration_status FROM t_event e " +
                "JOIN t_registration r ON e.event_id = r.event_id " +
                "WHERE r.user_id = ? " +
                "ORDER BY r.reg_time DESC";
        return DBUtil.query(sql, Event.class, userId);
    }

    @Override
    public List<Event> search(String keyword, String category, String location, Date startDate, Date endDate) {
        // 1. 基础 SQL：只查有效的活动，并按时间倒序
        StringBuilder sql = new StringBuilder("SELECT * FROM t_event WHERE is_active = 1");
        List<Object> params = new ArrayList<>();

        // 2. 动态拼接条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (title LIKE ? OR detail LIKE ?)");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        if (category != null && !category.trim().isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category.trim());
        }

        if (location != null && !location.trim().isEmpty()) {
            // 地点支持模糊搜索
            sql.append(" AND location LIKE ?");
            params.add("%" + location.trim() + "%");
        }

        if (startDate != null) {
            sql.append(" AND start_time >= ?");
            params.add(startDate);
        }

        if (endDate != null) {
            sql.append(" AND start_time <= ?");
            params.add(endDate);
        }

        // 3. 排序
        sql.append(" ORDER BY start_time DESC");

        // 4. 执行查询 (List转Array)
        return DBUtil.query(sql.toString(), Event.class, params.toArray());
    }
}
