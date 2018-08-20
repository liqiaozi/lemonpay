package com.lemon.common;/**
 * Created by xflig on 2018/7/31.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: ServerResponse
 * @Description: 前后端交互数据标准.
 * @Author: 李雪飞
 * @Date: 2018/7/31 22:41
 * @Vserion 1.0
 **/
@Data
@JsonSerialize(include =  JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成功标志 */
    private Boolean success;

    /** 提示信息 */
    private String message;

    /** 返回码 */
    private Integer code;

    /** 时间戳 */
    private long timestamp = System.currentTimeMillis();

    /** 返回数据对象 */
    private Object data;

    public ServerResponse(){

    }

    public ServerResponse(boolean success, String message, Integer code){
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = null;
    }

    public ServerResponse(boolean success, String message, Integer code, Object data){
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ServerResponse(Object data){
        this.success = Boolean.TRUE;
        this.code = 200;
        this.message = "ok";
        this.data = data;
    }

    public ServerResponse(String message){
        this.success = Boolean.TRUE;
        this.code = 200;
        this.message = message;
        this.data = null;
    }

    @JsonIgnore  //使之不在json序列化结果当中
    public boolean isSuccess(){
        return this.success == Boolean.TRUE;
    }

    public static ServerResponse build(boolean success, String message, Integer code, Object data){
        return new ServerResponse( success,  message,  code,  data);
    }

    /**
     * 成功返回.
     * @return
     */
    public static ServerResponse ok(){
        return new ServerResponse(null);
    }

    /**
     * 成功返回.
     * @return
     */
    public static ServerResponse ok(String message){
        return new ServerResponse(null);
    }

    public static ServerResponse ok(Object data){
        return new ServerResponse(data);
    }


    //------------------------------------------------------------------------------------------------------------------
    /**
     * 错误返回.
     * @param message
     * @return
     */
    public static ServerResponse error(String message){

        return new ServerResponse(Boolean.FALSE,message,500);
    }

    public static ServerResponse error(Integer code,String message){

        return new ServerResponse(Boolean.FALSE,message,code);
    }



    //------------------------------------------------------------------------------------------------------------------
    /**
     * 将json结果集转化为TaotaoResult对象
     *
     * @param jsonData json数据
     * @param clazz TaotaoResult中的object类型
     * @return
     */
    public static ServerResponse formatToPojo(String jsonData, Class<?> clazz) {
        try {
            if (clazz == null) {
                return new ObjectMapper().readValue(jsonData, ServerResponse.class);
            }
            JsonNode jsonNode = new ObjectMapper().readTree(jsonData);
            JsonNode data = jsonNode.get("data");
            Object obj = null;
            if (clazz != null) {
                if (data.isObject()) {
                    obj = new ObjectMapper().readValue(data.traverse(), clazz);
                } else if (data.isTextual()) {
                    obj = new ObjectMapper().readValue(data.asText(), clazz);
                }
            }
            return build(jsonNode.get("success").asBoolean(),
                    jsonNode.get("message").asText(),
                    jsonNode.get("code").intValue(),
                    obj);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 没有object对象的转化
     *
     * @param json
     * @return
     */
    public static ServerResponse format(String json) {
        try {
            return new ObjectMapper().readValue(json, ServerResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Object是集合转化
     *
     * @param jsonData json数据
     * @param clazz 集合中的类型
     * @return
     */
    public static ServerResponse formatToList(String jsonData, Class<?> clazz) {
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(jsonData);
            JsonNode data = jsonNode.get("data");
            Object obj = null;
            if (data.isArray() && data.size() > 0) {
                obj = new ObjectMapper().readValue(data.traverse(),
                        new ObjectMapper().getTypeFactory().constructCollectionType(List.class, clazz));
            }
            return build(jsonNode.get("success").asBoolean(),
                    jsonNode.get("message").asText(),
                    jsonNode.get("code").intValue(),
                    obj);
        } catch (Exception e) {
            return null;
        }
    }




}
