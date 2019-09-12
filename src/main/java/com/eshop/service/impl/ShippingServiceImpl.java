package com.eshop.service.impl;

import com.eshop.common.ServerResponce;
import com.eshop.dao.ShippingMapper;
import com.eshop.pojo.Shipping;
import com.eshop.service.IShippingService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {
    @Autowired
    ShippingMapper shippingMapper;

    @Override
    public ServerResponce add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        shipping.setCreateTime(new Date());
        shipping.setUpdateTime(new Date());
        int rowCount=shippingMapper.insert(shipping);
        if(rowCount>0){
            Map result= Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponce.createBySuccess("新建地址成功",result);
        }
        return ServerResponce.createByErrorMessage("新建地址失败");
    }

    @Override
    public ServerResponce delete(Integer userId, Integer shippingId) {
        //要防止横向越权，让该用户删除了 不属于自己的 购物车地址。自己写一个新的SQL
        int resultCount=shippingMapper.deleteByUserIdShippingId(userId,shippingId);
        if(resultCount>0){
            return ServerResponce.createBySuccess("删除地址成功");
        }
        return ServerResponce.createByErrorMessage("删除地址失败");
    }

    @Override
    public ServerResponce update(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int resultCount=shippingMapper.updateByShipping(shipping);
        if(resultCount>0){
            return ServerResponce.createBySuccess("修改地址成功");
        }
        return ServerResponce.createByErrorMessage("修改地址失败");
    }

    @Override
    public ServerResponce<Shipping> select(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectByUserIdShippingId(userId, shippingId);
        if(shipping ==null){
            return ServerResponce.createByErrorMessage("无法获得该地址");
        }
        return ServerResponce.createBySuccess("成功查到地址",shipping);
    }

    @Override
    public ServerResponce<PageInfo> list(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo=new PageInfo(shippingList);
        return ServerResponce.createBySuccess(pageInfo);
    }
}
