package com.laanto.it.paycenter.domain;

import com.laanto.it.paycenter.constant.Payment;
import com.laanto.it.paycenter.constant.RefundType;
import com.laanto.it.paycenter.constant.UpdateType;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "t_refund_req")
@Where(clause = "deleted='false'")
public class RefundRequest extends BaseDomain {
    private static final long serialVersionUID = -4902663768050071409L;

    /**
     * 支付时的订单号
     */
    @Column(name = "out_trade_no", nullable = false)
    private String outTradeNo;

    /**
     * 退款金额
     */
    @Column(name = "refund_fee", nullable = false, precision = 11, scale = 2)
    private BigDecimal refundFee;

    /**
     * 退款类型
     */

    @Column(name = "refund_type", nullable = false)
    private RefundType refundType;

    /**
     * 退款时的订单号
     */
    @Column(name = "out_refund_no", nullable = false, unique = true)
    private String outRefundNo;

    /**
     * 退款成功流水号
     */
    @Column(name = "refund_id")
    private String refundId;

    /**
     * 退款是否成功
     */
    private Boolean success;

    /**
     * 退款错误消息
     */
    @Column(name = "error_msg")
    private String errorMsg;

    /**
     * 退款接口返回的报文数据
     */
    @Column(name = "refund_res", columnDefinition = "TEXT")
    private String refundResponse;

    /**
     * 退款时间yyyyMMddHHmmss
     */
    @Column(name = "order_time")
    private String orderTime;

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public BigDecimal getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(BigDecimal refundFee) {
        this.refundFee = refundFee;
    }

    public RefundType getRefundType() {
        return refundType;
    }

    public void setRefundType(RefundType refundType) {
        this.refundType = refundType;
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

    public String getOutRefundNo() {
        return outRefundNo;
    }

    public void setOutRefundNo(String outRefundNo) {
        this.outRefundNo = outRefundNo;
    }

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
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

    public String getRefundResponse() {
        return refundResponse;
    }

    public void setRefundResponse(String refundResponse) {
        this.refundResponse = refundResponse;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }
}



