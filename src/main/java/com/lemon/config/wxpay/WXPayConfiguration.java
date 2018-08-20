package com.lemon.config.wxpay;/**
 * Created by xflig on 2018/8/15.
 */

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: WXPayConfiguration
 * @Description: 微信支付配置
 * @Author: 李雪飞
 * @Date: 2018/8/15 0:20
 * @Vserion 1.0
 **/
@Configuration
@EnableConfigurationProperties(MyWXPayConfig.class)
public class WXPayConfiguration {
    @Autowired
    private MyWXPayConfig wxPayConfig;

    @Bean
    public WXPay wxPay(){
        return new WXPay(wxPayConfig, WXPayConstants.SignType.MD5,wxPayConfig.isUseSandbox());
    }

    @Bean
    public WXPayClient wxPayClient(){
        return new WXPayClient(wxPayConfig, WXPayConstants.SignType.MD5,wxPayConfig.isUseSandbox());
    }
}
