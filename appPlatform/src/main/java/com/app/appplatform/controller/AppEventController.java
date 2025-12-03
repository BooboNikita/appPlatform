package com.app.appplatform.controller;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.common.Result;
import com.app.appplatform.entity.AppEvent;
import com.app.appplatform.service.AppEventService;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-events")
public class AppEventController {

    @Autowired
    private AppEventService appEventService;

    /**
     * 提交单个事件
     */
    @PermitAll
    @PostMapping("/submit")
    public Result<?> submitEvent(@RequestBody AppEvent event) {
        appEventService.sendToQueue(event);
        return Result.success("提交成功");
    }

    /**
     * 批量提交事件
     */
    @PermitAll
    @PostMapping("/batch")
    public Result<?> batchSubmitEvents(@RequestBody List<AppEvent> events) {
        events.forEach(appEventService::sendToQueue);
        return Result.success("提交成功");
    }

    /**
     * 获取最近的事件
     */
    @PermitAll
    @GetMapping("/recent")
    public Result<PageResult<AppEvent>> getRecentEvents(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(appEventService.getRecentEvents(pageNum, pageSize));
    }
}
