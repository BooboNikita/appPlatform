package com.app.appplatform.mapper;

import com.app.appplatform.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

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
}