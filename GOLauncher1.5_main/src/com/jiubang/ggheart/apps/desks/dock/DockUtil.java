package com.jiubang.ggheart.apps.desks.dock;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.go.util.Utilities;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingVisualIconTabView;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.autofit.AnimationControler;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.components.BubbleTextView;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.info.FunTaskItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.launcher.AppIdentifier;
import com.jiubang.ggheart.launcher.GOLauncherApp;


/**
 * 
 * <br>类描述:dock工具类
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-10-16]
 */
public class DockUtil {
	public static final int MOVE_TO_LEFT = 0;
	public static final int MOVE_TO_RIGHT = 1;
	public static final int MOVE_TO_CENTENT = 2;
	public static final int MOVE_TO_OUT = 3;
	/** 错误值 */
	public static final int ERROR_NONE = 1200;
	public static final int ERROR_BAD_PARAM = 1201;
	public static final int ERROR_KEEPDATA_FAILD = 1202;

	public final static int HANDLE_INIT_DOCK_FRAME = 0;
	public final static int HANDLE_SHOW_DIRTYDATA_TIPS = 2; // 弹出有脏数据提示框
	public final static int HANDLE_REFRESH_DOCK_ITEM_UI = 3; // 刷新dock条子项UI
	public final static int HANDLE_ADD_APPLICATION = 4; // 插入应用程序
	public final static int HANDLE_ANIMATION_ADD_ITEM_FROM_FOLDER = 5; // 从文件夹添加到dock动画
	public final static int HANDLE_ANIMATION_MERGE_FOLDER = 6; // 合并文件夹动画
	public final static int HANDLE_SNAP_TO_SCREEN = 7; // 合并文件夹动画
	public final static int HANDLE_ANIMATION_START_MOVE = 8; // 移动DOCK图标动画
	public final static int HANDLE_ANIMATION_START_MOVE_TO_SCREEN = 9; //移动dock图标到屏幕层动画
	// 新加mxj
	public final static int HANDLE_MODIFY_INDEX = 10; // 通知更新dockItem所在行的索引
	public final static int HANDLE_INSERT_ITEM = 11; // 通知插入新的项

	// 动画类型
	public final static int ANIMATION_ENTER_SHOW = 2;
	public final static int ANIMATION_LEAVE_HIDE = 3;

	// 挤压交换动画类型
	public final static int ANIMATION_ZOOM_NORMAL = 0; // 不变
	public final static int ANIMATION_ZOOM_NORMAL_TO_SMALL = 1; // 缩小
	public final static int ANIMATION_ZOOM_NORMAL_TO_BIG = 2; // 放大
	public final static int ANIMATION_ZOOM_SMALL_TO_NORMAL = 3;
	public final static int ANIMATION_ZOOM_SMALL_TO_SMALL = 4;
	public final static int ANIMATION_ZOOM_BIG_TO_SMALL = 5;

	// 模式类型
	public static int sModeFit = 1; // 自适应
	public static int sModeUnfit = 2; // 非适应

	/** 一行几个图标 */
	public static final int ICON_COUNT_IN_A_ROW = 5;

	/** 一共几行 */
	public static final int TOTAL_ROWS = 3;

	public static final float DOCK_RESPOND_GESTURE_OFFSET = 12;

	public static final float DOCK_DISDINGUISH_CLICK_OR_DRAG_OFFSET = 7;

	public static final int DOCK_COUNT = 15;

	// Dock 风格数目(和资源数组对应)
	public final static int DOCK_STYLE_COUNT = 2;

	// 风格字符串
	public final static String DOCK_DEFAULT_STYLE_STRING = "defaultstyle";
	public final static String DOCK_TRANSPARENT_STYLE_STRING = "transparentstyle";

	// dock文件夹请求异步刷新的原因
	public final static int TYPE_REFRASH_FOLDER_CONTENT_NORMAL = 1;
	public final static int TYPE_REFRASH_FOLDER_CONTENT_UNINSTALLAPP = 2;

	/**
	 * 图标在一行dock中的位置类型
	 */
	public final static int POSITION_TYPE_HEAD = 1; // 行的最左边／列的最下边
	public final static int POSITION_TYPE_TAIL = 2; // 行的最右边／列的最上边
	public final static int POSITION_TYPE_MIDDLE = 3; // 行／列的中间

	/**
	 * changeapp:类型
	 */
	public static final int CHANGE_FROM_APPLICATON = 1;
	public static final int CHANGE_FROM_SHORTCUT = 2;
	public static final int CHANGE_FROM_GESTURE = 3;
	public static final int CHANGE_FROM_DRAG = 4;
	public static final int CHANGE_FROM_FOLDER = 5;
	public static final int CHANGE_FROM_DELETEFOLER = 6; // folder只剩一个图标时，删除folder

	public static final int MOVE_TO_SCREEN_TRANSACTION_DURATION = 150; //dock挤压到桌面，位移时间
	public static final int MOVE_TO_SCREEN_SHAKE_DURATION = 300;       //dock挤压到桌面，抖动动画时间

	private static final Intent INTENT_SYS_BROWSER = AppIdentifier.getSysBrowserIntent();
	private static final Intent INTENT_GOOGLE_URI = AppIdentifier.getGoogleUriIntent();

	// 调试日志
	private static final String RXQ_LOG = "rxq";

	public static void rxqLog(String string) {
		Log.i(RXQ_LOG, string);
	}

	/**
	 * 调ＢＵＧ日志记录
	 * 
	 * @param string
	 */
	public static void writeLogToTXT(String string) {
		try {
			File file = new File("/sdcard/Gotest.txt");

			if (!file.exists()) {
				file.createNewFile();
			}

			RandomAccessFile rdfile = new RandomAccessFile("/sdcard/Gotest.txt", "rw");
			long filelength = file.length();
			rdfile.seek(filelength);
			rdfile.writeBytes(string + "\n");
			rdfile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取dock图标图片size
	 * 
	 * @param count
	 *            当前行共有几个图标
	 * @return
	 */
	public static int getIconSize(int count) {
		int ret = -1;
		final int style = GoLauncher.getIconSizeStyle();
		switch (count) {
			case 0 :
			case 1 :
			case 2 :
			case 3 :
			case 4 :
				ret = Utilities.getStandardIconSize(GOLauncherApp.getContext());
				break;

			case 5 :
				// 默认和自定图标
				if (style == DeskSettingVisualIconTabView.DEFAULT_ICON_SIZE
						|| style == DeskSettingVisualIconTabView.DIY_ICON_SIZE) {
					ret = Utilities.getStandardIconSize(GOLauncherApp.getContext()) * 60 / 72;
				} else {
					ret = Utilities.getStandardIconSize(GOLauncherApp.getContext()) * 72 / 84;
				}
				break;

			default :
				break;
		}
		if (ret == -1) {
			ret = style == DeskSettingVisualIconTabView.LARGE_ICON_SIZE
					? DrawUtils.dip2px(48f)
					: DrawUtils.dip2px(40f);
		}
		return ret;
	}

	public static int getBgHeight() {
		return GOLauncherApp.getContext().getResources()
				.getDimensionPixelSize(R.dimen.dock_bg_height);
	}

	/**
	 * 获取合并文件夹背景图缩放比例
	 * 
	 * @param dockViewSize
	 *            图标个数
	 * @param mAnimationControler
	 *            动画控制器
	 * @param dragView
	 *            当前抓起的图标
	 * @param firstCurLine
	 *            第一次抓起图标的row
	 * @param curLine
	 *            当前row
	 * @return
	 */
	public static float getZoomProportion(int dockViewSize, AnimationControler mAnimationControler,
			View dragView, int firstCurLine, int curLine) {
		float zoomProportion = 1.0f;
		float bitMapSizeSmall = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
		float bitMapSizeBig = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW - 1);
		float changeSize = 0.16f;
		int iconSize = DrawUtils.dip2px(48f);
		if (mAnimationControler != null && dragView != null) {
			boolean isDockIconScrentToDock = mAnimationControler.getIsDockIconScrentToDock();
			boolean isDeskIconScrentToDock = mAnimationControler.getIsDeskIconScrentToDock();
			if (dragView instanceof BubbleTextView) {
				if (dockViewSize == DockUtil.ICON_COUNT_IN_A_ROW
						|| (dockViewSize == DockUtil.ICON_COUNT_IN_A_ROW - 1 && !isDeskIconScrentToDock)) {
					zoomProportion = bitMapSizeSmall / iconSize + changeSize;
				} else {
					zoomProportion = bitMapSizeBig / iconSize + changeSize;
				}
			}

			else if (dragView instanceof DockIconView) {
				if (dockViewSize == DockUtil.ICON_COUNT_IN_A_ROW
						&& !isDockIconScrentToDock
						|| (firstCurLine != curLine
								&& dockViewSize == DockUtil.ICON_COUNT_IN_A_ROW - 1 && !isDeskIconScrentToDock)) {
					zoomProportion = bitMapSizeSmall / iconSize + changeSize;
				} else {
					zoomProportion = bitMapSizeBig / iconSize + changeSize;
				}
			}
		} else {
			if (dockViewSize < DockUtil.ICON_COUNT_IN_A_ROW) {
				zoomProportion = bitMapSizeBig / iconSize + changeSize;
			} else {
				zoomProportion = bitMapSizeSmall / iconSize + changeSize;
			}
		}

		return zoomProportion;
	}

	/**
	 * 获取合并文件夹背景图缩放比例
	 * 
	 * @param dockViewSize
	 *            图标个数
	 * @param mAnimationControler
	 *            动画控制器
	 * @param dragView
	 *            当前抓起的图标
	 * @param firstCurLine
	 *            第一次抓起图标的row
	 * @param curLine
	 *            当前row
	 * @return
	 */
	public static float getZoomProportion() {
		float zoomProportion = 1.0f;
		float bitMapSizeSmall = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
		float changeSize = 0.16f;
		int iconSize = DrawUtils.dip2px(48f);
		zoomProportion = bitMapSizeSmall / iconSize + changeSize;
		return zoomProportion;
	}

	/**
	 * 判断是否dock自定义的拨号
	 * 
	 * @param intent
	 * @return
	 */
	public static final boolean isDockDial(Intent intent) {
		return ConvertUtils.selfIntentCompare(intent,
				AppIdentifier.createSelfDialIntent(GOLauncherApp.getContext()));
	}

	/**
	 * 判断是否dock自定义的联系人
	 * 
	 * @param intent
	 * @return
	 */
	public static final boolean isDockContacts(Intent intent) {
		return ConvertUtils.selfIntentCompare(intent,
				AppIdentifier.createSelfContactIntent(GOLauncherApp.getContext()));
	}

	/**
	 * 判断是否dock自定义的功能表
	 * 
	 * @param intent
	 * @return
	 */
	public static final boolean isDockAppdrawer(Intent intent) {
		return ConvertUtils.selfIntentCompare(intent, AppIdentifier.createAppdrawerIntent());
	}

	/**
	 * 判断是否dock自定义的信息
	 * 
	 * @param intent
	 * @return
	 */
	public static final boolean isDockSms(Intent intent) {
		return ConvertUtils.selfIntentCompare(intent, AppIdentifier.createSelfMessageIntent());
	}

	/**
	 * 判断是否dock自定义的浏览器
	 * 
	 * @param intent
	 * @return
	 */
	public static final boolean isDockBrowser(Intent intent) {
		boolean isSysBrowser = ConvertUtils.selfIntentCompare(intent, INTENT_SYS_BROWSER);
		boolean isGoogleUri = false;
		if (!isSysBrowser) {
			isGoogleUri = ConvertUtils.selfIntentCompare(intent, INTENT_GOOGLE_URI);
		}

		return isSysBrowser || isGoogleUri;
	}

	/**
	 * 是否是最后一个dock功能表图标
	 * 
	 * @param intent
	 * @return
	 */
	public static boolean isTheLastDockAppdrawer(View view) {
		Intent intent = null;
		if (null != view && null != view.getTag() && view.getTag() instanceof ShortCutInfo) {
			ShortCutInfo info = (ShortCutInfo) view.getTag();
			if (info.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
				intent = info.mIntent;
			}
		}

		if (isDockAppdrawer(intent)) {
			DataProvider provider = DataProvider.getInstance(GOLauncherApp.getContext());
			return provider.isTheLastDockAppdrawer();
		}
		return false;
	}

	/**
	 * <br>
	 * 功能简述:过滤dock默认浏览器intent <br>
	 * 功能详细描述:过滤dock默认浏览器intent,过滤规则自定,主要是根据市场部运营部的需求
	 * 
	 * <br>
	 * 过滤规则： <br>
	 * 满足以下条件： <br>
	 * 1: dock默认浏览器 <br>
	 * 2: CN用户； <br>
	 * 3: 非200、205渠道； <br>
	 * 4: 非3.12之前的老用户；
	 * 
	 * <br>
	 * 新intent规则： <br>
	 * 1: 查找正在运行的浏览器，如果找到，则直接拉起，否则，进行步骤2： <br>
	 * 2: 跳转：http://xuan.3g.cn/index.aspx?fr=goindex，浏览器交由系统查找
	 * 
	 * <br>
	 * 注意:
	 * 
	 * @param context
	 * @param itemtype
	 *            {@link IItemType}
	 * @param intent
	 *            传入intent
	 * @return　过滤后的新intent
	 */
	public static final Intent filterDockBrowserIntent(Context context, int itemtype, Intent intent) {
		try {
			if (itemtype != IItemType.ITEM_TYPE_SHORTCUT
					|| GoStorePhoneStateUtil.is200ChannelUid(context)
					|| GoStorePhoneStateUtil.getUid(context).equals("205") || Machine.isNotCnUser()
					|| !isDockBrowser(intent) || isVersionBefore312(context)) {
				return intent;
			}

			// 跳转intent
			Uri uri = Uri.parse("http://xuan.3g.cn/index.aspx?fr=goindex");
			Intent myIntent = new Intent(Intent.ACTION_VIEW, uri);

			// 1:已安装的浏览器列表
			PackageManager pm = context.getPackageManager();
			List<ResolveInfo> resolveList = pm.queryIntentActivities(myIntent, 0);

			if (resolveList != null && !resolveList.isEmpty()) {
				// 2:获取当前运行程序列表
				ArrayList<FunTaskItemInfo> curRunList = null;
				try {
					curRunList = AppCore.getInstance().getTaskMgrControler().getProgresses();
				} catch (Throwable e) {
				}
				int curRunSize = (curRunList != null) ? curRunList.size() : 0;

				// 两个列表循环遍历
				for (int i = curRunSize - 1; i > 0; i--) {
					FunTaskItemInfo funTaskItemInfo = curRunList.get(i);
					Intent funIntent = funTaskItemInfo.getAppItemInfo().mIntent;
					ComponentName funComponentName = funIntent.getComponent();
					for (ResolveInfo resolveInfo : resolveList) {
						if (resolveInfo.activityInfo.packageName != null
								&& resolveInfo.activityInfo.packageName.equals(funComponentName
										.getPackageName())) {
							// 找到正在运行的浏览器，直接拉起
							if (funIntent.getComponent() != null) {
								String pkgString = funIntent.getComponent().getPackageName();
								if (pkgString != null) {
									if (pkgString.equals("com.android.browser")
											|| pkgString.equals("com.dolphin.browser.cn")
											|| pkgString.equals("com.android.chrome")
											|| pkgString.equals("com.qihoo.browser")) {
										//上述浏览器后台拉起会跳转浏览器首页，而非保存的用户原来页面
										funIntent.setAction(Intent.ACTION_VIEW);
										funIntent.setData(uri);
									}
								}
							}

							return funIntent;
						}
					}
				}
			}
			return myIntent;
		} catch (Throwable e) {
			//有错，则不进行intent过滤
		}
		return intent;
	}

	/**
	 * <br>
	 * 功能简述:是否3.12之前的老用户 <br>
	 * 功能详细描述: <br>
	 * 注意:供{@link#filterDockBrowserIntent} 调用
	 * 
	 * @return
	 */
	private static final boolean isVersionBefore312(Context context) {
		boolean isVersionBefore312 = false;
		isVersionBefore312 = DataProvider.getInstance(context).isVersionBefore312();
		return isVersionBefore312;
	}
}
