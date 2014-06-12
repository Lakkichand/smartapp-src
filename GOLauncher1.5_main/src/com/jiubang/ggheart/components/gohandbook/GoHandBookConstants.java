package com.jiubang.ggheart.components.gohandbook;

/**
 * 
 * <br>类描述: GO手册公共参数类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-5]
 */
public class GoHandBookConstants {

	public static final String GO_HANDBOOK_URL = "go_handbook_url";
	public static final String GO_HANDBOOK_LANGUAGE_AND_VERSION = "go_handbook_language_and_version";

	public static final String GO_BOOK_VERSION_NEW = "1.0";
	//	public static final String INDEX_URL_CN = "file:///android_asset/handbook/page/index.html"; 

	public static final String INDEX_URL_CN = "http://smsftp.3g.cn/soft/3GHeart/handbook_cn/page/index.html"; //中文地址
	public static final String INDEX_URL_EN = "http://smsftp.3g.cn/soft/3GHeart/handbook_en/page/index.html"; //英文地址

	public static final String SERVER_ADDRESS = "server_address"; //服务器地址
	public static final String TITLE = "title"; //标题
	public static final String URL_LIST = "url_list"; //Url队列
	public static final String ID_LIST = "id_list"; //ID 队列
	public static final String OPEN_PAGE = "open_page"; //要打开的页面

	public static final String RESULT_TYPE = "result_type"; //继续浏览
	public static final int TRY_NOW_TYPE = 0; //马上试用
	public static final int CONTINUE_BROWSE_TYPE = 1; //继续浏览

	public static final String GO_HANDBOOK_USE_NOW_TYPE = "go_handbook_use_now_type"; //"马上试用"广播参数

	//当前打开的页面
	public static final int BROWSE_PAGE_DESK = 0;
	public static final int BROWSE_PAGE_DOCK = 1;
	public static final int BROWSE_PAGE_FUNCTION = 2;
	public static final int BROWSE_PAGE_FOLDER = 3;
	public static final int BROWSE_PAGE_WIDGET = 4;
	public static final int BROWSE_PAGE_GESTURE = 5;
	public static final int BROWSE_PAGE_CUSTOM = 6;
	public static final int BROWSE_PAGE_PERIPHERAL = 7;
	public static final int BROWSE_PAGE_MORE = 8;

}
