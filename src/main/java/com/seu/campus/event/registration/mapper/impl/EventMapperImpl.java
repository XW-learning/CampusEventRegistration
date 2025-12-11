package com.seu.campus.event.registration.mapper.impl;

import com.seu.campus.event.registration.mapper.EventMapper;
import com.seu.campus.event.registration.model.Event;
import com.seu.campus.event.registration.util.DBUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author XW
 */
public class EventMapperImpl implements EventMapper {

    @Override
    public int save(Event event) {
        String sql = "INSERT INTO t_event (title, category, location, start_time, end_time, reg_deadline, detail, publisher_id, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return DBUtil.update(sql, event.getTitle(), event.getCategory(), event.getLocation(), event.getStartTime(), event.getEndTime(), event.getRegDeadline(), event.getDetail(), event.getPublisherId(), event.getIsActive());
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

    // 🟢 核心修复：添加 r.checkin_status
    @Override
    public List<Event> findRegisteredByUserId(Integer userId) {
        String sql = "SELECT e.*, r.status AS registration_status, r.checkin_status " +
                "FROM t_event e " +
                "JOIN t_registration r ON e.event_id = r.event_id " +
                "WHERE r.user_id = ? " +
                "ORDER BY r.reg_time DESC";
        return DBUtil.query(sql, Event.class, userId);
    }

    @Override
    public List<Event> searchByPage(String keyword, String category, String location, Date startDate, Date endDate, int offset, int pageSize) {
        StringBuilder sql = new StringBuilder("SELECT * FROM t_event WHERE is_active = 1");
        List<Object> params = new ArrayList<>();

        buildSearchCondition(sql, params, keyword, category, location, startDate, endDate);

        // 添加分页和排序
        sql.append(" ORDER BY start_time DESC LIMIT ?, ?");
        params.add(offset);
        params.add(pageSize);

        return DBUtil.query(sql.toString(), Event.class, params.toArray());
    }

    // 提取公共的查询条件构建逻辑
    private void buildSearchCondition(StringBuilder sql, List<Object> params, String keyword, String category, String location, Date startDate, Date endDate) {
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
            sql.append(" AND location LIKE ?");
            params.add("%" + location.trim() + "%");
        }
        if (startDate != null) {
            sql.append(" AND start_time >= ?");
            params.add(new Timestamp(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND start_time <= ?");
            params.add(new Timestamp(endDate.getTime()));
        }
    }

    // 🟢 补全：设置签到码方法
    @Override
    public void updateCheckinCode(Integer eventId, String code) {
        String sql = "UPDATE t_event SET checkin_code = ? WHERE event_id = ?";
        DBUtil.update(sql, code, eventId);
    }

    @Override
    public int countSearch(String keyword, String category, String location, Date startDate, Date endDate) {
        StringBuilder sql = new StringBuilder("SELECT count(*) FROM t_event WHERE is_active = 1");
        List<Object> params = new ArrayList<>();

        buildSearchCondition(sql, params, keyword, category, location, startDate, endDate);

        return DBUtil.queryCount(sql.toString(), params.toArray());
    }
}