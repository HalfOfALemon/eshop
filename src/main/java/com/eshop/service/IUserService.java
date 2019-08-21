package com.eshop.service;

import com.eshop.common.ServerResponce;
import com.eshop.pojo.User;

public interface IUserService {
    ServerResponce<User> login(String username, String password);
}
