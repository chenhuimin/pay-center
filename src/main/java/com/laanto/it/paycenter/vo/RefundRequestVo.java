package com.laanto.it.paycenter.vo;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by chenhuimin on 2016/4/19 0019.
 */
public class RefundRequestVo implements Serializable {

    private static final long serialVersionUID = 979219293038483106L;

    private String outTradeNo;


    private BigDecimal refundFee;


//    private String notifyUrl;
//
//
//    private String returnUrl;


    private Integer refundType;

    @NotBlank(message = "订单号不能为空")
    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }


    @NotNull(message = "退款金额不能为空")
    @Min(value = 0, message = "退款金额必须大于或等于0")
    public BigDecimal getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(BigDecimal refundFee) {
        this.refundFee = refundFee;
    }


//    public String getNotifyUrl() {
//        return notifyUrl;
//    }
//
//    public void setNotifyUrl(String notifyUrl) {
//        this.notifyUrl = notifyUrl;
//    }
//
//    public String getReturnUrl() {
//        return returnUrl;
//    }
//
//    public void setReturnUrl(String returnUrl) {
//        this.returnUrl = returnUrl;
//    }

    @NotNull(message = "退款方式不能为空")
    @Min(value = 0, message = "退款方式必须大于或等于0")
    public Integer getRefundType() {
        return refundType;
    }

    public void setRefundType(Integer refundType) {
        this.refundType = refundType;
    }
}
