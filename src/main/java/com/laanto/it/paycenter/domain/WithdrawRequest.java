package com.laanto.it.paycenter.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

@Entity
@Table(name = "t_withdraw_req")
@Where(clause = "deleted='false'")
public class WithdrawRequest extends BaseDomain {
	private static final long serialVersionUID = -6951067443068310046L;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "bank_card_no", nullable = false)
    private java.lang.String bankCardNumber;

	@Column(name = "bank_name", nullable = false)
    private java.lang.String bankName;

	@Column(name = "credit_name", nullable = false)
    private java.lang.String creditName;

	@Column(name = "description")
    private java.lang.String description;

	@Column(name = "kaihuhang", nullable = false)
    private java.lang.String kaihuhang;

	@Column(name = "order_id", nullable = false)
    private java.lang.String orderId;

	@Column(name = "province_city", nullable = false)
    private java.lang.String provinceCity;

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public java.lang.String getBankCardNumber() {
		return bankCardNumber;
	}

	public void setBankCardNumber(java.lang.String bankCardNumber) {
		this.bankCardNumber = bankCardNumber;
	}

	public java.lang.String getBankName() {
		return bankName;
	}

	public void setBankName(java.lang.String bankName) {
		this.bankName = bankName;
	}

	public java.lang.String getCreditName() {
		return creditName;
	}

	public void setCreditName(java.lang.String creditName) {
		this.creditName = creditName;
	}

	public java.lang.String getDescription() {
		return description;
	}

	public void setDescription(java.lang.String description) {
		this.description = description;
	}

	public java.lang.String getKaihuhang() {
		return kaihuhang;
	}

	public void setKaihuhang(java.lang.String kaihuhang) {
		this.kaihuhang = kaihuhang;
	}

	public java.lang.String getOrderId() {
		return orderId;
	}

	public void setOrderId(java.lang.String orderId) {
		this.orderId = orderId;
	}

	public java.lang.String getProvinceCity() {
		return provinceCity;
	}

	public void setProvinceCity(java.lang.String provinceCity) {
		this.provinceCity = provinceCity;
	}
}
