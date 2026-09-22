package com.app.appplatform.mapper.primary;

import com.app.appplatform.entity.DynamicConfigItemGrayUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DynamicConfigItemGrayUserMapper {

    String COLS = "id, item_id as itemId, list_type as listType, username, operator, " +
            "create_time as createTime, update_time as updateTime";

    /**
     * INSERT IGNORE：唯一键 (item_id, list_type, username) 冲突时跳过，天然幂等
     */
    @Insert("INSERT IGNORE INTO dynamic_config_item_gray_user(item_id, list_type, username, operator, create_time, update_time) " +
            "VALUES(#{itemId}, #{listType}, #{username}, #{operator}, NOW(), NOW())")
    int insert(DynamicConfigItemGrayUser grayUser);

    @Insert("<script>" +
            "INSERT IGNORE INTO dynamic_config_item_gray_user(item_id, list_type, username, operator, create_time, update_time) VALUES " +
            "<foreach collection='list' item='g' separator=','>" +
            "(#{g.itemId}, #{g.listType}, #{g.username}, #{g.operator}, NOW(), NOW())" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<DynamicConfigItemGrayUser> list);

    @Select("SELECT " + COLS + " FROM dynamic_config_item_gray_user WHERE id = #{id}")
    DynamicConfigItemGrayUser findById(Long id);

    @Select("<script>" +
            "SELECT " + COLS + " FROM dynamic_config_item_gray_user WHERE item_id = #{itemId}" +
            "<if test=\"listType != null and listType != ''\"> AND list_type = #{listType}</if>" +
            "<if test=\"keyword != null and keyword != ''\"> AND username LIKE CONCAT('%', #{keyword}, '%')</if>" +
            " ORDER BY id ASC" +
            "</script>")
    List<DynamicConfigItemGrayUser> findByItemId(@Param("itemId") Long itemId,
                                                 @Param("listType") String listType,
                                                 @Param("keyword") String keyword);

    @Select("<script>" +
            "SELECT " + COLS + " FROM dynamic_config_item_gray_user WHERE item_id IN " +
            "<foreach collection='itemIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<DynamicConfigItemGrayUser> findByItemIds(@Param("itemIds") List<Long> itemIds);

    @Select("SELECT DISTINCT item_id FROM dynamic_config_item_gray_user WHERE username = #{username}")
    List<Long> findItemIdsByUsername(String username);

    @Delete("DELETE FROM dynamic_config_item_gray_user WHERE id = #{id}")
    int deleteById(Long id);

    @Delete("DELETE FROM dynamic_config_item_gray_user WHERE item_id = #{itemId} AND list_type = #{listType}")
    int deleteByItemIdAndType(@Param("itemId") Long itemId, @Param("listType") String listType);

    @Delete("DELETE FROM dynamic_config_item_gray_user WHERE item_id = #{itemId}")
    int deleteByItemId(Long itemId);

    @Delete("<script>" +
            "DELETE FROM dynamic_config_item_gray_user WHERE item_id IN " +
            "<foreach collection='itemIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int deleteByItemIds(@Param("itemIds") List<Long> itemIds);
}
