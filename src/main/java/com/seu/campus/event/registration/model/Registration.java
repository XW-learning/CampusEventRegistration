package com.seu.campus.event.registration.model;

import java.util.Date;

/**
 * 报名信息实体类
 * 对应数据库表: t_registration
 *
 * @author XW
 */
public class Registration {
    private Integer regId;
    private Integer eventId;
    private Integer userId;
    private Date regTime;
    private String status;
    private Integer isSignedIn;
    private String contactName;
    private String contactPhone;

    // 无参构造
    public Registration() {
    }

    public Registration(Integer regId, Integer eventId, Integer userId, Date regTime, String status, Integer isSignedIn, String contactName, String contactPhone) {
        this.regId = regId;
        this.eventId = eventId;
        this.userId = userId;
        this.regTime = regTime;
        this.status = status;
        this.isSignedIn = isSignedIn;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
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

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}