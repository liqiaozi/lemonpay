package com.lemon.entity;/**
 * Created by xflig on 2018/8/16.
 */

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: WXPayPrecreatePayModel
 * @Description: TODO
 * @Author: 李雪飞
 * @Date: 2018/8/16 12:40
 * @Vserion 1.0
 **/
@Data
public class WXPayPrecreatePayModel implements Serializable {

    /** 支付订单号 */
    private String orderNo;

    /** 支付类型 */
    private String tradeType;

    /** 产品ID */
    private String productId;

    /** 订单描述 */
    private String body;

    /** 支付总金额 */
    private String total_fee;

    /** APP和网页支付提交用户端IP,Native 支付填调用微信支付API的机器IP */
    private String spbiiCreateIP;

    /** 异步接收微信支付结果通知的回调地址,通知url必须为日外网可访问的url,不能携带参数 */
    private String notifyUrl;

    /** 自定义参数,可以为终端设备号(门店号或收银设备ID),PC网页或公众号内支付可以传"WEB" */
    private String deviceInfo;

    /** 附加数据,在查询api或支付通知中原样返回,可作为自定义参数使用 */
    private String attach;

}
