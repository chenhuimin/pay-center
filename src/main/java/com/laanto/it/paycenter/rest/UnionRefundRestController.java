package com.laanto.it.paycenter.rest;

import com.laanto.it.paycenter.constant.MediaTypes;
import com.laanto.it.paycenter.constant.RefundType;
import com.laanto.it.paycenter.domain.PayRequest;
import com.laanto.it.paycenter.domain.RefundRequest;
import com.laanto.it.paycenter.pay.unionpay.config.UnionpayConfig;
import com.laanto.it.paycenter.pay.unionpay.util.UnionRefundUtils;
import com.laanto.it.paycenter.pay.unionpay.util.UnionpayUtils;
import com.laanto.it.paycenter.service.PayRequestService;
import com.laanto.it.paycenter.service.RefundRequestService;
import com.laanto.it.paycenter.utils.BeanValidators;
import com.laanto.it.paycenter.utils.WebUtil;
import com.laanto.it.paycenter.vo.RefundRequestVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


@RestController
@RequestMapping("/rest/refund/union")
public class UnionRefundRestController {
    private static final Logger logger = LoggerFactory.getLogger(UnionRefundRestController.class);

    @Autowired
    private UnionpayConfig unionpayConfig;

    @Autowired
    private PayRequestService payRequestService;

    @Autowired
    private RefundRequestService refundRequestService;

    @Autowired
    private Validator validator;

    @Autowired
    private UnionRefundUtils unionRefundUtils;

    /**
     * 银联退款
     *
     * @param refundRequestVo
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaTypes.JSON_UTF_8, produces = MediaTypes.JSON_UTF_8)
    public Map<String, Object> unionRefund(@RequestBody RefundRequestVo refundRequestVo, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", false);
        Map<String, String> constraintViolations = BeanValidators.validate(validator, refundRequestVo);
        if (constraintViolations != null && !constraintViolations.isEmpty()) {
            String constraintViolationsMsg = String.format("退款参数错误：%s", constraintViolations.toString());
            resultMap.put("errmsg", constraintViolationsMsg);
            return resultMap;
        } else {
//            if (StringUtils.isBlank(refundRequestVo.getReturnUrl())) {
//                String returnUrlErrorMsg = "银联退款时，页面跳转同步通知页面不能为空";
//                resultMap.put("errmsg", returnUrlErrorMsg);
//                return resultMap;
//            }
//            if (StringUtils.isBlank(refundRequestVo.getNotifyUrl())) {
//                String notifyUrlErrorMsg = "银联退款时，页面跳转异步通知页面不能为空";
//                resultMap.put("errmsg", notifyUrlErrorMsg);
//                return resultMap;
//            }
            RefundType refundType = RefundType.fromValue(refundRequestVo.getRefundType());
            if (refundType == null || !refundType.equals(RefundType.UNION_PAY_REFUND)) {
                String refundTypeErrorMsg = String.format("退款方式参数错误，银联退款时refundType的取值只能是0，当前refundType=%d", refundRequestVo.getRefundType());
                resultMap.put("errmsg", refundTypeErrorMsg);
                return resultMap;
            }
            PayRequest payRequest = payRequestService.findByOutTradeNo(refundRequestVo.getOutTradeNo());
            if (payRequest == null) {
                String outTradeNoErrorMsg = "订单号错误，根据订单号查询不到支付记录";
                resultMap.put("errmsg", outTradeNoErrorMsg);
                return resultMap;
            }
            String origQid = payRequest.getTradeNo();
            if (StringUtils.isBlank(origQid)) {
                String qidErrorMsg = "该笔订单支付可能未完成，根据订单查询不到银联支付完成的交易流水号";
                resultMap.put("errmsg", qidErrorMsg);
                return resultMap;
            }
            String outRefundNo = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            String orderTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            Map<String, String> refundParams = createRefundParams(origQid, outRefundNo, orderTime, refundRequestVo, request);
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setOutTradeNo(refundRequestVo.getOutTradeNo());
            refundRequest.setRefundFee(refundRequestVo.getRefundFee());
            refundRequest.setRefundType(refundType);
//            refundRequest.setReturnUrl(refundRequestVo.getReturnUrl());
//            refundRequest.setNotifyUrl(refundRequestVo.getNotifyUrl());
            refundRequest.setOutRefundNo(outRefundNo);
            refundRequest.setOrderTime(orderTime);
            refundRequestService.save(refundRequest);
            String res = unionRefundUtils.doRefund(refundParams, unionpayConfig);
            boolean success = false;
            String errmsg = null;
            if (StringUtils.isNotBlank(res)) {
                Map<String, String> resMap = unionRefundUtils.getResArr(res);
                logger.debug("银联退款执行之后返回的参数集合：{}", resMap.toString());
                if (checkSecurity(resMap)) {// 验证签名
                    // 以下是商户业务处理
                    String respCode = resMap.get("respCode");
                    String respMsg = resMap.get("respMsg");
                    if ("00".equals(respCode)) {
                        success = true;
                    } else {
                        errmsg = String.format("银联退款失败，调用银联退款接口的返回结果：respCode=%s，respMsg=%s", respCode, respMsg);
                    }
                } else {
                    errmsg = "银联退款失败，签名验证失败";
                }
            } else {
                errmsg = "银联退款失败，返回结果为空";
            }
            refundRequest.setSuccess(success);
            refundRequest.setErrorMsg(errmsg);
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

    /**
     * 银联退款完成之后页面跳转同步通知页面路径
     *
     * @return
     */
    @RequestMapping(value = "/return_url", method = RequestMethod.POST)
    public void createReturnUrl(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("进入银联退款同步通知页面");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/html; charset=utf-8");
        Map<String, String> params = getRefundResultParams(request);
        String signature = request.getParameter(UnionpayConfig.KEY_SIGNATURE);
        Map<String, Object> checkRefundResult = checkRefundResult(params, signature);
        boolean refundResult = (Boolean) checkRefundResult.get("success");
        String errorMsg = (String) checkRefundResult.get("errorMsg");
        String outRefundNo = params.get("orderNumber");
        String refundId = params.get("qid");
        RefundRequest refundRequest = refundRequestService.findByOutRefundNo(outRefundNo);
        if (refundRequest != null) {
            if (refundRequest.getRefundId() == null) {
                refundRequest.setRefundId(refundId);
                refundRequest.setUpdateTime(new Date());
                if (StringUtils.isNotBlank(errorMsg)) {
                    refundRequest.setErrorMsg(errorMsg);
                }
                if (refundRequest.getRefundResponse() == null && params != null) {
                    refundRequest.setRefundResponse(params.toString());
                }
                refundRequestService.save(refundRequest);
            }
        } else {
            logger.error("根据退款订单号outRefundNo={},查找不到退款请求", outRefundNo);
        }
        if (refundResult) {
            //程序执行完后必须打印输出“success”（不包含引号）。
            logger.debug("退款成功，返回给银联的值：success，状态码：200");
            try {
                response.getWriter().write("success");
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.debug("退款失败，返回给银联的值：fail，状态码：500");
            try {
                response.setStatus(500);
                response.getWriter().write("fail");
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 银联退款完成之后服务器异步通知页面路径
     *
     * @return
     */
    @RequestMapping(value = "/notify_url", method = RequestMethod.POST)
    public void createNotifyUrl(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("进入银联退款异步通知页面");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/html; charset=utf-8");
        Map<String, String> params = getRefundResultParams(request);
        String signature = request.getParameter(UnionpayConfig.KEY_SIGNATURE);
        Map<String, Object> checkRefundResult = checkRefundResult(params, signature);
        boolean refundResult = (Boolean) checkRefundResult.get("success");
        String errorMsg = (String) checkRefundResult.get("errorMsg");
        String outRefundNo = params.get("orderNumber");
        String refundId = params.get("qid");
        RefundRequest refundRequest = refundRequestService.findByOutRefundNo(outRefundNo);
        if (refundRequest != null) {
            if (refundRequest.getRefundId() == null) {
                refundRequest.setRefundId(refundId);
                refundRequest.setUpdateTime(new Date());
                if (StringUtils.isNotBlank(errorMsg)) {
                    refundRequest.setErrorMsg(errorMsg);
                }
                if (refundRequest.getRefundResponse() == null && params != null) {
                    refundRequest.setRefundResponse(params.toString());
                }
                refundRequestService.save(refundRequest);
            }
        } else {
            logger.error("根据退款订单号outRefundNo={},查找不到退款请求", outRefundNo);
        }
        if (refundResult) {
            //程序执行完后必须打印输出“success”（不包含引号）。
            logger.debug("退款成功，返回给银联的值：success，状态码：200");
            try {
                response.getWriter().write("success");
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.debug("退款失败，返回给银联的值：fail，状态码：500");
            try {
                response.setStatus(500);
                response.getWriter().write("fail");
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = MediaTypes.JSON_UTF_8)
    public Map<String, Object> queryUnionRefund(@RequestParam("outRefundNo") String outRefundNo) {
        Map<String, Object> resultMap = new HashMap<>();
        boolean success = false;
        String errmsg = null;
        RefundRequest refundRequest = refundRequestService.findByOutRefundNo(outRefundNo);
        if (refundRequest != null) {
            Map<String, String> queryRefundParams = createQueryRefundParams(outRefundNo, refundRequest.getOrderTime());
            String res = unionRefundUtils.doQueryRefund(queryRefundParams, unionpayConfig);
            if (StringUtils.isNotBlank(res)) {
                Map<String, String> resMap = unionRefundUtils.getResArr(res);
                logger.debug("查询银联退款返回的参数集合：{}", resMap.toString());
                if (checkSecurity(resMap)) {// 验证签名
                    // 以下是商户业务处理
                    String respCode = resMap.get("respCode");
                    String respMsg = resMap.get("respMsg");
                    if ("00".equals(respCode)) {
                        success = true;
                    } else {
                        errmsg = String.format("银联退款失败，查询银联退款的返回结果：respCode=%s，respMsg=%s", respCode, respMsg);
                    }
                } else {
                    errmsg = "查询银联退款失败，签名验证失败";
                }
            } else {
                errmsg = "查询银联退款失败，返回结果为空";
            }
            refundRequestService.save(refundRequest);
        } else {
            errmsg = "退款订单号错误，查询不到退款申请记录";
        }
        resultMap.put("success", success);
        if (StringUtils.isNotBlank(errmsg)) {
            resultMap.put("errmsg", errmsg);
        }
        return resultMap;

    }

    private Map<String, String> createQueryRefundParams(String orderNumber, String orderTime) {
        Map<String, String> queryRefundParams = new TreeMap<String, String>();
        queryRefundParams.put("version", unionpayConfig.getVersion().trim());
        queryRefundParams.put("charset", unionpayConfig.getCharset().trim());
        queryRefundParams.put("transType", unionpayConfig.getTransTypeRefund().trim());
        queryRefundParams.put("merId", unionpayConfig.getMerId().trim());
        queryRefundParams.put("orderNumber", orderNumber);
        queryRefundParams.put("orderTime", orderTime);
        queryRefundParams.put("merReserved", "");
        return queryRefundParams;

    }


    private Map<String, String> createRefundParams(String origQid, String outRefundNo, String orderTime, RefundRequestVo refundRequestVo, HttpServletRequest request) {
        Map<String, String> refundParams = new TreeMap<String, String>();
        refundParams.put("version", unionpayConfig.getVersion().trim());
        refundParams.put("charset", unionpayConfig.getCharset().trim());
        refundParams.put("transType", unionpayConfig.getTransTypeRefund().trim());
        refundParams.put("origQid", origQid);
        refundParams.put("merId", unionpayConfig.getMerId().trim());
        refundParams.put("merAbbr", unionpayConfig.getMerAbbr().trim());
        refundParams.put("acqCode", "");
        refundParams.put("merCode", "");
        refundParams.put("commodityUrl", "");
        refundParams.put("commodityName", "");
        refundParams.put("commodityUnitPrice", "");
        refundParams.put("commodityQuantity", "");
        refundParams.put("commodityDiscount", "");
        refundParams.put("transferFee", "");
        refundParams.put("orderNumber", outRefundNo);
        long refundFee = (refundRequestVo.getRefundFee().multiply(new BigDecimal(100))).longValue();
        refundParams.put("orderAmount", String.valueOf(refundFee));
        refundParams.put("orderCurrency", unionpayConfig.getOrderCurrency().trim());
        refundParams.put("orderTime", orderTime);
        String customerId = WebUtil.getUserIp(request);
        refundParams.put("customerIp", customerId);
        refundParams.put("customerName", "");
        refundParams.put("defaultPayType", "");
        refundParams.put("defaultBankNumber", "");
        refundParams.put("transTimeout", "");
        String frontEndUrl = WebUtil.getAbsoluteUrl(request, unionpayConfig.getRefundFrontEndUrl().trim());
        refundParams.put("frontEndUrl", frontEndUrl);//前台通知地址
        String backEndUrl = WebUtil.getAbsoluteUrl(request, unionpayConfig.getRefundBackEndUrl().trim());
        refundParams.put("backEndUrl", backEndUrl);// 后台通知地址
        refundParams.put("merReserved", "");
        return refundParams;
    }

    // 验证签名
    private boolean checkSecurity(Map<String, String> map) {
        // 验证签名
        int checkedRes = unionRefundUtils.checkSecurity(map, unionpayConfig);
        if (checkedRes == 1) {
            return true;
        } else if (checkedRes == 0) {
            return false;
        } else if (checkedRes == 2) {
            return false;
        }
        return false;
    }

    private Map<String, Object> checkRefundResult(Map<String, String> params, String signature) {
        Map<String, Object> result = new HashMap<>();
        boolean signatureCheck = UnionpayUtils.checkSign(params, signature, unionpayConfig);
        String respCode = params.get("respCode");
        String respMsg = params.get("respMsg");
        boolean success = false;
        String errorMsg = null;
        if (signatureCheck) {// 签名验证成功
            if ("00".equals(respCode)) {
                success = true;
            } else if ("30".equals(respCode)) {
                errorMsg = String.format("银联退款失败，报文格式错误，respCode=%s，respMsg=%s", respCode, respMsg);
            } else if ("94".equals(respCode)) {
                errorMsg = String.format("银联退款失败，重复交易，respCode=%s，respMsg=%s", respCode, respMsg);
            } else if ("25".equals(respCode)) {
                errorMsg = String.format("银联退款失败，查询原交易失败，respCode=%s，respMsg=%s", respCode, respMsg);
            } else if ("36".equals(respCode)) {
                errorMsg = String.format("银联退款失败，交易金额超限，respCode=%s，respMsg=%s", respCode, respMsg);
            } else {
                errorMsg = String.format("银联退款失败，其他错误，respCode=%s，respMsg=%s", respCode, respMsg);
            }
        } else {// 验证失败
            errorMsg = "银联退款失败，签名验证失败";
        }
        result.put("success", success);
        if (StringUtils.isNotBlank(errorMsg)) {
            result.put("errorMsg", errorMsg);
        }
        return result;
    }

    private Map<String, String> getRefundResultParams(HttpServletRequest request) {
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
}
