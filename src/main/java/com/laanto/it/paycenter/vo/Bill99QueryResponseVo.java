package com.laanto.it.paycenter.vo;

import java.io.Serializable;

public class Bill99QueryResponseVo implements Serializable{
	private static final long serialVersionUID = 7329809451960979368L;

	private double amount;

    private double dealFee;

    private java.lang.String dealId;

    private java.lang.String dealStatus;

    private java.lang.String failureCause;

    private java.lang.String orderId;

    private boolean resultFlag;

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getDealFee() {
		return dealFee;
	}

	public void setDealFee(double dealFee) {
		this.dealFee = dealFee;
	}

	public java.lang.String getDealId() {
		return dealId;
	}

	public void setDealId(java.lang.String dealId) {
		this.dealId = dealId;
	}

	public java.lang.String getDealStatus() {
		return dealStatus;
	}

	public void setDealStatus(java.lang.String dealStatus) {
		this.dealStatus = dealStatus;
	}

	public java.lang.String getFailureCause() {
		return failureCause;
	}

	public void setFailureCause(java.lang.String failureCause) {
		this.failureCause = failureCause;
	}

	public java.lang.String getOrderId() {
		return orderId;
	}

	public void setOrderId(java.lang.String orderId) {
		this.orderId = orderId;
	}

	public boolean isResultFlag() {
		return resultFlag;
	}

	public void setResultFlag(boolean resultFlag) {
		this.resultFlag = resultFlag;
	}
    
}
