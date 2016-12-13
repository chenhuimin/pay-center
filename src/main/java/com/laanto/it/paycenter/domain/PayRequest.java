package com.laanto.it.paycenter.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.laanto.it.paycenter.constant.UpdateType;
import org.hibernate.annotations.Where;

import com.laanto.it.paycenter.constant.Payment;

@Entity
@Table(name = "t_pay_req")
@Where(clause = "deleted='false'")
public class PayRequest extends BaseDomain {
    private static final long serialVersionUID = -517743615018262226L;

    @Column(name = "commodity_name", nullable = false)
    private String commodityName;

    @Column(name = "total_fee", nullable = false, precision = 11, scale = 2)
    private BigDecimal totalFee;

    @Column(name = "out_trade_no", nullable = false, unique = true)
    private String outTradeNo;

    @Column(name = "notify_url", nullable = false)
    private String notifyUrl;

    @Column(name = "return_url")
    private String returnUrl;

    @Column(name = "payment", nullable = false)
    private Payment payment;

    @Column(name = "trade_no")
    private String tradeNo;

    private Boolean success;

    @Column(name = "error_msg")
    private String errorMsg;

    private String openid;

    @Column(name = "wx_pay_page")
    private String wxPayPage;

    @Column(name = "update_by")
    private UpdateType updateBy;

    @Column(name = "res_params", columnDefinition = "TEXT")
    private String responseParams;

    public String getCommodityName() {
        return commodityName;
    }

    public void setCommodityName(String commodityName) {
        this.commodityName = commodityName;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getWxPayPage() {
        return wxPayPage;
    }

    public void setWxPayPage(String wxPayPage) {
        this.wxPayPage = wxPayPage;
    }

    public UpdateType getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(UpdateType updateBy) {
        this.updateBy = updateBy;
    }

    public String getResponseParams() {
        return responseParams;
    }

    public void setResponseParams(String responseParams) {
        this.responseParams = responseParams;
    }
}
