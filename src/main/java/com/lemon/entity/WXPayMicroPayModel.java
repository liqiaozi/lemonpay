package com.lemon.entity;/**
 * Created by xflig on 2018/8/16.
 */

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: WXPayMicroPayModel
 * @Description: TODO
 * @Author: 李雪飞
 * @Date: 2018/8/16 0:23
 * @Vserion 1.0
 **/
@Data
public class WXPayMicroPayModel implements Serializable {

    private String orderNo;

    private String totalFee;

    private String authCode;

    private String body;
}
