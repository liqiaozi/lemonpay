package com.lemon.controller.alipay;/**
 * Created by xflig on 2018/8/13.
 */

import com.alipay.demo.trade.model.builder.AlipayTradeQueryRequestBuilder;
import com.lemon.common.ServerResponse;
import com.lemon.service.alipay.AlipayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * @ClassName: AlipayController
 * @Description: 支付宝通用接口.
 * @Author: 李雪飞
 * @Date: 2018/8/13 22:45
 * @Vserion 1.0
 **/
@RestController
@RequestMapping(value="/alipay")
@Slf4j
public class AlipayController {

    @Autowired
    private AlipayService alipayService;


    /**
     * 支付回调.
     * API: https://docs.open.alipay.com/194/103296/
     * @param request
     * @return
     */
    @RequestMapping(value="/notify")
    public String notify(HttpServletRequest request) throws UnsupportedEncodingException {
        String result = alipayService.notify(request);
        return result;
    }

    /**
     * 查询支付信息.
     * 描述:
     *     该接口提供所有支付宝支付订单的查询，商户可以通过该接口主动查询订单状态，完成下一步的业务逻辑。
     * 使用场景:
     *      当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知；
     *      调用支付接口后，返回系统错误或未知交易状态情况；
     *      调用alipay.trade.pay，返回INPROCESS的状态；
     *      调用alipay.trade.cancel之前，需确认支付状态；
     * @param orderNo
     * @param tradeNo
     * @return
     */
    @GetMapping(value="/query")
    public ServerResponse query(String orderNo, String tradeNo){

        AlipayTradeQueryRequestBuilder builder = new AlipayTradeQueryRequestBuilder();
        if(StringUtils.isEmpty(orderNo) && StringUtils.isEmpty(tradeNo)){
            return  null;
        }
        if(!StringUtils.isEmpty(orderNo)){
            builder.setOutTradeNo(orderNo);
        }
        if(!StringUtils.isEmpty(tradeNo)){
            builder.setTradeNo(tradeNo);
        }
        ServerResponse result = alipayService.queryTradeInfo(builder);

        return result;
    }


}
