package com.jiubang.ggheart.apps.desks.diy.themescan;

/**
 * 主题模块中使用到的常量值
 * 
 * @author yangbing
 * */
public class ThemeConstants {
	//http://ggtest.3g.net.cn/gostoremanage/confmsg.do?Module=confmsg&Func=MAIN
	/**
	 * 桌面本地主题
	 * */
	public static final int LAUNCHER_INSTALLED_THEME_ID = 1;
	/**
	 * 桌面精选主题
	 * */
	public static final int LAUNCHER_FEATURED_THEME_ID = 2;
	/**
	 * 锁屏本地主题
	 * */
	public static final int LOCKER_INSTALLED_THEME_ID = 3;
	/**
	 * 锁屏精选主题
	 * */
	public static final int LOCKER_FEATURED_THEME_ID = 4;

	/**
	 * 热门主题
	 */
	public static final int LAUNCHER_HOT_THEME_ID = 5;
	/**
	 * 专题
	 */
	public static final int LAUNCHER_SPEC_THEME_ID = 6;

	//	/**
	//	 * go锁屏action
	//	 * */
	//	public static final String LOCKER_DETAIL_ACTION = "com.jiubang.goscreenlock.themeDetail";
	/**
	 * go锁屏extra
	 * */
	public static final String LOCKER_DETAIL_EXTRA_KEY = "themePkg";
	//	/**
	//	 * go精品action
	//	 * */
	//	public static final String GOSTORE_DETAIL_ACTION = "com.jiubang.ggheart.apps.gowidget.gostore.ITEM_DETAIL";
	/**
	 * 包名extra
	 * */
	public static final String PACKAGE_NAME_EXTRA_KEY = "pkgname";
	/**
	 * 锁屏包名extra
	 * */
	public static final String LOCKER_PACKAGE_NAME_EXTRA_KEY = "themePkg";
	/**
	 * go锁屏版本号
	 * */
	public static final int REQUEST_LOCKER_VERSION_NUM = 236;
	/**
	 * go锁屏版本号
	 * */
	public static final int REQUEST_LOCKER_GO_THEME_VERSION_NUM = 276;
	/**
	 * 桌面主题包名前缀
	 * */
	public final static String LAUNCHER_THEME_PREFIX = "com.gau.go.launcherex.theme";
	/**
	 * 锁屏主题包名前缀
	 * */
	public final static String LOCKER_THEME_PREFIX = "com.jiubang.goscreenlock.theme";
	/**
	 * 一些包含短信主题、桌面主题和锁屏主题的大主题包名的前缀
	 */
	public final static String LAUNCHER_BIG_THEME_PREFIX = "com.jb.go.bigtheme";
	/**
	 * 进入详情界面的模式
	 */
	public static final String DETAIL_MODEL_EXTRA_KEY = "detailModel";
	public static final int DETAIL_MODEL_INSTALL_EXTRA_VALUE = 0; //本地详情
	public static final int DETAIL_MODEL_FEATURED_EXTRA_VALUE = 1; //推荐详情
	/**
	 * 主题ID
	 */
	public static final String DETAIL_ID_EXTRA_KEY = "id";
	
	public static final String TITLE_EXTRA_KEY = "themeTitle";

	/**
	 * 区分热门和精选
	 */
	public static final String TAB_THEME_KEY = "featured_or_hot_theme_key";

	public static final int STATICS_ID_FEATURED = 24;
	public static final int STATICS_ID_HOT = 25;
	public static final int STATICS_ID_LOCKER = 26;

	public static final int STATICS_ID_FEATURED_NOTIFY = 175;
	public static final int STATICS_ID_LOCKER_NOTIFY = 176;

	public static final int CUSTOMER_LEVEL0 = 0;
	public static final int CUSTOMER_LEVEL1 = 1;
	public static final int CUSTOMER_LEVEL2 = 2;
	public static final int CUSTOMER_LEVEL1_UPGRADE = 3;
	public static final String CLICK_THEME_PKGNAME = "click_theme_pkgname";

	//国内地址如下（注意参数）:
	public static final String VIPPAY_PAGE_URL_CN = "http://goappcenter.3g.net.cn/recommendedapp/getpage.do?pid=%pid&vs=%page&lang=%lang&local=%local&rd=";

	//国外地址如下（注意参数）:
	public static final String VIPPAY_PAGE_URL_FOREIGN = "http://goappcenter.goforandroid.com/recommendedapp/getpage.do?pid=%pid&vs=%page&lang=%lang&local=%local&rd=";
	public static final String VIP_LEVE1_PAY_ID = "com_gau_go_launcherex_vip_lite";
	public static final String VIP_LEVE2_PAY_ID = "com_gau_go_launcherex_vip";
	public static final String VIP_LEVE1_UPGRADE_PAY_ID = "com_gau_go_launcherex_vip_upgrade";
//		public static final String VIP_LEVE1_PAY_ID = "android.test.purchased";
//		public static final String VIP_LEVE2_PAY_ID = "android.test.purchased";

	/**
	 * 更新全屏图的id
	 */
	public static final String CHANGE_FULLSCREEN_IMAGE_ID = "change_fullscreen_image_id";
	
	/**
	 * 传递给全屏图的主题信息
	 */
	public static final String FULLSCREEN_THEME_INFO = "fullscreen_theme_info";
	/**
	 * 全屏图用来显示哪一页
	 */
	public static final String FULLSCREEN_CURRENT_INDEX = "fullscreen_current_index";
}
