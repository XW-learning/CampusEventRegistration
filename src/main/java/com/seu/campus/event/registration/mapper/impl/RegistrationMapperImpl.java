//package com.seu.campus.event.registration.mapper.impl;
//
//import com.seu.campus.event.registration.mapper.RegistrationMapper;
//import com.seu.campus.event.registration.model.Registration;
//import com.seu.campus.event.registration.util.DBUtil;
//
//import java.util.Date;
//import java.util.List;
//
///**
// * @author XW
// */
//public class RegistrationMapperImpl implements RegistrationMapper {
//    @Override
//    public int save(Registration reg) {
//        // 🟢 修改点 1：SQL 语句中增加 reg_time 字段
//        String sql = "INSERT INTO t_registration (event_id, user_id, contact_name, contact_phone, status, reg_time) VALUES (?, ?, ?, ?, ?, ?)";
//
//        return DBUtil.update(sql,
//                reg.getEventId(),
//                reg.getUserId(),
//                reg.getContactName(),
//                reg.getContactPhone(),
//                reg.getStatus() == null ? "pending" : reg.getStatus(),
//                new Date()
//        );
//    }
//
//    @Override
//    public Registration findByEventIdAndUserId(Integer eventId, Integer userId) {
//        String sql = "SELECT * FROM t_registration WHERE event_id = ? AND user_id = ?";
//        // 利用 DBUtil.query 查询，返回 List
//        List<Registration> list = DBUtil.query(sql, Registration.class, eventId, userId);
//
//        // 如果 List 为空说明没查到，返回 null；否则返回第一条
//        return list.isEmpty() ? null : list.get(0);
//    }
//
//    @Override
//    public List<Registration> findByEventId(Integer eventId) {
//        String sql = "SELECT * FROM t_registration WHERE event_id = ? AND status != 'cancelled' ORDER BY reg_time DESC";
//        return DBUtil.query(sql, Registration.class, eventId);
//    }
//
//    @Override
//    public int updateStatus(Integer regId, String status) {
//        String sql = "UPDATE t_registration SET status = ? WHERE reg_id = ?";
//        return DBUtil.update(sql, status, regId);
//    }
//
//    @Override
//    public int cancel(Integer userId, Integer eventId, String reason) {
//        String sql = "UPDATE t_registration SET status = 'cancelled', cancel_reason = ? WHERE user_id = ? AND event_id = ?";
//        return DBUtil.update(sql, reason, userId, eventId);
//    }
//
//    @Override
//    public void updateCheckinStatus(Integer userId, Integer eventId, Integer status, Date time) {
//        String sql = "UPDATE t_registration SET checkin_status = ?, checkin_time = ? WHERE user_id = ? AND event_id = ?";
//        DBUtil.update(sql, status, time, userId, eventId);
//    }
//
//    @Override
//    public int reJoin(Integer userId, Integer eventId) {
//        // 逻辑：将状态改为 pending，同时 重新报名次数 + 1
//        String sql = "UPDATE t_registration SET status = 'pending', re_join_count = re_join_count + 1 WHERE user_id = ? AND event_id = ?";
//        return DBUtil.update(sql, userId, eventId);
//    }
//}
