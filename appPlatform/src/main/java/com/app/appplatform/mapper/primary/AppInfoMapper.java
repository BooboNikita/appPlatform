package com.app.appplatform.mapper.primary;

import com.app.appplatform.entity.AppInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AppInfoMapper {

    int insert(AppInfo appInfo);

    AppInfo findById(Integer id);

    AppInfo findByPackageAndVersion(@Param("packageName") String packageName, @Param("version") String version);

    List<AppInfo> findAll();
    
    List<AppInfo> findAllWithFilters(
            @Param("appName") String appName,
            @Param("version") String version,
            @Param("buildNumber") String buildNumber,
            @Param("isBeta") Boolean isBeta
    );

    List<AppInfo> findByPackageName(String packageName);

    int incrementDownloadCount(Integer id);

    int deleteById(Integer id);
    
    /**
     * 更新应用信息
     * @param appInfo 包含更新信息的应用对象
     * @return 更新记录数
     */
    int updateAppInfo(AppInfo appInfo);
    
    /**
     * 检查指定包名和版本的应用是否已存在
     * @param packageName 包名
     * @param version 版本号
     * @param excludeId 要排除的应用ID（通常是当前正在更新的应用ID）
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByPackageAndVersion(
            @Param("packageName") String packageName,
            @Param("version") String version,
            @Param("excludeId") Integer excludeId
    );
    
    /**
     * 根据包名获取需要显示更新弹窗或强制更新的应用版本列表
     * @param packageName 包名
     * @return 需要更新的应用版本列表
     */
    List<AppInfo> findUpdateVersionsByPackage(@Param("packageName") String packageName);
}
