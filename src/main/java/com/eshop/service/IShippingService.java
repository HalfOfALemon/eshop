package com.eshop.service;

import com.eshop.common.ServerResponce;
import com.eshop.pojo.Shipping;
import com.github.pagehelper.PageInfo;

public interface IShippingService {
    ServerResponce add(Integer userId, Shipping shipping);
    ServerResponce delete(Integer userId,Integer shippingId);
    ServerResponce update(Integer userId, Shipping shipping);
    ServerResponce<Shipping> select(Integer userId,Integer shippingId);
    ServerResponce<PageInfo> list(Integer userId,int pageNum,int pageSize);
}
