package com.eshop.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.eshop.common.Const;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponce;
import com.eshop.pojo.User;
import com.eshop.service.IOrderService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("order")
public class OrderController {
    private static final Logger logger= LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iorderService;

    /**
     * 创建订单
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponce create(HttpSession session,Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iorderService.createOrder(user.getId(),shippingId);
    }

    /**
     * 取消订单
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponce cancel(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iorderService.cancel(user.getId(),orderNo);
    }

    /**
     * 在点击购物车的 去结算 ，得到订单的商品详情；这时数据库并没有产生订单，当在这个页面点击 提交订单 才会产生订单。
     * @param session
     * @return
     */
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponce getOrderCartProduct(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iorderService.getOrderCartProduct(user.getId());
    }

    /**
     *得到订单详情
     * @param session
     * @return
     */
         @RequestMapping("detail.do")
     @ResponseBody
     public ServerResponce detail(HttpSession session,Long orderNo){
     User user = (User) session.getAttribute(Const.CURRENT_USER);
     if(user==null){
     return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
     }
     return iorderService.getOrderDetail(user.getId(),orderNo);
     }
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponce list(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iorderService.getOrderList(user.getId(),pageNum,pageSize);
    }
    /**
     * 支付
     * @param session
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponce pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String path=request.getSession().getServletContext().getRealPath("upload");
        return iorderService.pay(orderNo,user.getId(),path);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        //用于接收request里边的params
        Map<String,String> params= Maps.newHashMap();
        Map requestParams=request.getParameterMap();
        //因为getParameterMap()的返回值是  Map<String, String[]> ，所以取出来要把数组拼接
        for(Iterator iter=requestParams.keySet().iterator();iter.hasNext();){
            String name= (String) iter.next();
            String [] values= (String[]) requestParams.get(name);
            String valueStr="";
            for(int i=0;i<values.length;i++){
                valueStr= (i == values.length-1) ? valueStr+values[i]:valueStr+values[i]+",";
            }
            params.put(name,valueStr);
        }
        logger.info("支付宝回调，sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());
        //非常重要！！！  验证回调的正确性，是不是支付宝发的；还要避免重复通知

        params.remove("sign_type");
        try {
            //验证正确性
            boolean alipayRSACheckedV2= AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if(!alipayRSACheckedV2){
                return ServerResponce.createByErrorMessage("非法操作，验证不通过，请不要恶意请求！");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝验证回调异常",e);
        }

        //todo 验证各种数据是否一致

        //业务逻辑
        ServerResponce serverResponce = iorderService.alipayCallback(params);
        if(serverResponce.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }
    /**
     * 前台查询 轮询订单的支付状态：当用户扫描二维码后，前台会调用这个接口查询订单的支付状态
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponce<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponce<Boolean> serverResponce = iorderService.queryOrderPayStatus(user.getId(), orderNo);
        if(serverResponce.isSuccess()){
            return ServerResponce.createBySuccess(true);
        }
        return ServerResponce.createBySuccess(false);
    }
}
