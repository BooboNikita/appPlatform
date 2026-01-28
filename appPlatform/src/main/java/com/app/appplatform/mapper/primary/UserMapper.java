package com.app.appplatform.mapper.primary;

import com.app.appplatform.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE username = #{username}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "password", column = "password"),  // 确保列名正确
        @Result(property = "role", column = "role"),
        @Result(property = "avatar", column = "avatar")
    })
    User findByUsername(String username);

    @Insert("INSERT INTO user (username, password, role, avatar) VALUES (#{username}, #{password}, #{role}, #{avatar})")
    void insertUser(User user);
}
