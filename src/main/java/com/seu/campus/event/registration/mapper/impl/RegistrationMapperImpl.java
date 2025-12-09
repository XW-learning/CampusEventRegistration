package com.seu.campus.event.registration.mapper.impl;

import com.seu.campus.event.registration.mapper.RegistrationMapper;
import com.seu.campus.event.registration.model.Registration;
import com.seu.campus.event.registration.util.DBUtil;

import java.util.Date;
import java.util.List;

/**
 * @author XW
 */
public class RegistrationMapperImpl implements RegistrationMapper {
    @Override
    public int save(Registration reg) {
        // 🟢 修改点 1：SQL 语句中增加 reg_time 字段
        String sql = "INSERT INTO t_registration (event_id, user_id, contact_name, contact_phone, status, reg_time) VALUES (?, ?, ?, ?, ?, ?)";

        return DBUtil.update(sql,
                reg.getEventId(),
                reg.getUserId(),
                reg.getContactName(),
                reg.getContactPhone(),
                reg.getStatus() == null ? 0 : reg.getStatus(),
                // 🟢 修改点 2：显式传入当前时间对象
                new Date()
        );
    }

    @Override
    public Registration findByEventIdAndUserId(Integer eventId, Integer userId) {
        String sql = "SELECT * FROM t_registration WHERE event_id = ? AND user_id = ?";
        // 利用 DBUtil.query 查询，返回 List
        List<Registration> list = DBUtil.query(sql, Registration.class, eventId, userId);

        // 如果 List 为空说明没查到，返回 null；否则返回第一条
        return list.isEmpty() ? null : list.get(0);
    }
}
