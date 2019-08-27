package com.eshop.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TokenCache {
    //声明日志
    private static Logger logger=LoggerFactory.getLogger(TokenCache.class);
    public static String TOKEN_PREFIX="token_";


    /**
     *     .initialCapacity(1000):设置初始化的容量; maximumSize(1000):当超过3000时，会使用LRU算法对缓存进行清理
     *     expireAfterAccess(12, TimeUnit.HOURS):缓存的有效期是12个小时
     */
    private static LoadingCache<String,String> localCache= CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(3000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默认的加载实现，当调用get方法时，找不到对应的token，就会加载这个方法
                @Override
                public String load(String s) throws Exception {
                    //为了不必要的空指针异常，返回一个字符串的 “null”
                    return "null";
                }
            });

    public static void setKey(String key,String value){
        /*保存key*/
        localCache.put(key,value);
    }
    public static String getKey(String key){
        String value=null;
        try {
            value = localCache.get(key);
            if(value.equals("null")){
                return null;
            }
            return value;
        }catch (Exception e){
            logger.error("localCache get error",e);
        }
        return null;
    }
}
