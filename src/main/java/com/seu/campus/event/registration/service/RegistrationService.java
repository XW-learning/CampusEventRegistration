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
}
