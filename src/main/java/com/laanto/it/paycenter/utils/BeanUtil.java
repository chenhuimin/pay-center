package com.laanto.it.paycenter.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BeanUtil {
	private static final Logger logger = LoggerFactory.getLogger(BeanUtil.class);

	/**
	 * Bean --> Map 1: 利用Introspector和PropertyDescriptor 将Bean --> Map
	 * 
	 * @param obj
	 * @return
	 */
	public static Map<String, Object> transferBean2Map(Object obj) {
		if (obj == null) {
			return null;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor property : propertyDescriptors) {
				String key = property.getName();
				if (!key.equals("class")) {
					Method getter = property.getReadMethod();
					Object value = getter.invoke(obj);
					map.put(key, value);
				}

			}
		} catch (Exception e) {
			logger.error("Transer bean to map eror", e);
		}
		return map;
	}

}
