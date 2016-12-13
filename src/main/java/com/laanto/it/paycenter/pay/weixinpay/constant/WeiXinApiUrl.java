package com.laanto.it.paycenter.pay.weixinpay.constant;

public interface WeiXinApiUrl {
    // 网页授权获取用户基本信息,第一步：用户同意授权，获取code
    String CONNECT_OAUTH2_AUTHORIZE = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect";

    // 网页授权获取用户基本信息,第二步：通过code换取网页授权access_token
    String SNS_OAUTH2_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";

    // 菜单创建（POST）
    String MENU_CREATE_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
    // 菜单查询（GET）
    String MENU_GET_URL = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=ACCESS_TOKEN";
    // 菜单删除（GET）
    String MENU_DELETE_URL = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=ACCESS_TOKEN";

    String GET_ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";

    String GET_TICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";

    String PAY_UNIFIEDORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    String PAY_REFUND = "https://api.mch.weixin.qq.com/secapi/pay/refund";

    String PAY_REFUND_QUERY = "https://api.mch.weixin.qq.com/pay/refundquery";

}
