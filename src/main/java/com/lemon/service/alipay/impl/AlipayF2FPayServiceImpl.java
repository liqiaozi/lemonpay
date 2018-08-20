package com.lemon.service.alipay.impl;

import com.alipay.api.response.AlipayTradePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.demo.trade.model.builder.AlipayTradePayRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradeRefundRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPayResult;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.model.result.AlipayF2FRefundResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.google.gson.Gson;
import com.google.zxing.WriterException;
import com.lemon.common.ServerResponse;
import com.lemon.service.alipay.AlipayF2FPayService;
import com.lemon.utils.PayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @ClassName: F2FPayService
 * @Description: 当面付service实现类.
 * API地址:
 * @Author: 李雪飞
 * @Date: 2018/8/9 20:22
 * @Vserion 1.0
 **/
@Service
@Slf4j
public class AlipayF2FPayServiceImpl implements AlipayF2FPayService {
    @Autowired
    private AlipayTradeService alipayTradeService;


    /**
     * 当面付-条码支付 API: https://docs.open.alipay.com/api_1/alipay.trade.pay
     *
     * @param authCode
     * @param builder
     * @return
     */
    @Override
    public ServerResponse barCodePay(String authCode, AlipayTradePayRequestBuilder builder) {
        builder.setAuthCode(authCode);
        // 同步返回支付结果
        AlipayF2FPayResult f2FPayResult = alipayTradeService.tradePay(builder);
        // 注意：一定要处理支付的结果，因为不是每次支付都一定会成功，可能会失败
        AlipayTradePayResponse response = f2FPayResult.getResponse();
        switch (f2FPayResult.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝支付成功: )");
                return ServerResponse.ok(response.getBody());

            case FAILED:
                log.error("支付宝支付失败!,支付请求信息:{},异常响应信息:{}", new Gson().toJson(builder), new Gson().toJson(response));
                return ServerResponse.error(Integer.valueOf(response.getCode()), "支付宝支付失败!");

            case UNKNOWN:
                log.error("系统异常，订单状态未知!支付请求信息:{},异常响应信息:{}", new Gson().toJson(builder), new Gson().toJson(response));
                return ServerResponse.error(Integer.valueOf(response.getCode()), "系统异常，订单状态未知!");

            default:
                log.error("不支持的交易状态，交易返回异常!支付请求信息:{},异常响应信息:{}", new Gson().toJson(builder), new Gson().toJson(response));
                return ServerResponse.error(Integer.valueOf(response.getCode()), "不支持的交易状态，交易返回异常!");
        }
    }

    /**
     * 当面付-扫码支付
     * API: https://docs.open.alipay.com/api_1/alipay.trade.precreate
     *
     * @param builder
     * @param request
     * @param response
     */
    @Override
    public void precreatePay(AlipayTradePrecreateRequestBuilder builder, HttpServletRequest request, HttpServletResponse response) throws IOException, WriterException {
        AlipayF2FPrecreateResult precreateResult = alipayTradeService.tradePrecreate(builder);
        String qrCodeUrl = null;

        //响应结果.
        AlipayTradePrecreateResponse precreateResponse = precreateResult.getResponse();
        switch (precreateResult.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功!");

                File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "static/images/");
                if (!file.exists()) {
                    file.mkdirs();
                }

//                String absolutePath = file.getAbsolutePath();
//                String fileName = String.format("%sqr-%s.png", File.separator, precreateResponse.getOutTradeNo());
//                String filePath = new StringBuilder(absolutePath).append(fileName).toString();
//
//                // 这里只是演示将图片写到服务器中，实际可以返回二维码让前端去生成
//                String basePath = request.getScheme()+ "://"+request.getServerName()+":"+ request.getServerPort()+request.getContextPath()+"/";
//                qrCodeUrl = basePath + fileName;
//                response.getWriter().write("<img src=\"" + qrCodeUrl + "\" />");
//                ZxingUtils.getQRCodeImge(precreateResponse.getQrCode(), 256, filePath);

                BufferedImage image = PayUtil.genQRCodeImage(precreateResponse.getQrCode());
                response.setContentType("image/jpeg");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setIntHeader("Expires", -1);
                ImageIO.write(image, "JPEG", response.getOutputStream());
                break;

            case FAILED:
                log.error("支付宝预下单失败!");
                log.error("支付宝异常响应信息为:{}", new Gson().toJson(precreateResponse));
                break;

            case UNKNOWN:
                log.error("系统异常,预下单状态未知!");
                log.error("支付宝异常响应信息为:{}", new Gson().toJson(precreateResponse));
                break;

            default:
                log.error("不支持的交易状态,交易返回异常!");
                log.error("支付宝异常响应信息为:{}", new Gson().toJson(precreateResponse));
                break;
        }
    }


    /**
     * 当面付-退款
     * API: https://docs.open.alipay.com/api_1/alipay.trade.refund
     *
     * @param builder
     * @return
     */
    @Override
    public ServerResponse tradeRefund(AlipayTradeRefundRequestBuilder builder) {

        AlipayF2FRefundResult result = alipayTradeService.tradeRefund(builder);
        AlipayTradeRefundResponse response = result.getResponse();
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("退款成功");
                return ServerResponse.ok(response);

            case FAILED:
                log.error("支付宝退款失败!退款请求信息:{},异常响应信息:{}", new Gson().toJson(builder), new Gson().toJson(response));
                return ServerResponse.error(Integer.valueOf(response.getCode()), "支付宝退款失败!");

            case UNKNOWN:
                log.error("系统异常,订单退款状态未知!退款请求信息:{},异常响应信息:{}", new Gson().toJson(builder), new Gson().toJson(response));
                return ServerResponse.error(Integer.valueOf(response.getCode()), "系统异常,订单退款状态未知!");

            default:
                log.error("不支持的交易状态,交易返回异常!退款请求信息:{},异常响应信息:{}", new Gson().toJson(builder), new Gson().toJson(response));
                return ServerResponse.error(Integer.valueOf(response.getCode()), "不支持的交易状态,交易返回异常!");
        }

    }


}
