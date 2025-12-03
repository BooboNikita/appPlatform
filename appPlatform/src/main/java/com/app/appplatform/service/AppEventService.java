package com.app.appplatform.service;

import com.app.appplatform.entity.AppEvent;
import java.util.List;

public interface AppEventService {
    /**
     * 保存事件
     */
    void saveEvent(AppEvent appEvent);

    /**
     * 批量保存事件
     */
    void batchSaveEvents(List<AppEvent> events);

    /**
     * 获取最近的事件（分页）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    com.app.appplatform.common.PageResult<AppEvent> getRecentEvents(int pageNum, int pageSize);

    /**
     * 发送事件到消息队列
     */
    void sendToQueue(AppEvent event);
    
    /**
     * 获取最近的事件（兼容旧接口）
     * @deprecated 使用分页接口 {@link #getRecentEvents(int, int)} 替代
     */
    @Deprecated
    default List<AppEvent> getRecentEvents(int limit) {
        return getRecentEvents(1, limit).getList();
    }
}
