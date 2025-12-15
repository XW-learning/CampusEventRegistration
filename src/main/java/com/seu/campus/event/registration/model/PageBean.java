package com.seu.campus.event.registration.model;

import java.util.List;

/**
 * 分页结果封装类
 *
 * @param <T> 数据类型 (这里是 Event)
 */
public class PageBean<T> {
    private int currentPage;
    private int pageSize;
    private int totalCount;
    private int totalPage;
    private List<T> list;

    // 构造函数中计算总页数
    public PageBean(int currentPage, int pageSize, int totalCount, List<T> list) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.list = list;

        // 计算总页数：总数 / 每页数，如果有余数则 +1
        this.totalPage = (totalCount % pageSize == 0) ? (totalCount / pageSize) : (totalCount / pageSize + 1);
    }

    // Getter Setter
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}