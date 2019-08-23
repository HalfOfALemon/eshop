package com.eshop.service.impl;

import com.eshop.common.Const;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponce;
import com.eshop.common.TokenCache;
import com.eshop.dao.UserMapper;
import com.eshop.pojo.User;
import com.eshop.service.IUserService;
import com.eshop.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    UserMapper userMapper;
    @Override
    public ServerResponce<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount==0){
            return ServerResponce.createByErrorMessage("用户名不存在");
        }
        //对用户密码进行MD5加密
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if(user==null){
            return ServerResponce.createByErrorMessage("密码错误");
        }
        //将用户密码设置为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponce.createBySuccess("登录成功",user);
    }

    @Override
    public ServerResponce<String> register(User user) {
        //检验用户名是否存在
        ServerResponce<String> checkValid = this.checkValid(user.getUsername(), Const.USERNAME);
        if(!checkValid.isSuccess()){
            return checkValid;
        }
        //检验邮箱是否存在
        checkValid = this.checkValid(user.getEmail(), Const.EMAIL);
        if(!checkValid.isSuccess()){
            return checkValid;
        }
        //设置用户权限
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        //增加用户
        int resultCount= userMapper.insert(user);
        if(resultCount==0){
            return ServerResponce.createByErrorMessage("注册失败");
        }
        return ServerResponce.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponce<String> checkValid(String str, String type) {
        //检查参数是否有效
        if(StringUtils.isNotBlank(type)){
            //检查type是否为空，其中空格也会返回 false
            if(Const.USERNAME.equals(type)){
                //检验用户名是否存在
                int resultCount = userMapper.checkUsername(str);
                if(resultCount>0){
                    return ServerResponce.createByErrorMessage("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                //检验邮箱是否存在
                int resultCount=userMapper.checkEmail(str);
                if(resultCount>0){
                    return ServerResponce.createByErrorMessage("邮箱已存在");
                }
            }
        }else{
            return ServerResponce.createByErrorMessage("参数错误");
        }
        return ServerResponce.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponce<String> selectQuestion(String username) {
        //查询找回密码问题
        ServerResponce<String> checkValid = this.checkValid(username, Const.USERNAME);
        if(checkValid.isSuccess()){
            return ServerResponce.createByErrorMessage("用户名不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponce.createBySuccess(question);
        }
        return ServerResponce.createByErrorMessage("找回密码的问题为空");
    }

    @Override
    public ServerResponce<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount>0){
            //使用UUID生成一个随机字符串，当做token储存起来
            String forgetToken= UUID.randomUUID().toString();
            //创建token类保存token
            TokenCache.setKey("token"+username,forgetToken);
            return ServerResponce.createBySuccess(forgetToken);
        }
        return ServerResponce.createByErrorMessage("答案错误");
    }
}
