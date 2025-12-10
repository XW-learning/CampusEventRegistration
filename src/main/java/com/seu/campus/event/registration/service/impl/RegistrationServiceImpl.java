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
        reg.setStatus("pending");
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

    @Override
    public String audit(String regIdsStr, String status) {
        if (regIdsStr == null || regIdsStr.isEmpty()) return "未选择任何记录";

        String[] ids = regIdsStr.split(",");
        int count = 0;

        for (String idStr : ids) {
            try {
                Integer regId = Integer.parseInt(idStr);
                count += registrationMapper.updateStatus(regId, status);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return count > 0 ? "SUCCESS" : "审核失败：未更新任何记录";
    }

    @Override
    public String cancel(Integer userId, Integer eventId, String reason) {
        // 1. 检查是否存在
        Registration reg = registrationMapper.findByEventIdAndUserId(eventId, userId);
        if (reg == null) {
            return "未找到报名记录";
        }

        // 2. 检查是否已经取消 (避免重复提交)
        if ("cancelled".equals(reg.getStatus())) {
            return "该报名已取消";
        }

        // 3. 执行取消
        int rows = registrationMapper.cancel(userId, eventId, reason);
        return rows > 0 ? "SUCCESS" : "取消失败，系统错误";
    }

    @Override
    public String verifyCheckin(Integer userId, Integer eventId, String inputCode) {
        Event event = eventMapper.findById(eventId);
        if (event == null || event.getCheckinCode() == null) {
            return "活动未开启签到";
        }

        // 忽略大小写比较
        if (!event.getCheckinCode().equalsIgnoreCase(inputCode.trim())) {
            return "签到码错误";
        }

        // 更新状态
        registrationMapper.updateCheckinStatus(userId, eventId, 1, new Date());
        return "SUCCESS";
    }

    @Override
    public String reJoin(Integer userId, Integer eventId) {
        // 1. 查询当前记录
        Registration reg = registrationMapper.findByEventIdAndUserId(eventId, userId);

        if (reg == null) {
            return "未找到原来的报名记录";
        }

        // 2. 只有“已取消”状态才能重新报名
        // 注意：你数据库里存的是 "cancelled" (英文)，我在 MapperImpl 里看到了
        if (!"cancelled".equals(reg.getStatus())) {
            return "当前状态无法重新报名";
        }

        // 3. 检查次数限制 (使用你在实体类里加的 reJoinCount 字段)
        // 如果数据库字段默认为 NULL，Java 对象里可能是 null，要注意判空
        int currentCount = reg.getReJoinCount() == null ? 0 : reg.getReJoinCount();

        if (currentCount >= 3) {
            return "您的重新报名次数已用完（限3次）";
        }

        // 4. 执行更新
        int rows = registrationMapper.reJoin(userId, eventId);
        return rows > 0 ? "SUCCESS" : "操作失败，请重试";
    }

}
