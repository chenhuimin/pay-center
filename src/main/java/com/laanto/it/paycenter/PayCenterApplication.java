package com.laanto.it.paycenter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.validation.Validator;

import org.apache.http.Header;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ServletComponentScan
@ComponentScan({ "com.laanto.it", "com.lannto.it" })
public class PayCenterApplication {

  @Value("${weixinpay.mch_id}")
  private String mchId;

  public static void main(String[] args) {
    SpringApplication.run(PayCenterApplication.class, args);
  }

  // JSR303 Bean Validator
  @Bean
  public Validator localValidatorFactoryBean() {
    return new LocalValidatorFactoryBean();
  }

  @Bean
  public RestTemplate getRestTemplate() {
    FileInputStream instream = null;
    SSLContext sslcontext = null;
    Set<KeyManager> keymanagers = new LinkedHashSet<>();
    Set<TrustManager> trustmanagers = new LinkedHashSet<>();
    try {
      KeyStore keyStore = KeyStore.getInstance("PKCS12");
      ClassLoader classLoader = getClass().getClassLoader();
      instream = new FileInputStream(new File(classLoader.getResource("wxpay_cert/apiclient_cert.p12").getFile()));
      keyStore.load(instream, mchId.trim().toCharArray());
      final KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmfactory.init(keyStore, mchId.trim().toCharArray());
      final KeyManager[] kms = kmfactory.getKeyManagers();
      if (kms != null) {
        for (final KeyManager km : kms) {
          keymanagers.add(km);
        }
      }
      trustmanagers.add(new HttpsTrustManager());
      KeyManager[] km = keymanagers.toArray(new KeyManager[keymanagers.size()]);
      TrustManager[] tm = trustmanagers.toArray(new TrustManager[trustmanagers.size()]);
      sslcontext = SSLContexts.custom().build();
      sslcontext.init(km, tm, new SecureRandom());
    } catch (KeyStoreException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (CertificateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (UnrecoverableKeyException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    } finally {
      if (instream != null) {
        try {
          instream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }

      }
    }
    SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext,
        SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    HttpClientBuilder httpClientBuilder = HttpClients.custom();
    httpClientBuilder.setSSLSocketFactory(factory);
    // PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(30, TimeUnit.SECONDS);
    // pollingConnectionManager.setMaxTotal(1000);
    // // 同路由的并发数
    // pollingConnectionManager.setDefaultMaxPerRoute(1000);
    // httpClientBuilder.setConnectionManager(pollingConnectionManager);
    // 重试次数，默认是3次，没有开启
    httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(2, true));
    // 保持长连接配置，需要在头添加Keep-Alive
    httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());

    List<Header> headers = new ArrayList<>();
    headers.add(new BasicHeader("User-Agent",
        "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.16 Safari/537.36"));
    headers.add(new BasicHeader("Accept-Encoding", "gzip,deflate"));
    headers.add(new BasicHeader("Accept-Language", "zh-CN"));
    headers.add(new BasicHeader("Connection", "Keep-Alive"));
    httpClientBuilder.setDefaultHeaders(headers);
    CloseableHttpClient httpClient = httpClientBuilder.build();
    if (httpClient != null) {
      // httpClient连接配置，底层是配置RequestConfig
      HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
      // 连接超时
      clientHttpRequestFactory.setConnectTimeout(60 * 1000);
      // 数据读取超时时间，即SocketTimeout
      clientHttpRequestFactory.setReadTimeout(5 * 60 * 1000);
      // 连接不够用的等待时间，不宜过长，必须设置，比如连接不够用时，时间过长将是灾难性的
      clientHttpRequestFactory.setConnectionRequestTimeout(5000);
      // 缓冲请求数据，默认值是true。通过POST或者PUT大量发送数据时，建议将此属性更改为false，以免耗尽内存。
      // clientHttpRequestFactory.setBufferRequestBody(false);
      // 添加内容转换器
      List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
      messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
      messageConverters.add(new MappingJackson2HttpMessageConverter());
      messageConverters.add(new FormHttpMessageConverter());
      messageConverters.add(new MappingJackson2XmlHttpMessageConverter());

      RestTemplate restTemplate = new RestTemplate(messageConverters);
      restTemplate.setRequestFactory(clientHttpRequestFactory);
      restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
      return restTemplate;
    } else {
      return null;
    }

  }

  public static class HttpsTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[] {};
    }

  }
}
