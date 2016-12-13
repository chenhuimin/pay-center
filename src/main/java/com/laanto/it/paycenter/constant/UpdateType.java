package com.laanto.it.paycenter.constant;

public enum UpdateType {
    /**
     * 异步页面更新
     */
    UPDATE_ASYNC(0),

    /**
     * 同步页面更新
     */
    UPDATE_SYNC(1);

    private final int value;

    private UpdateType(int value) {
        this.value = value;
    }

    public static UpdateType fromValue(int value) {
        UpdateType result = null;
        for (UpdateType updateType : values()) {
            if (updateType.value == value) {
                result = updateType;
                break;
            }
        }
        return result;
    }

    public int toValue() {
        return value;
    }

}
