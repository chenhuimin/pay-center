package com.laanto.it.paycenter.utils;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class WebUtil {
    public static String getBaseUrlWithContextPath(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        String contextPath = "/".equals(request.getContextPath()) ? "" : request.getContextPath();
        if (request.getServerPort() != 80) {
            baseUrl = baseUrl + ":" + request.getServerPort() + contextPath;
        } else {
            baseUrl = baseUrl + contextPath;
        }
        return baseUrl;
    }

    public static String getBaseUrlWithoutContextPath(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort() != 80) {
            baseUrl = baseUrl + ":" + request.getServerPort();
        }
        return baseUrl;
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isNotBlank(ipAddress) && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.split(",")[0];
        } else {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;

    }

    /**
     * 当前网页的URL，不包含#及其后面部分
     */
    public static String getCurrentPageUrl(HttpServletRequest request) {
        String url = getBaseUrlWithoutContextPath(request) + request.getRequestURI();
        if (StringUtils.isNotBlank(request.getQueryString())) {
            url = url + "?" + request.getQueryString();
        }
        url = url.split("#")[0];
        return url;
    }

    public static String getAbsoluteUrl(HttpServletRequest request, String relateiveUrl) {
        String baseUrl = getBaseUrlWithContextPath(request);
        StringBuilder sb = new StringBuilder();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        sb.append(baseUrl);
        if (!relateiveUrl.startsWith("/")) {
            sb.append("/");
        }
        sb.append(relateiveUrl);
        return sb.toString();
    }

    public static String getUserIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isNotBlank(ipAddress) && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.split(",")[0];
        } else {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
