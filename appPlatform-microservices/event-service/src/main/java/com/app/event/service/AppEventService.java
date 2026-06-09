package com.app.event.service;

import com.app.common.PageResult;
import com.app.event.entity.AppEvent;
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
     * @param userId 用户ID（模糊查询）
     * @param userName 用户名（模糊查询，优先级高于 nickname）
     * @param nickname 用户昵称（模糊查询，仅在 userName 为空时使用，会转换为用户名查询）
     * @param eventId 事件ID（模糊查询，对应 eventInfo.eventId）
     * @param pageUrl 页面URL（模糊查询）
     * @param eventType 事件类型（精确匹配）
     * @param filterKey 过滤键
     * @param filterValue 过滤值
     * @return 分页结果
     */
    PageResult<AppEvent> getRecentEvents(int pageNum, int pageSize, 
                                       String userId, String userName, String nickname,
                                       String eventId, String pageUrl, String eventType, 
                                       String filterKey, String filterValue);

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
        return getRecentEvents(1, limit, null, null, null, null, null, null, null, null).getList();
    }
}
