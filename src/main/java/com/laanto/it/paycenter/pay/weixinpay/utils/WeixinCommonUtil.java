package com.laanto.it.paycenter.pay.weixinpay.utils;

import com.laanto.it.paycenter.pay.weixinpay.constant.WeiXinApiUrl;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeixinCommonUtil {
    private static Logger logger = LoggerFactory.getLogger(WeixinCommonUtil.class);

    private static XppDriver xppDriver = new XppDriver(new XmlFriendlyNameCoder("_-", "_")) {

        @Override
        public HierarchicalStreamWriter createWriter(Writer out) {
            return new PrettyPrintWriter(out, super.getNameCoder()) {
                // 对所有xml节点的转换都增加CDATA标记
                boolean cdata = true;

                @Override
                public void startNode(String name, Class clazz) {
                    super.startNode(name, clazz);
                }

                @Override
                protected void writeText(QuickWriter writer, String text) {
                    if (cdata) {
                        writer.write("<![CDATA[");
                        writer.write(text);
                        writer.write("]]>");
                    } else {
                        writer.write(text);
                    }

                }
            };
        }
    };

    /**
     * 扩展xstream使其支持CDATA
     */
    private static XStream xstream = new XStream(xppDriver);

    /**
     * 发送https请求，并返还json object
     *
     * @param requestUrl    请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr     提交的数据
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static Map<String, String> processHttpsRequestAndReturnMap(String requestUrl, String requestMethod, String outputStr) {
        String response = httpsRequest(requestUrl, requestMethod, outputStr);
        Map<String, String> map = null;
        if (StringUtils.isNotBlank(response)) {
            map = parseXml(response);
        }
        return map;
    }

    /**
     * 发送https请求，并返还json object
     *
     * @param requestUrl    请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr     提交的数据
     * @return String 请求返还结果
     */

    public static String httpsRequest(String requestUrl, String requestMethod, String outputStr) {
        String response = null;
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = {new X509TrustManager() {

                // 检查客户端证书
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                // 检查服务器端证书
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                // 返回受信任的X509证书数组
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(ssf);

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            conn.setRequestMethod(requestMethod);

            // 当outputStr不为null时向输出流写数据
            if (StringUtils.isNotBlank(outputStr)) {
                OutputStream outputStream = conn.getOutputStream();
                // 注意编码格式
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 从输入流读取返回内容
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }

            // 释放资源
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            inputStream = null;
            conn.disconnect();
            response = buffer.toString();
        } catch (ConnectException e) {
            logger.error("微信调用连接超时：{}", e);
        } catch (Exception e) {
            logger.error("https请求异常：{}", e);
        }
        return response;
    }

    /**
     * 解析微信发来的请求（XML）
     *
     * @return
     */
    public static Map<String, Object> parseXml(InputStream inputStream) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(inputStream);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (Element e : elements) {
                map.put(e.getName(), e.getText());
            }
        } catch (DocumentException e) {
            logger.error("DOM4J解析输入流错误", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    /**
     * 解析微信发来的请求（XML）
     *
     * @return
     */
    public static Map<String, String> parseXml(String response) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            Document document = DocumentHelper.parseText(response); // 将字符串转为XML
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (Element e : elements) {
                map.put(e.getName(), e.getText());
            }

        } catch (DocumentException e) {
            logger.error("DOM4J解析微信返回的xml数据错误", e);
        }
        return map;
    }

    /**
     * 把java bean 转化成xml String
     *
     * @param bean
     * @param clazz
     * @return
     */

    public static String beanToXml(Object bean, Class clazz) {
        xstream.alias("xml", clazz);
        return xstream.toXML(bean);
    }

    /**
     * 获取接口访问凭证
     *
     * @param appid     凭证
     * @param appsecret 密钥
     * @return
     */
    public static String getToken(String appid, String appsecret) {
        String accessToken = null;
        String requestUrl = WeiXinApiUrl.GET_ACCESS_TOKEN.replace("APPID", appid).replace("APPSECRET", appsecret);
        logger.debug("调用公众号全局唯一接口调用凭据的url={}", requestUrl);
        String response = httpsRequest(requestUrl, "GET", null);
        if (StringUtils.isNotBlank(response)) {
            try {
                JSONObject jsonObject = JSONObject.fromObject(response);
                accessToken = jsonObject.getString("access_token");
            } catch (JSONException e) {
                // 获取accessToken失败
                logger.error("获取公众号全局唯一接口调用凭据(accessToken)失败,错误原因;{}", response);
            }
        }
        return accessToken;
    }

    public static String getJSApiTicket(String accessToken) {
        String jsapiTicket = null;
        String requestUrl = WeiXinApiUrl.GET_TICKET.replace("ACCESS_TOKEN", accessToken);
        logger.debug("调用公众号用于调用微信JS接口的临时票据的url={}", requestUrl);
        String response = httpsRequest(requestUrl, "GET", null);
        if (StringUtils.isNotBlank(response)) {
            try {
                JSONObject jsonObject = JSONObject.fromObject(response);
                jsapiTicket = jsonObject.getString("ticket");
            } catch (JSONException e) {
                // 获取jsapiTicket失败
                logger.error("获取公众号用于调用微信JS接口的临时票据(jsapiTicket)失败, 错误原因;{}", response);
            }
        }
        return jsapiTicket;
    }
}
