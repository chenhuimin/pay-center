package com.laanto.it.paycenter.pay.alipay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 */
@Component
public class AlipayConfig {

    /**
     * 服务类型,即时到账或担保交易 pc端
     */
    @Value("${alipay.service.pc}")
    private String service_pc;


    /**
     * 服务类型,即时到账或担保交易wap端
     */
    @Value("${alipay.service.wap}")
    private String service_wap;

    /**
     * 合作身份者ID，以2088开头由16位纯数字组成的字符串
     */
    @Value("${alipay.partner}")
    private String partner;

    /**
     * 商户的私钥
     */
    @Value("${alipay.key}")
    private String key;

    /**
     * 字符编码格式 目前支持 gbk 或 utf-8
     */
    @Value("${alipay.input_charset}")
    private String input_charset;

    /**
     * 签名方式 不需修改
     */
    @Value("${alipay.sign_type}")
    private String sign_type;

    /**
     * 支付宝消息验证地址
     */
    @Value("${alipay.https_verify_url}")
    private String https_verify_url;

    /**
     * 支付宝提供给商户的服务接入网关URL(新)
     */
    @Value("${alipay.alipay_gateway_new}")
    private String alipay_gateway_new;

    /**
     * 卖家支付宝帐户
     */
    @Value("${alipay.seller_email}")
    private String seller_email;

    /**
     * 卖家支付宝用户号
     */
    @Value("${alipay.partner}")
    private String sellerId;

    /**
     * 支付类型
     */
    @Value("${alipay.payment_type}")
    private String payment_type;

    @Value("${alipay.return_url}")
    private String return_url;

    @Value("${alipay.notify_url}")
    private String notify_url;

    public String getService_pc() {
        return service_pc;
    }

    public void setService_pc(String service_pc) {
        this.service_pc = service_pc;
    }

    public String getService_wap() {
        return service_wap;
    }

    public void setService_wap(String service_wap) {
        this.service_wap = service_wap;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getInput_charset() {
        return input_charset;
    }

    public void setInput_charset(String input_charset) {
        this.input_charset = input_charset;
    }

    public String getSign_type() {
        return sign_type;
    }

    public void setSign_type(String sign_type) {
        this.sign_type = sign_type;
    }

    public String getHttps_verify_url() {
        return https_verify_url;
    }

    public void setHttps_verify_url(String https_verify_url) {
        this.https_verify_url = https_verify_url;
    }

    public String getAlipay_gateway_new() {
        return alipay_gateway_new;
    }

    public void setAlipay_gateway_new(String alipay_gateway_new) {
        this.alipay_gateway_new = alipay_gateway_new;
    }

    public String getSeller_email() {
        return seller_email;
    }

    public void setSeller_email(String seller_email) {
        this.seller_email = seller_email;
    }

    public String getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(String payment_type) {
        this.payment_type = payment_type;
    }

    public String getReturn_url() {
        return return_url;
    }

    public void setReturn_url(String return_url) {
        this.return_url = return_url;
    }

    public String getNotify_url() {
        return notify_url;
    }

    public void setNotify_url(String notify_url) {
        this.notify_url = notify_url;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
}
