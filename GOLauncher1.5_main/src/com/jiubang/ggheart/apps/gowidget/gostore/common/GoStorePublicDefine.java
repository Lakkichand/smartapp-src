package com.jiubang.ggheart.apps.gowidget.gostore.common;

import android.app.AlarmManager;
import android.graphics.Color;
import android.os.Environment;

/**
 * GoStore业务相关公共定义模块
 * @author huyong
 *
 */
public class GoStorePublicDefine {

	public static final int BGCOLOR = 0xffffffff; //界面背景色-白色

	public static final int TEXTCOLOR = 0xff000000; //文字颜色-黑色

	public static final int GREEN = 0xff8EC51D; //文字颜色-绿色

	public static final int RED = 0xffF09900; //文字颜色-红色

	public static final int GRAY = 0xffB4B4B4; //文字颜色-灰色
	
	public static final int DARK_GREEN = 0xff32b16c; //文字颜色-深绿

	public static final int INSTALLTEXTCOLOR = 0xff626262; //文字颜色-黑色

	public static final String DIVIDER_COLOER = "#D8D8D8"; //item项中分界线的颜色值

	public static final String LIST_COLORHINT = "#00000000"; //列表滚动时的拖影

	public static final int TITLE_BOX_HEIGHT = 68; //title部分的高度

	public static final int TABS_BOX_HEIGHT = 80; //tab栏部分的高度

	public static final int NUM_PER_PAGE = 10; //每页请求记录条数

	public static final int NUM_ITEMS_IN_MAIN_PAGE = 2; //首页面第一行元素的个数
	public static final int NUM_ITEMS_IN_MAIN_PAGE_LANDSCAPE = 3; //如果有足够宽度，横屏时首页面第一行元素的个数

	public static final float STANDARD_DENSITYDPI = 240f; //标准屏幕的densityDpi
	
	public static final int GOSTORE_DETAIL_BIG_STYLE = 1; //精品详情大图风格
	public static final int GOSTORE_DETAIL_DEFAULT_STLYE = 0; //精品详情默认风格

//	public static final String URL_HOST3 = "http://gostore.3g.cn/gostore/entrance";   //目前使用的正式服务器地址

	
		public static final String URL_HOST3 = "http://192.168.215.121:8080/gostore/entrance";    //贤钹机子
	//	public static final String URL_HOST3 = "http://192.168.112.238:80/gostore/entrance";    //内网IP正式服务器地址
	//	public static final String URL_HOST3 = "http://61.145.124.129:80/gostore/entrance";     //目前使用的正式服务器地址
//		public static final String URL_HOST3 = "http://192.168.215.169:8080/gostore/entrance";  //贤钹机子IP 
	//	public static final String URL_HOST3 = "http://192.168.112.64:80/gostore/entrance";     //目前使用的正式服务器地址
	//	public static final String URL_HOST3 = "http://192.168.214.162:8080/gostore/entrance";  //孝伟机子IP
//		public static final String URL_HOST3 = "http://61.145.124.64:8081/gostore/entrance";	//测试服务器地址
	//	public static final String URL_HOST3 = "http://192.168.112.72:8081/gostore/entrance";	//测试服务器地址
	//	public static final String URL_HOST3 = "http://gostore.3gcdn.cn/gostore/entrance";   	//测试DNS的域名
	//	public static final String URL_HOST3 = "http://176.32.85.253:80/gostore/entrance";		//国外地址1
	//	public static final String URL_HOST3 = "http://176.34.62.53:80/gostore/entrance"我;		//国外地址2
	//	public static final String URL_HOST3 = "http://176.34.53.25:80/gostore/entrance";		//国外地址3
	//	public static final String URL_HOST3 = "http://192.168.112.238:80/gostore/entrance";
	//	public static final String URL_HOST3 = "http://192.168.114.186:80/gostore/entrance";
	//	public static final String URL_HOST3 = "http://6e 9.28.52.38:80/gostore/entrance";  		//国外IP
	// 桌面官方主题下载站点
	public static final String GOLAUNCHER_THEME_SITE_URL = "http://theme.3g.cn/xuan/xuanList.aspx?fr=golauncherxuan";

	public static final String URL_PID_THEMES[] = { "1001", //免费主题
			"1002", //收费主题
			"1003" //最新主题
	};

	public static final String URL_WIDGETS[] = { "1004" //免费widget
	};

	public static final String URL_THEMESTORE_MAIN = "1005"; //主页面

	public static final String APP_UID = "100"; //软件UID

	public static final int FUNID_RECOMMEND = 2; //首页推荐列表
	public static final int FUNID_SORTLIST = 3; //分类商品列表
	public static final int FUNID_SEARCH = 4; //全局搜索商品列表
	public static final int FUNID_PRODUCT_DETAIL = 5; //获取具体商品详情
	public static final int FUNID_IMAGELIST = 6; //批量获取图片数据
	public static final int FUNID_MORE = 7; //更多下载渠道信息
	public static final int FUNID_NEW_PRODUCT = 8; //新商品列表
	public static final int FUNID_VERSION = 9; //版本验证
	public static final int FUNID_CHANNEL_CHECK = 12; //合作渠道包验证
	public static final int FUNID_UPATE_CHECK = 13; //检查更新时间戳
	public static final int FUNID_SEARCH_KEYS = 14; //搜索关键词
	public static final int FUNID_APPS_UPDATE = 15; //软件更新列表功能号
	public static final int FUNID_SORT = 18; //分类信息
	public static final int FUNID_MAIN_VIEW = 19; //首页
	public static final int FUNID_COMPLE_SORT = 34; //复合分类

	public static final String ITEM_ID_KEY = "id"; //在Intent中传递商品广告ID的键值
	public static final String ITEM_PKG_NAME = "pkgname"; ////在Intent中传递商品广告包命的键值
	public static final String ITEM_URL = "url"; //在Intent中传递商品广告URL地址
	public static final String SCAN_IMAGE_IDS_KEY = "scanImageIds"; //在Intent中传递浏览图片ID集合的键值
	public static final String SCAN_IMAGE_CUR_INDEX_KEY = "scanImageCurIndex"; //在Intent中传递浏览图片当前选中图片的键值

	public static final byte VIEW_TYPE_MAIN = 0; //主界面类型
	public static final byte VIEW_TYPE_SORT_LIST = 1; //分类列表类型
	public static final byte VIEW_TYPE_TAB_LIST = 2; //选项卡列表类型
	public static final byte VIEW_TYPE_SEARCH_INPUT = 3; //搜索输入页面类型
	public static final byte VIEW_TYPE_SEARCH_RESULT = 4; //搜索结果页面类型
	public static final byte VIEW_TYPE_ITEMDETAIL = 5; //详情页面类型
	public static final byte VIEW_TYPE_APPS_MANAGER = 6; //软件管理页面类型
	public static final byte VIEW_TYPE_SORT = 7; //分类类型
	public static final byte VIEW_TYPE_TOPIC = 8; //专题类型
	public static final byte VIEW_TYPE_WALLPAPER_DATA_LIST = 9; //壁纸预览列表类型
	public static final byte VIEW_TYPE_WALLPAPER_SORT_LIST = 10; //壁纸分类列表类型
	public static final byte VIEW_TYPE_KEYWORDS = 11; //关键字类型
	public static final byte VIEW_TYPE_TAB_GRID = 12; //选项卡九宫格类型

	public static final int URL_TYPE_HTTP_SERVER = 1; //服务器下载地址
	public static final int URL_TYPE_GOOGLE_MARKET = 2; //GoogleMarket地址
	public static final int URL_TYPE_DETAIL_ADDRESS = 3; //详情地址
	public static final int URL_TYPE_OTHER_ADDRESS = 4; //其它地址
	public static final int URL_TYPE_WEB_GOOGLE_MARKET = 5; //其它地址

	public static final String SEARCHKEYS_FILE_NAME = "searchKeys"; //保存搜索历史记录所使用的SharedPreference的名称
	public static final String SEARCHKEYS_KEY = "searchKeys"; //保存搜索历史记录所使用的键值
	public static final String SEARCHKEYS_SPLIT = "~!@#"; //保存搜索关键字时所使用的分隔符
	public static final int SEARCHKEYS_LIMIT = 10; //搜索关键字的数目限制

	public static final String URI_SECHEMA_MAIN = "main"; //跳转商城的主界面时的sechema
	public static final String URI_SECHEMA_SEARCH = "search"; //跳转商城的搜索界面时的sechema
	public static final String URI_SECHEMA_SEARCH_RESULT = "search_result"; //跳转商城的搜索界面时的sechema
	public static final String URI_SECHEMA_DETAIL = "detail"; //跳转商城的详情界面时的sechema
	public static final String URI_SECHEMA_SORT = "sort"; //跳转商城的分类界面时的sechema
	public static final String URI_SECHEMA_APPS_MANAGER = "apps_manager"; //跳转商城的软件管理界面是的sechema

	public static final String GOSTORESCHEME = "gostorewidget"; //商城跳转Uri的scheme

	public static final int MAIN_SORT_LANDSCAPE_REQUEST_WIDTH = 664; //主页分类横屏显示3项是所需要的宽度

	public static final String DOWNLOAD_URL_KEY = "downloadUrl"; //直接进行文件下载时，进行参数传递的URL地址的键值
	public static final String DOWNLOAD_FILENAME_KEY = "downloadFileName"; //直接进行文件下载时，进行参数传递的下载文件名称的键值

	// public static boolean mIsOrientationLandscape = false; //是否是横屏的标志
	// public static boolean mIsOrientationChange = false; //是否进行了横竖屏切换

	public static final String APP_ID_KEY = "appId"; //外部应用跳转GO精品时，应用ID的键值
	public static final int GO_STORE_WIDGET_ID = 1; //用于GO精品入口统计所分配的GO精品Widget的ID
	public static final int GOLAUNCHER_MENU_ID = 2; //用于GO精品入口统计所分配的GO桌面菜单的ID

	//edit by chenguanyu
	public static final String CACHE_IMAGE_SIZE_FILE = "image_record_cache"; //图片数据长度记录文件名称

	//    public static final long IMG_EXPIRED_TIME = 12*60*60*1000;				//图片过期时间12h
	public static final long IMG_EXPIRED_TIME = 15 * AlarmManager.INTERVAL_DAY; //图片过期时间12h
	public static final long CACHE_EXPIRED_TIME = 1 * 10 * 60 * 1000; //缓存文件过期时间10min

	public final static String SDCARD = Environment.getExternalStorageDirectory().getPath();
	
	public static final String COMMON_ICON_PATH = SDCARD + "/GoStore/icon/";
	public static final String GOSTORE_VIEW_CACHE_FILE_PATH = SDCARD + "/GoStore/view_data_cache"; //GO精品页面数据的缓存文件夹路径
	public static final String CACHE_FILE_PATH = SDCARD + "/GoStore/gostore_widget_cache/";
	public static final String CACHE_CONTENT_FILE = "content_cache.xml";

	public static final String USERDATA_KEY_SEARCH = "search_userdata"; //统计数据中，搜索按钮点击次数保存时使用的KEY
	public static final String USERDATA_KEY_RECOMMEND = "recommend_userdata"; //统计数据中，推荐区项目点击次数保存时使用的KEY
	public static final String USERDATA_KEY_SORT = "sort_userdata"; //统计数据中，栏目点击次数保存时使用的KEY

	public static final String GOWIDGET_THEME_FILE = "widget_gostorewidget.xml"; //gostorewidget主题配置文件名称 
	public static final String GOWIDGET_THEME = "gowidget_theme"; //应用主题时，桌面传过来的Widget主题包的KEY
	public static final String GOWIDGET_TYPE = "gowidget_type"; //应用主题时，桌面传过来的Widget类型的KEY
	public static final String GOWIDGET_THEMEID = "gowidget_themeid"; //应用主题时，桌面传过来的主题ID的KEY
	public static final String GOWIDGET_ID = "gowidget_widgetid"; //widget id

	public static final int DEFAULT_TEXT_COLOR = Color.BLACK; //皮肤默认文字颜色,黑色

	public static final String GOSTORE_DEFAULT_CHANNEL = "999"; //GO精品默认渠道号
	
	public static final String APP_CENTER_DETAIL_IS_THEME = "is_theme";
	//end edit
//	//窗体finish action
//	public static final String ACTION_ACTIVITY_FINISH = "com.jiubang.ggheart.apps.gowidget.gostore.ACTIVITY_FINISH";

}
