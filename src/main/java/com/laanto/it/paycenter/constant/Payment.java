package com.laanto.it.paycenter.constant;

public enum Payment {
    /**
     * 支付宝PC支付.
     */
    ALI_PAY_PC(0),

    /**
     * 支付宝WAP支付.
     */
    ALI_PAY_WAP(1),

    /**
     * 银联支付.
     */
    UNION_PAY(2),

    /**
     * 微信支付.
     */
    WEIXIN_PAY(3);

    private final int value;

    private Payment(int value) {
        this.value = value;
    }

    public static Payment fromValue(int value) {
        Payment result = null;
        for (Payment payment : values()) {
            if (payment.value == value) {
                result = payment;
                break;
            }
        }
        return result;
    }

    public int toValue() {
        return value;
    }

}
