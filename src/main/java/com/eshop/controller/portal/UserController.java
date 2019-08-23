package com.eshop.controller.portal;

import com.eshop.common.Const;
import com.eshop.common.ServerResponce;
import com.eshop.pojo.User;
import com.eshop.service.IUserService;
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
    @RequestMapping(value = "logout.do",method = RequestMethod.GET)
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
    @RequestMapping(value = "register.do",method = RequestMethod.GET)
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
    @RequestMapping(value = "check_valid.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponce<String> checkValid(String str,String type){
        return iUserService.checkValid(str, type);
    }

    /**
     * 获取登录用户信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.GET)
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
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.GET)
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
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponce<String> forgetCheckAnswer(String username,String question,String answer){
        return iUserService.checkAnswer(username,question,answer);
    }

}
