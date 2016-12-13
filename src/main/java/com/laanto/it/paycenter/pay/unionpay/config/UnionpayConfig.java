package com.laanto.it.paycenter.pay.unionpay.config;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UnionpayConfig implements Serializable {

    private static final long serialVersionUID = -3007981934150800889L;

    // 签名的key名称
    public static final String KEY_SIGNATURE = "signature";

    // 前台交易地址
    @Value("${unionpay.payUrl}")
    private String payUrl;

    // 后续类交易地址
    @Value("${unionpay.bsPayUrl}")
    private String bsPayUrl;

    // 交易信息查询地址
    @Value("${unionpay.queryUrl}")
    private String queryUrl;

    //加密方式
    @Value("${unionpay.signType}")
    private String signType;

    @Value("${unionpay.signTypeSHA1WithRSA}")
    private String signTypeSHA1WithRSA;

    //商户密匙
    @Value("${unionpay.securityKey}")
    private String securityKey;

    // 版本号
    @Value("${unionpay.version}")
    private String version;

    // 编码方式
    @Value("${unionpay.charset}")
    private String charset;

    //交易类型:消费
    @Value("${unionpay.transType.consume}")
    private String transTypeConsume;

    //交易类型:消费
    @Value("${unionpay.transType.refund}")
    private String transTypeRefund;


    // 商户号
    @Value("${unionpay.merId}")
    private String merId;

    // 商户名称简写
    @Value("${unionpay.merAbbr}")
    private String merAbbr;

    // 交易币种(默认人民币)
    @Value("${unionpay.orderCurrency}")
    private String orderCurrency;

    @Value("${unionpay.frontEndUrl.pay}")
    private String payFrontEndUrl;

    @Value("${unionpay.backEndUrl.pay}")
    private String payBackEndUrl;

    @Value("${unionpay.frontEndUrl.refund}")
    private String refundFrontEndUrl;

    @Value("${unionpay.backEndUrl.refund}")
    private String refundBackEndUrl;


    // 组装消费请求包
    private String[] reqVo = new String[]{"version", "charset", "transType", "origQid", "merId", "merAbbr", "acqCode", "merCode",
            "commodityUrl", "commodityName", "commodityUnitPrice", "commodityQuantity", "commodityDiscount", "transferFee", "orderNumber",
            "orderAmount", "orderCurrency", "orderTime", "customerIp", "customerName", "defaultPayType", "defaultBankNumber",
            "transTimeout", "frontEndUrl", "backEndUrl", "merReserved"};

    private String[] notifyVo = new String[]{"charset", "cupReserved", "exchangeDate", "exchangeRate", "merAbbr", "merId", "orderAmount",
            "orderCurrency", "orderNumber", "qid", "respCode", "respMsg", "respTime", "settleAmount", "settleCurrency", "settleDate",
            "traceNumber", "traceTime", "transType", "version"};

    private String[] queryVo = new String[]{"version", "charset", "transType", "merId", "orderNumber", "orderTime", "merReserved"};

    private String[] smsVo = new String[]{"version", "charset", "acqCode", "merId", "merAbbr", "orderNumber", "orderAmount",
            "orderCurrency", "merReserved"};

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public String getBsPayUrl() {
        return bsPayUrl;
    }

    public void setBsPayUrl(String bsPayUrl) {
        this.bsPayUrl = bsPayUrl;
    }

    public String getQueryUrl() {
        return queryUrl;
    }

    public void setQueryUrl(String queryUrl) {
        this.queryUrl = queryUrl;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getSignTypeSHA1WithRSA() {
        return signTypeSHA1WithRSA;
    }

    public void setSignTypeSHA1WithRSA(String signTypeSHA1WithRSA) {
        this.signTypeSHA1WithRSA = signTypeSHA1WithRSA;
    }

    public String getSecurityKey() {
        return securityKey;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }


    public String getTransTypeConsume() {
        return transTypeConsume;
    }

    public void setTransTypeConsume(String transTypeConsume) {
        this.transTypeConsume = transTypeConsume;
    }

    public String getTransTypeRefund() {
        return transTypeRefund;
    }

    public void setTransTypeRefund(String transTypeRefund) {
        this.transTypeRefund = transTypeRefund;
    }

    public String getMerId() {
        return merId;
    }

    public void setMerId(String merId) {
        this.merId = merId;
    }

    public String getMerAbbr() {
        return merAbbr;
    }

    public void setMerAbbr(String merAbbr) {
        this.merAbbr = merAbbr;
    }

    public String getOrderCurrency() {
        return orderCurrency;
    }

    public void setOrderCurrency(String orderCurrency) {
        this.orderCurrency = orderCurrency;
    }

    public String getPayFrontEndUrl() {
        return payFrontEndUrl;
    }

    public void setPayFrontEndUrl(String payFrontEndUrl) {
        this.payFrontEndUrl = payFrontEndUrl;
    }

    public String getPayBackEndUrl() {
        return payBackEndUrl;
    }

    public void setPayBackEndUrl(String payBackEndUrl) {
        this.payBackEndUrl = payBackEndUrl;
    }

    public String getRefundFrontEndUrl() {
        return refundFrontEndUrl;
    }

    public void setRefundFrontEndUrl(String refundFrontEndUrl) {
        this.refundFrontEndUrl = refundFrontEndUrl;
    }

    public String getRefundBackEndUrl() {
        return refundBackEndUrl;
    }

    public void setRefundBackEndUrl(String refundBackEndUrl) {
        this.refundBackEndUrl = refundBackEndUrl;
    }

    public String[] getReqVo() {
        return reqVo;
    }

    public void setReqVo(String[] reqVo) {
        this.reqVo = reqVo;
    }

    public String[] getNotifyVo() {
        return notifyVo;
    }

    public void setNotifyVo(String[] notifyVo) {
        this.notifyVo = notifyVo;
    }

    public String[] getQueryVo() {
        return queryVo;
    }

    public void setQueryVo(String[] queryVo) {
        this.queryVo = queryVo;
    }

    public String[] getSmsVo() {
        return smsVo;
    }

    public void setSmsVo(String[] smsVo) {
        this.smsVo = smsVo;
    }
}
