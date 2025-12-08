package com.seu.campus.event.registration.service;


import com.seu.campus.event.registration.model.Event;

public interface EventService {
    /**
     * 发布活动
     *
     * @param event 活动对象
     * @return 状态信息
     */
    String publishEvent(Event event);
}
