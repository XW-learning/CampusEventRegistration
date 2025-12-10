package com.seu.campus.event.registration.model;

import java.util.Date;

/**
 * 活动实体类
 * 对应数据库表: t_event
 *
 * @author XW
 */
public class Event {
    private Integer eventId;
    private String title;
    private String category;
    private String location;
    private Date startTime;
    private Date endTime;
    private Date regDeadline;
    private String detail;
    private Integer publisherId;
    private Integer isActive;

    // 🟢 新增字段 1：用于存储当前用户的报名状态
    private String registrationStatus;

    // 🟢 新增字段 2: 数据库的签到码 (t_event.checkin_code)
    private String checkinCode;

    // 🟢 新增字段 3: 告诉前端是否有码 (虚拟字段，不存库)
    private boolean hasCheckinCode;

    // 🟢 新增字段 4: 当前用户的签到状态 (0:未签, 1:已签)，来自 t_registration
    private Integer checkinStatus;

    public Event() {
    }

    // --- Getter / Setter ---

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getRegDeadline() {
        return regDeadline;
    }

    public void setRegDeadline(Date regDeadline) {
        this.regDeadline = regDeadline;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Integer getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    // 🟢 新增字段的 Getter/Setter
    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public String getCheckinCode() {
        return checkinCode;
    }

    public void setCheckinCode(String checkinCode) {
        this.checkinCode = checkinCode;
    }

    public boolean isHasCheckinCode() {
        return hasCheckinCode;
    }

    public void setHasCheckinCode(boolean hasCheckinCode) {
        this.hasCheckinCode = hasCheckinCode;
    }

    public Integer getCheckinStatus() {
        return checkinStatus;
    }

    public void setCheckinStatus(Integer checkinStatus) {
        this.checkinStatus = checkinStatus;
    }
}