package com.eshop.service.impl;

import com.eshop.common.Const;
import com.eshop.common.ServerResponce;
import com.eshop.common.TokenCache;
import com.eshop.dao.UserMapper;
import com.eshop.pojo.User;
import com.eshop.service.IUserService;
import com.eshop.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
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
        //设置时间
        Date date = new Date();
        user.setCreateTime(date);
        user.setUpdateTime(date);
        //增加用户
        int resultCount= userMapper.insert(user);
        if(resultCount==0){
            return ServerResponce.createByErrorMessage("注册失败");
        }
        return ServerResponce.createBySuccessMessage("注册成功");
    }

    /**
     * 校验参数
     * @param str
     * @param type
     * @return
     */
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
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponce.createBySuccess(forgetToken);
        }
        return ServerResponce.createByErrorMessage("答案错误");
    }

    /**
     * 重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @Override
    public ServerResponce<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        //检验参数是否为空
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponce.createByErrorMessage("参数错误，需要传递token");
        }
        //检验用户名是否存在
        ServerResponce<String> checkValid = this.checkValid(username, Const.USERNAME);
        if(checkValid.isSuccess()){
            return ServerResponce.createByErrorMessage("用户名不存在");
        }
        //检验token是否存在 token
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if(StringUtils.isBlank(token)){
            return ServerResponce.createByErrorMessage("token无效或者过期");
        }
        if(StringUtils.equals(token,forgetToken)){
             //MD5加密后保存
            String md5Password= MD5Util.MD5EncodeUtf8(passwordNew);
            int count = userMapper.updatePasswordByUsername(username, md5Password);
            //使用md5加密后的密码
            if(count>0){
                return ServerResponce.createBySuccessMessage("修改密码成功");
            }
        }else {
            return ServerResponce.createByErrorMessage("token错误，请重新获得");
        }
        return ServerResponce.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponce<String> resetPassword(String passwordOld, String passwordNew, User user) {
        //防止横向越权，要检验旧密码是否属于这个用户
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if(resultCount==0){
            return ServerResponce.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount>0){
            return ServerResponce.createBySuccessMessage("密码修改成功");
        }
        return ServerResponce.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponce<User> updateInformation(User user) {
        //username不能被更新
        //email要校验是否存在，而且存在的email如果相同，不能是单前用户的旧email
        int resultcount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if(resultcount>0){
            return ServerResponce.createByErrorMessage("该邮箱已经存在，请换一个邮箱");
        }
        User updateUser=new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount>0){
            return ServerResponce.createBySuccess("更新用户信息成功",updateUser);
        }
        return ServerResponce.createByErrorMessage("更新用户信息失败");
    }

    /**
     * 得到用户信息， 返回用户时记得把密码设置为空！！
     * @param userId
     * @return
     */
    @Override
    public ServerResponce<User> getInformation(Integer userId) {
        User user= userMapper.selectByPrimaryKey(userId);
        if(user==null){
            ServerResponce.createByErrorMessage("找不到该用户");
        }
        //记得把密码设置为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponce.createBySuccess(user);
    }

    /**
     * 检查是否是管理员
     * @param user
     * @return
     */
    @Override
    public ServerResponce checkAdminRole(User user){
        if(user!=null && user.getRole()==Const.Role.ROLE_ADMIN){
            return ServerResponce.createBySuccess();
        }
        return ServerResponce.createByError();
    }

}
