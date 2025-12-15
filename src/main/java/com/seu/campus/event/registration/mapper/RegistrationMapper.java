package com.seu.campus.event.registration.mapper;

import com.seu.campus.event.registration.model.Registration;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

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
    Registration findByEventIdAndUserId(@Param("eventId") Integer eventId,
                                        @Param("userId") Integer userId);

    /**
     * 根据活动 ID 查询该活动下的所有报名信息
     *
     * @param eventId 活动 ID
     * @return 报名信息列表
     */
    List<Registration> findByEventId(Integer eventId);

    /**
     * 修改报名状态
     *
     * @param regId  报名 ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("regId") Integer regId,
                     @Param("status") String status);

    /**
     * 取消报名
     *
     * @param userId  用户 ID
     * @param eventId 活动 ID
     * @param reason  取消原因
     */
    void cancel(@Param("userId") Integer userId,
                @Param("eventId") Integer eventId,
                @Param("reason") String reason);

    /**
     * 更新签到状态
     *
     * @param userId  用户 ID
     * @param eventId 活动 ID
     * @param status  签到状态
     * @param time    签到时间
     */
    void updateCheckinStatus(@Param("userId") Integer userId,
                             @Param("eventId") Integer eventId,
                             @Param("status") Integer status,
                             @Param("time") Date time);

    /**
     * 重新报名（状态改回 pending，且次数+1）
     *
     * @param userId  用户 ID
     * @param eventId 活动 ID
     */
    void reJoin(@Param("userId") Integer userId,
                @Param("eventId") Integer eventId);
}
