package com.app.appplatform.controller;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.common.Result;
import com.app.appplatform.entity.AppEvent;
import com.app.appplatform.service.AppEventService;
import com.app.appplatform.service.ConfigService;
import io.jsonwebtoken.lang.Maps;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-events")
public class AppEventController {

    private static final String CONFIG_KEY_EVENT_TRACKING = "event_tracking_enabled";

    final private AppEventService appEventService;

    final private ConfigService configService;

    AppEventController(AppEventService appEventService, ConfigService configService) {
        this.appEventService = appEventService;
        this.configService = configService;
    }

    /**
     * 提交单个事件
     */
    /**
     * 获取埋点上报开关状态
     * @return 当前埋点上报开关状态
     */
    @PermitAll
    @GetMapping("/tracking/status")
    public Result<Map<String, Boolean>> getTrackingStatus() {
        boolean isEnabled = configService.getBooleanConfig(CONFIG_KEY_EVENT_TRACKING, true);
        return Result.success(Map.of("eventTrack", isEnabled));
    }

    /**
     * 设置埋点上报开关状态
     * @param enabled 是否开启埋点上报
     * @return 操作结果
     */
    @PermitAll
    @PostMapping("/tracking/set-status")
    public Result<?> setTrackingStatus(@RequestParam boolean enabled) {
        configService.updateConfig(CONFIG_KEY_EVENT_TRACKING, String.valueOf(enabled));
        return Result.success("埋点上报已" + (enabled ? "开启" : "关闭"));
    }

    /**
     * 提交单个事件
     */
    @PermitAll
    @PostMapping("/submit")
    public Result<?> submitEvent(@RequestBody AppEvent event) {
        if (!configService.getBooleanConfig(CONFIG_KEY_EVENT_TRACKING, true)) {
            return Result.success("埋点上报已关闭，事件未提交");
        }
        appEventService.sendToQueue(event);
        return Result.success("提交成功");
    }

    /**
     * 批量提交事件
     */
    @PermitAll
    @PostMapping("/batch")
    public Result<?> batchSubmitEvents(@RequestBody List<AppEvent> events) {
        if (!configService.getBooleanConfig(CONFIG_KEY_EVENT_TRACKING, true)) {
            return Result.success("埋点上报已关闭，批量事件未提交");
        }
        events.forEach(appEventService::sendToQueue);
        return Result.success("提交成功");
    }

    /**
     * 获取最近的事件
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param userId 用户ID（模糊查询）
     * @param userName 用户名（模糊查询，优先级高于 nickname）
     * @param nickname 用户昵称（模糊查询，仅在 userName 为空时使用）
     * @param eventId 事件ID（模糊查询，对应 eventInfo.eventId）
     * @param pageUrl 页面URL（模糊查询）
     * @param eventType 事件类型（精确匹配）
     * @param filterKey 过滤键
     * @param filterValue 过滤值
     */
    @GetMapping("/recent")
    public Result<PageResult<AppEvent>> getRecentEvents(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String eventId,
            @RequestParam(required = false) String pageUrl,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String filterKey,
            @RequestParam(required = false) String filterValue) {
        return Result.success(appEventService.getRecentEvents(pageNum, pageSize, userId, userName, nickname, eventId, pageUrl, eventType, filterKey, filterValue));
    }
}
