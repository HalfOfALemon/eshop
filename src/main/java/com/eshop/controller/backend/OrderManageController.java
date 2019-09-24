package com.eshop.controller.backend;

import com.eshop.common.Const;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponce;
import com.eshop.pojo.User;
import com.eshop.service.IOrderService;
import com.eshop.service.IUserService;
import com.eshop.vo.OrderVo;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("manage/order")
public class OrderManageController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponce<PageInfo> orderList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        //检查用户有没有登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //检查是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            return iOrderService.manageList(pageNum,pageSize);
        }else {
            return ServerResponce.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponce<OrderVo> orderDetail(HttpSession session, Long orderNo){
        //检查用户有没有登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //检查是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            return iOrderService.manageDetail(orderNo);
        }else {
            return ServerResponce.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponce<PageInfo> orderSearch(HttpSession session, Long orderNo,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        //检查用户有没有登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //检查是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            return iOrderService.manageSearch(orderNo,pageNum,pageSize);
        }else {
            return ServerResponce.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponce<String> orderSendGoods(HttpSession session, Long orderNo){
        //检查用户有没有登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //检查是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            return iOrderService.manageSendGoods(orderNo);
        }else {
            return ServerResponce.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }



}
