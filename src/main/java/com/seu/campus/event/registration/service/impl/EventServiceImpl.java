package com.seu.campus.event.registration.service.impl;

import com.seu.campus.event.registration.mapper.EventMapper;
import com.seu.campus.event.registration.mapper.impl.EventMapperImpl;
import com.seu.campus.event.registration.model.Event;
import com.seu.campus.event.registration.service.EventService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 活动服务实现类
 * 主要负责处理活动相关的业务逻辑，包括：
 * 1. 发布新活动（包含数据校验和保存）
 * 2. 查询所有有效的活动列表
 *
 * @author XW
 */

public class EventServiceImpl implements EventService {
    private final EventMapper eventMapper = new EventMapperImpl();

    @Override
    public String publishEvent(Event event) {
        // 1. 基础校验
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            return "活动标题不能为空";
        }

        // <-- 修改在这里：主动清洗数据 (Trim)，防止数据库存入多余空格
        if (event.getTitle() != null) {
            event.setTitle(event.getTitle().trim());
        }
        if (event.getCategory() != null) {
            event.setCategory(event.getCategory().trim());
        }
        if (event.getLocation() != null) {
            event.setLocation(event.getLocation().trim());
        }

        // 2. 时间校验
        if (event.getStartTime() != null && event.getEndTime() != null) {
            if (event.getEndTime().before(event.getStartTime())) {
                return "结束时间不能早于开始时间";
            }
        }

        // 3. 设置默认状态
        event.setIsActive(1);

        // 4. 保存
        int rows = eventMapper.save(event);
        return rows > 0 ? "SUCCESS" : "发布失败：数据库保存错误";
    }

    @Override
    public List<Event> findAllActiveEvents() {
        return eventMapper.findAllActive();
    }

    @Override
    public List<Event> findMyEvents(Integer userId, String type) {
        if ("published".equals(type)) {
            return eventMapper.findByPublisherId(userId);
        } else if ("joined".equals(type)) {
            return eventMapper.findRegisteredByUserId(userId);
        }
        return List.of();
    }

    @Override
    public List<Event> searchEvents(String keyword, String category, String location, String startDateStr, String endDateStr) {
        Date startDate = null;
        Date endDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            // 处理日期：前端传的是 yyyy-MM-dd
            if (startDateStr != null && !startDateStr.isEmpty()) {
                // 补全时间为当天的 00:00:00
                startDate = sdf.parse(startDateStr + " 00:00:00");
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                // 补全时间为当天的 23:59:59，确保包含当天活动
                endDate = sdf.parse(endDateStr + " 23:59:59");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 日期格式错误时忽略日期条件，防止报错
        }

        return eventMapper.search(keyword, category, location, startDate, endDate);
    }

    @Override
    public String setCheckinCode(Integer userId, Integer eventId, String code) {
        Event event = eventMapper.findById(eventId);
        if (event == null || !event.getPublisherId().equals(userId)) {
            return "权限不足";
        }

        eventMapper.updateCheckinCode(eventId, code);
        return "SUCCESS";
    }
}

