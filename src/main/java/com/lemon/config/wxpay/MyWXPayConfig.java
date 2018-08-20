package com.lemon.config.wxpay;/**
 * Created by xflig on 2018/8/14.
 */

import com.github.wxpay.sdk.WXPayConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @ClassName: MyWXPayConfig
 * @Description: 微信支付参数配置, 需要实现 WXPayConfig 中的方法.
 * @Author: 李雪飞
 * @Date: 2018/8/14 21:51
 * @Vserion 1.0
 **/
@Data
@Slf4j
@ConfigurationProperties(prefix = "pay.wxpay")
public class MyWXPayConfig implements WXPayConfig {

    /**
     * 公众账号ID
     */
    private String appID;

    /**
     * 商户号
     */
    private String mchID;

    /**
     * API密钥
     */
    private String key;

    /**
     * API 沙箱环境密钥
     */
    private String sandboxKey;

    /**
     * API证书绝对路径
     */
    private String certPath;

    /**
     * 退款异步通知地址
     */
    private String notifyUrl;

    /**
     * 是否使用沙箱环境
     */
    private boolean useSandbox;

    /**
     * HTTP(S) 连接超时时间,单位毫秒
     */
    private int httpConnectTimeoutMs = 8000;

    /**
     * HTTP(S) 读数据超时时间,单位毫秒
     */
    private int httpReadTimeoutMs = 10000;


    /**
     * 获取 API 密钥
     *
     * @return API密钥
     */
    @Override
    public String getKey() {
        if (useSandbox) {
            return sandboxKey;
        }
        return key;
    }

    /**
     * 获取证书内容.
     *
     * @return 商户证书内容
     */
    @Override
    public InputStream getCertStream() {
        File certFile = new File(certPath);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(certFile);
        } catch (FileNotFoundException e) {
            log.error("cert file not found,path={},exception is:{}", certPath, e);
        }
        return inputStream;
    }


}
