package com.seu.campus.event.registration.service;


import com.seu.campus.event.registration.model.Event;

import java.util.List;

public interface EventService {
    /**
     * 发布活动
     *
     * @param event 活动对象
     * @return 状态信息
     */
    String publishEvent(Event event);

    /**
     * 查询所有有效活动
     * @return 活动列表
     */
    List<Event> findAllActiveEvents(); // <-- 新增
}
