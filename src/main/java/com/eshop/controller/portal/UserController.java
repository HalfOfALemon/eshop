package com.eshop.controller.portal;

import com.eshop.common.Const;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponce;
import com.eshop.pojo.User;
import com.eshop.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    IUserService iUserService;

    /**
     * 用户登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponce<User> login(String username, String password, HttpSession session){
        ServerResponce<User> responce=iUserService.login(username,password);
        if(responce.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,responce.getData());
        }
        return responce;
    }

    /**
     * 退出登录
     * @param session
     * @return
     */
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponce<String> logout(HttpSession session){
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponce.createBySuccess();
    }

    /**
     * 注册
     * @param user
     * @return
     */
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponce<String> register(User user){
        return iUserService.register(user);
    }

    /**
     * 检验用户名或者邮箱是否存在
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponce<String> checkValid(String str,String type){
        return iUserService.checkValid(str, type);
    }

    /**
     * 获取登录用户信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponce<User> getUserInfo(HttpSession session){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if(user!=null){
            return ServerResponce.createBySuccess(user);
        }
        return ServerResponce.createByErrorMessage("用户未登录，无法获取用户信息");
    }

    /**
     * 获取找回密码的问题
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponce<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }

    /**
     * 检查找回密码的问题的答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponce<String> forgetCheckAnswer(String username,String question,String answer){
        return iUserService.checkAnswer(username,question,answer);
    }

    /**
     * 忘记密码的重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponce<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        return iUserService.forgetResetPassword(username,passwordNew,forgetToken);
    }

    /**
     * 登录状态的重置密码
     * @param session
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponce<String> resetPassword( HttpSession session,String passwordOld,String passwordNew){
        //检验是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponce.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    /**
     * 更新用户信息
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponce<User> update_information(HttpSession session,User user){
        //检验是否登录
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponce.createByErrorMessage("用户未登录");
        }
        //userId 设置成当前的登录ID,从session里边获取，防止用户名被修改
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponce<User> serverResponce = iUserService.updateInformation(user);
        if(serverResponce.isSuccess()){
            //返回的user还要把用户名存进去
            serverResponce.getData().setUsername(currentUser.getUsername());
            //保存到session里边
            session.setAttribute(Const.CURRENT_USER,serverResponce.getData());
        }
        return serverResponce;
    }

    /**
     * 得到用户信息，没有登录会强制登录 返回用户时记得把密码设置为空！！
     * @param session
     * @return
     */
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponce<User> get_information(HttpSession session){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser==null){
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登录status=10");
        }
        return iUserService.getInformation(currentUser.getId());
    }

}
