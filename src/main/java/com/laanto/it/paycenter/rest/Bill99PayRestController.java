package com.laanto.it.paycenter.rest;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.laanto.it.paycenter.constant.MediaTypes;
import com.laanto.it.paycenter.domain.WithdrawRequest;
import com.laanto.it.paycenter.pay.bill99pay.config.Bill99PayConfig;
import com.laanto.it.paycenter.pay.bill99pay.pojo.BankRequestBean;
import com.laanto.it.paycenter.pay.bill99pay.pojo.BankResponseBean;
import com.laanto.it.paycenter.pay.bill99pay.pojo.QueryRequestBean;
import com.laanto.it.paycenter.pay.bill99pay.pojo.QueryResponseBean;
import com.laanto.it.paycenter.pay.bill99pay.util.Bill99PayUtils;
import com.laanto.it.paycenter.service.Bill99ErrorCodeService;
import com.laanto.it.paycenter.service.Bill99Service;
import com.laanto.it.paycenter.service.WithdrawRequestService;
import com.laanto.it.paycenter.utils.BeanValidators;
import com.laanto.it.paycenter.vo.Bill99QueryResponseVo;
import com.laanto.it.paycenter.vo.WithdrawRequestVo;

@RestController
@RequestMapping("/rest/withdraw/bill99")
public class Bill99PayRestController {

	private static final Logger logger = LoggerFactory.getLogger(Bill99PayRestController.class);

	@Autowired
	private WithdrawRequestService withdrawRequestService;

	@Autowired
	private Bill99Service bill99Service;

	@Autowired
	private Bill99PayConfig bill99PayConfig;

	@Autowired
	private Validator validator;
	
	@Autowired
	private Bill99ErrorCodeService errorCodeService;

	/**
	 * 快钱提现到银行卡
	 *
	 * @param withdrawRequestVo
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/tobank", method = RequestMethod.POST, consumes = MediaTypes.JSON_UTF_8, produces = MediaTypes.JSON_UTF_8)
	public Map<String, Object> bill99WithdrawCash(@RequestBody WithdrawRequestVo withdrawRequestVo,
			HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, String> constraintViolations = BeanValidators.validate(validator, withdrawRequestVo);
		if (constraintViolations != null && !constraintViolations.isEmpty()) {
			String constraintViolationsMsg = String.format("参数错误：%s", constraintViolations.toString());
			resultMap.put("errmsg", constraintViolationsMsg);
			logger.error(constraintViolationsMsg);
			return resultMap;
		} else {
			WithdrawRequest withdrawRequest = parseWithdrawRequestVo(withdrawRequestVo);
			withdrawRequestService.save(withdrawRequest);
			BankResponseBean bankResponseBean;
			try {
				bankResponseBean = withdraw(withdrawRequest);
			} catch (Exception e) {
				String message = String.format("订单{%s}提现失败：%s", withdrawRequestVo.getOrderId(), e.getMessage());
				resultMap.put("errmsg", message);
				logger.error(message, e);
				return resultMap;

			}
			resultMap.put("result", bankResponseBean);
			if (bankResponseBean.isResultFlag()) {
				logger.info(String.format("订单{%s}提现提交成功", withdrawRequestVo.getOrderId()));
			} else {
				String message = String.format("订单{%s}提现提交失败：%s", withdrawRequestVo.getOrderId(),
						errorCodeService.getErrorMessage(bankResponseBean.getFailureCause()));
				resultMap.put("errmsg", message);
				logger.error(message);
			}
			return resultMap;
		}
	}

	/**
	 * 查询提现进度
	 *
	 * @param payRequestVo
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryDeal/{orderId}", method = RequestMethod.GET)
	public Map<String, Object> bill99QueryDeal(@PathVariable String orderId, HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<>();
		if (StringUtils.isBlank(orderId)) {
			String errorMessage = "查询订单号不能为空";
			resultMap.put("errmsg", errorMessage);
			logger.error(errorMessage);
			return resultMap;
		}
		QueryResponseBean[] queryResponseBeans;
		try {
			queryResponseBeans = queryDeal(orderId);
		} catch (Exception e) {
			String message = String.format("订单{%s}查询失败：%s", orderId, e.getMessage());
			resultMap.put("errmsg", message);
			logger.error(message, e);
			return resultMap;
		}
		
		if(queryResponseBeans == null || queryResponseBeans.length <= 0) {
			resultMap.put("result", null);
			return resultMap;
		} else {
			QueryResponseBean queryResponseBean = queryResponseBeans[0];
			Bill99QueryResponseVo queryResponseVo = parseQueryResponseBean(queryResponseBean);
			resultMap.put("result", queryResponseVo);
			if (queryResponseVo.isResultFlag()) {
				logger.info(String.format("订单{%s}查询成功", orderId));
			} else {
				String message = String.format("订单{%s}查询失败：%s", orderId,
						errorCodeService.getErrorMessage(queryResponseVo.getFailureCause()));
				resultMap.put("errmsg", message);
				logger.error(message);
			}
			return resultMap;
		}		
	}

	private WithdrawRequest parseWithdrawRequestVo(WithdrawRequestVo withdrawRequestVo) {
		WithdrawRequest withdrawRequest = new WithdrawRequest();
		withdrawRequest.setAmount(withdrawRequestVo.getAmount());
		withdrawRequest.setBankCardNumber(withdrawRequestVo.getBankCardNumber());
		withdrawRequest.setBankName(withdrawRequestVo.getBankName());
		withdrawRequest.setCreditName(withdrawRequestVo.getCreditName());
		withdrawRequest.setDescription(withdrawRequestVo.getDescription());
		withdrawRequest.setKaihuhang(withdrawRequestVo.getKaihuhang());
		withdrawRequest.setOrderId(withdrawRequestVo.getOrderId());
		withdrawRequest.setProvinceCity(withdrawRequestVo.getProvinceCity());
		return withdrawRequest;
	}

	private BankResponseBean withdraw(WithdrawRequest withdrawRequest) throws Exception {
		BankRequestBean[] bankPayRequest = { parseWithdrawRequest(withdrawRequest) };
		BankResponseBean[] bankPayResponse = bill99Service.bankPay(bankPayRequest, bill99PayConfig.getMerchantId(),
				bill99PayConfig.getMerchantIP());
		return bankPayResponse[0];
	}

	private BankRequestBean parseWithdrawRequest(WithdrawRequest withdrawRequest) {
		BankRequestBean bankRequestBean = new BankRequestBean();
		bankRequestBean.setAmount(withdrawRequest.getAmount().doubleValue());
		bankRequestBean.setBankCardNumber(withdrawRequest.getBankCardNumber());
		bankRequestBean.setBankName(withdrawRequest.getBankName());
		bankRequestBean.setCreditName(withdrawRequest.getCreditName());
		bankRequestBean.setDescription(withdrawRequest.getDescription());
		bankRequestBean.setKaihuhang(withdrawRequest.getKaihuhang());
		bankRequestBean.setMac(getMacString(withdrawRequest));
		bankRequestBean.setOrderId(withdrawRequest.getOrderId());
		bankRequestBean.setProvince_city(withdrawRequest.getProvinceCity());
		return bankRequestBean;
	}

	private String getMacString(WithdrawRequest withdrawRequest) {
		String macVal = String.format("%s%s%s%s", withdrawRequest.getBankCardNumber(), withdrawRequest.getAmount()
				.toString(), withdrawRequest.getOrderId(), bill99PayConfig.getKey());
		return Bill99PayUtils.md5Hex(Bill99PayUtils.getContentBytes(macVal, "gb2312")).toUpperCase();
	}

	private QueryResponseBean[] queryDeal(String orderId) throws Exception {
		QueryRequestBean queryRequestBean = new QueryRequestBean();
		queryRequestBean.setQueryType("bankPay");
		queryRequestBean.setDealId("0");
		queryRequestBean.setOrderId(orderId);
		return bill99Service.queryDeal(queryRequestBean, bill99PayConfig.getMerchantId(),
				bill99PayConfig.getMerchantIP());
	}
	
	private Bill99QueryResponseVo parseQueryResponseBean(QueryResponseBean queryResponseBean) {
		Bill99QueryResponseVo bill99QueryResponseVo = new Bill99QueryResponseVo();
		bill99QueryResponseVo.setAmount(queryResponseBean.getAmount());
		bill99QueryResponseVo.setDealFee(queryResponseBean.getDealFee());
		bill99QueryResponseVo.setDealId(queryResponseBean.getDealId());
		bill99QueryResponseVo.setDealStatus(parseQueryResultCode(queryResponseBean.getDealStatus()));
		bill99QueryResponseVo.setFailureCause(queryResponseBean.getFailureCause());
		bill99QueryResponseVo.setOrderId(queryResponseBean.getOrderId());
		bill99QueryResponseVo.setResultFlag(queryResponseBean.isResultFlag());
		return bill99QueryResponseVo;
	}
	
	private String parseQueryResultCode(String code) {
		String resultMessage;
		if(StringUtils.isEmpty(code)) {
			resultMessage = "";
		} else if(code.equals("111")) {
			resultMessage = "成功";
		} else if(code.equals("101")) {
			resultMessage = "进行中";
		} else if(code.equals("112")) {
			resultMessage = "失败";
		} else if(code.equals("114")) {
			resultMessage = "已退款";
		} else {
			resultMessage = code;
		}
		return resultMessage;
	}
}
