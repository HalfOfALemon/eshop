package com.eshop.service;

import com.eshop.common.ServerResponce;
import com.eshop.vo.CartVo;

public interface ICartService {
    ServerResponce<CartVo> add(Integer userId, Integer productId, Integer count);
    ServerResponce<CartVo> update(Integer userId, Integer productId, Integer count);
    ServerResponce<CartVo> deleteProduct(Integer userId,String productIds);
    ServerResponce<CartVo> list(Integer userId);
    ServerResponce<CartVo> selectOrUnSelect(Integer userId,Integer productId, Integer checked);
    ServerResponce<Integer> getCartProductCount(Integer userId);
}
