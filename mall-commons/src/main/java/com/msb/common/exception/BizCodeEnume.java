package com.msb.common.exception;

/**
 * 错误编码和错误信息的枚举类
 */
public enum BizCodeEnume {

    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式异常"),
    PRODUCT_UP_EXCEPTION(11001,"商品上架异常"),
    VALUE_SMS_EXCEPTION(10002,"短信发送频率过高，请稍等一段时间在发送"),
    USERNAME_EXSIT_EXCEPTION(15001,"用户名已存在"),
    PHONE_EXSIT_EXCEPTION(15002,"手机号已注册"),
    USER_NOEXSIT_EXCEPTION(15003,"用户名不存在，请先注册！！！"),
    LOGIN_CHECK_EXCEPTION(15004,"用户名或密码错误"),
    ORDER_CREATE_EXCEPTION(12001,"多次提交异常"),
    NO_STOCK_EXCEPTION(16001,"库存不足");

    private int code;
    private String msg;

    BizCodeEnume(int code,String msg){
        this.code = code;
        this.msg = msg;
    }
    public int getCode(){
        return code;
    }

    public String getMsg(){
        return msg;
    }
}
