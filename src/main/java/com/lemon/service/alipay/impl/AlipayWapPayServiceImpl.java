package com.lemon.service.alipay.impl;/**
 * Created by xflig on 2018/8/11.
 */

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.lemon.common.ServerResponse;
import com.lemon.config.alipay.AlipayProperties;
import com.lemon.service.alipay.AlipayWapPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName: AlipayWapPayServiceImpl
 * @Description: TODO
 * @Author: 李雪飞
 * @Date: 2018/8/11 23:52
 * @Vserion 1.0
 **/
@Service
@Slf4j
public class AlipayWapPayServiceImpl implements AlipayWapPayService {

    /**
     * 销售产品码
     */
    public static final String PRODUCT_CODE = "QUICK_WAP_WAY";

    @Autowired
    private AlipayClient client;

    @Autowired
    private AlipayProperties alipayProperties;


    /**
     * 去支付页面.
     *
     * @param request
     * @param response
     * @throws UnsupportedEncodingException
     */
    @Override
    public void gotoPayPage(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException {

        // 封装请求支付信息.
        AlipayTradeWapPayModel model = buildAlipayTradeWapPayModel(request);

        AlipayTradeWapPayRequest alipay_request = new AlipayTradeWapPayRequest();
        alipay_request.setBizModel(model);
        alipay_request.setNotifyUrl(alipayProperties.getNotifyUrl());
        alipay_request.setReturnUrl(alipay_request.getReturnUrl());

        // 调用SDK生成比导弹,并直接将完成的表单html输出到页面.
        String form = client.pageExecute(alipay_request).getBody();
        response.setContentType("text/html;charset=" + alipayProperties.getCharset());
        response.getWriter().write(form);
        response.getWriter().flush();
        response.getWriter().close();

    }


    /**
     * 支付宝页面跳转同步通知页面.
     * @param request
     * @param response
     * @return
     * @throws UnsupportedEncodingException
     * @throws AlipayApiException
     */
    @Override
    public String returnUrl(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, AlipayApiException {
        response.setContentType("text/html:charset=" + alipayProperties.getCharset());

        Map<String, String> params = buildParams(request);

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
            return "wappaySuccess";
        } else {
            return "wappayFail";
        }
    }

    /**
     * 退款.
     * @param model
     * @return
     * @throws AlipayApiException
     */
    @Override
    public ServerResponse refund(AlipayTradeRefundModel model) throws AlipayApiException {

        AlipayTradeRefundRequest alipay_request = new AlipayTradeRefundRequest();
        alipay_request.setBizModel(model);

        AlipayTradeRefundResponse response = client.execute(alipay_request);
        if(response.getCode().equals("10000")){
            log.info("退款成功=========");
            log.info("退款结果为:{}",response.getBody());
            return ServerResponse.ok(response.getBody());
        }else{
            log.info("退款失败,错误信息为:{}",response.getBody());
            return ServerResponse.error("退款失败");
        }


    }

    /**
     * 退款查询.
     * @param model
     * @return
     */
    @Override
    public ServerResponse refundQuery(AlipayTradeFastpayRefundQueryModel model) throws AlipayApiException {
        AlipayTradeFastpayRefundQueryRequest alipayRequest = new AlipayTradeFastpayRefundQueryRequest();
        alipayRequest.setBizModel(model);

        AlipayTradeFastpayRefundQueryResponse response = client.execute(alipayRequest);

        if(response.getCode().equals("10000")){
            log.info("退款查询成功=========");
            log.info("退款查询结果为:{}",response.getBody());
            return ServerResponse.ok(response.getBody());
        }else{
            log.info("退款查询失败,错误信息为:{}",response.getBody());
            return ServerResponse.error("退款查询失败");
        }
    }

    /**
     * 关闭支付.
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse closePay(String orderNo) throws AlipayApiException {
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(orderNo);

        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizModel(model);

        AlipayTradeCloseResponse response = client.execute(request);

        if(response.getCode().equals("10000")){
            log.info("订单关闭支付成功=========");
            log.info("订单关闭支付结果为:{}",response.getBody());
            return ServerResponse.ok(response.getBody());
        }else{
            log.info("订单关闭支付失败,错误信息为:{}",response.getBody());
            return ServerResponse.error("订单关闭支付失败");
        }
    }


    public Map<String, String> buildParams(HttpServletRequest request) throws UnsupportedEncodingException {
        //获取支付宝GET过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        return params;
    }


    /**
     * 封装请求支付信息.
     *
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    private AlipayTradeWapPayModel buildAlipayTradeWapPayModel(HttpServletRequest request) throws UnsupportedEncodingException {
        // (必填) 商户订单号，商户网站订单系统中唯一订单号
        String out_trade_no = new String(request.getParameter("orderNo").getBytes("ISO-8859-1"), "UTF-8");
        // (必填) 订单名称
        String subject = new String(request.getParameter("subject").getBytes("ISO-8859-1"), "UTF-8");
        // (必填) 付款金额
        String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");
        // (可空) 商品描述
        String body = new String(request.getParameter("body").getBytes("ISO-8859-1"), "UTF-8");
        // (可空) 超时时间
        String timeout_express = "2m";

        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(out_trade_no);
        model.setSubject(subject);
        model.setTotalAmount(total_amount);
        model.setBody(body);
        model.setTimeoutExpress(timeout_express);
        model.setProductCode(this.PRODUCT_CODE);

        return model;

    }
}
