package com.eshop.service;

import com.eshop.common.ServerResponce;
import com.eshop.pojo.Product;
import com.eshop.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;

public interface IProductService {
    ServerResponce saveOrUpdateProduct(Product product);
    ServerResponce<String> setSaleStatus(Integer productId,Integer status);
    ServerResponce<ProductDetailVo> manageProductDetail(Integer productId);
    ServerResponce<PageInfo> getProductList(int pageNum,int pageSize);
    ServerResponce<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize);
}
