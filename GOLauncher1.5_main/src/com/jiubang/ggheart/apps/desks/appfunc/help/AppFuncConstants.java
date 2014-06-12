package com.jiubang.ggheart.apps.desks.appfunc.help;

/**
 * 
 * @author tanshu
 * 
 */
public class AppFuncConstants {
	// 常量
	public static final String FOLDER_ID = "Foder_Id";
	public static final String CREATEFOLDER = "CREATE_FOLDER";
	public static final String FOLDER_INTENT = "FOLDER_INTENT";
	public static final String IS_TEMP_FOLDER = "IS_TEMP_FOLDER";
	public static final String NEW_FOLDER_NAME = "NEW_FOLDER_NAME";
	public static final String NEW_FOLDER_TYPE = "NEW_FOLDER_TYPE";
	public static final String NONE = "none";
	/**
	 * go主题包名前缀
	 */
	public static final String THEME_PACKGE_NAME = "com.gau.go.launcherex.theme.";
	// 滚动条大小
	public static final int SCROLL_SIZE = 6;
	// 最大帧数：用于计时
	public static final int MAXFRAME = 33;

	// Frame ID
	public static final int BACKGROUND_FRAME_ID = 1;
	public static final int NORMAL_FRAME_ID = 2;

	// Tab ID
	public static final int ALLAPPS = 0;
	public static final int RECENTAPPS = 1;
	public static final int PROCESSMANAGEMENT = 2;
	public static final int MAXTABINDEX = 2;

	/**
	 * 文件夹缩略图最多显示的应用程序个数
	 */
	public static final int SHOW_ICON_SIZE = 4;

	// 图标背景颜色
	public static final int ICON_BG_FOCUSED = 0x66FFFFFF;
	public static final int ICON_BG_PRESSED = 0xFFFFB500;
	public static final int ICON_TEXT_COLOR = 0xFFFFFFFF;
	public static final int ICON_IN_EDGE_COLOR = 0xA112ff00;

	public static final int TICK_COUNT = 1;

	// Color
	public static final int TAB_SELECT_TOP = 0xffcfcfcf;
	public static final int TAB_SELECT_BOTTOM = 0xffababab;
	public static final int TAB_FOCUS_TOP = 0xff88cf00;
	public static final int TAB_FOCUS_BOTTOM = 0xff51a200;
	public static final int TAB_EMPTY = 0x33000000;
	public static final int WHITE_LINE = 0x40ffffff;
	public static final int BLACK_LINE = 0x55000000;
	public static final int DEFAULT_BG_COLOR = 0x8D000000;

	/**
	 * 消息ID
	 * @author yangguanxiang
	 *
	 */
	public static enum MessageID {
		APP_ADDED, // 新加一个应用程序
		APPLIST_ADDED, // 新加一组应用程序
		APP_REMOVED, // 卸载应用程序
		APP_LOADINGFINISH, // 后台图标加载完毕
		BG_CHANGED, // 背景图改变
		BG_SHOWED, // 背景图是否显示改变
		STANDARD_CHANGED, // 规格改变
		SHOWNAME_CHANGED, // 显示名称改变
		INOUTEFFECT_CHANGED, // 特效设置改变
		SLIDEDIRECTION_CHANGED, // 滑动方向变化
		CLEAR_RECENTAPP, // 清空最近打开
		UPDATE_RECENTAPP, // “最近打开”列表更新
		SINGLE_TASKMANAGE, // "程序管理"结束单个程序
		ALL_TASKMANAGE, // "程序管理"结束所有程序
		LOCK_LIST_CHANGED, // “程序管理”忽略列表发生变化
		UPDATE_FOLDERTITLE, // 更新文件夹名称
		ALL_PROGRAMSORT, // "所有程序"重新排序结束
		ALL_SORTSETTING, // "所有程序"图标排序方式改变
		STARTREFRESHLIST, // 开始刷新应用程序列表
		FINISHREFRESHLIST, SDLOADINGFINISH, // SD卡loading完毕
		// 完成刷新应用程序列表
		ICONEFFECT_CHANGED, // 图标设置改变
		SCROLL_LOOP_CHANGED, // 横向循环滚屏设置改变
		BLUR_BACKGROUND_CHANGED, // 是否模糊背景的设置改变
		SHOW_TAB_ROW_CHANGED, // 是否显示Tab栏的设置改变
		VERTICAL_SCROLL_EFFECT_CHANGED, // 竖向滑动特效设置改变
		SHOW_SEARCH, REFRASH_FOLDER_ICON, ADD_BATCH_APP, UPDATE_BATCH_APP, 
		HIDE_APPS, ADD_ITEM, ADD_ITEMS, REMOVE_ITEM, REMOVE_ITEMS
	}

	// 横竖屏
	/**
	 * 水平
	 */
	public static final byte HORIZONTAL = 2;
	/**
	 * 竖直
	 */
	public static final byte VERTICAL = 1;
	/**
	 * 第一次被初始化，没有方向
	 */
	public static final byte NOORIENTATION = 0;

	// Model Message
	// public static final int FOCUS = 0; // 通知聚焦事件
	public static final int XGRIDONCHANGE = 1; // 通知XGRID调用onChange方法
	// public static final int ONLONGCLICKDOWN = 2; // 长按事件，通知XViewFrame让房子做动画
	// public static final int ONLONGCLICKUP = 3; //
	// 长按弹起事件，通知XViewFrame让“移动到桌面”控件做动画
	// public static final int MOVETODESKISTOUCHED = 4;
	public static final int MOVETODESKISUNTOUCHED = 5;
	// public static final int RELAYOUTALLGRIDS = 6; // 重新排版所有Grid
	public static final int EXITAPPFUNCFRAME = 7; // 离开功能表
	public static final int EXIT_APPFUNC_FRAME_WITHOUT_ANIMATION = 74; // 离开功能表
	public static final int EXITEDITMODEL = 8; // 退出编辑模式
	public static final int REFRESHPROCESSGRID = 9; // 刷新程序管理Tab
	public static final int EXITAPPFUNCFROMHOME = 10; // 拖动图标到小房子离开功能表
	public static final int POST_REPANIT = 11;
	// public static final int REMOVEFOCUS = 12; // 移除焦点
	// public static final int CREATEFOLDER = 13; // 新建文件夹
	// public static final int REVERTICON = 14; // 应用程序归位
	// public static final int OPENFOLDER = 15; // 打开文件夹
	public static final int APP_TICK = 16; // 主线程数据刷新
	public static final int LAYOUTFOLDERGRID = 17; // 文件夹重新布局
	public static final int REMOVEFOLDER = 18; // 移除文件夹
	public static final int FOLDER_RENAME = 19; // 文件夹重命名
	public static final int ENTEREDITMODE = 20; // 进入编辑模式
	// public static final int REFRESHGRID = 21; // 刷新Grid
	public static final int OPENDIALOG_ALLAPP = 22; // 所有程序异步消息同步到UI线程打开创建文件夹对话框
	public static final int XGRID_DRAG_STATUS = 23;
	public static final int START_REFRESH_GRID_LIST = 24; // 开始刷新应用程序列表
	public static final int FINISH_REFRESH_GRID_LIST = 25; // 完成刷新应用程序列表
	// public static final int APPINFOLDER_REMOVED = 26; //有文件夹中的应用程序被移除
	public static final int REFRESHICON = 27; // 刷新图标
	public static final int SUSPENDEDITMODE = 28; // 暂停编辑模式
	public static final int RESUMEEDITMODE = 29; // 恢复编辑模式
	public static final int SET_ALL_APPS_TAB = 67; // 进入功能表请求设置为[all]Tab
	// public static final int UNFOCUS = 30; // 通知聚焦事件
	public static final int RESETCOMPONENTS = 31; // 退出功能表时的复位事件
	public static final int RESETHOMEMOVETODESK = 32; // 退出功能表时home组件和movetodesk组件重置
	public static final int SHOWQUICKACTIONMENU = 33; // 显示锁定的弹出菜单
	public static final int APP_GRID_LOCATE_ITEM = 34; // 定位到某个应用程序图标使之聚焦
	public static final int ALL_APP_SEARCH_SHOWRESULT = 36; // 功能表搜索菜单被点击
	public static final int ALL_APP_SEARCH_HISTORY_ITEM_CLICKED = 38; // 功能表历史列表项被点击
	public static final int ALL_APP_SEARCH_HISTORY_CLEAR_CLICKED = 39; // 功能表搜索历史列表清楚项被点击
	public static final int ALL_APP_SEARCH_SAVE_KEY_WORD = 40; // 保存搜索关键字
	public static final int ALL_APP_SEARCH_CLEAR_TEXT = 41; // 清除搜索界面编辑框的文字
	public static final int ALL_APP_SEARCH_SEARCH_WEB = 47; // 搜索网络数据
	public static final int TUTORIAL_DRAG_MODE = 42; // 进入拖动icon到桌面教程
	public static final int TUTORIAL_CREATE_FOLDER_MODE = 43; // 进入创建文件夹教程
	public static final int TUTORIAL_HIDE_APP_MODE = 44; // 进入隐藏app教程
	public static final int APP_COMP_SET_CURRENT_TAB_INDEX = 45; // 设置当前TAB index
	public static final int ALL_APP_SEARCH_HIDE = 46; // 在不开启功能表搜索而通过点击搜索按钮显示搜索界面后需要隐藏搜索界面，发送这个消息
	public static final int ALL_APP_SEARCH_SHOW = 53;
	public static final int ALL_APP_MENU_SHOW = 54;
	public static final int ALL_APP_MENU_HIDE = 55;
	public static final int APP_FUNC_MENUKEY_LONGPRESS = 49; // 长按菜单事件
	public static final int ALL_APP_SEARCH_RESET = 50; // 重置搜索页面
	public static final int ALL_APP_ALPHA_ICON = 51; // 半透明功能表图标
	public static final int SCREEN_LOAD_FINISH = 52; // 桌面加载完数据
	public static final int DRAW_SIDEBG_YES = 56; // 绘制屏幕预览区域滚屏高亮图片
	public static final int DRAW_SIDEBG_NO = 57; // 绘制屏幕预览区域滚屏高亮图片
	public static final int ALL_APP_SEARCH_SEARCH_WEB_ICON_FINISH = 58; // 网络数据的图标下载完成
	public static final int ALL_APP_SWITCH_CONTENT_TYPE = 59; // 切换所有程序tab界面展示内容
	// public static final int SYSTEM_CONFIGURATION_CHANGED = 52; // 进入编辑模式
	// 对象ID
	// public static final int ALLAPPS_APPMOVETODESK = 0; // 所有程序中的移动到桌面控件
	public static final int ADD_ITEM_TO_WORKSPACE_START = 53; // 添加图标到屏幕预览开始添加到桌面
	public static final int ADD_ITEM_TO_WORKSPACE_FINISH = 54; // 添加图标到屏幕预览添加到桌面完成
	public static final int SYSTEM_CONFIGURATION_CHANGED = 60; // 添加图标到屏幕预览添加到桌面完成
	public static final int PRO_MANAGE_MENU_SHOW = 61; // 显示正在运行页菜单栏
	public static final int PRO_MANAGE_MENU_HIDE = 62; // 隐藏正在运行页菜单栏
	public static final int PRO_MANAGE_REFRESH = 63; // 通知正在运行Tab进行刷新
	public static final int MEDIA_TABCONTENT = 64; // 所有程序Tab对应的Grid
	public static final int IS_GO_TO_INFO = 65; // 告诉正在运行Tab对应的Grid，去了程序信息页面

	public static final int PROGRESSBAR_SHOW = 66; // 显示正在运行页菜单栏
	public static final int PROGRESSBAR_HIDE = 67; // 隐藏正在运行页菜单栏
	public static final int MEMORY_REFRESH_FINISHED = 68; // 告诉正在运行内存以前刷新完成
	public static final int MEDIA_PLUGIN_CHANGE = 75; // 资源管理插件安装或卸载

	public static final int ALLAPPS_TABCONTENT = 1; // 所有程序Tab对应的内容
	public static final int ALLAPPS_GRID = 2; // 所有程序Tab对应的Grid
	public static final int RECENTAPPS_GRID = 3; // 最近打开Tab对应的Grid
	public static final int PROCESS_GRID = 4; // 程序管理Tab对应的Grid
	public static final int TABCOMPONENT = 5; // 整个Tab栏
	public static final int APPFUNCFRAME = 6; // 最外层的框架Frame
	public static final int APPFOLDER = 7; // 文件夹ID
	public static final int APPFOLDER_GRID = 8; // 文件夹对应的Grid
	public static final int XVIEW = 9; // XViewFrame
	public static final int PROMANAGEHOMEICON = 65; // 正在运行的Home栏
	public static final int APP_FUNC_MAIN_VIEW = 42;
	public static final int APP_FUNC_SEARCH_RESULT_CONTAINER = 37; // 功能表搜索结果容器
	public static final int APP_FUNC_ALLAPP_CONTAINER = 48; // 功能表所有程序面板中间容器
	public static final int APP_FUNC_APPTODESK = 49; // 移动到桌面
	public static final int MEDIA_HIDE_MODE_CHANGE = 66; // 切换隐藏资源编辑模式
	public static final int APP_FUNC_FOLDER_QUICK_ADD_BAR = 69; // 功能表文件夹快捷栏 
	public static final int PRO_MANAGE_EDIT_DOCK = 70; // 正在运行编辑栏 
	public static final int PRO_MANAGE_EDIT_DOCK_CLEAR_EDIT_DOCK_BG = 71; // 清楚编辑栏背景色
	public static final int PRO_MANAGE_EDIT_DOCK_CHANGE_LOCK_TEXT = 72; // 改变锁定/解除锁定文字
	public static final int APP_FUNC_SWITCH_BUTTON = 73; // 多媒体切换按钮
	public static final int APP_FUNC_HOME_ICON = 74; // 正在运行底部栏

	// public static final int FOLDERADAPTER = 10; //文件夹对应的Adapter

	// 打开文件夹的方式
	// public static final int APPFOLDER_OPENINVALID = -1; // 初始状态
	// public static final int APPFOLDER_CLICKOPEN = 0; // 单击打开文件夹
	// public static final int APPFOLDER_APPADDOPEN = 1; // 拖入某个文件打开文件夹

	// 新建文件夹的方式
	// public static final int BYMENU = 0; // 从Menu新建
	// public static final int BYXGRID = 1; // 从XGrid新建

	// 用于群发的消息ID
	public static final int THEME_CHANGE = 10000; // 主题改变
	public static final int LOADTHEMERES = 10001; // 加载主题资源
	public static final int LOCKCHANGES = 10002; // 桌面锁屏设置发生变化
	public static final int LOADINDICATORTHEMERES = 10003; // 桌面锁屏设置发生变化
	public static final int RELOADTABHOMETHEMERES = 10004; // 桌面锁屏设置发生变化
	public static final int THEME_NEW_INSTALLED = 10005; // 新安装了主题
	public static final int RELOAD_FOLDER_THEMES = 10006; // 重新加载文件夹主题

	// 用于发消息给桌面的bundle字段
	public static final String FOLDERINFOLIST = "folderinfolist";
	public static final String FOLDERCOUNT = "foldercount";

	// GOSTORE Component常量
	public static final String GOSTORECOMPONENTNAME = "ComponentInfo{com.gau.diy.gostore/com.jiubang.intent.action.FUNC_SPECIAL_APP_GOSTORE}";
	
	//应用中心  Component常量
	public static final String APPGAME_APP_CENTER_COMPENTANME = "ComponentInfo{com.gau.diy.recomendcenter/com.jiubang.intent.aciton_FUNC_RECOMMENDCENTER}";

	// 禁用的组件透明度
	public static final int DISABLED_COMPONENT_ALPHA = 150;

	/**
	 * 返回音乐播放界面
	 * 
	 * @author yangbing
	 * */
	public static final int RETURN_MUSIC_PLAY = 151;
	
	/**
	 * 刷新功能表文件夹快捷栏 所有图标
	 */
	public static final int REFRESH_FOLDER_QUICK_ADD_BAR = 152;
	
	/**
	 * 刷新功能表文件夹快捷栏 普通文件夹图标
	 */
	public static final int REFRESH_FOLDER_QUICK_ADD_BAR_NORMAL_FOLDER = 153;
	
	/**
	 * 刷新功能表文件夹快捷栏 特殊文件夹图标
	 */
	public static final int REFRESH_FOLDER_QUICK_ADD_BAR_SEPCIAL_FOLDER = 154;
	
	/**
	 * 弹出多媒体切换菜单
	 */
	public static final int SHOW_SWITCH_MENU = 155;
	
	/**
	 * 定位到指定多媒体资源图标
	 */
	public static final int LOCATE_MEDIA_ITEM = 156;
	
	/**
	 * 手动设置从音乐播放器返回的播放列表信息
	 */
	public static final int MUSIC_PLAYER_SET_PLAYLIST_INFO = 157;
	
	/**
	 * 功能表文件夹快捷栏 某个特殊文件夹不显示
	 */
	public static final int SET_SPECIAL_FOLDER_DISMISS = 158;
	
	/**
	 * 打开图片浏览器
	 */
	public static final int OPEN_IMAGE_BROWSER = 159;
}
