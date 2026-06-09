package com.app.app.service;

import com.app.app.dto.AppInfoDto;
import com.app.app.dto.AppVersionCheckDto;
import com.app.app.entity.AppInfo;
import com.app.common.PageResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.List;

public interface AppInfoService {

    /**
     * 上传APP文件并保存应用信息
     * @param file APP文件
     * @param appInfo 应用信息
     * @return 保存后的应用信息
     * @throws IOException 文件操作异常
     */
    AppInfo uploadApp(MultipartFile file, AppInfo appInfo) throws IOException;

    /**
     * 获取应用列表（支持分页和筛选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param appName 应用名称（模糊查询）
     * @param version 版本号（精确匹配）
     * @param buildNumber 构建号（精确匹配）
     * @param isBeta 是否测试版
     * @return 分页应用列表
     */
    PageResult<AppInfoDto> getAllApps(
            int pageNum,
            int pageSize,
            String appName,
            String version,
            String buildNumber,
            Boolean isBeta
    );

    /**
     * 获取所有应用列表（兼容旧版本）
     * @deprecated 使用 {@link #getAllApps(int, int, String, String, String, Boolean)} 替代
     */
    @Deprecated
    default PageResult<AppInfoDto> getAllApps(int pageNum, int pageSize) {
        return getAllApps(pageNum, pageSize, null, null, null, null);
    }

    /**
     * 根据包名获取应用列表
     * @param packageName 包名
     * @return 应用列表
     */
    List<AppInfo> getAppsByPackageName(String packageName);

    /**
     * 根据ID获取应用信息
     * @return 应用信息
     */
    AppInfo getAppById(Integer id);

    /**
     * 更新应用信息
     * @param appInfo 包含更新信息的应用对象
     * @return 更新后的应用信息
     */
    AppInfo updateAppInfo(AppInfo appInfo);

    /**
     * 根据ID删除应用
     * @param id 应用ID
     * @return 是否删除成功
     */
    boolean deleteApp(Integer id);

    /**
     * 增加下载次数
     * @param id 应用ID
     * @return 更新后的下载次数
     */
    int incrementDownloadCount(Integer id);

    /**
     * 检查指定包名和版本的应用是否已存在
     * @param packageName 包名
     * @param version 版本号
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByPackageAndVersion(String packageName, String version);

    /**
     * 检查应用版本更新
     * @param headers HTTP请求头，包含appinfo和deviceInfo
     * @return 版本检查结果
     */
    AppVersionCheckDto checkVersionUpdate(HttpHeaders headers);

    /**
     * 下载应用文件
     * @param appInfo 应用信息
     * @param rangeHeader 范围请求头
     * @return 文件流响应
     */
    ResponseEntity<StreamingResponseBody> downloadAppFile(AppInfo appInfo, String rangeHeader);

    /**
     * 获取APP更新总开关状态
     * @return 是否启用
     */
    boolean getUpdateTotalStatus();

    /**
     * 设置APP更新总开关状态
     * @param enabled 是否启用
     */
    void setUpdateTotalStatus(boolean enabled);
}
