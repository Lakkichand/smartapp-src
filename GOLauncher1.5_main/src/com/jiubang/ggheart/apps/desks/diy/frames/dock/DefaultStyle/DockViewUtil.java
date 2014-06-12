package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle;

import java.util.ArrayList;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewParent;

import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderIcon;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>类描述:dockview内部工具类
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-9-20]
 */
public class DockViewUtil {
	/**
	 * 判断当前是否发光
	 */
	public void judgeShowCurrentIconLight(DockIconView view) {
		if (view == null) {
			return;
		}
		try {
			ItemInfo info = view.getInfo().mItemInfo;
			if (info.mItemType != IItemType.ITEM_TYPE_APPLICATION
					|| info.mItemType != IItemType.ITEM_TYPE_SHORTCUT) {
				view.setmIsBgShow(true);
			} else if ((((ShortCutInfo) info).mIntent == null)
					|| (!ICustomAction.ACTION_BLANK.equals(((ShortCutInfo) info).mIntent
							.getAction()))) {
				view.setmIsBgShow(true);
			}
		} catch (Exception e) {

		}
	}

	public static float easeOut(float begin, float end, float t) {
		t = 1 - t;
		return begin + (end - begin) * (1 - t * t * t);
	}

	/**
	 * 获取一个在文件夹内的图标在桌面的位置
	 * 
	 * @param targetRect
	 * @param sequenceNum
	 *            　文件夹内的第几个图标，1-4算在文件夹内位置，5或以上算文件夹中间位置
	 * @param targetView
	 *            用于获取排版参数的类似的dockIconView
	 * @return　
	 */
	public static Rect getAIconRectInAFolder(int sequenceNum, DockIconView targetView) {
		if (sequenceNum <= 0 || null == targetView) {
			// bad params
			return null;
		}

		// 桌面图标与dock大小不一致，在folderIcon算图标摆放位置时，是以桌面图标大小来计算的
		AbsLineLayout lineLayout = (AbsLineLayout) targetView.getParent();
		final int iconDockSize = DockUtil.getIconSize(lineLayout.getChildCount());

		// 计算上LineLayout在dockView里的排版参数
		int parentLeft = 0;
		int parentTop = 0;
		ViewParent parent = targetView.getParent();
		if (null != parent && parent instanceof View) {
			View parentView = (View) parent;
			parentLeft += parentView.getLeft();
			parentTop += parentView.getTop();
		}

		// 处理第>=5以上图标
		if (sequenceNum > FolderIcon.INNER_ICON_SIZE) {
			int l = parentLeft + targetView.getLeft() + targetView.getWidth() / 2;
			int t = parentTop + targetView.getTop() + targetView.getHeight() / 2;
			int r = l;
			int b = t;
			return new Rect(l, t, r, b);
		}

		Rect rect = new Rect();
		final int col = (sequenceNum % 2 == 0) ? 1 : 0;
		final int row = (sequenceNum > 2) ? 1 : 0;

		final float first = iconDockSize * 0.12f;
		final float grap = iconDockSize * 0.015f;
		final int innerIconSize = (int) (iconDockSize - first * 2 - grap * 2) / 2;
		final float left = first + col * (innerIconSize + grap * 2);
		final float top = first + row * (innerIconSize + grap * 2);

		rect.left = parentLeft + targetView.getLeft() + targetView.getPaddingLeft() + (int) left;
		rect.top = parentTop + targetView.getTop() + targetView.getPaddingTop() + (int) top;
		rect.right = rect.left + innerIconSize;
		rect.bottom = rect.top + innerIconSize;

		return rect;
	}

	/**
	 * 获取15个初始化系统程序，可能找到不足15个
	 * 外部用完负责释放ArrayList<DockItemInfo>
	 * 
	 * @param engine
	 * @return
	 */
	public static ArrayList<DockItemInfo> getInitDockData() {
		ArrayList<DockItemInfo> list = new ArrayList<DockItemInfo>();

		try {
			final String[] packageName = ScreenUtils.getDefaultInitAppPkg();
			final AppDataEngine dataEngine = GOLauncherApp.getAppDataEngine();
			ArrayList<AppItemInfo> dbItemInfos = dataEngine.getAllAppItemInfos();

			int findItemCount = 0;
			final int pkgNamesSize = packageName.length;
			final int daItemSize = dbItemInfos.size();
			for (int i = 0; i < pkgNamesSize; i++) {
				for (int j = 0; j < daItemSize; j++) {
					AppItemInfo dbItemInfo = dbItemInfos.get(j);
					if (null == dbItemInfo.mIntent.getComponent()) {
						continue;
					}
					String dbPackageName = dbItemInfo.mIntent.getComponent().getPackageName();
					if (packageName[i].equals(dbPackageName)) {
						// 4是与screendatamodel推荐app不重复
						if (findItemCount >= ScreenUtils.sScreenInitedDefaultAppCount) {
							DockItemInfo dockItemInfo = new DockItemInfo(
									IItemType.ITEM_TYPE_APPLICATION, DockUtil.ICON_COUNT_IN_A_ROW);
							ShortCutInfo shortCutInfo = (ShortCutInfo) dockItemInfo.mItemInfo;
							shortCutInfo.mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
							shortCutInfo.mFeatureTitle = dbItemInfo.getTitle();
							shortCutInfo.mIntent = dbItemInfo.mIntent;

							list.add(dockItemInfo);
						}
						findItemCount++;
						break;
					}
				}
				if (list.size() >= DockUtil.DOCK_COUNT) {
					break;
				}
			}
		} catch (Exception e) {
			// 不处理
		}

		return list;
	}
}
