package com.app.app.mapper;

import com.app.app.entity.AppInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppInfoMapper {
    int insert(AppInfo appInfo);

    AppInfo selectById(Integer id);

    List<AppInfo> selectAll();

    List<AppInfo> selectByPackageName(String packageName);

    int update(AppInfo appInfo);

    int deleteById(Integer id);

    int incrementDownloadCount(Integer id);

    AppInfo selectByPackageAndVersion(@Param("packageName") String packageName, @Param("version") String version);

    List<AppInfo> selectByCondition(@Param("appName") String appName,
                                     @Param("version") String version,
                                     @Param("buildNumber") String buildNumber,
                                     @Param("isBeta") Boolean isBeta);
}
