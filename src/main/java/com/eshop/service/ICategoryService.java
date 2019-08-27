package com.eshop.service;

import com.eshop.common.ServerResponce;

public interface ICategoryService {
    ServerResponce<String> addCategory(String categoryName,Integer parentId);
    ServerResponce<String> updateCategory(Integer catrgoryId,String categoryName);
}
