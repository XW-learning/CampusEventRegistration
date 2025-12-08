package com.seu.campus.event.registration.model;

import java.util.Date;

/**
 * 报名信息实体类
 * 对应数据库表: t_registration
 */
public class Registration {
    private Integer regId;        // reg_id
    private Integer eventId;      // event_id
    private Integer userId;       // user_id
    private Date regTime;         // reg_time
    private String status;        // status (pending/approved/rejected)
    private Integer isSignedIn;   // is_signed_in (1:是, 0:否)

    // 无参构造
    public Registration() {
    }

    // Getter / Setter
    public Integer getRegId() {
        return regId;
    }

    public void setRegId(Integer regId) {
        this.regId = regId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getRegTime() {
        return regTime;
    }

    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getIsSignedIn() {
        return isSignedIn;
    }

    public void setIsSignedIn(Integer isSignedIn) {
        this.isSignedIn = isSignedIn;
    }
}