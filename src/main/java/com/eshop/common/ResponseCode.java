package com.eshop.common;

/**
 * 响应编码的枚举类
 */
public enum  ResponseCode {
    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    //需要登录
    NEED_LOGIN(10,"NEED_LOGIN"),
    //参数错误
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");
    /*构造器，与SUCCESS(0,"SUCCESS")里的参数有关*/
    private final int code;
    private final String desc;
    ResponseCode(int code, String desc) {
        this.code=code;
        this.desc = desc;
    }
    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
