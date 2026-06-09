package com.app.auth.mapper;

import com.app.auth.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE username = #{username}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "password", column = "password"),
        @Result(property = "role", column = "role"),
        @Result(property = "avatar", column = "avatar")
    })
    User findByUsername(String username);

    @Insert("INSERT INTO user (username, password, role, avatar) VALUES (#{username}, #{password}, #{role}, #{avatar})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(User user);
}
