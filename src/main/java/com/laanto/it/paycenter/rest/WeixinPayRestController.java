package com.laanto.it.paycenter.rest;

import com.laanto.it.paycenter.constant.MediaTypes;
import com.laanto.it.paycenter.constant.Payment;
import com.laanto.it.paycenter.constant.UpdateType;
import com.laanto.it.paycenter.domain.PayRequest;
import com.laanto.it.paycenter.pay.weixinpay.config.WeixinpayConfig;
import com.laanto.it.paycenter.pay.weixinpay.constant.WeiXinApiUrl;
import com.laanto.it.paycenter.pay.weixinpay.pojo.UnifiedOrder;
import com.laanto.it.paycenter.pay.weixinpay.pojo.WeiXinPayResult;
import com.laanto.it.paycenter.pay.weixinpay.utils.MD5;
import com.laanto.it.paycenter.pay.weixinpay.utils.WeixinCommonUtil;
import com.laanto.it.paycenter.pay.weixinpay.utils.WeixinPayUtil;
import com.laanto.it.paycenter.pay.weixinpay.utils.WexinSignUtil;
import com.laanto.it.paycenter.service.PayRequestService;
import com.laanto.it.paycenter.utils.BeanUtil;
import com.laanto.it.paycenter.utils.BeanValidators;
import com.laanto.it.paycenter.utils.RestTemplateUtils;
import com.laanto.it.paycenter.utils.WebUtil;
import com.laanto.it.paycenter.vo.PayRequestVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

/**
 * 微信支付rest api
 *
 * @author user
 */
@RestController
@RequestMapping("/rest/pay/weixin")
public class WeixinPayRestController {
    private static final Logger logger = LoggerFactory.getLogger(WeixinPayRestController.class);

    @Autowired
    private WeixinpayConfig weixinpayConfig;

    @Autowired
    private PayRequestService payRequestService;

    @Autowired
    private Validator validator;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaTypes.JSON_UTF_8, produces = MediaTypes.JSON_UTF_8)
    public Map<String, Object> weixinPay(@RequestBody PayRequestVo payRequestVo, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, String> constraintViolations = BeanValidators.validate(validator, payRequestVo);
        if (constraintViolations != null && !constraintViolations.isEmpty()) {
            String constraintViolationsMsg = String.format("支付参数错误:%s", constraintViolations.toString());
            resultMap.put("errmsg", constraintViolationsMsg);
            return resultMap;
        } else {
            if (StringUtils.isBlank(payRequestVo.getOpenid())) {
                String openidErrorMsg = "微信公众号支付且trade_type=JSAPI时，openid不能为空";
                resultMap.put("errmsg", openidErrorMsg);
                return resultMap;
            }
            if (StringUtils.isBlank(payRequestVo.getWxPayPage())) {
                String wxPayPageErrorMsg = "微信公众号支付时，需要传入支付当前页的URL";
                resultMap.put("errmsg", wxPayPageErrorMsg);
                return resultMap;
            }
            Payment payment = Payment.fromValue(payRequestVo.getPayment());
            if (payment == null || !payment.equals(Payment.WEIXIN_PAY)) {
                String paymentErrorMsg = String.format("支付方式参数错误,微信支付payment的取值只能是3，当前payment=%d", payRequestVo.getPayment());
                resultMap.put("errmsg", paymentErrorMsg);
                return resultMap;
            }
            PayRequest payRequest = new PayRequest();
            payRequest.setCommodityName(payRequestVo.getCommodityName());
            payRequest.setOutTradeNo(payRequestVo.getOutTradeNo());
            payRequest.setNotifyUrl(payRequestVo.getNotifyUrl());
            payRequest.setPayment(payment);
            payRequest.setTotalFee(payRequestVo.getTotalFee());
            payRequest.setOpenid(payRequestVo.getOpenid());
            payRequest.setWxPayPage(payRequestVo.getWxPayPage());
            payRequestService.save(payRequest);
            UnifiedOrder unifiedOrder = createUnifiedOrder(payRequest, request);
            Map<String, Object> unifiedOrderMap = WeixinPayUtil.paraFilter(BeanUtil.transferBean2Map(unifiedOrder));
            String stringForSign = WeixinPayUtil.createLinkString(unifiedOrderMap);
            unifiedOrder.setSign(MD5.signWeixinPay(stringForSign, weixinpayConfig.getPartnerKey(), "utf-8").toUpperCase());
            String unifiedOrderXml = WeixinCommonUtil.beanToXml(unifiedOrder, UnifiedOrder.class);
            logger.debug("微信支付统一下单请求参数xml={}", unifiedOrderXml);
            Map<String, String> unifiedOrderResult = WeixinCommonUtil.processHttpsRequestAndReturnMap(WeiXinApiUrl.PAY_UNIFIEDORDER, "POST", unifiedOrderXml);
            if (unifiedOrderResult != null && !unifiedOrderResult.isEmpty()) {
                //resultMap.put("unifiedOrderResult", unifiedOrderResult);
                // 设置JSAPI方式微信支付的参数
                if ("SUCCESS".equalsIgnoreCase(unifiedOrderResult.get("return_code")) && "SUCCESS".equalsIgnoreCase(unifiedOrderResult.get("result_code"))) {
                    String prepayId = unifiedOrderResult.get("prepay_id");
                    // B.设置JSAPI WXPay微信支付的参数
                    if (StringUtils.isNotBlank(prepayId)) {
                        String packageParam = "prepay_id=" + prepayId;
                        Map<String, String> wxPayConfig = WexinSignUtil.signJSAPIWXPay(weixinpayConfig.getAppid(), packageParam, "MD5", weixinpayConfig.getPartnerKey(), "UTF-8");
                        resultMap.put("wxPayConfig", wxPayConfig);
                        logger.debug("微信支付签名参数集合：[appId ={},timeStamp={},nonceStr={},package={},signType={},paySign={} ] ",
                                wxPayConfig.get("appId"), wxPayConfig.get("timeStamp"), wxPayConfig.get("nonceStr"),
                                wxPayConfig.get("packageParam"), wxPayConfig.get("signType"), wxPayConfig.get("paySign"));
                    }
                } else {
                    String unifiedOrderErrorMsg = "微信支付统一下单失败。返回结果为：" + unifiedOrderResult.toString();
                    resultMap.put("errmsg", unifiedOrderErrorMsg);
                }
            } else {
                String unifiedOrderErrorMsg = "微信支付统一下单失败，返回结果为空";
                resultMap.put("errmsg", unifiedOrderErrorMsg);
            }
            return resultMap;
        }
    }

    /**
     * 微信支付后台通知处理函数
     *
     * @return
     */

    @RequestMapping(value = "/notify_url", method = RequestMethod.POST, produces = MediaTypes.APPLICATION_XML_UTF_8)
    public String createNotifyUrl(HttpServletRequest request) {
        logger.debug("进入微信支付异步通知页面");
        // 获取微信支付POST过来反馈信息
        WeiXinPayResult payResultMap = new WeiXinPayResult("FAIL", "FAIL");
        boolean payResult = false;
        String errorMsg = null;
        String userReturnResult = "";
        try {
            Map<String, Object> wxReturnParams = WeixinCommonUtil.parseXml(request.getInputStream());
            logger.debug("微信支付成功之后的返回参数集合："+wxReturnParams.toString());
            Map<String, Object> checkPayResult = checkPayResult(wxReturnParams);
            payResult = (Boolean) checkPayResult.get("success");
            errorMsg = (String) checkPayResult.get("errorMsg");
            String out_trade_no = (String) wxReturnParams.get("out_trade_no");
            String transaction_id = (String) wxReturnParams.get("transaction_id");
            logger.debug("微信支付获取的交易流水号:transaction_id={}", transaction_id);
            PayRequest payRequest = payRequestService.findByOutTradeNo(out_trade_no);
            if (payRequest != null) {
                if (payRequest.getSuccess() == null) {
                    payRequest.setSuccess(payResult);
                    payRequest.setTradeNo(transaction_id);
                    payRequest.setUpdateTime(new Date());
                    payRequest.setUpdateBy(UpdateType.UPDATE_ASYNC);
                    if (StringUtils.isNotBlank(errorMsg)) {
                        payRequest.setErrorMsg(errorMsg);
                    }
                    if (payRequest.getResponseParams() == null && wxReturnParams != null) {
                        payRequest.setResponseParams(wxReturnParams.toString());
                    }
                    payRequestService.save(payRequest);
                }
                String notifyUrl = payRequest.getNotifyUrl();
                if (StringUtils.isNotBlank(notifyUrl)) {
                    Map<String, String> returnParams = new HashMap<>();
                    returnParams.put("success", Boolean.toString(payResult));
                    returnParams.put("out_trade_no", out_trade_no);
                    returnParams.put("transaction_id", transaction_id);
                    String json = RestTemplateUtils.getJsonString(returnParams);
                    HttpEntity<String> entity = new HttpEntity<>(json, RestTemplateUtils.getHeaderWithApplicationJsonAndUTF8());
                    ResponseEntity<String> responseEntity = restTemplate.postForEntity(notifyUrl, entity, String.class);
                    if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                        userReturnResult = responseEntity.getBody();
                        logger.debug("用户回调的结果userReturnResult={}", userReturnResult);
                    }
                } else {
                    logger.error("在支付请求中查询不到完成之后异步通知页面路径");
                }
            } else {
                logger.error("根据订单号：{},查找不到支付请求。", out_trade_no);
            }
        } catch (IOException e) {
            logger.error("微信支付异步回调获取服务器端输入流错误", e);
        }
        //如果从用户处返回的结果为success表示用户已经成功处理。我们就返回给微信支付成功，让微信支付不在重发通知
        if (payResult && "success".equalsIgnoreCase(userReturnResult)) {
            //程序执行完后必须打印输出“success”（不包含引号）。如果商户反馈给支付宝的字符不是success这7个字符，支付宝服务器会不断重发通知，直到超过24小时22分钟。
            logger.debug("支付及用户回调成功，返回给微信的值return_code：SUCCESS，return_msg：OK，状态码：200");
            payResultMap.setReturn_code("SUCCESS");
            payResultMap.setReturn_msg("OK");
        }
        String xml = WeixinCommonUtil.beanToXml(payResultMap, WeiXinPayResult.class);
        return xml;
    }

    private Map<String, Object> checkPayResult(Map<String, Object> wxReturnParams) {
        Map<String, Object> result = new HashMap<>();
        boolean success = false;
        String errorMsg = null;
        Map<String, Object> mapForSign = new HashMap<String, Object>();
        Iterator<Map.Entry<String, Object>> iterator = wxReturnParams.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            String value = (String) entry.getValue();
            if (!"sign".equalsIgnoreCase(key) && StringUtils.isNotBlank(value)) {
                mapForSign.put(entry.getKey(), entry.getValue());
            }
        }
        String stringForSign = WeixinPayUtil.createLinkString(mapForSign);
        String wxReturnSign = (String) wxReturnParams.get("sign");
        if (StringUtils.isNotBlank(wxReturnSign) && MD5.verifyWeixinPaySign(stringForSign, weixinpayConfig.getPartnerKey(), "utf-8", wxReturnSign)) {
            if ("SUCCESS".equalsIgnoreCase((String) wxReturnParams.get("return_code")) && "SUCCESS".equalsIgnoreCase((String) wxReturnParams.get("result_code"))) {
                success = true;
            } else {
                errorMsg = "微信支付失败。原因：微信支付返回信息： " + wxReturnParams.get("return_msg").toString();
                logger.info(errorMsg);
            }
        } else {
            errorMsg = "微信支付失败。原因：签名验证失败。(待签名字符串为:" + stringForSign + ") ";
            logger.info(errorMsg);
        }
        result.put("success", success);
        if (StringUtils.isNotBlank(errorMsg)) {
            result.put("errorMsg", errorMsg);
        }
        return result;
    }

    private UnifiedOrder createUnifiedOrder(PayRequest payRequest, HttpServletRequest request) {
        UnifiedOrder unifiedOrder = new UnifiedOrder();
        unifiedOrder.setAppid(weixinpayConfig.getAppid().trim());
        unifiedOrder.setMch_id(weixinpayConfig.getMchId().trim());
        unifiedOrder.setNonce_str(UUID.randomUUID().toString().replace("-", ""));
        unifiedOrder.setBody(payRequest.getCommodityName().trim());
        unifiedOrder.setOut_trade_no(payRequest.getOutTradeNo().trim());
        long orderAmountValue = (payRequest.getTotalFee().multiply(new BigDecimal(100))).longValue();
        unifiedOrder.setTotal_fee(String.valueOf(orderAmountValue));
        unifiedOrder.setSpbill_create_ip(WebUtil.getUserIp(request));
        String notify_url = WebUtil.getAbsoluteUrl(request, weixinpayConfig.getNotifyUrl());
        unifiedOrder.setNotify_url(notify_url);
        unifiedOrder.setTrade_type(weixinpayConfig.getTradeType().trim());
        unifiedOrder.setOpenid(payRequest.getOpenid().trim());
        return unifiedOrder;
    }
}
