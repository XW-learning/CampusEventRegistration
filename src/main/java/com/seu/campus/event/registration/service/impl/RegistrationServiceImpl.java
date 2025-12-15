package com.seu.campus.event.registration.service.impl;

import com.seu.campus.event.registration.mapper.EventMapper;
import com.seu.campus.event.registration.mapper.RegistrationMapper;
import com.seu.campus.event.registration.model.Event;
import com.seu.campus.event.registration.model.Registration;
import com.seu.campus.event.registration.service.RegistrationService;
import com.seu.campus.event.registration.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.util.Date;
import java.util.List;

/**
 * 报名服务实现类（封装 SqlSession 后）
 *
 * @author XW
 */
public class RegistrationServiceImpl implements RegistrationService {

    @Override
    public String register(Integer userId, Integer eventId,
                           String contactName, String contactPhone) {

        try (SqlSession session = MyBatisUtil.openSession()) {
            EventMapper eventMapper = session.getMapper(EventMapper.class);
            RegistrationMapper regMapper = session.getMapper(RegistrationMapper.class);

            Event event = eventMapper.findById(eventId);
            if (event == null) return "活动不存在";
            if (event.getRegDeadline() != null
                    && new Date().after(event.getRegDeadline())) {
                return "报名已截止";
            }

            if (regMapper.findByEventIdAndUserId(eventId, userId) != null) {
                return "您已报名过该活动";
            }

            Registration reg = new Registration();
            reg.setUserId(userId);
            reg.setEventId(eventId);
            reg.setContactName(contactName);
            reg.setContactPhone(contactPhone);
            reg.setStatus("pending");

            regMapper.save(reg);
            session.commit();
            return "SUCCESS";
        }
    }

    @Override
    public List<Registration> getRegistrationList(Integer userId, Integer eventId) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EventMapper eventMapper = session.getMapper(EventMapper.class);
            RegistrationMapper regMapper = session.getMapper(RegistrationMapper.class);

            Event event = eventMapper.findById(eventId);
            if (event == null || !event.getPublisherId().equals(userId)) {
                return null;
            }
            return regMapper.findByEventId(eventId);
        }
    }

    @Override
    public String audit(String regIdsStr, String status) {
        if (regIdsStr == null || regIdsStr.isEmpty()) return "未选择任何记录";
        String[] ids = regIdsStr.split(",");
        int count = 0;

        try (SqlSession session = MyBatisUtil.openSession()) {
            RegistrationMapper regMapper = session.getMapper(RegistrationMapper.class);
            for (String idStr : ids) {
                try {
                    count += regMapper.updateStatus(Integer.parseInt(idStr), status);
                } catch (NumberFormatException ignored) {
                }
            }
            session.commit();
            return count > 0 ? "SUCCESS" : "无变更";
        }
    }

    @Override
    public String cancel(Integer userId, Integer eventId, String reason) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            RegistrationMapper regMapper = session.getMapper(RegistrationMapper.class);

            Registration reg = regMapper.findByEventIdAndUserId(eventId, userId);
            if (reg == null) return "未找到报名记录";
            if ("cancelled".equals(reg.getStatus())) return "该报名已取消";

            regMapper.cancel(userId, eventId, reason);
            session.commit();
            return "SUCCESS";
        }
    }

    @Override
    public String verifyCheckin(Integer userId, Integer eventId, String inputCode) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            EventMapper eventMapper = session.getMapper(EventMapper.class);
            RegistrationMapper regMapper = session.getMapper(RegistrationMapper.class);

            Event event = eventMapper.findById(eventId);
            if (event == null || event.getCheckinCode() == null) {
                return "活动未开启签到";
            }
            if (!event.getCheckinCode().equalsIgnoreCase(inputCode.trim())) {
                return "签到码错误";
            }

            regMapper.updateCheckinStatus(userId, eventId, 1, new Date());
            session.commit();
            return "SUCCESS";
        }
    }

    @Override
    public String reJoin(Integer userId, Integer eventId) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            RegistrationMapper regMapper = session.getMapper(RegistrationMapper.class);

            Registration reg = regMapper.findByEventIdAndUserId(eventId, userId);
            if (reg == null) return "未找到记录";
            if (!"cancelled".equals(reg.getStatus())) return "状态不符";

            int count = reg.getReJoinCount() == null ? 0 : reg.getReJoinCount();
            if (count >= 3) return "重报次数已耗尽";

            regMapper.reJoin(userId, eventId);
            session.commit();
            return "SUCCESS";
        }
    }

    @Override
    public Event getEventById(Integer eventId) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            return session.getMapper(EventMapper.class).findById(eventId);
        }
    }
}
