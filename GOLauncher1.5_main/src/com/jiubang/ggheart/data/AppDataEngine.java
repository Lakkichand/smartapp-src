package com.jiubang.ggheart.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xmlpull.v1.XmlPullParser;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.Utilities;
import com.go.util.device.Machine;
import com.go.util.log.LogUnit;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.appfunc.business.AllAppBussiness;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.config.utils.ConfigUtils;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.common.controler.CommonControler;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.info.AppConfigInfo;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ScreenStyleConfigInfo;
import com.jiubang.ggheart.data.model.AppConfigDataModel;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeConfig;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.data.theme.bean.AppDataThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskFolderThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.data.theme.parser.AppThemeParser;
import com.jiubang.ggheart.data.theme.parser.DeskFolderThemeParser;
import com.jiubang.ggheart.data.theme.parser.DeskThemeParser;
import com.jiubang.ggheart.data.theme.parser.IParser;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 负责管理手机应用程序
 * 
 * @author huyong
 * 
 */
//CHECKSTYLE:OFF
public class AppDataEngine implements BroadCasterObserver, ICleanable {
	private static final String LOG_TAG = "AppDataEngine";
	private static final int EVENT_LOAD_ICON = 103; // load图标
	private static final int EVENT_LOAD_ICON_AND_TITLES = 6; // title和icon加载完毕

	private Context mContext;
	private ConcurrentHashMap<ComponentName, AppItemInfo> mAllAppItemsMap; // 手机所有应用程序，map结构，方便快速查找
	private BitmapDrawable mSysAppIcon = null; // 系统机器人图标
	private Boolean mHasLoaded = false; // 是否已经加载过数据
	private Object mHasLoadedLock = new Object(); // 是否已经加载过数据的锁
	private boolean mIsSDCardOK = false; // SD卡是否准备完成
	private volatile boolean mIsIconTitleLoadingFinish = false; // 系统图标和名称是否都已经加载完毕

	private Handler mHandler = null;
	private LoadThread mCurLoadThread; // 加载图片、title等耗时操作时的异步线程

	private AppDataFilter mAppDataFilter = null; // 应用程序过滤器

	private Canvas mCanvas = null;
	private Paint mPaint = null;
	private Matrix mMatrix = null;

	private MonitorSver mMonitorSver = null;

	private static AppDataEngine mAppDataEngineSelf = null;

	private boolean mShowBase;

	public static String MAIN_GOWIDGET_PNAME = "com.gau.go.launcherex.gowidget."; // GoWidget
																					// 过滤的动作
	public static String GOWIDGET_CATEGORY = "android.intent.category.DEFAULT"; // GoWidget包category

	private PorterDuffXfermode mXfermode; //实现遮罩层 (mask)
	
	public static synchronized AppDataEngine getInstance(Context context) {
		if (mAppDataEngineSelf == null) {
			mAppDataEngineSelf = new AppDataEngine(context);
		}

		return mAppDataEngineSelf;
	}

	private AppDataEngine(final Context context) {

		mContext = context;
//		Log.i(LOG_TAG, "construct");
		if (mAllAppItemsMap == null) {
//			Log.i(LOG_TAG, "create mAllAppItemsMap");
			mAllAppItemsMap = new ConcurrentHashMap<ComponentName, AppItemInfo>();
		}

		final Resources resources = context.getResources();
		Bitmap bmp = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_application);
		mSysAppIcon = new BitmapDrawable(resources, bmp);

		// 缩放图片
		if (bmp != null) {
			final Bitmap bitmap = Utilities.createBitmapThumbnail(bmp, mContext);
			mSysAppIcon = new BitmapDrawable(resources, bitmap);
		}

		mCanvas = new Canvas();
		mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
		mPaint = new Paint();
		mMatrix = new Matrix();
		mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
		initAppInfo();

		initAppDataFilter();

		initHandler();

		initMonitorSver();

		initGoStoreAndGoThemeIcon();
		// 在所有程序的集合中加入应用游戏中心相关的假图标
		initAppGameCenterIcon();
	}

	private void initMonitorSver() {
		mMonitorSver = new MonitorSver(mContext);
		// 数据注册监控服务
		mMonitorSver.registerObserver(this);
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case EVENT_LOAD_ICON_AND_TITLES : {
						// 同步至主线程加载图片
						TitleAndIcon titleAndIcon = (TitleAndIcon) msg.obj;
						if (titleAndIcon != null) {
							AppItemInfo itemInfo = titleAndIcon.getAppItemInfo();
							if (itemInfo != null) {
								itemInfo.setTitle(titleAndIcon.getTitle());
								itemInfo.setIcon(titleAndIcon.getIcon());
							}
							titleAndIcon.clearData();
						}

					}
						break;

					case EVENT_LOAD_ICON : {
						TitleAndIcon titleAndIcon = (TitleAndIcon) msg.obj;
						if (titleAndIcon != null) {
							AppItemInfo itemInfo = titleAndIcon.getAppItemInfo();
							if (itemInfo != null) {
								if (itemInfo.mIcon != null && !itemInfo.mIcon.equals(mSysAppIcon)) {
									break;
								}
								BitmapDrawable drawable = titleAndIcon.getIcon();
								itemInfo.setIcon(drawable);
							}
							titleAndIcon.clearData();
						}
					}
						break;
					default :
						break;
				}
			}
		};
	}

	public void onHandleThemeIconStyleChanged(String packageStr) {
		// 是否安装
		if (!ThemeManager.isInstalledTheme(mContext, packageStr)) {
			return;
		}
		// 是否需要
		if (!mAppDataFilter.needResetData(packageStr)) {
			return;
		}

		// 重新加载
		AppDataThemeBean bean = createAppDataThemeBean(packageStr);
		if (null != bean) {
			mAppDataFilter.resetData(bean);
		} else {
			mAppDataFilter.resetData();
		}
		ArrayList<AppItemInfo> appItemInfos = getAllAppItemInfos();
		loadItems(appItemInfos, false);
	}

	public void onHandleScreenIndicatorStyleChanged() {
		// 获取设置的主题
		ScreenStyleConfigInfo info = GoSettingControler.getInstance(mContext)
				.getScreenStyleSettingInfo();
		String packageName = null;
		if (null == info || null == (packageName = info.getIndicatorStyle())) {
			return;
		}
		onHandleScreenIndicatorThemeIconStyleChanged(packageName);
	}

	public void onHandleFolderThemeIconStyleChanged() {
		// 获取设置的主题
		ScreenStyleConfigInfo info = GoSettingControler.getInstance(mContext)
				.getScreenStyleSettingInfo();
		String packageName = null;
		if (null == info || null == (packageName = info.getFolderStyle())) {
			return;
		}
		onHandleFolderThemeIconStyleChanged(packageName);
	}

	public void onHandleScreenThemeIconStyleChanged() {
		// 获取设置的主题
		ScreenStyleConfigInfo info = GoSettingControler.getInstance(mContext)
				.getScreenStyleSettingInfo();
		String packageName = null;
		if (null == info || null == (packageName = info.getIconStyle())) {
			return;
		}
		if (mAppDataFilter.getDataThemeBeanPackage() != null
				&& mAppDataFilter.getDataThemeBeanPackage().equals(packageName)) {
			return;
		}
		onHandleThemeIconStyleChanged(packageName);
	}

	public void onHandleFolderThemeIconStyleChanged(String packageStr) {
		// 重新加载
		DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
		DeskThemeBean themeBean = themeControler.getDeskThemeBean();
		// 是否安装
		if (!ThemeManager.isInstalledTheme(mContext, packageStr)) {
			return;
		}
		if (LauncherEnv.PACKAGE_NAME.equals(packageStr)) {
			themeBean.mScreen.mFolderStyle.mPackageName = packageStr;
			GoLauncher
					.sendBroadcastMessage(this, IDiyMsgIds.EVENT_LOAD_ICONS_FINISH, 0, null, null);
			GoLauncher.sendBroadcastHandler(this, IDiyMsgIds.APPDRAWER_FOLDER_THEME_CHANGE, -1,
					null, null);
			return;
		}

		if (themeBean.mScreen.mFolderStyle.mPackageName != null
				&& themeBean.mScreen.mFolderStyle.mPackageName.equals(packageStr)) {
			return;
		}

		InputStream inputStream = null;
		XmlPullParser xmlPullParser = null;
		IParser parser = null;
		inputStream = ThemeManager.getInstance(mContext).createParserInputStream(packageStr,
				ThemeConfig.DESKTHEMEFILENAME);
		if (inputStream != null) {
			xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
		} else {
			xmlPullParser = XmlParserFactory.createXmlParser(mContext,
					ThemeConfig.DESKTHEMEFILENAME, packageStr);
		}
		DeskFolderThemeBean folderThemeBean = null;
		if (xmlPullParser != null) {
			folderThemeBean = new DeskFolderThemeBean(packageStr);
			parser = new DeskFolderThemeParser();
			parser.parseXml(xmlPullParser, folderThemeBean);
			folderThemeBean.mFolderStyle.mPackageName = packageStr;
			parser = null;
		}
		// 关闭inputStream
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (folderThemeBean != null && folderThemeBean.mFolderStyle != null) {
			themeBean.mScreen.mFolderStyle = folderThemeBean.mFolderStyle;
		}
		GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.EVENT_LOAD_ICONS_FINISH, 0, null, null);
		GoLauncher.sendBroadcastHandler(this, IDiyMsgIds.APPDRAWER_FOLDER_THEME_CHANGE, -1, null,
				null);
	}

	public void onHandleScreenIndicatorThemeIconStyleChanged(String packageName) {

		DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
		DeskThemeBean deskThemeBean = themeControler.getDeskThemeBean();
		if (packageName.equals(ScreenIndicator.SHOWMODE_NUMERIC)
				|| LauncherEnv.PACKAGE_NAME.equals(packageName)) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.INDICATOR_CHANGE_SHOWMODE, -1, null, null);
			//3D插件消息
			GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
					IDiyMsgIds.INDICATOR_CHANGE_SHOWMODE, -1, null, null);
			
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
					IDiyMsgIds.INDICATOR_CHANGE_SHOWMODE, -1, null, null);
			deskThemeBean.mIndicator.setPackageName(packageName);
			return;
		}
		// 是否安装
		//		if (!AppUtils.isAppExist(mContext, packageName)) {
		//			return;
		//		}
		// 是否安装  update by zhoujun 添加判断zip包主题 
		if (!ThemeManager.isInstalledTheme(mContext, packageName)) {
			return;
		}
		//update by zhoujun 2012-08-24 end 

		// 重新加载
		if (deskThemeBean.mIndicator != null && null != deskThemeBean.mIndicator.getPackageName()
				&& deskThemeBean.mIndicator.getPackageName().equals(packageName)) {
			return;
		}

		InputStream inputStream = null;
		XmlPullParser xmlPullParser = null;
		DeskThemeParser parser = null;
		inputStream = ThemeManager.getInstance(mContext).createParserInputStream(packageName,
				ThemeConfig.DESKTHEMEFILENAME);
		if (inputStream != null) {
			xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
		} else {
			xmlPullParser = XmlParserFactory.createXmlParser(mContext,
					ThemeConfig.DESKTHEMEFILENAME, packageName);
		}
		DeskThemeBean themBean = null;
		if (xmlPullParser != null) {
			themBean = new DeskThemeBean(packageName);
			parser = new DeskThemeParser();
			parser.parseXml(xmlPullParser, themBean);
			parser = null;
		}
		// 关闭inputStream
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (null != themBean && themBean.mIndicator != null) {
			deskThemeBean.mIndicator = themBean.mIndicator;
			deskThemeBean.mIndicator.setPackageName(packageName);
		}
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.INDICATOR_CHANGE_SHOWMODE, -1, null, null);
		//3D插件消息
		GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
				IDiyMsgIds.INDICATOR_CHANGE_SHOWMODE, -1, null, null);
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.INDICATOR_CHANGE_SHOWMODE, -1, null, null);
	}

	public void onHandleThemeIconStyleChanged() {
		// 获取设置的主题
		ScreenStyleConfigInfo info = GoSettingControler.getInstance(mContext)
				.getScreenStyleSettingInfo();
		if (null == info) {
			return;
		}
		String deskIconPackageName = info.getIconStyle();
		String deskfolderPackageName = info.getFolderStyle();
		String deskIndicatorPackageName = info.getIndicatorStyle();
		if (deskIconPackageName == null || deskfolderPackageName == null) {
			return;
		}
		onHandleThemeIconStyleChanged(deskIconPackageName);
		onHandleFolderThemeIconStyleChanged(deskfolderPackageName);
		onHandleScreenIndicatorThemeIconStyleChanged(deskIndicatorPackageName);
	}

	public void onHandleShowIconBaseChanged(boolean showBase) {
		mShowBase = showBase;
		ArrayList<AppItemInfo> appItemInfos = getAllAppItemInfos();
		loadItems(appItemInfos, false);
	}

	/**
	 * <br>功能简述:创建指定主题包名appfilter.xml的数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param packageStr
	 * @return
	 */
	public AppDataThemeBean createAppDataThemeBean(String packageStr) {
		if (packageStr != null && packageStr.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) {
			return new AppDataThemeBean(packageStr);
		}

		ThemeBean bean = null;
		ThemeInfoBean infoBean = ThemeManager.getInstance(GOLauncherApp.getContext()).getThemeInfo(
				packageStr);
		boolean isEncrypt = false;
		if (infoBean != null) {
			isEncrypt = infoBean.isEncrypt();
		}
		// 解析应用程序过滤器信息
		bean = new AppThemeParser().autoParseAppThemeXml(mContext, packageStr, isEncrypt);
		return (AppDataThemeBean) bean;
	}

	private void initAppDataFilter() {
		if (mAppDataFilter == null) {
			// 获取设置的主题
			DesktopSettingInfo info = GoSettingControler.getInstance(mContext)
					.getDesktopSettingInfo();
			if (null == info || null == info.mThemeIconStylePackage
					|| !AppUtils.isAppExist(mContext, info.mThemeIconStylePackage)
					|| ThemeManager.DEFAULT_THEME_PACKAGE.equals(info.mThemeIconStylePackage)) {

				mAppDataFilter = new AppDataFilter(mContext);
			} else {
				AppDataThemeBean bean = createAppDataThemeBean(info.mThemeIconStylePackage);
				mAppDataFilter = new AppDataFilter(mContext, bean);
			}
		}
	}

	/**
	 * 清除数据
	 * 
	 * @author huyong
	 */
	@Override
	public void cleanup() {
	}

	public void loadInitDataInService() {
		List<ResolveInfo> resolveInfos = AppUtils.getLauncherApps(mContext);
		addAllAppItems(resolveInfos);
		getAllCompletedAppItemInfos();
	}

	/**
	 * Returns whether <em>apps</em> contains <em>component</em>.
	 */
	private static boolean findActivity(List<ResolveInfo> apps, ComponentName component) {
		final String className = component.getClassName();
		for (ResolveInfo info : apps) {
			final ActivityInfo activityInfo = info.activityInfo;
			if (activityInfo.name.equals(className)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 程序升级，或者enable/disable，刷新与此包名相关的程序信息 Add and remove icons for this package
	 * which has been updated.
	 */
	public void updatePackage(final String packageName) {
		final List<ResolveInfo> matches = findActivitiesForPackage(packageName);
		ArrayList<ComponentName> removeList = new ArrayList<ComponentName>();
		if (matches.size() > 0) {
			// 查找更新后无效的component
			Iterator<Entry<ComponentName, AppItemInfo>> iter = mAllAppItemsMap.entrySet()
					.iterator();
			while (iter.hasNext()) {
				Entry<ComponentName, AppItemInfo> entry = iter.next();
				ComponentName key = entry.getKey();
				if (key.getPackageName().equals(packageName)) {
					if (!findActivity(matches, key)) {
						removeList.add(key);
					}
				}
			}

			// 添加可能新加的入口
			ArrayList<AppItemInfo> updateList = new ArrayList<AppItemInfo>();
			ArrayList<AppItemInfo> appItemInfos = addAllAppItems(matches, updateList);
			if (appItemInfos != null && appItemInfos.size() > 0) {
				loadIconsAndTitles(appItemInfos);
				// 对外广播，通知批量安装了新程序
				GoLauncher.sendBroadcastMessage(AppDataEngine.this, IDiyMsgIds.EVENT_INSTALL_APP,
						0, packageName, appItemInfos);
			} else {
				// 查找更新的包是否是GOWidget，例如短信升级后包含了GOWidget的情况
				Intent intent = new Intent(ICustomAction.ACTION_MAIN_GOWIDGET_PACKAGE);
				intent.addCategory(GOWIDGET_CATEGORY);
				intent.setPackage(packageName);
				PackageManager pm = mContext.getPackageManager();
				List<ResolveInfo> widgets = pm.queryIntentActivities(intent, 0);
				if (widgets != null && widgets.size() > 0) {
					GoLauncher.sendBroadcastMessage(AppDataEngine.this,
							IDiyMsgIds.EVENT_INSTALL_PACKAGE, 0, packageName, null);
				}
			}

			// 更新已经存在的图标和标题
			reloadIconsAndTitles(updateList);
			updateList.clear();
		} else {
			// 查找不到这个包对应的所有程序入口，则删除mAllAppItemsMap中所有与此包名相关的数据
			Iterator<Entry<ComponentName, AppItemInfo>> iter = mAllAppItemsMap.entrySet()
					.iterator();
			while (iter.hasNext()) {
				Entry<ComponentName, AppItemInfo> entry = iter.next();
				ComponentName key = entry.getKey();
				if (key.getPackageName().equals(packageName)) {
					removeList.add(key);
				}
			}
		}

		if (removeList.size() > 0) {
			ArrayList<AppItemInfo> uninstalList = new ArrayList<AppItemInfo>();
			// 更新缓存
			for (ComponentName componentName : removeList) {
				uninstalList.add(mAllAppItemsMap.remove(componentName));
			}

			// 对外广播，通知观察者卸载了程序
			GoLauncher.sendBroadcastMessage(AppDataEngine.this, IDiyMsgIds.EVENT_UNINSTALL_APP, 0,
					packageName, uninstalList);

			uninstalList.clear();
		}
		removeList.clear();
	}

	/**
	 * 程序卸载
	 * 
	 * @author huyong
	 */
	private synchronized void uninstallAppItem(final Intent intent, String packageName) {
		if (intent == null || packageName == null || mAllAppItemsMap == null) {
			return;
		}

//		Log.i(LOG_TAG, "uninstallAppItem pkg = " + packageName);
		ArrayList<AppItemInfo> removeAppItems = new ArrayList<AppItemInfo>();
		ArrayList<ComponentName> removeCnList = new ArrayList<ComponentName>();
		Iterator<Entry<ComponentName, AppItemInfo>> iter = mAllAppItemsMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<ComponentName, AppItemInfo> entry = iter.next();
			ComponentName key = entry.getKey();
			AppItemInfo val = entry.getValue();
			final String appComponent = val.mIntent.getComponent().getPackageName();
			if (appComponent.equals(packageName)) {
				removeAppItems.add(val);
				removeCnList.add(key);
			}
		}

		if (removeAppItems != null && removeAppItems.size() > 0) {
			// 更新缓存
			for (ComponentName componentName : removeCnList) {
				mAllAppItemsMap.remove(componentName);
			}

			CommonControler.getInstance(mContext).checkUnInstallAppIsRecommend(packageName, removeAppItems); // 检查卸载的应用是否推荐应用
			// 对外广播，通知观察者卸载了程序
			GoLauncher.sendBroadcastMessage(AppDataEngine.this, IDiyMsgIds.EVENT_UNINSTALL_APP, 0,
					packageName, removeAppItems);
		} else {
			// 不在map程序列表中的卸载程序事件，则直接将intent通知出去
			GoLauncher.sendBroadcastMessage(AppDataEngine.this, IDiyMsgIds.EVENT_UNINSTALL_PACKAGE,
					0, packageName, null);
		}

		removeAppItems.clear();
		removeAppItems = null;
		removeCnList.clear();
		removeCnList = null;
	}

	// /**
	// * 批量程序卸载
	// * @author huyong
	// */
	// @SuppressWarnings("unused")
	// private void uninstallAppItems(final ArrayList<Intent> intents) {
	// if (intents == null || intents.size() <= 0) {
	// return;
	// }
	// int size = intents.size();
	// Intent intent = null;
	// for (int i = 0; i < size; i++) {
	// intent = intents.get(i);
	// uninstallAppItem(intent);
	// }
	// }

	/**
	 * 批量新程序安装
	 * 
	 * @author huyong
	 * @param resolveInfos
	 */
	private void installAppItems(final List<ResolveInfo> resolveInfos, String packageName) {
		// 发送广播的标识
		ArrayList<AppItemInfo> appItemInfos = addAllAppItems(resolveInfos);
		if (appItemInfos != null && appItemInfos.size() > 0) {
			loadIconsAndTitles(appItemInfos);
			CommonControler.getInstance(mContext).queryAppsClassify(packageName, appItemInfos);
			// 对外广播，通知批量安装了新程序
//			Log.i(LOG_TAG, "installAppItems pkg = " + packageName);
			CommonControler.getInstance(mContext).checkInstallAppIsRecommend(packageName, appItemInfos); // 检查新安装的应用是否推荐应用
			GoLauncher.sendBroadcastMessage(AppDataEngine.this, IDiyMsgIds.EVENT_INSTALL_APP, 0,
					packageName, appItemInfos);
		} else {
			GoLauncher.sendBroadcastMessage(AppDataEngine.this, IDiyMsgIds.EVENT_INSTALL_PACKAGE,
					0, packageName, null);
		}
		if (appItemInfos != null) {
			appItemInfos.clear();
			appItemInfos = null;
		}
	}

	/**
	 * 返回当前所有应用程序。里面信息可能不完整，取决于当前是否已经加载过图片和title
	 * 注意：每次取到的list是新创建的，其中的item项源于map，
	 * 
	 * @author huyong
	 * @return
	 */
	public final ArrayList<AppItemInfo> getAllAppItemInfos() {
		if (mAllAppItemsMap == null) {
			return null;
		}
		// NOTE:由于此处原有做法不能保证线程安全，因此此处替换新做法。
		// 原因：mAllAppItemsMap保证线程安全，但mAllAppItemsMap.values()的值只是一个普通的collection，外部对此collection的使用的线程安全性
		// 需由外部自行保证。ConcurrentHashMap不再保证。而此处在执行new
		// ArrayList<AppItemInfo>(mAllAppItemsMap.values())语句时，需要针对
		// 此collection转成数组后进行拷贝，即执行collection.toArray()，而在进行toArray()时，内部会使用迭代器进行读取拷贝。此时在多线程访问环境
		// 下就容易出现未同步问题。比如已经暴露出来的NoSuchElementException异常的抛出。
		// 综上，在执行new
		// ArrayList<AppItemInfo>语句内部进行拷贝时，需要针对这个collection内部的iterator进行同步，但这是内部行为。此处提供一个
		// 已同步的线程安全的列表给其内部使用即可。
		// 第1版 ArrayList<AppItemInfo> allAppItemsList = new
		// ArrayList<AppItemInfo>(mAllAppItemsMap.values());
		// 第2版 ArrayList<AppItemInfo> allAppItemsList =
		// new ArrayList<AppItemInfo>(
		// Collections.synchronizedCollection( mAllAppItemsMap.values() )
		// );
		// 第3版 ArrayList<AppItemInfo> allAppItemsList = null;
		// Collection<AppItemInfo> synCollection =
		// Collections.synchronizedCollection( mAllAppItemsMap.values() );
		// synchronized (synCollection)
		// {
		// allAppItemsList = new ArrayList<AppItemInfo>( synCollection );
		// }
		// 第4版：相比原有直接拷贝，利用iterator的弱一致性，不会抛出ConcurrentModificationException
		ArrayList<AppItemInfo> allAppItemsList = new ArrayList<AppItemInfo>(mAllAppItemsMap.size());
		Iterator<Entry<ComponentName, AppItemInfo>> iter = mAllAppItemsMap.entrySet().iterator();
		if (iter == null) {
			return null;
		}
		AppItemInfo itemInfo = null;
		try {
			while (iter.hasNext()) {
				itemInfo = iter.next().getValue();
				allAppItemsList.add(itemInfo);
			}

		} catch (Exception e) {
			// TODO: handle exception
		}

		return allAppItemsList;
	}

	/**
	 * 返回当前所有应用程序。并把隐藏的应用加入HideAppItemsList里 注意：每次取到的list是新创建的，其中的item项源于map，
	 */
	public final ArrayList<AppItemInfo> getAppItemInfosExceptHide() {
		if (mAllAppItemsMap == null) {
			return null;
		}
		// 得到隐藏应用的componentNamelist
		ArrayList<ComponentName> componentNamelist = getHideComponentList();

		ArrayList<AppItemInfo> allAppItemsListExceptHide = new ArrayList<AppItemInfo>();
		Iterator<Entry<ComponentName, AppItemInfo>> iter = mAllAppItemsMap.entrySet().iterator();
		if (iter == null) {
			return null;
		}
		AppItemInfo itemInfo = null;
		ComponentName componentName = null;
		try {
			while (iter.hasNext()) {
				componentName = iter.next().getKey();

				if (componentName != null) {
					itemInfo = mAllAppItemsMap.get(componentName);
					// 如果应用不是隐藏应用就加进allAppItemsListExceptHide
					if (componentNamelist != null && itemInfo != null
							&& !componentNamelist.contains(componentName)) {
						allAppItemsListExceptHide.add(itemInfo);
					}
				}
			}
			componentNamelist.clear();
			componentNamelist = null;
		} catch (Exception e) {
			// TODO: handle exception
		}
		// 返回除了隐藏应用以外的所有应用list
		return allAppItemsListExceptHide;
	}

	/**
	 * 从数据库里读取隐藏应用列表 返回隐藏应用列表
	 * 
	 */

	public ArrayList<ComponentName> getHideComponentList() {
		AppConfigDataModel mAppConfigDataModel = new AppConfigDataModel(mContext);
		ArrayList<ComponentName> mComponentNamelist = new ArrayList<ComponentName>();
		ArrayList<AppConfigInfo> AppConfigInfos = null;
		if (mAppConfigDataModel != null) {
			AppConfigInfos = mAppConfigDataModel.getAllHideAppItems();
		}
		if (AppConfigInfos != null) {
			if (!AppConfigInfos.isEmpty()) {
				int size = AppConfigInfos.size();
				for (int i = 0; i < size; i++) {
					AppConfigInfo info = AppConfigInfos.get(i);
					if (info != null && info.getIntent() != null) {
						mComponentNamelist.add(info.getIntent().getComponent());
					}
				}
			}
			AppConfigInfos.clear();
			AppConfigInfos = null;
		}
		return mComponentNamelist;
	}

	/**
	 * 返回N个当前应用程序。里面信息可能不完整，取决于当前是否已经加载过图片和title
	 * 注意：每次取到的list是新创建的，其中的item项源于map，
	 * 
	 * @author Stan
	 * @return
	 */
	public final ArrayList<AppItemInfo> getAppItemInfos(int n) {
		if (mAllAppItemsMap == null) {
			return null;
		}

		int count = 1;
		ArrayList<AppItemInfo> allAppItemsList = new ArrayList<AppItemInfo>();
		Iterator<Entry<ComponentName, AppItemInfo>> it = mAllAppItemsMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<ComponentName, AppItemInfo> entry = it.next();
			allAppItemsList.add(entry.getValue());
			if ((++count) > n) {
				return allAppItemsList;
			}
		}
		return allAppItemsList;
	}

	/**
	 * 返回当前所有应用程序，保证所有信息完整。 注意：每次取到的list是新创建的，其中的item项源于map，
	 * 
	 * @author huyong
	 * @return
	 */
	public final ArrayList<AppItemInfo> getAllCompletedAppItemInfos() {
		ArrayList<AppItemInfo> allAppItemsList = getAllAppItemInfos();
		loadIconsAndTitles(allAppItemsList);
		return allAppItemsList;
	}

	/**
	 * 返回当前除隐藏应用外的所有应用程序，保证所有信息完整。
	 */
	public final ArrayList<AppItemInfo> getCompletedAppItemInfosExceptHide() {
		ArrayList<AppItemInfo> allAppItemsListExceptHide = getAppItemInfosExceptHide();
		loadIconsAndTitles(allAppItemsListExceptHide);
		return allAppItemsListExceptHide;
	}

	public final ArrayList<AppItemInfo> getAppItemInfosForNotification() {
		ArrayList<AppItemInfo> allAppItemsListExceptHide = getAppItemInfosForNotify();
		loadIconsAndTitles(allAppItemsListExceptHide);
		return allAppItemsListExceptHide;
	}

	/**
	 * 通过一组intent获取一组item的完整信息
	 * 
	 * @author huyong
	 * @param itemIntents
	 * @return
	 */
	public final ArrayList<AppItemInfo> getCompletedAppItems(final ArrayList<Intent> itemIntents) {
		if (itemIntents == null) {
			return null;
		}
		ArrayList<AppItemInfo> appItemInfos = new ArrayList<AppItemInfo>();
		Intent intent = null;
		AppItemInfo itemInfo = null;
		int size = itemIntents.size();
		for (int i = 0; i < size; i++) {
			intent = itemIntents.get(i);
			itemInfo = getCompletedAppItem(intent);
			if (itemInfo == null) {
				continue;
			}
			appItemInfos.add(itemInfo);
		}
		return appItemInfos;
	}

	/**
	 * 通过指定intent获取item的完整信息
	 * 
	 * @author huyong
	 * @param intent
	 * @return
	 */
	public final AppItemInfo getCompletedAppItem(final Intent intent) {
		AppItemInfo itemInfo = getAppItem(intent);
		loadIconAndTitle(itemInfo);
		return itemInfo;
	}

	/**
	 * 通过intent来获取AppItemInfo
	 * 
	 * @author huyong
	 * @param intent
	 * @return
	 */
	public final AppItemInfo getAppItem(final Intent intent) {
		if (intent == null) {
			return null;
		}
		ComponentName cn = intent.getComponent();
		AppItemInfo appItemInfo = null;
		if (cn != null) {
			appItemInfo = mAllAppItemsMap.get(cn);
			cn = null;
		}
		return appItemInfo;
	}
	
	public final AppItemInfo getAppItemExceptHide(final Intent intent) {
		if (intent == null) {
			return null;
		}
		ArrayList<AppItemInfo> appItemInfos = getAppItemInfosExceptHide();
		ComponentName cn = intent.getComponent();
		AppItemInfo appItemInfo = null;
		if (cn != null) {
			appItemInfo = mAllAppItemsMap.get(cn);
			if (appItemInfos.contains(appItemInfo)) {
				return appItemInfo;
			}
			cn = null;
		}
		return null;
	}
	
	/**
	 * 通过intent获取item图片，找不到，则返回机器人默认图标，
	 * 
	 * @author huyong
	 * @param intent
	 * @return BitmapDrawable, or return sysappicon if cann't get
	 */
	public final BitmapDrawable getAppItemIconByIntent(final Intent intent) {
		AppItemInfo appItemInfo = getAppItem(intent);
		BitmapDrawable itemIcon = null;
		if (appItemInfo != null) {
			itemIcon = appItemInfo.getIcon();
			if (itemIcon == null || itemIcon.equals(mSysAppIcon)) {
				itemIcon = getOriginalIcon(intent);
			}
		} else {
			itemIcon = getOriginalIcon(intent);
		}
		if (itemIcon == null) {
			itemIcon = mSysAppIcon;
		}
		return itemIcon;
	}

	/**
	 * 获取原生图，如果找不到，则返回null
	 * 
	 * @author huyong
	 * @param itemIntent
	 * @return BitmapDrawable, return null if cann't get
	 */
	public BitmapDrawable getOriginalIcon(final Intent itemIntent) {
		if (null == itemIntent) {
			return null;
		}

		BitmapDrawable drawable = null;
		if (null != mAppDataFilter) {
			drawable = mAppDataFilter.getThemeDrawable(itemIntent);
		}

		if (drawable != null) {
			return drawable;
		}

		final Resources resources = mContext.getResources();
		// 取罩子和底座和mask蒙版
		Bitmap base = null;
		BitmapDrawable cover = null;
		BitmapDrawable mask = null;
		
		try {
			Drawable activityIcon = null;
			if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME.equals(itemIntent.getAction())) {
				if (Machine.isTablet(mContext)) {
					activityIcon = ImageExplorer.getInstance(mContext).getDrawableForDensity(
							resources, R.drawable.theme);
				}
				if (null == activityIcon) {
					Bitmap bmp = BitmapFactory.decodeResource(resources, R.drawable.theme);
					// 缩放图片
					if (bmp != null) {
						activityIcon = new BitmapDrawable(resources,
								Utilities.createBitmapThumbnail(bmp, mContext));
					}
				}
			} else if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE.equals(itemIntent.getAction())) {
				if (Machine.isTablet(mContext)) {
					activityIcon = ImageExplorer.getInstance(mContext).getDrawableForDensity(
							resources, R.drawable.store);
				}
				if (null == activityIcon) {
					Bitmap bmp = BitmapFactory.decodeResource(resources, R.drawable.store);
					// 缩放图片
					if (bmp != null) {
						activityIcon = new BitmapDrawable(resources,
								Utilities.createBitmapThumbnail(bmp, mContext));
					}
				}
			} else if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET
					.equals(itemIntent.getAction())) {
				if (Machine.isTablet(mContext)) {
					activityIcon = ImageExplorer.getInstance(mContext).getDrawableForDensity(
							resources, R.drawable.gowidget);
				}
				if (null == activityIcon) {
					Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
							R.drawable.gowidget);
					// 缩放图片
					if (bmp != null) {
						activityIcon = new BitmapDrawable(resources,
								Utilities.createBitmapThumbnail(bmp, mContext));
					}
				}
			} else if (ICustomAction.ACTION_FUNC_SHOW_RECOMMENDLIST.equals(itemIntent.getAction())
					|| ICustomAction.ACTION_SHOW_RECOMMENDLIST.equals(itemIntent.getAction())) {
				if (Machine.isTablet(mContext)) {
					activityIcon = ImageExplorer.getInstance(mContext).getDrawableForDensity(
							resources, R.drawable.yjziji_shortcut);
				}
				if (null == activityIcon) {
					Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
							R.drawable.yjziji_shortcut);
					// 缩放图片
					if (bmp != null) {
						activityIcon = new BitmapDrawable(resources,
								Utilities.createBitmapThumbnail(bmp, mContext));
					}
				}
			} else if (ICustomAction.ACTION_SHOW_RECOMMENDCENTER.equals(itemIntent.getAction())
					|| ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER
							.equals(itemIntent.getAction())) {
				if (Machine.isTablet(mContext)) {
					activityIcon = ImageExplorer.getInstance(mContext).getDrawableForDensity(
							resources, R.drawable.app_center_icon_large);
				}
				if (null == activityIcon) {
					Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
							R.drawable.app_center_icon_large);
					// 缩放图片
					if (bmp != null) {
						activityIcon = new BitmapDrawable(resources,
								Utilities.createBitmapThumbnail(bmp, mContext));
					}
				}
			} else {
				if (Machine.isTablet(mContext)) {
					final ResolveInfo resolveInfo = mContext.getPackageManager().resolveActivity(
							itemIntent, 0);
					final int resId = resolveInfo.getIconResource();
					Resources themeResources = mContext.getPackageManager()
							.getResourcesForApplication(resolveInfo.activityInfo.packageName);
					activityIcon = ImageExplorer.getInstance(mContext).getDrawableForDensity(
							themeResources, resId);
				}
				if (activityIcon == null) {
					activityIcon = mContext.getPackageManager().getActivityIcon(itemIntent);
				}
			}

			// activityIcon =
			// mContext.getPackageManager().getActivityIcon(itemIntent);
			if (activityIcon instanceof BitmapDrawable) {
				drawable = (BitmapDrawable) activityIcon;
			} else {
				drawable = Utilities.createBitmapDrawableFromDrawable(activityIcon, mContext);
			}

			synchronized (mAppDataFilter) {
				cover = mAppDataFilter.getIconupon(mShowBase);
				mask = mAppDataFilter.getIconmask(mShowBase);
				final BitmapDrawable iconBase = mAppDataFilter.getIconback(mShowBase);
				if (iconBase != null) {
					base = iconBase.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
				}
			}

			// 缩放图片
			if (drawable != null && drawable.getBitmap() != null) {
				if (base != null || cover != null) {
					float scale = mAppDataFilter.getIconScaleFactor();
					drawable = composeIcon(base, cover, drawable, mask , scale);
				} else {
					// drawable = new
					// BitmapDrawable(Utilities.createBitmapThumbnail(drawable.getBitmap(),
					// mContext));
					/*
					 * 默认主题没有底座，直接对取得的图标缩放成标准大小。但是如果图标已经是标准大小，
					 * Utilities.createBitmapThumbnail方法会直接返回原图。直接使用这些图标来绘图，
					 * 可能会受到系统共享保护之类的机制而导致绘制时间变长？例如功能表里面使用球和圆柱特效，
					 * 在某些屏之间切换会很慢，但是换其他特效又不会，确实很奇怪。 不管怎么样，这里保证了拷贝后，不会慢了。
					 */
					Bitmap bitmap = Utilities.createBitmapThumbnail(drawable.getBitmap(), mContext);
					if (bitmap != null && bitmap == drawable.getBitmap()) {
						bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
					}
					drawable = new BitmapDrawable(resources, bitmap);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		base = null;
		cover = null;
		return drawable;
	}

	/**
	 * 我的应用和应用更新，根据默认的activityIcon，生成所需的icon
	 * 
	 * @param activityIcon
	 * @return
	 */
	public BitmapDrawable createBitmapDrawable(Drawable activityIcon) {
		BitmapDrawable drawable = null;
		final Resources resources = mContext.getResources();
		// 取罩子和底座和mask蒙版
		Bitmap base = null;
		BitmapDrawable cover = null;
		BitmapDrawable mask = null;
		try {
			if (activityIcon instanceof BitmapDrawable) {
				drawable = (BitmapDrawable) activityIcon;
			} else {
				drawable = Utilities.createBitmapDrawableFromDrawable(activityIcon, mContext);
			}

			synchronized (mAppDataFilter) {
				cover = mAppDataFilter.getIconupon(mShowBase);
				mask = mAppDataFilter.getIconmask(mShowBase);
				final BitmapDrawable iconBase = mAppDataFilter.getIconback(mShowBase);
				if (iconBase != null) {
					base = iconBase.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
				}
			}

			// 缩放图片
			if (drawable != null && drawable.getBitmap() != null) {
				if (base != null || cover != null) {
					float scale = mAppDataFilter.getIconScaleFactor();
					drawable = composeIcon(base, cover, drawable, mask , scale);
				} else {
					// drawable = new
					// BitmapDrawable(Utilities.createBitmapThumbnail(drawable.getBitmap(),
					// mContext));
					/*
					 * 默认主题没有底座，直接对取得的图标缩放成标准大小。但是如果图标已经是标准大小，
					 * Utilities.createBitmapThumbnail方法会直接返回原图。直接使用这些图标来绘图，
					 * 可能会受到系统共享保护之类的机制而导致绘制时间变长？例如功能表里面使用球和圆柱特效，
					 * 在某些屏之间切换会很慢，但是换其他特效又不会，确实很奇怪。 不管怎么样，这里保证了拷贝后，不会慢了。
					 */
					Bitmap bitmap = Utilities.createBitmapThumbnail(drawable.getBitmap(), mContext);
					if (bitmap != null && bitmap == drawable.getBitmap()) {
						bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
					}
					drawable = new BitmapDrawable(resources, bitmap);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		base = null;
		cover = null;
		return drawable;
	}

	/**
	 * 将图片设为72*72
	 * 
	 * @param drawable
	 * @return
	 */
	public static BitmapDrawable convertLePhoneIcon(Context context, BitmapDrawable drawable) {
		int width = drawable.getBitmap().getWidth();
		int height = drawable.getBitmap().getHeight();
		int newWidth = 72;
		int newHeight = 72;

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		// create the new Bitmap object
		Bitmap resizedBitmap = Bitmap.createBitmap(drawable.getBitmap(), 0, 0, width, height,
				matrix, true);
		BitmapDrawable bmd = new BitmapDrawable(context.getResources(), resizedBitmap);
		return bmd;
	}

	/**
	 * 通过intent来判断程序是否存在
	 * 
	 * @author huyong
	 * @param intent
	 * @return
	 */
	public boolean isAppExist(final Intent intent) {
		if (intent == null) {
			return false;
		}
		ComponentName componentName = intent.getComponent();
		return isAppExist(componentName);
	}
	
	/**
	 * 功能简述:通过ComponentName来判断程序是否存在
	 * 功能详细描述:
	 * 注意:
	 * @param componentName
	 * @return
	 */
	public boolean isAppExist(final ComponentName componentName) {
		return (componentName != null) ? mAllAppItemsMap.containsKey(componentName) : false;
	}

	public AppItemInfo getCachedAppItemInfo(ResolveInfo resolveInfo) {
		if (resolveInfo == null || resolveInfo.activityInfo == null) {
			return null;
		}

		final String infoName = resolveInfo.activityInfo.name;
		String packageName = null;
		final ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
		if (applicationInfo != null) {
			packageName = applicationInfo.packageName;
		}

		if (packageName == null || infoName == null) {
			return null;
		}

		ComponentName cn = new ComponentName(packageName, infoName);
		return mAllAppItemsMap.get(cn);
	}

	/**
	 * 通过resolveInfo来判断程序是否存在
	 * 
	 * @author huyong
	 * @param resolveInfo
	 * @return
	 */
	public boolean isAppExist(final ResolveInfo resolveInfo) {
		boolean result = false;
		if (resolveInfo == null || resolveInfo.activityInfo == null) {
			return result;
		}
		final String infoName = resolveInfo.activityInfo.name;
		String packageName = null;
		final ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
		if (applicationInfo != null) {
			packageName = applicationInfo.packageName;
		}
		if (packageName == null || infoName == null) {
			return result;
		}
		ComponentName cn = new ComponentName(packageName, infoName);
		if (cn != null) {
			result = mAllAppItemsMap.containsKey(cn);
			cn = null;
		}
		return result;
	}

	/**
	 * 增加一个新程序
	 * 
	 * @author huyong
	 * @param resolveInfo
	 * @return
	 */
	@SuppressWarnings("unused")
	private AppItemInfo addAppItems(final ResolveInfo resolveInfo) {
		if (resolveInfo == null) {
			return null;
		}
		AppItemInfo appItemInfo = new AppItemInfo();
		convertResolveToAppItem(resolveInfo, appItemInfo);
		loadIconAndTitle(appItemInfo);
		if (appItemInfo.mIntent == null) {
			appItemInfo = null;
			return null;
		}
		// String intentString = appItemInfo.mIntent.toUri(0);
		ComponentName cn = appItemInfo.mIntent.getComponent();
		if (cn != null) {
			mAllAppItemsMap.put(cn, appItemInfo);
			cn = null;
		}
		return appItemInfo;
	}
	/**
	 * 把map的数据结构转化为以包名做key的map
	 * @param appsMapKeyByPkg
	 * @param appItemInfo
	 */
	public void converToAppMapByPkg(HashMap<String, Object> appsMapKeyByPkg, ConcurrentHashMap<ComponentName, AppItemInfo> allAppItemsMap) {
		Iterator<Entry<ComponentName, AppItemInfo>> iter = mAllAppItemsMap.entrySet().iterator();
		if (iter == null) {
			return;
		}
		AppItemInfo appItemInfo = null;
		try {
			while (iter.hasNext()) {
				appItemInfo = iter.next().getValue();
				addInfoToAppMap(appsMapKeyByPkg, appItemInfo);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void addInfoToAppMap(HashMap<String, Object> appsMapKeyByPkg, AppItemInfo appItemInfo) {
		String pkg = appItemInfo.getAppPackageName();
		if (pkg != null) {
			if (appsMapKeyByPkg.containsKey(pkg)) {
				Object obj = appsMapKeyByPkg.get(pkg);
				if (obj instanceof ArrayList<?>) {
					((ArrayList<AppItemInfo>) obj).add(appItemInfo);
				} else if (obj instanceof AppItemInfo) {
					ArrayList<AppItemInfo> samePkgInfo = new ArrayList<AppItemInfo>();
					samePkgInfo.add((AppItemInfo) obj);
					samePkgInfo.add(appItemInfo);
					appsMapKeyByPkg.put(pkg, samePkgInfo);
				}
			} else {
				appsMapKeyByPkg.put(pkg, appItemInfo);
			}
		}
	}
	
	public HashMap<String, Object> getAppsMap() {
		HashMap<String, Object> appsMapKeyByPkg = new HashMap<String, Object>();
		converToAppMapByPkg(appsMapKeyByPkg, mAllAppItemsMap);
		return appsMapKeyByPkg;
	}

	/**
	 * 初始化添加扫描出的所有应用程序
	 * 
	 * @author huyong
	 * @param resolveInfos
	 */
	public void scanInitAllAppItems() {
		List<ResolveInfo> resolveInfos = AppUtils.getLauncherApps(mContext);
		addAllAppItems(resolveInfos);
		CommonControler.getInstance(mContext).initAllNewRecommendApps(); // 初始化，从数据库获取所有需要显示New标识的推荐应用
		// 根据渠道配置信息，检查是否需要移除应用游戏中心相关假图标的方法
		CommonControler.getInstance(mContext).initAllAppClassify();
		checkAppGameCenterIcon();
		final int[] orderID = { IDiyFrameIds.SCREEN_FRAME };
		GoLauncher.sendBroadcastMessage(AppDataEngine.this, IMsgType.SYNC,
				IDiyMsgIds.EVENT_LOAD_FINISH, 0, null, null, orderID);

	}

	private boolean isGoWidgetVisible(String packageName, PackageManager pm) {
		if (packageName.startsWith(MAIN_GOWIDGET_PNAME)) {
			try {
				ApplicationInfo app = pm.getApplicationInfo(packageName,
						PackageManager.GET_META_DATA);
				if (null != app) {
					if (app.metaData != null) {
						// isHideLauncher=1，表示需要隐藏应用程序
						// isHideLauncher=0，表示不需要隐藏应用程序
						// isHideLauncher=-1，表示未知，这里不作任何处理
						int isHideLauncher = app.metaData.getInt("isHideLauncher", -1);
						if (isHideLauncher == 1) {
							// 需要隐藏应用程序
							return false;
						}
					}
				}
			} catch (Throwable e) {
			}
		}
		return true;
	}

	private boolean isNotificationVisible(String packageName, PackageManager pm) {
		if (packageName.equals(ICustomAction.NOTIFICATION_PACKAGE)) {
			try {
				ApplicationInfo app = pm.getApplicationInfo(packageName,
						PackageManager.GET_META_DATA);
				if (null != app) {
					if (app.metaData != null) {
						// isHideLauncher=1，表示需要隐藏应用程序
						// isHideLauncher=0，表示不需要隐藏应用程序
						// isHideLauncher=-1，表示未知，这里不作任何处理
						int isHideLauncher = app.metaData.getInt("isHideLauncher", -1);
						if (isHideLauncher == 1) {
							// 需要隐藏应用程序
							return false;
						}
					}
				}
			} catch (Throwable e) {
			}
		}
		return true;
	}
	
	private synchronized final ArrayList<AppItemInfo> addAllAppItems(
			List<ResolveInfo> resolveInfos, List<AppItemInfo> existList) {
		if (resolveInfos == null) {
			return null;
		}

		PackageManager pm = mContext.getPackageManager();
		ArrayList<AppItemInfo> appItemInfos = new ArrayList<AppItemInfo>();
		int size = resolveInfos.size();

		AppItemInfo existAppInfo = null;
		ResolveInfo resolveInfo = null;
		for (int i = 0; i < size; ++i) {
			resolveInfo = resolveInfos.get(i);

			// ResolveInfo里面信息不全
			if (resolveInfo == null || resolveInfo.activityInfo == null
					|| resolveInfo.activityInfo.packageName == null) {
				continue;
			}

			final String packageName = resolveInfo.activityInfo.packageName;

			// 已经存在
			existAppInfo = getCachedAppItemInfo(resolveInfo);
			if (existAppInfo != null) {
				if (existList != null) {
					existList.add(existAppInfo);
				}
				continue;
			}

			// 过滤掉主题的图标展示
			if (packageName.startsWith(ThemeManager.MAIN_THEME_PACKAGE)
					|| !isGoWidgetVisible(packageName, pm)
					|| !isNotificationVisible(packageName, pm)) {
				continue;
			}
			
			AppItemInfo appItemInfo = new AppItemInfo();
			convertResolveToAppItem(resolveInfo, appItemInfo);
			if (appItemInfo.mIntent == null) {
				appItemInfo = null;
				continue;
			}

			ComponentName cn = appItemInfo.mIntent.getComponent();
			if (cn != null) {
				mAllAppItemsMap.put(cn, appItemInfo);
				appItemInfos.add(appItemInfo);
				cn = null;
			}
			appItemInfo = null;
		}
		return appItemInfos;
	}

	/**
	 * 初始化添加扫描出的所有应用程序
	 * 
	 * @author huyong
	 * @param resolveInfos
	 * @return 成功加入的items
	 */
	private final ArrayList<AppItemInfo> addAllAppItems(List<ResolveInfo> resolveInfos) {
		return addAllAppItems(resolveInfos, null);
	}

	private void convertResolveToAppItem(ResolveInfo resolveInfo, AppItemInfo appItemInfo) {
		if (resolveInfo == null || appItemInfo == null || resolveInfo.activityInfo == null) {
			return;
		}

		String infoName = resolveInfo.activityInfo.name;
		ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
		if (infoName == null || applicationInfo == null || applicationInfo.packageName == null) {
			return;
		}
		String packageName = applicationInfo.packageName;

		appItemInfo.mIcon = mSysAppIcon;
		boolean isSystemApp = ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
				|| ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
		// boolean isSystem =
		// applicationInfo.sourceDir.startsWith("/system/app") ||
		// applicationInfo.sourceDir.startsWith("/system/framework");
		appItemInfo.setIsSysApp(isSystemApp ? 1 : 0);

		ComponentName c = new ComponentName(packageName, infoName);
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(c);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		appItemInfo.mIntent = intent;
		appItemInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
		appItemInfo.mProcessName = resolveInfo.activityInfo.processName;
		appItemInfo.mUri = Uri.parse("package:" + packageName);
	}

	/**
	 * 开始加载完整数据
	 * 
	 * @author huyong
	 */
	public void startLoadCompletedData() {
		if (mAllAppItemsMap == null) {
//			Log.i(LOG_TAG, "startLoadCompletedData complete");
			return;
		}
		synchronized (mHasLoadedLock) {
			if (mHasLoaded) {
//				Log.i(LOG_TAG, "startLoadCompletedData hasLoaded");
				return;
			}
			mHasLoaded = true;
		}
//		Log.i(LOG_TAG, "startLoadCompletedData begine");
		ArrayList<AppItemInfo> allAppItemInfos = getAllAppItemInfos();
		asynLoadIconsAndTitles(allAppItemInfos);
		if (null != allAppItemInfos) {
//			Log.i(LOG_TAG, "startLoadCompletedData finish, size = " + allAppItemInfos.size());
		}
		allAppItemInfos = null;

		onHandleScreenThemeIconStyleChanged();
		onHandleFolderThemeIconStyleChanged();
		onHandleScreenIndicatorStyleChanged();
	}

	/**
	 * 是否加载过完整数据
	 * 
	 * @author huyong
	 * @return
	 */
	public boolean isLoadedCompletedData() {
		return mIsIconTitleLoadingFinish;
	}

	/**
	 * 异步加载程序图片和名称
	 * 
	 * @author huyong
	 */
	private void asynLoadIconsAndTitles(final ArrayList<AppItemInfo> appItemInfos) {

		Thread thread = new Thread(ThreadName.ASYNC_LOAD_ICONS_AND_TITLES) {
			@Override
			public void run() {
					super.run();
					// loadIconsAndTitles(appItemInfos);
					synchronized (AllAppBussiness.getSaveLock()) {
						if (appItemInfos == null) {
							return;
						}
						int size = appItemInfos.size();
						for (int i = 0; i < size; ++i) {
							Thread.yield();
							AppItemInfo itemInfo = appItemInfos.get(i);
							if (itemInfo == null) {
								continue;
							}
							if (itemInfo.mIcon == null || itemInfo.mIcon == mSysAppIcon) {
								itemInfo.setIcon(getAppIcon(itemInfo));
							}

							// TODO: 需要与外面协商默认的题目是什么！
							itemInfo.setTitle(getAppTitle(itemInfo));

							// Log.i("aoriming","title = "+itemInfo.mTitle);
							// Log.i("aoriming","component = "+itemInfo.mIntent.getComponent());
						}

						LogUnit.i(LOG_TAG, "End AppDataEngine.asynLoadIconsAndTitles before broadcast");
						GoLauncher.sendBroadcastMessage(AppDataEngine.this,
								IDiyMsgIds.EVENT_LOAD_TITLES_FINISH, 0, null, null);
						GoLauncher.sendBroadcastMessage(AppDataEngine.this,
								IDiyMsgIds.EVENT_LOAD_ICONS_FINISH, 0, null, null);
						mIsIconTitleLoadingFinish = true;
						LogUnit.i(LOG_TAG, "End AppDataEngine.asynLoadIconsAndTitles");
					}
				}
		};
		thread.setPriority(Thread.NORM_PRIORITY - 2); // 低于正常线程3个优先级来运行
		thread.start();
	}

	/**
	 * *****************************TODO:整理异步加载itmes项的线程处理。
	 * *********************
	 * ********TODO:修改LoadThread的run方法，采取类似策略模型，将内部加载实现交给外部控制。
	 */
	/**
	 * 加载items中的未完成项
	 * 
	 * @author huyong
	 * @param appItemInfos
	 *            待加载的items列表
	 * @param isSynchronized
	 *            是否为与当前线程同步加载.true for 同步加载，false for 异步加载
	 */
	private void loadItems(ArrayList<AppItemInfo> appItemInfos, boolean isSynchronized) {
		if (appItemInfos == null || appItemInfos.size() <= 0) {
			return;
		}
		if (mCurLoadThread != null) {
			mCurLoadThread.stop();
			mCurLoadThread = null;
		}
		mCurLoadThread = new LoadThread(appItemInfos);
		mCurLoadThread.setNeedLoadIcon(true);
		if (isSynchronized) {
			mCurLoadThread.run();
		} else {
			new Thread(mCurLoadThread).start();
		}
	}

	private class LoadThread implements Runnable {
		private boolean mIsStop;
		private boolean mIsLoadTitle;
		private boolean mIsloadIcon;

		private ArrayList<AppItemInfo> mToLoadAppItemInfos;

		public LoadThread(ArrayList<AppItemInfo> itemsInfo) {
			mIsStop = false;
			mIsLoadTitle = false;
			mIsloadIcon = false;
			mToLoadAppItemInfos = itemsInfo;
		}

		public void stop() {
			mIsStop = true;
		}

		@SuppressWarnings("unused")
		public void setLoadAppList(ArrayList<AppItemInfo> itemInfos) {
			mToLoadAppItemInfos = itemInfos;
		}

		@SuppressWarnings("unused")
		public void setNeedTitle(boolean isLoadTitle) {
			mIsLoadTitle = isLoadTitle;
		}

		public void setNeedLoadIcon(boolean isLoadIcon) {
			mIsloadIcon = isLoadIcon;
		}

		@Override
		public void run() {
			if (mToLoadAppItemInfos == null) {
				return;
			}
			LogUnit.i(LOG_TAG, "Begin AppDataEngien LoadThread() ");
			int size = mToLoadAppItemInfos.size();
			TitleAndIcon titleAndIcon = null;
			for (int i = 0; i < size; ++i) {
				if (mIsStop) {
					break;
				}
				AppItemInfo itemInfo = mToLoadAppItemInfos.get(i);
				titleAndIcon = new TitleAndIcon(itemInfo);
				if (mIsLoadTitle) {
					titleAndIcon.setTitle(getAppTitle(itemInfo));
				}
				if (mIsloadIcon) {
					titleAndIcon.setIcon(getOriginalIcon(itemInfo.mIntent));
				}

				if (mHandler != null) {
					Message message = mHandler.obtainMessage();
					message.what = EVENT_LOAD_ICON_AND_TITLES;
					message.obj = titleAndIcon;
					mHandler.sendMessage(message);
				}
			}

			// 主题应用完成，仅发送图标改变的消息
			LogUnit.i(LOG_TAG, "end AppDataEngien LoadThread() before broadcast");
			GoLauncher.sendBroadcastMessage(AppDataEngine.this, IDiyMsgIds.EVENT_LOAD_ICONS_FINISH,
					0, null, null);
			LogUnit.i(LOG_TAG, "end AppDataEngien LoadThread() ");
		}
	}

	/**
	 * 加载图片及titles
	 * 
	 * @author huyong
	 */
	private void loadIconsAndTitles(final ArrayList<AppItemInfo> appItemInfos) {

		if (appItemInfos == null) {
			return;
		}
		int size = appItemInfos.size();
		for (int i = 0; i < size; ++i) {
			AppItemInfo itemInfo = appItemInfos.get(i);
			loadIconAndTitle(itemInfo);
		}
	}

	/**
	 * 为指定item项加载图片及title信息
	 * 
	 * @author huyong
	 * @param itemInfo
	 */
	private void loadIconAndTitle(AppItemInfo itemInfo) {
		loadTitle(itemInfo);
		loadIcon(itemInfo);
	}

	private void loadTitle(AppItemInfo itemInfo) {
		if (itemInfo == null || itemInfo.mIntent == null) {
			return;
		}
		if (itemInfo.mTitle != null) {
			return;
		}
		String itemTitle = getAppTitle(itemInfo);
		itemInfo.setTitle(itemTitle);
	}

	private void loadIcon(AppItemInfo itemInfo) {
		if (itemInfo == null || itemInfo.mIntent == null) {
			return;
		}
		if (itemInfo.mIcon != null && itemInfo.mIcon != mSysAppIcon) {
			return;
		}

		BitmapDrawable itemIcon = getAppIcon(itemInfo);
		itemInfo.setIcon(itemIcon);
	}

	/**
	 * <br>功能简述:重新加载应用的图标和标题
	 * <br>功能详细描述:重新加载应用的图标和标题，主要用于应用程序升级时重新从应用包中获取最新版本的图标和标题
	 * <br>注意:
	 * @param itemInfo
	 */
	private void reloadIconAndTitle(AppItemInfo itemInfo) {
		reloadTitle(itemInfo);
		reloadIcon(itemInfo);
	}

	private void reloadTitle(AppItemInfo itemInfo) {
		if (itemInfo == null || itemInfo.mIntent == null) {
			return;
		}
		String itemTitle = getAppTitle(itemInfo);
		itemInfo.setTitle(itemTitle);
	}

	private void reloadIconsAndTitles(final ArrayList<AppItemInfo> appItemInfos) {

		if (appItemInfos == null) {
			return;
		}
		int size = appItemInfos.size();
		for (int i = 0; i < size; ++i) {
			AppItemInfo itemInfo = appItemInfos.get(i);
			reloadIconAndTitle(itemInfo);
		}
	}

	private void reloadIcon(AppItemInfo itemInfo) {
		if (itemInfo == null || itemInfo.mIntent == null) {
			return;
		}

		BitmapDrawable bitmapDrawable = getOriginalIcon(itemInfo.mIntent);
		if (bitmapDrawable == null) {
			bitmapDrawable = mSysAppIcon;
		}
		itemInfo.setIcon(bitmapDrawable);
	}

	/**
	 ************************************************ 
	 */

	/**
	 * 获取item原生title
	 * 
	 * @author huyong
	 * @param itemInfo
	 * @return
	 */
	private String getAppTitle(final AppItemInfo itemInfo) {
		if (itemInfo == null || itemInfo.mIntent == null) {
			return null;
		}

		String itemTitle = null;
		PackageManager pm = mContext.getPackageManager();
		try {

			if (itemInfo.mIntent != null) {
				String action = itemInfo.mIntent.getAction();
				if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME.equals(action)) {
					itemTitle = mContext.getResources().getString(R.string.go_theme);
				} else if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE.equals(action)) {
					itemTitle = mContext.getResources().getString(R.string.menuitem_gostore);
				} else if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET.equals(action)) {
					itemTitle = mContext.getResources().getString(R.string.func_gowidget_icon);
				} else if (ICustomAction.ACTION_FUNC_SHOW_RECOMMENDLIST.equals(action)
						|| ICustomAction.ACTION_SHOW_RECOMMENDLIST.equals(action)) {
					itemTitle = mContext.getResources().getString(R.string.recommended_yjzj);
				} else if (ICustomAction.ACTION_SHOW_RECOMMENDGAME.equals(action)
						|| ICustomAction.ACTION_FUNC_SHOW_RECOMMENDGAME.equals(action)) {
					itemTitle = mContext.getResources().getString(R.string.recommended_yjwj);
				} else if (ICustomAction.ACTION_SHOW_RECOMMENDCENTER.equals(action)
						|| ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER.equals(action)) {
					// 应用中心
					itemTitle = mContext.getResources().getString(R.string.appcenter_title);
				} else if (ICustomAction.ACTION_SHOW_GAMECENTER.equals(action)
						|| ICustomAction.ACTION_FUNC_SHOW_GAMECENTER.equals(action)) {
					// 游戏中心
					itemTitle = mContext.getResources().getString(R.string.gamecenter_title);
				} else {
					itemTitle = pm.getActivityInfo(itemInfo.mIntent.getComponent(), 0)
							.loadLabel(pm).toString();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemTitle;

	}

	/**
	 * 获取item原生图，若未找到，则返回机器人系统图标
	 * 
	 * @author huyong
	 * @param itemInfo
	 * @return
	 */
	private BitmapDrawable getAppIcon(final AppItemInfo itemInfo) {
		if (itemInfo == null) {
			return null;
		}
		if (itemInfo.mIcon != null && itemInfo.mIcon != mSysAppIcon) {
			return itemInfo.mIcon;
		}
		BitmapDrawable bitmapDrawable = getOriginalIcon(itemInfo.mIntent);
		if (bitmapDrawable == null) {
			bitmapDrawable = mSysAppIcon;
		}
		return bitmapDrawable;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case MonitorSver.APPCHANGE : {
				// 安装卸载事件
				handleAppChange(param, (Intent) object);
			}
				break;

			case MonitorSver.EXTERNAL_APP_AVAILABLE : {
				// 外部存储卡准备好
				// 重新扫描
				LogUnit.i(LOG_TAG, "MonitorSver.EXTERNALAPPCHANGE");
				asynReScanSysApp(IDiyMsgIds.EVENT_REFLUSH_SDCARD_IS_OK, (Intent) object);
				mIsSDCardOK = true;
			}
				break;

			case MonitorSver.SDMOUNT : {
				GoLauncher.sendBroadcastHandler(AppDataEngine.this, IDiyMsgIds.EVENT_SD_MOUNT, 0,
						null, null);
			}
				break;

			case MonitorSver.SDSHARED : {
				GoLauncher.sendBroadcastHandler(AppDataEngine.this, IDiyMsgIds.EVENT_SD_SHARED, 0,
						null, null);
			}
				break;

			default :
				break;
		}
	}

	/**
	 * 处理程序安装卸载的状态改变
	 * 
	 * @author huyong
	 * @param type
	 * @param intent
	 */
	private void handleAppChange(int type, final Intent intent) {
		if (null == intent) {
			return;
		}

		Uri uri = intent.getData();
		if (uri == null) {
			return;
		}
		final String packageName = uri.getSchemeSpecificPart();
		LogUnit.i(LOG_TAG, "AppDataEngine.handleAppChange() type = " + type + ", pkg = "
				+ packageName);
		switch (type) {
		// 检测到卸载
			case MonitorSver.FLAG_UNINSTALL : {

				setBeancount(packageName);
				// packageName找到AppDataEngine里的intent
				uninstallAppItem(intent, packageName);
			}
				break;

			// 检测到新安装和改变
			case MonitorSver.FLAG_INSTALL : {
				// 如果新安装的是Go桌面主题，就发消息刷新主题预览
				final List<ResolveInfo> newAppList = findActivitiesForPackage(packageName);
				installAppItems(newAppList, packageName);
			}
				break;

			case MonitorSver.FLAG_CHANGE : {
				final List<ResolveInfo> newAppList = findActivitiesForPackage(packageName);
				installAppItems(newAppList, packageName);
			}
				break;

			// 检测到程序更新
			case MonitorSver.FLAG_UPDATE : {
				setBeancount(packageName);
				updatePackage(packageName);
				GoLauncher.sendBroadcastMessage(AppDataEngine.this,
						IDiyMsgIds.EVENT_UPDATE_PACKAGE, 0, packageName, null);
				break;
			}

			default :
				break;
		}
	}

	/**
	 * 找到包名对应的ResolveInfo列表
	 * 
	 * @param context
	 *            上下文
	 * @param packageName
	 *            包名
	 * @return 匹配的ResolveInfo列表
	 */
	private List<ResolveInfo> findActivitiesForPackage(String packageName) {
		final PackageManager packageManager = mContext.getPackageManager();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
		final List<ResolveInfo> matches = new ArrayList<ResolveInfo>();

		if (apps != null) {
			// Find all activities that match the packageName
			int count = apps.size();
			ResolveInfo info = null;
			ActivityInfo activityInfo = null;
			for (int i = 0; i < count; i++) {
				info = apps.get(i);
				if (info == null) {
					continue;
				}
				activityInfo = info.activityInfo;
				if (activityInfo != null && packageName.equals(activityInfo.packageName)) {
					matches.add(info);
				}
			}
		}

		return matches;
	}

	/**
	 * 异步扫描校验系统程序
	 * 
	 * @author huyong
	 */
	/* package */
	void asynReScanSysApp(final int whatEvent, final Intent intent) {
		// 起线程异步扫描系统程序
		if (mIsSDCardOK) {
			GoLauncher.sendBroadcastMessage(AppDataEngine.this,
					IDiyMsgIds.EVENT_UPDATE_EXTERNAL_PACKAGES, -1, null, null);
			return;
		}
		new Thread(ThreadName.ASYNC_RESCAN_SYSAPP) {
			@Override
			public void run() {
				String packages[] = null;
				if (intent != null) {
					packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
				}

				LogUnit.i(LOG_TAG, "asynReScanSysApp begin");
				List<ResolveInfo> resolveInfos = AppUtils.getLauncherApps(mContext);
				ArrayList<AppItemInfo> itemInfos = addAllAppItems(resolveInfos);

				// TODO:新增数据信息需要loadIconsAndTitles，并且旧有数据，若信息不完整，则也需要加载
				// 因此，此处直接将所有信息重新加载
				ArrayList<AppItemInfo> allAppItemInfos = getAllAppItemInfos();
//				CommonControler.getInstance(mContext).initAllAppClassify();
				loadIconsAndTitles(allAppItemInfos);
				allAppItemInfos = null;

				// 同步至主线程二次扫描完毕
				GoLauncher.sendBroadcastHandler(AppDataEngine.this, whatEvent, 0, null, itemInfos);

				// 扫描不在appdraw出现的并且安装到sd卡上的包，例如桌面或widget的主题包
				if (packages != null && itemInfos != null) {
					ArrayList<String> updatePkgList = new ArrayList<String>(16);
					for (String string : packages) {
						updatePkgList.add(string);
					}

					for (String pkgstr : packages) {
						for (AppItemInfo itemInfo : itemInfos) {
							final String appPkg = AppUtils.getPackage(itemInfo.mIntent);
							if (appPkg != null && appPkg.equals(pkgstr)) {
								updatePkgList.remove(pkgstr);
								break; // 查找下一个包
							}
						}
					}
					packages = null;
					// 同步至主线程二次扫描完毕
					GoLauncher.sendBroadcastMessage(AppDataEngine.this,
							IDiyMsgIds.EVENT_UPDATE_EXTERNAL_PACKAGES, -1, null, updatePkgList);
				}
				LogUnit.i(LOG_TAG, "asynReScanSysApp end");
			}
		}.start();

	}

	/**
	 * 异步加载title和icon后，同步投递至ui线程时用
	 * 
	 * @author huyong
	 * 
	 */
	private class TitleAndIcon {
		private String mTitle; // 程序title
		private BitmapDrawable mIcon; // 程序图标
		private AppItemInfo mAppItemInfo;

		public TitleAndIcon(final AppItemInfo appItemInfo) {
			mAppItemInfo = appItemInfo;
			/*
			 * mTitle = getAppTitle(appItemInfo); mIcon =
			 * getAppIcon(appItemInfo);
			 */
		}

		public String getTitle() {
			return mTitle;
		}

		public void setTitle(String mTitle) {
			this.mTitle = mTitle;
		}

		public BitmapDrawable getIcon() {
			return mIcon;
		}

		public void setIcon(BitmapDrawable icon) {
			this.mIcon = icon;
		}

		public AppItemInfo getAppItemInfo() {
			return mAppItemInfo;
		}

		public void clearData() {
			mAppItemInfo = null;
			mIcon = null;
			mTitle = null;
		}
	}

	public final ConcurrentHashMap<ComponentName, AppItemInfo> getAllAppHashMap() {
		return mAllAppItemsMap;
	}

	/**
	 * 合成图片
	 * 
	 * @author huyong
	 * @param base
	 *            ：合成图片底图
	 * @param cover
	 *            ：合成图片罩子
	 * @param drawable
	 *            ： 待合成的源图
	 * @param drawable
	 *            ： 合成图片蒙版
	 * @param canvas
	 *            ：画布
	 * @param matrix
	 *            ：缩放matrix
	 * @param paint
	 *            ：画笔
	 * @param scale
	 *            ：缩放比率
	 * @return
	 */
	private BitmapDrawable composeIcon(Bitmap base, BitmapDrawable cover, BitmapDrawable drawable, BitmapDrawable mask,
			float scale) {
		LogUnit.i(LOG_TAG, "composeImg begin");
		// 用于判断mask是否只影响图标还是图标与底座都合成,目前只为true 
		boolean maskAppIcon = true;
		Bitmap tempBitmap = null;
		// 有底图或罩子
		if (base == null) {
			if (cover != null && cover.getBitmap() != null) {
				final Bitmap.Config config = cover.getOpacity() != PixelFormat.OPAQUE
						? Bitmap.Config.ARGB_8888
						: Bitmap.Config.RGB_565;
				base = Bitmap.createBitmap(cover.getBitmap().getWidth(), cover.getBitmap()
						.getHeight(), config);
			}
			
			if (base == null) {
				LogUnit.i(LOG_TAG, "Error can't create base");
				return drawable;
			}
		}
		int width = base.getWidth();
		int height = base.getHeight();
		final float scaleWidth = scale * width; // 缩放后的宽大小
		final float scaleHeight = scale * height; // 缩放后的高大小
		final Bitmap midBitmap = drawable.getBitmap();
		float scaleFactorW = 0f; // 缩放后较原图的宽的比例
		float scaleFactorH = 0f; // 缩放后较原图的高的比例
		if (midBitmap != null) {
			final int realWidth = midBitmap.getWidth();
			final int realHeight = midBitmap.getHeight();
			scaleFactorW = scaleWidth / realWidth;
			scaleFactorH = scaleHeight / realHeight;
		}

		synchronized (mCanvas) {
			final Canvas canvas = mCanvas;
			final Paint paint = mPaint;
			final Matrix matrix = mMatrix;
			if (maskAppIcon) {
				tempBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			} else {
				tempBitmap = base;
			}
			canvas.setBitmap(tempBitmap);
			int saveId = canvas.save();
			paint.setAntiAlias(true);
			matrix.setScale(scaleFactorW, scaleFactorH);
			matrix.postTranslate((width - scaleWidth) / 2f, (height - scaleHeight) / 2f);
			canvas.drawBitmap(drawable.getBitmap(), matrix, paint);
			canvas.restoreToCount(saveId);
			if (!maskAppIcon && cover != null) {
				canvas.drawBitmap(cover.getBitmap(), 0, 0, null);
			}
			// 加上mask蒙版
			if (mask != null) {
				Xfermode xf = paint.getXfermode();
				paint.setXfermode(mXfermode);
				canvas.drawBitmap(mask.getBitmap(), 0, 0, paint);
				paint.setXfermode(xf);
			}
			Bitmap fitIcon = Utilities.createBitmapThumbnail(tempBitmap, mContext);
			if (maskAppIcon && base != null ) {
				//底座需要放缩  	ADT-10807(功能表，更改图标大小后图标大小不统一)
				base = Utilities.createBitmapThumbnail(base, mContext);
				if(fitIcon == null){ //如果为空只画底座
					drawable = new BitmapDrawable(mContext.getResources(), base);
				}else{
					canvas.setBitmap(base);
					saveId = canvas.save();
					//居中
					// canvas.translate((width - fitIcon.getWidth()) / 2f, (height - fitIcon.getHeight()) / 2f);
					canvas.drawBitmap(fitIcon, 0, 0, paint);
					canvas.restoreToCount(saveId);
					if (cover != null) {
						float coverScale = (fitIcon.getWidth() + 0.1f) / cover.getIntrinsicWidth();
						matrix.reset();
						matrix.setScale(coverScale, coverScale);
						canvas.drawBitmap(cover.getBitmap(), matrix, paint);
					}
					drawable = new BitmapDrawable(mContext.getResources(), base);
				}
			} else {
				drawable = new BitmapDrawable(mContext.getResources(), fitIcon);
			}
		}
		LogUnit.i(LOG_TAG, "composeIcon end");
		return drawable;
	}

	/**
	 * 返回系统机器人图标
	 * 
	 * @author huyong
	 * @return
	 */
	public final BitmapDrawable getSysBitmapDrawable() {
		return mSysAppIcon;
	}

	public boolean isLoadData() {
		return mHasLoaded;
	}

	/**
	 * 初始化应用游戏中心相关假图标的方法
	 */
	private void initAppGameCenterIcon() {
		// 获取存储，检测应用中心、游戏中心图标是否已被标记为删除
		// add by songzhaochun, 2012.06.19
		PreferencesManager sp = null;
		if (mContext != null) {
			sp = new PreferencesManager(mContext,IPreferencesIds.DESK_SHAREPREFERENCES_FILE,
					Context.MODE_PRIVATE);
		}

		if (sp != null) {
			// 应用中心
			if (!sp.getBoolean(ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER, false)) {
				AppItemInfo goThemeInfo = new AppItemInfo();
				goThemeInfo.setIsSysApp(0);
				Intent recommendIntent = new Intent(ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER);
				goThemeInfo.mIntent = recommendIntent;
				goThemeInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
				ComponentName com = new ComponentName(LauncherEnv.RECOMMAND_CENTER_PACKAGE_NAME,
						ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER);
				goThemeInfo.mIntent.setComponent(com);
				goThemeInfo.mIntent.setData(Uri.parse("package:"
						+ LauncherEnv.RECOMMAND_CENTER_PACKAGE_NAME));
				goThemeInfo.mIcon = mSysAppIcon;// getOriginalIcon(goThemeInfo.mIntent);
				mAllAppItemsMap.put(com, goThemeInfo);
			}

			// 游戏中心
			if (!sp.getBoolean(ICustomAction.ACTION_FUNC_SHOW_GAMECENTER, false)) {
				// package
				ComponentName gameComp = new ComponentName(LauncherEnv.GAME_CENTER_PACKAGE_NAME,
						ICustomAction.ACTION_FUNC_SHOW_GAMECENTER);
				// app item info
				AppItemInfo gameCenterII = new AppItemInfo();
				gameCenterII.setIsSysApp(0);
				gameCenterII.mIntent = new Intent(ICustomAction.ACTION_FUNC_SHOW_GAMECENTER);
				gameCenterII.mItemType = IItemType.ITEM_TYPE_APPLICATION;
				gameCenterII.mIntent.setComponent(gameComp);
				gameCenterII.mIntent.setData(Uri.parse("package:"
						+ LauncherEnv.GAME_CENTER_PACKAGE_NAME));
				gameCenterII.mIcon = mSysAppIcon;// getOriginalIcon(gameCenterII.mIntent);
				mAllAppItemsMap.put(gameComp, gameCenterII);
			}
		}
		// // 一键装机
		// recommendApp = "com.gau.diy.recommendapp";
		// goThemeInfo = new AppItemInfo();
		// goThemeInfo.setIsSysApp(0);
		// recommendIntent = new
		// Intent(ICustomAction.ACTION_FUNC_SHOW_RECOMMENDLIST);
		// goThemeInfo.mIntent = recommendIntent;
		// goThemeInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
		// com = new ComponentName(recommendApp,
		// ICustomAction.ACTION_FUNC_SHOW_RECOMMENDLIST);
		// goThemeInfo.mIntent.setComponent(com);
		// goThemeInfo.mIntent.setData(Uri.parse("package:"+recommendApp));
		// goThemeInfo.mIcon = mSysAppIcon;
		// mAllAppItemsMap.put(com, goThemeInfo);

		// // 一键玩机
		// recommendApp = "com.gau.diy.recommendgame";
		// goThemeInfo = new AppItemInfo();
		// goThemeInfo.setIsSysApp(0);
		// recommendIntent = new
		// Intent(ICustomAction.ACTION_FUNC_SHOW_RECOMMENDGAME);
		// goThemeInfo.mIntent = recommendIntent;
		// goThemeInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
		// com = new ComponentName(recommendApp,
		// ICustomAction.ACTION_FUNC_SHOW_RECOMMENDGAME);
		// goThemeInfo.mIntent.setComponent(com);
		// goThemeInfo.mIntent.setData(Uri.parse("package:"+recommendApp));
		// goThemeInfo.mIcon = mSysAppIcon;
		// mAllAppItemsMap.put(com, goThemeInfo);
	}

	/**
	 * 检查是否需要移除应用游戏中心相关假图标的方法 根据渠道配置信息，如果本包的渠道号配置没有应用游戏中心的假图标
	 * 就要把在AppDataEngine初始化时加入的假图标移除掉
	 */
	private void checkAppGameCenterIcon() {
		if (null == mAllAppItemsMap) {
			return;
		}
		if (ConfigUtils.isNeedCheckAppGameInFunItemByChannelConfig()) {
			// 只有不需要在功能表添加应用中心或者游戏中心图标的时候，我们才做这样的检查和删除操作
			ArrayList<ComponentName> remove = new ArrayList<ComponentName>();
			Set<ComponentName> keySets = mAllAppItemsMap.keySet();
			AppItemInfo appItemInfo = null;
			for (ComponentName key : keySets) {
				appItemInfo = mAllAppItemsMap.get(key);
				if (ConfigUtils.isNeedRemoveAppGameFromFunByChannelConfig(appItemInfo)) {
					remove.add(key);
				}
			}
			for (ComponentName componentName : remove) {
				mAllAppItemsMap.remove(componentName);
			}
		}
	}

	/**
	 * 初始化go精品、go主题和gowidget图标
	 */
	private void initGoStoreAndGoThemeIcon() {
		// final String goStoreName = "com.gau.diy.gostore";
		// final String goThemeName = "com.gau.diy.gotheme";
		// final String goWidgetName = "com.gau.diy.gowidget";

		FunAppSetting funAppSetting = GoSettingControler.getInstance(mContext).getFunAppSetting();

		if (funAppSetting == null) {
			return;
		}
		if (funAppSetting.getShowGoStoreAndGoTheme(FunAppSetting.FUNC_APP_TYPE_GOTHEME)) {
			AppItemInfo goThemeInfo = new AppItemInfo();
			goThemeInfo.setIsSysApp(0);
			Intent goThemeIntent = new Intent(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME);
			goThemeInfo.mIntent = goThemeIntent;
			goThemeInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
			ComponentName goThemeCom = new ComponentName(LauncherEnv.GO_THEME_PACKAGE_NAME,
					ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME);
			goThemeInfo.mIntent.setComponent(goThemeCom);
			goThemeInfo.mIntent.setData(Uri.parse("package:" + LauncherEnv.GO_THEME_PACKAGE_NAME));
			goThemeInfo.mIcon = mSysAppIcon;
			mAllAppItemsMap.put(goThemeCom, goThemeInfo);
		}
		if (funAppSetting.getShowGoStoreAndGoTheme(FunAppSetting.FUNC_APP_TYPE_GOSTORE)) {
			// 如果是非国内用户则加go精品假图标
			// 群众对这个反应有点强烈，先撤下来，再说
			/**
			 * @edit by huangshaotao
			 * @date 2012-4-6 国内用户也显示gostore图标 if (Machine.isNotCnUser()) {
			 *       AppItemInfo goStoreInfo = new AppItemInfo();
			 *       goStoreInfo.setIsSysApp(0); Intent goStoreIntent = new
			 *       Intent(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE);
			 *       goStoreInfo.mIntent = goStoreIntent; goStoreInfo.mIcon =
			 *       mSysAppIcon; goStoreInfo.mItemType =
			 *       IItemType.ITEM_TYPE_APPLICATION; ComponentName goStoreCom =
			 *       new ComponentName(LauncherEnv.GO_STORE_PACKAGE_NAME,
			 *       ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE);
			 *       goStoreInfo.mIntent.setComponent(goStoreCom);
			 *       goStoreInfo.mIntent
			 *       .setData(Uri.parse("package:"+LauncherEnv
			 *       .GO_STORE_PACKAGE_NAME)); mAllAppItemsMap.put(goStoreCom,
			 *       goStoreInfo); }
			 */

			AppItemInfo goStoreInfo = new AppItemInfo();
			goStoreInfo.setIsSysApp(0);
			Intent goStoreIntent = new Intent(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE);
			goStoreInfo.mIntent = goStoreIntent;
			goStoreInfo.mIcon = mSysAppIcon;
			goStoreInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
			ComponentName goStoreCom = new ComponentName(LauncherEnv.GO_STORE_PACKAGE_NAME,
					ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE);
			goStoreInfo.mIntent.setComponent(goStoreCom);
			goStoreInfo.mIntent.setData(Uri.parse("package:" + LauncherEnv.GO_STORE_PACKAGE_NAME));
			mAllAppItemsMap.put(goStoreCom, goStoreInfo);
		}
		if (funAppSetting.getShowGoStoreAndGoTheme(FunAppSetting.FUNC_APP_TYPE_GOWIDGET)) {
			AppItemInfo goWidgetInfo = new AppItemInfo();
			goWidgetInfo.setIsSysApp(0);
			Intent goWidgetIntent = new Intent(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET);
			goWidgetInfo.mIntent = goWidgetIntent;
			goWidgetInfo.mIcon = mSysAppIcon;
			goWidgetInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
			ComponentName goWidgetCom = new ComponentName(LauncherEnv.GO_WIDGET_PACKAGE_NAME,
					ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET);
			goWidgetInfo.mIntent.setComponent(goWidgetCom);
			goWidgetInfo.mIntent
					.setData(Uri.parse("package:" + LauncherEnv.GO_WIDGET_PACKAGE_NAME));
			mAllAppItemsMap.put(goWidgetCom, goWidgetInfo);
		}
	}

	private void initAppInfo() {
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		if (controler != null) {
			DesktopSettingInfo info = controler.getDesktopSettingInfo();
			if (null != info) {
				mShowBase = info.mShowIconBase;
			} else {
				mShowBase = true;
			}
		}
	}

	private void setBeancount(String packageName) {
		if (null == packageName) {
			return;
		}
		ArrayList<AppBean> mListBeans = null;
		if (AppFuncFrame.getFunControler() == null) {
			return;
		}
		mListBeans = AppFuncFrame.getFunControler().getBeanlist();
		int count = AppFuncFrame.getFunControler().getmBeancount();
		if (null != mListBeans && count >= 0) {
			for (AppBean bean : mListBeans) {
				if (packageName.equals(bean.mPkgName)) {
					count--;
					AppFuncFrame.getFunControler().setmBeancount(count);
				}
			}
		}
	}
	/**
	 * 返回当前所有属于通讯统计的应用程序。并把隐藏的应用排除 注意：每次取到的list是新创建的，其中的item项源于map，
	 */
	public final ArrayList<AppItemInfo> getAppItemInfosForNotify() {
		if (mAllAppItemsMap == null) {
			return null;
		}
		// 得到隐藏应用的componentNamelist
		ArrayList<ComponentName> componentNamelist = getHideComponentList();

		ArrayList<AppItemInfo> allAppItemsListForNotity = new ArrayList<AppItemInfo>();
		Iterator<Entry<ComponentName, AppItemInfo>> iter = mAllAppItemsMap.entrySet().iterator();
		if (iter == null) {
			return null;
		}
		AppItemInfo itemInfo = null;
		ComponentName componentName = null;
		try {
			String[] packageNames = mContext.getResources().getStringArray(
					R.array.notification_more_app_array);
			int len = packageNames.length;
			while (iter.hasNext()) {
				componentName = iter.next().getKey();

				if (componentName != null) {
					itemInfo = mAllAppItemsMap.get(componentName);
					// 如果应用不是隐藏应用就加进allAppItemsListExceptHide
					if (componentNamelist != null && itemInfo != null
							&& !componentNamelist.contains(componentName)) {
						for (int i = 0; i < len; i++) {
							if (packageNames[i].equals(componentName.getPackageName())) {
								allAppItemsListForNotity.add(itemInfo);
							}
						}
					}
				}
			}
			componentNamelist.clear();
			componentNamelist = null;
		} catch (Exception e) {
			// TODO: handle exception
		}
		// 返回除了隐藏应用以外的所有应用list
		return allAppItemsListForNotity;
	}
}
