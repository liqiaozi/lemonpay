package com.lemon.service.alipay;/**
 * Created by xflig on 2018/8/13.
 */

import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePagePayModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @ClassName: AlipayPagePayService
 * @Description: TODO
 * @Author: 李雪飞
 * @Date: 2018/8/13 23:41
 * @Vserion 1.0
 **/
public interface AlipayPagePayService {

    /**
     * 跳转支付页面.
     * @param model
     * @param response
     */
    void gotoPayPage(AlipayTradePagePayModel model, HttpServletResponse response) throws AlipayApiException, IOException;

    /**
     * 支付宝服务器同步通知页面.
     * @param request
     * @param response
     * @return
     */
    String returnUrl(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, UnsupportedEncodingException;
}
