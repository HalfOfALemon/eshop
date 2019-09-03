package com.eshop.service.impl;

import com.eshop.common.ServerResponce;
import com.eshop.dao.CategoryMapper;
import com.eshop.pojo.Category;
import com.eshop.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    private Logger logger= LoggerFactory.getLogger(CategoryServiceImpl.class);
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

    @Override
    public ServerResponce<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponce.createBySuccess(categoryList);
    }

    /**
     *递归获得子分类
     * @param categoryId
     * @return
     */
    @Override
    public ServerResponce selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet=new HashSet<>();
        Set<Category> childrenCategory = findChildrenCategory(categorySet, categoryId);
        List<Category> list=new ArrayList<>();
        if(categoryId!=null){
            for(Category categoryItem:childrenCategory){
                //TODO 存对象还是存Id
                list.add(categoryItem);
            }
        }
        return ServerResponce.createBySuccess(list);
    }

    /**
     * 递归算法，找出子节点
     * @param categorySet
     * @param categoryId
     * @return
     */
    private Set<Category> findChildrenCategory(Set<Category> categorySet,Integer categoryId){
        //使用set集合，Category要重写equal和hashcode
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category!=null){
            categorySet.add(category);
        }
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for(Category categoryItem:categoryList){
            findChildrenCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }
}
