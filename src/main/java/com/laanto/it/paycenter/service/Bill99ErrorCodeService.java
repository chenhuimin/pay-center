package com.laanto.it.paycenter.service;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.stereotype.Service;

@Service
public class Bill99ErrorCodeService {
	private Properties properties = new Properties();
	private static final String errorCodeFile = "bill99/error-code.properties";
	public static final String ENCODE = "UTF-8";
	
	public String getErrorMessage(String errorCode){
		if(properties.isEmpty()) {
			initProperties();
		} 
		
		return properties.getProperty(errorCode);
	}
	
	private void initProperties(){
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(errorCodeFile);
		
		try {
			if(inputStream != null) {
				properties.load(inputStream);
			}
		} catch (IOException e) {
			String message = String.format("加载资源文件{%s}失败", errorCodeFile);
			throw new RuntimeException(message);
		} finally {
			close(inputStream);
		}
	} 
	
	private void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
