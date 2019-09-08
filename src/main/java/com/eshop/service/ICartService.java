package com.eshop.service;

import com.eshop.common.ServerResponce;
import com.eshop.vo.CartVo;

public interface ICartService {
    ServerResponce<CartVo> add(Integer userId, Integer productId, Integer count);
}
