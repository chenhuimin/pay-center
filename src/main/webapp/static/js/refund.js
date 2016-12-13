/**
 * Created by Administrator on 2016/4/22 0022.
 */
//获取baseUrl
var localObj = window.location;
var baseUrl = localObj.protocol + "//" + localObj.hostname + (localObj.port ? ':' + localObj.port : '');

$(document).ready(function () {
    $("#btn_union_refund,#btn_weixin_refund").click(function () {
        var outTradeNo = $("#outTradeNo").val();
        var refundFee = $("#refundFee").val();
        var refundType = $("#refundType").val();
        var data = {
            outTradeNo: outTradeNo,
            refundFee: refundFee,
            refundType: refundType
        };
        var dataJson = $.toJSON(data);
        var postUrl = '';
        if (refundType === '0') {
            postUrl = baseUrl + "/rest/refund/union";

        } else if (refundType === '1') {
            postUrl = baseUrl + "/rest/refund/weixin";
        }
        $.ajax({
            type: "POST",
            url: postUrl,
            data: dataJson,
            contentType: "application/json; charset=UTF-8",
            dataType: "json",
            async: false,
            success: function (refundResult, status) {
                var success = refundResult.success;
                if (success) {
                    window.location.href = baseUrl + "/static/refundSuccess.html";
                } else {
                    window.location.href = baseUrl + "/static/refundFail.html";
                }
            }
        });

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
