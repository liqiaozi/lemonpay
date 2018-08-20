package com.lemon.common;

/**
 * Created by geely
 */
public enum ResponseStatus {

    SUCCESS(200,"OK"),
    ERROR(500,"ERROR")
    ;

    private final int code;
    private final String desc;


    ResponseStatus(int code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode(){
        return code;
    }
    public String getDesc(){
        return desc;
    }

}
