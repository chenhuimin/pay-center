package com.laanto.it.paycenter.service;

import com.laanto.it.paycenter.pay.weixinpay.utils.WeixinCommonUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@Service
public class WeixinTokenService {

    /**
     * 获取授权Token
     *
     * @param appid
     * @param secret
     * @return
     */
    @Cacheable(value = "wx_access_token", key = "#appid")
    public String getToken(String appid, String secret) {
        return WeixinCommonUtil.getToken(appid, secret);
    }

    /**
     * 获取JSApiTicket
     *
     * @param accessToken
     * @return
     */
    @Cacheable(value = "wx_jsapi_ticket", key = "#accessToken")
    public String getJSApiTicket(String accessToken) {
        return WeixinCommonUtil.getJSApiTicket(accessToken);
    }

}
