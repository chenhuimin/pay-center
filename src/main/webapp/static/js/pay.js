/**
 * Created by Administrator on 2016/4/22 0022.
 */
//获取baseUrl
var localObj = window.location;
var baseUrl = localObj.protocol + "//" + localObj.hostname + (localObj.port ? ':' + localObj.port : '');

$(document).ready(function () {
    $("#btn_alipay_pc,#btn_alipay_wap,#btn_unionpay,#btn_weixinpay").click(function () {
        var commodityName = $("#commodityName").val();
        var totalFee = $("#totalFee").val();
        var outTradeNo = $("#outTradeNo").val();
        var notifyUrl = $("#notifyUrl").val();
        var payment = $("#payment").val();
        var data = {
            commodityName: commodityName,
            totalFee: totalFee,
            outTradeNo: outTradeNo,
            notifyUrl: notifyUrl,
            payment: payment
        };
        console.log("payment=" + payment);
        if (payment === '0' || payment === '1' || payment === '2') {
            var returnUrl = $("#returnUrl").val();
            data.returnUrl = returnUrl;
            var dataJson = $.toJSON(data);
            var postUrl = '';
            if (payment === '0' || payment === '1') {
                postUrl = baseUrl + "/rest/pay/ali";
            } else {
                postUrl = baseUrl + "/rest/pay/union";
            }
            $.ajax({
                type: "POST",
                url: postUrl,
                data: dataJson,
                contentType: "application/json; charset=UTF-8",
                dataType: "json",
                async: false,
                success: function (payResult, status) {
                    if (payResult.errmsg) {
                        alert(payResult.errmsg);
                    } else {
                        var payForm = payResult.payForm;
                        $(payForm).appendTo('body').submit();
                    }
                }
            });
        } else if (payment === '3') {//微信支付
            var openid = $("#openid").val();
            var wxPayPage = $("#wxPayPage").val();
            data.openid = openid;
            data.wxPayPage = wxPayPage;
            var dataJson = $.toJSON(data);
            postUrl = baseUrl + "/rest/pay/weixin";
            $.ajax({
                type: "POST",
                url: postUrl,
                data: dataJson,
                contentType: "application/json; charset=UTF-8",
                dataType: "json",
                async: false,
                success: function (payResult, status) {
                    if (payResult.errmsg) {
                        alert(payResult.errmsg);
                    } else {
                        var wxConfig = payResult.wxPayConfig;
                        if (typeof WeixinJSBridge == "undefined") {
                            if (document.addEventListener) {
                                document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
                            } else if (document.attachEvent) {
                                document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
                                document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
                            }
                        } else {
                            onBridgeReady(wxConfig);
                        }
                    }
                }
            });
        }
    });
});

function onBridgeReady(wxConfig) {
    //alert(wxconfig.appId  + "_" + wxconfig.timeStamp + "_" + wxconfig.nonceStr + "_" + wxconfig.packageParam +"_"+ wxconfig.signType + "_" +wxconfig.paySign);
    WeixinJSBridge.invoke(
        'getBrandWCPayRequest', {
            "appId": wxConfig.appId,     				//公众号名称，由商户传入
            "timeStamp": wxConfig.timeStamp,         //时间戳，自1970年以来的秒数
            "nonceStr": wxConfig.nonceStr, 			//随机串
            "package": wxConfig.packageParam,
            "signType": wxConfig.signType,         	//微信签名方式：
            "paySign": wxConfig.paySign 				//微信签名
        },
        function (res) {
            //alert(res.err_msg);
            if (res.err_msg == "get_brand_wcpay_request:ok") {
                window.location.href = baseUrl + "/static/paySuccess.html";
            }     // 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回    ok，但并不保证它绝对可靠。
            else if (res.err_msg == "get_brand_wcpay_request:cancel" || res.err_msg == "get_brand_wcpay_request:fail") {
                window.location.href = baseUrl + "/static/payFail.html";
            }
        }
    );
}
