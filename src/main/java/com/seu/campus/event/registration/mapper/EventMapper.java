package com.seu.campus.event.registration.mapper;

import com.seu.campus.event.registration.model.Event;

import java.util.List;
import java.util.Date;

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

    /**
     * 多条件组合筛选活动
     *
     * @param keyword   关键字 (标题或详情)
     * @param category  分类
     * @param location  地点
     * @param startDate 开始日期范围 - 起始
     * @param endDate   开始日期范围 - 结束
     * @return 符合条件的活动列表
     */
    List<Event> search(String keyword, String category, String location, Date startDate, Date endDate);

    /**
     * 更新签到码
     *
     * @param eventId 活动 ID
     * @param code    签到码
     * @return 影响行数
     */
    int updateCheckinCode(Integer eventId, String code);

}
