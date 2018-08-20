package com.lemon.service.alipay.impl;/**
 * Created by xflig on 2018/8/13.
 */

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.demo.trade.model.builder.AlipayTradeQueryRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FQueryResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.google.gson.Gson;
import com.lemon.common.PayConstant;
import com.lemon.common.ServerResponse;
import com.lemon.config.alipay.AlipayProperties;
import com.lemon.service.alipay.AlipayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName: AlipayServiceImpl
 * @Description: TODO
 * @Author: 李雪飞
 * @Date: 2018/8/13 22:50
 * @Vserion 1.0
 **/

@Slf4j
@Service
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    private AlipayProperties alipayProperties;

    @Autowired
    private AlipayTradeService alipayTradeService;

    /**
     * 支付回调.
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    @Override
    public String notify(HttpServletRequest request) throws UnsupportedEncodingException {
        //一定要验签,防止黑客篡改数据.
        Map<String, String[]> parameterMap = request.getParameterMap();

        StringBuilder notifyBuild = new StringBuilder("/****************************** alipay notify ******************************/\n");
        parameterMap.forEach((key, value) -> notifyBuild.append(key + "=" + value[0] + "\n"));
        log.info(notifyBuild.toString());

        boolean flag = this.rsaCheckV1(request);
        if (flag) {

            /**
             * TODO 需要严格按照如下描述校验通知数据的正确性
             *
             * 商户需要验证该通知数据中的 out_trade_no 是否为商户系统中创建的订单号对应的out_trade_no ，
             * 并判断 total_amount 是否确实为该订单的实际金额（即商户订单创建时的金额），
             * 同时需要校验通知中的 seller_id（或者seller_email) 是否为o ut_trade_no 这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），
             *
             * 上述有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
             * 在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，并且过滤重复的通知结果数据。
             * 在支付宝的业务通知中，只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功。
             */

            //交易状态
            String tradeStatus = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
            // 商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //付款金额
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

            // TRADE_FINISHED(表示交易已经成功结束，并不能再对该交易做后续操作);
            // TRADE_SUCCESS(表示交易已经成功结束，可以对该交易做后续操作，如：分润、退款等);
            if(tradeStatus.equals("TRADE_FINISHED")){
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，
                // 并判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），并执行商户的业务程序
                //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                //如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
            } else if (tradeStatus.equals("TRADE_SUCCESS")){
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，
                // 并判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），并执行商户的业务程序
                //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。

            }

            return PayConstant.SUCCESS;
        }


        return PayConstant.FAIL;
    }

    /**
     * 当面付-查询支付信息.(通用)
     * API: https://docs.open.alipay.com/api_1/alipay.trade.query
     *
     * @param builder
     * @return
     */
    @Override
    public ServerResponse queryTradeInfo(AlipayTradeQueryRequestBuilder builder) {
        AlipayF2FQueryResult result = alipayTradeService.queryTradeResult(builder);

        AlipayTradeQueryResponse response = result.getResponse();
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("查询返回该订单支付成功!");
                return ServerResponse.ok(response);

            case FAILED:
                log.error("查询返回该订单支付失败,请求信息为:{},异常响应为:{}", new Gson().toJson(builder), new Gson().toJson(response));
                return ServerResponse.error(Integer.valueOf(response.getCode()), "查询返回该订单支付失败.");

            case UNKNOWN:
                log.error("系统异常,订单支付状态失败,请求信息为:{},异常响应为:{}", new Gson().toJson(builder), new Gson().toJson(response));
                return ServerResponse.error(Integer.valueOf(response.getCode()), "查询返回该订单支付失败.");

            default:
                log.error("不支持的交易状态,交易返回异常.请求信息为:{},异常响应为:{}", new Gson().toJson(builder), new Gson().toJson(response));
                return ServerResponse.error(Integer.valueOf(response.getCode()), "查询返回该订单支付失败.");
        }

    }


    /**
     * JAVA服务端验证异步通知信息参数
     * URL: https://docs.open.alipay.com/54/106370
     *
     * @param request
     * @return
     */
    private boolean rsaCheckV1(HttpServletRequest request) {
        // 获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        try {
            boolean verifyResult = AlipaySignature.rsaCheckV1(params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType());

            return verifyResult;
        } catch (AlipayApiException e) {
            log.debug("verify sigin error, exception is:{}", e);
            return false;
        }

    }
}
