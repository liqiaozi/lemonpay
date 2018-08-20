package com.lemon.config.wxpay;/**
 * Created by xflig on 2018/8/14.
 */

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: WXPayClient
 * @Description:
 *      1.对 WXPay的封装,增加了 microPayWithPos 方法,其内部是调用了 WXPay的microPay;这里当
 * 微信支付时微信提示用户输入密码,逻辑是在指定的时间内去轮询支付结果,然后将轮询的结果返回出去,而不是像其sdk中的
 * 那样直接返回错误.demo中的实现逻辑是,当用户输入密码的情况下会轮询的去下单去调用 WXPay的microPay,这种做法明显和文档不符合.
 *      2.增加了获取沙箱环境API密钥和解密退还通知的方法.
 *
 * @Author: 李雪飞
 * @Date: 2018/8/14 22:10
 * @Vserion 1.0
 **/
@Slf4j
public class WXPayClient extends WXPay{

    /** 密钥算法 */
    private static final String ALGORITHM = "AES";

    /** 加解密算法/工作模式/填充模式 */
    private static final String ALGORITHM_MODE_PADDING = "AES/ECB/PKCS5Padding";

    /** 用户支付中,需要输入密码 */
    private static final String ERR_CODE_USERPAYING = "USERPAYING";
    private static final String ERR_CODE_AUTHCODEEXCEPIRE = "AUTHCODEEXPIRE";

    /** 交易状态:未支付 */
    private static final String TRADE_STATE_NOTPAY = "NOTPAY";

    /** 接口返回错误 */
    private static final String ERR_CODE_SYSTEMERROR = "SYSTEMERROR";

    /** 银行系统异常 */
    private static final String ERR_CODE_BANKERROR = "BANKERROR";

    /** 用户输入密码,尝试30秒内去查询支付结果 */
    private static Integer remainingTimeMs = 30000;

    private WXPayConfig config;

    public WXPayClient(WXPayConfig config, WXPayConstants.SignType signType, boolean useSandbox) {
        super(config, signType, useSandbox);
        this.config = config;
    }

    /**
     * 刷卡支付
     *
     * 描述: 对 WXPay的microPay 增加了当支付结果是 USERPAYING 时,去轮询查询支付结果的逻辑处理.
     *
     * 该方法没有处理 return_code=FAIL 的情况,暂时不考虑网络情况,这种情况直接返回错误.
     * @param reqData
     * @return
     * @throws Exception
     */
    public Map<String,String> microPayWithPOS(Map<String,String> reqData)throws Exception{
        // 开始时间(毫秒)
        long startTimestampMs = System.currentTimeMillis();

        Map<String, String> responseMapForPay = super.microPay(reqData);
        log.info("支付返回信息={}",responseMapForPay.toString());

        //先判断协议返回字段 return_code ,再判断 业务返回 err_code,最后判断交易状态 trade_state
        String returnCode = responseMapForPay.get("return_code");
        if(WXPayConstants.SUCCESS.equals(returnCode)){
            String errCode = responseMapForPay.get("err_code");

            //余额不足,信用卡失效.
            if(ERR_CODE_USERPAYING.equals(errCode) || ERR_CODE_SYSTEMERROR.equals(errCode) || ERR_CODE_BANKERROR.equals(errCode)){
                Map<String,String> orderQueryMap = null;
                Map<String,String> requestData = new HashMap<>();
                requestData.put("out_trade_no",reqData.get("out_trade_no"));

                // 用户支付中,需要输入密码或系统错误,则去重新查询订单API err_code result_code err_code_des
                //每次循环时的当前系统时间 - 开始时的记录时间 > 30秒就退出.
                while(System.currentTimeMillis() - startTimestampMs < remainingTimeMs){
                    // 商户收银台得到 USERPAYING 状态后,经过商户后台系统调用 [查询订单API]查询实际支付结果.
                    orderQueryMap = super.orderQuery(requestData);
                    String returnCodeForQuery = orderQueryMap.get("return_code");

                    if(WXPayConstants.SUCCESS.equals(returnCodeForQuery)){
                        //通讯成功.
                        String tradeState = orderQueryMap.get("trade_state");
                        if(WXPayConstants.SUCCESS.equals(tradeState)){
                            //如果成功了直接将查询结果返回.
                            return orderQueryMap;
                        }
                        //如果支付结果仍为支付中(USERPAYING),则每隔5秒循环调用[查询订单API]判断实际结果.
                        Thread.sleep(5000);
                    }
                }

                //如果用户取消支付或累计30秒都未支付,商户收银台退出查询流程后继续调用[撤销订单API]撤销支付交易.
                String tradeState = orderQueryMap.get("trade_state");
                if(TRADE_STATE_NOTPAY.equals(tradeState) || ERR_CODE_USERPAYING.equals(tradeState) || ERR_CODE_AUTHCODEEXCEPIRE.equals(tradeState)){
                    Map<String, String> reverseMap = this.reverse(requestData);
                    String returnCodeForReverse = reverseMap.get("return_code");
                    String resultCode = reverseMap.get("result_code");
                    if(WXPayConstants.SUCCESS.equals(returnCodeForReverse) && WXPayConstants.SUCCESS.equals(resultCode) ){
                        //如果撤销成功了,需要告诉客户端:已经撤销该订单了.
                        responseMapForPay.put("err_code_des","用户取消支付或未支付,后台已经撤销该订单,请重新支付!");
                    }
                }
            }
        }

        return responseMapForPay;
    }


    /**
     * 从request的inputStream中获取参数
     * @param request
     * @return
     * @throws Exception
     */
    public Map<String, String> getNotifyParameter(HttpServletRequest request) throws Exception {
        InputStream inputStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, length);
        }
        outSteam.close();
        inputStream.close();

        // 获取微信调用我们notify_url的返回信息
        String resultXml = new String(outSteam.toByteArray(), "utf-8");
        Map<String, String> notifyMap = WXPayUtil.xmlToMap(resultXml);

        return notifyMap;
    }

    /**
     * 解密退款通知
     *
     * <a href="https://pay.weixin.qq.com/wiki/doc/api/micropay.php?chapter=9_16&index=11>退款结果通知文档</a>
     * @return
     * @throws Exception
     */
    public Map<String, String> decodeRefundNotify(HttpServletRequest request) throws Exception {
        // 从request的流中获取参数
        Map<String, String> notifyMap = this.getNotifyParameter(request);
        log.info(notifyMap.toString());

        String reqInfo = notifyMap.get("req_info");
        //（1）对加密串A做base64解码，得到加密串B
        byte[] bytes = new BASE64Decoder().decodeBuffer(reqInfo);

        //（2）对商户key做md5，得到32位小写key* ( key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置 )
        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING);
        SecretKeySpec key = new SecretKeySpec(WXPayUtil.MD5(config.getKey()).toLowerCase().getBytes(), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        //（3）用key*对加密串B做AES-256-ECB解密（PKCS7Padding）
        // java.security.InvalidKeyException: Illegal key size or default parameters
        // https://www.cnblogs.com/yaks/p/5608358.html
        String responseXml = new String(cipher.doFinal(bytes),"UTF-8");
        Map<String, String> responseMap = WXPayUtil.xmlToMap(responseXml);
        return responseMap;
    }

    /**
     * 获取沙箱环境验签秘钥API
     * <a href="https://pay.weixin.qq.com/wiki/doc/api/micropay.php?chapter=23_1">获取验签秘钥API文档</a>
     * @return
     * @throws Exception
     */
    public Map<String, String> getSignKey() throws Exception {
        Map<String, String> reqData = new HashMap<>();
        reqData.put("mch_id", config.getMchID());
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        String sign = WXPayUtil.generateSignature(reqData, config.getKey(), WXPayConstants.SignType.MD5);
        reqData.put("sign", sign);
        String responseXml = this.requestWithoutCert("https://api.mch.weixin.qq.com/sandboxnew/pay/getsignkey", reqData,
                config.getHttpConnectTimeoutMs(), config.getHttpReadTimeoutMs());

        Map<String, String> responseMap = WXPayUtil.xmlToMap(responseXml);

        return responseMap;
    }


}
