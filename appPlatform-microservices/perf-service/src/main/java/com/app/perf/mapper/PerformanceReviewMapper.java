package com.app.perf.mapper;

import com.app.perf.entity.PerformanceReview;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PerformanceReviewMapper {

    /**
     * 插入绩效评估配置
     */
    @Insert("INSERT INTO performance_review (dept_id, name, cover_image, deadline, create_time, update_time) " +
            "VALUES (#{deptId}, #{name}, #{coverImage}, #{deadline}, #{createTime}, #{updateTime})")
    int insert(PerformanceReview performanceReview);

    /**
     * 根据部门ID查询配置
     */
    @Select("SELECT * FROM performance_review WHERE dept_id = #{deptId}")
    PerformanceReview findByDeptId(String deptId);

    /**
     * 查询所有配置
     */
    @Select("SELECT * FROM performance_review ORDER BY create_time DESC")
    List<PerformanceReview> findAll();

    /**
     * 根据部门ID列表批量查询
     */
    @Select("<script>" +
            "SELECT * FROM performance_review WHERE dept_id IN " +
            "<foreach collection='deptIds' item='deptId' open='(' separator=',' close=')'>" +
            "#{deptId}" +
            "</foreach>" +
            " ORDER BY create_time DESC" +
            "</script>")
    List<PerformanceReview> findByDeptIds(@Param("deptIds") List<String> deptIds);

    /**
     * 更新配置
     */
    @Update("UPDATE performance_review SET " +
            "name = #{name}, " +
            "cover_image = #{coverImage}, " +
            "deadline = #{deadline}, " +
            "update_time = #{updateTime} " +
            "WHERE dept_id = #{deptId}")
    int updateByDeptId(PerformanceReview performanceReview);

    /**
     * 更新封面图
     */
    @Update("UPDATE performance_review SET " +
            "cover_image = #{coverImage}, " +
            "update_time = #{updateTime} " +
            "WHERE dept_id = #{deptId}")
    int updateCoverImage(@Param("deptId") String deptId,
                        @Param("coverImage") String coverImage,
                        @Param("updateTime") java.time.LocalDateTime updateTime);

    /**
     * 更新截止时间
     */
    @Update("UPDATE performance_review SET " +
            "deadline = #{deadline}, " +
            "update_time = #{updateTime} " +
            "WHERE dept_id = #{deptId}")
    int updateDeadline(@Param("deptId") String deptId,
                      @Param("deadline") String deadline,
                      @Param("updateTime") java.time.LocalDateTime updateTime);

    /**
     * 更新组织名称
     */
    @Update("UPDATE performance_review SET " +
            "name = #{name}, " +
            "update_time = #{updateTime} " +
            "WHERE dept_id = #{deptId}")
    int updateName(@Param("deptId") String deptId,
                   @Param("name") String name,
                   @Param("updateTime") java.time.LocalDateTime updateTime);

    /**
     * 根据部门ID删除配置
     */
    @Delete("DELETE FROM performance_review WHERE dept_id = #{deptId}")
    int deleteByDeptId(String deptId);

    /**
     * 检查部门配置是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM performance_review WHERE dept_id = #{deptId}")
    boolean existsByDeptId(String deptId);
}
