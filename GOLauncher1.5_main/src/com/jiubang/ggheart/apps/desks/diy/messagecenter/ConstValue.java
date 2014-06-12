package com.jiubang.ggheart.apps.desks.diy.messagecenter;


/**
 * 
 * @author rongjinsong
 * @version 1.0
 */
public class ConstValue {

	public final static boolean DEBUG = false;
	public final static String MSG_TAG = "messagecenter";
	public final static String ENCRYPT_KEY = "MESSAGECENTER130114";

	// 测试服务器地址1
	//    public static String HOSTURL_BASE="http://61.145.124.64:8011/golaunchermsg/msgservice.do?";
	//测试服务器地址2
//		public final static String HOSTURL_BASE = "http://ggtest.3g.net.cn/golaunchermsg/msgservice.do?";
	
	//新测试地址
//	public final static String HOSTURL_BASE = "http://ggtest.3g.net.cn:8011/golaunchermsg/msgservice.do?";
	//张华机子
//	public static String HOSTURL_BASE= "http://192.168.214.63:8080/golaunchermsg/msgservice.do?";

	//  正式服务器地址
	public static final String HOSTURL_BASE = "http://launchermsg.3g.cn/golaunchermsg/msgservice.do?";
	public static final int URL_GET_MSG_LIST = 1;
	public static final int URL_GET_MSG_CONTENT = 2;
	public static final int URLPOST_MSG_STATICDATA = 3;
	public static final int URL_GET_URL = 4;

	public static final int STATTUS_OK = 1;
	public static final int STATTUS_ERR_CODE_0 = 0; // 请求参数错误,
	public static final int STATTUS_ERR_CODE_1 = -1; // 服务器处理出错,
	public static final int STATTUS_ERR_CODE_2 = -2; // 业务处理异常；

	public static final String PREFIX_MSG = "msg://id=";
	public static final String PREFIX_GUI = "gui://id=";
	public static final String PREFIX_GUIDETAIL = "guidetail://id=";
	public static final String PREFIX_GUISPEC = "guispec://id=";
	public static final String PREFIX_MARKET = "market://id=";
	public static final String PREFIX_GOSTORETYPE = "gostoretype://id=";
	public static final String PREFIX_GOSTOREDETAIL = "gostoredetail://id=";
	public static final String PREFIX_HTTP = "http://";
	public static final String PREFIX_APPCENTERTYPE = "appcentertype://id=";
	public static final String PREFIX_APPCENTERTOPIC = "appcentertopic://id=";
	public static final String PREFIX_APPCENTERDETAIL = "appcenterdetail://id=";

	
	// 测试后台
	// http://ggtest.3g.net.cn:8183/gomsgmanage/webcontent/index.jsp
}
