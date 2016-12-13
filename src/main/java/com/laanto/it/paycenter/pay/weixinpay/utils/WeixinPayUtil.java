package com.laanto.it.paycenter.pay.weixinpay.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WeixinPayUtil {
	private static final Logger logger = LoggerFactory.getLogger(WeixinPayUtil.class);

	/**
	 * 除去数组中的空值和签名参数
	 * 
	 * @param sArray 签名参数组
	 * @return 去掉空值与签名参数后的新签名参数组
	 */
	public static Map<String, Object> paraFilter(Map<String, Object> sArray) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (sArray == null || sArray.size() <= 0) {
			return sArray;
		}
		for (String key : sArray.keySet()) {
			Object value = sArray.get(key);
			if (value == null || ((String) value).equals("") || key.equalsIgnoreCase("sign")) {
				continue;
			}
			result.put(key, value);
		}
		return result;
	}

	/**
	 * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
	 * 
	 * @param params 需要排序并参与字符拼接的参数组
	 * @return 拼接后字符串
	 */
	public static String createLinkString(Map<String, Object> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		String stringA = "";
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			Object value = params.get(key);
			if (i == keys.size() - 1) {
				stringA = stringA + key + "=" + value;

			} else {
				stringA = stringA + key + "=" + value + "&";
			}

		}
		return stringA;

	}

	// public static void main(String[] args) {
	// UnifiedOrder test = new UnifiedOrder();
	// test.setAppid("wxd930ea5d5a258f4f");
	// test.setMch_id("10000100");
	// test.setBody("测试");
	// test.setTotal_fee(100);
	// String xml = WeixinPayUtil.beanToXml(test, UnifiedOrder.class);
	// WeixinPayUtil.parseXml(new ByteArrayInputStream(xml.getBytes()));
	// System.out.println();
	//
	// }

}
