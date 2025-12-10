package com.seu.campus.event.registration.service;

import com.seu.campus.event.registration.model.Registration;

import java.util.List;

/**
 * @author XW
 */
public interface RegistrationService {
    /**
     * 处理活动报名
     *
     * @param userId  当前登录用户ID
     * @param eventId 要报名的活动ID
     * @return 处理结果 ("SUCCESS" 或 错误提示信息)
     */
    String register(Integer userId, Integer eventId, String contactName, String contactPhone);


    /**
     * 获取名单列表
     *
     * @param userId  当前登录用户ID
     * @param eventId 要查询的报名信息对应的活动ID
     * @return 报名信息列表
     */
    List<Registration> getRegistrationList(Integer userId, Integer eventId);

    /**
     * 批量审核
     *
     * @param regIdsStr 逗号分隔的ID字符串 (如 "101,102,103")
     * @param status    目标状态 (1:通过, 2:拒绝)
     */
    String audit(String regIdsStr, String status);

    /**
     * 取消报名
     *
     * @param userId  当前登录用户ID
     * @param eventId 要取消报名的活动ID
     * @param reason  取消报名的原因
     * @return 处理结果 ("SUCCESS" 或 错误提示信息)
     */
    String cancel(Integer userId, Integer eventId, String reason);

    /**
     * 签到
     *
     * @param userId    当前登录用户ID
     * @param eventId   要签到的活动ID
     * @param inputCode 输入的签到码
     * @return 处理结果 ("SUCCESS" 或 错误提示信息)
     */
    String verifyCheckin(Integer userId, Integer eventId, String inputCode);

    /**
     * 重新报名
     *
     * @param userId  当前登录用户ID
     * @param eventId 要重新报名的活动ID
     * @return 处理结果
     */
    String reJoin(Integer userId, Integer eventId);

}
