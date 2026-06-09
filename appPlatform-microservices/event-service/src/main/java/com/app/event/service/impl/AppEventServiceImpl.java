package com.app.event.service.impl;

import com.app.common.PageResult;
import com.app.event.entity.AppEvent;
import com.app.event.service.AppEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class AppEventServiceImpl implements AppEventService {

    private final BlockingQueue<AppEvent> eventQueue = new LinkedBlockingQueue<>();

    @Override
    public void saveEvent(AppEvent appEvent) {
        log.info("Saving event: {}", appEvent);
        // TODO: 实现数据库保存逻辑
    }

    @Override
    public void batchSaveEvents(List<AppEvent> events) {
        log.info("Batch saving {} events", events.size());
        // TODO: 实现批量数据库保存逻辑
    }

    @Override
    public PageResult<AppEvent> getRecentEvents(int pageNum, int pageSize,
                                               String userId, String userName, String nickname,
                                               String eventId, String pageUrl, String eventType,
                                               String filterKey, String filterValue) {
        log.info("Getting recent events, pageNum: {}, pageSize: {}", pageNum, pageSize);
        // TODO: 实现数据库查询逻辑
        int pages = 0;
        return new PageResult<>(new ArrayList<>(), 0, pageNum, pageSize, pages);
    }

    @Override
    public void sendToQueue(AppEvent event) {
        eventQueue.offer(event);
        log.info("Event sent to queue: {}", event);
    }
}
