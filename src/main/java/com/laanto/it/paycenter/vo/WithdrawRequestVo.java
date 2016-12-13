package com.laanto.it.paycenter.vo;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class WithdrawRequestVo implements Serializable {
	private static final long serialVersionUID = 161102352648326188L;

	private BigDecimal amount;

    private String bankCardNumber;

    private String bankName;

    private String creditName;

    private String description;

    private String kaihuhang;

    private String orderId;

    private String provinceCity;

    @NotNull(message = "提现金额不能为空")
    @Min(value = 0, message = "提现金额必须大于或等于0")
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@NotNull(message = "银行卡号不能为空")
	public String getBankCardNumber() {
		return bankCardNumber;
	}

	public void setBankCardNumber(String bankCardNumber) {
		this.bankCardNumber = bankCardNumber;
	}

	@NotNull(message = "银行名称不能为空")
	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	@NotNull(message = "收款人姓名不能为空")
	public String getCreditName() {
		return creditName;
	}

	public void setCreditName(String creditName) {
		this.creditName = creditName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@NotNull(message = "开户行不能为空")
	public String getKaihuhang() {
		return kaihuhang;
	}

	public void setKaihuhang(String kaihuhang) {
		this.kaihuhang = kaihuhang;
	}

	@NotNull(message = "订单号不能为空")
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@NotNull(message = "城市不能为空")
	public String getProvinceCity() {
		return provinceCity;
	}

	public void setProvinceCity(String provinceCity) {
		this.provinceCity = provinceCity;
	}
    
}
