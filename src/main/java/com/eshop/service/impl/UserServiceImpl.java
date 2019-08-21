package com.eshop.service.impl;

import com.eshop.common.ServerResponce;
import com.eshop.dao.UserMapper;
import com.eshop.pojo.User;
import com.eshop.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;

public class UserServiceImpl implements IUserService {
    @Autowired
    UserMapper userMapper;
    @Override
    public ServerResponce<User> login(String username, String password) {
        return null;
    }
}
