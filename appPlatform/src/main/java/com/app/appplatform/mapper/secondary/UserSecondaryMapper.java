package com.app.appplatform.mapper.secondary;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 副数据库用户信息 Mapper - 查询 xxg_idaas 数据库的 ei_user 表
 */
@Mapper
public interface UserSecondaryMapper {

    /**
     * 根据用户名查询用户昵称
     * 
     * @param username 用户名
     * @return 用户昵称，如果不存在返回 null
     */
    @Select("SELECT nick_name FROM ei_user WHERE login_name = #{username} LIMIT 1")
    String findNicknameByUsername(@Param("username") String username);

    /**
     * 根据昵称查询用户名
     * 
     * @param nickname 用户昵称
     * @return 用户名，如果不存在返回 null
     */
    @Select("SELECT login_name FROM ei_user WHERE nick_name = #{nickname} LIMIT 1")
    String findUsernameByNickname(@Param("nickname") String nickname);

    /**
     * 根据用户名查询用户信息（可扩展其他字段）
     *
     * @param username 用户名
     * @return 用户信息映射
     */
    @Select("SELECT nick_name, email, phone FROM ei_user WHERE login_name = #{username} LIMIT 1")
    Map<String, Object> findUserInfoByUsername(@Param("username") String username);

    /**
     * 批量查询用户昵称（用于性能优化）
     *
     * @param usernames 用户名列表
     * @return 用户名到昵称的映射
     */
    @Select("<script>" +
            "SELECT login_name as username, nick_name as nickname FROM ei_user WHERE login_name IN " +
            "<foreach collection='usernames' item='username' open='(' separator=',' close=')'>" +
            "#{username}" +
            "</foreach>" +
            "</script>")
    List<Map<String, Object>> findNicknamesByUsernames(@Param("usernames") List<String> usernames);
}
