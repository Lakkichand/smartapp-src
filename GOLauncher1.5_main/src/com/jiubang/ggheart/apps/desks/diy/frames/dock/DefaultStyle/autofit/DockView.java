package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.autofit;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DeskSettingDockDefaultIconsDialog.OnDefaultIconsListner;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsDockView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsLineLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.FeatureItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;

/**
 * 
 * <br>类描述:自适应模式dock根视图
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-9-20]
 */
public class DockView extends AbsDockView implements OnDefaultIconsListner, OnDismissListener {

	private int mFirstCurLine = -1; // 第一次拖动时所在的位置
	private int mFirstDockInListIndex = -1; // 第一次拖动图标所在List的位置
	private AnimationControler mAnimationControler; // 动画控制器

	private int mLongClickBlankRow; // 长按空白时的行数
	private int mLongClickBlankIndexInRow; //长按空白时的索引
	
	/**
	 * @param context
	 * @param attrs
	 */
	public DockView(Context context, AttributeSet attrs) {
		super(context, attrs);
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
				int addIndex = 0;
				int listSize = m_IconViews.size();
				for (int j = 0; j < listSize; j++) {
					DockIconView view = m_IconViews.get(j);
					if (view.getInfo().getmIndexInRow() < dockIconView.getInfo().getmIndexInRow()) {
						addIndex++;
					} else {
						break;
					}
				}
				m_IconViews.add(addIndex, dockIconView);
			}
		}
		return m_IconViews;
	}

	@Override
	public float getZoomProportion() {
		return DockUtil.getZoomProportion(mCurDockViewList.size(), mAnimationControler, mDragView,
				mFirstCurLine, mLineLayoutContainer.getCurLine());
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
				int size = dockViewList.size();
				for (int j = 0; j < size; j++) {
					DockIconView dockIconView = dockViewList.get(j);
					if (null != dockIconView.getInfo() && null != dockIconView.getInfo().mItemInfo
							&& dockIconView.getInfo().mItemInfo.mInScreenId == id) {
						boolean removed = dockViewList.remove(dockIconView);
						if (removed) {
							view = dockIconView;
							size--;
							j--;
							reLayout();
						}
					}
				}
			}
		} catch (Exception e) {
		}
		clearAnimationAndresetFlag();
		return view;
	}

	@Override
	public boolean refreshUiChange(DockIconView view, int index) {
		if (null == view || index < 0 || index >= DockUtil.ICON_COUNT_IN_A_ROW) {
			return false;
		}

		boolean ret = false;
		try {
			ArrayList<DockIconView> dockViewList = mIconViewsHashMap.get(mLineLayoutContainer
					.getCurLine());
			boolean removed = dockViewList.remove(view);
			if (removed) {
				dockViewList.add(index, view);
				ret = true;
			}
		} catch (Exception e) {
			// 不处理
		}
		view.setVisibility(View.VISIBLE);
		view.setmIsBgShow(false); // 不显示高亮
		clearAnimationAndresetFlag();
		reLayout();
		return ret;
	}

	/**
	 * 刷新图标_插入
	 */
	@Override
	public boolean refreshUiAdd(DockItemInfo info) {
		boolean ret = false;
		int dockInAreaIndex = info.getmIndexInRow();
		if (null != mCurDockViewList && 0 <= dockInAreaIndex
				&& dockInAreaIndex <= mCurDockViewList.size()
				&& mCurDockViewList.size() <= DockUtil.ICON_COUNT_IN_A_ROW
				&& dockInAreaIndex < DockUtil.ICON_COUNT_IN_A_ROW) {
			DockIconView view = initIcon(info);
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
				mCurDockViewList.add(dockInAreaIndex, view);
				ret = true;
			}
		}
		clearAnimationAndresetFlag();
		reLayout();
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
		mAnimationControler = null;
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

		mDragView = dragView;
		if (mAnimationControler == null) {
			mCurDockViewList = mIconViewsHashMap.get(mLineLayoutContainer.getCurLine());
			int layoutW = GoLauncher.getScreenWidth();
			boolean trashgone = GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME,
					IDiyMsgIds.IS_TRASH_GONE, -1, null, null);
			int layoutH = (trashgone || dragType == DragFrame.TYPE_APPFUNC_ITEM_DRAG || dragType == DragFrame.TYPE_APPFUNC_FOLDERITEM_DRAG)
					? mLayoutH
					: GoLauncher.getScreenHeight();
			//TODO:AnimationControler可以不仅在构造时设置layoutH
			mAnimationControler = new AnimationControler(layoutW, layoutH, mIconPortraitH,
					mIconLandscapeW, mCurDockViewList, getContext(), dragView, mLineLayoutContainer);
			mAnimationControler.setmDragPositionListner(this);
		}

		if (dragType == DragFrame.TYPE_SCREEN_FOLDER_DRAG
				|| dragType == DragFrame.TYPE_SCREEN_ITEM_DRAG
				|| dragType == DragFrame.TYPE_DOCK_FOLDERITEM_DRAG
				|| dragView.getParent() != mLineLayoutContainer.getChildAt(mLineLayoutContainer
						.getCurLine())) {
			return mAnimationControler.deskIconViewMove(point);
		}

		else if (dragType == DragFrame.TYPE_DOCK_DRAG) {
			return mAnimationControler.dockIconViewMove(point, dragView);
		}
		return false;
	}

	/**
	 * 长按DOCK图标时记录DOCK图标所在的位置
	 * 
	 * @param dockIconView
	 */
	@Override
	public void initFirstDragDockViewData(DockIconView dockIconView) {
		dockIconView.setVisibility(View.INVISIBLE);
		mFirstCurLine = mLineLayoutContainer.getCurLine();
		ArrayList<DockIconView> dockViewList = mIconViewsHashMap.get(mFirstCurLine);
		mFirstDockInListIndex = dockViewList.indexOf(dockIconView);
	}

	@Override
	public void resetFirstDragDockViewData() {
		mFirstCurLine = -1; // 第一次拖动时所在的位置
		mFirstDockInListIndex = -1;
	}

	/**
	 * 获取拿起DOCK图标所在的行
	 * 
	 * @return
	 */
	public int getFirstCurLine() {
		return mFirstCurLine;
	}

	/**
	 * 获取拿起DOCK图标所在队列的位置
	 * 
	 * @return
	 */
	public int getFirstDockInListIndex() {
		return mFirstDockInListIndex;
	}

	/**
	 * 返回当前图标在哪个区域的位置
	 * 
	 * @return
	 */
	@Override
	public int getDockInAreaIndex() {
		return mAnimationControler.getDockInAreaIndex();
	}

	/**
	 * 返回当前图标在哪个图标队列中的位置
	 * 
	 * @return
	 */
	public int getDockInListIndex() {
		return mAnimationControler.getDockInListIndex();
	}

	@Override
	public boolean setDockDragViewNewInAreaIndex() {
		return true;
	}

	@Override
	public void setAddIconIndex(ArrayList<Integer> list) {
	}

	@Override
	public AbsLineLayout getLineLayout() {
		LineLayout layout = new LineLayout(getContext());
		layout.setOnLongClickListener(this);
		return layout;
	}

	@Override
	public int getDockIconSize(int iconNum) {
		return DockUtil.getIconSize(iconNum);
	}

	@Override
	public boolean dragOverHandle(int dragType, boolean mergeFolder, boolean intoFolder,
			int toIndexInRow, AbsLineLayout lineLayout, int rowId, View dragView) {
		boolean ret = false;
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
					// 自身交换位置
					DockIconView dragDockIconView = (DockIconView) dragView;
					if (refreshUiChange(dragDockIconView, toIndexInRow)) {
						ArrayList<DockIconView> viewList = getRowDockIcons(getLineLayoutContainer()
								.getCurLine());
						if (null != viewList) {
							int size = viewList.size();

							for (int i = 0; i < size; i++) {
								DockIconView dockIconView = viewList.get(i);
								if (null != dockIconView && null != dockIconView.getInfo()) {
									mDockControler.modifyShortcutItemIndex(dockIconView.getInfo(),
											i);
								}
							}
						}
						ret = true;
					}
					// UI同步数据库校验
					verifyData();
				}
				break;

			// 屏幕图标（包括文件夹）
			case DragFrame.TYPE_SCREEN_ITEM_DRAG :

				if (null != lineLayout) {
					FeatureItemInfo tag = (FeatureItemInfo) dragView.getTag();
					if (mergeFolder || intoFolder) {
						ret = true;
					} else if (oldcount < DockUtil.ICON_COUNT_IN_A_ROW) {
						screenItemDragHandle(tag, oldcount, rowId, toIndexInRow);
						ret = true;
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
					}
					if (!toSelf) {
						if (oldcount < DockUtil.ICON_COUNT_IN_A_ROW) {
							folderItemDragHandle(oldcount, rowId, toIndexInRow);
							ret = true;
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
				if (ret) {
					handleMessage(this, IMsgType.SYNC, IDiyMsgIds.DOCK_DELETE_FOLDERITEM, -1,
							dragView, null);
				}
				break;

			case DragFrame.TYPE_SCREEN_FOLDER_DRAG :
			case DragFrame.TYPE_APPFUNC_ITEM_DRAG : {
				if (mergeFolder || intoFolder) {
					ret = true;
				} else if (oldcount < DockUtil.ICON_COUNT_IN_A_ROW) {
					folderDragHandle(oldcount, rowId, toIndexInRow);
					ret = true;
				} else { // 超过5个，不能插入
					ret = saveMoveToScreenData();
				}
			}
				break;

			case DragFrame.TYPE_APPFUNC_FOLDERITEM_DRAG : {
				if (oldcount < DockUtil.ICON_COUNT_IN_A_ROW) {
					appFunFolderDragHandle(oldcount, rowId, toIndexInRow);
					ret = true;
				} else { // 超过5个，不能插入
					ret = saveMoveToScreenData();
					if (ret && mDragView != null && mDragView.getTag() != null
							&& mDragView.getTag() instanceof UserFolderInfo) {
						UserFolderInfo userFolderInfo = (UserFolderInfo) mDragView.getTag();
						mDockControler.addDrawerFolderToDock(userFolderInfo);
					}
				}
			}
				break;
			default :
				break;
		}
		return ret;
	}

	/***
	 * 屏幕层的图标（包括文件夹）拖动处理
	 * 
	 * @param tag
	 * @param oldcount
	 * @param rowId
	 * @param toIndexInRow
	 */
	private void screenItemDragHandle(FeatureItemInfo tag, int oldcount, int rowId, int toIndexInRow) {
		// 插入
		DockItemInfo dockItemInfo = new DockItemInfo(tag.mItemType,
				DockUtil.getIconSize(oldcount + 1));
		dockItemInfo.setInfo(tag);
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);
		if (tag instanceof UserFolderInfo) {
			final BitmapDrawable bDrawable = dockItemInfo.getFolderBackIcon();
			final Bitmap bitmap = dockItemInfo.prepareOpenFolderIcon(bDrawable);
			final BitmapDrawable icon = new BitmapDrawable(mContext.getResources(), bitmap);
			dockItemInfo.setIcon(icon);
		}
		refShortcutItem(dockItemInfo, oldcount, rowId, false, null);
	}

	/***
	 * DOCK条文件夹里面的图标拖动处理
	 * 
	 * @param oldcount
	 * @param rowId
	 * @param toIndexInRow
	 */
	private void folderItemDragHandle(int oldcount, int rowId, int toIndexInRow) {
		// 插入
		ShortCutInfo tag = (ShortCutInfo) mDragView.getTag();
		DockItemInfo dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_SHORTCUT,
				DockUtil.getIconSize(oldcount + 1));
		dockItemInfo.setInfo(tag);
		// 索引
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);
		refShortcutItem(dockItemInfo, oldcount, rowId, false, null);
	}

	/***
	 * 功能表的图标和屏幕层文件夹里面的图标拖动处理
	 * 
	 * @param oldcount
	 * @param rowId
	 * @param toIndexInRow
	 */
	private void folderDragHandle(int oldcount, int rowId, int toIndexInRow) {
		ShortCutInfo tag = (ShortCutInfo) mDragView.getTag();
		DockItemInfo dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_SHORTCUT,
				DockUtil.getIconSize(oldcount + 1));
		dockItemInfo.setInfo(tag);
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);
		refShortcutItem(dockItemInfo, oldcount, rowId, false, null);
	}

	/***
	 * 功能表文件夹里面的图标拖动处理
	 * 
	 * @param oldcount
	 * @param rowId
	 * @param toIndexInRow
	 */
	private void appFunFolderDragHandle(int oldcount, int rowId, int toIndexInRow) {
		// 插入
		UserFolderInfo tag = (UserFolderInfo) mDragView.getTag();
		DockItemInfo dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_USER_FOLDER,
				DockUtil.getIconSize(oldcount + 1));
		tag.mInScreenId = System.currentTimeMillis();
		dockItemInfo.setInfo(tag);
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);
		// 图标
		final BitmapDrawable bDrawable = dockItemInfo.getFolderBackIcon();
		final Bitmap bitmap = dockItemInfo.prepareOpenFolderIcon(bDrawable);
		final BitmapDrawable icon = new BitmapDrawable(mContext.getResources(), bitmap);
		dockItemInfo.setIcon(icon);
		refShortcutItem(dockItemInfo, oldcount, rowId, true, tag);
	}

	/***
	 * 刷UI和数据库
	 * 
	 * @param dockItemInfo
	 * @param oldcount
	 * @param rowId
	 * @param isInsert
	 * @param tag
	 */
	private void refShortcutItem(DockItemInfo dockItemInfo, int oldcount, int rowId,
			boolean isInsert, UserFolderInfo tag) {
		// 先add UI,再add DB
		if (refreshUiAdd(dockItemInfo)) {
			ArrayList<DockIconView> list = getRowDockIcons(rowId);
			if (null != list) {
				int listSize = list.size();
				for (int i = 0; i < listSize; i++) {
					DockIconView dockIconView = list.get(i);
					int index = dockIconView.getInfo().getmIndexInRow();
					if (dockIconView.getInfo() == dockItemInfo) {
						boolean insert = insertShortcutItem(dockItemInfo);
						if (isInsert && insert) {
							mDockControler.addDrawerFolderToDock(tag); // 插入文件夹子项
						}
					}
					if (index != i) {
						// 可能响应到其他图标的索引更新
						mDockControler.modifyShortcutItemIndex(dockIconView.getInfo(), i);
					}
				}
			}
			// 判断图片size是否改变
			if (oldcount == DockUtil.ICON_COUNT_IN_A_ROW - 1) {
				updateLineLayoutIconsSize(rowId);
			}
		}
		// UI同步数据库校验
		verifyData();
	}

	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		boolean ret = super.handleMessage(who, type, msgId, param, object, objects);

		if (!ret) {
			//可能是基类AbsDockView没处理的消息
			switch (msgId) {
				case IDiyMsgIds.DOCK_ADD_APPLICATION :
				case IDiyMsgIds.DOCK_ADD_SHORTCUT :
					Message msg = new Message();
					msg.what = DockUtil.HANDLE_ADD_APPLICATION;
					msg.obj = object;
					mHandler.sendMessage(msg);
					ret = true;
					break;
					
				case IDiyMsgIds.ADD_APPS_TO_DOCK_FIT:
					if (objects != null && !objects.isEmpty()) {
						ArrayList<AppItemInfo> addItemInfos = (ArrayList<AppItemInfo>) objects;
						for (AppItemInfo appItemInfo : addItemInfos) {
							addApplication(appItemInfo);
						}
					}
				
				//dock条长按添加图标-添加一个图标
				case IDiyMsgIds.DOCK_ADD_ICON_ADD_ONE:
					if (object instanceof AppItemInfo) {
						addApplication(object);
					} else if (object instanceof ShortCutInfo) {
						addShortcut(object);
					}
					break;
				default :
					break;
			}
		}

		return ret;
	}

	@Override
	protected void clickBlank(View v) {
	}

	@Override
	protected boolean longClickBlank(View v) {
		mLongClickBlankRow = getLineLayoutContainer().getCurLine();
		mLongClickBlankIndexInRow = getClickBlankIndex();
	
		AbsLineLayout curLineLayout = (AbsLineLayout) getLineLayoutContainer().getChildAt(mLongClickBlankRow);
		final int count = curLineLayout.getChildCount();
		
		//准备DOCK长按图标添加层
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
				IDiyFrameIds.DOCK_ADD_ICON_FRAME, null, null);
		
		//初始化添加层数据
		GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_ADD_ICON_FRAME, IDiyMsgIds.DOCK_ADD_ICON_INIT, 
				mLongClickBlankIndexInRow, count, null);
		return false;
	}

	@Override
	protected void addApplication(Object object) {
		if ((object != null) && (object instanceof AppItemInfo)) {
			AppItemInfo appItemInfo = (AppItemInfo) object;

			AbsLineLayout lineLayout = (AbsLineLayout) getLineLayoutContainer().getChildAt(
					mLongClickBlankRow);
			int iconsize = lineLayout.getChildCount() == DockUtil.ICON_COUNT_IN_A_ROW - 1
					? DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW)
					: DockUtil.getIconSize(1);
			DockItemInfo dockItemInfo = createDockItemInfo(appItemInfo, mLongClickBlankRow,
					mLongClickBlankIndexInRow, iconsize);

			if (dockItemInfo != null) {
				mCurDockViewList = mIconViewsHashMap.get(mLongClickBlankRow);
				refShortcutItem(dockItemInfo, lineLayout.getChildCount(), mLongClickBlankRow,
						false, null);
			}
		}
		mLongClickBlankIndexInRow++;
		
		//通知dock添加图标层添加图标完成，可以继续点击添加下一个图标
		sendDockAddFrameAddFinish();
	}

	@Override
	protected void addShortcut(Object object) {
		if ((object != null) && (object instanceof ShortCutInfo)) {
			ShortCutInfo shortCutInfo = (ShortCutInfo) object;

			AbsLineLayout lineLayout = (AbsLineLayout) getLineLayoutContainer().getChildAt(
					mLongClickBlankRow);
			int iconsize = lineLayout.getChildCount() == DockUtil.ICON_COUNT_IN_A_ROW - 1
					? DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW)
					: DockUtil.getIconSize(1);

			DockItemInfo dockInfo = createDockItemInfo(shortCutInfo, mLongClickBlankRow,
					mLongClickBlankIndexInRow, iconsize);
			if (dockInfo != null) {
				mCurDockViewList = mIconViewsHashMap.get(mLongClickBlankRow);
				refShortcutItem(dockInfo, lineLayout.getChildCount(), mLongClickBlankRow, false,
						null);
			}
		}
		mLongClickBlankIndexInRow++;
		
		//通知dock添加图标层添加图标完成，可以继续点击添加下一个图标
		sendDockAddFrameAddFinish();
	}

	/**
	 * <br>功能简述:通知dock添加图标层添加图标完成，可以继续点击添加下一个图标
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void sendDockAddFrameAddFinish() {
		GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_ADD_ICON_FRAME, IDiyMsgIds.DOCK_ADD_ICON_ADD_FINISH, 
				-1, 0, null);
	}
	
	private int getClickBlankIndex() {
		Point downPoint = getLineLayoutContainer().getDownPoint();
		AbsLineLayout curLineLayout = getLineLayoutContainer().getCurLineLayout();
		int index = -1;
		if (downPoint != null && curLineLayout != null) {
			index = curLineLayout.getChildCount();
			if (sPortrait) {
				for (int i = 0; i < curLineLayout.getChildCount(); i++) {
					View iconView = curLineLayout.getChildAt(i);
					if (downPoint.x < iconView.getLeft()) {
						index = i;
						break;
					}
				}
			} else {
				for (int i = 0; i < curLineLayout.getChildCount(); i++) {
					View iconView = curLineLayout.getChildAt(i);
					if (downPoint.y > iconView.getBottom()) {
						index = i;
						break;
					}
				}
			}
		}
		return index;
	}

	@Override
	public void onDefaultIconsClick(ArrayList<ShortCutInfo> infos) {
		if (infos != null) {
			for (ShortCutInfo shortCutInfo : infos) {
				addShortcut(shortCutInfo);
				mLongClickBlankIndexInRow++;
			}
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		
	}
	
	@Override
	public void configurationChange() {
		
	}
}