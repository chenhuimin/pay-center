package com.laanto.it.paycenter.rest;

import com.laanto.it.paycenter.constant.MediaTypes;
import com.laanto.it.paycenter.constant.Payment;
import com.laanto.it.paycenter.constant.UpdateType;
import com.laanto.it.paycenter.domain.PayRequest;
import com.laanto.it.paycenter.pay.alipay.config.AlipayConfig;
import com.laanto.it.paycenter.pay.alipay.utils.AlipayUtils;
import com.laanto.it.paycenter.service.PayRequestService;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/rest/pay/ali")
public class AliPayRestController {
    private static final Logger logger = LoggerFactory.getLogger(AliPayRestController.class);

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private PayRequestService payRequestService;

    @Autowired
    private Validator validator;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 支付宝支付
     *
     * @param payRequestVo
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaTypes.JSON_UTF_8, produces = MediaTypes.JSON_UTF_8)
    public Map<String, String> aliPay(@RequestBody PayRequestVo payRequestVo, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        Map<String, String> constraintViolations = BeanValidators.validate(validator, payRequestVo);
        if (constraintViolations != null && !constraintViolations.isEmpty()) {
            String constraintViolationsMsg = String.format("支付参数错误：%s", constraintViolations.toString());
            resultMap.put("errmsg", constraintViolationsMsg);
            return resultMap;
        } else {
            if (StringUtils.isBlank(payRequestVo.getReturnUrl())) {
                String returnUrlErrorMsg = "支付宝支付时，页面跳转同步通知页面路径不能为空";
                resultMap.put("errmsg", returnUrlErrorMsg);
                return resultMap;
            }
            Payment payment = Payment.fromValue(payRequestVo.getPayment());
            if (payment == null || !(payment.equals(Payment.ALI_PAY_PC) || payment.equals(Payment.ALI_PAY_WAP))) {
                String paymentErrorMsg = String.format("支付方式参数错误，支付宝支付payment的取值只能是0,1，当前payment=%d", payRequestVo.getPayment());
                resultMap.put("errmsg", paymentErrorMsg);
                return resultMap;
            }
            PayRequest payRequest = new PayRequest();
            payRequest.setCommodityName(payRequestVo.getCommodityName());
            payRequest.setOutTradeNo(payRequestVo.getOutTradeNo());
            payRequest.setNotifyUrl(payRequestVo.getNotifyUrl());
            payRequest.setReturnUrl(payRequestVo.getReturnUrl());
            payRequest.setPayment(payment);
            payRequest.setTotalFee(payRequestVo.getTotalFee());
            payRequestService.save(payRequest);
            String payForm = createPayForm(payRequest, request);
            resultMap.put("payForm", payForm);
            return resultMap;
        }
    }

    /**
     * 支付宝支付完成之后页面跳转同步通知页面路径
     *
     * @return
     */
    @RequestMapping(value = "/return_url", method = RequestMethod.GET)
    public void createReturnUrl(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("进入支付宝支付同步通知页面");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/html; charset=utf-8");
        Map<String, String> params = getPayResultParams(request);
        Map<String, Object> checkPayResult = checkPayResult(params);
        boolean payResult = (Boolean) checkPayResult.get("success");
        String errorMsg = (String) checkPayResult.get("errorMsg");
        String out_trade_no = params.get("out_trade_no");
        String trade_no = params.get("trade_no");
        PayRequest payRequest = payRequestService.findByOutTradeNo(out_trade_no);
        if (payRequest != null) {
            if (payRequest.getSuccess() == null) {
                payRequest.setSuccess(payResult);
                payRequest.setTradeNo(trade_no);
                payRequest.setUpdateTime(new Date());
                payRequest.setUpdateBy(UpdateType.UPDATE_SYNC);
                if (StringUtils.isNotBlank(errorMsg)) {
                    payRequest.setErrorMsg(errorMsg);
                }
                if (payRequest.getResponseParams() == null && params != null) {
                    payRequest.setResponseParams(params.toString());
                }
                payRequestService.save(payRequest);
            }
            if (StringUtils.isNotBlank(payRequest.getReturnUrl())) {
                Map<String, String> returnParams = new HashMap<>();
                returnParams.put("success", Boolean.toString(payResult));
                returnParams.put("out_trade_no", out_trade_no);
                String redirectUrl = createRedirectUrl(payRequest.getReturnUrl(), returnParams);
                try {
                    response.sendRedirect(redirectUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                logger.error("在支付请求中查询不到同步通知页面路径");
            }
        } else {
            logger.error("根据订单号：{}，查找不到支付请求", out_trade_no);
        }
    }

    /**
     * 支付宝支付完成之后服务器异步通知页面路径
     *
     * @return
     */
    @RequestMapping(value = "/notify_url", method = RequestMethod.POST)
    public void createNotifyUrl(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("进入支付宝异步通知页面");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/html; charset=utf-8");
        // 获取支付宝POST过来反馈信息
        Map<String, String> params = getPayResultParams(request);
        Map<String, Object> checkPayResult = checkPayResult(params);
        boolean payResult = (Boolean) checkPayResult.get("success");
        String errorMsg = (String) checkPayResult.get("errorMsg");
        String out_trade_no = params.get("out_trade_no");
        String trade_no = params.get("trade_no");
        String userReturnResult = "";
        PayRequest payRequest = payRequestService.findByOutTradeNo(out_trade_no);
        if (payRequest != null) {
            if (payRequest.getSuccess() == null) {
                payRequest.setSuccess(payResult);
                payRequest.setTradeNo(trade_no);
                payRequest.setUpdateTime(new Date());
                payRequest.setUpdateBy(UpdateType.UPDATE_ASYNC);
                if (StringUtils.isNotBlank(errorMsg)) {
                    payRequest.setErrorMsg(errorMsg);
                }
                if (payRequest.getResponseParams() == null && params != null) {
                    payRequest.setResponseParams(params.toString());
                }
                payRequestService.save(payRequest);
            }
            String notifyUrl = payRequest.getNotifyUrl();
            if (StringUtils.isNotBlank(notifyUrl)) {
                Map<String, String> returnParams = new HashMap<>();
                returnParams.put("success", Boolean.toString(payResult));
                returnParams.put("out_trade_no", out_trade_no);
                String json = RestTemplateUtils.getJsonString(returnParams);
                HttpEntity<String> entity = new HttpEntity<String>(json, RestTemplateUtils.getHeaderWithApplicationJsonAndUTF8());
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
        //如果从用户处返回的结果为success表示用户已经成功处理。我们就返回给支付宝成功，让支付宝不在重发通知
        if (payResult && "success".equalsIgnoreCase(userReturnResult)) {
            //程序执行完后必须打印输出“success”（不包含引号）。如果商户反馈给支付宝的字符不是success这7个字符，支付宝服务器会不断重发通知，直到超过24小时22分钟。
            logger.debug("支付及用户回调成功，返回给支付宝的值：success，状态码：200");
            try {
                response.getWriter().write("success");
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private String createPayForm(PayRequest entity, HttpServletRequest request) {
        Map<String, String> parameters = new HashMap<String, String>();
        if (entity.getPayment().equals(Payment.ALI_PAY_PC)) {
            parameters.put("service", alipayConfig.getService_pc().trim());
        } else {
            parameters.put("service", alipayConfig.getService_wap().trim());
        }
        parameters.put("partner", alipayConfig.getPartner().trim());
        parameters.put("_input_charset", alipayConfig.getInput_charset().trim());
        String return_url = WebUtil.getAbsoluteUrl(request, alipayConfig.getReturn_url().trim());
        parameters.put("return_url", return_url);
        String notify_url = WebUtil.getAbsoluteUrl(request, alipayConfig.getNotify_url().trim());
        parameters.put("notify_url", notify_url);
        parameters.put("out_trade_no", entity.getOutTradeNo().trim());
        parameters.put("subject", entity.getCommodityName().trim());
        parameters.put("total_fee", entity.getTotalFee().toPlainString());
        parameters.put("seller_id", alipayConfig.getSellerId()); // 卖家支付宝用户号
        parameters.put("payment_type", alipayConfig.getPayment_type().trim());
        //parameters.put("seller_email", alipayConfig.getSeller_email().trim());
        logger.debug("支付宝支付参数集合：" + parameters);
        String payForm = AlipayUtils.createPayForm(parameters, alipayConfig);
        return payForm;
    }

    private Map<String, Object> checkPayResult(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        boolean signatureCheck = AlipayUtils.verify(params, alipayConfig);
        String trade_status = params.get("trade_status");
        boolean success = false;
        String errorMsg = null;
        if (signatureCheck) {// 签名验证成功
            if ("TRADE_FINISHED".equalsIgnoreCase(trade_status) || "TRADE_SUCCESS".equals(trade_status)) {
                success = true;
            } else {
                errorMsg = "支付宝支付失败。原因：支付宝支付返回状态： " + trade_status;
                logger.info(errorMsg);
            }
        } else {// 验证失败
            errorMsg = "支付宝支付失败。原因：签名验证失败。(待签名字符串为:" + AlipayUtils.createLinkString(params) + ") ";
            logger.info(errorMsg);
        }
        result.put("success", success);
        if (StringUtils.isNotBlank(errorMsg)) {
            result.put("errorMsg", errorMsg);
        }
        return result;
    }

    private Map<String, String> getPayResultParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        return params;
    }

    private String createRedirectUrl(String baseUrl, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(baseUrl);
        if (params != null && !params.isEmpty()) {
            if (baseUrl.contains("?")) {
                sb.append("&");
            } else {
                sb.append("?");
            }
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                if (iterator.hasNext()) {
                    sb.append("&");
                }
            }
        }
        return sb.toString();
    }
}
