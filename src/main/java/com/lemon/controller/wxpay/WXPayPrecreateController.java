package com.lemon.controller.wxpay;/**
 * Created by xflig on 2018/8/16.
 */

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.lemon.config.wxpay.WXPayClient;
import com.lemon.entity.WXPayPrecreatePayModel;
import com.lemon.utils.PayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: WXPayPrecreateController
 * @Description: 微信支付-扫码支付.
 * https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=6_5
 * @Author: 李雪飞
 * @Date: 2018/8/16 12:36
 * @Vserion 1.0
 **/
@Slf4j
@RestController
@RequestMapping(value="/wxpay/precreate")
public class WXPayPrecreateController {

    @Autowired
    private WXPay wxPay;

    @Autowired
    private WXPayClient wxPayClient;

    /**
     * 扫码支付-统一下单
     */
    @PostMapping(value="")
    public void precreate(WXPayPrecreatePayModel model, HttpServletResponse response) throws Exception {
        Map<String,String> reqData = new HashMap<>();
        reqData.put("out_trade_no",model.getOrderNo());
        reqData.put("trade_type",model.getTradeType());
        reqData.put("product_id",model.getProductId());
        reqData.put("body",model.getBody());
        reqData.put("total_fee",model.getTotal_fee());
        reqData.put("spbill_create_ip",model.getSpbiiCreateIP());
        reqData.put("notify_url",model.getNotifyUrl());
        reqData.put("device_info",model.getDeviceInfo());
        reqData.put("attach",model.getAttach());

        /**
         * 返回结果:
         * {
         * code_url=weixin://wxpay/bizpayurl?pr=vvz4xwC,
         * trade_type=NATIVE,
         * return_msg=OK,
         * result_code=SUCCESS,
         * return_code=SUCCESS,
         * prepay_id=wx18111952823301d9fa53ab8e1414642725
         * }
         */

        Map<String, String> responseMap = wxPay.unifiedOrder(reqData);
        log.info(responseMap.toString());

        String returnCode = responseMap.get("return_code");
        String resultCode = responseMap.get("result_code");

        if(WXPayConstants.SUCCESS.equals(returnCode) && WXPayConstants.SUCCESS.equals(resultCode)){
            String prepayId = responseMap.get("prepay_id");
            String codeUrl = responseMap.get("code_url");

            //身材二维码图片.
            BufferedImage image = PayUtil.genQRCodeImage(codeUrl);

            response.setContentType("image/jpeg");
            response.setHeader("Pragma","no-cache");
            response.setHeader("Cache-Control","no-cache");
            response.setIntHeader("Expires",-1);
            ImageIO.write(image,"JPEG",response.getOutputStream());
        }
    }

    /**
     * 微信支付-扫码支付回调通知.
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="/notify")
    public void precreatePayNotify(HttpServletRequest request,HttpServletResponse response) throws Exception {
        Map<String, String> reqData = wxPayClient.getNotifyParameter(request);
        /**
         * {
         * transaction_id=4200000138201806180751222945,
         * nonce_str=aaaf3fe4d3aa44d8b245bc6c97bda7a8,
         * bank_type=CFT,
         * openid=xxx,
         * sign=821A5F42F5E180ED9EF3743499FBCF13,
         * fee_type=CNY,
         * mch_id=xxx,
         * cash_fee=1,
         * out_trade_no=186873223426017,
         * appid=xxx,
         * total_fee=1,
         * trade_type=NATIVE,
         * result_code=SUCCESS,
         * time_end=20180618131247,
         * is_subscribe=N,
         * return_code=SUCCESS
         * }
         */

        log.info(reqData.toString());

        //注意:商户系统对于支付结果通知的内容一定要做签名验证,并校验返回的订单金额是否
        //     与商户侧的订单金额一致,防止数据泄露导致出现"假通知",造成资金损失.
        boolean signatureValid = wxPay.isPayResultNotifySignatureValid(reqData);
        if(signatureValid){
            /**
             * 注意:同样的通知可能会多次发送给商户系统,商户系统必须能够正确处理重复的通知;
             * 推荐的做法是,当收到通知进行处理时,首先检查对应业务数据状态,判断该通知是否已经处理过,
             * 如果没有处理过再进行处理,如果处理过就直接返回处理成功;
             * 在对业务进行状态检查和处理之前,要采用数据锁进行并发控制,避免函数重入造成数据混乱.
             */
            Map<String,String> responseMap = new HashMap<>();
            responseMap.put("return_code","SUCCESS");
            responseMap.put("return_msg","OK");
            String responseXML = WXPayUtil.mapToXml(responseMap);

            response.setContentType("text/xml");
            response.getWriter().write(responseXML);
            response.flushBuffer();

        }

    }


}
