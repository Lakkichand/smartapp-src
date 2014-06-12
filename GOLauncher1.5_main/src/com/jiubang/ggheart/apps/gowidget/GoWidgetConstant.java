package com.jiubang.ggheart.apps.gowidget;

import android.appwidget.AppWidgetProviderInfo;

import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;

public final class GoWidgetConstant {
	public static final String GOWIDGET = "com.gau.gowidget";

	// ----------------解析array.xml用到的字段---------------------//
	/**
	 * 选择样式的title信息
	 */
	public static final String WIDGET_TITLE = "widget_title";
	/**
	 * widget图标
	 */
	public static final String WIDGET_ICON = "widget_icon";
	/**
	 * 选择样式的选择样式预览图片
	 */
	public static final String PREVIEW_LIST = "stylepreviewlist";
	/**
	 * 选择样式的选择样式预览文字说明
	 */
	public static final String STYLE_NAME_LIST = "stylenamelist";
	/**
	 * 选择样式的选择样式 a x b
	 */
	public static final String ROW_LIST = "rowlist";
	public static final String COL_LIST = "columnlist";

	/**
	 * 选择样式的选择样式layout id
	 */
	public static final String LAYOUT_LIST = "layoutidlist";

	/**
	 * 选择样式的选择样式type id
	 */
	public static final String TYPE_LIST = "styletypelist";

	/**
	 * 选择样式的选择样式宽最小值
	 */
	public static final String MIN_WIDTH = "minWidth";

	/**
	 * 选择样式的选择样式高最小值
	 */
	public static final String MIN_HEIGHT = "minHeight";

	/**
	 * 选择样式的设置界面路径
	 */
	public static final String CONFIG_LIST = "configlist";

	/**
	 * 选择样式的长按时的设置界面路径
	 */
	public static final String SETTING_LIST = "settinglist";
	// ----------------------------------------------------------//

	// for Bundle
	/**
	 * {@link GoWidgetBaseInfo#mWidgetId}
	 */
	public static final String GOWIDGET_ID = "gowidget_Id";

	/**
	 * {@link GoWidgetBaseInfo#mType}
	 */
	public static final String GOWIDGET_TYPE = "gowidget_type";

	/**
	 * {@link GoWidgetBaseInfo#mLayout}
	 */
	public static final String GOWIDGET_LAYOUT = "gowidget_layout";

	/**
	 * {@link AppWidgetProviderInfo}
	 */
	public static final String GOWIDGET_PROVIDER = "gowidget_provider";

	/**
	 * 主题bundle字段
	 */
	public static final String GOWIDGET_THEME = "gowidget_theme";
	public static final String GOWIDGET_THEMEID = "gowidget_themeid";

	/**
	 * GoWidget 原型，用于区分内置和独立的widget
	 */
	public static final String GOWIDGET_PROTOTYPE = "gowidget_prototype";

	/**
	 * 进入设置的标志：用于区分是否从弹出菜单进入设置activity 由bundle传入，取值为true表示从菜单进入
	 */
	public static final String GOWIDGET_SETTING_ENTRY = "setting_entry";

	/**
	 * 全部地区，在validArea与invalidArea中用到
	 */
	public static final String GOWIDGET_ALL_AREA = "-1";

	/**
	 * onStart的Bundle字段，Boolean，ture表示用户添加操作触发的onStart.
	 */
	public static final String GOWIDGET_ADD_TO_SCREEN = "gowidget_add_to_screen";

	/**
	 * onStart的Bundle字段，Boolean，ture表示当前gowidget的运行环境为平板
	 */
	public static final String GOWIDGET_IS_TABLET = "gowidget_is_tablet";

//	// for intent
//	public static final String ACTION_ADD = GOWIDGET + "_action_add";
//	public static final String ACTION_DELETE = GOWIDGET + "_action_delete";
//	public static final String ACTION_UPDATE = GOWIDGET + "_action_update";
//
//	/**
//	 * 通过反射调用GoWidgetView的 OnStart(Bundle)
//	 */
//	public static final String ACTION_START = GOWIDGET + "_action_start";
//	/**
//	 * 通过反射调用GoWidgetView的 OnStop()
//	 */
//	public static final String ACTION_STOP = GOWIDGET + "_action_stop";
//
//	public static final String ACTION_ITEM_CLICK = GOWIDGET + "_action_item_click";
//	public static final String ACTION_VIEW_CLICK = GOWIDGET + "_action_view_click";
//
//	// 完成gowidget设置
//	public static final String ACTION_CONFIG_FINISH = GOWIDGET + "_action_config_finish";
//
//	// 桌面被销毁后通知GOWidget
//	public static final String ACTION_DESTROY_GOWIDGETS = GOWIDGET + "_action_destroy_gowidgets";
//
//	/**
//	 * 直接跳转进入指定gowidget详情界面
//	 */
//	public static final String DATA_PKG_GOTO_SPECIFICK_WIDGET_DETAIL = GOWIDGET
//			+ "_data_pkg_goto_specific_widget_detail";
//
//	// 桌面恢复默认
//	public static final String ACTION_RESET_TO_DEFAULT = GOWIDGET + "_action_reset_to_default";
//
//	// 请求获取焦点，桌面响应请求会跳转到GOwidget所在的屏幕
//	public static final String ACTION_REQUEST_FOCUS = GOWIDGET + "_action_request_focus";
//
//	// 大主题，所有放在桌面的widget更换皮肤
//	public static final String ACTION_CHANGE_WIDGETS_THEME = GOWIDGET
//			+ "_action_change_widgets_theme";
//
//	// 大主题，跳到gowidget未安装页面
//	public static final String ACTION_GOTO_GOWIDGET_FRAME = GOWIDGET
//			+ "_action_goto_gowidget_frame";
//
//	public static final String WIDGET_THEME_KEY = "widget_theme_key";
//
//	/**
//	 * 开关gowidget卸载的命令
//	 */
//	// 开关包名
//	public static final String PKG_GOWIDGET_SWITCH = "com.gau.go.launcherex.gowidget.switchwidget";
//	// 广播将卸载开关
//	public static final String ACTION_ON_OFF_UNINSTALL_BROADCAST = GOWIDGET
//			+ "_uninstall_gowidget_on_off";
//	// //接收是否可以卸载
//	// public static final String ACTION_ON_OFF_RECEIVER_CAN_UNINSTALL =
//	// GOWIDGET + "_can_uninstall_gowidget_on_off";
//	// //接收是否可以卸载，解析字段
//	// public static final String ACTION_ON_OFF_RECEIVER_CAN_UNINSTALL_DATA =
//	// "can_uninstall";
//	// 桌面广播是否卸载开关成功
//	public static final String ACTION_ON_OFF_RECEIVER__UNINSTALL_IS_SUCCESS = GOWIDGET
//			+ "_uninstall_gowidget_on_off_is_success";
//	// 桌面广播是否卸载开关成功，解析字段
//	public static final String ACTION_ON_OFF_RECEIVER_UNINSTALL_IS_SUCCESS = "is_success";

	// for method
	public static final String METHOD_ON_START = "onStart";
	public static final String METHOD_ON_STOP = "onStop";
	// 用户手动删除widget
	public static final String METHOD_ON_DELETE = "onDelete";
	// 删除widget视图，横竖屏切换时调用，非用户删除
	public static final String METHOD_ON_REMOVE = "onRemove";

	// 桌面切换到前后台事件
	public static final String METHOD_ON_PAUSE = "onPause";
	public static final String METHOD_ON_RESUME = "onResume";

	// 进入桌面显示区域
	public static final String METHOD_ON_ENTER = "onEnter";
	// 离开桌面显示区域
	public static final String METHOD_ON_LEAVE = "onLeave";

	// 应用主题
	public static final String METHOD_ON_APPLY_THEME = "onApplyTheme";

	// 获取\删除统计信息和统计发生错误
	public static final String METHOD_ON_GET_STATISTIC = "onGetStatistic";
	public static final String METHOD_ON_CLEAR_STATISTIC = "onClearStatistic";
	public static final String METHOD_ON_ERROR_STATISTIC = "onErrorStatistic";

	// for intent
}
