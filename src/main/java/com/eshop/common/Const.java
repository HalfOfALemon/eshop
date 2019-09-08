package com.eshop.common;

import com.google.common.collect.Sets;

import java.util.Set;

public class Const {
    public static final String CURRENT_USER="currentUser";
    public static final String EMAIL="email";
    public static final String USERNAME="username";
    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC= Sets.newHashSet("price_desc","price_asc");
    }
    /**
     * 内部接口，用户权限的常量
     */
    public interface Role{
        int ROLE_CUSTOMER =0;//普通用户
        int ROLE_ADMIN =1;//管理员
    }
    public interface Cart{
        int CHECKED=1;//购物车选中状态
        int UN_CHECKED=0;//购物车未选中状态

        //限制产品的数量是否成功
        String LIMIT_NUM_FAIL="LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS="LIMIT_NUM_SUCCESS";
    }

    //产品上下架
    public enum ProductStatusEnmu{
        ON_SALE(1,"在线");
        private int code;
        private String value;

        ProductStatusEnmu(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }
    }
}
