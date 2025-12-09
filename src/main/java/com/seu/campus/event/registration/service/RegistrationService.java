package com.seu.campus.event.registration.service;

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
}
