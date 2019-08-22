package com.eshop.common;

public class Const {
    public static final String CURRENT_USER="currentUser";
    public static final String EMAIL="email";
    public static final String USERNAME="username";
    //内部接口，用户权限的常量
    public interface Role{
        int ROLE_CUSTOMER =0;//普通用户
        int ROLE_ADMIN =1;//管理员
    }
}
