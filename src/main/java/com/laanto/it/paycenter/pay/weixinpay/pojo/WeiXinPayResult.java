package com.laanto.it.paycenter.pay.weixinpay.pojo;

import java.io.Serializable;

public class WeiXinPayResult implements Serializable {

	private static final long serialVersionUID = -8180981194933603327L;

	private String return_code;

	private String return_msg;

	public String getReturn_code() {
		return return_code;
	}

	public void setReturn_code(String return_code) {
		this.return_code = return_code;
	}

	public String getReturn_msg() {
		return return_msg;
	}

	public void setReturn_msg(String return_msg) {
		this.return_msg = return_msg;
	}

	public WeiXinPayResult() {

	}

	public WeiXinPayResult(String return_code, String return_msg) {
		this.return_code = return_code;
		this.return_msg = return_msg;
	}

}
