package com.eshop.service;

import com.eshop.common.ServerResponce;
import com.eshop.pojo.User;

public interface IUserService {
    ServerResponce<User> login(String username, String password);
    ServerResponce<String> register(User user);
    ServerResponce<String> checkValid(String str,String type);
    ServerResponce<String> selectQuestion(String username);
    ServerResponce<String> checkAnswer(String username,String question,String answer);
}
