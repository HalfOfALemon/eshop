package com.eshop.controller.backend;

import com.eshop.common.Const;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponce;
import com.eshop.pojo.User;
import com.eshop.service.ICategoryService;
import com.eshop.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("manage/category")
public class CategoryManageController {
    @Autowired
    IUserService iUserService;
    @Autowired
    ICategoryService iCategoryService;

    /**
     * 添加分类
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponce<String> addCategory(HttpSession session,String categoryName,@RequestParam(value = "parentId",defaultValue = "0") Integer parentId){
        //检查用户有没有登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //检查是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //进行增加分类
            return iCategoryService.addCategory(categoryName, parentId);
        }else {
            return ServerResponce.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }
    @RequestMapping("set_category.do")
    @ResponseBody
    public ServerResponce<String> setCategoryName(HttpSession session,Integer catrgoryId,String categoryName){
        //检查用户有没有登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //检查是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //进行修改分类
            return iCategoryService.updateCategory(catrgoryId, categoryName);
        }else {
            return ServerResponce.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }
}
