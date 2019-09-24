package com.eshop.service;

import com.eshop.common.ServerResponce;
import com.eshop.vo.OrderVo;
import com.github.pagehelper.PageInfo;

import java.util.Map;

public interface IOrderService {
    ServerResponce pay(Long orderNo, Integer userId, String path);
    ServerResponce alipayCallback(Map<String,String> params);
    ServerResponce<Boolean> queryOrderPayStatus(Integer userId,Long orderNo);
    ServerResponce createOrder(Integer userId,Integer shipping);
    ServerResponce<String> cancel(Integer userId,Long orderNo);
    ServerResponce getOrderCartProduct(Integer userId);
    ServerResponce<OrderVo> getOrderDetail(Integer userId,Long orderNo);
    ServerResponce<PageInfo> getOrderList(Integer userId,int pageNum,int pageSize);
    ServerResponce<PageInfo> manageList(int pageNum,int pageSize);
    ServerResponce<OrderVo> manageDetail(Long orderNo);
    ServerResponce<PageInfo> manageSearch(Long orderNo,int pageNum,int pageSize);
    ServerResponce<String> manageSendGoods(Long orderNo);
}
