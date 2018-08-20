package com.lemon.controller.alipay;/**
 * Created by xflig on 2018/8/9.
 */

import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePayRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradeQueryRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradeRefundRequestBuilder;
import com.lemon.common.ServerResponse;
import com.lemon.service.alipay.AlipayF2FPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @ClassName: AlipayF2FController
 * @Description: 支付宝-当面付.
 * @Author: 李雪飞
 * @Date: 2018/8/9 18:48
 * @Vserion 1.0
 **/

@Slf4j
@RestController
@RequestMapping(value = "/alipay/f2fpay")
public class AlipayF2FPayController {

    @Autowired
    private AlipayF2FPayService f2FPayService;

    /**
     * 当面付-条码付.
     * 描述: 收银员使用扫码设备读取用户手机支付宝“付款码”/声波获取设备（如麦克风）
     *       读取用户手机支付宝的声波信息后，将二维码或条码信息/声波信息通过本接口上送至支付宝发起支付。
     * @param authCode
     * @param orderId
     * @return
     */
    @PostMapping(value="/barCodePay")
    public ServerResponse barCodePay(String authCode,String orderId){
        //TODO 根据订单ID,查询订单信息.
        //TODO 根据订单信息,构造请求支付信息.
        AlipayTradePayRequestBuilder builder = wrapAlipayTradePayRequestBuilderDemo();
        ServerResponse result = f2FPayService.barCodePay(authCode, builder);
        return result;
    }

    private AlipayTradePayRequestBuilder wrapAlipayTradePayRequestBuilderDemo(){

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        String outTradeNo = UUID.randomUUID().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“喜士多（浦东店）消费”
        String subject = "测试订单";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = "购买商品2件共20.05元";

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = "0.01";

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";


        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<>();
        GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "全麦小面包", 1, 1);
        goodsDetailList.add(goods1);
        GoodsDetail goods2 = GoodsDetail.newInstance("goods_id002", "黑人牙刷", 1, 2);
        goodsDetailList.add(goods2);

        // 支付超时，线下扫码交易定义为5分钟
        String timeoutExpress = "5m";


        AlipayTradePayRequestBuilder builder = new AlipayTradePayRequestBuilder()
                .setOutTradeNo(outTradeNo)
                .setSubject(subject)
                .setBody(body)
                .setTotalAmount(totalAmount)
                .setTotalAmount(totalAmount)
                .setStoreId(storeId)
                .setOperatorId(operatorId)
                .setGoodsDetailList(goodsDetailList)
                .setTimeoutExpress(timeoutExpress);

        return builder;

    }


    /**
     * 当面付-扫码支付.
     * 描述:收银员通过收银台或商户后台调用支付宝接口，生成二维码后，展示给用户，由用户扫描二维码完成订单支付。
     * @param orderId
     * @param request
     * @param response
     */
    @PostMapping(value="/precreate")
    public void precreate(String orderId, HttpServletRequest request, HttpServletResponse response)throws Exception{
        //TODO 根据订单Id 查询"订单信息".

        //TODO 根据订单信息,构造与支付请求结构体信息.
        AlipayTradePrecreateRequestBuilder builder = wrapAlipayTradePrecreateRequestBuilderDemo();
        //支付.
        f2FPayService.precreatePay(builder,request,response);
    }

    private AlipayTradePrecreateRequestBuilder wrapAlipayTradePrecreateRequestBuilderDemo(){
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        String outTradeNo = UUID.randomUUID().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“喜士多（浦东店）消费”
        String subject = "测试订单";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = "购买商品2件共20.05元";

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = "0.01";

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";


        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<>();
        GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "全麦小面包", 1, 1);
        goodsDetailList.add(goods1);
        GoodsDetail goods2 = GoodsDetail.newInstance("goods_id002", "黑人牙刷", 1, 2);
        goodsDetailList.add(goods2);

        // 支付超时，线下扫码交易定义为5分钟
        String timeoutExpress = "5m";

        AlipayTradePrecreateRequestBuilder builder =new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject)
                .setTotalAmount(totalAmount)
                .setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount)
                .setSellerId(sellerId)
                .setBody(body)
                .setOperatorId(operatorId)
                .setStoreId(storeId)
                .setTimeoutExpress(timeoutExpress)
                //支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
//                .setNotifyUrl(aliPayProperties.getNotifyUrl())
                .setGoodsDetailList(goodsDetailList);

        return builder;
    }





    /**
     * 当面付-退款.
     * 描述:
     *     当交易发生之后一段时间内，由于买家或者卖家的原因需要退款时，卖家可以通过退款接口将支付款退还给买家，
     *     支付宝将在收到退款请求并且验证成功之后，按照退款规则将支付款按原路退到买家帐号上。
     *     交易超过约定时间（签约时设置的可退款时间）的订单无法进行退款.
     *     支付宝退款支持单笔交易分多次退款，多次退款需要提交原支付订单的商户订单号和设置不同的退款单号。
     *     一笔退款失败后重新提交，要采用原来的退款单号。总退款金额不能超过用户实际支付金额.
     * @param orderNo
     * @return
     */
    @PostMapping(value="/refund")
    public ServerResponse refund(String orderNo){

        AlipayTradeRefundRequestBuilder builder = new AlipayTradeRefundRequestBuilder();
        ServerResponse result = f2FPayService.tradeRefund(builder);
        return result;

    }

    private AlipayTradeRefundRequestBuilder wrapAlipayTradeRefundRequestBuilderDemo(String orderNo){


        AlipayTradeRefundRequestBuilder builder = new AlipayTradeRefundRequestBuilder()
                .setOutTradeNo(orderNo)                   // (必填) 外部订单号，需要退款交易的商户外部订单号
                .setRefundAmount("0.01")                  // (必填) 退款金额，该金额必须小于等于订单的支付金额，单位为元
                .setRefundReason("当面付退款测试")        // (必填) 退款原因，可以说明用户退款原因，方便为商家后台提供统计
                .setOutRequestNo(String.valueOf(System.nanoTime()))           // (可选，需要支持重复退货时必填) 商户退款请求号，相同支付宝交易号下的不同退款请求号对应同一笔交易的不同退款申请，
                                                         // 对于相同支付宝交易号下多笔相同商户退款请求号的退款交易，支付宝只会进行一次退款

                .setStoreId("A1");                    // (必填) 商户门店编号，退款情况下可以为商家后台提供退款权限判定和统计等作用，详询支付宝技术支持
        return builder;

    }




}
