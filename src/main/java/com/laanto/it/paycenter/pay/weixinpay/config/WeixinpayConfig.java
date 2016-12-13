package com.laanto.it.paycenter.pay.weixinpay.config;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WeixinpayConfig implements Serializable {
    private static final long serialVersionUID = 542581912215522056L;

    @Value("${weixinpay.appid}")
    private String appid;

    @Value("${weixinpay.secret}")
    private String secret;

    @Value("${weixinpay.mch_id}")
    private String mchId;

    @Value("${weixinpay.partner_key}")
    private String partnerKey;

    @Value("${weixinpay.trade_type}")
    private String tradeType;

    @Value("${weixinpay.notify_url}")
    private String notifyUrl;

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getPartnerKey() {
        return partnerKey;
    }

    public void setPartnerKey(String partnerKey) {
        this.partnerKey = partnerKey;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
