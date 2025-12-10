package com.seu.campus.event.registration.service;


import com.seu.campus.event.registration.model.Event;

import java.util.List;

/**
 * @author XW
 */
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
     *
     * @return 活动列表
     */
    List<Event> findAllActiveEvents(); // <-- 新增

    /**
     * 获取我的相关活动
     *
     * @param userId 用户 ID
     * @param type   查询类型："registered" 查询已报名的活动，"created" 查询已创建的活动
     * @return 活动列表
     */
    List<Event> findMyEvents(Integer userId, String type);

    /**
     * 搜索活动
     *
     * @param keyword      关键词
     * @param category     活动类别
     * @param location     活动地点
     * @param startDateStr 开始时间
     * @param endDateStr   结束时间
     * @return 活动列表
     */
    List<Event> searchEvents(String keyword, String category, String location, String startDateStr, String endDateStr);

    /**
     * 设置签到码
     *
     * @param userId  用户 ID
     * @param eventId 活动 ID
     * @param code    签到码
     * @return 状态信息
     */
    String setCheckinCode(Integer userId, Integer eventId, String code);
}
