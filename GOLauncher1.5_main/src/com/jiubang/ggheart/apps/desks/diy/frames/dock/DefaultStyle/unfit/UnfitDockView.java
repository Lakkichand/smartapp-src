/**
 * Author: ruxueqin 
 * Date: 2012-5-25
 * Description: Dock快捷条显示视图组件
 */
package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.unfit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.gau.go.launcherex.R;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogSingleChoice;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsDockView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsLineLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.IDockSettingMSG;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenModifyFolderActivity;
import com.jiubang.ggheart.apps.desks.dock.DockChangeIconControler;
import com.jiubang.ggheart.apps.desks.dock.DockStylePkgInfo;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.FeatureItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.DockBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 
 * <br>类描述:非自适应模式dock根视图
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-9-20]
 */
public class UnfitDockView extends AbsDockView implements IAddIconResHandler {

	private Drawable mAddDrawable; // +号
	private Drawable mLightDrawable; // 发光底图

	private int mClickAddIconRowId; // 点击+号的行数
	private int mClickAddIconIndexInRow; // 点击+号的索引

	protected UnfitAnimationControler mAnimationControler; // 动画控制器

	/**
	 * @param context
	 * @param attrs
	 */
	public UnfitDockView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void init() {
		super.init();
		initAdd();
	}

	private void initAdd() {
		try {
			String style = GOLauncherApp.getSettingControler().getShortCutSettingInfo().mStyle;
			DeskThemeBean.SystemDefualtItem dockThemeItem = getDockThemeAddItem(style);
			if (null != dockThemeItem && null != dockThemeItem.mIcon
					&& null != dockThemeItem.mIcon.mResName) {
				// 主题安装包
				mAddDrawable = ImageExplorer.getInstance(mContext).getDrawable(style,
						dockThemeItem.mIcon.mResName);
			} else {
				// 风格安装包
				mAddDrawable = ImageExplorer.getInstance(mContext).getDrawable(style,
						DockStylePkgInfo.ADD_NAME);
			}
		} catch (Exception e) {
			// 不处理
		}
		if (null == mAddDrawable) {
			mAddDrawable = getContext().getResources().getDrawable(R.drawable.shortcut_0_addicon);
		}
		// 发光图片
		mLightDrawable = getContext().getResources().getDrawable(R.drawable.dock_light);
	}

	/**
	 * 初始化每个Linelayout的图标
	 * 
	 * @param bitmap
	 *            图标显示图
	 * @param rows
	 *            第几行
	 */
	@Override
	protected ArrayList<DockIconView> initIcon(int row) {
		ArrayList<DockIconView> m_IconViews = new ArrayList<DockIconView>();
		DockIconView dockIconView = null;
		for (int i = 0; i < DockUtil.ICON_COUNT_IN_A_ROW; i++) {
			dockIconView = initIcon(row, i); // 初始化每个图标
			if (null != dockIconView) {
				m_IconViews.add(dockIconView);
			}
		}
		return m_IconViews;
	}

	@Override
	public float getZoomProportion() {
		return DockUtil.getZoomProportion();
	}

	/**
	 * 刷新图标_删除
	 */
	@Override
	public DockIconView refreshUiDel(Long id) {
		DockIconView view = null;
		try {
			int iconhashmapSize = mIconViewsHashMap.size();
			for (int i = 0; i < iconhashmapSize; i++) {
				ArrayList<DockIconView> dockViewList = mIconViewsHashMap.get(i);
				for (DockIconView dockIconView : dockViewList) {
					if (null != dockIconView.getInfo().mItemInfo
							&& dockIconView.getInfo().mItemInfo.mInScreenId == id) {
						boolean removed = dockViewList.remove(dockIconView);
						if (removed) {
							view = dockIconView;
							reLayout();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		clearAnimationAndresetFlag();
		return view;
	}

	/**
	 * 刷新图标_交换
	 */
	@Override
	public void refreshUiChange() {
		clearAnimationAndresetFlag();
		reLayout();
	}

	/**
	 * 刷新图标_插入
	 */
	@Override
	public boolean refreshUiAdd(DockItemInfo info) {
		int curRow = mLineLayoutContainer.getCurLine();
		ArrayList<DockIconView> list = mIconViewsHashMap.get(curRow);
		if (list == null || list.size() >= DockUtil.ICON_COUNT_IN_A_ROW) {
			return false;
		}
		boolean ret = false;
		DockIconView view = initIcon(info);
		if (view != null) {
			view.setAreaPosition(info.getmIndexInRow());
		}
		if (null != view) {
			if (info.mItemInfo instanceof UserFolderInfo) {
				UserFolderInfo folder = (UserFolderInfo) info.mItemInfo;
				if (folder.mTotleUnreadCount > 0) {
					view.setmIsNotifyShow(true);
					view.setmNotifyCount(folder.mTotleUnreadCount);
				} else {
					view.setmIsNotifyShow(false);
					view.setmNotifyCount(0);
				}
			} else if (info.mItemInfo instanceof ShortCutInfo) {
				ShortCutInfo shortCut = (ShortCutInfo) info.mItemInfo;
				if (shortCut.mCounter > 0) {
					view.setmIsNotifyShow(true);
					view.setmNotifyCount(shortCut.mCounter);
				} else {
					view.setmIsNotifyShow(false);
					view.setmNotifyCount(0);
				}
			}
			list.add(view); // 更新获取新图标信息
			reLayout();
			ret = true;
		}
		clearAnimationAndresetFlag();
		return ret;
	}

	/**
	 * 删除当前图标所有的动画 和复位标志位
	 */
	@Override
	public void clearAnimationAndresetFlag() {
		if (null != mCurDockViewList) {
			for (DockIconView dockIconView : mCurDockViewList) {
				dockIconView.clearAnimation();
			}
		}
		// reLayout();
		if (mAnimationControler != null) {
			mAnimationControler.cancleAnimationMessage();
			mAnimationControler = null;
		}
		mDragView = null;
	}

	/**
	 * 执行图标动画
	 * 
	 * @param dragType
	 *            图标类型
	 * @param point
	 *            坐标
	 * @param dragView
	 *            拿起的图标
	 * @return
	 */
	@Override
	public boolean viewMoveAnimation(int dragType, Point point, View dragView) {
		if (null == mIconViewsHashMap || mLineLayoutContainer.getCurLine() < 0
				|| mLineLayoutContainer.getCurLine() >= mIconViewsHashMap.size()) {
			return false;
		}

		this.mDragView = dragView;
		if (mAnimationControler == null) {
			mCurDockViewList = mIconViewsHashMap.get(mLineLayoutContainer.getCurLine());
			int layoutW = GoLauncher.getScreenWidth();
			boolean trashgone = GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME,
					IDiyMsgIds.IS_TRASH_GONE, -1, null, null);
			int layoutH = (trashgone || dragType == DragFrame.TYPE_APPFUNC_ITEM_DRAG || dragType == DragFrame.TYPE_APPFUNC_FOLDERITEM_DRAG)
					? mLayoutH
					: GoLauncher.getScreenHeight();
			mAnimationControler = new UnfitAnimationControler(layoutW, layoutH, mIconPortraitH,
					mIconLandscapeW, mCurDockViewList, getContext(), dragView, mLineLayoutContainer);
			mAnimationControler.setmDragPositionListner(this);
		}

		return mAnimationControler.viewMoveAnimation(point, dragView);
	}

	/**
	 * 长按DOCK图标时记录DOCK图标所在的位置
	 * 
	 * @param dockIconView
	 */
	@Override
	public void initFirstDragDockViewData(DockIconView dockIconView) {
		dockIconView.setVisibility(View.INVISIBLE);
	}

	@Override
	public int getDockInAreaIndex() {
		if (mAnimationControler != null) {
			return mAnimationControler.getDockInAreaIndex();
		} else {
			return -1;
		}

	}

	/**
	 * 设置拖动图标的新位置
	 */
	@Override
	public boolean setDockDragViewNewInAreaIndex() {
		if (mAnimationControler != null) {
			mAnimationControler.cancleAnimationMessage(); // 先清移动动画消息
		}
		int dockInAreaIndex = getDockInAreaIndex();
		if (dockInAreaIndex != -1) {
			((DockIconView) mDragView).setAreaPosition(dockInAreaIndex);
			return true;
		}
		return false;

	}
	/**
	 * 从某一主题获得+号
	 * 
	 * @return
	 */
	private DeskThemeBean.SystemDefualtItem getDockThemeAddItem(String themePkg) {
		if (null == themePkg) {
			return null;
		}

		DockBean bean = DockChangeIconControler.getInstance(mContext).getDockBean(themePkg);
		return bean.mNoApplicationIcon;
	}

	@Override
	public Drawable getAddDrawable() {
		return mAddDrawable;
	}

	@Override
	public Drawable getLightDrawable() {
		return mLightDrawable;
	}

	@Override
	public void doStyleChange() {
		super.doStyleChange();
		initAdd();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		int action = ev.getAction();
		switch (action) {
			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP :
				int count = mLineLayoutContainer.getChildCount();
				for (int i = 0; i < count; i++) {
					UnfitLineLayout lineLayout = (UnfitLineLayout) mLineLayoutContainer
							.getChildAt(i);
					lineLayout.setLightAddIndex(-1);
				}
				break;

			default :
				break;
		}

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public void setAddIconIndex(ArrayList<Integer> list) {
		UnfitLineLayout unfitLineLayout = (UnfitLineLayout) mLineLayoutContainer
				.getChildAt(mLineLayoutContainer.getCurLine());
		unfitLineLayout.setAddIndex(list);
	}

	@Override
	public AbsLineLayout getLineLayout() {
		UnfitLineLayout layout = new UnfitLineLayout(getContext());
		layout.setOnClickListener(this);
		layout.setOnLongClickListener(this);
		layout.setAddIconResHandler(this);
		return layout;
	}

	@Override
	public int getDockIconSize(int iconNum) {
		return DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
	}

	@Override
	public boolean dragOverHandle(int dragType, boolean mergeFolder, boolean intoFolder,
			int toIndexInRow, AbsLineLayout lineLayout, int rowId, View dragView) {
		boolean ret = false;
		// 快速把图标放到DOCK后放手。动画还没开始做。所以获取的值为-1.这时需要取消动画
		boolean canInsert = 0 <= rowId && rowId < getShortCutItems().size() && 0 <= toIndexInRow
				&& toIndexInRow < DockUtil.ICON_COUNT_IN_A_ROW;
		int oldcount = 0;
		if (lineLayout != null) {
			oldcount = lineLayout.getChildCount();
		}
		switch (dragType) {
			// DOCK图标
			case DragFrame.TYPE_DOCK_DRAG :
				if (mergeFolder || intoFolder) {
					// 删除原有项
					DockIconView dockIconView = (DockIconView) dragView;
					if (null != dockIconView.getInfo() && null != dockIconView.getInfo().mItemInfo) {
						handleMessage(this, -1, IDiyMsgIds.DELETE_DOCK_ITEM, -1,
								dockIconView.getInfo().mItemInfo.mInScreenId, null);
					}
				} else {
					// 自身交换
					// 判断没有进行交换就放手
					if (setDockDragViewNewInAreaIndex()) {
						if (null != getCurretnIcon()) {
							getCurretnIcon().setVisibility(View.VISIBLE);
						}
						boolean modify = modifyShortcutItemIndexUnfit();
						if (modify) {
							refreshUiChange();
						}
					}
				}
				break;

			// 屏幕图标（包括文件夹）
			case DragFrame.TYPE_SCREEN_ITEM_DRAG :
				if (null != lineLayout) {
					if (mergeFolder || intoFolder) {
						ret = true;
					} else if (oldcount < DockUtil.ICON_COUNT_IN_A_ROW && canInsert) {
						ret = screenItemDrag(oldcount, rowId, toIndexInRow, canInsert, ret);
					} else { // 超过5个，不能插入
						ret = saveMoveToScreenData();
					}
				} else {
					ret = false;
				}
				break;

			// DOCK条文件夹里面的图标
			case DragFrame.TYPE_DOCK_FOLDERITEM_DRAG :
				if (mergeFolder || intoFolder) {
					ret = true;
				} else {
					boolean toSelf = false;
					// 判断是否插入本身所在的文件夹
					try {
						DockIconView mergeView_target = getmOpenFolderView();
						DockItemInfo itemInfo_target = mergeView_target.getInfo();
						toSelf = getCurretnIcon().getInfo() == itemInfo_target;
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (!toSelf && canInsert) {
						if (lineLayout.getChildCount() < DockUtil.ICON_COUNT_IN_A_ROW) {
							ret = dockFolderItemDragHandle(oldcount, rowId, toIndexInRow,
									canInsert, ret);
						} else {
							long folderId = getCurretnIcon() != null
									? getCurretnIcon().getInfo().mItemInfo.mInScreenId
									: -1;
							ret = saveMoveToScreenData();
							if (ret) {
								ArrayList<ItemInfo> list = new ArrayList<ItemInfo>();
								list.add((ItemInfo) mDragView.getTag());
								//要用handler异步才可以清除，因为screenFrame addview是异步的
								GoLauncher.sendHandler(this, IDiyFrameIds.SCREEN_FRAME,
										IDiyMsgIds.SCREEN_DELETE_FOLDER_ITEM, -1, folderId, list);
							}
						}
					} else {
						invalidate();
					}
				}
				// 如果插入成功，就删除文件夹数据
				if (ret) {
					handleMessage(this, IMsgType.SYNC, IDiyMsgIds.DOCK_DELETE_FOLDERITEM, -1,
							dragView, null);
				}
				break;

			case DragFrame.TYPE_SCREEN_FOLDER_DRAG :
			case DragFrame.TYPE_APPFUNC_ITEM_DRAG :
				if (mergeFolder || intoFolder) {
					ret = true;
				} else if (lineLayout.getChildCount() < DockUtil.ICON_COUNT_IN_A_ROW && canInsert) {
					ret = screenFolderDragHandler(rowId, toIndexInRow, canInsert, ret);
				} else { // 超过5个，不能插入
					ret = saveMoveToScreenData();
				}
				break;

			case DragFrame.TYPE_APPFUNC_FOLDERITEM_DRAG :
				if (lineLayout.getChildCount() < DockUtil.ICON_COUNT_IN_A_ROW && canInsert) {
					appFunFolderItemDragHandle(rowId, toIndexInRow, canInsert, ret);
				} else { // 超过5个，不能插入
					ret = saveMoveToScreenData();
					if (ret && mDragView != null && mDragView.getTag() != null
							&& mDragView.getTag() instanceof UserFolderInfo) {
						UserFolderInfo userFolderInfo = (UserFolderInfo) mDragView.getTag();
						mDockControler.addDrawerFolderToDock(userFolderInfo);
					}
				}
				break;

			default :
				break;
		}
		return ret;
	}

	/****
	 * 屏幕层的图标（包括文件夹）拖动处理
	 * 
	 * @param rowId
	 * @param toIndexInRow
	 * @param canInsert
	 * @param ret
	 * @return
	 */
	private boolean screenItemDrag(int oldcount, int rowId, int toIndexInRow, boolean canInsert,
			boolean ret) {
		FeatureItemInfo tag = (FeatureItemInfo) mDragView.getTag();
		// 插入
		DockItemInfo dockItemInfo = new DockItemInfo(tag.mItemType,
				DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW));
		dockItemInfo.setInfo(tag);
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);
		if (tag instanceof UserFolderInfo) {
			final BitmapDrawable bDrawable = dockItemInfo.getFolderBackIcon();
			final Bitmap bitmap = dockItemInfo.prepareOpenFolderIcon(bDrawable);
			final BitmapDrawable icon = new BitmapDrawable(mContext.getResources(), bitmap);
			dockItemInfo.setIcon(icon);
		}
		// 先修改其他图标索引
		ret = refShortcutItem(ret, dockItemInfo, toIndexInRow, canInsert, tag, false);
		return ret;
	}

	/***
	 * DOCK条文件夹里面的图标拖动处理
	 * 
	 * @param rowId
	 * @param toIndexInRow
	 * @param canInsert
	 * @param ret
	 * @return
	 */
	private boolean dockFolderItemDragHandle(int oldcount, int rowId, int toIndexInRow,
			boolean canInsert, boolean ret) {
		// 插入
		ShortCutInfo tag = (ShortCutInfo) mDragView.getTag();
		DockItemInfo dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_SHORTCUT,
				DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW));
		dockItemInfo.setInfo(tag);

		// 索引
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);

		ret = refShortcutItem(ret, dockItemInfo, toIndexInRow, canInsert, tag, false);
		return ret;
	}

	/****
	 * 屏幕层文件夹里面的图标和功能表的图标拖动处理
	 * 
	 * @param rowId
	 * @param toIndexInRow
	 * @param canInsert
	 * @param ret
	 * @return
	 */
	private boolean screenFolderDragHandler(int rowId, int toIndexInRow, boolean canInsert,
			boolean ret) {
		// 插入
		ShortCutInfo tag = (ShortCutInfo) mDragView.getTag();
		DockItemInfo dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_SHORTCUT,
				DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW));
		dockItemInfo.setInfo(tag);
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);
		ret = refShortcutItem(ret, dockItemInfo, toIndexInRow, canInsert, tag, false);
		return ret;
	}

	/***
	 * 功能表文件夹图标拖动处理
	 * 
	 * @param rowId
	 * @param toIndexInRow
	 * @param canInsert
	 * @param ret
	 * @return
	 */
	private boolean appFunFolderItemDragHandle(int rowId, int toIndexInRow, boolean canInsert,
			boolean ret) {
		// 插入
		UserFolderInfo tag = (UserFolderInfo) mDragView.getTag();
		DockItemInfo dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_USER_FOLDER,
				DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW));
		tag.mInScreenId = System.currentTimeMillis();
		dockItemInfo.setInfo(tag);
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);
		// 图标
		final BitmapDrawable bDrawable = dockItemInfo.getFolderBackIcon();
		final Bitmap bitmap = dockItemInfo.prepareOpenFolderIcon(bDrawable);
		final BitmapDrawable icon = new BitmapDrawable(mContext.getResources(), bitmap);
		dockItemInfo.setIcon(icon);

		ret = refShortcutItem(ret, dockItemInfo, toIndexInRow, canInsert, tag, true);
		return ret;
	}

	private boolean refShortcutItem(boolean ret, DockItemInfo dockItemInfo, int toIndexInRow,
			boolean canInsert, FeatureItemInfo tag, boolean insertFolder) {
		// 先修改其他图标索引
		boolean modify = modifyShortcutItemIndexUnfit();
		ArrayList<DockIconView> currentDockIcons = getRowDockIcons(getLineLayoutContainer()
				.getCurLine());
		int size = currentDockIcons.size();
		DockIconView dockIconView = null;
		for (int i = 0; i < size; i++) {
			dockIconView = currentDockIcons.get(i);
			DockItemInfo info = dockIconView.getInfo();
			if (info.getmIndexInRow() == toIndexInRow) {
				canInsert = false;
				break;
			}
		}
		if (canInsert) {
			if (insertFolder) {
				if (insertShortcutItem(dockItemInfo)) {
					mDockControler.addDrawerFolderToDock((UserFolderInfo) tag); // 插入文件夹子项
					refreshUiAdd(dockItemInfo); // 刷新缓存
				}
			} else {
				insertShortcutItem(dockItemInfo);
				refreshUiAdd(dockItemInfo); // 刷新缓存
			}

			ret = true;
		} else if (modify) {
			requestLayout();
		}

		return ret;
	}

	@Override
	public ArrayList<Integer> getBlanks(int lineID) {
		ConcurrentHashMap<Integer, ArrayList<Integer>> hashmap = mDockControler
				.getShortCutUnfitBlanks();
		ArrayList<Integer> list = hashmap.get(lineID);

		return list;
	}

	@Override
	public void setBlank() {
		boolean add = mDockControler.addBlank(mClickAddIconRowId, mClickAddIconIndexInRow);
		if (add) {
			mLineLayoutContainer.requestLayout();
		}
	}

	@Override
	protected void clickBlank(View v) {
		showAddIconDialog(getLineLayoutContainer().getCurLine(),
				((UnfitLineLayout) v).getClickAddIndex());
	}

	@Override
	protected boolean longClickBlank(View v) {
		int rowid = getLineLayoutContainer().getCurLine();
		int indexinrow = ((UnfitLineLayout) v).getLongClickBlank();

		if (0 <= rowid && rowid < DockUtil.TOTAL_ROWS && 0 <= indexinrow
				&& indexinrow < DockUtil.ICON_COUNT_IN_A_ROW) {
			// 判断当前是否是空白显示
			ConcurrentHashMap<Integer, ArrayList<Integer>> hashmap = mDockControler
					.getShortCutUnfitBlanks();
			if (null != hashmap && rowid < hashmap.size()) {
				ArrayList<Integer> list = hashmap.get(rowid);
				if (list.contains(indexinrow)) {
					showAddIconDialog(rowid, indexinrow);
					return true;
				}
			}
		}
		return false;
	}

	/***
	 * 点击加号响应
	 * 
	 * @param rowid
	 * @param indexinrow
	 */
	private void showAddIconDialog(int rowid, int indexinrow) {
		if (0 <= rowid && rowid < DockUtil.TOTAL_ROWS && 0 <= indexinrow
				&& indexinrow < DockUtil.ICON_COUNT_IN_A_ROW) {

			mClickAddIconRowId = rowid;
			mClickAddIconIndexInRow = indexinrow;
			if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
				LockScreenHandler.showLockScreenNotification(mContext);
				return;
			}
			try {
//				DockSettingDialog aDialog = DockSettingDialog
//						.getDockSettingDialog((Activity) getContext());
//				aDialog.mListener = this;
//				aDialog.show();
				
				DialogSingleChoice addIconDailog = new DialogSingleChoice(getContext());
				addIconDailog.show();
				addIconDailog.setTitle(R.string.select_app_icon);
				int[] imageId = {
						R.drawable.shortcut_dialog_application,
						R.drawable.shortcut_dialog_shortcut,
						R.drawable.shortcut_dialog_funclist,
						R.drawable.shortcut_dialog_blank
				};
				
				CharSequence[] items = {
						mContext.getString(R.string.open_App),
						mContext.getString(R.string.add_app_icon),
						mContext.getString(R.string.appfunc),
						mContext.getString(R.string.blank)
				};
				
				addIconDailog.setItemData(items, imageId, -1, false);
				addIconDailog.setOnItemClickListener(new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
							case IDockSettingMSG.OPEN_APP :
								if (mContext != null) {
									Intent intent = new Intent(mContext, ScreenModifyFolderActivity.class);
									intent.putExtra(ScreenModifyFolderActivity.ADD_APP_TO_DOCK_UNFIT, true);
									mContext.startActivity(intent);
								}
								break;

							case IDockSettingMSG.OPEN_WIDGET :
								selectShortCut(true);
								break;

							case IDockSettingMSG.FUNCLIST :
								setAppFunIcon();
								break;

							case IDockSettingMSG.BLANK :
								setBlank();

							default :
								break;
						}
					}
				});
				
			} catch (Throwable e) {
				// 异常，不显示对话框
			}
		}
	}

	@Override
	public void setAppFunIcon() {
		DockItemInfo dockInfo = new DockItemInfo(IItemType.ITEM_TYPE_SHORTCUT,
				DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW));
		dockInfo.setmRowId(mClickAddIconRowId);
		dockInfo.setmIndexInRow(mClickAddIconIndexInRow);

		ShortCutInfo shortCutInfo = new ShortCutInfo();
		shortCutInfo.mIntent = new Intent(ICustomAction.ACTION_SHOW_FUNCMENU);
		shortCutInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
		shortCutInfo.mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
		shortCutInfo.mInScreenId = System.currentTimeMillis();
		mDockControler.prepareItemInfo(shortCutInfo);

		dockInfo.setInfo(shortCutInfo);

		// 数据库
		insertShortcutItem(dockInfo);
		Message msg = new Message();
		msg.what = DockUtil.HANDLE_REFRESH_DOCK_ITEM_UI;
		msg.obj = dockInfo;
		mHandler.sendMessage(msg);
	}

	protected void addApplication(Object object) {
		if ((object != null) && (object instanceof AppItemInfo)) {
			AppItemInfo info = (AppItemInfo) object;
			DockItemInfo dockItemInfo = createDockItemInfo(info, mClickAddIconRowId,
					mClickAddIconIndexInRow, DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW));

			// 数据库
			insertShortcutItem(dockItemInfo);
			Message msg = new Message();
			msg.what = DockUtil.HANDLE_REFRESH_DOCK_ITEM_UI;
			msg.obj = dockItemInfo;
			mHandler.sendMessage(msg);
		}
	}

	protected void addShortcut(Object object) {
		if ((object != null) && (object instanceof ShortCutInfo)) {
			int iconSize = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
			DockItemInfo dockInfo = createDockItemInfo((ShortCutInfo) object, mClickAddIconRowId,
					mClickAddIconIndexInRow, iconSize);
			if (null != dockInfo) {
				((ShortCutInfo) object).mInScreenId = System.currentTimeMillis();
				dockInfo.setmRowId(mClickAddIconRowId);
				dockInfo.setmIndexInRow(mClickAddIconIndexInRow);
				insertShortcutItem(dockInfo);
				Message msg = new Message();
				msg.what = DockUtil.HANDLE_REFRESH_DOCK_ITEM_UI;
				msg.obj = dockInfo;
				mHandler.sendMessage(msg);
			}
		}
	}

	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		boolean ret = super.handleMessage(who, type, msgId, param, object, objects);

		if (!ret) {
			//可能是没处理的消息
			switch (msgId) {
				case IDiyMsgIds.DOCK_ADD_APPLICATION :
					addApplication(object);
					ret = true;
					break;

				case IDiyMsgIds.DOCK_ADD_SHORTCUT :
					addShortcut(object);
					ret = true;
					break;

				default :
					break;
			}
		}

		return ret;
	}
}