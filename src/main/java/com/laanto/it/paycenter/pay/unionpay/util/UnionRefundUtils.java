package com.laanto.it.paycenter.pay.unionpay.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.laanto.it.paycenter.pay.unionpay.config.UnionpayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 名称：支付工具类 功能：工具类，可以生成付款表单等 类属性：公共类 版本：1.0 日期：2011-03-11 作者：中国银联UPOP团队 版权：中国银联
 * 说明：以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。该代码仅供参考。
 */
@Component
public class UnionRefundUtils {
    private static final Logger logger = LoggerFactory.getLogger(UnionRefundUtils.class);

    public String createBackStrForBackTrans(Map<String, String> map, UnionpayConfig config) {
        map.put("signature", signMap(map, config));
        map.put("signMethod", config.getSignType());
        return joinMapValueBySpecial(map, '&', config.getCharset());
    }

    public String createBackStr(Map<String, String> map, UnionpayConfig config) {
        map.put("signature", signMap(map, config));
        map.put("signMethod", config.getSignType());
        return joinMapValue(map, '&');
    }

    /**
     * 查询验证签名
     *
     * @param map
     * @return 0:验证失败 1验证成功 2没有签名信息（报文格式不对）
     */
    public int checkSecurity(Map<String, String> map, UnionpayConfig config) {
        if ("".equals(map.get("signature"))) {
            return 2;
        }
        String signature = map.get("signature");
        boolean isValid = false;
        map.remove("signature");
        map.remove("signMethod");
        String strBeforeMd5 = joinMapValue(map, '&') + UnionpayUtils.md5(config.getSecurityKey(), config.getSignType(), config.getCharset());
        isValid = signature.equals(UnionpayUtils.md5(strBeforeMd5, config.getSignType(), config.getCharset()));
        return (isValid ? 1 : 0);
    }


    private String signMap(Map<String, String> map, UnionpayConfig config) {
        String strBeforeMd5 = joinMapValue(map, '&') + UnionpayUtils.md5(config.getSecurityKey(), config.getSignType(), config.getCharset());
        return UnionpayUtils.md5(strBeforeMd5, config.getSignType(), config.getCharset());
    }


    public static Map<String, String> getResArr(String str) {
        Map<String, String> resMap = new TreeMap<>();
        String regex = "(.*?cupReserved\\=)(\\{[^}]+\\})(.*)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);

        String reserved = "";
        if (matcher.find()) {
            reserved = matcher.group(2);
        }

        String result = str.replaceFirst(regex, "$1$3");
        String[] resArr = result.split("&");
        for (int i = 0; i < resArr.length; i++) {
            if ("cupReserved=".equals(resArr[i])) {
                resArr[i] += reserved;
            }
            String[] keyValue = resArr[i].split("=");
            resMap.put(keyValue[0], keyValue.length >= 2 ? resArr[i].substring(keyValue[0].length() + 1) : "");
        }
        return resMap;
    }

    private String joinMapValue(Map<String, String> map, char connector) {
        StringBuffer b = new StringBuffer();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            b.append(entry.getKey());
            b.append('=');
            if (entry.getValue() != null) {
                b.append(entry.getValue());
            }
            b.append(connector);
        }
        return b.toString();
    }

    private String joinMapValueBySpecial(Map<String, String> map, char connector, String charset) {
        StringBuffer b = new StringBuffer();
        for (Map.Entry<String, String> entry : map.entrySet()) {

            b.append(entry.getKey());
            b.append('=');
            if (entry.getValue() != null) {
                try {
                    b.append(java.net.URLEncoder.encode(entry.getValue(), charset));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            b.append(connector);
        }
        return b.toString();
    }

    /**
     * 查询方法
     *
     * @return
     */
    public String doRefund(Map<String, String> map, UnionpayConfig config) {
        String req = createBackStrForBackTrans(map, config);
        String result = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            URL url = new URL(config.getBsPayUrl());
            URLConnection con = url.openConnection();
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            out = new BufferedOutputStream(con.getOutputStream());
            byte outBuf[] = req.getBytes(config.getCharset());
            out.write(outBuf);
            out.close();
            in = new BufferedInputStream(con.getInputStream());
            result = readByteStream(in, config);
        } catch (Exception ex) {
            logger.error("执行银联退款遇到错误", ex);
            return "";
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        if (result == null) {
            return "";
        } else {
            return result;
        }
    }

    private String readByteStream(BufferedInputStream in, UnionpayConfig config) throws IOException {
        LinkedList<Mybuf> bufList = new LinkedList<Mybuf>();
        int size = 0;
        byte buf[];
        do {
            buf = new byte[128];
            int num = in.read(buf);
            if (num == -1) {
                break;
            }
            size += num;
            bufList.add(new Mybuf(buf, num));
        } while (true);
        buf = new byte[size];
        int pos = 0;
        for (ListIterator<Mybuf> p = bufList.listIterator(); p.hasNext(); ) {
            Mybuf b = p.next();
            for (int i = 0; i < b.size; ) {
                buf[pos] = b.buf[i];
                i++;
                pos++;
            }
        }
        return new String(buf, config.getCharset());
    }

    public String doQueryRefund(Map<String, String> queryRefundParams, UnionpayConfig config) {
        String req = createBackStr(queryRefundParams, config);
        String result = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            URL url = new URL(config.getQueryUrl());
            URLConnection con = url.openConnection();
            // if (con instanceof HttpsURLConnection) {
            // ((HttpsURLConnection) con).setHostnameVerifier(new HostnameVerifier() {
            // @Override
            // public boolean verify(String hostname, SSLSession session) {
            // return true;
            // }
            // });
            // }
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            out = new BufferedOutputStream(con.getOutputStream());
            byte outBuf[] = req.getBytes(config.getCharset());
            out.write(outBuf);
            out.close();
            in = new BufferedInputStream(con.getInputStream());
            result = readByteStream(in, config);
        } catch (Exception ex) {
            logger.error("执行银联退款查询遇到错误", ex);
            return "";
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        if (result == null) {
            return "";
        } else {
            return result;
        }
    }
}

class Mybuf {

    public byte buf[];
    public int size;


    public Mybuf(byte b[], int s) {
        buf = b;
        size = s;
    }
}
