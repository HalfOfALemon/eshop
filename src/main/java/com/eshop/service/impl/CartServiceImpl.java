package com.eshop.service.impl;

import com.eshop.common.Const;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponce;
import com.eshop.dao.CartMapper;
import com.eshop.dao.ProductMapper;
import com.eshop.pojo.Cart;
import com.eshop.pojo.Product;
import com.eshop.service.ICartService;
import com.eshop.util.BigDecimalUtil;
import com.eshop.util.PropertiesUtil;
import com.eshop.vo.CartProductVo;
import com.eshop.vo.CartVo;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    CartMapper cartMapper;
    @Autowired
    ProductMapper productMapper;
    @Override
    public ServerResponce<CartVo> add(Integer userId, Integer productId, Integer count) {
        if(productId==null || count==null){
            return ServerResponce.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart  cart=cartMapper.selectCartByUserIdProductId(userId,productId);
        if(cart==null){
            //这个产品不在购物车里，新增一个购物车
            Cart cartItem=new Cart();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setCreateTime(new Date());
            cartItem.setUpdateTime(new Date());
            cartMapper.insert(cartItem);
        }else {
            //这个产品在购物车里，数量相加
            count=cart.getQuantity()+count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        CartVo cartVo=this.getCartVoLimit(userId);
        return ServerResponce.createBySuccess(cartVo);
    }

    /**
     * 限制购物车，其中会判断数量是否正确
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo=new CartVo();
        //获得该用户的购物车list
        List<Cart> cartList=cartMapper.selectCartByUserId(userId);
        //创建一个CarProductVoList
        List<CartProductVo> cartProductVoList= Lists.newArrayList();

        BigDecimal cartTotalPrice=new BigDecimal("0");
        //判断购物车是否为空
        if(CollectionUtils.isNotEmpty(cartList)){
            //将Cart的项装配到CarProductVo，再add到CarProductVoList
            for(Cart cartItem : cartList){
                CartProductVo cartProductVo=new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                Product product=productMapper.selectByPrimaryKey(cartItem.getProductId());
                if(product!=null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //判断库存
                    int buyLimitCount =0;
                    if(product.getStock()>=cartItem.getQuantity()){
                        //库存充足的时候
                        buyLimitCount=cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else {
                        buyLimitCount=product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //更新购物车有效库存
                        Cart cartForQuantity=new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总价
                     cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                     cartProductVo.setProductChecked(cartItem.getChecked());
                }
                if(cartItem.getChecked()==Const.Cart.CHECKED){
                    cartTotalPrice=BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    /**
     * 判断购物车是否全选
     * @param userId
     * @return
     */
    private boolean getAllCheckedStatus(Integer userId){
        if(userId==null){
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId)==0;
    }
}
