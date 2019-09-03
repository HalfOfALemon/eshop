package com.eshop.service.impl;

import com.eshop.service.IFileService;
import com.eshop.util.FTPUtil;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    private static Logger logger= LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String upload (MultipartFile file, String path){
        //文件名
        String fileName=file.getOriginalFilename();
        //扩展名  abc.jpg ----> jpg
        String fileExtensionName=fileName.substring(fileName.lastIndexOf(".")+1);
        //要上传的文件名
        String uploadFileName= UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("开始上传文件,上传的文件夹名：{}，上传路径：{}，新文件夹名：{}",fileName,path,uploadFileName);
        //判断有没有该文件夹，没有就创建
        File fileDir=new File(path);
        if(!fileDir.exists()){
            //创建文件夹前，要打开 可写的权限 ，防止tomcat没有开权限
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        //创建一个完全的文件
        File targetFile=new File(path,uploadFileName);
        try {
            file.transferTo(targetFile);
            //文件上传已完成

            //将targetFile上传到FTP服务器上面
            FTPUtil.uploadFile("img",Lists.newArrayList(targetFile));
            //上传完后，把targetFile删除
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        return targetFile.getName();
    }
}
