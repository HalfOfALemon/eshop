package com.eshop.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {
    private BigDecimalUtil(){
        //私有构造器防止被实例化
    }
    public static BigDecimal add(double v1,double v2){
        BigDecimal b1=new BigDecimal(Double.toString(v1));
        BigDecimal b2=new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }
    public static BigDecimal sub(double v1,double v2){
        BigDecimal b1=new BigDecimal(Double.toString(v1));
        BigDecimal b2=new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }
    public static BigDecimal mul(double v1,double v2){
        BigDecimal b1=new BigDecimal(Double.toString(v1));
        BigDecimal b2=new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }
    public static BigDecimal div(double v1,double v2){
        BigDecimal b1=new BigDecimal(Double.toString(v1));
        BigDecimal b2=new BigDecimal(Double.toString(v2));
        return b1.divide(b2,2, RoundingMode.HALF_UP);
        //除不尽的情况：取两位小数，四舍五入
    }
}
