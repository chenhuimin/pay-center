package com.laanto.it.paycenter.pay.unionpay.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.laanto.it.paycenter.pay.unionpay.config.UnionpayConfig;
import org.apache.commons.lang3.StringUtils;

import static com.sun.corba.se.spi.activation.IIOP_CLEAR_TEXT.value;

/**
 * 名称：支付工具类 功能：工具类，可以生成付款表单等 类属性：公共类 版本：1.0 日期：2011-03-11 作者：中国银联UPOP团队 版权：中国银联
 * 说明：以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。该代码仅供参考。
 */
public class UnionpayUtils {

    /**
     * 生成银联支付的参数列表
     *
     * @param parameters
     * @param config
     * @return
     */
    public static Map<String, String> getParamMap(Map<String, String> parameters, UnionpayConfig config) {
        parameters.put("signature", signMap(parameters, config));
        parameters.put("signMethod", config.getSignType());
        return parameters;
    }

    /**
     * 生成发送银联报文页面
     *
     * @param parameters
     * @param config
     * @return
     */
    public static String createPayHtml(Map<String, String> parameters, UnionpayConfig config) {
        parameters.put("signature", signMap(parameters, config));
        parameters.put("signMethod", config.getSignType());
        String payHtml = generateAutoSubmitForm(config.getPayUrl(), parameters);
        return payHtml;
    }

    public static String createPayForm(Map<String, String> parameters, UnionpayConfig config) {
        parameters.put("signature", signMap(parameters, config));
        parameters.put("signMethod", config.getSignType());
        StringBuilder payForm = new StringBuilder();
        payForm.append("<form id=\"pay_form\" name=\"pay_form\" action=\"").append(config.getPayUrl()).append("\" method=\"post\" target=\"_blank\">\n");
        for (String key : parameters.keySet()) {
            payForm.append("<input type=\"hidden\" name=\"" + key + "\" id=\"" + key + "\" value=\"" + parameters.get(key) + "\"/>\n");
        }
        payForm.append("</form>");
        return payForm.toString();
    }

    /**
     * 生成加密钥
     *
     * @param parameters
     * @return
     */
    public static String signMap(Map<String, String> parameters, UnionpayConfig config) {
        String strBeforeMd5 = joinMapValue(parameters, '&') + md5(config.getSecurityKey(), config.getSignType(), config.getCharset());
        return md5(strBeforeMd5, config.getSignType(), config.getCharset());
    }

    public static String joinMapValue(Map<String, String> parameters, char connector) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            b.append(entry.getKey());
            b.append('=');
            if (entry.getValue() != null) {
                b.append(entry.getValue());
            }
            b.append(connector);
        }
        return b.toString();
    }

    /**
     * get the md5 hash of a string
     *
     * @param str
     * @return
     */
    public static String md5(String str, String signType, String charset) {

        if (str == null) {
            return null;
        }

        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance(signType);
            messageDigest.reset();
            messageDigest.update(str.getBytes(charset));
        } catch (NoSuchAlgorithmException e) {
            return str;
        } catch (UnsupportedEncodingException e) {
            return str;
        }

        byte[] byteArray = messageDigest.digest();

        StringBuilder md5StrBuff = new StringBuilder();

        for (byte element : byteArray) {
            if (Integer.toHexString(0xFF & element).length() == 1) {
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & element));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & element));
            }
        }

        return md5StrBuff.toString();
    }

    /**
     * Generate an form, auto submit data to the given <code>actionUrl</code>
     *
     * @param actionUrl
     * @param parameters
     * @return
     */
    public static String generateAutoSubmitForm(String actionUrl, Map<String, String> parameters) {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>\n<html>\n<head>\n<meta charset=\"utf-8\">\n");
        html.append("<script language=\"javascript\">window.onload=function(){document.pay_form.submit();}</script>\n</head>\n");
        html.append("<body>");
        html.append("<form id=\"pay_form\" name=\"pay_form\" action=\"").append(actionUrl).append("\" method=\"post\">\n");

        for (String key : parameters.keySet()) {
            html.append("<input type=\"hidden\" name=\"" + key + "\" id=\"" + key + "\" value=\"" + parameters.get(key) + "\">\n");
        }
        html.append("</form>\n");
        html.append("</body>\n</html>");
        return html.toString();
    }

    /**
     * 验证签名
     *
     * @param parameters
     * @param signature
     * @return
     */

    public static Boolean checkSign(Map<String, String> parameters, String signature, UnionpayConfig config) {
        if (StringUtils.isBlank(signature)) {
            return false;
        }
        String strBeforeMd5 = joinMapValue(parameters, '&') + md5(config.getSecurityKey(), config.getSignType(), config.getCharset());
        return signature.equals(md5(strBeforeMd5, config.getSignType(), config.getCharset()));

    }

}
