package com.jiubang.ggheart.apps.desks.diy.frames.drag;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.animation.MyAnimationUtils;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.FadePainter;
import com.go.util.window.WindowControl;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.CellLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Workspace;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.TrashLayer;

/**
 * 屏幕层拖动事件处理类
 * 
 * @author yuankai
 * @version 1.0
 */
public class ScreenDragHandler extends AbstractDragHandler implements AnimationListener {
	// 当前状态
	private final static int STATUS_NORMAL = 0; // 一般状态，表示简单的移动位置
	private final static int STATUS_DEL = 1; // 删除状态
	private final static int STATUS_UNINSTALL = 2; // 卸载状态
	private final static int STATUS_MOVE2DOCK = 3; // 拖动到DOCK
	private final static int STATUS_FLING_TO_DEL = 4; // 甩动删除
	private int mStatus = STATUS_NORMAL;
	// 垃圾箱
	// 消息ID
	// 时间
	// 是否处于计时状态
	private final static int HANDLE_OVER_TRASH_EADGE = 1;
	private final static long TRASH_WAIT_DURATION = 1500;
	private boolean mIsTrashCounting = false;
	// 垃圾箱属性
	private ImageView mTrashIcon;
	private Drawable mTrashBgNormal;
	private Drawable mTrashBgHover;
	private Drawable mTrashCanNormal;
	private Drawable mTrashCanFocus;
	private int mIconFileterColor;
	// 垃圾箱
	protected RelativeLayout mTrashAreaLayout;

	private int mEdgeColor = 0xA112ff00;
	private FadeView mEdgeFadeView;

	// 文件夹操作
	//	private Rect mLastRect;
	private List<Rect> mFolderAreaList = new ArrayList<Rect>();

	// 桌面挤压图标
	private boolean mLastInDock = false; // 上一个停留网格是否在dock内，用于判断是否要通知桌面复位各childView位置

	private final static int ANIMATION_NONE = 0;
	private final static int ANIMATION_REMOVE = 1;
	private int mAnimationType;
	private int[] mDockPostion = null;

	private boolean mFolderReplace = false;

	public void setFolderReplace(boolean folderReplace) {
		this.mFolderReplace = folderReplace;
	}

	private Point mCenterPoint = new Point(-1, -1);

	// 拖拽图标时所在的区域标识
	public static final int DRAG_IN_FOLDER = 0;
	public static final int DRAG_IN_WORKSPACE = 1;
	public static final int DRAG_IN_DOCK = 2;

	protected ScreenDragHandler(int type, DragFrame dragFrame, IFrameManager frameManager,
			View draggedView) {
		super(type, dragFrame, frameManager, draggedView);

		mDragListener = IDiyFrameIds.SCREEN_FRAME;

		mTrashAreaLayout = (RelativeLayout) (mFrame.getContentView()
				.findViewById(R.id.trash_area_stub));
		if (null != mTrashAreaLayout) {
			mTrashIcon = (ImageView) mTrashAreaLayout.findViewById(R.id.trash_can);
		}

		initTrash();

		// 文件夹层不显示垃圾箱
		if (dragFrame.getDragType() == DragFrame.TYPE_SCREEN_FOLDER_DRAG
				|| dragFrame.getDragType() == DragFrame.TYPE_DOCK_FOLDERITEM_DRAG) {
			mFolderReplace = true;
		}
	}

	@Override
	public void selfDestruct() {
		super.selfDestruct();

		mTrashIcon = null;
		mTrashBgNormal = null;
		mTrashBgHover = null;
		mTrashCanNormal = null;
		mTrashCanFocus = null;

		mTrashAreaLayout = null;

		//		mLastRect = null;
		mFolderAreaList.clear();
		mFolderAreaList = null;

		mHandler.removeMessages(HANDLE_OVER_TRASH_EADGE);
		mHandler = null;
	}

	private void initTrash() {
		// 加载主题
		mIconFileterColor = mFrame.getActivity().getResources()
				.getColor(R.color.delete_color_filter);
		DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
		if (themeControler != null && themeControler.isUesdTheme()) {
			DeskThemeBean themeBean = themeControler.getDeskThemeBean();
			if (themeBean != null && themeBean.mScreen != null
					&& themeBean.mScreen.mTrashStyle != null) {
				if (themeBean.mScreen.mTrashStyle.mIconForeColor != 0) {
					mIconFileterColor = themeBean.mScreen.mTrashStyle.mIconForeColor;
				}
				final TrashLayer normalLayer = themeBean.mScreen.mTrashStyle.mTrashingLayer;
				if (normalLayer != null) {
					mTrashCanNormal = themeControler.getDrawable(normalLayer.mResImage,
							R.drawable.del);
					// mTrashCanNormal =
					// DrawUtils.zoomDrawable(themeControler.getDrawable(
					// normalLayer.mResImage, R.drawable.trash_can), 0.5f, 0.5f,
					// GoLauncher.getContext().getResources());
					if (normalLayer.mBackImage != null) {
						boolean bool = false;
						while (!bool) {
							try {
								mTrashBgNormal = themeControler
										.getDrawable(normalLayer.mBackImage.mResName,
												R.drawable.trash_bg_normal);
								// mTrashBgNormal =
								// DrawUtils.zoomDrawable(themeControler.getDrawable(
								// normalLayer.mBackImage.mResName,
								// R.drawable.trash_bg_normal), 0.5f, 0.5f,
								// GoLauncher.getContext().getResources());
								bool = true;
							} catch (OutOfMemoryError e) {
								OutOfMemoryHandler.handle();
							}
						}
					}
				}

				final TrashLayer hightLightLayer = themeBean.mScreen.mTrashStyle.mTrashedLayer;
				if (hightLightLayer != null) {
					boolean bool = false;
					while (!bool) {
						// 若因内存溢出取资源图片不成功，一直循环取
						try {
							mTrashCanFocus = themeControler.getDrawable(hightLightLayer.mResImage,
									R.drawable.del_open);
							// mTrashCanFocus =
							// DrawUtils.zoomDrawable(themeControler.getDrawable(
							// hightLightLayer.mResImage,
							// R.drawable.trash_can), 0.5f, 0.5f,
							// GoLauncher.getContext().getResources());
							if (hightLightLayer.mBackImage != null) {
								mTrashBgHover = themeControler.getDrawable(
										hightLightLayer.mBackImage.mResName,
										R.drawable.trash_bg_hover);
								// mTrashBgHover =
								// DrawUtils.zoomDrawable(themeControler.getDrawable(
								// hightLightLayer.mBackImage.mResName,
								// R.drawable.trash_bg_hover), 0.5f, 0.5f,
								// GoLauncher.getContext().getResources());
							}
							bool = true;
						} catch (OutOfMemoryError e) {
							OutOfMemoryHandler.handle();
						}
					}
				}
			}
		}

		if (mTrashBgNormal == null) {
			while (mTrashBgNormal == null) {
				// 若因内存溢出取资源图片不成功，一直循环取
				try {
					mTrashBgNormal = mFrame.getActivity().getResources()
							.getDrawable(R.drawable.trash_bg_normal);
					// mTrashBgNormal =
					// DrawUtils.zoomDrawable(mFrame.getActivity().getResources().getDrawable(R.drawable.trash_bg_normal),
					// 0.5f, 0.5f, GoLauncher.getContext().getResources());
				} catch (OutOfMemoryError e) {
					OutOfMemoryHandler.handle();
					mTrashBgNormal = null;
				}
			}
		}

		if (mTrashBgHover == null) {
			while (mTrashBgHover == null) {
				// 若因内存溢出取资源图片不成功，一直循环取
				try {
					mTrashBgHover = mFrame.getActivity().getResources()
							.getDrawable(R.drawable.trash_bg_hover);
					// mTrashBgHover =
					// DrawUtils.zoomDrawable(mFrame.getActivity().getResources().getDrawable(R.drawable.trash_bg_hover),
					// 0.5f, 0.5f, GoLauncher.getContext().getResources());
				} catch (OutOfMemoryError e) {
					OutOfMemoryHandler.handle();
					mTrashBgHover = null;
				}
			}
		}

		if (mTrashCanNormal == null) {
			while (mTrashCanNormal == null) {
				// 若因内存溢出取资源图片不成功，一直循环取
				try {
					mTrashCanNormal = mFrame.getActivity().getResources()
							.getDrawable(R.drawable.del);
					// mTrashCanNormal =
					// DrawUtils.zoomDrawable(mFrame.getActivity().getResources().getDrawable(R.drawable.trash_can),
					// 0.5f, 0.5f, GoLauncher.getContext().getResources());
				} catch (OutOfMemoryError e) {
					OutOfMemoryHandler.handle();
					mTrashCanNormal = null;
				}
			}
		}

		if (mTrashCanFocus == null) {
			while (mTrashCanFocus == null) {
				// 若因内存溢出取资源图片不成功，一直循环取
				try {
					mTrashCanFocus = mFrame.getActivity().getResources()
							.getDrawable(R.drawable.del_open);
					// mTrashCanFocus =
					// DrawUtils.zoomDrawable(mFrame.getActivity().getResources().getDrawable(R.drawable.trash_can),
					// 0.5f, 0.5f, GoLauncher.getContext().getResources());
				} catch (OutOfMemoryError e) {
					OutOfMemoryHandler.handle();
					mTrashCanFocus = null;
				}
			}
		}

		if (mTrashIcon != null) {
			mTrashIcon.setImageDrawable(mTrashCanNormal);
			mTrashIcon.setBackgroundDrawable(mTrashBgNormal);
		}

		startAnimation();
	}

	private void startAnimation() {
		if (mTrashAreaLayout == null) {
			return;
		}
		Animation animation = MyAnimationUtils.getPopupAnimation(
				MyAnimationUtils.POP_FROM_LONG_START_SHOW, -1);
		if (animation != null) {
			mTrashAreaLayout.startAnimation(animation);
		}
	}

	@Override
	public void focusTrash(IDragObject obj) {
		// 设置垃圾箱背景为警告
		if (null != mTrashIcon) {
			mTrashIcon.setBackgroundDrawable(mTrashBgHover);
			// 针对某些主题垃圾箱open图片检测不到 add by xiangliang
			if (mTrashCanFocus.getIntrinsicHeight() < mTrashCanNormal
					.getIntrinsicHeight()) {
				mTrashIcon.setImageDrawable(mTrashCanNormal);
			} else {
				mTrashIcon.setImageDrawable(mTrashCanFocus);
			}
		}

		if (null != obj) {
			obj.setColor(mIconFileterColor, PorterDuff.Mode.SRC_ATOP);
		}
	}

	// 屏幕震动
	private void vibrate() {
		if (mDraggedView != null) {
			mDraggedView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
					HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
		}
	}

	private void unFocusTrash(IDragObject obj) {
		if (null != mTrashIcon) {
			mTrashIcon.setBackgroundDrawable(mTrashBgNormal);
			mTrashIcon.setImageDrawable(mTrashCanNormal);
		}
		if (null != obj) {
			obj.setColor(0, PorterDuff.Mode.SRC_ATOP);
		}
	}

	private boolean isOverEdgeOfTrash(int centerX, int centerY) {
		// 还没有初始化完成
		if (null == mTrashIcon || (0 == mTrashIcon.getWidth() && 0 == mTrashIcon.getHeight())) {
			return false;
		}

		int statusBarHeight = 0;
		if (!WindowControl.getIsFullScreen(mFrame.getActivity())) {
			statusBarHeight += StatusBarHandler.getStatusbarHeight();
		}
		Rect trashRc = new Rect(mTrashIcon.getLeft(), mTrashIcon.getTop() + statusBarHeight,
				mTrashIcon.getRight(), mTrashIcon.getBottom() + statusBarHeight);

		// return centerX < trashRc.right && centerY < trashRc.bottom;
		// 由于横屏情况下，垃圾箱也在上方出现，所以不区分横竖屏
		if (CellLayout.sPortrait) {
			// *0.7是因为垃圾箱底部与图标有重叠，视觉上重叠，但拖动操作位置上为了不重叠，所以*0.7
			return centerY < trashRc.bottom * 0.7;
		} else {
			final int rightPadding = ShortCutSettingInfo.sEnable ? DockUtil.getBgHeight() : 0;
			return centerY < trashRc.bottom * 0.7
					&& centerX < DrawUtils.sWidthPixels - rightPadding;
		}
	}

	private boolean isOverEdgeOfDock(int centerX, int centerY) {
		if (mCenterPoint.x == -1 || mCenterPoint.y == -1) {
			if (!(mDraggedView instanceof DockIconView))// 修复桌面长按最靠近dock一排图标立即松手后出现与dock条交换问题
			{
				return false;
			}
		}
		mDockPostion = null;
		ArrayList<Object> indexArray = new ArrayList<Object>();
		Object tag = mDraggedView.getTag();
		indexArray.add(mDraggedView);
		if ((tag instanceof ShortCutInfo || tag instanceof UserFolderInfo)
				&& Workspace.getLayoutScale() == 1.0f) {
			return GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
					IDiyMsgIds.DOCK_CHECK_POSITION, mFrame.getDragType(), new Point(centerX,
							centerY), indexArray);
		} else {
			return false;
		}
	}

	private void endDrag(IDragObject obj) {
		boolean isDragSuccess = true;
		// 相应命令处理
		switch (mStatus) {
			case STATUS_NORMAL :
				if (!mFolderReplace) {
					obj.setVisable(false);
					isDragSuccess = handleEndDragWithNormal(obj);
				}
				break;

			case STATUS_DEL :
			case STATUS_FLING_TO_DEL :
				handleEndDragWithDel();
				break;

			case STATUS_UNINSTALL :
				handleEndDragWithUninstall();
				break;

			case STATUS_MOVE2DOCK :
				isDragSuccess = handleEndDragWithDock(obj);
				break;

			default :
				handleEndDragWithNormal(obj);
				break;
		}

		doAfterEndDrag(isDragSuccess);
	}

	/**
	 * 放手前的操作控制
	 * 
	 * @param obj
	 */
	private void doBeforeDragFinish(IDragObject obj) {
		if (null == obj && null != mFrame && null != mCenterPoint) {
			return;
		}

		switch (mFrame.getDragType()) {
			case DragFrame.TYPE_SCREEN_FOLDER_DRAG :
			case DragFrame.TYPE_DOCK_FOLDERITEM_DRAG :
				// 目的：解决场景－－桌面、dock文件夹打开后，快速拖到文件夹内图标出文件夹（时间片没到可以触发文件夹关闭）的情况，放手坐标要与文件夹打开后的桌面计算位置偏移（桌面此时为截图，坐标不是正常状态的坐标）
				int[] folderLayoutData = new int[] { -1, -1, -1 };
				// TODO:向文件夹层取数据,定义规则:folderLayoutData[0]为文件夹层的mFolderTop,folderLayoutData[1]为文件夹层的mFolderBottom,folderLayoutData[2]为文件夹层的mClipLine
				GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
						IDiyMsgIds.GET_FOLDER_LAYOUT_DATA, -1, folderLayoutData, null);
				if (folderLayoutData[0] > 0 && folderLayoutData[1] > 0 && folderLayoutData[2] > 0) {
					int dragCenterY = mCenterPoint.y;
					// 判断已经拖出了文件夹层dragover响应范围
					if (dragCenterY < folderLayoutData[0]
							&& folderLayoutData[0] <= folderLayoutData[2]) {
						int reduce = folderLayoutData[2] - folderLayoutData[0];
						int l = obj.getLeft();
						int t = obj.getTop() + reduce;
						int r = obj.getRight();
						int b = t + obj.getHeight();
						obj.layout(l, t, r, b);
						mCenterPoint.y = (t + b) / 2;
					} else if (dragCenterY > folderLayoutData[1]
							&& folderLayoutData[1] >= folderLayoutData[2]) {
						int add = folderLayoutData[1] - folderLayoutData[2];
						int l = obj.getLeft();
						int b = obj.getBottom() - add;
						int t = b - obj.getHeight();
						int r = obj.getRight();
						obj.layout(l, t, r, b);
						mCenterPoint.y = (t + b) / 2;
					}
				}
				break;

			default :
				break;
		}

	}

	private void doAfterEndDrag(boolean isDragSuccess) {
		if (isDragSuccess) {
			switch (mStatus) {
				case STATUS_MOVE2DOCK : {
					switch (mFrame.getDragType()) {
						case DragFrame.TYPE_SCREEN_ITEM_DRAG :
							if (mDraggedView.getTag() instanceof ShortCutInfo) {
								GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
										IDiyMsgIds.MOVE_SCREEN_SHORTCUT_TO_DOCK, -1, mDraggedView,
										null); // 通知dock的shortcutinfo重新注册appiteminfo
							} else {
								GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
										IDiyMsgIds.MOVE_SCREEN_FOLDER_TO_DOCK, -1, mDraggedView,
										null); // 删除屏幕上原来图标
							}
							break;

						case DragFrame.TYPE_SCREEN_FOLDER_DRAG :
							GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
									IDiyMsgIds.DELETE_CACHE_INFO_IN_FOLDER, -1,
									mDraggedView.getTag(), null);
							GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
									IDiyMsgIds.SCREEN_REMOVE_FOLDER_ITEM, -1, mDraggedView, null); // 删除桌面上原来图标
							break;

						default :
							break;
					}
				}
					break;

				case STATUS_NORMAL : {
					switch (mFrame.getDragType()) {
						case DragFrame.TYPE_DOCK_DRAG :
							GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
									IDiyMsgIds.DELETE_DOCK_ITEM, -1, mDragItemId, null); // 删除屏幕上原来图标
							break;

						case DragFrame.TYPE_DOCK_FOLDERITEM_DRAG :
							if (!mFolderReplace) {
								// 把旧inscreenid传过去，是因为加到screen时，iteminfo的inscreenid已经改变，必须用旧id才可以删除之前dock中的数据
								ArrayList<Long> list = new ArrayList<Long>();
								list.add(mDragItemId);
								GoLauncher.sendMessage(mFrame, IDiyFrameIds.DOCK_FRAME,
										IDiyMsgIds.DOCK_DELETE_FOLDERITEM, -1, mDraggedView, list);
								list.clear();
								list = null;
							}
						default :
							break;
					}
				}
					break;

				default :
					break;
			}
		}
	}

	private boolean handleEndDragWithNormal(IDragObject obj) {
		mDockPostion = null;
		// 通知给监听者移动完成
		Rect rect = new Rect(obj.getLeft() + mFrame.getExtraDragX(), obj.getTop(), obj.getRight() + mFrame.getExtraDragX(), obj.getBottom());
		List<Rect> list = new ArrayList<Rect>();
		list.add(rect);
		return GoLauncher.sendMessage(mFrame, mDragListener, IDiyMsgIds.DRAG_OVER, -1,
				mDraggedView, list);
	}

	private boolean handleEndDragWithDock(IDragObject obj) {
		// 通知给监听者移动完成
		List<Object> list = new ArrayList<Object>();
		Rect rect = new Rect(obj.getLeft(), obj.getTop(), obj.getRight(), obj.getBottom());
		list.add(rect);

		boolean ret = GoLauncher.sendMessage(mFrame, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DRAG_OVER,
				mFrame.getDragType(), mDraggedView, list);
		list.clear();
		list = null;
		mDockPostion = null;
		if (Build.VERSION.SDK_INT >= 11) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_FORCE_REDRAW_CELLLAYOUT, -1, null, null);
		}
		return ret;
	}

	private boolean handleEndDragWithDel() {
		boolean ret = false;
		mDockPostion = null;
//		boolean delLastDockAppdrawer = DockUtil.isTheLastDockAppdrawer(mDraggedView);
//		if (!delLastDockAppdrawer) {
			switch (mFrame.getDragType()) {
				case DragFrame.TYPE_DOCK_DRAG :
					if (mDraggedView != null
							&& mDraggedView.getTag() != null
							&& mDraggedView.getTag() instanceof ItemInfo
							&& ((ItemInfo) mDraggedView.getTag()).mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
						// 如果是文件夹，先清除文件夹内容
						GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
								IDiyMsgIds.REMOVE_FOLDER_CONTENT, -1, mDragItemId, null);
					}
					// 删除屏幕上原来图标
					GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
							IDiyMsgIds.DELETE_DOCK_ITEM, -1, mDragItemId, null);
					//用户行为统计。
					StatisticsData.countUserActionData(
							StatisticsData.DESK_ACTION_ID_LONG_CLICK_DOCK_ICON,
							StatisticsData.USER_ACTION_FOUR, IPreferencesIds.DESK_ACTION_DATA);
					break;

				case DragFrame.TYPE_DOCK_FOLDERITEM_DRAG :
					GoLauncher.sendMessage(mFrame, IDiyFrameIds.DOCK_FRAME,
							IDiyMsgIds.DOCK_DELETE_FOLDERITEM, -1, mDraggedView, null);
					//用户行为统计。
					StatisticsData.countUserActionData(
							StatisticsData.DESK_ACTION_ID_LONG_CLICK_DOCK_ICON,
							StatisticsData.USER_ACTION_FOUR, IPreferencesIds.DESK_ACTION_DATA);
					break;

				default :
					GoLauncher
							.sendMessage(mFrame, IDiyFrameIds.SCREEN_FRAME,
									IDiyMsgIds.QUICKACTION_EVENT, IQuickActionId.DELETE,
									mDraggedView, null);
					//用户行为统计。
					if (Workspace.getLayoutScale() < 1) {
						StatisticsData.countUserActionData(
								StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
								StatisticsData.USER_ACTION_ONE, IPreferencesIds.DESK_ACTION_DATA);
					} else {
						int actionId = -1;
						Object targetInfo = mDraggedView.getTag();
						if (targetInfo instanceof ShortCutInfo) {
							actionId = StatisticsData.DESK_ACTION_ID_LONG_CLICK_ICON;
						} else if (targetInfo instanceof ScreenAppWidgetInfo) {
							actionId = StatisticsData.DESK_ACTION_ID_LONG_CLICK_WIDGET;
						} else if (targetInfo instanceof UserFolderInfo) {
							actionId = StatisticsData.DESK_ACTION_ID_LONG_CLICK_FLODER;
						}
						StatisticsData.countUserActionData(actionId,
								StatisticsData.USER_ACTION_FIVE, IPreferencesIds.DESK_ACTION_DATA);
					}
					break;
			}
	//	}
		return ret;
	}

	private void handleEndDragWithUninstall() {
		mDockPostion = null;
		if (mFrame.getDragType() == DragFrame.TYPE_DOCK_DRAG) {
			GoLauncher.sendMessage(mFrame, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_UNINSTALL_APP,
					-1, mDraggedView, null);
		} else {
			GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.QUICKACTION_EVENT,
					IQuickActionId.UNINSTALL, mDraggedView, null);
		}
	}

	@Override
	public void leaveImmediatly() {
		leaveNormally();
	}

	public void updateFolderRects(List<Rect> rcList) {
		mFolderAreaList.clear();
		if (null != rcList) {
			mFolderAreaList = rcList;
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HANDLE_OVER_TRASH_EADGE : {
					if (mDraggedView.getTag() instanceof ShortCutInfo) {
						// 处于卸载状态
						mStatus = STATUS_UNINSTALL;
						showUninstallToast();
					}
				}
					break;

				default :
					break;
			}
		};
	};

	private void showUninstallToast() {
		vibrate();
		DeskToast.makeText(mFrame.getActivity(), R.string.drag_uninstall_tip, Toast.LENGTH_SHORT)
				.show();
	}

	// 在文件夹内的拖拽处理
	private void doDragInFolder() {
		// 当前是文件夹内的换位置
		mFolderReplace = GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
				IDiyMsgIds.FOLDER_CHECK_POSITION, -1, mCenterPoint, null);
	}

	// 在workspace内的拖拽的处理
	private void doDragInWorkspace(float x, float y) {
		ArrayList<Point> list = new ArrayList<Point>();
		list.add(new Point((int) x, (int) y));
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DRAG_MOVING, -1,
				mDraggedView, list);
		list.clear();
		list = null;
		mLastInDock = false;
	}

	// 在dock内拖拽的处理
	private void doDragInDock() {
		Object tag = mDraggedView.getTag();
		if (!mFolderReplace && (tag instanceof ShortCutInfo || tag instanceof UserFolderInfo)) {
			ArrayList<Object> indexArray = new ArrayList<Object>();
			indexArray.add(mDraggedView);
			GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_CHECK_POSITION,
					mFrame.getDragType(), mCenterPoint, indexArray);
			if (mFrame.getDragType() == DragFrame.TYPE_DOCK_FOLDERITEM_DRAG)// 不在函数开始返回是为了让Dock
																			// folder显示开口状态
			{
				indexArray.clear();
				indexArray = null;
			} else {
				if (indexArray.size() > 1)// 在Dock区域
				{
					if (!mLastInDock) {
						// 第一次在桌面图标排版区域外，复位；比如在dock上
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_RESET_REPLACE, -1, null, null);
					}
					mLastInDock = true;
				} else {
					mDockPostion = null;
				}
				indexArray.clear();
				indexArray = null;
			}
		}
	}

	@Override
	public void onCenterPointF(IDragObject obj, float x, float y, int dragType) {
		mCenterPoint.set((int) x, (int) y);
		boolean needRedraw = true;

		switch (dragType) {
		// 1:文件夹操作
			case DRAG_IN_FOLDER : {
				needRedraw = false;
				doDragInFolder();
			}
				break;

			// 2:dock操作
			case DRAG_IN_DOCK : {
				doDragInDock();
			}
				break;

			// 3:workspace操作
			case DRAG_IN_WORKSPACE : {
				doDragInWorkspace(x, y);
			}
				break;

			default :
				break;
		}

		if (Build.VERSION.SDK_INT >= 11) {
			if (needRedraw) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_FORCE_REDRAW_CELLLAYOUT, -1, null, null);
			}
		}
	}

	private boolean isShortCutView(View view) {
		if (null != view) {
			Object tag = view.getTag();
			if (tag != null && tag instanceof ShortCutInfo) {
				if (((ShortCutInfo) tag).mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onDragFinish(IDragObject obj, float x, float y) {
		if (mFrame != null) {
			doBeforeDragFinish(obj);
			if (mCenterPoint.x > 0) {
				// 如果有中心点记录，判断dock位置就使用中心点记录
				// 否则使用原来的x,y
				x = mCenterPoint.x;
				y = mCenterPoint.y;
			}

			if ((mDraggedView.getTag() instanceof ShortCutInfo || mDraggedView.getTag() instanceof UserFolderInfo)
					&& isOverEdgeOfDock((int) x, (int) y)) {
				mStatus = STATUS_MOVE2DOCK;
			}

			endDrag(obj);
			leaveImmediatly();
		}
		mCenterPoint.set(-1, -1);
	}

	@Override
	public void onEnterEdge(IDragObject obj, int edgeType) {
		// 如果是垃圾桶，就不进入预览了
		if (STATUS_NORMAL != mStatus) {
			return;
		}

		if (null != obj) {
			obj.setColor(mEdgeColor, PorterDuff.Mode.SRC_ATOP);

			if (null != mEdgeFadeView) {
				mFrame.removeView(mEdgeFadeView);
				mEdgeFadeView.selfDestruct();
				mEdgeFadeView = null;
			}
			mEdgeFadeView = new FadeView(mDraggedView.getContext());
			int mode = 0;
			int width = 10;
			RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(0, 0);
			if (IDragListener.EDGE_LEFT == edgeType) {
				mode = FadePainter.DIR_FROM_LEFT;

				rlp.width = width;
				rlp.height = mFrame.getContentView().getHeight();
			} else if (IDragListener.EDGE_RIGHT == edgeType) {
				mode = FadePainter.DIR_FROM_RIGHT;

				rlp.width = width;
				rlp.height = mFrame.getContentView().getHeight();
				rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
			} else if (IDragListener.EDGE_TOP == edgeType) {
				mode = FadePainter.DIR_FROM_TOP;

				rlp.width = mFrame.getContentView().getWidth();
				rlp.height = width;
			}

			else if (IDragListener.EDGE_DOCK_LEFT == edgeType) {
				if (GoLauncher.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
					mode = FadePainter.DIR_FROM_LEFT;
					rlp.width = width;
					rlp.height = mFrame.getContentView().getResources()
							.getDimensionPixelSize(R.dimen.screen_long_end_padding_port);
					rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
				} else {
					mode = FadePainter.DIR_FROM_TOP;
					rlp.width = mFrame.getContentView().getResources()
							.getDimensionPixelSize(R.dimen.screen_long_end_padding_port);
					rlp.height = width;
					rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
				}

			}

			else if (IDragListener.EDGE_DOCK_RIGHT == edgeType) {
				if (GoLauncher.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
					mode = FadePainter.DIR_FROM_RIGHT;
					rlp.width = width;
					rlp.height = mFrame.getContentView().getResources()
							.getDimensionPixelSize(R.dimen.screen_long_end_padding_port);
					rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
					rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
				} else {
					mode = FadePainter.DIR_FROM_BOTTOM;
					rlp.width = mFrame.getContentView().getResources()
							.getDimensionPixelSize(R.dimen.screen_long_end_padding_port);
					rlp.height = width;
					rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
					rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
				}

			}

			else {
				mode = FadePainter.DIR_FROM_BOTTOM;

				rlp.width = mFrame.getContentView().getWidth();
				rlp.height = width;
				rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
			}
			mEdgeFadeView.initFade(mode, mEdgeColor);
			mFrame.getContentView().addView(mEdgeFadeView, rlp);
		}
	}

	@Override
	public void onLeaveEdge(IDragObject obj, int edgeType) {
		// 如果是垃圾桶，就不进入预览了
		if (STATUS_NORMAL != mStatus) {
			return;
		}

		if (null != obj) {
			obj.setColor(0, PorterDuff.Mode.SRC_ATOP);

			if (null != mEdgeFadeView) {
				mFrame.removeView(mEdgeFadeView);
				mEdgeFadeView.selfDestruct();
				mEdgeFadeView = null;
			}
		}
	}

	@Override
	public void onEdge(IDragObject obj, int edgeType) {
		// 如果是垃圾桶，就不进入预览了
		if (STATUS_NORMAL != mStatus) {
			return;
		}

		// 取消颜色
		if (null != obj) {
			obj.setColor(0, PorterDuff.Mode.SRC_ATOP);

			if (null != mEdgeFadeView) {
				mFrame.removeView(mEdgeFadeView);
				mEdgeFadeView.selfDestruct();
				mEdgeFadeView = null;
			}
		}

		if (edgeType == IDragListener.EDGE_DOCK_LEFT || edgeType == IDragListener.EDGE_DOCK_RIGHT) {
			GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_SNAP_TO_SCREEN,
					edgeType, null, null); // 删除屏幕上原来图标
			return;
		}

		// 切换操作
		if (mTrashAreaLayout != null) {
			mTrashAreaLayout.setVisibility(View.INVISIBLE);
		}

		mFrame.screenToPreviewSwitchHandle();
	}

	@Override
	public void onFingerPointF(IDragObject obj, float x, float y) {
		final int centerX = (int) x;
		final int centerY = (int) y;

		// 垃圾箱操作
		if (isOverEdgeOfTrash(centerX, centerY)) {
			if (mStatus == STATUS_NORMAL) {
				focusTrash(obj);
				mStatus = STATUS_DEL;
			}

			if (!mIsTrashCounting && !isShortCutView(mDraggedView)) {
				mIsTrashCounting = true;
				mHandler.sendEmptyMessageDelayed(HANDLE_OVER_TRASH_EADGE, TRASH_WAIT_DURATION);
			}
		} else {
			if (mStatus != STATUS_NORMAL) {
				unFocusTrash(obj);
				mStatus = STATUS_NORMAL;
			}

			if (mIsTrashCounting) {
				mIsTrashCounting = false;
				mHandler.removeMessages(HANDLE_OVER_TRASH_EADGE);
			}
		}
	}

	void leaveWithAnimation() {
		// 显示Dock层
		// GoLauncher.sendMessage(mFrame, IDiyFrameIds.DOCK_FRAME,
		// IDiyMsgIds.ENTER_FRAME,
		// -1, null, null);
		boolean animationStarted = false;
		mAnimationType = ANIMATION_REMOVE;
		View view = mStatus == STATUS_NORMAL ? mTrashAreaLayout : mFrame.getContentView();
		if (view != null) {
			Animation animation = MyAnimationUtils.getPopupAnimation(
					MyAnimationUtils.POP_TO_LONG_START_HIDE, -1);
			if (animation != null) {
				animationStarted = true;
				animation.setAnimationListener(this);
				view.startAnimation(animation);
			}
		}
		if (!animationStarted) {
			onAnimationEnd(null);
		}
	}

	void leaveNormally() {
		if (mFrame != null) {
			if (mFrame instanceof DragFrame) {
				GoLauncher.sendMessage(mFrame, IDiyFrameIds.APPFUNC_FRAME, IDiyMsgIds.DRAG_OVER,
						-1, null, null);
			}
			GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCHEDULE_FRAME,
					IFrameworkMsgId.REMOVE_FRAME, mFrame.getId(), null, null);
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (mAnimationType == ANIMATION_REMOVE) {
			mAnimationType = ANIMATION_NONE;
			// leaveNormally();
			if (mFrame != null) {
				// 先隐藏，再异步通知移除拖动层
				mFrame.getContentView().setVisibility(View.INVISIBLE);
				GoLauncher.postMessage(mFrame, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, mFrame.getId(), null, null);
			}
		}

	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

	@Override
	public void onDragMove(IDragObject obj, float x, float y) {
	}

	/**
	 * 用来判断View是否在dock上,不关心具体位置
	 * 
	 * @return
	 */

	public boolean isOverEdgeOfDock() {
		if (mCenterPoint != null) {
			if (mDraggedView instanceof DockIconView
					&& (mCenterPoint.x == -1 || mCenterPoint.y == -1))// dock图标且未移动过
			{
				return true;
			}
			return isOverEdgeOfDock(mCenterPoint.x, mCenterPoint.y);
		}
		return false;
	}

	@Override
	public void setTrashGone() {
		mTrashAreaLayout.setVisibility(View.GONE);
		mTrashAreaLayout.clearAnimation();
	}

	/***
	 * 垃圾桶是否可见
	 * 
	 * @return
	 */
	public boolean isShowTrash() {
		if (null != mTrashAreaLayout && mTrashAreaLayout.getVisibility() == View.VISIBLE) {
			return true;
		}
		return false;
	}

	public void showTrash() {
		mTrashAreaLayout.setVisibility(View.VISIBLE);
		startAnimation();
	}

	public void onFlyToDelFinish(IDragObject obj) {
		mStatus = STATUS_FLING_TO_DEL;
		endDrag(obj);
		mCenterPoint.set(-1, -1);
	}
}
