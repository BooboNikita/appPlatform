package com.app.appplatform.mapper.primary;

import com.app.appplatform.entity.CrashReport;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Flutter Crash SDK Data Access Layer
 * Corresponds to crash_reports table
 */
@Mapper
public interface CrashReportMapper {

    /**
     * Insert crash report
     * 
     * @param crashReport Crash report entity
     * @return Number of inserted records
     */
    @Insert("INSERT INTO crash_reports (" +
            "crash_id, app_id, user_id, session_id, crash_type, message, stack_trace, " +
            "app_version, app_build_number, device_model, device_brand, os_version, platform, " +
            "screen_resolution, total_memory, available_memory, network_type, battery_level, " +
            "custom_data, crash_timestamp, report_timestamp, sdk_version, created_at" +
            ") VALUES (" +
            "#{crashId}, #{appId}, #{userId}, #{sessionId}, #{crashType}, #{message}, #{stackTrace}, " +
            "#{appVersion}, #{appBuildNumber}, #{deviceModel}, #{deviceBrand}, #{osVersion}, #{platform}, " +
            "#{screenResolution}, #{totalMemory}, #{availableMemory}, #{networkType}, #{batteryLevel}, " +
            "#{customData}, #{crashTimestamp}, #{reportTimestamp}, #{sdkVersion}, #{createdAt}" +
            ")")
    int insert(CrashReport crashReport);

    /**
     * Check if crash ID exists
     * 
     * @param crashId Crash ID
     * @return Number of existing records
     */
    @Select("SELECT COUNT(1) FROM crash_reports WHERE crash_id = #{crashId}")
    int countByCrashId(String crashId);

    /**
     * Find crash report by ID
     * 
     * @param id Crash report ID
     * @return Crash report entity
     */
    @Select("SELECT id, crash_id, app_id, user_id, session_id, crash_type, message, stack_trace, " +
            "app_version, app_build_number, device_model, device_brand, os_version, platform, " +
            "screen_resolution, total_memory, available_memory, network_type, battery_level, " +
            "custom_data, crash_timestamp, report_timestamp, sdk_version, created_at " +
            "FROM crash_reports WHERE id = #{id}")
    CrashReport findById(Long id);

    /**
     * Logical delete crash report by ID
     * 
     * @param id Crash report ID
     * @return Number of updated records
     */
    @Update("UPDATE crash_reports SET deleted = 1, deleted_at = NOW() WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * Find crash reports by conditions
     * 
     * @param appId Application ID filter (optional)
     * @param crashType Crash type filter (optional)
     * @param username Username filter (optional)
     * @param startDate Start date filter (optional)
     * @param endDate End date filter (optional)
     * @return List of crash reports
     */
    @Select("SELECT id, crash_id, app_id, user_id, session_id, crash_type, message, stack_trace, " +
            "app_version, app_build_number, device_model, device_brand, os_version, platform, " +
            "screen_resolution, total_memory, available_memory, network_type, battery_level, " +
            "custom_data, crash_timestamp, report_timestamp, sdk_version, created_at " +
            "FROM crash_reports WHERE deleted = 0 " +
            "ORDER BY crash_timestamp DESC")
    List<CrashReport> findByConditions(@Param("appId") String appId,
                                        @Param("crashType") String crashType,
                                        @Param("username") String username,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Count crash reports by conditions
     * 
     * @param appId Application ID filter (optional)
     * @param crashType Crash type filter (optional)
     * @param username Username filter (optional)
     * @param startDate Start date filter (optional)
     * @param endDate End date filter (optional)
     * @return Total count
     */
    @Select("SELECT COUNT(1) FROM crash_reports WHERE deleted = 0")
    long countByConditions(@Param("appId") String appId,
                          @Param("crashType") String crashType,
                          @Param("username") String username,
                          @Param("startDate") LocalDateTime startDate,
                          @Param("endDate") LocalDateTime endDate);
}
