package com.seu.campus.event.registration.service.impl;

import com.seu.campus.event.registration.mapper.EventMapper;
import com.seu.campus.event.registration.mapper.RegistrationMapper;
import com.seu.campus.event.registration.mapper.impl.EventMapperImpl;
import com.seu.campus.event.registration.mapper.impl.RegistrationMapperImpl;
import com.seu.campus.event.registration.model.Event;
import com.seu.campus.event.registration.model.Registration;
import com.seu.campus.event.registration.service.RegistrationService;

import java.util.Date;
import java.util.List;

/**
 * 报名服务实现类
 * 主要负责处理活动报名相关的业务逻辑，包括：
 * 1. 验证活动是否存在和有效性
 * 2. 检查报名截止时间
 * 3. 防止重复报名
 * 4. 保存报名信息
 *
 * @author XW
 */

public class RegistrationServiceImpl implements RegistrationService {
    private final RegistrationMapper registrationMapper = new RegistrationMapperImpl();
    private final EventMapper eventMapper = new EventMapperImpl();

    @Override
    public String register(Integer userId, Integer eventId, String contactName, String contactPhone) {
        // 1. 校验活动是否存在
        Event event = eventMapper.findById(eventId);
        if (event == null) {
            return "活动不存在";
        }

        // 2. 校验活动是否截止
        if (event.getRegDeadline() != null && new Date().after(event.getRegDeadline())) {
            return "报名已截止";
        }

        // 3. 校验重复报名
        Registration exist = registrationMapper.findByEventIdAndUserId(eventId, userId);
        if (exist != null) {
            return "您已报名过该活动，请勿重复提交";
        }

        // 4. 组装对象并保存
        Registration reg = new Registration();
        reg.setUserId(userId);
        reg.setEventId(eventId);
        reg.setContactName(contactName);
        reg.setContactPhone(contactPhone);
        reg.setStatus("1");
        return registrationMapper.save(reg) > 0 ? "SUCCESS" : "系统繁忙，请重试";
    }

    @Override
    public List<Registration> getRegistrationList(Integer userId, Integer eventId) {
        // 1. 安全校验：确保当前用户是该活动的发布者 (防止恶意查看)
        Event event = eventMapper.findById(eventId);
        if (event == null || !event.getPublisherId().equals(userId)) {
            return null;
        }

        // 2. 查询报名列表
        return registrationMapper.findByEventId(eventId);
    }
}