package com.eshop.controller.backend;

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
@RequestMapping("manage/user")
public class UserManageController {
    @Autowired
    IUserService iUserService;
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponce<User> login(HttpSession session,String username,String password){
        ServerResponce<User> responce = iUserService.login(username, password);
        if(responce.isSuccess()){
            //要判断该用户是否是管理员
            User user = responce.getData();
            if(Const.Role.ROLE_ADMIN==user.getRole()){
                //设置登录session
                session.setAttribute(Const.CURRENT_USER,user);
                return responce;
            }
        }else {
            return ServerResponce.createByErrorMessage("不是管理员，无法登录");
        }
        return responce;
    }
}
