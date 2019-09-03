package com.eshop.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertiesUtil {
    private static Logger logger= LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties properties;
    /*静态代码块（只执行一次） 优于 普通代码块 优于 构造代码块*/
    static {
        String fileName="eshop.properties";
        properties=new Properties();
        try {
            properties.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"UTF-8"));
        } catch (IOException e) {
            logger.error("配置文件读取异常",e);
        }
    }

    /**
     * 得到配置文件里边的值
     * @param key
     * @return
     */
    public static String getProperty(String key){
        //避免key左右有空格，trim（）去掉空格
        String value=properties.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.trim();
    }

    public static String getProperty(String key,String defaultVaule){
        //避免key左右有空格，trim（）去掉空格
        String value=properties.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            return defaultVaule;
        }
        return value.trim();
    }
}
