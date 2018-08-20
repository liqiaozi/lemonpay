package com.lemon.controller.alipay;/**
 * Created by xflig on 2018/8/11.
 */

import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.lemon.common.ServerResponse;
import com.lemon.service.alipay.AlipayWapPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @ClassName: AlipayWapPayController
 * @Description: 支付宝-手机网站支付controller.
 * @Author: 李雪飞
 * @Date: 2018/8/11 23:28
 * @Vserion 1.0
 **/
@Controller
@Slf4j
@RequestMapping(value="/aiipay/wap")
public class AlipayWapPayController {

    @Autowired
    private AlipayWapPayService alipayWapPayService;

    /**
     * 手机网站支付-去支付.
     * 描述:
     *     对于页面跳转类API，SDK不会也无法像系统调用类API一样自动请求支付宝并获得结果，
     *     而是在接受request请求对象后，为开发者生成前台页面请求需要的完整form表单的html（包含自动提交脚本），
     *     商户直接将这个表单的String输出到http response中即可。
     * @param request
     * @param response
     * @throws UnsupportedEncodingException
     */
    @PostMapping(value="/alipage")
    public void gotoPayPage(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException {
        alipayWapPayService.gotoPayPage(request,response);
    }

    /**
     * 支付宝页面跳转同步通知页面.
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value="/returnUrl")
    public String returnUrl(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, AlipayApiException {
        return alipayWapPayService.returnUrl(request,response);
    }

    @PostMapping(value="/refund")
    @ResponseBody
    public ServerResponse refund(String orderNo) throws AlipayApiException {
        AlipayTradeRefundModel model = wrapAlipayTradeRefundModel();
        ServerResponse result = alipayWapPayService.refund(model);
        return result;

    }

    private AlipayTradeRefundModel wrapAlipayTradeRefundModel(){
        //商户订单号和支付宝交易号不能同时为空。 trade_no、  out_trade_no如果同时存在优先取trade_no
        //商户订单号，和支付宝交易号二选一
        String out_trade_no = "123";
        //支付宝交易号，和商户订单号二选一
        String trade_no = "123";
        //退款金额，不能大于订单总金额
        String refund_amount = "0.01";
        //退款的原因说明
        String refund_reason= "无理由退货";
        //标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传。
        String out_request_no = "";

        AlipayTradeRefundModel model=new AlipayTradeRefundModel();
        model.setOutTradeNo(out_trade_no);
        model.setTradeNo(trade_no);
        model.setRefundAmount(refund_amount);
        model.setRefundReason(refund_reason);
        model.setOutRequestNo(out_request_no);

        return model;
    }


    /**
     * 退款查询
     * 描述:商户可使用该接口查询自已通过alipay.trade.refund提交的退款请求是否执行成功。
     *      该接口的返回码10000，仅代表本次查询操作成功，不代表退款成功。
     *      如果该接口返回了查询数据，则代表退款成功，如果没有查询到则代表未退款成功，可以调用退款接口进行重试。
     *      重试时请务必保证退款请求号一致。
     *
     * API: https://docs.open.alipay.com/api_1/alipay.trade.fastpay.refund.query
     * @param orderNo
     * @param refundOrderNo
     * @return
     */
    @GetMapping(value="/refundQuery")
    @ResponseBody
    public ServerResponse refundQuery(String orderNo,String refundOrderNo) throws AlipayApiException {

        AlipayTradeFastpayRefundQueryModel model = wrapAlipayTradeFastpayRefundQueryModel(orderNo,refundOrderNo);
        ServerResponse result = alipayWapPayService.refundQuery(model);
        return result;

    }

    private AlipayTradeFastpayRefundQueryModel wrapAlipayTradeFastpayRefundQueryModel(String orderNo,String refundOrderNo){
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setTradeNo(orderNo);
        model.setOutRequestNo(refundOrderNo);
        return model;
    }

    /**
     * 关闭订单支付.
     * 描述: 用于交易创建后，用户在一定时间内未进行支付，可调用该接口直接将未付款的交易进行关闭。
     * API: https://docs.open.alipay.com/api_1/alipay.trade.close
     * @param orderNo
     * @return
     */
    @PostMapping(value="/close")
    @ResponseBody
    public ServerResponse closePay(String orderNo){

        ServerResponse result = alipayWapPayService.closePay(orderNo);
        return result;
    }





}
