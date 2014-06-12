package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import mobi.intuitit.android.content.LauncherIntent;
import mobi.intuitit.android.content.LauncherMetadata;

import org.acra.ErrorReporter;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.go.util.Utilities;
import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsLineLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.gowidget.GoWidgetConstant;
import com.jiubang.ggheart.apps.gowidget.GoWidgetFinder;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.apps.gowidget.WidgetParseInfo;
import com.jiubang.ggheart.components.BubbleTextView;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.data.info.ScreenFolderInfo;
import com.jiubang.ggheart.data.info.ScreenLiveFolderInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.FolderStyle;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;

/**
 * 屏幕层工具类
 * 
 */
public class ScreenUtils {
	public static void showToast(int id, Context context) {
		String textString = context.getString(id);
		try {
			DeskToast.makeText(context, textString, Toast.LENGTH_SHORT).show();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			Log.w("showToast", " Error Code is " + e);
		} catch (Throwable e2) {
			Log.w("showToast", " Error Code is " + e2);
		}
		textString = null;
	}

	/**
	 * 功能描述：弹出Toast
	 * 
	 * @param id
	 * @param context
	 * @param duration
	 *            控制Toast弹出时间 (Either {Toast.LENGTH_SHORT} or
	 *            {Toast.LENGTH_LONG})
	 */
	public static void showToast(int id, Context context, int duration) {
		String textString = context.getString(id);
		try {
			DeskToast.makeText(context, textString, duration).show();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			Log.w("showToast", " Error Code is " + e);
		} catch (Throwable e2) {
			Log.w("showToast", " Error Code is " + e2);
		}
		textString = null;
	}

	static void unbindShortcut(ShortCutInfo shortCutInfo) {
		if (shortCutInfo != null && shortCutInfo.mIcon != null) {
			shortCutInfo.selfDestruct();
		}
	}

	public static void unbindeUserFolder(UserFolderInfo folderInfo) {
		if (folderInfo != null) {
			folderInfo.clear();
			folderInfo.selfDestruct();
		}
	}

	static void unbindLiveFolder(ScreenLiveFolderInfo folderInfo) {
		if (folderInfo != null) {
			folderInfo.selfDestruct();
		}
	}

	static void unbindDesktopObject(ArrayList<ItemInfo> screenInfos) {
		if (screenInfos == null) {
			return;
		}

		for (ItemInfo itemInfo : screenInfos) {
			if (itemInfo == null) {
				continue;
			}

			final int itemType = itemInfo.mItemType;
			switch (itemType) {
			case IItemType.ITEM_TYPE_APPLICATION: {
				unbindShortcut((ShortCutInfo) itemInfo);
				break;
			}
			case IItemType.ITEM_TYPE_SHORTCUT: {
				unbindShortcut((ShortCutInfo) itemInfo);
				break;
			}

			case IItemType.ITEM_TYPE_USER_FOLDER: {
				unbindeUserFolder((UserFolderInfo) itemInfo);
				break;
			}

			case IItemType.ITEM_TYPE_LIVE_FOLDER: {
				unbindLiveFolder((ScreenLiveFolderInfo) itemInfo);
				break;
			}
			default:
				break;
			}
		}
	}

	static int computeIndex(int cur, int src, int dst) {
		if (src > cur && dst <= cur) {
			++cur; // 从当前位置后面移到前面
		} else if (src < cur && dst >= cur) {
			--cur; // 从当前位置前面移到后面
		} else if (src == cur) {
			cur = dst;
		}
		return cur;
	}

	static void changeFoderIconState(boolean open, Rect rect,
			Workspace workspace) {
		final int screenIndex = workspace.getCurrentScreen();
		final CellLayout currentScreen = (CellLayout) workspace
				.getChildAt(screenIndex);
		if (currentScreen == null) {
			return;
		}
		final int count = currentScreen.getChildCount();
		for (int i = 0; i < count; i++) {
			View childView = currentScreen.getChildAt(i);
			if (childView instanceof FolderIcon) {
				// 如果使一文件夹图标为打开，则其余全关闭
				if (open) {
					if (childView.getLeft() == rect.left
							&& childView.getTop() == rect.top
							&& childView.getRight() == rect.right
							&& childView.getBottom() == rect.bottom) {
						((FolderIcon) childView).open();
					} else {
						((FolderIcon) childView).close();
					}
				}
				// 如果使一文件夹图标为关闭，则只针对当前文件夹
				else {
					if (childView.getLeft() == rect.left
							&& childView.getTop() == rect.top
							&& childView.getRight() == rect.right
							&& childView.getBottom() == rect.bottom) {
						((FolderIcon) childView).close();
						return;
					}
				}

			}
		}
	}

	static boolean findVacant(int[] xy, int spanX, int spanY, int screenIndex,
			Workspace workspace) {
		if (screenIndex < 0 || screenIndex >= workspace.getChildCount()) {
			return false;
		}

		final CellLayout destScreen = (CellLayout) workspace
				.getChildAt(screenIndex);
		if (destScreen == null) {
			return false;
		}
		boolean isExistVacant = destScreen.getVacantCell(xy, spanX, spanY);
		return isExistVacant;
	}

	static int[] findNearestVacant(int pixelX, int pixelY, int spanX,
			int spanY, int screenIndex, Workspace workspace) {
		if (screenIndex < 0 || screenIndex >= workspace.getChildCount()) {
			return null;
		}

		final CellLayout destScreen = (CellLayout) workspace
				.getChildAt(screenIndex);
		if (destScreen == null) {
			return null;
		}
		int[] cell = destScreen.findNearestVacantArea(pixelX, pixelY, spanX,
				spanY, destScreen.findAllVacantCells(
						destScreen.getOccupiedCells(), null), null);
		return cell;
	}

	static void closeFolderView(FolderView folder) {
		if (folder == null) {
			return;
		}

		final ScreenFolderInfo folderInfo = folder.getInfo();
		if (folderInfo != null) {
			folderInfo.mOpened = false;
		}

		folder.onClose();
		ViewGroup parent = (ViewGroup) folder.getParent();
		if (parent != null) {
			parent.removeView(folder);
		}
	}

	static boolean isExistShortcut(Intent intent, Workspace workspace) {
		if (intent == null) {
			return false;
		}

		boolean ret = false;
		int screenCount = workspace.getChildCount();
		String intentString = ConvertUtils.intentToString(intent);
		if (intentString == null) {
			return false;
		}

		for (int i = 0; i < screenCount; i++) {
			CellLayout screen = (CellLayout) workspace.getChildAt(i);
			int childCount = screen.getChildCount();
			for (int j = 0; j < childCount; j++) {
				Object tag = screen.getChildAt(j).getTag();
				if (tag != null && tag instanceof ShortCutInfo) {
					ShortCutInfo shortCutInfo = (ShortCutInfo) tag;
					if (shortCutInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
						String scIntentString = ConvertUtils
								.intentToString(shortCutInfo.mIntent);
						if (intentString.equals(scIntentString)) {
							ret = true;
							break;
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * 获取空白单元格个数
	 * 
	 * @param screenIndex
	 *            屏幕索引
	 * @param vacantList
	 *            返回空单元格列表
	 * @return
	 */
	static int findSingleVancant(int screenIndex, ArrayList<Point> vacantList,
			Workspace workspace) {
		if (screenIndex < 0 || screenIndex >= workspace.getChildCount()) {
			return 0;
		}

		final CellLayout destScreen = (CellLayout) workspace
				.getChildAt(screenIndex);
		if (destScreen != null) {
			return destScreen.getSingleVacantCellCount(vacantList);
		}
		return 0;
	}

	static boolean ocuppiedArea(int screen, int id, Rect rect,
			Workspace workspace) {
		int screenCount = workspace.getChildCount();
		if (screen < 0 || screen >= screenCount) {
			return false;
		}

		CellLayout cellLayout = (CellLayout) workspace.getChildAt(screen);
		if (cellLayout == null) {
			return false;
		}

		Rect r = new Rect();
		final int childCount = cellLayout.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View view = cellLayout.getChildAt(i);
			if (view == null || view.getTag() == null) {
				continue;
			}

			ItemInfo it = (ItemInfo) view.getTag();
			if (it instanceof ScreenAppWidgetInfo
					&& ((ScreenAppWidgetInfo) it).mAppWidgetId == id) {
				continue;
			}

			r.set(it.mCellX, it.mCellY, it.mCellX + it.mSpanX, it.mCellY
					+ it.mSpanY);
			if (rect.intersect(r)) {
				return true;
			}
		}

		return false;
	}

	static Search findSearchOnCurrentScreen(Workspace workspace) {
		CellLayout currentScreen = workspace.getCurrentScreenView();
		if (currentScreen != null) {
			for (int i = 0; i < currentScreen.getChildCount(); i++) {
				View view = currentScreen.getChildAt(i);
				if (view != null && view instanceof Search) {
					return (Search) view;
				}
			}
		}
		return null;
	}

	static void appwidgetReadyBroadcast(int appWidgetId, ComponentName cname,
			int[] widgetSpan, Context context) {
		if (GoWidgetManager.isGoWidget(appWidgetId)) {
			return;
		}

		Intent motosize = new Intent(ICustomAction.ACTION_SET_WIDGET_SIZE);

		motosize.setComponent(cname);
		motosize.putExtra("appWidgetId", appWidgetId);
		motosize.putExtra("spanX", widgetSpan[0]);
		motosize.putExtra("spanY", widgetSpan[1]);
		motosize.putExtra("com.motorola.blur.home.EXTRA_NEW_WIDGET", true);
		context.sendBroadcast(motosize);

		Intent ready = new Intent(LauncherIntent.Action.ACTION_READY)
				.putExtra(LauncherIntent.Extra.EXTRA_APPWIDGET_ID, appWidgetId)
				.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
				.putExtra(LauncherIntent.Extra.EXTRA_API_VERSION,
						LauncherMetadata.CurrentAPIVersion).setComponent(cname);
		context.sendBroadcast(ready);
	}

	static void pauseGoWidget(int screenIndex,
			HashMap<Integer, ArrayList<ItemInfo>> allInfos) {
		if (allInfos == null) {
			return;
		}

		ArrayList<ItemInfo> list = allInfos.get(screenIndex);
		final GoWidgetManager widgetManager = AppCore.getInstance()
				.getGoWidgetManager();
		if (list != null) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				ItemInfo info = list.get(i);
				if (info != null && info instanceof ScreenAppWidgetInfo) {
					ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) info;
					if (GoWidgetManager.isGoWidget(widgetInfo.mAppWidgetId)) {
						widgetManager.pauseWidget(widgetInfo.mAppWidgetId);
					}
				}
			}
		}
	}

	/**
	 * 获取widget所在的屏幕索引
	 * 
	 * @param widgetid
	 *            widgetid
	 * @param allInfos
	 * @return
	 */
	static int getScreenIndexofWidget(int widgetid,
			HashMap<Integer, ArrayList<ItemInfo>> allInfos) {
		int screenCount = allInfos.size();
		for (int i = 0; i < screenCount; i++) {
			ArrayList<ItemInfo> list = allInfos.get(i);
			if (list != null) {
				for (ItemInfo itemInfo : list) {
					if (itemInfo.mItemType == IItemType.ITEM_TYPE_APP_WIDGET) {
						ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) itemInfo;
						if (widgetInfo.mAppWidgetId == widgetid) {
							return i;
						}
					}
				}
			}
		}
		return -1;
	}

	static void resumeGoWidget(int screenIndex,
			HashMap<Integer, ArrayList<ItemInfo>> allInfos) {
		if (allInfos == null) {
			return;
		}

		ArrayList<ItemInfo> list = allInfos.get(screenIndex);
		final GoWidgetManager widgetManager = AppCore.getInstance()
				.getGoWidgetManager();
		if (list != null) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				ItemInfo info = list.get(i);
				if (info != null && info instanceof ScreenAppWidgetInfo) {
					ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) info;
					if (GoWidgetManager.isGoWidget(widgetInfo.mAppWidgetId)) {
						widgetManager.resumeWidget(widgetInfo.mAppWidgetId);
					}
				}
			}
		}
	}

	public static CharSequence getItemTitle(ItemInfo targetInfo) {
		if (targetInfo == null) {
			return null;
		}

		CharSequence title = null;
		if (targetInfo.mItemType == IItemType.ITEM_TYPE_APPLICATION
				|| targetInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
			title = ((ShortCutInfo) targetInfo).mTitle;
		} else if (targetInfo.mItemType == IItemType.ITEM_TYPE_LIVE_FOLDER
				|| targetInfo.mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
			title = ((ScreenFolderInfo) targetInfo).mTitle;
		}
		return title;
	}

	/**
	 * 根据{@link ItemInfo#mInScreenId} 获取在屏幕上对应的view
	 * 
	 * @param itemId
	 *            {@link ItemInfo#mInScreenId}
	 * @param screenIndex
	 *            屏幕索引，小于0遍历整个屏幕
	 * @param workspace
	 * @return
	 */
	static View getViewByItemId(long itemId, int screenIndex,
			Workspace workspace) {
		if (workspace == null) {
			return null;
		}
		if (screenIndex >= workspace.getChildCount()) {
			return null;
		}
		if (screenIndex < 0) {
			int screenCount = workspace.getChildCount();
			for (int screen = 0; screen < screenCount; screen++) {
				CellLayout currentScreen = (CellLayout) workspace
						.getChildAt(screen);
				if (currentScreen != null) {
					int count = currentScreen.getChildCount();
					for (int i = 0; i < count; i++) {
						View child = currentScreen.getChildAt(i);
						if (child != null) {
							Object tagObject = child.getTag();
							if (tagObject != null
									&& tagObject instanceof ItemInfo
									&& ((ItemInfo) tagObject).mInScreenId == itemId) {
								return child;
							}
						}
					}
				}
			}
		} else {
			CellLayout currentScreen = (CellLayout) workspace
					.getChildAt(screenIndex);
			if (currentScreen != null) {
				int count = currentScreen.getChildCount();
				for (int i = 0; i < count; i++) {
					View child = currentScreen.getChildAt(i);
					if (child != null) {
						Object tagObject = child.getTag();
						if (tagObject != null && tagObject instanceof ItemInfo
								&& ((ItemInfo) tagObject).mInScreenId == itemId) {
							return child;
						}
					}
				}
			}
		}
		return null;
	}

	static void removeViewByItemInfo(ItemInfo itemInfo, Workspace workspace) {
		View targetView = getViewByItemId(itemInfo.mInScreenId, -1, workspace);
		if (targetView != null) {
			ViewParent parent = targetView.getParent();
			if (parent != null && parent instanceof ViewGroup) {
				((ViewGroup) parent).removeView(targetView);
			}
		}
		itemInfo.selfDestruct();
	}

	static void setBubbleTextTitle(boolean showTitle, BubbleTextView view) {
		if (view != null) {
			if (showTitle) {
				Object tag = view.getTag();
				if (tag != null) {
					if (tag instanceof ShortCutInfo) {
						view.setText(((ShortCutInfo) tag).mTitle);
					} else if (tag instanceof ScreenLiveFolderInfo) {
						view.setText(((ScreenLiveFolderInfo) tag).mTitle);
					} else if (tag instanceof UserFolderInfo) {
						view.setText(((UserFolderInfo) tag).mTitle);
					}
				}
			} else {
				view.setText(null);
			}
		}
	}

	/**
	 * 获取一个在文件夹内的图标在桌面的位置
	 * 
	 * @param targetRect
	 * @param sequenceNum
	 *            　文件夹内的第几个图标，1-4算在文件夹内位置，5或以上算文件夹中间位置
	 * @param targetView
	 *            用于获取排版参数的类似的BubbleTextView
	 * @return　
	 */
	static Rect getAIconRectInAFolder(int sequenceNum, BubbleTextView targetView) {
		if (sequenceNum <= 0 || null == targetView /*
													 * || null ==
													 * targetView.getLayout()
													 */) {
			// bad params
			return null;
		}

		Rect targetRect = new Rect(targetView.getLeft(), targetView.getTop(),
				targetView.getRight(), targetView.getBottom());

		// view内整体图标位置数据
		// final int iconsize = DockConstant.getScreenIconSize();
		final int iconsize = Utilities.getStandardIconSize(targetView
				.getContext());
		final int folderIconDrawableLeft = (targetView.getWidth() - iconsize) / 2;
		final int folderIconDrawableTop = targetView.getPaddingTop();

		if (sequenceNum > FolderIcon.INNER_ICON_SIZE) {
			// 处理第5个以上图标
			int l = targetView.getLeft() + targetView.getWidth() / 2;
			int t = targetView.getTop() + folderIconDrawableTop + iconsize / 2;
			int r = l;
			int b = t;
			return new Rect(l, t, r, b);
		}
		// 整体图标内某个小icon位置数据
		Rect rect = new Rect();
		Resources resources = targetView.getResources();
		// int innerIconSize =
		// resources.getDimensionPixelSize(R.dimen.inner_desk_folder_icon_size);
		int col = (sequenceNum % 2 == 0) ? 1 : 0;
		int row = (sequenceNum > 2) ? 1 : 0;
		// final float innerLeft = iconsize *
		// FolderIcon.FOLDER_INNER_ICONS_PADDING + col
		// * (innerIconSize + iconsize *
		// FolderIcon.FOLDER_SCREEN_INNER_ICONS_SPACE_BETWEEN);
		// final float innerTop = iconsize *
		// FolderIcon.FOLDER_INNER_ICONS_PADDING + row
		// * (innerIconSize + iconsize *
		// FolderIcon.FOLDER_SCREEN_INNER_ICONS_SPACE_BETWEEN);

		final float first = iconsize * 0.12f;
		final float grap = iconsize * 0.015f;
		final int innerIconSize = (int) (iconsize - first * 2 - grap * 2) / 2;
		final float left = first + col * (innerIconSize + grap * 2);
		final float top = first + row * (innerIconSize + grap * 2);

		rect.left = targetRect.left + folderIconDrawableLeft + (int) left;
		rect.top = targetRect.top + folderIconDrawableTop + (int) top;
		rect.right = rect.left + innerIconSize;
		rect.bottom = rect.top + innerIconSize;

		// 总的位置数据
		// rect.left = targetRect.left + folderIconDrawableLeft +
		// (int)innerLeft;
		// rect.top = targetRect.top + folderIconDrawableTop + (int)innerTop;
		// rect.right = rect.left + innerIconSize;
		// rect.bottom = rect.top + innerIconSize;

		return rect;
	}

	public static Rect getZoomoutSrcRect(Rect dragRect, View dragView) {
		if (null == dragRect || null == dragView) {
			return null;
		}

		int iconsize = 0;
		Drawable drawable = null;
		int top = 0;
		if (dragView instanceof BubbleTextView) {
			BubbleTextView iconView = (BubbleTextView) dragView;
			iconsize = Utilities.getStandardIconSize(dragView.getContext());
			drawable = iconView.getIcon();
			top = iconView.getPaddingTop();
		} else if (dragView instanceof DockIconView) {
			AbsLineLayout lineLayout = (AbsLineLayout) dragView.getParent();
			iconsize = DockUtil.getIconSize(lineLayout.getChildCount());
			drawable = ((DockIconView) dragView).getDrawable();
			top = dragView.getPaddingTop();
		}

		if (null != drawable) {
			int left = (dragView.getWidth() - iconsize) / 2;
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();

			left *= DragFrame.sScaleFactor;
			top *= DragFrame.sScaleFactor;

			final int l = dragRect.left + left;
			final int t = dragRect.top + top;
			final int r = (int) (l + width * DragFrame.sScaleFactor);
			final int b = (int) (t + height * DragFrame.sScaleFactor);

			Rect rect = new Rect(l, t, r, b);
			return rect;
		}

		return null;
	}

	static float easeOut(float begin, float end, float t) {
		t = 1 - t;
		return begin + (end - begin) * (1 - t * t * t);
	}

	public static boolean isOccupied(int screenIndex, int cellX, int cellY,
			Workspace workspace) {
		if (screenIndex < 0 || screenIndex >= workspace.getChildCount()) {
			return true;
		}

		final CellLayout destScreen = (CellLayout) workspace
				.getChildAt(screenIndex);
		if (destScreen == null) {
			return true;
		} else {
			return destScreen.isOccupied(cellX, cellY);
		}
	}

	// 确定飞行动画图标左上角终点
	public static void cellToPoint(int screenIndex, int cellX, int cellY,
			Workspace workspace, int[] result) {
		if (result == null || screenIndex < 0
				|| screenIndex >= workspace.getChildCount()) {
			return;
		}

		final CellLayout destScreen = (CellLayout) workspace
				.getChildAt(screenIndex);
		if (destScreen == null) {
			return;
		} else {
			destScreen.cellToGridCenter(cellX, cellY, result);
		}
	}

	public static void cellToRealCenterPoint(int screenIndex, int cellX,
			int cellY, Workspace workspace, int[] result) {
		if (result == null || screenIndex < 0
				|| screenIndex >= workspace.getChildCount()) {
			return;
		}

		final CellLayout destScreen = (CellLayout) workspace
				.getChildAt(screenIndex);
		if (destScreen == null) {
			return;
		} else {
			destScreen.cellToRealCenterPoint(cellX, cellY, result);
		}
	}

	/** BEGIN　初始化应用程序列表 */
	/** ScreenFrame初始化了几个桌面初始化应用程序数组里的数据 */
	public static int sScreenInitedDefaultAppCount = 0;

	/**
	 * <br>
	 * 功能简述:桌面初始化应用程序数组 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public static String[] getDefaultInitAppPkg() {
		String[] packageName = null;
		if (Machine.isCnUser()) {
			String[] name = {
					"cn.jingling.motu.photowonder", // 魔图精灵
					"com.tencent.mm", // 微信
					"com.UCMobile", // UC
					"com.qihoo360.mobilesafe", // 360
					"com.sds.android.ttpod", // 天天动听
					"com.tencent.qq", PackageName.SINA_WEIBO,
					LauncherEnv.Market.PACKAGE, "com.uc.browser",
					"vStudio.Android.Camera360", "com.android.clock",
					"com.android.calendar", PackageName.GMAIL,
					"com.android.settings", "com.android.camera",
					"com.google.android.apps.maps", "com.android.gallery",
					"com.android.calculator", "com.android.music",
					PackageName.GOOGLE_TALK_ANDROID_TALK, "com.android.email",
					"com.android.contacts", "com.android.mms",
					"com.android.browser" };
			packageName = name;
		} else {
			String country = Locale.getDefault().getLanguage();
			if (country != null && !country.trim().equals("")
					&& country.toLowerCase().equals("ko")) {
				// 如果是语言是韩国的
				String[] name = { "com.kakao.talk", "com.nhn.android.search",
						"com.kth.PuddingCamera", "kr.co.tictocplus",
						"com.btb.minihompy", "com.brainpub.phonedecor",
						"ss.ga.jess", PackageName.GMAIL,
						PackageName.GOOGLE_TALK_ANDROID_TALK,
						LauncherEnv.Market.PACKAGE,
						"com.google.android.apps.maps", "com.android.clock",
						"com.android.calendar", "com.android.settings",
						"com.android.camera", "com.android.gallery",
						"com.android.calculator", "com.android.music",
						"com.android.email", "com.android.contacts",
						"com.android.mms", "com.android.browser" };
				packageName = name;
			} else {
				// 其他国家地区
				String[] name = {
						PackageName.FACEBOOK, // Facebook
						"com.devuni.flashlight", // Tiny Flashlight+LED
						"com.jb.gosms", // go sms
						"com.metago.astro", // Astro File Manager
						"com.whatsapp", // Whatsapp
						"com.skype.raider", // skype
						"com.google.android.apps.translate", // google翻译
						"com.ringdroid", // Ringdroid
						PackageName.TWITTER, // Twitter
						"com.rechild.advancedtaskkiller", // advanced task
															// killer
						PackageName.GMAIL,
						PackageName.GOOGLE_TALK_ANDROID_TALK,
						LauncherEnv.Market.PACKAGE,
						"com.google.android.apps.maps", "com.android.clock",
						"com.android.calendar", "com.android.settings",
						"com.android.camera", "com.android.gallery",
						"com.android.calculator", "com.android.music",
						"com.android.email", "com.android.contacts",
						"com.android.mms", "com.android.browser" };
				packageName = name;
			}
		}
		return packageName;
	}

	/** END　初始化应用程序列表 */

	/**
	 * 返回GO系列应用的包名数组
	 * 
	 * @return
	 */
	public static String[] getGoAppsPkgName() {
		String[] goPkgName = { LauncherEnv.GO_STORE_PACKAGE_NAME,
				LauncherEnv.GO_WIDGET_PACKAGE_NAME,
				LauncherEnv.GO_THEME_PACKAGE_NAME,
				LauncherEnv.Plugin.RECOMMAND_GOWEATHEREX_PACKAGE,
				LauncherEnv.Plugin.RECOMMAND_GOSMS_PACKAGE,
				LauncherEnv.Plugin.RECOMMAND_GOKEYBOARD_PACKAGE,
				LauncherEnv.Plugin.RECOMMAND_GOPOWERMASTER_PACKAGE,
				LauncherEnv.Plugin.RECOMMAND_GOTASKMANAGER_PACKAGE,
				LauncherEnv.Plugin.RECOMMAND_GOLOCKER_PACKAGE,
				LauncherEnv.Plugin.RECOMMAND_GOBACKUPEX_PACKAGE,
				LauncherEnv.Plugin.RECOMMAND_LOCKSCREEN_PACKAGE };
		return goPkgName;
	}

	/**
	 * 返回GO系列应用的程序名称的id数组
	 * 
	 * @return
	 */
	public static int[] getGoAppsNameIds() {
		int[] goAppsNameIds = { R.string.customname_gostore,
				R.string.func_gowidget_icon, R.string.go_theme,
				R.string.recommand_goweatherex, R.string.recommand_gosms,
				R.string.recommand_gokeyboard,
				R.string.recommand_gopowermaster,
				R.string.recommand_gotaskmanager, R.string.customname_golocker,
				R.string.recommand_gobackup, R.string.recommand_lockscreen };
		return goAppsNameIds;
	}

	/**
	 * 返回GO系列应用的图标id数组
	 * 
	 * @return
	 */
	public static int[] getGoAppsIconIds() {
		int[] goAppsIconIds = { R.drawable.store, R.drawable.gowidget,
				R.drawable.change_theme_4_def3, R.drawable.goweatherex_4_def3,
				R.drawable.gosmspro_4_def3, R.drawable.recommand_icon_keyboard,
				R.drawable.recommand_icon_gopowermaster,
				R.drawable.recommand_icon_gotaskmanager,
				R.drawable.screen_edit_golocker,
				R.drawable.recommand_icon_gobackup, R.drawable.lock_screen };
		return goAppsIconIds;
	}

	/**
	 * 返回GO系列应用的Action
	 * 
	 * @return
	 */
	public static String[] getGoAppsActions() {
		String[] goAppsActions = {
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE,
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET,
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME,
				ICustomAction.ACTION_RECOMMAND_GOWEATHEREX_DOWNLOAD,
				ICustomAction.ACTION_RECOMMAND_GOSMS_DOWNLOAD,
				ICustomAction.ACTION_RECOMMAND_GOKEYBOARD_DOWNLOAD,
				ICustomAction.ACTION_RECOMMAND_GOPOWERMASTER_DOWNLOAD,
				ICustomAction.ACTION_RECOMMAND_GOTASKMANAGER_DOWNLOAD,
				ICustomAction.ACTION_RECOMMAND_GOLOCKER_DOWNLOAD,
				ICustomAction.ACTION_RECOMMAND_GOBACKUP_DOWNLOAD,
				ICustomAction.ACTION_RECOMMAND_LOCKSCREEN_DOWNLOAD };
		return goAppsActions;
	}

	/**
	 * 返回GO系列应用的FTP地址 前四个分别是GOStore、GOWidget和桌面主题，不需要FTP地址
	 * 
	 * @return
	 */
	public static String[] getGoAppsFtpUrl() {
		String[] goAppsFtpUrls = { "", "", "",
				LauncherEnv.Url.GOWEATHEREX_FTP_URL,
				LauncherEnv.Url.GOSMSPRO_FTP_URL,
				LauncherEnv.Url.GOKEYBOARD_FTP_URL,
				LauncherEnv.Url.GOPOWERMASTER_FTP_URL,
				LauncherEnv.Url.GOTASKMANAGEREX_FTP_URL,
				LauncherEnv.Url.GOLOCKER_FTP_URL,
				LauncherEnv.Url.GOBACKUP_EX_FTP_URL,
				LauncherEnv.Url.LOCK_SCREEN };
		return goAppsFtpUrls;
	}

	public static void startFeedbackIntent(final Context context) {
		String bugString = context.getResources().getString(
				R.string.feedback_select_type_bug);
		String suggestionString = context.getResources().getString(
				R.string.feedback_select_type_suggestion);
		String questionString = context.getResources().getString(
				R.string.feedback_select_type_question);
		final CharSequence[] items = { bugString, suggestionString,
				questionString };

		String bugForMailString = context.getResources().getString(
				R.string.feedback_select_type_bug_for_mail);
		String suggestionForMailString = context.getResources().getString(
				R.string.feedback_select_type_suggestion_for_mail);
		String questionForMailString = context.getResources().getString(
				R.string.feedback_select_type_question_for_mail);
		final CharSequence[] itemsForMail = { bugForMailString,
				suggestionForMailString, questionForMailString };

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.feedback_select_type_title);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int index) {
				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				String[] receiver = new String[] { "golauncher@goforandroid.com" };
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						receiver);
				String subject = "GO Launcher EX(v"
						+ context.getString(R.string.curVersion)
						+ ") Feedback(" + itemsForMail[index] + ")";
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						subject);
//				String body = "\n\n";
				StringBuffer body = new StringBuffer("\n\n");
				body.append("\nProduct=" + android.os.Build.PRODUCT);
				body.append("\nPhoneModel=" + android.os.Build.MODEL);
				body.append("\nROM=" + android.os.Build.DISPLAY);
				body.append("\nBoard=" + android.os.Build.BOARD);
				body.append("\nDevice=" + android.os.Build.DEVICE);
				body.append("\nDensity="
						+ String.valueOf(context.getResources()
								.getDisplayMetrics().density));
				body.append("\nPackageName=" + context.getPackageName());
				body.append("\nAndroidVersion=" + android.os.Build.VERSION.RELEASE);
				body.append("\nTotalMemSize="
						+ (ErrorReporter.getTotalInternalMemorySize() / 1024 / 1024)
						+ "MB");
				body.append("\nFreeMemSize="
						+ (ErrorReporter.getAvailableInternalMemorySize() / 1024 / 1024)
						+ "MB");
				body.append("\nRom App Heap Size="
						+ Integer.toString((int) (Runtime.getRuntime()
								.maxMemory() / 1024L / 1024L)) + "MB");
				emailIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
				emailIntent.setType("plain/text");
				try {
					context.startActivity(emailIntent);
				} catch (Exception e) {
					// e.printStackTrace();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri
							.parse("http://golauncher.goforandroid.com"));
					intent.setClassName("com.android.browser",
							"com.android.browser.BrowserActivity");
					context.startActivity(intent);
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * 获取文件夹默认背景图
	 * 
	 * @return
	 */
	public static BitmapDrawable getFolderBackIcon() {
		DeskThemeControler themeControler = AppCore.getInstance()
				.getDeskThemeControler();
		FolderStyle folderStyle = null;
		// GO主题类型
		ImageExplorer imageExplorer = ImageExplorer.getInstance(GOLauncherApp
				.getContext());
		if (themeControler != null /* && themeControler.isUesdTheme() */) {
			DeskThemeBean themeBean = themeControler.getDeskThemeBean();
			if (themeBean != null && themeBean.mScreen != null) {
				folderStyle = themeBean.mScreen.mFolderStyle;
			}
		}
		if (folderStyle != null && folderStyle.mBackground != null) {
			Drawable icon = imageExplorer.getDrawable(folderStyle.mPackageName,
					folderStyle.mBackground.mResName);
			if (icon != null) {
				return (BitmapDrawable) icon;
			}
		}
		return (BitmapDrawable) GOLauncherApp.getContext().getResources()
				.getDrawable(R.drawable.folder_back);

	}

	/**
	 * 获取对应widget的所有样式
	 * 
	 * @param mActivity
	 * @param packageName
	 *            包名
	 * @return
	 */
	public static ArrayList<WidgetParseInfo> getWidgetStyle(Context mActivity,
			String packageName) {
		try {
			ArrayList<WidgetParseInfo> mWidgetDatasScan = new ArrayList<WidgetParseInfo>();

			int count = 0;
			Resources resources = mActivity.getPackageManager()
					.getResourcesForApplication(packageName);

			// 获取图片
			int drawableList = resources.getIdentifier(
					GoWidgetConstant.PREVIEW_LIST, "array", packageName);
			if (drawableList > 0) {
				final String[] extras = resources.getStringArray(drawableList);
				for (String extra : extras) {
					int res = resources.getIdentifier(extra, "drawable",
							packageName);
					if (res != 0) {
						WidgetParseInfo item = new WidgetParseInfo();
						item.resouceId = res;
						item.resouces = resources;
						item.themePackage = null;
						mWidgetDatasScan.add(item);
					}
				}
			}

			// 获取标题
			int titilList = resources.getIdentifier(
					GoWidgetConstant.STYLE_NAME_LIST, "array", packageName);
			if (titilList > 0) {
				final String[] titles = resources.getStringArray(titilList);
				count = 0;
				for (String titl : titles) {
					int res = resources.getIdentifier(titl, "string",
							packageName);
					if (res != 0) {
						WidgetParseInfo item = mWidgetDatasScan.get(count);
						item.title = resources.getString(res);
						count++;
					}
				}
			}

			// 获取类型
			int typeList = resources.getIdentifier(GoWidgetConstant.TYPE_LIST,
					"array", packageName);
			if (typeList > 0) {
				final int[] typeLists = resources.getIntArray(typeList);
				count = 0;
				for (int types : typeLists) {

					WidgetParseInfo item = mWidgetDatasScan.get(count);
					item.type = types;
					item.styleType = String.valueOf(types);
					count++;
				}
			}

			// 获取行数
			int rowList = resources.getIdentifier(GoWidgetConstant.ROW_LIST,
					"array", packageName);
			if (rowList > 0) {
				final int[] rowLists = resources.getIntArray(rowList);
				count = 0;
				for (int row : rowLists) {

					WidgetParseInfo item = mWidgetDatasScan.get(count);
					item.mRow = row;
					count++;
				}
			}

			// 获取列数
			int colList = resources.getIdentifier(GoWidgetConstant.COL_LIST,
					"array", packageName);
			if (colList > 0) {
				final int[] colListS = resources.getIntArray(colList);
				count = 0;
				for (int col : colListS) {

					WidgetParseInfo item = mWidgetDatasScan.get(count);
					item.mCol = col;
					count++;
				}
			}

			// 获取layout id
			int layoutIDList = resources.getIdentifier(
					GoWidgetConstant.LAYOUT_LIST, "array", packageName);
			if (layoutIDList > 0) {
				final String[] layouIds = resources
						.getStringArray(layoutIDList);
				count = 0;
				for (String id : layouIds) {
					WidgetParseInfo item = mWidgetDatasScan.get(count);
					item.layoutID = id;
					count++;
				}
			}

			// 获取竖屏最小宽度
			int minWidthVer = resources.getIdentifier(
					GoWidgetConstant.MIN_WIDTH, "array", packageName);
			if (minWidthVer > 0) {
				final int[] widthIds = resources.getIntArray(minWidthVer);
				count = 0;
				for (int w : widthIds) {
					WidgetParseInfo item = mWidgetDatasScan.get(count);
					item.minWidth = w;
					count++;
				}
			}

			// 获取竖屏最小高度
			int minHeightVer = resources.getIdentifier(
					GoWidgetConstant.MIN_HEIGHT, "array", packageName);
			if (minHeightVer > 0) {
				final int[] widthIds = resources.getIntArray(minHeightVer);
				count = 0;
				for (int h : widthIds) {
					WidgetParseInfo item = mWidgetDatasScan.get(count);
					item.minHeight = h;
					count++;
				}
			}

			// 获取layout id
			int configActivityList = resources.getIdentifier(
					GoWidgetConstant.CONFIG_LIST, "array", packageName);
			if (configActivityList > 0) {
				final String[] layouIds = resources
						.getStringArray(configActivityList);
				count = 0;
				for (String id : layouIds) {
					WidgetParseInfo item = mWidgetDatasScan.get(count);
					item.configActivty = id;
					count++;
				}
			}

			int longkeyconfigActivityList = resources.getIdentifier(
					GoWidgetConstant.SETTING_LIST, "array", packageName);
			if (longkeyconfigActivityList > 0) {
				final String[] layouIds = resources
						.getStringArray(longkeyconfigActivityList);
				count = 0;
				for (String id : layouIds) {
					WidgetParseInfo item = mWidgetDatasScan.get(count);
					item.longkeyConfigActivty = id;
					count++;
				}
			}
			return mWidgetDatasScan;
		} catch (Exception e) {
			Log.i("ScreenUtils", "getWidgetStyle() has exception = " + e.getMessage());
			return null;
		}
	}

	/**
	 * 获取对应widget的信息
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static GoWidgetProviderInfo getWidgetProviderInfo(Context context,
			String packageName) {
		GoWidgetProviderInfo mGoWidgetProviderInfo = null;

		GoWidgetFinder mFinder = new GoWidgetFinder(context);
		mFinder.scanAllInstalledGoWidget();
		HashMap<String, GoWidgetProviderInfo> mProviderMap = mFinder
				.getGoWidgetInfosMap();
		Set<Entry<String, GoWidgetProviderInfo>> entryset = mProviderMap
				.entrySet();
		for (Entry<String, GoWidgetProviderInfo> entry : entryset) {
			String pkgName = entry.getValue().mProvider.provider
					.getPackageName();
			if (pkgName.equals(packageName)) {
				mGoWidgetProviderInfo = entry.getValue();
				break;
			}
		}
		return mGoWidgetProviderInfo;
	}

	/**
	 * 获取go天气调整大小后匹配的样式
	 * 
	 * @param widgetStyleList
	 * @param widgetProviderInfo
	 * @param rect
	 * @param curWidgetCols
	 * @param curWidgetRows
	 * @param curWidgetInfo
	 * @return
	 */
	public static WidgetParseInfo getWeatherWidgetStyle(
			ArrayList<WidgetParseInfo> widgetStyleList,
			GoWidgetProviderInfo widgetProviderInfo, Rect rect,
			int curWidgetCols, int curWidgetRows, GoWidgetBaseInfo curWidgetInfo) {
		// 获取包命对应widget的样式列表
		if (widgetStyleList == null || widgetProviderInfo == null
				|| rect == null || curWidgetCols <= 0 || curWidgetRows <= 0
				|| curWidgetInfo == null) {
			return null;
		}

		int currentIndex = -1;
		int newRows = rect.height(); // 新区域行数
		int newCols = rect.width(); // 新区域列数

		String curWidgetLayout = curWidgetInfo.mLayout; // 当前widget布局

//		Log.i("lch", "当前widget:" + curWidgetCols + "x" + curWidgetRows);
//		Log.i("lch", "拖动区域行数:" + newCols + "x" + newRows);
//		Log.i("lch", "rect:" + rect.toString());

		// 判断新区域行数和列数是否一直
		if (curWidgetCols == newCols && curWidgetRows == newRows) {
//			Log.i("lch", "区域和当前widget大小一样");
			return null;
		}

		// 遍历已有样式，看是否有符合当前行X列的样式
		int widgetStyleSize = widgetStyleList.size();
		for (int i = 0; i < widgetStyleSize; i++) {
			WidgetParseInfo widgetParseInfo = widgetStyleList.get(i);
			if (newRows == widgetParseInfo.mRow
					&& newCols == widgetParseInfo.mCol) {
				// 判断当前widget布局文件是否和匹配出来的布局文件一样，一样就退出
				// 例如3x1 变成 4x1
				if (curWidgetLayout.equals(widgetParseInfo.layoutID)) {
//					Log.i("lch", "布局一样，不需要更换11：");
//					Log.i("lch", curWidgetCols + "x" + curWidgetRows + " --> "
//							+ newCols + "x" + newRows);
					return null;
				}

				currentIndex = i;
//				Log.i("lch", "存在匹配样式11：currentIndex:" + currentIndex
//						+ "   style: " + newCols + "x" + newRows);
				break;
			}
		}

		// 没有匹配到合适的样式，重新循环遍历。处理特殊情况
		if (currentIndex == -1) {
			for (int i = 0; i < widgetStyleSize; i++) {
				WidgetParseInfo widgetParseInfo = widgetStyleList.get(i);

				// 处理由1行直接拖动大于2行的，直接获取4x2的样式
				if (newRows > 2 && widgetParseInfo.mRow == 2
						&& widgetParseInfo.mCol == 4) {
					// 判断当前widget样式是否已经是4x2
					if (curWidgetLayout.equals(widgetParseInfo.layoutID)) {
//						Log.i("lch", "当前widget样式是否已经是4x2！");
						return null;
					}
					currentIndex = i;
//					Log.i("lch", "存在匹配样式：currentIndex:" + currentIndex);
					break;
				}

				// 拖动区域是1行或者2行，就取 列数=4 和 行数=新行数 的样式
				else if (newRows <= 2 && widgetParseInfo.mRow == newRows
						&& widgetParseInfo.mCol == 4) {
					// 判断当前样式是否一样，是否同一个局部，例如6x1（4x1） 变 3x1
					if (curWidgetLayout.equals(widgetParseInfo.layoutID)) {
//						Log.i("lch", "布局一样，不需要更换22：");
//						Log.i("lch", curWidgetCols + "x" + curWidgetRows
//								+ " --> " + newCols + "x" + newRows);
						return null;
					}
					currentIndex = i;
//					Log.i("lch", "存在匹配样式22：currentIndex:" + currentIndex
//							+ "   style: " + widgetParseInfo.mCol + "x"
//							+ widgetParseInfo.mRow);
					break;
				}
			}
		}

		if (currentIndex == -1) {
			return null;
		} else {
			return widgetStyleList.get(currentIndex);
		}
	}

	/**
	 * 获取go任务管理器调整大小后匹配的样式
	 * 
	 * @param widgetStyleList
	 * @param widgetProviderInfo
	 * @param rect
	 * @param curWidgetCols
	 * @param curWidgetRows
	 * @param curWidgetInfo
	 * @return
	 */
	public static WidgetParseInfo getTaskWidgetStyle(
			ArrayList<WidgetParseInfo> widgetStyleList,
			GoWidgetProviderInfo widgetProviderInfo, Rect rect,
			int curWidgetCols, int curWidgetRows, GoWidgetBaseInfo curWidgetInfo) {
		// 获取包命对应widget的样式列表
		if (widgetStyleList == null || widgetProviderInfo == null
				|| rect == null || curWidgetCols <= 0 || curWidgetRows <= 0
				|| curWidgetInfo == null) {
			return null;
		}

		String curWidgetLayout = curWidgetInfo.mLayout; // 当前widget布局
		int curWidgetType = curWidgetInfo.mType; // 当前样式类型

		int curType = -1;
		int type4X1 = 1; // 和widget配置文件对应的值一致
		int type4X2 = 2;

		// 判断当前类型是否指定的4x1或者4x2
		if (curWidgetType == type4X1) {
			curType = type4X1;
		}

		else if (curWidgetType == type4X2) {
			curType = type4X2;
		}

		if (!(curType == type4X1 || curType == type4X2)) {
			return null;
		}

		int currentIndex = -1;
		int newRows = rect.height(); // 新区域行数
		int newCols = rect.width(); // 新区域列数

		// 判断新区域行数和列数是否一直
		if (curWidgetCols == newCols && curWidgetRows == newRows) {
			return null;
		}

		// 遍历已有样式，看是否有符合当前行X列的样式
		int widgetStyleSize = widgetStyleList.size();
		for (int i = 0; i < widgetStyleSize; i++) {
			WidgetParseInfo widgetParseInfo = widgetStyleList.get(i);
			// 判断类型是否4x1 和4x2
			if (widgetParseInfo.type == type4X1
					|| widgetParseInfo.type == type4X2) {
				if (newRows == widgetParseInfo.mRow
						&& newCols == widgetParseInfo.mCol) {
					// 判断当前widget布局文件是否和匹配出来的布局文件一样，一样就退出
					// 例如3x1 变成 4x1
					if (curWidgetLayout.equals(widgetParseInfo.layoutID)) {
						return null;
					}

					currentIndex = i;
					break;
				}
			}
		}

		// 没有匹配到合适的样式，重新循环遍历。处理特殊情况
		if (currentIndex == -1) {
			for (int i = 0; i < widgetStyleSize; i++) {
				WidgetParseInfo widgetParseInfo = widgetStyleList.get(i);

				// 判断类型是否4x1 和4x2
				if (widgetParseInfo.type == type4X1
						|| widgetParseInfo.type == type4X2) {

					// 处理由1行直接拖动大于2行的，直接获取4x2的样式
					if (newRows > 2 && widgetParseInfo.mRow == 2
							&& widgetParseInfo.mCol == 4) {
						// 判断当前widget样式是否已经是4x2
						if (curWidgetLayout.equals(widgetParseInfo.layoutID)) {
							return null;
						}
						currentIndex = i;
						break;
					}

					// 拖动区域是1行或者2行，就取 列数=4 和 行数=新行数 的样式
					else if (newRows <= 2 && widgetParseInfo.mRow == newRows
							&& widgetParseInfo.mCol == 4) {
						// 判断当前样式是否一样，是否同一个局部，例如6x1（4x1） 变 3x1
						if (curWidgetLayout.equals(widgetParseInfo.layoutID)) {
							return null;
						}
						currentIndex = i;
						break;
					}
				}
			}
		}

		if (currentIndex == -1) {
			return null;
		} else {
			return widgetStyleList.get(currentIndex);
		}
	}

}
