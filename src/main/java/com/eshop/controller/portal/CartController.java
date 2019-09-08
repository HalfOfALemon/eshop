package com.eshop.controller.portal;

import com.eshop.common.Const;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponce;
import com.eshop.pojo.User;
import com.eshop.service.ICartService;
import com.eshop.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("cart")
public class CartController {
    @Autowired
    ICartService iCartService;
    public ServerResponce<CartVo> add(HttpSession session, Integer count, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.add(user.getId(),productId,count);
    }
}
