package com.laanto.it.paycenter.pay.bill99pay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/* *
 *类名：Bill99PayConfig
 *功能：基础配置类
 */
@Component
public class Bill99PayConfig {

    /**
     * 密钥
     */
    @Value("${bill99.merchant.key}")
    private String key;
    
    /**
     * 用户编号
     */
    @Value("${bill99.merchant.id}")
    private String merchantId;
    
    /**
     * 付款程序所在服务器的 IP 地址
     */
    @Value("${bill99.merchant.ip}")
    private String merchantIP;
    
    /**
     * 快钱接口地址
     */
    @Value("${bill99.port_address}")
    private String portAddress;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getMerchantIP() {
		return merchantIP;
	}

	public void setMerchantIP(String merchantIP) {
		this.merchantIP = merchantIP;
	}

	public String getPortAddress() {
		return portAddress;
	}

	public void setPortAddress(String portAddress) {
		this.portAddress = portAddress;
	}
    
}
