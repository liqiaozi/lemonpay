package com.lemon.service.alipay;/**
 * Created by xflig on 2018/8/9.
 */

import com.alipay.demo.trade.model.builder.AlipayTradePayRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradeQueryRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradeRefundRequestBuilder;
import com.google.zxing.WriterException;
import com.lemon.common.ServerResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @ClassName: F2FPayService
 * @Description: 当面付service接口.
 * @Author: 李雪飞
 * @Date: 2018/8/9 20:22
 * @Vserion 1.0
 **/
public interface AlipayF2FPayService {

    /**
     * 当面付-条码支付.
     * @param authCode
     * @param builder
     * @return
     */
    ServerResponse barCodePay(String authCode,AlipayTradePayRequestBuilder builder);


    /**
     * 当面付-扫码支付.
     * @param builder
     * @param request
     * @param response
     */
    void precreatePay(AlipayTradePrecreateRequestBuilder builder, HttpServletRequest request, HttpServletResponse response) throws IOException, WriterException;


    /**
     * 当面付-退款
     * @param builder
     * @return
     */
    ServerResponse tradeRefund(AlipayTradeRefundRequestBuilder builder);


}
