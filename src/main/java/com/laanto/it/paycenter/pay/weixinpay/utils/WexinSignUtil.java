package com.laanto.it.paycenter.pay.weixinpay.utils;

import java.security.MessageDigest;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WexinSignUtil {
    private static Logger logger = LoggerFactory.getLogger(WexinSignUtil.class);

    public static Map<String, String> signJSAPIWXPay(String appId, String packageParam, String signType, String keyValue,
                                                     String input_charset) {
        Map<String, String> map = new HashMap<String, String>();
        String nonceStr = createNonceStr();
        String timeStamp = createTimestamp();
        String stringForSign = "appId=" + appId + "&nonceStr=" + nonceStr + "&package=" + packageParam + "&signType=" + signType
                + "&timeStamp=" + timeStamp;
        logger.debug("JSAPI方式调用微信支付的签名字符串:{}", stringForSign);
        String paySign = MD5.signWeixinPay(stringForSign, keyValue, input_charset).toUpperCase();
        map.put("appId", appId);
        map.put("timeStamp", timeStamp);
        map.put("nonceStr", nonceStr);
        map.put("packageParam", packageParam);
        map.put("signType", signType);
        map.put("paySign", paySign);
        return map;
    }

    public static Map<String, String> signJSSDKWXConfig(String jsapi_ticket, String url, String signType, String input_charset) {
        Map<String, String> map = new HashMap<String, String>();
        String nonce_str = createNonceStr();
        String timestamp = createTimestamp();
        String signature = null;
        // 注意这里参数名必须全部小写，且必须有序
        String stringForSign = "jsapi_ticket=" + jsapi_ticket + "&noncestr=" + nonce_str + "&timestamp=" + timestamp + "&url=" + url;
        logger.debug("微信jssdk调用时 wx.config签名字符串:{}", stringForSign);
        try {
            MessageDigest crypt = MessageDigest.getInstance(signType);
            crypt.reset();
            crypt.update(stringForSign.getBytes(input_charset));
            signature = byteToHex(crypt.digest());
        } catch (Exception e) {
            logger.error("微信jssdk调用时 wx.config签名错误", e);
        }
        map.put("url", url);
        map.put("jsapi_ticket", jsapi_ticket);
        map.put("nonceStr", nonce_str);
        map.put("timestamp", timestamp);
        map.put("signature", signature);
        return map;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String createNonceStr() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String createTimestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
}
