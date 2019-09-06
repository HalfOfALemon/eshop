package com.eshop.service;

import com.eshop.common.ServerResponce;
import com.eshop.pojo.Category;

import java.util.List;

public interface ICategoryService {
    ServerResponce<String> addCategory(String categoryName,Integer parentId);
    ServerResponce<String> updateCategory(Integer catrgoryId,String categoryName);
    ServerResponce<List<Category>> getChildrenParallelCategory(Integer categoryId);
    ServerResponce<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}
