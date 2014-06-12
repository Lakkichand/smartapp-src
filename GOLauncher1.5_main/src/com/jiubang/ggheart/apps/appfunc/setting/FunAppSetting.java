package com.jiubang.ggheart.apps.appfunc.setting;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

import com.go.util.file.media.FileEngine;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.ISettingObserver;
import com.jiubang.ggheart.data.Setting;
import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.ggheart.data.model.GoSettingDataModel;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.plugin.mediamanagement.MediaManagementOpenChooser;

/**
 * 功能表设置数据
 * 
 * @author 周玉
 * @version 1.0
 */
public class FunAppSetting {
	// 设置的观察者
	ArrayList<ISettingObserver> mISettingObservers = null;
	// 通知id
	public final static int RESETALL = 0; // 回复默认设置
	public final static int INDEX_MENUAPPSTYLE = 1;
	public final static int INDEX_TURNSCREENDIRECTION = 2;
	public final static int INDEX_APPNAMEVISIABLE = 3;
	public final static int INDEX_LINECOLUMNNUM = 4;
	public final static int INDEX_BACKGROUNDPICPATH = 5;
	public final static int INDEX_BGSWITCH = 6;
	public final static int INDEX_SORTTYPE = 7;
	public final static int INDEX_SHOWNEGLECTAPP = 8;
	public final static int INDEX_INOUTEFFECT = 9;
	public final static int INDEX_ICONEFFECT = 10;
	public final static int INDEX_SCROLL_LOOP = 11;
	public final static int INDEX_BLUR_BACKGROUND = 12;
	public final static int INDEX_SHOW_TAB_ROW = 13;
	public final static int INDEX_VERTICAL_SCROLL_EFFECT = 14;
	public final static int INDEX_SHOW_SEARCH = 15;
	public final static int INDEX_TAB_BOTTOM_BG = 16;
	public final static int INDEX_APP_UPDATE = 17;
	public final static int INDEX_SHOW_HOME_KEY_ONLY = 18;
	public final static int INDEX_SHOW_ACTION_BAR = 19;
	public final static int INDEX_GLIDE_UP_ACTION = 20;
	public final static int INDEX_GLIDE_DOWN_ACTION = 21;

	// 设置项增多时请更改下面这项TODO:去掉这个无语的INDEX_MAX
	public final static int INDEX_MAX = 22;

	/**
	 * 开关
	 */
	public static final int OFF = 0;
	public static final int ON = 1;

	/**
	 * 功能表风格
	 */
	public static final int STYLEORIGINAL = 0;
	public static final int STYLECHARACTER = 1;
	public static final int STYLETAG = 2;
	/**
	 * 排序风格
	 */
	public static final int SORTTYPE_LETTER = 0;
	public static final int SORTTYPE_TIMENEAR = 1;
	public static final int SORTTYPE_TIMEREMOTE = 2;
	public static final int SORTTYPE_FREQUENCY = 3;
	/**
	 * 翻屏方向
	 */
	public static final int SCREENMOVEVERTICAL = 0;
	public static final int SCREENMOVEHORIZONTAL = 1;
	/**
	 * 是否显示程序名
	 */
	public static final int APPNAMEVISIABLENO = 0;
	public static final int APPNAMEVISIABLEYES = 1;
	/**
	 * 行列数设置
	 */
	public static final int LINECOLUMNNUMXY_AUTO_FIT = 0; // 自适应
	public static final int LINECOLUMNNUMXY_SPARSE = 1; // 疏 (4*4)
	public static final int LINECOLUMNNUMXY_MIDDLE = 2; // 中(4*5)
	public static final int LINECOLUMNNUMXY_THICK = 3; // 密 (5*5)
	public static final int LINECOLUMNNUMXY_MIDDLE_2 = 4; // 中(5*4)
	public static final int LINECOLUMNNUMXY_DIY = 5; // 自定义
	/**
	 * 是否忽略显示锁定的程序
	 */
	public static final int NEGLECTAPPS = 0;
	public static final int SHOWAPPS = 1;
	/**
	 * 功能表设置
	 */
	public static final int MENUAPPDEFAULTSETTING = 0;
	public static final int MENUAPPCURRENTSETTING = 1;
	/**
	 * 默认背景路径
	 */
	public static final String DEFAULTBGPATH = "0";
	/**
	 * 背景类型: 0和1在之前的版本使用，目前不再使用
	 */
	public static final int BG_NON = 2;
	public static final int BG_DEFAULT = 3;
	public static final int BG_GO_THEME = 4;
	public static final int BG_CUSTOM = 5;

	/**
	 * 功能表gostore、gotheme和gowidget假图标类型
	 */
	public static final int FUNC_APP_TYPE_GOSTORE = 0;
	public static final int FUNC_APP_TYPE_GOTHEME = 1;
	public static final int FUNC_APP_TYPE_GOWIDGET = 2;

	/**
	 * 功能表gostore、gotheme和gowidget假图标类型数据在sharedpreference中的字段名
	 */
	public static final String FUNC_APP_GOSTORE_ISSHOW = "FUNC_APP_GOSTORE_ISSHOW";
	public static final String FUNC_APP_GOTHEME_ISSHOW = "FUNC_APP_GOTHEME_ISSHOW";
	public static final String FUNC_APP_GOWIDGET_ISSHOW = "FUNC_APP_GOWIDGET_ISSHOW";
	public static final String FUNC_APP_SEARCH_ISSHOW = "FUNC_APP_SEARCH_ISSHOW";
	/**
	 * 是否显示功能表资源管理按钮
	 */
	public static final String FUNC_APP_SHOW_MEDIAMAGEMENT = "FUNC_APP_SHOW_MEDIAMAGEMENT";
	/**
	 * 默认音乐播放器
	 */
	public static final String FUNC_APP_MEDIAMAGEMENT_MUSIC = "FUNC_APP_MEDIAMAGEMENT_MUSIC";
	/**
	 * 默认图片浏览器
	 */
	public static final String FUNC_APP_MEDIAMAGEMENT_IMAGE = "FUNC_APP_MEDIAMAGEMENT_IMAGE";

	public static final int SCREEN_SCROLL_VERTICAL = 0;
	public static final int SCREEN_SCROLL_HORIZONTAL = 1;
	
	
	public static final int SCREEN_SCROLL_HORIZONTAL_LOOP = 2;
	public static final int SCREEN_SCROLL_VERTICAL_LOOP = 3;

	private GoSettingDataModel mDataModel;
	private ThemeManager mThemeManager;
	private ISettingObserver mObserver;
	private Context mContext;

	/**
	 * 构造函数
	 * 
	 * @param context
	 *            上下文
	 * @param dbname
	 *            数据库名
	 */
	public FunAppSetting(Context context, GoSettingDataModel dataModel, ThemeManager themeManager) {
		mContext = context;
		mDataModel = dataModel;
		mThemeManager = themeManager;
		checkAppSetting();
		initTaskManagerWidgetNotify();
	}

	/**
	 * 初始化任务管理器小工具通信设置
	 */
	private void initTaskManagerWidgetNotify() {
		// final String requestIsShowLock =
		// "com.gau.go.launcherex.gowidget.taskmanager.action.requestIsShowLock";
		// final String setShowLock =
		// "com.gau.go.launcherex.gowidget.taskmanager.action.setShowLock";
		// BroadcastReceiver receiver = new BroadcastReceiver()
		// {
		// @Override
		// public void onReceive(Context context, Intent intent)
		// {
		// LogUnit.i(intent.getAction());
		// if (intent.getAction().equals(setShowLock))
		// {
		// int extra = intent.getIntExtra("setShowLock", 0);
		// if (extra != 1)
		// {
		// setShowNeglectApp(0);
		// } else
		// {
		// setShowNeglectApp(1);
		// }
		// } else if (intent.getAction().equals(requestIsShowLock))
		// {
		// notifyTaskManagerWidget(getShowNeglectApp());
		// }
		// }
		// };
		// IntentFilter filter = new IntentFilter();
		// filter.addAction(requestIsShowLock);
		// filter.addAction(setShowLock);
		// filter.addAction(GoWidgetConstant.ACTION_DESTROY_GOWIDGETS);
		// mContext.unregisterReceiver(receiver);
		// mContext.registerReceiver(receiver, filter);
		// LogUnit.i("FunAppSetting");
		if (mObserver != null) {
			unsetISettingObserver(mObserver);
		}
		mObserver = new ISettingObserver() {
			@Override
			public void onSettingChange(int index, int value, Object object) {
				if (index == INDEX_SHOWNEGLECTAPP) {
					notifyTaskManagerWidget(value);
				}
			}
		};
		setISettingObserver(mObserver);
	}

	private void notifyTaskManagerWidget(int isShowNeglectApp) {
		//		final String respondIsShowLock = "com.gau.go.launcherex.gowidget.taskmanager.action.respondIsShowLock";
		int showNeglectApp = getShowNeglectApp();
		Intent intent = new Intent(ICustomAction.ACTION_RESPOND_IS_SHOWLOCK);
		intent.putExtra("isShowLock", showNeglectApp);
		GoLauncher context = GoLauncher.getContext();
		if (context != null) {
			context.sendBroadcast(intent);
		}
	}

	/**
	 * 重设设置
	 */
	@SuppressWarnings("unused")
	private void resetSettings() {
		// // setMenuAppStyle(menuAppStyle);
		// setAppNameVisiable(APPNAMEVISIABLEYES);
		// setTurnScreenDirection(SCREENMOVEHORIZONTAL);
		// setBackgroundPicPath(null);
		// setLineColumnNum(LINECOLUMNNUMXY_SPARSE);
		// setBgVisiable(OFF);
		// mDataModel.resetAppSetting();
		// 通知观察者
		// if (null != mISettingObserver) {
		// mISettingObserver.onSettingChange(RESETALL, 0, null);
		// }
	}

	/**
	 * 设置观察者
	 * 
	 * @param iSettingObserver
	 *            观察者
	 */
	public void setISettingObserver(ISettingObserver iSettingObserver) {
		if (this.mISettingObservers == null) {
			this.mISettingObservers = new ArrayList<ISettingObserver>();
		}
		this.mISettingObservers.add(iSettingObserver);
	}

	/**
	 * 反注册
	 * 
	 * @param iSettingObserver
	 */
	public void unsetISettingObserver(ISettingObserver iSettingObserver) {
		if (this.mISettingObservers == null) {
			return;
		}

		for (ISettingObserver observer : mISettingObservers) {
			if (observer == iSettingObserver) {
				mISettingObservers.remove(observer);
				return;
			}
		}
	}

	/**
	 * 通知观察者
	 */
	private void infoObservers(int index, int value, Object object) {
		if (null != mISettingObservers) {
			for (ISettingObserver observer : mISettingObservers) {
				if (observer != null) {
					observer.onSettingChange(index, value, object);
				}
			}
		}
	}

	/**
	 * 设置功能表风格
	 * 
	 * @param menuAppStyle
	 *            功能表风格
	 */
	public void setMenuAppStyle(int menuAppStyle) {
		// 主题不相关，直接操作默认主题的设置
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.MENUAPPSTYLE,
				Integer.toString(menuAppStyle));

		// 通知观察者
		infoObservers(INDEX_MENUAPPSTYLE, menuAppStyle, null);
	}

	private int getValueFromDefaultThemePakage(int id, int defaultValue) {
		String str = getAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, id,
				String.valueOf(defaultValue));
		int value = defaultValue;
		try {
			value = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return value;
	}

	public void setAppFuncSetting(String pkname, int key, String value) {
		mDataModel.setAppFuncSetting(pkname, key, value);
	}

	public String getAppFuncSetting(String pkname, int key, String defaultValue) {
		String value = mDataModel.getAppFuncSetting(pkname, key);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	public int getInOutEffect() {
		return getValueFromDefaultThemePakage(Setting.INOUTEFFECT, AppSettingDefault.INOUTEFFECT);
	}

	public void setInOutEffect(int inOutEffect) {
		StatisticsData.saveIntData(mContext, IPreferencesIds.APPFUNC_INOUT_EFFECT, inOutEffect);
		// 主题不相关，直接操作默认主题的设置
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.INOUTEFFECT,
				Integer.toString(inOutEffect));

		// 通知观察者
		infoObservers(INDEX_INOUTEFFECT, inOutEffect, null);
	}

	public int getIconEffect() {
		return getValueFromDefaultThemePakage(Setting.ICONEFFECT, AppSettingDefault.ICONEFFECT);
	}

	public void setIconEffect(int iconEffect) {
		StatisticsData.saveIntData(mContext, IPreferencesIds.APPFUNC_HORIZONTAL_EFFECT, iconEffect);
		// 主题不相关，直接操作默认主题的设置
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.ICONEFFECT,
				Integer.toString(iconEffect));

		// 通知观察者
		infoObservers(INDEX_ICONEFFECT, iconEffect, null);
	}

	/**
	 * 取得功能表风格
	 * 
	 * @param menuAppDefaultSetting
	 *            是否默认设置
	 * @return 功能表风格值
	 */
	public int getMenuAppStyle() {
		return getValueFromDefaultThemePakage(Setting.MENUAPPSTYLE, AppSettingDefault.MENUAPPSTYLE);
	}

	/**
	 * 设置翻屏方向
	 * 
	 * @param turnScreenDirection
	 *            功能表风格
	 */
	public void setTurnScreenDirection(int turnScreenDirection) {
		// 主题不相关，直接操作默认主题的设置
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.TURNSCREENDIRECTION,
				Integer.toString(turnScreenDirection));

		// 通知观察者
		infoObservers(INDEX_TURNSCREENDIRECTION, turnScreenDirection, null);
	}

	/**
	 * 取得翻屏方向
	 * 
	 * @param menuAppDefaultSetting
	 *            是否默认设置
	 * @return 功能表风格
	 */
	public int getTurnScreenDirection() {
		return getValueFromDefaultThemePakage(Setting.TURNSCREENDIRECTION,
				AppSettingDefault.TURNSCREENDIRECTION);
	}

	/**
	 * 功能表背景设置(重用BgVisible)
	 * 
	 * @return 功能表背景设置类型：2 无 3主题默认 4 GO主题 5 自定义
	 */
	public int getBgSetting() {
		// 主题相关
		ThemeBean themeBean = null;
		if (mThemeManager != null) {
			themeBean = mThemeManager.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		}
		String str = null;
		if (null != themeBean) {
			// 其他主题
			str = getAppFuncSetting(mThemeManager.getCurThemePackage(), Setting.BGVISIABLE, null);
		} else {
			// 默认主题
			str = getAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.BGVISIABLE, null);
		}

		themeBean = null;
		if (null == str) {
			return AppSettingDefault.BACKGROUNDVISIABLE;
		}

		return Integer.parseInt(str);
	}

	/**
	 * 设置功能表tab和底座背景
	 * 
	 * @return
	 */
	public String getTabHomeBgSetting() {
		// 主题相关
		ThemeBean themeBean = null;
		if (mThemeManager != null) {
			themeBean = mThemeManager.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		}
		String str = null;
		if (null != themeBean) {
			// 其他主题
			str = getAppFuncSetting(mThemeManager.getCurThemePackage(), Setting.TAB_HOME_BG, null);
		} else {
			// 默认主题
			str = getAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.TAB_HOME_BG, null);
		}

		themeBean = null;
		if (null == str) {
			return ThemeManager.DEFAULT_THEME_PACKAGE;
		}
		return str;
	}

	/**
	 * 获取功能表指示器
	 * 
	 * @return
	 */
	public String getIndicatorSetting() {
		// 主题相关
		ThemeBean themeBean = null;
		if (mThemeManager != null) {
			themeBean = mThemeManager.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		}
		String str = null;
		if (null != themeBean) {
			// 其他主题
			str = getAppFuncSetting(mThemeManager.getCurThemePackage(), Setting.INDICATOR_STYLE,
					null);
		} else {
			// 默认主题
			str = getAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.INDICATOR_STYLE,
					null);
		}

		themeBean = null;
		if (null == str) {
			return ThemeManager.DEFAULT_THEME_PACKAGE;
		}
		return str;
	}

	/**
	 * 设置功能表指示器
	 * 
	 * @return
	 */
	public void setIndicatorSetting(String style) {
		// 主题相关
		ThemeBean themeBean = null;
		if (mThemeManager != null) {
			themeBean = mThemeManager.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		}
		if (null != themeBean) {
			// 其他主题
			setAppFuncSetting(mThemeManager.getCurThemePackage(), Setting.INDICATOR_STYLE, style);
		} else {
			// 默认主题
			setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.INDICATOR_STYLE, style);
		}

		themeBean = null;
		GoLauncher.sendBroadcastHandler(this, IDiyMsgIds.APPDRAWER_INDICATOR_THEME_CHANGE, -1,
				null, null);

	}

	/**
	 * 设置功能表TAB bottom背景
	 * 
	 * @param packageName
	 * 
	 */
	public void setTabHomeBgSetting(String packageName) {
		// 主题相关
		ThemeBean themeBean = null;
		if (mThemeManager != null) {
			themeBean = mThemeManager.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		}

		if (null != themeBean) {
			// 其他主题
			setAppFuncSetting(mThemeManager.getCurThemePackage(), Setting.TAB_HOME_BG, packageName);
		} else {
			// 默认主题
			setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.TAB_HOME_BG, packageName);
		}
		themeBean = null;
		GoLauncher.sendBroadcastHandler(this, IDiyMsgIds.APPDRAWER_TAB_HOME_THEME_CHANGE, -1, null,
				null);
		// 通知观察者
		// infoObservers(INDEX_BGSWITCH, bgVisiable, null);
	}

	/**
	 * 设置功能表背景(重用BgVisible)
	 * 
	 * @param bgVisiable
	 *            : 0 无 1 主题默认 2 GO主题 3 自定义
	 */
	public void setBgSetting(int bgVisiable) {
		// 主题相关
		ThemeBean themeBean = null;
		if (mThemeManager != null) {
			themeBean = mThemeManager.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		}

		if (null != themeBean) {
			// 其他主题
			setAppFuncSetting(mThemeManager.getCurThemePackage(), Setting.BGVISIABLE,
					Integer.toString(bgVisiable));
		} else {
			// 默认主题
			setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.BGVISIABLE,
					Integer.toString(bgVisiable));
		}
		themeBean = null;

		// 通知观察者
		infoObservers(INDEX_BGSWITCH, bgVisiable, null);
	}

	/**
	 * 获取排序风格
	 * 
	 * @return 排序风格
	 */
	public int getSortType() {
		return getValueFromDefaultThemePakage(Setting.SORTTYPE, AppSettingDefault.SORTTYPE);
	}

	/**
	 * 设置排序风格
	 * 
	 * @param sortType
	 *            排序风格
	 */
	public void setSortType(int sortType, final boolean notify) {
		// 主题不相关，直接操作默认主题的设置
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.SORTTYPE,
				Integer.toString(sortType));

		if (notify) {
			// 通知观察者
			infoObservers(INDEX_SORTTYPE, sortType, null);
		}
	}

	/**
	 * 取得是否显示白名单程序
	 * 
	 * @param menuAppDefaultSetting
	 *            是否默认设置
	 * @return 功能表风格
	 */
	public int getShowNeglectApp() {
		return getValueFromDefaultThemePakage(Setting.SHOWNEGLECTAPP,
				AppSettingDefault.SHOWNEGLECTAPPS);
	}

	/**
	 * 设置是否显示锁定列表程序
	 * 
	 * @param showNeglectApp
	 *            是否显示锁定列表程序
	 */
	public void setShowNeglectApp(int showNeglectApp) {
		// 主题不相关，直接操作默认主题的设置
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.SHOWNEGLECTAPP,
				Integer.toString(showNeglectApp));

		// 通知观察者
		infoObservers(INDEX_SHOWNEGLECTAPP, showNeglectApp, null);
	}

	/**
	 * 取得是否显示程序名
	 * 
	 * @param menuAppDefaultSetting
	 *            是否默认设置
	 * @return 功能表风格
	 */
	public int getAppNameVisiable() {
		return getValueFromDefaultThemePakage(Setting.APPNAMEVISIABLE,
				AppSettingDefault.APPNAMEVISIABLE);
	}

	/**
	 * 设置是否显示程序名
	 * 
	 * @param appNameVisiable
	 *            是否显示程序名
	 */
	public void setAppNameVisiable(int appNameVisiable) {
		// 主题不相关，直接操作默认主题的设置
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.APPNAMEVISIABLE,
				Integer.toString(appNameVisiable));

		// 通知观察者
		infoObservers(INDEX_APPNAMEVISIABLE, appNameVisiable, null);
	}

	/**
	 * 设置功能表中是否显示搜素界面
	 * 
	 * @author huyong
	 * @param isShow
	 */
	public void setShowSearch(int isShow) {
		// 主题不相关，直接操作默认主题的设置
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.SHOW_SEARCH,
				Integer.toString(isShow));

		// 通知观察者
		infoObservers(INDEX_SHOW_SEARCH, isShow, null);
	}

	/**
	 * 设置功能表中是否在应用图标上显示更新提示
	 * 
	 * @author huyong
	 * @param isShow
	 */
	public void setAppUpdate(int isShow) {
		// 主题不相关，直接操作默认主题的设置
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.APPUPDATE,
				Integer.toString(isShow));

		// 通知观察者
		infoObservers(INDEX_APP_UPDATE, isShow, null);
	}

	/**
	 * 判断是否显示功能表搜素界面
	 * 
	 * @author huyong
	 * @return
	 */
	public int getShowSearch() {
		return getValueFromDefaultThemePakage(Setting.SHOW_SEARCH,
				AppSettingDefault.APP_SEARCH_VISIABLE);
	}

	/**
	 * 判断是否在应用图标上显示更新提示
	 * 
	 * @author huyong
	 * @return
	 */
	public int getAppUpdate() {
		return getValueFromDefaultThemePakage(Setting.APPUPDATE, AppSettingDefault.PROUPDATEAPP);
	}

	/**
	 * 判断是否显示操作栏
	 * 
	 * @author kingyang
	 * @return
	 */
	public int getShowActionBar() {
		return getValueFromDefaultThemePakage(Setting.SHOW_ACTION_BAR,
				AppSettingDefault.SHOW_ACTION_BAR);
	}

	/**
	 * 判断是否只显示Home键
	 * 
	 * @author kingyang
	 * @return
	 */
	public int getShowHomeKeyOnly() {
		return getValueFromDefaultThemePakage(Setting.SHOW_HOME_KEY_ONLY,
				AppSettingDefault.SHOW_HOME_KEY_ONLY);
	}

	/**
	 * 取得行列数设置
	 * 
	 * @param menuAppDefaultSetting
	 *            是否默认设置
	 * @return 行列数设置
	 */
	public int getLineColumnNum() {
		// 主题相关
		// ThemeManager themeManager = AppCore.getInstance(mContext)
		// .getThemeManager();
		// ThemeBean themeBean = themeManager
		// .getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		// String str = null;
		// if (null == themeBean) {
		// // 默认主题
		// str = getAppSetting(ThemeManager.DEFAULT_THEME_PACKAGE,
		// Setting.LINECOLUMNNUM);
		// } else {
		// // 其他主题
		// str = getAppSetting(themeBean.getPackageName(),
		// Setting.LINECOLUMNNUM);
		// }
		//
		// if (null == str) {
		// return 0;
		// }
		// 主题无关,只从默认主题取

		return getValueFromDefaultThemePakage(Setting.LINECOLUMNNUM,
				AppSettingDefault.LINECOLUMNNUM);
	}

	/**
	 * 设置行列数
	 * 
	 * @param lineColumnNum
	 *            行列数设置
	 */
	public void setLineColumnNum(int lineColumnNum) {
		if (lineColumnNum != LINECOLUMNNUMXY_AUTO_FIT) {
			if (lineColumnNum < LINECOLUMNNUMXY_SPARSE) {
				lineColumnNum = LINECOLUMNNUMXY_SPARSE;
			}
			if (lineColumnNum > LINECOLUMNNUMXY_MIDDLE_2) {
				if (lineColumnNum == LINECOLUMNNUMXY_DIY) {
					lineColumnNum = LINECOLUMNNUMXY_DIY;
				} else {
					lineColumnNum = LINECOLUMNNUMXY_THICK;
				}
			}
		}

		// 主题相关
		// ThemeManager themeManager = AppCore.getInstance(mContext)
		// .getThemeManager();
		// ThemeBean themeBean = themeManager
		// .getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		// if (null == themeBean) {
		// // 默认主题
		// setAppSetting(ThemeManager.DEFAULT_THEME_PACKAGE,
		// Setting.LINECOLUMNNUM, Integer.toString(lineColumnNum));
		// } else {
		// // 其他主题
		// setAppSetting(themeBean.getPackageName(), Setting.LINECOLUMNNUM,
		// Integer.toString(lineColumnNum));
		// }
		// 主题无关: 只修改默认主题
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.LINECOLUMNNUM,
				Integer.toString(lineColumnNum));

		// 通知观察者
		infoObservers(INDEX_LINECOLUMNNUM, lineColumnNum, null);
	}

	/**
	 * 获取行数
	 * 
	 * @return
	 */
	public int getRowNum() {
		return getValueFromDefaultThemePakage(Setting.APPFUNC_ROWNUM,
				AppSettingDefault.APPFUNC_ROWNUM);
	}

	/**
	 * 设置行数
	 * 
	 * @param row
	 */
	public void setRowNum(int row) {
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.APPFUNC_ROWNUM,
				Integer.toString(row));
	}

	/**
	 * 获取列数
	 * 
	 * @return
	 */
	public int getColNum() {
		return getValueFromDefaultThemePakage(Setting.APPFUNC_COLNUM,
				AppSettingDefault.APPFUNC_COLNUM);
	}

	/**
	 * 设置列数
	 * 
	 * @param col
	 */
	public void setColNum(int col) {
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.APPFUNC_COLNUM,
				Integer.toString(col));
	}

	/**
	 * 取得背景图片存放位置
	 * 
	 * @param menuAppDefaultSetting
	 *            是否默认设置
	 * @return 背景图片存放位置
	 */
	public String getBackgroundPicPath() {
		// 主题相关
		ThemeBean themeBean = null;
		if (mThemeManager != null) {
			themeBean = mThemeManager.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		}

		String str = null;
		if (null != themeBean) {
			// 其他主题
			str = getAppFuncSetting(mThemeManager.getCurThemePackage(), Setting.BACKGROUNDPICPATH,
					null);
		} else {
			// 默认主题
			str = getAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.BACKGROUNDPICPATH,
					null);
		}

		return str;
	}

	/**
	 * 设置背景图片存放位置
	 * 
	 * @param backgroundPicPath
	 *            背景图片存放位置
	 */
	public void setBackgroundPicPath(String backgroundPicPath) {
		if (null == backgroundPicPath) {
			return;
		}

		// 主题相关
		ThemeBean themeBean = null;
		if (mThemeManager != null) {
			themeBean = mThemeManager.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		}

		if (null != themeBean) {
			// 其他主题
			setAppFuncSetting(mThemeManager.getCurThemePackage(), Setting.BACKGROUNDPICPATH,
					backgroundPicPath);
		} else {
			// 默认主题
			setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.BACKGROUNDPICPATH,
					backgroundPicPath);
		}
		themeBean = null;

		// 通知观察者
		infoObservers(INDEX_BACKGROUNDPICPATH, 0, backgroundPicPath);
	}

	/**
	 * 获取功能表设置信息
	 * 
	 * @author huyong
	 * @param settingItem
	 *            设置项
	 * @return 返回对应设置项的值
	 */
	// public String getAppSetting(final String packageName, final int
	// settingItem)
	// {
	// // return mDataModel.getAppSetting(packageName, settingItem);
	// return mDataModel.getAppFuncSetting(pkname, settingItem);
	// }

	/**
	 * 更新功能表设置信息
	 * 
	 * @author huyong
	 * @param settingItem
	 *            设置项
	 * @param value
	 *            设置值
	 */
	// public void setAppSetting(final String packageName, final int
	// settingItem,
	// final String value)
	// {
	// mDataModel.setAppSetting(packageName, settingItem, value);
	// }

	/**
	 * 检查数据库中功能表的设置数据，如果为空，填入初始化数据
	 */
	public void checkAppSetting() {
		// 主题相关
		ThemeBean themeBean = null;
		if (mThemeManager != null) {
			themeBean = mThemeManager.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		}
		// 初始设置
		if (null != themeBean) {
			mDataModel.addAppFuncSetting(mThemeManager.getCurThemePackage());
		} else {
			mDataModel.addAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE);
		}
		themeBean = null;
	}

	/**
	 * 取得是否横向循环滚屏
	 */
	public int getScrollLoop() {
		return getValueFromDefaultThemePakage(Setting.SCROLL_LOOP, AppSettingDefault.SCROLL_LOOP);
	}

	/**
	 * 设置是否横向循环滚屏
	 */
	public void setScrollLoop(int scrollLoop) {
		// 主题不相关，直接操作默认主题的设置
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.SCROLL_LOOP,
				Integer.toString(scrollLoop));

		// 通知观察者
		infoObservers(INDEX_SCROLL_LOOP, scrollLoop, null);
	}

	/**
	 * 取得是否模糊背景
	 */
	public int getBlurBackground() {
		// 主题相关
		ThemeBean themeBean = null;
		if (mThemeManager != null) {
			themeBean = mThemeManager.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		}

		String str = null;
		if (null != themeBean) {
			// 其他主题
			str = getAppFuncSetting(mThemeManager.getCurThemePackage(), Setting.BLUR_BACKGROUND,
					null);
		} else {
			// 默认主题
			str = getAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.BLUR_BACKGROUND,
					null);
		}
		themeBean = null;

		// String str = getAppSetting(ThemeManager.DEFAULT_THEME_PACKAGE,
		// Setting.BLUR_BACKGROUND);
		if (null == str) {
			return AppSettingDefault.BLUR_BACKGROUND;
		}

		return Integer.parseInt(str);
	}

	/**
	 * 设置是否模糊背景
	 * 
	 */
	public void setBlurBackground(int blur) {
		// 主题相关
		ThemeBean themeBean = null;
		if (mThemeManager != null) {
			themeBean = mThemeManager.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		}

		if (null != themeBean) {
			// 其他主题
			setAppFuncSetting(mThemeManager.getCurThemePackage(), Setting.BLUR_BACKGROUND,
					Integer.toString(blur));
		} else {
			// 默认主题
			setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.BLUR_BACKGROUND,
					Integer.toString(blur));
		}
		themeBean = null;
		// // 主题不相关，直接操作默认主题的设置
		// setAppSetting(ThemeManager.DEFAULT_THEME_PACKAGE,
		// Setting.BLUR_BACKGROUND, Integer.toString(blur));

		// 通知观察者
		infoObservers(INDEX_BLUR_BACKGROUND, blur, null);
	}

	/**
	 * 取得是否显示tab栏
	 */
	public int getShowTabRow() {
		return getValueFromDefaultThemePakage(Setting.SHOW_TAB_ROW, AppSettingDefault.SHOW_TAB_ROW);
	}

	/**
	 * 设置是否显示tab栏
	 */
	public void setShowTabRow(int showTabRow) {
		// 主题不相关，直接操作默认主题的设置
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.SHOW_TAB_ROW,
				Integer.toString(showTabRow));

		// 通知观察者
		infoObservers(INDEX_SHOW_TAB_ROW, showTabRow, null);
	}

	public int getVerticalScrollEffect() {
		return getValueFromDefaultThemePakage(Setting.VERTICAL_SCROLL_EFFECT,
				AppSettingDefault.VERTICAL_SCROLL_EFFECT);
	}

	public void setVerticalScrollEffect(int effect) {
		StatisticsData.saveIntData(mContext, IPreferencesIds.APPFUNC_VERTICAL_EFFECT, effect);
		// 主题不相关，直接操作默认主题的设置
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.VERTICAL_SCROLL_EFFECT,
				Integer.toString(effect));

		// 通知观察者
		infoObservers(INDEX_VERTICAL_SCROLL_EFFECT, effect, null);
	}

	/**
	 * 
	 * @param show
	 * @param type
	 *            0表示gostore的数据，1表示goTheme的数据
	 */
	public void setShowGoStoreAndGoTheme(boolean show, int type) {
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		if (type == FUNC_APP_TYPE_GOSTORE) {
			// 设置gostore
			preferences.putBoolean(FUNC_APP_GOSTORE_ISSHOW, show);
		} else if (type == FUNC_APP_TYPE_GOSTORE) {
			preferences.putBoolean(FUNC_APP_GOTHEME_ISSHOW, show);
		} else {
			preferences.putBoolean(FUNC_APP_GOWIDGET_ISSHOW, show);
		}
		preferences.commit();
	}

	public boolean getShowGoStoreAndGoTheme(int type) {
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		if (type == FUNC_APP_TYPE_GOSTORE) {
			// 获取gostore设置
			return preferences.getBoolean(FUNC_APP_GOSTORE_ISSHOW, true);
		} else if (type == FUNC_APP_TYPE_GOTHEME) {
			return preferences.getBoolean(FUNC_APP_GOTHEME_ISSHOW, true);
		} else {
			return preferences.getBoolean(FUNC_APP_GOWIDGET_ISSHOW, true);
		}
	}

	/**
	 * 设置是否显示资源管理按钮
	 * 
	 * @param show
	 */
	public void setShowMediaManagement(boolean show) {
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		preferences.putBoolean(FUNC_APP_SHOW_MEDIAMAGEMENT, show);
		preferences.commit();
	}

	/**
	 * 是否显示功能表资源管理
	 * 
	 * @return
	 */
	public boolean isShowMediaManagement() {
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		return preferences.getBoolean(FUNC_APP_SHOW_MEDIAMAGEMENT, false);
	}

	/**
	 * 保存媒体文件默认打开方式
	 * 
	 * @param type
	 *            :媒体文件类型
	 * @param uri
	 *            :打开方式值
	 */
	public void saveMediaOpenWay(int type, String uri) {
		switch (type) {
			case FileEngine.TYPE_AUDIO : {
				setAudioOpenWay(uri);
				break;
			}
			case FileEngine.TYPE_IMAGE : {
				setImageOpenWay(uri);
				break;
			}
			default :
				break;
		}

	}

	/**
	 * 获取媒体文件默认打开方式
	 * 
	 * @param type
	 *            :媒体文件类型
	 */
	public String getMediaOpenWay(int type) {
		String res = MediaManagementOpenChooser.APP_NONE;
		switch (type) {
			case FileEngine.TYPE_AUDIO : {
				res = getAudioOpenWay();
				break;
			}
			case FileEngine.TYPE_IMAGE : {
				res = getImageOpenWay();
				break;
			}
			default :
				break;
		}
		return res;
	}

	public int[] getAppInOutCustomRandomEffect() {
		return getAppCustomRandomEffect(Setting.APPINOUTCUSTOMRANDOMEFFECT);
	}

	public int[] getAppIconCustomRandomEffect() {
		return getAppCustomRandomEffect(Setting.APPICONCUSTOMEFFECTSETTING);
	}

	@SuppressWarnings("null")
	public int[] getAppCustomRandomEffect(int id) {
		int[] effects = null;
		String value = getAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, id, null);
		if (value != null) {
			String[] items = value.split(";");
			if (items != null) {
				effects = new int[items.length];
				for (int i = 0; i < items.length; i++) {
					try {
						effects[i] = Integer.parseInt(items[i]);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return effects != null ? effects : new int[] { -1 };

	}

	public void setAppInOutCustomRandomEffect(int[] effects) {
		setAppCustomRandomEffect(effects, Setting.APPINOUTCUSTOMRANDOMEFFECT);
	}

	public void setAppIconCustomRandomEffect(int[] effects) {
		setAppCustomRandomEffect(effects, Setting.APPICONCUSTOMEFFECTSETTING);
	}

	public void setAppCustomRandomEffect(int[] effects, int id) {
		if (effects == null) {
			return;
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < effects.length; i++) {
			buffer.append(effects[i] + ";");
		}
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, id, buffer.toString());
	}

	/**
	 * 设置功能表中是否只显示底部操作栏
	 * 
	 * @author kingyang
	 * @param flag
	 */
	public void setShowActionBar(int flag) {
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.SHOW_ACTION_BAR,
				Integer.toString(flag));

		// 通知观察者
		infoObservers(INDEX_SHOW_ACTION_BAR, flag, null);
	}

	/**
	 * 设置功能表中是否只显示Home键
	 * 
	 * @author kingyang
	 * @param flag
	 */
	public void setShowHomeKeyOnly(int flag) {
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.SHOW_HOME_KEY_ONLY,
				Integer.toString(flag));

		// 通知观察者
		infoObservers(INDEX_SHOW_HOME_KEY_ONLY, flag, null);
	}

	/**
	 * 通知功能表背景恢复默认
	 * 
	 * @author yangbing
	 */
	public void resetFuncBgObserver() {

		infoObservers(INDEX_BGSWITCH, BG_DEFAULT, null);
	}

	/**
	 * 获取默认图片打开方式
	 * 
	 * @author yangbing
	 * @return
	 */
	public String getImageOpenWay() {
		return getAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.IMAGE_OPEN_WAY,
				MediaManagementOpenChooser.APP_NONE);
	}

	/**
	 * 获取默认音频打开方式
	 * 
	 * @author yangbing
	 * @return
	 */
	public String getAudioOpenWay() {
		return getAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.AUDIO_OPEN_WAY,
				MediaManagementOpenChooser.APP_NONE);
	}

	/**
	 * 保存默认图片打开方式
	 * 
	 * @author yangbing
	 * @return
	 */
	public void setImageOpenWay(String value) {
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.IMAGE_OPEN_WAY, value);

	}

	/**
	 * 保存默认音频打开方式
	 * 
	 * @author yangbing
	 * @return
	 */
	public void setAudioOpenWay(String value) {
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.AUDIO_OPEN_WAY, value);
	}

	/**
	 * 设置是否启用上滑手势
	 * 
	 * @author yangguanxiang
	 * @param value
	 */
	public void setGlideUpAction(int value) {
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.ENABLE_GLIDE_UP_ACTION,
				String.valueOf(value));
		// 通知观察者
		infoObservers(INDEX_GLIDE_UP_ACTION, value, null);
	}

	/**
	 * 判断是否启用上滑手势
	 * 
	 * @author kingyang
	 * @return
	 */
	public int getGlideUpAction() {
		return getValueFromDefaultThemePakage(Setting.ENABLE_GLIDE_UP_ACTION, OFF);
		//		
		//		int result = OFF;
		//		try {
		//			result = Integer.parseInt(getAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE,
		//					Setting.ENABLE_GLIDE_UP_ACTION, String.valueOf(OFF)));
		//		} catch (NumberFormatException e) {
		//		}
		//		return result;
	}

	/**
	 * 设置是否启用下滑手势
	 * 
	 * @author yangguanxiang
	 * @param value
	 */
	public void setGlideDownAction(int value) {
		setAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.ENABLE_GLIDE_DOWN_ACTION,
				String.valueOf(value));
		// 通知观察者
		infoObservers(INDEX_GLIDE_DOWN_ACTION, value, null);
	}

	/**
	 * 判断是否启用上滑手势
	 * 
	 * @author yangguanxiang
	 * @return
	 */
	public int getGlideDownAction() {
		return getValueFromDefaultThemePakage(Setting.ENABLE_GLIDE_DOWN_ACTION, OFF);

		//		int result = OFF;
		//		try {
		//			result = Integer.parseInt(getAppFuncSetting(ThemeManager.DEFAULT_THEME_PACKAGE,
		//					Setting.ENABLE_GLIDE_DOWN_ACTION, String.valueOf(OFF)));
		//		} catch (NumberFormatException e) {
		//		}
		//		return result;
	}
}