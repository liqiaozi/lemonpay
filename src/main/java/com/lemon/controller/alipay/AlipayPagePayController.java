package com.lemon.controller.alipay;/**
 * Created by xflig on 2018/8/13.
 */

import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.lemon.service.alipay.AlipayPagePayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @ClassName: AlipayPagePayController
 * @Description: 支付宝-电脑网站支付.
 * @Author: 李雪飞
 * @Date: 2018/8/13 23:29
 * @Vserion 1.0
 **/
@Controller
@RequestMapping(value = "/alipay/page")
public class AlipayPagePayController {

    public static final String producetCode = "FAST_INSTANT_TRADE_PAY";

    @Autowired
    private AlipayPagePayService alipayPagePayService;

    /**
     * 跳转去电脑网页支付页面.
     * @param model
     * @param response
     */
    public void gotoPayPage(AlipayTradePagePayModel model,HttpServletResponse response) throws IOException, AlipayApiException {
        alipayPagePayService.gotoPayPage(model,response);
    }

    private AlipayTradePagePayModel wrapAlipayTradePagePayModel(AlipayTradePagePayModel model){
        model.setOutTradeNo(UUID.randomUUID().toString());
        model.setSubject("支付测试");
        model.setTotalAmount("0.01");
        model.setBody("支付测试,总金额:0.01元");
        model.setProductCode(this.producetCode);
        return model;
    }


    @RequestMapping(value="/returnUrl")
    public String returnuRL(HttpServletRequest request,HttpServletResponse response){

        String page = alipayPagePayService.returnUrl(request,response);
        return  null;
    }




}
