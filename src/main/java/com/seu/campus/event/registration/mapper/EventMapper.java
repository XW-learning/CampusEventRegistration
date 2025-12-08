package com.seu.campus.event.registration.mapper;

import com.seu.campus.event.registration.model.Event;

import java.util.List;

/**
 * 活动数据访问层接口
 * 职能：定义针对 t_event 表的操作规范
 */
public interface EventMapper {
    /**
     * 保存活动
     *
     * @param event 活动对象
     * @return 影响行数
     */
    int save(Event event);

    /**
     * 查询所有活动
     *
     * @return 活动列表
     */
    List<Event> findAllActive();
}
