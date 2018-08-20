package com.lemon.controller.wxpay;/**
 * Created by xflig on 2018/8/16.
 */

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.lemon.config.wxpay.MyWXPayConfig;
import com.lemon.config.wxpay.WXPayClient;
import com.lemon.entity.WXPayRefundModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: WXPayController
 * @Description: 微信支付-通用API.
 * @Author: 李雪飞
 * @Date: 2018/8/16 0:35
 * @Vserion 1.0
 **/
@Slf4j
@RestController
@RequestMapping(value="/wxpay")
public class WXPayController {
    @Autowired
    private WXPay wxPay;

    @Autowired
    private WXPayClient wxPayClient;

    @Autowired
    private MyWXPayConfig wxPayConfig;

    /**
     * 订单查询.
     * @param orderNo
     * @return
     * @throws Exception
     */
    @GetMapping(value="/orderQuery")
    public Object orderQuery(String orderNo) throws Exception {
        Map<String,String> reqData = new HashMap<>();
        reqData.put("out_trade_no",orderNo);
        Map<String, String> resultForQuery = wxPay.orderQuery(reqData);

        log.info(resultForQuery.toString());
        return resultForQuery;
    }

    /**
     * 微信退款
     * 注意:调用申请退款 撤销订单接口需要商户证书;
     *      沙箱环境响应结果可能回事"沙箱支付金额不正确,请确认验收case",但是正式环境不会报该错误;
     *      微信支付的最小金额为0.1元,所以在测试支付时金额必须大于0.1元,否则会提示微信支付配置错误
     * @param model
     * @return
     * @throws Exception
     */
    @PostMapping(value="/refund")
    public Object refund(WXPayRefundModel model) throws Exception {
        Map<String,String> reqData = new HashMap<>();

        reqData.put("out_trade_no",model.getOrderNo());
        reqData.put("out_refund_no",model.getOutRefundNo());
        reqData.put("total_fee",model.getTotalFee());
        reqData.put("refund_fee",model.getRefundFee());
        reqData.put("refund_fee_type",model.getRefundFeeType());

        reqData.put("notify_url",wxPayConfig.getNotifyUrl());
        reqData.put("op_user_id",wxPayConfig.getMchID());

        Map<String, String> resultForRefund = wxPay.refund(reqData);
        log.info(resultForRefund.toString());

        return resultForRefund;
    }

    /**
     * 微信退款结果通知.
     * @param request
     * @return
     */
    @RequestMapping(value="/refund/notify")
    public String refundNotify(HttpServletRequest request) throws Exception {

        /**
         * 注意:同样的通知可能多次发送给商户系统,商户系统必须能够正确的处理重复的通知;
         * 推荐做法:
         *     当收到通知进行处理时,首先检查对应业务数据状态,判断该通知是否已经处理过,如果没有处理过再进行处理,如果处理过就直接返回处理成功;
         *     在对业务进行状态检查和处理之前,要采用数据锁进行并发控制,避免函数重入造成数据混乱.
         */
        Map<String, String> refundNotify = wxPayClient.decodeRefundNotify(request);

        //商户处理退款通知参数后同步给微信参数.
        Map<String,String> responseMap = new HashMap<>();
        responseMap.put("return_code","SUCCESS");
        responseMap.put("return_msg","OK");
        String xml = WXPayUtil.mapToXml(responseMap);

        return xml;
    }

    /**
     * 退款查询.
     * @param orderNo
     * @return
     * @throws Exception
     */
    @GetMapping(value="/refundQuery")
    public Object refundQuery(String orderNo) throws Exception {
        Map<String,String> reqData = new HashMap<>();

        reqData.put("out_trade_no",orderNo);
        Map<String, String> refundQuery = wxPayClient.refundQuery(reqData);
        log.info(refundQuery.toString());
        return refundQuery;
    }

    /**
     * 下载对账单.
     * 注意: 微信再次日9点启动生成前一天的对账单,建议商户10点后再获取;
     * 对账单接口只能下载3个月以内的账单.
     * @param billDate
     * @return
     * @throws Exception
     */
    @PostMapping(value="/downlaodBill")
    public Object downloadBill(String billDate) throws Exception {
        Map<String,String> reqData = new HashMap<>();
        reqData.put("bill_data",billDate);
        reqData.put("bill_type","ALL");
        Map<String, String> downloadBillResult = wxPayClient.downloadBill(reqData);
        log.info(downloadBillResult.toString());
        return downloadBillResult;
    }

    /**
     * 获取沙箱环境的API密钥
     *
     * @return
     * @throws Exception
     */
    @GetMapping(value="/sandbox/genSignKey")
    public Object getSignKey() throws Exception {
        Map<String, String> signKey = wxPayClient.getSignKey();
        log.info(signKey.toString());
        return signKey;
    }

}
