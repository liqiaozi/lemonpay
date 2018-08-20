package com.lemon.service.alipay;/**
 * Created by xflig on 2018/8/11.
 */

import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.lemon.common.ServerResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @ClassName: AlipayWapPay
 * @Description: TODO
 * @Author: 李雪飞
 * @Date: 2018/8/11 23:48
 * @Vserion 1.0
 **/
public interface AlipayWapPayService {

    /**
     * 手机网站支付-去支付页面.
     * @param request
     * @param response
     * @throws IOException
     * @throws AlipayApiException
     */
    void gotoPayPage(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException;

    /**
     * 支付宝页面跳转同步通知页面.
     * @param request
     * @param response
     * @return
     */
    String returnUrl(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, AlipayApiException;

    /**
     * 退款.
     * @param model
     * @return
     */
    ServerResponse refund(AlipayTradeRefundModel model) throws AlipayApiException;

    /**
     * 退款查询.
     * @param model
     * @return
     */
    ServerResponse refundQuery(AlipayTradeFastpayRefundQueryModel model) throws AlipayApiException;

    /**
     * 根据订单号关闭订单.
     * @param orderNo
     * @return
     */
    ServerResponse closePay(String orderNo) throws AlipayApiException;

}
