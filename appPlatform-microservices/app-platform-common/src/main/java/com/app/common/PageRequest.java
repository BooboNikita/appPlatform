package com.app.common;

public class PageRequest {
    private int pageNum = 1;
    private int pageSize = 10;

    public int getPageNum() {
        return Math.max(1, pageNum);
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return Math.min(100, Math.max(1, pageSize));
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
