package com.laanto.it.paycenter.rest;

import com.laanto.it.paycenter.constant.MediaTypes;
import com.laanto.it.paycenter.constant.Payment;
import com.laanto.it.paycenter.domain.PayRequest;
import com.laanto.it.paycenter.pay.alipay.config.AlipayConfig;
import com.laanto.it.paycenter.pay.alipay.utils.AlipayUtils;
import com.laanto.it.paycenter.service.PayRequestService;
import com.laanto.it.paycenter.utils.BeanValidators;
import com.laanto.it.paycenter.utils.RestTemplateUtils;
import com.laanto.it.paycenter.utils.WebUtil;
import com.laanto.it.paycenter.vo.PayRequestVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/rest/pay_result")
public class PayResultRestController {
    private static final Logger logger = LoggerFactory.getLogger(PayResultRestController.class);

    /**
     * 支付完成之后页面跳转同步通知页面路径
     *
     * @return
     */
    @RequestMapping(value = "/return_url", method = RequestMethod.GET)
    public void createReturnUrl(@RequestParam("success") boolean success, @RequestParam("out_trade_no") String out_trade_no, HttpServletRequest request,
                                HttpServletResponse response) {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/html; charset=utf-8");
        String redirectUrl = "";
        if (success) {
            redirectUrl = WebUtil.getAbsoluteUrl(request, "/static/paySuccess.html?out_trade_no=" + out_trade_no);
        } else {
            redirectUrl = WebUtil.getAbsoluteUrl(request, "/static/payFail.html?out_trade_no=" + out_trade_no);
        }
        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 支付完成之后服务器异步通知页面路径
     *
     * @return
     */
    @RequestMapping(value = "/notify_url", method = RequestMethod.POST, consumes = MediaTypes.JSON_UTF_8, produces = MediaTypes.TEXT_PLAIN_UTF_8)
    public String createNotifyUrl(@RequestBody Map<String, String> resultMap, HttpServletRequest request, HttpServletResponse response) {
        logger.debug("进入PayResult异步通知页面");
        if (resultMap != null && !resultMap.isEmpty()) {
            boolean success = Boolean.valueOf(resultMap.get("success"));
            String out_trade_no = resultMap.get("out_trade_no");
            logger.debug("支付结果success={}，订单号out_trade_no={}，请在下面更新你的订单状态", success, out_trade_no);
            return "success";
        } else {
            return "fail";
        }

    }
}
