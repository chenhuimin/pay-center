package com.laanto.it.paycenter.rest;

import com.laanto.it.paycenter.constant.MediaTypes;
import com.laanto.it.paycenter.constant.Payment;
import com.laanto.it.paycenter.constant.RefundType;
import com.laanto.it.paycenter.constant.UpdateType;
import com.laanto.it.paycenter.domain.PayRequest;
import com.laanto.it.paycenter.domain.RefundRequest;
import com.laanto.it.paycenter.pay.weixinpay.config.WeixinpayConfig;
import com.laanto.it.paycenter.pay.weixinpay.constant.WeiXinApiUrl;
import com.laanto.it.paycenter.pay.weixinpay.pojo.QueryRefund;
import com.laanto.it.paycenter.pay.weixinpay.pojo.Refund;
import com.laanto.it.paycenter.pay.weixinpay.pojo.UnifiedOrder;
import com.laanto.it.paycenter.pay.weixinpay.pojo.WeiXinPayResult;
import com.laanto.it.paycenter.pay.weixinpay.utils.MD5;
import com.laanto.it.paycenter.pay.weixinpay.utils.WeixinCommonUtil;
import com.laanto.it.paycenter.pay.weixinpay.utils.WeixinPayUtil;
import com.laanto.it.paycenter.pay.weixinpay.utils.WexinSignUtil;
import com.laanto.it.paycenter.service.PayRequestService;
import com.laanto.it.paycenter.service.RefundRequestService;
import com.laanto.it.paycenter.utils.BeanUtil;
import com.laanto.it.paycenter.utils.BeanValidators;
import com.laanto.it.paycenter.utils.RestTemplateUtils;
import com.laanto.it.paycenter.utils.WebUtil;
import com.laanto.it.paycenter.vo.PayRequestVo;
import com.laanto.it.paycenter.vo.RefundRequestVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import java.io.*;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * 微信支付rest api
 *
 * @author user
 */
@RestController
@RequestMapping("/rest/refund/weixin")
public class WeixinRefundRestController {
    private static final Logger logger = LoggerFactory.getLogger(WeixinRefundRestController.class);

    @Autowired
    private WeixinpayConfig weixinpayConfig;

    @Autowired
    private PayRequestService payRequestService;

    @Autowired
    private RefundRequestService refundRequestService;

    @Autowired
    private Validator validator;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 微信退款
     *
     * @param refundRequestVo
     * @return
     */

    @RequestMapping(method = RequestMethod.POST, consumes = MediaTypes.JSON_UTF_8, produces = MediaTypes.JSON_UTF_8)
    public Map<String, Object> weixinRefund(@RequestBody RefundRequestVo refundRequestVo) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", false);
        Map<String, String> constraintViolations = BeanValidators.validate(validator, refundRequestVo);
        if (constraintViolations != null && !constraintViolations.isEmpty()) {
            String constraintViolationsMsg = String.format("退款参数错误：%s", constraintViolations.toString());
            resultMap.put("errmsg", constraintViolationsMsg);
            return resultMap;
        } else {
            RefundType refundType = RefundType.fromValue(refundRequestVo.getRefundType());
            if (refundType == null || !refundType.equals(RefundType.WEIXIN_PAY_REFUND)) {
                String refundTypeErrorMsg = String.format("退款方式参数错误，微信退款时refundType的取值只能是1，当前refundType=%d", refundRequestVo.getRefundType());
                resultMap.put("errmsg", refundTypeErrorMsg);
                return resultMap;
            }
            PayRequest payRequest = payRequestService.findByOutTradeNo(refundRequestVo.getOutTradeNo());
            if (payRequest == null) {
                String outTradeNoErrorMsg = "订单号错误，根据订单号查询不到支付记录";
                resultMap.put("errmsg", outTradeNoErrorMsg);
                return resultMap;
            }
            String transactionId = payRequest.getTradeNo();
            if (StringUtils.isBlank(transactionId)) {
                String transactionIdErrorMsg = "该笔订单支付可能未完成，根据订单查询不到微信支付完成的交易流水号";
                resultMap.put("errmsg", transactionIdErrorMsg);
                return resultMap;
            }
            String outRefundNo = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setOutTradeNo(refundRequestVo.getOutTradeNo());
            refundRequest.setRefundFee(refundRequestVo.getRefundFee());
            refundRequest.setRefundType(refundType);
            refundRequest.setOutRefundNo(outRefundNo);
            refundRequestService.save(refundRequest);
            Refund refund = createRefundParams(transactionId, outRefundNo, payRequest.getTotalFee(), refundRequestVo.getRefundFee());
            String refundXml = WeixinCommonUtil.beanToXml(refund, Refund.class);
            logger.debug("微信申请退款的请求参数xml格式：{}", refundXml);
            HttpEntity<String> entity = new HttpEntity<>(refundXml, RestTemplateUtils.getHeaderWithApplicationXmlAndUTF8());
            String response = restTemplate.postForObject(WeiXinApiUrl.PAY_REFUND, entity, String.class);
            boolean success = false;
            String errmsg = null;
            if (StringUtils.isNotBlank(response)) {
                Map<String, String> params = WeixinCommonUtil.parseXml(response);
                if (params != null && !params.isEmpty()) {
                    logger.debug("微信退款执行之后返回的参数集合：{}", params.toString());
                    if (refundRequest.getRefundResponse() == null && params != null) {
                        refundRequest.setRefundResponse(params.toString());
                    }
                    String return_code = params.get("return_code");
                    String return_msg = params.get("return_msg");
                    if (return_code != null && "SUCCESS".equalsIgnoreCase(return_code)) {
                        String result_code = params.get("result_code");
                        String err_code_des = params.get("err_code_des");
                        String refundId = params.get("refund_id");
                        if (result_code != null && "SUCCESS".equalsIgnoreCase(result_code)) {
                            success = true;
                            refundRequest.setRefundId(refundId);
                        } else {
                            errmsg = err_code_des;
                        }
                    } else {
                        errmsg = return_msg;
                    }
                } else {
                    errmsg = "微信退款失败，返回内容解析失败";
                }
            } else {
                errmsg = "微信退款失败，返回内容为空";
            }
            refundRequest.setSuccess(success);
            refundRequest.setErrorMsg(errmsg);
            refundRequest.setUpdateTime(new Date());
            refundRequestService.save(refundRequest);
            resultMap.put("success", success);
            if (success) {
                resultMap.put("outRefundNo", outRefundNo);
            }
            if (StringUtils.isNotBlank(errmsg)) {
                resultMap.put("errmsg", errmsg);
            }
            return resultMap;
        }
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = MediaTypes.JSON_UTF_8)
    public Map<String, Object> queryWeixinRefund(@RequestParam("outRefundNo") String outRefundNo) {
        Map<String, Object> resultMap = new HashMap<>();
        boolean success = false;
        String errmsg = null;
        String refundStatus = null;
        RefundRequest refundRequest = refundRequestService.findByOutRefundNo(outRefundNo);
        if (refundRequest != null) {
            QueryRefund queryRefund = createQueryRefundParams(outRefundNo);
            String queryRefundXml = WeixinCommonUtil.beanToXml(queryRefund, QueryRefund.class);
            logger.debug("微信查询退款的请求参数xml格式：{}", queryRefundXml);
            HttpEntity<String> entity = new HttpEntity<>(queryRefundXml, RestTemplateUtils.getHeaderWithApplicationXmlAndUTF8());
            String response = restTemplate.postForObject(WeiXinApiUrl.PAY_REFUND_QUERY, entity, String.class);
            if (StringUtils.isNotBlank(response)) {
                Map<String, String> params = WeixinCommonUtil.parseXml(response);
                if (params != null && !params.isEmpty()) {
                    logger.debug("查询微信退款返回的参数集合：{}", params.toString());
                    String return_code = params.get("return_code");
                    String return_msg = params.get("return_msg");
                    if (return_code != null && "SUCCESS".equalsIgnoreCase(return_code)) {
                        String result_code = params.get("result_code");
                        String err_code_des = params.get("err_code_des");
                        String refundStatusKey = null;
                        for (String key : params.keySet()) {
                            if (key.startsWith("refund_status_")) {
                                refundStatusKey = key;
                                break;
                            }
                        }
                        if (refundStatusKey != null) {
                            refundStatus = params.get(refundStatusKey);
                        }
                        if (result_code != null && "SUCCESS".equalsIgnoreCase(result_code)) {
                            success = true;
                        } else {
                            errmsg = err_code_des;
                        }
                    } else {
                        errmsg = return_msg;
                    }
                } else {
                    errmsg = "查询微信退款失败，返回内容解析失败";
                }
            } else {
                errmsg = "查询微信退款失败，返回结果为空";
            }
        } else {
            errmsg = "退款订单号错误，查询不到退款申请记录";
        }
        resultMap.put("success", success);
        if (StringUtils.isNotBlank(errmsg)) {
            resultMap.put("errmsg", errmsg);
        }
        if (StringUtils.isNotBlank(refundStatus)) {
            resultMap.put("refundStatus", refundStatus);
        }
        return resultMap;
    }

    private QueryRefund createQueryRefundParams(String outRefundNo) {
        QueryRefund queryRefund = new QueryRefund();
        queryRefund.setAppid(weixinpayConfig.getAppid());
        queryRefund.setMch_id(weixinpayConfig.getMchId());
        queryRefund.setNonce_str(UUID.randomUUID().toString().replace("-", ""));
        queryRefund.setOut_refund_no(outRefundNo);
        Map<String, Object> queryRefundMap = WeixinPayUtil.paraFilter(BeanUtil.transferBean2Map(queryRefund));
        String stringForSign = WeixinPayUtil.createLinkString(queryRefundMap);
        queryRefund.setSign(MD5.signWeixinPay(stringForSign, weixinpayConfig.getPartnerKey(), "utf-8").toUpperCase());
        return queryRefund;
    }

    private Refund createRefundParams(String transactionId, String outRefundNo, BigDecimal totalFee, BigDecimal refundFee) {
        Refund refund = new Refund();
        refund.setAppid(weixinpayConfig.getAppid());
        refund.setMch_id(weixinpayConfig.getMchId());
        refund.setNonce_str(UUID.randomUUID().toString().replace("-", ""));
        refund.setTransaction_id(transactionId);
        refund.setOut_refund_no(outRefundNo);
        long covertedTotalFee = totalFee.multiply(new BigDecimal(100)).longValue();
        refund.setTotal_fee(String.valueOf(covertedTotalFee));
        long covertedRefundlFee = refundFee.multiply(new BigDecimal(100)).longValue();
        refund.setRefund_fee(String.valueOf(covertedRefundlFee));
        refund.setOp_user_id(weixinpayConfig.getAppid());
        Map<String, Object> refundMap = WeixinPayUtil.paraFilter(BeanUtil.transferBean2Map(refund));
        String stringForSign = WeixinPayUtil.createLinkString(refundMap);
        refund.setSign(MD5.signWeixinPay(stringForSign, weixinpayConfig.getPartnerKey(), "utf-8").toUpperCase());
        return refund;
    }
}
