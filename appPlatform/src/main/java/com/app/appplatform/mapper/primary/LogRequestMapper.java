package com.app.appplatform.mapper.primary;

import com.app.appplatform.entity.LogRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 日志请求记录Mapper
 */
@Mapper
public interface LogRequestMapper {

    /**
     * 插入日志请求记录
     */
    @Insert("INSERT INTO log_request (username, request_time, expire_time, status) " +
           "VALUES (#{username}, #{requestTime}, #{expireTime}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LogRequest logRequest);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM log_request WHERE id = #{id} AND is_deleted = 0")
    LogRequest findById(Integer id);

    /**
     * 根据用户名查询有效的日志请求（未过期且未上传且未删除）
     */
    @Select("SELECT * FROM log_request WHERE username = #{username} " +
           "AND status = 0 AND expire_time > NOW() AND is_deleted = 0 ORDER BY request_time DESC")
    List<LogRequest> findActiveByUsername(String username);

    /**
     * 根据用户名查询最新的日志请求（未删除）
     */
    @Select("SELECT * FROM log_request WHERE username = #{username} AND is_deleted = 0 ORDER BY request_time DESC LIMIT 1")
    LogRequest findLatestByUsername(String username);

    /**
     * 更新状态
     */
    @Update("UPDATE log_request SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 标记过期的请求（只标记未删除的）
     */
    @Update("UPDATE log_request SET status = 2 WHERE status = 0 AND expire_time <= NOW() AND is_deleted = 0")
    void markExpiredRequests();

    /**
     * 软删除记录
     */
    @Update("UPDATE log_request SET is_deleted = 1 WHERE id = #{id}")
    void delete(Integer id);

    /**
     * 根据用户名更新状态为已上传（用于主动上传场景）
     * 更新该用户下所有待上传(status=0)且未删除(is_deleted=0)的记录为已上传(status=1)
     */
    @Update("UPDATE log_request SET status = 1 WHERE username = #{username} AND status = 0 AND is_deleted = 0")
    void updateStatusToUploadedByUsername(@Param("username") String username);

    /**
     * 根据条件查询日志请求列表（支持分页）
     * @param username 用户名（可选）
     * @param status 状态（可选）
     * @param startDate 请求开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 请求结束日期（可选，格式：yyyy-MM-dd）
     * @return 日志请求列表
     */
    @Select({"<script>",
            "SELECT * FROM log_request WHERE is_deleted = 0 ",
            "<if test='username != null and username != \"\"'> AND username LIKE CONCAT('%', #{username}, '%')</if>",
            "<if test='status != null'> AND status = #{status}</if>",
            "<if test='startDate != null and startDate != \"\"'> AND DATE(request_time) &gt;= #{startDate}</if>",
            "<if test='endDate != null and endDate != \"\"'> AND DATE(request_time) &lt;= #{endDate}</if>",
            " ORDER BY request_time DESC",
            "</script>"})
    List<LogRequest> findByCondition(
            @Param("username") String username,
            @Param("status") Integer status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);
}
