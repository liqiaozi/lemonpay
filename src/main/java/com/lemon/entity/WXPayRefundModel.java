package com.lemon.entity;/**
 * Created by xflig on 2018/8/16.
 */

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: WXPayRefundModel
 * @Description: TODO
 * @Author: 李雪飞
 * @Date: 2018/8/16 0:41
 * @Vserion 1.0
 **/
@Data
public class WXPayRefundModel implements Serializable{
    private String orderNo;

    private String outRefundNo;

    private String totalFee;;

    private String refundFee;

    private String refundFeeType;
}
