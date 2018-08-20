package com.lemon.controller.wxpay;/**
 * Created by xflig on 2018/8/16.
 */

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.lemon.config.wxpay.WXPayClient;
import com.lemon.entity.WXPayH5PayModel;
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
 * @ClassName: WXPayH5PayController
 * @Description: 微信支付-H5支付.
 * @Author: 李雪飞
 * @Date: 2018/8/16 13:23
 * @Vserion 1.0
 **/

@Slf4j
@RestController
@RequestMapping(value="/wxpay/h5Pay")
public class WXPayH5PayController {
    @Autowired
    private WXPay wxPay;

    @Autowired
    private WXPayClient wxPayClient;

    @PostMapping(value="/order")
    public Object h5Pay(WXPayH5PayModel model) throws Exception {
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
        reqData.put("scene_info",model.getScneInfo());

        Map<String, String> responseMap = wxPay.unifiedOrder(reqData);
        log.info(responseMap.toString());

        String returnCode = responseMap.get("return_code");
        String resultCode = responseMap.get("result_code");

        if(WXPayConstants.SUCCESS.equals(returnCode) && WXPayConstants.SUCCESS.equals(resultCode)){
            // 预支付交易会话标识.
            String prepayId = responseMap.get("prepay_id");
            /**
             * 支付跳转链接(前端需要在该地址上拼接上 redirect_url,该参数不是必须的)
             * 正常流程:
             *      用户支付完成后会返回至发起支付的页面,如需返回指定页面,则可以在 MWEB_URL
             *      后拼接上 redirect_url参数,来指定回调页面.需要对 redirect_url 进行urlcode编码.
             *
             */
            String mweb_url = responseMap.get("mweb_url");

            /**
             * 正常流程用户支付完成后会返回至发起支付的页面，如需返回至指定页面，则可以在MWEB_URL后拼接上redirect_url参数，来指定回调页面。

             如，您希望用户支付完成后跳转至https://www.wechatpay.com.cn，则可以做如下处理：

             假设您通过统一下单接口获到的MWEB_URL= https://wx.tenpay.com/cgi-bin/mmpayweb-bin/checkmweb?prepay_id=wx20161110163838f231619da20804912345&package=1037687096

             则拼接后的地址为MWEB_URL= https://wx.tenpay.com/cgi-bin/mmpayweb-bin/checkmweb?prepay_id=wx20161110163838f231619da20804912345&package=1037687096&redirect_url=https%3A%2F%2Fwww.wechatpay.com.cn
             */

        }

        return responseMap;
    }


    /**
     * 回调通知.
     * @param request
     * @param response
     */
    @RequestMapping(value="/notify")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> reqData = wxPayClient.getNotifyParameter(request);
        log.info(reqData.toString());

        String returnCode = reqData.get("return_code");
        String resultCode = reqData.get("result_code");

        if(WXPayConstants.SUCCESS.equals(returnCode) && WXPayConstants.SUCCESS.equals(resultCode)) {
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

}
