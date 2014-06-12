package com.jiubang.ggheart.apps.gowidget;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.AsyncHandler;
import com.go.util.DeferredHandler;
import com.go.util.device.Machine;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.CellLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenMissIconBugUtil;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.ThreadName;
/***
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  
 * @date  [2012-10-15]
 */
public class GoWidgetManager implements ICleanable {
	public static final int INVALID_GOWIDGET_ID = -100;
	private static final String TAG = "GoWidgetManager";
	private static final boolean DEBUG = false;

	private static final int ACTION_START = 0;
	private static final int ACTION_STOP = 1;
	private static final int ACTION_DELETE = 2;
	private static final int ACTION_REMOVE = 3;
	private static final int ACTION_RESUME = 4;
	private static final int ACTION_PAUSE = 5;
	private static final int ACTION_APPLY_THEME = 6;
	private static final int ACTION_ENTER = 7;
	private static final int ACTION_LEAVE = 8;

	// 通讯统计
	private static final int ACTION_STATISTIC_CLEAR = 9;
	private static final int ACTION_STATISTIC_ERROR = 10;

	final ArrayList<GoWidgetBaseInfo> mWidgetMap = new ArrayList<GoWidgetBaseInfo>();
	final ConcurrentHashMap<Integer, View> mWidgetView = new ConcurrentHashMap<Integer, View>();
	private ArrayList<InnerWidgetInfo> mInnerWidgets = null;
	private LayoutInflater mInflater;
	private Object mLock = new Object();

	// 为了与系统标准widget区分，gowidgetid使用负数表示
	int mNextAppWidgetId = INVALID_GOWIDGET_ID - 1;

	private DataProvider mDataProvider = null;
	private Context mContext;
	private final WidgetHandler mUiHandler = new WidgetHandler(this);
	private WidgetAsyncHandler mAsyncHandler;

	public GoWidgetManager(Context context) {
		mAsyncHandler = new WidgetAsyncHandler();
		mContext = context;
		mDataProvider = DataProvider.getInstance(context);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void onDestory() {
		try {
			// 停止handler
			mAsyncHandler.stop();
			mUiHandler.cancel();

			Intent intent = new Intent(ICustomAction.ACTION_DESTROY_GOWIDGETS);
			mContext.sendBroadcast(intent);
		} catch (Exception e) {
			Log.e(TAG, "notifyDestory fail");
		}
	}

	public void onResetDefault() {
		try {
			Intent intent = new Intent(ICustomAction.ACTION_RESET_TO_DEFAULT);
			mContext.sendBroadcast(intent);
		} catch (Exception e) {
			Log.e(TAG, "notify reset to default fail");
		}
	}

	/***
	 * 通知widget进入/离开屏幕显示区域
	 * 
	 * @param viewGroup
	 *            当前所在屏幕
	 * @param visiable
	 *            true 进入显示区域
	 */
	public void fireVisible(ViewGroup viewGroup, boolean visiable) {
		if (viewGroup == null) {
			return;
		}

		// 转到异步线程上执行通知进入消息
		int msg = visiable
				? WidgetAsyncHandler.MSG_FIRE_ON_ENTER
				: WidgetAsyncHandler.MSG_FIRE_ON_LEAVE;
		mAsyncHandler.obtainMessage(msg, viewGroup).sendToTarget();
	}

	/**
	 * 传递pause事件
	 * 
	 * @param widgetid
	 */
	public void pauseWidget(int widgetid) {
		GoWidgetBaseInfo info = getWidgetInfo(widgetid);
		if (info != null) {
			applyWidget(info, ACTION_PAUSE);
		}
	}

	/**
	 * 传递resume事件
	 * 
	 * @param widgetid
	 */
	public void resumeWidget(int widgetid) {
		GoWidgetBaseInfo info = getWidgetInfo(widgetid);
		if (info != null) {
			applyWidget(info, ACTION_RESUME);
		}
	}

	public void checkWidgetTheme(GoWidgetBaseInfo info) {
		// 使用主题需要更新view
		if (isUsedTheme(info)) {
			applyWidget(info, ACTION_APPLY_THEME, info.mThemeId, info.mTheme);
		}
	}

	public boolean isUsedTheme(GoWidgetBaseInfo info) {
		return info != null
				&& ((info.mTheme != null && !info.mTheme.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) || (info.mThemeId != GoWidgetBaseInfo.DEFAULT_THEME_ID));
	}

	/**
	 * 应用widget主题
	 * 
	 * @param widgetId
	 *            widgetId
	 * @param theme
	 *            主题包名
	 */
	public void applyWidgetTheme(int widgetId, Bundle bundle) {
		GoWidgetBaseInfo info = getWidgetInfo(widgetId);
		final String themepkg = bundle.getString(GoWidgetConstant.GOWIDGET_THEME);
		final int id = bundle.getInt(GoWidgetConstant.GOWIDGET_THEMEID);
		if (info != null) {
			applyWidget(info, ACTION_APPLY_THEME, id, themepkg);
		}
	}

	public void prepareGoWidgetInfo() {
		synchronized (mLock) {
			mWidgetMap.clear();
			final ArrayList<GoWidgetBaseInfo> list = mDataProvider.getAllGoWidgetInfos();
			int count = list.size();
			for (int i = 0; i < count; i++) {
				final GoWidgetBaseInfo info = list.get(i);
				if (info != null) {
					addGoWidget(info, false);
					if (info.mWidgetId <= mNextAppWidgetId) {
						// 下一未被使用的widgetid
						mNextAppWidgetId = info.mWidgetId - 1;
					}
				}
			}

			// 加载内置widget的信息
			mInnerWidgets = InnerWidgetParser.getInnerWidgets(mContext);
		}
	}

	/**
	 * 获取内置的gowidget信息
	 * 
	 * @return
	 */
	public ArrayList<InnerWidgetInfo> getInnerWidgetList() {
		synchronized (mLock) {
			return mInnerWidgets;
		}
	}

	private String getStatisticPackage(GoWidgetBaseInfo info) {
		if (info == null) {
			return null;
		}

		if (info.mPrototype != GoWidgetBaseInfo.PROTOTYPE_NORMAL) {
			synchronized (mLock) {
				if (mInnerWidgets != null) {
					int size = mInnerWidgets.size();
					for (int i = 0; i < size; i++) {
						InnerWidgetInfo innerWidgetInfo = mInnerWidgets.get(i);
						if (innerWidgetInfo.mPrototype == info.mPrototype) {
							// 全内置类型返回统计伪包名
							if (innerWidgetInfo.mBuildin == InnerWidgetInfo.BUILDIN_ALL) {
								return innerWidgetInfo.mStatisticPackage;
							}
							return innerWidgetInfo.mWidgetPkg;
						}
					}
				}
			}
		}
		return info.mPackage;
	}

	/**
	 * 获取统计数据 Map以PackageName为key，widget的统计数据为value
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, String> getStatisticData() {
		HashMap<String, GoWidgetBaseInfo> distinctWidgetMap;
		GoWidgetBaseInfo info;
		synchronized (mLock) {
			distinctWidgetMap = new HashMap<String, GoWidgetBaseInfo>();
			int count = mWidgetMap.size();
			String statisticPackage = null;
			info = null;
			for (int i = 0; i < count; i++) {
				info = mWidgetMap.get(i);
				statisticPackage = getStatisticPackage(info);
				if (statisticPackage != null && !distinctWidgetMap.containsKey(statisticPackage)) {
					distinctWidgetMap.put(statisticPackage, info);
				}
			}
		}
		HashMap<String, String> statisticData = new HashMap<String, String>();
		if (!distinctWidgetMap.isEmpty()) {
			Iterator<Entry<String, GoWidgetBaseInfo>> iterator = distinctWidgetMap.entrySet()
					.iterator();

			while (iterator.hasNext()) {
				Entry<String, GoWidgetBaseInfo> entry = iterator.next();
				info = entry.getValue();

				View widgetView = mWidgetView.get(info.mWidgetId);
				if (widgetView != null && !(widgetView instanceof WidgetErrorView)) {
					// 无论反射能不能取到数据，都把键放到MAP里面，外面拿到这个MAP可以判断哪些Widget已经添加到桌面
					statisticData.put(entry.getKey(), null);
					try {
						Class tempClass = widgetView.getClass();
						Method method = tempClass
								.getMethod(GoWidgetConstant.METHOD_ON_GET_STATISTIC);
						String data = (String) method.invoke(widgetView);

						if (data != null) {
							statisticData.put(entry.getKey(), data);
						}
					} catch (Exception e) {
						if (DEBUG) {
							Log.w(TAG, "invoke onGetStatistic err, widget id = " + info.mWidgetId);
						}
					}
				}

			}
		}

		distinctWidgetMap.clear();
		distinctWidgetMap = null;
		return statisticData;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void fireWidgetStatsticEvent(int action) {
		synchronized (mLock) {
			HashMap<String, GoWidgetBaseInfo> distinctWidgetMap = new HashMap<String, GoWidgetBaseInfo>();
			int count = mWidgetMap.size();

			String statisticPackage = null;
			GoWidgetBaseInfo info = null;
			for (int i = 0; i < count; i++) {
				info = mWidgetMap.get(i);
				statisticPackage = getStatisticPackage(info);
				if (statisticPackage != null && !distinctWidgetMap.containsKey(statisticPackage)) {
					distinctWidgetMap.put(statisticPackage, info);
				}
			}

			if (!distinctWidgetMap.isEmpty()) {
				Iterator<Entry<String, GoWidgetBaseInfo>> iterator = distinctWidgetMap.entrySet()
						.iterator();

				while (iterator.hasNext()) {
					Entry<String, GoWidgetBaseInfo> entry = iterator.next();
					info = entry.getValue();

					View widgetView = mWidgetView.get(info.mWidgetId);
					if (widgetView != null && !(widgetView instanceof WidgetErrorView)) {
						switch (action) {
							case ACTION_STATISTIC_CLEAR :
								try {
									Class tempClass = widgetView.getClass();
									Method method = tempClass
											.getMethod(GoWidgetConstant.METHOD_ON_CLEAR_STATISTIC);
									method.invoke(widgetView);
								} catch (Exception e) {
									if (DEBUG) {
										Log.w(TAG, "invoke onClearStatistic err, widget id = "
												+ info.mWidgetId);
									}
								}
								break;

							case ACTION_STATISTIC_ERROR :
								try {
									Class tempClass = widgetView.getClass();
									Method method = tempClass
											.getMethod(GoWidgetConstant.METHOD_ON_ERROR_STATISTIC);
									method.invoke(widgetView);
								} catch (Exception e) {
									if (DEBUG) {
										Log.w(TAG, "invoke onErrorStatistic err, widget id = "
												+ info.mWidgetId);
									}
								}
								break;

							default :
								break;
						}
					}
				}
			}

			distinctWidgetMap.clear();
			distinctWidgetMap = null;
		}
	}

	/**
	 * 清除统计数据
	 */
	public void clearStatisticData() {
		fireWidgetStatsticEvent(ACTION_STATISTIC_CLEAR);
	}

	/**
	 * 统计错误时通知widget进行相应的处理
	 */
	public void notifyStatisticError() {
		fireWidgetStatsticEvent(ACTION_STATISTIC_ERROR);
	}

	public InnerWidgetInfo getInnerWidgetInfo(int prototype) {
		synchronized (mLock) {
			if (mInnerWidgets != null) {
				int size = mInnerWidgets.size();
				for (int i = 0; i < size; i++) {
					InnerWidgetInfo info = mInnerWidgets.get(i);
					if (info.mPrototype == prototype) {
						return info;
					}
				}
			}
		}
		return null;
	}

	public InnerWidgetInfo getInnerWidgetInfo(String packageName) {
		synchronized (mLock) {
			if (mInnerWidgets != null) {
				int size = mInnerWidgets.size();
				for (int i = 0; i < size; i++) {
					InnerWidgetInfo innerWidgetInfo = mInnerWidgets.get(i);
					if (innerWidgetInfo.mWidgetPkg.equals(packageName)) {
						return innerWidgetInfo;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 获取设置入口
	 * 
	 * @param info
	 * @return
	 */
	public ComponentName getConfigComponent(GoWidgetBaseInfo info) {
		final String pkgName = getWidgetPackage(info);
		if (pkgName != null) {
			return new ComponentName(pkgName, info.mClassName);
		}
		return null;
	}

	/**
	 * 获取实际的widget包名 例如内置的任务管理器返回的包是{@link GoWidgetAdapter#TASK_MANAGER}
	 * 
	 * @param info
	 * @return
	 */
	public String getWidgetPackage(GoWidgetBaseInfo info) {
		if (info == null) {
			return null;
		}

		if (info.mPrototype == GoWidgetBaseInfo.PROTOTYPE_TASKMAN) {
			synchronized (mLock) {
				if (mInnerWidgets != null) {
					int size = mInnerWidgets.size();
					for (int i = 0; i < size; i++) {
						InnerWidgetInfo innerWidgetInfo = mInnerWidgets.get(i);
						if (innerWidgetInfo.mPrototype == info.mPrototype) {
							return innerWidgetInfo.mWidgetPkg;
						}
					}
				}
			}
		}
		return info.mPackage;
	}

	/**
	 * 获取真正inflate widget的包名 例如任务管理器实际的布局在主包里
	 * 
	 * @param widgetPkg
	 * @return
	 */
	public String getInflatePackage(String widgetPkg) {
		if (widgetPkg != null) {
			synchronized (mLock) {
				if (mInnerWidgets != null) {
					int size = mInnerWidgets.size();
					for (int i = 0; i < size; i++) {
						InnerWidgetInfo innerWidgetInfo = mInnerWidgets.get(i);
						if (widgetPkg.equals(innerWidgetInfo.mWidgetPkg)) {
							return innerWidgetInfo.mInflatePkg;
						}
					}
				}
			}
		}
		return widgetPkg;
	}

	private boolean isValid(GoWidgetBaseInfo info) {
		if (null == info) {
			return false;
		}
		// 开关在versionCode<14在android3.0上会导致崩溃等问题，需要过滤
		String packageName = info.mPackage;
		if (packageName != null
				&& packageName.equals("com.gau.go.launcherex.gowidget.switchwidget")) {
			PackageManager pm = mContext.getPackageManager();
			if (Build.VERSION.SDK_INT >= 11) {
				try {
					PackageInfo resolveInfo = pm.getPackageInfo(packageName, 0);
					return resolveInfo.versionCode >= 14;
				} catch (NameNotFoundException e) {
				}
			}
		}
		// 如果用户从有应用游戏中心的渠道升级到没有应用游戏中心的渠道
		// 要把应用游戏中心的Widget过滤掉
		if (info.mPrototype == GoWidgetBaseInfo.PROTOTYPE_APPGAME) {
			ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
			if (channelConfig != null
					&& (!channelConfig.isNeedAppCenter() || !channelConfig.isNeedGameCenter())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 只能在UI线程调用
	 * 
	 * @param info
	 * @return
	 */
	public View createView(int widgetid) {
		GoWidgetBaseInfo info = getWidgetInfo(widgetid);
		if (info == null) {
			ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil.ERROR_CREATE_CUSTOM_INFO_NULL);
			return null;
		}

		// 首先获取缓存中的视图
		View view = mWidgetView.get(widgetid);
		if (view != null) {
			return view;
		}

		View widgetView = null;
		boolean isValid = isValid(info);
		if (isValid) {
			try {
				Context remoteContext = mContext;
				if (!mContext.getPackageName().equals(info.mPackage)) {
					remoteContext = mContext.createPackageContext(info.mPackage,
							Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
				}
				LayoutInflater inflater = LayoutInflater.from(remoteContext);
				Resources resources = remoteContext.getResources();
				int resourceId = resources.getIdentifier(info.mLayout, "layout", info.mPackage);
				widgetView = inflater.inflate(resourceId, null);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (widgetView == null) {
			widgetView = getErrorView();
		}

		// 放入缓存
		if (widgetView != null) {
			mWidgetView.put(widgetid, widgetView);
		}

		// 检查主题
		checkWidgetTheme(info);
		return widgetView;
	}

	// 获取widget出错后的视图
	protected View getErrorView() {
		try {
			return mInflater.inflate(R.layout.widget_error, null);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			Log.w(TAG, "getErrorView error");
		}
		return null;
	}

	/**
	 * 清除视图，并通知widget
	 */
	public void cleanView() {
		synchronized (mLock) {
			int size = mWidgetMap.size();
			for (int i = 0; i < size; i++) {
				removeWidget(mWidgetMap.get(i).mWidgetId);
			}
		}

		mWidgetView.clear();
	}

	@Override
	public void cleanup() {
		cancelReplaceWidget();
		synchronized (mLock) {
			mWidgetMap.clear();
		}

		mWidgetView.clear();
	}

	/**
	 * SD卡加载之后刷新widget替换之前出错的视图
	 */
	public void startReplaceWidget(ArrayList<GoWidgetBaseInfo> widgetList) {
		mUiHandler.startReplaceWidget(widgetList);
	}

	/**
	 * 根据主程序主题更改，应用到所有的GOwidget
	 */
	public void startApplyWidgetTheme(String theme) {
		// theme = 桌面主题包，themeId = -1
		mUiHandler.startApplyWidgetTheme(theme);
	}

	public void cancelReplaceWidget() {
		mUiHandler.cancel();
	}

	void doReplaceWidget(GoWidgetBaseInfo info) {
		final View oldView = mWidgetView.get(info.mWidgetId);
		if (oldView != null) {
			mWidgetView.remove(info.mWidgetId);
			ViewGroup parent = (ViewGroup) oldView.getParent();
			if (parent != null && parent instanceof CellLayout) {
				View newView = createView(info.mWidgetId);
				((CellLayout) parent).replaceView(oldView, newView);
			}
			parent = null;
			applyWidget(info, ACTION_START);
		}
	}

	void doApplyWidgetTheme(int index, String theme) {
		GoWidgetBaseInfo info = null;
		synchronized (mLock) {
			if (index < 0 || index >= mWidgetMap.size()) {
				return;
			}

			info = mWidgetMap.get(index);
		}

		if (info != null) {
			// 桌面的皮肤包应用后，传递给widget的themeid为-1
			// applyWidget(info, ACTION_APPLY_THEME,
			// GoWidgetBaseInfo.DEFAULT_THEME_ID, theme);
			// 如果是大主题，themeid=0
			applyWidget(info, ACTION_APPLY_THEME, GoWidgetBaseInfo.NEW_THEME_ID, theme);
		}
	}

	/**
	 * 是否添加了packageName的widget包
	 * 
	 * @param packageName
	 * @return
	 */
	public boolean containsPackage(String packageName) {
		synchronized (mLock) {
			int size = mWidgetMap.size();
			String widgetPackage = null;
			GoWidgetBaseInfo info = null;
			for (int i = 0; i < size; i++) {
				info = mWidgetMap.get(i);
				widgetPackage = getWidgetPackage(info);
				if (widgetPackage != null && widgetPackage.equals(packageName)) {
					return true;
				}
			}
		}
		return false;
	}

	public int getWidgetCount() {
		return mWidgetMap.size();
	}

	public ArrayList<GoWidgetBaseInfo> getWidgets() {
		return mWidgetMap;
	}

	public void startWidget(int widgetid, Bundle bundle) {
		final GoWidgetBaseInfo info = getWidgetInfo(widgetid);
		if (info == null) {
			return;
		}
		applyWidget(info, ACTION_START, -1, bundle);
	}

	public void stopWidget(int widgetid) {
		final GoWidgetBaseInfo info = getWidgetInfo(widgetid);
		if (info == null) {
			return;
		}
		applyWidget(info, ACTION_STOP);
	}

	/**
	 * 删除widget视图，横竖屏切换时调用，非用户删除
	 * 
	 * @param widgetId
	 */
	public void removeWidget(int widgetid) {
		if (DEBUG) {
			Log.i(TAG, "remove widget id = " + widgetid);
		}
		final GoWidgetBaseInfo info = getWidgetInfo(widgetid);
		if (info == null) {
			return;
		}
		applyWidget(info, ACTION_REMOVE);
	}

	private void fireOnDelete(int widgetid) {
		GoWidgetBaseInfo info = getWidgetInfo(widgetid);
		if (info == null) {
			return;
		}
		applyWidget(info, ACTION_DELETE);
	}

	private void applyWidget(GoWidgetBaseInfo info, int action) {
		this.applyWidget(info, action, 0, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void applyWidget(GoWidgetBaseInfo info, int action, int param, Object obj) {
		View widgetView = mWidgetView.get(info.mWidgetId);
		if (widgetView == null || widgetView instanceof WidgetErrorView) {
			return;
		}

		switch (action) {
			case ACTION_START : {
				try {
					Class tempClass = widgetView.getClass();
					Method method = tempClass.getMethod(GoWidgetConstant.METHOD_ON_START,
							Bundle.class);
					Bundle bundle = null;
					if (obj != null && obj instanceof Bundle) {
						bundle = (Bundle) obj;
					} else {
						bundle = new Bundle();
					}
					bundle.putInt(GoWidgetConstant.GOWIDGET_ID, info.mWidgetId);
					bundle.putInt(GoWidgetConstant.GOWIDGET_TYPE, info.mType);
					bundle.putString(GoWidgetConstant.GOWIDGET_LAYOUT, info.mLayout);
					// 添加是否平板标志
					bundle.putBoolean(GoWidgetConstant.GOWIDGET_IS_TABLET,
							Machine.isTablet(mContext));
					method.invoke(widgetView, bundle);
					bundle = null;
				} catch (Exception e) {
					if (DEBUG) {
						Log.w(TAG, "invoke start Widget err, widget id = " + info.mWidgetId);
					}
				}
				break;
			}

			case ACTION_STOP : {
				try {
					Class tempClass = widgetView.getClass();
					Method method = tempClass.getMethod(GoWidgetConstant.METHOD_ON_STOP);
					method.invoke(widgetView);
				} catch (Exception e) {
					if (DEBUG) {
						Log.w(TAG, "invoke stop Widget err, widget id = " + info.mWidgetId);
					}
				}
				break;
			}

			case ACTION_DELETE : {
				try {
					Class tempClass = widgetView.getClass();
					Method method = tempClass.getMethod(GoWidgetConstant.METHOD_ON_DELETE,
							int.class);
					method.invoke(widgetView, info.mWidgetId);
				} catch (Exception e) {
					if (DEBUG) {
						Log.w(TAG, "invoke delete Widget err, widget id = " + info.mWidgetId);
					}
				}
				break;
			}

			case ACTION_REMOVE : {
				try {
					Class tempClass = widgetView.getClass();
					Method method = tempClass.getMethod(GoWidgetConstant.METHOD_ON_REMOVE,
							int.class);
					method.invoke(widgetView, info.mWidgetId);
				} catch (Exception e) {
					if (DEBUG) {
						Log.w(TAG, "invoke remove Widget err, widget id = " + info.mWidgetId);
					}
				}
				break;
			}

			case ACTION_PAUSE : {
				try {
					Class tempClass = widgetView.getClass();
					Method method = tempClass
							.getMethod(GoWidgetConstant.METHOD_ON_PAUSE, int.class);
					method.invoke(widgetView, info.mWidgetId);
				} catch (Exception e) {
					if (DEBUG) {
						Log.w(TAG, "invoke pause Widget err, widget id = " + info.mWidgetId);
					}
				}
				break;
			}

			case ACTION_RESUME : {
				try {
					Class tempClass = widgetView.getClass();
					Method method = tempClass.getMethod(GoWidgetConstant.METHOD_ON_RESUME,
							int.class);
					method.invoke(widgetView, info.mWidgetId);
				} catch (Exception e) {
					if (DEBUG) {
						Log.w(TAG, "invoke resume Widget err, widget id = " + info.mWidgetId);
					}
				}
				break;
			}

			case ACTION_APPLY_THEME : {
				try {
					if (obj != null && obj instanceof String) {
						final String themepkg = (String) obj;
						final int themeId = param;

						Class tempClass = widgetView.getClass();
						Method method = tempClass.getMethod(GoWidgetConstant.METHOD_ON_APPLY_THEME,
								Bundle.class);
						Bundle bundle = new Bundle();
						bundle.putString(GoWidgetConstant.GOWIDGET_THEME, themepkg);
						bundle.putInt(GoWidgetConstant.GOWIDGET_THEMEID, themeId);
						bundle.putInt(GoWidgetConstant.GOWIDGET_ID, info.mWidgetId);
						bundle.putInt(GoWidgetConstant.GOWIDGET_TYPE, info.mType);
						// 添加是否平板标志
						bundle.putBoolean(GoWidgetConstant.GOWIDGET_IS_TABLET,
								Machine.isTablet(mContext));
						boolean succeed = (Boolean) method.invoke(widgetView, bundle);
						// 成功应用主题后更新数据库
						if (succeed && (!themepkg.equals(info.mTheme) || themeId != info.mThemeId)) {
							info.mTheme = themepkg;
							info.mThemeId = themeId;
							mDataProvider.updateGoWidgetTheme(info);
						}
						if (null != widgetView) {
							widgetView.invalidate();
						}
					}

				} catch (Exception e) {
					if (DEBUG) {
						Log.w(TAG, "invoke ACTION_APPLY_THEME err, widget id = " + info.mWidgetId);
					}
				}
				break;
			}

			case ACTION_ENTER : {
				try {
					Class tempClass = widgetView.getClass();
					Method method = tempClass
							.getMethod(GoWidgetConstant.METHOD_ON_ENTER, int.class);
					method.invoke(widgetView, info.mWidgetId);
					info.mState = GoWidgetBaseInfo.STATE_ENTER;
				} catch (Exception e) {
					if (DEBUG) {
						Log.w(TAG, GoWidgetConstant.METHOD_ON_ENTER + ", widget id = "
								+ info.mWidgetId);
					}
				}
				break;
			}

			case ACTION_LEAVE : {
				try {
					Class tempClass = widgetView.getClass();
					Method method = tempClass
							.getMethod(GoWidgetConstant.METHOD_ON_LEAVE, int.class);
					method.invoke(widgetView, info.mWidgetId);
					info.mState = GoWidgetBaseInfo.STATE_LEAVE;
				} catch (Exception e) {
					if (DEBUG) {
						Log.w(TAG, "invoke leave Widget err, widget id = " + info.mWidgetId);
					}
				}
				break;
			}

			default :
				break;
		}
	}

	private boolean isExist(int widgetid) {
		int size = mWidgetMap.size();
		for (int i = 0; i < size; i++) {
			if (mWidgetMap.get(i).mWidgetId == widgetid) {
				if (DEBUG) {
					Log.i(TAG, "****** widgetid exist in widgetmap!!! id = " + widgetid);
				}
				return true;
			}
		}
		return false;
	}

	public GoWidgetBaseInfo getWidgetInfo(int widgetid) {
		synchronized (mLock) {
			int size = mWidgetMap.size();
			for (int i = 0; i < size; i++) {
				final GoWidgetBaseInfo info = mWidgetMap.get(i);
				if (info.mWidgetId == widgetid) {
					return info;
				}
			}
		}
		return null;
	}

	/**
	 * 分配一个widgetid
	 * 
	 * @return
	 */
	public synchronized int allocateWidgetId() {
		return mNextAppWidgetId--;
	}

	/**
	 * 占用某个id
	 * 
	 * @param widgetid
	 */
	public synchronized void takeWidgetId(int widgetid) {
		if (widgetid <= mNextAppWidgetId) {
			mNextAppWidgetId = widgetid - 1;
		}
	}

	public void deleteWidget(int id) {
		removeWidget(id);
		fireOnDelete(id);

		mWidgetView.remove(id);
		mDataProvider.deleteGoWidget(id);

		synchronized (mLock) {
			int i = 0;
			// 删除mWidgetMap缓存的数据
			while (i < mWidgetMap.size()) {
				if (mWidgetMap.get(i).mWidgetId == id) {
					mWidgetMap.remove(i);
					continue;
				}
				i++;
			}
		}
	}

	public boolean addGoWidget(GoWidgetBaseInfo info, boolean insertDB) {
		synchronized (mLock) {
			if (!isExist(info.mWidgetId)) {
				if (insertDB) {
					mDataProvider.addGoWidget(info);
				}

				mWidgetMap.add(info);
				return true;
			}
		}
		return false;
	}

	public boolean addGoWidget(GoWidgetBaseInfo info) {
		if (info == null) {
			return false;
		}

		return addGoWidget(info, true);
	}

	/**
	 * 删除某个包的widgt
	 * 
	 * @param pkgName
	 *            包名
	 */
	public void removeWidgetInfoFromPackage(String pkgName) {
		if (pkgName == null) {
			return;
		}
		synchronized (mLock) {
			int i = 0;
			while (i < mWidgetMap.size()) {
				GoWidgetBaseInfo info = mWidgetMap.get(i);
				if (info.mPackage.contains(pkgName)) {
					mWidgetMap.remove(i);
					continue;
				}
				i++;
			}
		}
		mDataProvider.deleteGoWidget(pkgName);
	}

	public void startListening() {
		synchronized (mLock) {
			if (DEBUG) {
				Log.i(TAG, "WidgetManager startListening");
			}
			@SuppressWarnings("unchecked")
			ArrayList<GoWidgetBaseInfo> list = (ArrayList<GoWidgetBaseInfo>) mWidgetMap.clone();
			mUiHandler.startFireOnStart(list);
		}
	}

	public void stopListening() {
		synchronized (mLock) {
			if (DEBUG) {
				Log.i(TAG, "WidgetManager stopListening");
			}
			@SuppressWarnings("unchecked")
			ArrayList<GoWidgetBaseInfo> list = (ArrayList<GoWidgetBaseInfo>) mWidgetMap.clone();
			mUiHandler.startFireOnStop(list);
		}
	}

	public static boolean isGoWidget(int widgetId) {
		if (widgetId < INVALID_GOWIDGET_ID) {
			return true;
		}
		return false;
	}

	/**
	 * 是否刷新加载失败的widget
	 * 
	 * @param info
	 * @return
	 */
	private boolean refreshErrorView(GoWidgetBaseInfo info) {
		if (info != null) {
			final View oldView = mWidgetView.get(info.mWidgetId);
			if (oldView != null && oldView instanceof WidgetErrorView) {
				mWidgetView.remove(info.mWidgetId);
				ViewGroup parent = (ViewGroup) oldView.getParent();
				if (parent != null && parent instanceof CellLayout) {
					View newView = createView(info.mWidgetId);
					((CellLayout) parent).replaceView(oldView, newView);
				}
				parent = null;
				applyWidget(info, ACTION_START);
				return true;
			}
		}
		return false;
	}

	/**
	 * sd卡挂载 后更新gowidget
	 * 
	 * @param list
	 */
	public void refreshExternalWidget(ArrayList<String> packageList) {

		synchronized (mLock) {
			ArrayList<GoWidgetBaseInfo> updateList = new ArrayList<GoWidgetBaseInfo>();
			for (GoWidgetBaseInfo widgetInfo : mWidgetMap) {
				View widgetView = mWidgetView.get(widgetInfo.mWidgetId);
				// 加载出错的widget
				if (widgetView != null && widgetView instanceof WidgetErrorView) {
					updateList.add(widgetInfo);
				} else {

					if (packageList == null || packageList.isEmpty()) {
						continue;
					}					
					// 主题包在sd卡上的
					for (String themePkg : packageList) {
						if (themePkg.equals(widgetInfo.mTheme)) {
							updateList.add(widgetInfo);
							break; // 跳出内循环，对比下一个widgetInfo
						}
					}
				}
			}

			if (updateList.isEmpty()) {
				updateList = null;
				return;
			}

			// 启动handler刷新主题
			mUiHandler.startRefreshExtWidget(updateList);
		}
	}

	/**
	 * 更新某个包的widget
	 * 
	 * @param packageName
	 */
	public void updateGoWidget(String packageName) {
		if (packageName == null
				|| (!packageName.contains(ICustomAction.MAIN_GOWIDGET_PACKAGE) && !packageName
						.equals(LauncherEnv.GOSMS_PACKAGE))) {
			return;
		}
		if (packageName.equals(LauncherEnv.GOSMS_PACKAGE)) {
			int versionCode = AppUtils.getVersionCodeByPkgName(mContext, packageName);
			if (versionCode < 80) {
				return;
			}
		}

		if (DEBUG) {
			Log.i(TAG, "updateGoWidget pkg = " + packageName);
		}
		packageName = getInflatePackage(packageName);
		ArrayList<GoWidgetBaseInfo> updateList = new ArrayList<GoWidgetBaseInfo>();
		synchronized (mLock) {
			for (GoWidgetBaseInfo widgetInfo : mWidgetMap) {
				if (widgetInfo != null && widgetInfo.mPackage != null
						&& widgetInfo.mPackage.equals(packageName)) {
					updateList.add(widgetInfo);
				}
			}
		}
		startReplaceWidget(updateList);
	}

	public AsyncHandler getAsyncHandler() {
		return mAsyncHandler;
	}

	/***
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  
	 * @date  [2012-10-15]
	 */
	private class WidgetAsyncHandler extends AsyncHandler {
		static final int MSG_FIRE_ON_LEAVE = 0x10;
		static final int MSG_FIRE_ON_ENTER = 0x11;
		private boolean mStop = false;

		WidgetAsyncHandler() {
			super(ThreadName.GOWIDGET_ASYNC_HANDLER, Process.THREAD_PRIORITY_BACKGROUND);
		}

		void stop() {
			mStop = true;
			cancel();
		}

		private synchronized void notifyVisible(ViewGroup viewGroup, boolean visible) {
			if (mStop) {
				return;
			}

			if (DEBUG) {
				Log.i(TAG, visible ? "onEnter" : "onLeave");
			}
			try {
				int size = viewGroup.getChildCount();
				int state = visible ? GoWidgetBaseInfo.STATE_ENTER : GoWidgetBaseInfo.STATE_LEAVE;
				View view = null;
				for (int i = 0; i < size; i++) {
					view = viewGroup.getChildAt(i);
					if (view != null && view.getVisibility() == View.VISIBLE
							&& !(view instanceof WidgetErrorView)) {
						Object tag = view.getTag();
						if (tag != null && tag instanceof ScreenAppWidgetInfo) {
							int widgetid = ((ScreenAppWidgetInfo) tag).mAppWidgetId;
							if (isGoWidget(widgetid)) {
								GoWidgetBaseInfo info = getWidgetInfo(widgetid);
								if (info != null && info.mState != state) {
									applyWidget(info, visible ? ACTION_ENTER : ACTION_LEAVE);
								}
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}

		@Override
		public void handleAsyncMessage(Message msg) {
			if (mStop) {
				return;
			}

			switch (msg.what) {
				case MSG_FIRE_ON_LEAVE : {
					if (msg.obj != null && msg.obj instanceof ViewGroup) {
						notifyVisible((ViewGroup) msg.obj, false);
					}
					break;
				}

				case MSG_FIRE_ON_ENTER : {
					if (msg.obj != null && msg.obj instanceof ViewGroup) {
						notifyVisible((ViewGroup) msg.obj, true);
					}
					break;
				}

				default :
					break;
			}
		}
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  
	 * @date  [2012-10-15]
	 */
	static class WidgetHandler extends DeferredHandler {
		static final int MESSAGE_FIRE_ONSTART = 0x1;
		static final int MESSAGE_FIRE_ONSTOP = 0x2;

		static final int MESSAGE_REPLACE_WIDGET = 0x3;
		static final int MESSAGE_APPLY_WIDGET_THEME = 0x4;
		static final int MESSAGE_REFRESH_EXTERNAL_WIDGTE = 0x5;

		private int mIndex = 0;
		private int mCount = 0;
		private final WeakReference<GoWidgetManager> mManReference;
		private ArrayList<GoWidgetBaseInfo> mUpdateList;

		WidgetHandler(GoWidgetManager manager) {
			mManReference = new WeakReference<GoWidgetManager>(manager);
			mCount = manager.getWidgetCount();
		}

		public void startFireOnStart(ArrayList<GoWidgetBaseInfo> widgetList) {
			mUpdateList = widgetList;
			mIndex = 0;
			mCount = mUpdateList.size();
			sendEmptyMessage(MESSAGE_FIRE_ONSTART);
		}

		public void startFireOnStop(ArrayList<GoWidgetBaseInfo> widgetList) {
			mUpdateList = widgetList;
			mIndex = 0;
			mCount = mUpdateList.size();
			sendEmptyMessage(MESSAGE_FIRE_ONSTOP);
		}

		public void startReplaceWidget(ArrayList<GoWidgetBaseInfo> widgetList) {
			mUpdateList = widgetList;
			mIndex = 0;
			mCount = mUpdateList.size();
			sendEmptyMessage(MESSAGE_REPLACE_WIDGET);
		}

		public void startApplyWidgetTheme(String theme) {
			mIndex = 0;
			GoWidgetManager manager = mManReference.get();
			if (manager != null) {
				mCount = manager.getWidgetCount();
			}

			sendMessage(MESSAGE_APPLY_WIDGET_THEME, 0, 0, theme);
		}

		public void startRefreshExtWidget(ArrayList<GoWidgetBaseInfo> widgetList) {
			mIndex = 0;
			mUpdateList = widgetList;
			mCount = widgetList.size();
			sendEmptyMessage(MESSAGE_REFRESH_EXTERNAL_WIDGTE);
		}

		@Override
		public void handleIdleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_FIRE_ONSTART : {
					if (mUpdateList != null && !mUpdateList.isEmpty()) {
						final GoWidgetManager manager = mManReference.get();
						final GoWidgetBaseInfo widgetBaseInfo = mUpdateList.remove(0);
						if (manager != null) {
							manager.applyWidget(widgetBaseInfo, ACTION_START);
						}

						// 下一次操作
						if (mUpdateList.isEmpty()) {
							mUpdateList = null;
							// 通知widget进入当前屏幕
							GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
									IDiyMsgIds.SCREEN_FIRE_WIDGET_ONENTER, -1, null, null);
						} else {
							sendEmptyMessage(msg.what);
						}
					}
					break;
				}

				case MESSAGE_FIRE_ONSTOP : {
					if (mUpdateList != null && !mUpdateList.isEmpty()) {
						final GoWidgetManager manager = mManReference.get();
						final GoWidgetBaseInfo widgetBaseInfo = mUpdateList.remove(0);
						if (manager != null) {
							manager.applyWidget(widgetBaseInfo, ACTION_STOP);
						}

						// 下一次操作
						if (mUpdateList.isEmpty()) {
							mUpdateList = null;
						} else {
							sendEmptyMessage(msg.what);
						}
					}
					break;
				}

				case MESSAGE_REPLACE_WIDGET : {
					if (mUpdateList != null && !mUpdateList.isEmpty()) {
						final GoWidgetManager manager = mManReference.get();
						final GoWidgetBaseInfo widgetBaseInfo = mUpdateList.remove(0);
						if (manager != null) {
							manager.doReplaceWidget(widgetBaseInfo);
						}

						// 下一次操作
						if (mUpdateList.isEmpty()) {
							mUpdateList = null;
						} else {
							sendEmptyMessage(msg.what);
						}
					}
				}
					break;

				case MESSAGE_APPLY_WIDGET_THEME : {
					final GoWidgetManager manager = mManReference.get();
					if (manager != null) {
						manager.doApplyWidgetTheme(mIndex, (String) msg.obj);
					}
					if (++mIndex < mCount) {
						// sendEmptyMessage(msg.what);
						sendMessage(MESSAGE_APPLY_WIDGET_THEME, 0, 0, msg.obj);
					}
				}
					break;

				case MESSAGE_REFRESH_EXTERNAL_WIDGTE : {
					if (mUpdateList != null && !mUpdateList.isEmpty()) {
						final GoWidgetManager manager = mManReference.get();
						final GoWidgetBaseInfo info = mUpdateList.remove(0);

						// 如果没有完成替换加载错误的widget，既更新主题
						if (manager != null && !manager.refreshErrorView(info)) {
							manager.applyWidget(info, ACTION_APPLY_THEME, info.mThemeId,
									info.mTheme);
						}

						// 下一次操作
						if (mUpdateList.isEmpty()) {
							mUpdateList = null;
						} else {
							sendEmptyMessage(msg.what);
						}
					}
				}
					break;

				default :
					break;
			}
		}
	}
}
