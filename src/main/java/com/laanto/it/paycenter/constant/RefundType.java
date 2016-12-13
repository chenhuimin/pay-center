package com.laanto.it.paycenter.constant;

/**
 * Created by chenhuimin on 2016/4/27 0027.
 */
public enum RefundType {
    /**
     * 银联支付退款
     */
    UNION_PAY_REFUND(0),

    /**
     * 微信支付退款
     */
    WEIXIN_PAY_REFUND(1);

    private final int value;

    private RefundType(int value) {
        this.value = value;
    }

    public static RefundType fromValue(int value) {
        RefundType result = null;
        for (RefundType refundType : values()) {
            if (refundType.value == value) {
                result = refundType;
                break;
            }
        }
        return result;
    }

    public int toValue() {
        return value;
    }

}
