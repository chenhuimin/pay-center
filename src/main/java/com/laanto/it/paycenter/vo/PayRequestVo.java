package com.laanto.it.paycenter.vo;

import com.laanto.it.paycenter.constant.Payment;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by chenhuimin on 2016/4/19 0019.
 */
public class PayRequestVo implements Serializable {
    private static final long serialVersionUID = -3544803945614576909L;

    private String commodityName;


    private BigDecimal totalFee;


    private String outTradeNo;


    private String notifyUrl;


    private String returnUrl;


    private Integer payment;

    private String openid;

    private String wxPayPage;

    @NotBlank(message = "商品名称不能为空")
    public String getCommodityName() {
        return commodityName;
    }

    public void setCommodityName(String commodityName) {
        this.commodityName = commodityName;
    }

    @NotNull(message = "支付金额不能为空")
    @Min(value = 0, message = "支付金额必须大于或等于0")
    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    @NotBlank(message = "订单号不能为空")
    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    @NotBlank(message = "服务器异步通知页面路径不能为空")
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

    @NotNull(message = "支付方式不能为空")
    @Min(value = 0, message = "支付方式必须大于或等于0")
    public Integer getPayment() {
        return payment;
    }

    public void setPayment(Integer payment) {
        this.payment = payment;
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
}
