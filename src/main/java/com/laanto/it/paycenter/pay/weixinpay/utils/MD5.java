package com.laanto.it.paycenter.pay.weixinpay.utils;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 功能：微信支付MD5签名处理核心文件，不需要修改
 */

public class MD5 {
    private static Logger logger = LoggerFactory.getLogger(MD5.class);
    public static final String PARAMETER_NAME_SECRET_KEY = "key";

    /**
     * 微信支付签名
     *
     * @param text          需要签名的字符串
     * @param keyValue      微信支付的PartnerKey
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static String signWeixinPay(String text, String keyValue, String input_charset) {
        String stringSignTemp = text + "&" + PARAMETER_NAME_SECRET_KEY + "=" + keyValue;
        byte[] contentBytes = getContentBytes(stringSignTemp, input_charset);
        if (contentBytes != null) {
            return DigestUtils.md5Hex(contentBytes);
        } else {
            return null;
        }
    }

    /**
     * 微信支付签名确认
     *
     * @param text          需要签名的字符串
     * @param keyValue      密钥
     * @param input_charset 编码格式
     * @param providedSign  签名结果
     * @return 签名结果
     */
    public static boolean verifyWeixinPaySign(String text, String keyValue, String input_charset, String providedSign) {
        String mysign = signWeixinPay(text, keyValue, input_charset).toUpperCase();
        logger.debug("微信支付签名确认：我们的自己产生的签名是：{}", mysign);
        if (StringUtils.isNotBlank(mysign) && mysign.equals(providedSign)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            logger.error("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset, e);
            return null;
        }
    }
}