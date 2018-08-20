package com.lemon.config.alipay;/**
 * Created by xflig on 2018/8/9.
 */

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: AlipayConfiguration
 * @Description: TODO
 * @Author: 李雪飞
 * @Date: 2018/8/9 18:27
 * @Vserion 1.0
 **/
@Configuration
@EnableConfigurationProperties(AlipayProperties.class)
public class AlipayConfiguration {

    private AlipayProperties alipayProperties;

    public AlipayConfiguration(AlipayProperties alipayProperties) {
        this.alipayProperties = alipayProperties;
    }

    // 当面付网站使用.
    @Bean
    public AlipayTradeService alipayTradeService() {
        return new AlipayTradeServiceImpl.ClientBuilder()
                .setGatewayUrl(alipayProperties.getGatewayUrl())
                .setAppid(alipayProperties.getAppid())
                .setPrivateKey(alipayProperties.getAppPrivateKey())
                .setAlipayPublicKey(alipayProperties.getAlipayPublicKey())
                .setSignType(alipayProperties.getSignType())
                .setFormat(alipayProperties.getFormat())
                .setCharset(alipayProperties.getCharset())
                .build();
    }

    // 手机网站支付使用.
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(alipayProperties.getGatewayUrl(),
                alipayProperties.getAppid(),
                alipayProperties.getAppPrivateKey(),
                alipayProperties.getFormat(),
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType()
        );

    }


}
