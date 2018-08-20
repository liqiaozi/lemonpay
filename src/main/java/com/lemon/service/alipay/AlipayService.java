package com.lemon.service.alipay;/**
 * Created by xflig on 2018/8/13.
 */

import com.alipay.demo.trade.model.builder.AlipayTradeQueryRequestBuilder;
import com.lemon.common.ServerResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * @ClassName: AlipayService
 * @Description: TODO
 * @Author: 李雪飞
 * @Date: 2018/8/13 22:48
 * @Vserion 1.0
 **/
public interface AlipayService {
    /**
     * 扫码支付-回调.
     * @param request
     * @return
     */
    String notify(HttpServletRequest request) throws UnsupportedEncodingException;

    /**
     * 查询支付信息.
     * @param builder
     * @return
     */
    ServerResponse queryTradeInfo(AlipayTradeQueryRequestBuilder builder);

}
