package com.eshop.service.impl;

import com.eshop.common.ServerResponce;
import com.eshop.dao.CategoryMapper;
import com.eshop.pojo.Category;
import com.eshop.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public ServerResponce<String> addCategory(String categoryName, Integer parentId) {
        //校验参数
        if(parentId==null || StringUtils.isBlank(categoryName)){
            return ServerResponce.createByErrorMessage("添加品类参数错误");
        }
        Category category=new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());

        int count = categoryMapper.insert(category);
        if(count>0){
            return ServerResponce.createBySuccessMessage("添加品类成功");
        }
        return ServerResponce.createByErrorMessage("添加品类失败");
    }

    @Override
    public ServerResponce<String> updateCategory(Integer catrgoryId, String categoryName) {
        //校验参数
        if(catrgoryId==null || StringUtils.isBlank(categoryName)){
            return ServerResponce.createByErrorMessage("添加品类参数错误");
        }
        Category category=new Category();
        category.setId(catrgoryId);
        category.setName(categoryName);
        category.setUpdateTime(new Date());

        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(resultCount>0){
            return ServerResponce.createBySuccessMessage("修改商品分类成功");
        }
        return ServerResponce.createByErrorMessage("修改商品分类失败");
    }
}
