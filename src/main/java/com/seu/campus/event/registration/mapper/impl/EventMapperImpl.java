package com.seu.campus.event.registration.mapper.impl;

import com.seu.campus.event.registration.mapper.EventMapper;
import com.seu.campus.event.registration.model.Event;
import com.seu.campus.event.registration.util.DBUtil;

import java.util.List;

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
}
