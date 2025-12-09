package com.seu.campus.event.registration.mapper;

import com.seu.campus.event.registration.model.Event;

import java.util.List;

/**
 * 活动数据访问层接口
 * 职能：定义针对 t_event 表的操作规范
 *
 * @author XW
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

    /**
     * 根据活动 ID 查询活动（用于报名时检查时间和状态）
     *
     * @param eventId 活动 ID
     * @return 活动对象
     */
    Event findById(Integer eventId);

    /**
     * 根据发布者 ID 查询该发布者发布的活动
     *
     * @param publisherId 发布者 ID
     * @return 活动列表
     */
    List<Event> findByPublisherId(Integer publisherId);

    /**
     * 根据用户 ID 查询该用户报名的活动
     *
     * @param userId 用户 ID
     * @return 活动列表
     */
    List<Event> findRegisteredByUserId(Integer userId);
}
