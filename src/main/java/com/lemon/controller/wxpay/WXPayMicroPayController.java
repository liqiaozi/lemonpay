package com.lemon.controller.wxpay;/**
 * Created by xflig on 2018/8/16.
 */

import com.lemon.config.wxpay.WXPayClient;
import com.lemon.entity.WXPayMicroPayModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: WXPayMicroPayController
 * @Description: 微信支付-刷卡支付
 * @Author: 李雪飞
 * @Date: 2018/8/16 0:19
 * @Vserion 1.0
 **/

@Slf4j
@RestController
@RequestMapping(value = "/wxpay/microPay")
public class WXPayMicroPayController {

    @Autowired
    private WXPayClient wxPayClient;

    /**
     * 刷卡支付(类似于支付宝的条码支付)
     *
     * 微信支付后台系统收到支付请求,根据验证密码规则判断是否验证用户的支付密码,
     *      不需要验证密码的交易直接发起扣款;
     *      需要验证密码的交易会弹出密码弹出框,
     * 支付成功后微信端会弹出成功页面,支付失败会弹出支付失败提示.
     * 注意:该接口有可能返回错误码为: USERPAYING 用户支付中
     *
     * 验证密码规则:
     * ###支付金额 >1000元的交易需要验证用户支付密码;
     * ###用户账号每天最多有5笔交易可以免密,超过后需要验证密码;
     * ###微信支付后台判断用户支付行为是否有异常情况,复合免密规则的交易也会验证密码.
     * @param model
     * @return
     * @throws Exception
     */
    @PostMapping(value="")
    public Object microPay(WXPayMicroPayModel model) throws Exception {
        Map<String,String> reqData = new HashMap<>();

        reqData.put("out_trade_no",model.getOrderNo()); //商户订单号/
        reqData.put("total_fee",model.getTotalFee());   //订单总金额,单位为分,整数
        reqData.put("auth_code",model.getAuthCode());   //授权码.
        reqData.put("body",model.getBody());            //订单描述.

        Map<String, String> resultMap = wxPayClient.microPayWithPOS(reqData);
        log.info(resultMap.toString());
        return resultMap;
    }
}
