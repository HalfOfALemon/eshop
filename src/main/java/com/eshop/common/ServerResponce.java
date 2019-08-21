package com.eshop.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
@JsonInclude(JsonInclude.Include.NON_NULL)
//除去null字段，属性为空时，不再json里边显示,旧的写法：@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponce<T> implements Serializable {
    private int status;
    private String msg;
    private T data;
    /*构造器*/
    private ServerResponce(int status){
        this.status=status;
    }
    private ServerResponce(int status,T data){
        this.status=status;
        this.data=data;
    }
    private ServerResponce(int status,String msg){
        this.status=status;
        this.msg=msg;
    }
    private ServerResponce(int status,String msg,T data){
        this.status=status;
        this.msg=msg;
        this.data=data;
    }
    /*对外开放的方法*/
    @JsonIgnore
    /* 使之不再序列化结果当中。*/
    public boolean isSuccess(){
        return this.status==ResponseCode.SUCCESS.getCode();
    }
    public int getStatus(){
        return  status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
    /*对外开放的静态方法*/

    public static <T> ServerResponce<T> createBySuccess(){
        /*创建一个成功的服务器响应对象*/
        return new ServerResponce<>(ResponseCode.SUCCESS.getCode());
    }
    public static <T> ServerResponce<T> createBySuccessMessage(String msg){
        return new ServerResponce<>(ResponseCode.SUCCESS.getCode(),msg);
    }
    public static <T> ServerResponce<T> createBySuccess(T data){
        return new ServerResponce<>(ResponseCode.SUCCESS.getCode(),data);
    }
    public static <T> ServerResponce<T> createBySuccess(String msg,T data){
        return new ServerResponce<>(ResponseCode.SUCCESS.getCode(),msg,data);
    }
    public static <T> ServerResponce<T> createByError(){
        /*创建一个错误对象*/
        return new ServerResponce<>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }
    public static <T> ServerResponce<T> createByErrorMessage(String errorMessage){
        return new ServerResponce<>(ResponseCode.ERROR.getCode(),errorMessage);
    }
    public static <T> ServerResponce<T> createByErrorCodeMessage(int errorCode,String errorMessage){
        return new ServerResponce<>(errorCode,errorMessage);
    }

}
