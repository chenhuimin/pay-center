package com.laanto.it.paycenter.rest;

import com.laanto.it.paycenter.constant.MediaTypes;
import com.laanto.it.paycenter.constant.Payment;
import com.laanto.it.paycenter.constant.UpdateType;
import com.laanto.it.paycenter.domain.PayRequest;
import com.laanto.it.paycenter.pay.unionpay.config.UnionpayConfig;
import com.laanto.it.paycenter.pay.unionpay.util.UnionpayUtils;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
@RequestMapping("/rest/pay/union")
public class UnionPayRestController {
    private static final Logger logger = LoggerFactory.getLogger(UnionPayRestController.class);

    @Autowired
    private UnionpayConfig unionpayConfig;

    @Autowired
    private PayRequestService payRequestService;

    @Autowired
    private Validator validator;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 银联支付
     *
     * @param payRequestVo
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaTypes.JSON_UTF_8, produces = MediaTypes.JSON_UTF_8)
    public Map<String, String> unionPay(@RequestBody PayRequestVo payRequestVo, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        Map<String, String> constraintViolations = BeanValidators.validate(validator, payRequestVo);
        if (constraintViolations != null && !constraintViolations.isEmpty()) {
            String constraintViolationsMsg = String.format("支付参数错误：%s", constraintViolations.toString());
            resultMap.put("errmsg", constraintViolationsMsg);
            return resultMap;
        } else {
            if (StringUtils.isBlank(payRequestVo.getReturnUrl())) {
                String returnUrlErrorMsg = "银联支付时，页面跳转同步通知页面路径不能为空";
                resultMap.put("errmsg", returnUrlErrorMsg);
                return resultMap;
            }
            Payment payment = Payment.fromValue(payRequestVo.getPayment());
            if (payment == null || !payment.equals(Payment.UNION_PAY)) {
                String paymentErrorMsg = String.format("支付方式参数错误，银联支付payment的取值只能是2，当前payment=%d", payRequestVo.getPayment());
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
     * 银联支付完成之后页面跳转同步通知页面路径
     *
     * @return
     */
    @RequestMapping(value = "/return_url", method = RequestMethod.POST)
    public void createReturnUrl(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("进入银联支付同步通知页面");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/html; charset=utf-8");
        Map<String, String> params = getPayResultParams(request);
        String signature = request.getParameter(UnionpayConfig.KEY_SIGNATURE);
        Map<String, Object> checkPayResult = checkPayResult(params, signature);
        boolean payResult = (Boolean) checkPayResult.get("success");
        String errorMsg = (String) checkPayResult.get("errorMsg");
        String out_trade_no = params.get("orderNumber");
        String trade_no = params.get("qid");
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
                returnParams.put("transaction_id", trade_no);
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
     * 银联支付完成之后服务器异步通知页面路径
     *
     * @return
     */
    @RequestMapping(value = "/notify_url", method = RequestMethod.POST)
    public void createNotifyUrl(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("进入银联支付异步通知页面");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/html; charset=utf-8");
        Map<String, String> params = getPayResultParams(request);
        String signature = request.getParameter(UnionpayConfig.KEY_SIGNATURE);
        Map<String, Object> checkPayResult = checkPayResult(params, signature);
        boolean payResult = (Boolean) checkPayResult.get("success");
        String errorMsg = (String) checkPayResult.get("errorMsg");
        String out_trade_no = params.get("orderNumber");
        String trade_no = params.get("qid");
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
                returnParams.put("transaction_id", trade_no);
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
            logger.error("根据订单号：{},查找不到支付请求", out_trade_no);
        }
        //如果从用户处返回的结果为success表示用户已经成功处理。我们就返回给支付宝成功，让支付宝不在重发通知
        if (payResult && "success".equalsIgnoreCase(userReturnResult)) {
            //程序执行完后必须打印输出“success”（不包含引号）。如果商户反馈给支付宝的字符不是success这7个字符，支付宝服务器会不断重发通知，直到超过24小时22分钟。
            logger.debug("支付及用户回调成功，返回给银联的值：success，状态码：200");
            try {
                response.getWriter().write("success");
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.debug("支付及用户回调失败，返回给银联的值：fail，状态码：500");
            try {
                response.setStatus(500);
                response.getWriter().write("fail");
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Map<String, Object> checkPayResult(Map<String, String> params, String signature) {
        Map<String, Object> result = new HashMap<>();
        boolean signatureCheck = UnionpayUtils.checkSign(params, signature, unionpayConfig);
        String respCode = params.get("respCode");
        boolean success = false;
        String errorMsg = null;
        if (signatureCheck) {// 签名验证成功
            if ("00".equals(respCode)) {
                success = true;
            } else {
                errorMsg = "银联支付失败。原因：银联支付返回状态信息： " + params.get("respMsg");
                logger.info(errorMsg);
            }
        } else {// 验证失败
            errorMsg = "银联支付失败。原因：签名验证失败。(待签名字符串为:" + UnionpayUtils.joinMapValue(params, '&') + ")";
            logger.info(errorMsg);
        }
        result.put("success", success);
        if (StringUtils.isNotBlank(errorMsg)) {
            result.put("errorMsg", errorMsg);
        }
        return result;
    }

    private Map<String, String> getPayResultParams(HttpServletRequest request) {
        Map<String, String> params = new TreeMap<String, String>();
        for (String name : unionpayConfig.getNotifyVo()) {
            String[] values = request.getParameterValues(name);
            if (values != null && values.length > 0) {
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }
        }
        return params;
    }

    private String createPayForm(PayRequest entity, HttpServletRequest request) {
        Map<String, String> sParaTemp = new TreeMap<String, String>();
        sParaTemp.put("version", unionpayConfig.getVersion().trim());// 协议版本
        sParaTemp.put("charset", unionpayConfig.getCharset().trim());// 字符编码
        sParaTemp.put("transType", unionpayConfig.getTransTypeConsume().trim());// 交易类型
        sParaTemp.put("merId", unionpayConfig.getMerId().trim());// 商户号
        sParaTemp.put("merAbbr", unionpayConfig.getMerAbbr().trim());// 商户简称
        sParaTemp.put("commodityName", entity.getCommodityName().trim());// 商品名称
        sParaTemp.put("orderNumber", entity.getOutTradeNo().trim()); // 订单号（需要商户自己生成）
        // 把充值金额转为 以分为单位
        long orderAmountValue = (entity.getTotalFee().multiply(new BigDecimal(100))).longValue();
        sParaTemp.put("orderAmount", String.valueOf(orderAmountValue));// 交易金额 单位：分
        sParaTemp.put("orderCurrency", unionpayConfig.getOrderCurrency().trim());// 交易币种
        sParaTemp.put("orderTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));// 交易时间
        String frontEndUrl = WebUtil.getAbsoluteUrl(request, unionpayConfig.getPayFrontEndUrl().trim());
        sParaTemp.put("frontEndUrl", frontEndUrl);//前台通知地址
        String backEndUrl = WebUtil.getAbsoluteUrl(request, unionpayConfig.getPayBackEndUrl().trim());
        sParaTemp.put("backEndUrl", backEndUrl);// 后台通知地址
        // sParaTemp.put("merReserved", UnionpayConfig.merReserved); // 商户保留域
        logger.debug("银联支付参数集合：" + sParaTemp);
        String payForm = UnionpayUtils.createPayForm(sParaTemp, unionpayConfig);
        return payForm;
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
