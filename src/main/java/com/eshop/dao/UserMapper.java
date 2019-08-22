package com.eshop.dao;

import com.eshop.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
    /*查询用户名是否存在*/
    int checkUsername(String username);
    /*查询邮箱是否存在*/
    int checkEmail(String email);
    /*查询用户和密码是否存在*/
    User selectLogin(@Param("username") String username, @Param("password") String password);
}