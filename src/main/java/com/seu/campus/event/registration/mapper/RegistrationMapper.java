package com.seu.campus.event.registration.mapper;

import com.seu.campus.event.registration.model.Registration;

/**
 * @author XW
 */
public interface RegistrationMapper {
    /**
     * 保存报名信息
     *
     * @param registration 报名信息
     * @return 影响行数
     */
    int save(Registration registration);

    /**
     * 根据活动ID和用户ID查询报名信息
     *
     * @param eventId 活动ID
     * @param userId  用户ID
     * @return 报名信息
     */
    Registration findByEventIdAndUserId(Integer eventId, Integer userId);
}
