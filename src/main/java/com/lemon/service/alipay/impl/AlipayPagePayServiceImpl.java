package com.lemon.service.alipay.impl;/**
 * Created by xflig on 2018/8/13.
 */

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.lemon.config.alipay.AlipayProperties;
import com.lemon.service.alipay.AlipayPagePayService;
import com.lemon.service.alipay.AlipayWapPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * @ClassName: AlipayPagePayServiceImpl
 * @Description: TODO
 * @Author: 李雪飞
 * @Date: 2018/8/13 23:42
 * @Vserion 1.0
 **/

@Service
@Slf4j
public class AlipayPagePayServiceImpl implements AlipayPagePayService {

    @Autowired
    private AlipayProperties alipayProperties;
    @Autowired
    private AlipayClient client;

    @Autowired
    private AlipayWapPayServiceImpl alipayWapPayServiceImplImpl;

    /**
     * 跳转支付页面.
     * @param model
     * @param response
     */
    @Override
    public void gotoPayPage(AlipayTradePagePayModel model, HttpServletResponse response) throws AlipayApiException, IOException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setReturnUrl("todo");
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        // 调用SDK生成表单, 并直接将完整的表单html输出到页面
        String form = client.pageExecute(request).getBody();
        response.setContentType("text/html;charset=" + alipayProperties.getCharset());
        response.getWriter().write(form);
        response.getWriter().flush();
        response.getWriter().close();
    }

    @Override
    public String returnUrl(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, UnsupportedEncodingException {

        response.setContentType("text/html:charset=" + alipayProperties.getCharset());

        Map<String, String> params = alipayWapPayServiceImplImpl.buildParams(request);

        boolean result = AlipaySignature.rsaCheckV1(params,
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getCharset(),
                "RSA2"
        );

        if (result) {
            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//

            //请在这里加上商户的业务逻辑程序代码，如保存支付宝交易号.
            return "pagepaySuccess";
        } else {
            return "pagepayFail";
        }
    }
}
