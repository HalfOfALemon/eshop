package com.eshop.controller.backend;

import com.eshop.common.Const;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponce;
import com.eshop.pojo.Product;
import com.eshop.pojo.User;
import com.eshop.service.IFileService;
import com.eshop.service.IProductService;
import com.eshop.service.IUserService;
import com.eshop.util.PropertiesUtil;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("manage/product")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    /**
     * 保存或者更新产品
     * @param session
     * @param product
     * @return
     */
    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponce productSave(HttpSession session, Product product){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            //强制用户登录
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //进行产品的保存或者修改
            return iProductService.saveOrUpdateProduct(product);
        }else {
            return ServerResponce.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 修改产品状态
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponce<String> setSaleStatus(HttpSession session, Integer productId,Integer status){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            //强制用户登录
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //进行产品的状态
            return iProductService.setSaleStatus(productId,status);
        }else {
            return ServerResponce.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 获取产品详情
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponce getDetail(HttpSession session, Integer productId){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            //强制用户登录
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充业务
            return iProductService.manageProductDetail(productId);
        }else {
            return ServerResponce.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 获取产品列表
     * @param session
     * @param pageNum 第几页
     * @param pageSize 一页多少个
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponce getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            //强制用户登录
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充业务
            return iProductService.getProductList(pageNum,pageSize);
        }else {
            return ServerResponce.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 后台搜索功能
     * @param session
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponce searchProduct(HttpSession session,String productName,Integer productId, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            //强制用户登录
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充业务
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }else {
            return ServerResponce.createByErrorMessage("无权限操作");
        }
    }
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponce upload(HttpSession session,@RequestParam(name = "upload_name",required = false) MultipartFile file, HttpServletRequest request){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            //强制用户登录
            return ServerResponce.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充业务
            String path=request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            String url= PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

            Map fileMap= Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponce.createBySuccess(fileMap);
        }else {
            return ServerResponce.createByErrorMessage("无权限操作");
        }
    }

    /**
     *
     * @param session
     * @param file
     * @param request
     * @param response  要添加响应头： response.addHeader("Access-Control-Allow-Headers","X-File-Name");
     * @return      {
     *                  "success": true/false,
     *                      "msg": "error message", # optional
     *                  "file_path": "[real file path]"
     *              }
     */
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(name = "upload_name",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map resultMap=Maps.newHashMap();
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            //强制用户登录
            resultMap.put("success",false);
            resultMap.put("msg","用户未登录");
            return resultMap;
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充业务
            //使用simditor富文本上传文件，对于返回值会有要求
            //富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
            String path=request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            String url= PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);

            //要添加响应头
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");

            return resultMap;
        }else {
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
    }

}
