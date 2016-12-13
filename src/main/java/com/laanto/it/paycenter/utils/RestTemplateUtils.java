package com.laanto.it.paycenter.utils;

import java.nio.charset.Charset;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestTemplateUtils<T> {

    public static HttpHeaders getHeaderWithApplicationJsonAndUTF8() {
        MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(),
                Charset.forName("utf8"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        return headers;
    }

    public static HttpHeaders getHeaderWithApplicationXmlAndUTF8() {
        MediaType contentType = new MediaType(MediaType.APPLICATION_XML.getType(), MediaType.APPLICATION_XML.getSubtype(),
                Charset.forName("utf8"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        return headers;
    }

    public static <T> String getJsonString(T t) {
        ObjectMapper objectMapper = new ObjectMapper();
        String userMapJson = null;
        try {
            userMapJson = objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return userMapJson;
    }

}
