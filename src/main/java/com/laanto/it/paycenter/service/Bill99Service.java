package com.laanto.it.paycenter.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.laanto.it.paycenter.pay.bill99pay.config.Bill99PayConfig;
import com.laanto.it.paycenter.pay.bill99pay.pojo.BankRequestBean;
import com.laanto.it.paycenter.pay.bill99pay.pojo.BankResponseBean;
import com.laanto.it.paycenter.pay.bill99pay.pojo.QueryRequestBean;
import com.laanto.it.paycenter.pay.bill99pay.pojo.QueryResponseBean;
import com.laanto.it.paycenter.pay.bill99pay.service.BatchPayServiceLocator;

@Service
@Transactional(readOnly = true)
public class Bill99Service {

	private final BatchPayServiceLocator locator;

	@Autowired
	private Bill99PayConfig bill99PayConfig;

	public Bill99Service() {
		locator = new BatchPayServiceLocator();
	}

	public BankResponseBean[] bankPay(BankRequestBean[] request, String merchant_id, String merchant_ip) throws Exception {
		BankResponseBean[] responseBeans = new BankResponseBean[1];
		try {
			responseBeans = locator.getBatchPayWS(new URL(bill99PayConfig.getPortAddress())).bankPay(request, merchant_id, merchant_ip);
		} catch (RemoteException e) {
			throw new Exception(String.format("快钱付款到银行卡失败:%s", e.getMessage()), e);
		} catch (MalformedURLException e) {
			throw new Exception(String.format("PortAddress{%s}不是有效的URL", bill99PayConfig.getPortAddress()), e);
		} catch (ServiceException e) {
			throw new Exception("快钱服务连接失败", e);
		}
		return responseBeans;
	}

	public QueryResponseBean[] queryDeal(QueryRequestBean request, String merchant_id, String merchant_ip) throws Exception {
		QueryResponseBean[] responseBeans = new QueryResponseBean[1];
		try {
			responseBeans = locator.getBatchPayWS(new URL(bill99PayConfig.getPortAddress())).queryDeal(request,
					merchant_id, merchant_ip);
		} catch (RemoteException e) {
			throw new Exception(String.format("快钱查询付款订单失败:%s", e.getMessage()), e);
		} catch (MalformedURLException e) {
			throw new Exception(String.format("PortAddress{%s}不是有效的URL", bill99PayConfig.getPortAddress()), e);
		} catch (ServiceException e) {
			throw new Exception("快钱服务连接失败", e);
		}
		return responseBeans;
	}
}
