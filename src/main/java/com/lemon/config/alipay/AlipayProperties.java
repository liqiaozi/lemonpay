package com.lemon.config.alipay;/**
 * Created by xflig on 2018/8/9.
 */

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

/**
 * @ClassName: AlipayProperties
 * @Description: 支付宝支付配置类.
 * @Author: 李雪飞
 * @Date: 2018/8/9 17:59
 * @Vserion 1.0
 **/

@Data
@Slf4j
@ConfigurationProperties(prefix = "pay.alipay")
public class AlipayProperties {

    /** 支付宝 gatewayUrl */
    private String gatewayUrl;

    /** 商户应用id */
    private String appid;

    /** RSA2应用私钥,用于对商户请求报文加签 */
    private String appPrivateKey;

    /** 支付宝RSA2公钥,用于验签支付宝应答 */
    private String alipayPublicKey;

    /** 同步地址 */
    private String returnUrl;

    /** 异步通知地址 */
    private String notifyUrl;

    /** 签名类型 */
    private String signType;

    /** 参数返回格式，只支持json */
    private String format;

    /** 请求和签名使用的字符编码格式，支持GBK和UTF-8 */
    private String charset;

    /** 最大查询次数 */
    private static int maxQueryRetry;

    /** 查询间隔(毫秒) */
    private static long queryDuration;

    /** 最大册小次数 */
    private static int maxCancelRetry;

    /** 册小间隔(毫秒)*/
    private static long cancelDuration;

    private AlipayProperties(){}

    @PostConstruct
    public void init(){
        log.info(alipaySetting());
    }

    public String alipaySetting(){
        StringBuilder sb = new StringBuilder("\nConfigs{");

        sb.append("支付宝网关: ").append(gatewayUrl).append(",\n");
        sb.append("appid: ").append(appid).append(",\n");
        sb.append("商户RSA私钥: ").append(getKeyDescription(appPrivateKey)).append(",\n");
        sb.append("支付宝RSA公钥: ").append(getKeyDescription(alipayPublicKey)).append(",\n");
        sb.append("签名类型: ").append(signType).append(",\n");

        sb.append("查询重试次数: ").append(maxQueryRetry).append(",\n");
        sb.append("查询间隔(毫秒): ").append(queryDuration).append(",\n");
        sb.append("撤销尝试次数: ").append(maxCancelRetry).append(",\n");
        sb.append("撤销重试间隔(毫秒): ").append(cancelDuration).append("\n");

        sb.append("}");
        return sb.toString();

    }

    private String getKeyDescription(String key) {
        int showLength = 6;
        if (StringUtils.isNotEmpty(key) && key.length() > showLength) {
            return new StringBuilder(key.substring(0, showLength)).append("******")
                    .append(key.substring(key.length() - showLength)).toString();
        }
        return null;
    }





}
