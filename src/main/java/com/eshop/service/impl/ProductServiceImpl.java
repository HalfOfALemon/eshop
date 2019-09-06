package com.eshop.service.impl;

import com.eshop.common.Const;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponce;
import com.eshop.dao.CategoryMapper;
import com.eshop.dao.ProductMapper;
import com.eshop.pojo.Category;
import com.eshop.pojo.Product;
import com.eshop.service.ICategoryService;
import com.eshop.service.IProductService;
import com.eshop.util.DateTimeUtil;
import com.eshop.util.PropertiesUtil;
import com.eshop.vo.ProductDetailVo;
import com.eshop.vo.ProductListVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.K;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    ProductMapper productMapper;
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    ICategoryService iCategoryService;

    /**
     * 保存或者更新产品
     * @param product
     * @return
     */
    @Override
    public ServerResponce saveOrUpdateProduct(Product product){
        if(product!=null){
            if(StringUtils.isNotBlank(product.getSubImages())){
                String[] subImagesArray = product.getSubImages().split(",");
                if(subImagesArray.length>0){
                    product.setMainImage(subImagesArray[0]);
                }
            }
            if(product.getId()!=null){
                product.setUpdateTime(new Date());
                int rowCount = productMapper.updateByPrimaryKeySelective(product);
                if(rowCount>0){
                    return ServerResponce.createBySuccessMessage("更新产品成功");
                }
                return ServerResponce.createByErrorMessage("更新产品成功失败");
            }else {
                product.setStatus(1);
                product.setCreateTime(new Date());
                product.setUpdateTime(new Date());
                int rowCount = productMapper.insert(product);
                if(rowCount>0){
                    return ServerResponce.createBySuccessMessage("添加产品成功");
                }
                return ServerResponce.createByErrorMessage("添加产品成功失败");
            }
        }
        return ServerResponce.createByErrorMessage("新增或更新产品参数不正确");
    }

    /**
     * 设置产品的状态：上架或者下架
     * @param productId
     * @param status   商品状态.1-在售 2-下架 3-删除
     * @return
     */
    @Override
    public ServerResponce<String> setSaleStatus(Integer productId, Integer status) {
        if(productId==null && status==null){
            return ServerResponce.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product=new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount>0){
            return ServerResponce.createBySuccessMessage("修改产品状态成功");
        }
        return ServerResponce.createByErrorMessage("修改产品状态失败");
    }

    @Override
    public ServerResponce<ProductDetailVo> manageProductDetail(Integer productId) {
        if(productId==null){
            //如果参数为空，直接返回参数错误
            return ServerResponce.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product=productMapper.selectByPrimaryKey(productId);
        if(product==null){
            return ServerResponce.createByErrorMessage("产品下架或者已删除");
        }
        ProductDetailVo productDetailVo=assembleProductDeatilVo(product);
        return ServerResponce.createBySuccess(productDetailVo);
    }

    @Override
    public ServerResponce<PageInfo> getProductList(int pageNum, int pageSize) {
        //分页 1 startPage ---start ； 2 填充自己的SQL查询逻辑 ；3 pageHelper---收尾
        //1
        PageHelper.startPage(pageNum,pageSize);
        //2
        List<Product> productList=productMapper.selectList();

        List<ProductListVo> productListVoList=new ArrayList<>();
        for(Product productItem:productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        //3
        PageInfo pageResult=new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponce.createBySuccess(pageResult);
    }

    @Override
    public ServerResponce<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(productName)){
            productName=new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);

        List<ProductListVo> productListVoList=new ArrayList<>();
        for(Product productItem:productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        PageInfo pageResult=new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponce.createBySuccess(pageResult);
    }

    /**
     * 用于组装 ProductDeatilVo
     * @param product
     * @return
     */
    private ProductDetailVo assembleProductDeatilVo(Product product){
        ProductDetailVo productDetailVo=new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImage(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());

        //从配置文件获取imageHost图片服务器的地址
        //todo 在配置文件设置值。 设置默认值
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix",""));
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category==null){
            productDetailVo.setParentCategoryId(0);
        }else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }

    /**
     * 用于组装 ProductListVo
     * @param product
     * @return
     */
    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo=new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        //todo 在配置文件设置值。 设置默认值
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix",""));
        return productListVo;
    }

    /**
     * 客户端返回产品详情
     * @param productId
     * @return
     */
    @Override
    public ServerResponce<ProductDetailVo> getProductDetail(Integer productId) {
        if(productId==null){
            //如果参数为空，直接返回参数错误
            return ServerResponce.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product=productMapper.selectByPrimaryKey(productId);
        if(product==null){
            return ServerResponce.createByErrorMessage("产品下架或者已删除");
        }
        if(product.getStatus()!= Const.ProductStatusEnmu.ON_SALE.getCode()){
            return ServerResponce.createByErrorMessage("产品下架或者已删除");
        }
        ProductDetailVo productDetailVo=assembleProductDeatilVo(product);
        return ServerResponce.createBySuccess(productDetailVo);
    }

    @Override
    public ServerResponce<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize,String orderBy){
        if(StringUtils.isBlank(keyword) && categoryId==null){
            return ServerResponce.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //储存子节点ID
        List<Integer> categoryIdList=new ArrayList<Integer>();
        //判断分类是否存在
        if(categoryId!=null){
            Category category=categoryMapper.selectByPrimaryKey(categoryId);
            if(category==null && StringUtils.isBlank(keyword)){
                //没有该分类，也没有关键字，返回一个空结果集，不报错
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList= Lists.newArrayList();
                PageInfo pageInfo=new PageInfo(productListVoList);
                return ServerResponce.createBySuccess(pageInfo);
            }
            categoryIdList=iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        //模糊查询字符串
        if(StringUtils.isNotBlank(keyword)){
            keyword=new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum,pageSize);
        //排序处理
        if(StringUtils.isNotBlank(orderBy)) {
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArray=orderBy.split("_");
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
            }
        }

        List<Product> productList=productMapper.selectByNameAndProductIds(StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);

        List<ProductListVo> productListVoList=Lists.newArrayList();
        for (Product product:productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo=new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponce.createBySuccess(pageInfo);
    }

}
