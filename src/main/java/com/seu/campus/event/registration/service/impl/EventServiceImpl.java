package com.seu.campus.event.registration.service.impl;

import com.seu.campus.event.registration.mapper.EventMapper;
import com.seu.campus.event.registration.mapper.impl.EventMapperImpl;
import com.seu.campus.event.registration.model.Event;
import com.seu.campus.event.registration.service.EventService;

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
        // 2. 时间校验 (逻辑：结束时间必须晚于开始时间)
        if (event.getStartTime() != null && event.getEndTime() != null) {
            if (event.getEndTime().before(event.getStartTime())) {
                return "结束时间不能早于开始时间";
            }
        }

        // 3. 设置默认状态
        event.setIsActive(1);

        // 4. 保存到数据库
        int rows = eventMapper.save(event);
        if (rows > 0) {
            return "SUCCESS";
        } else {
            return "发布失败：数据库保存错误";
        }
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
}

