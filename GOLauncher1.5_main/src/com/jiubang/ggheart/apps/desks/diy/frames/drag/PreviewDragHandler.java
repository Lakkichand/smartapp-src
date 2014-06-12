package com.jiubang.ggheart.apps.desks.diy.frames.drag;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;
import com.gau.go.launcherex.R;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.CardLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.SensePreviewFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Workspace;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.components.DeskToast;
/**
 * 预览层拖动事件处理类
 * 
 * @author yuankai
 * @version 1.0
 */
public class PreviewDragHandler extends AbstractDragHandler {
	@SuppressWarnings("unused")
	private final static String TAG = "previewdrag";

	// 特殊需求
	// 第一次切屏要延时
	private boolean mFirstSnapScreen = true;

	// 提示框
	// 操作延时
	private static final int REQUEST_TIME_OUT = 3;
	private final static long TIME_OUT_DURATION = 2000;
	private final static int TIP_TIMES = 5;

	// 状态
	private final static int STATE_NONE = 0;
	private final static int STATE_ENLARGE = 1;
	private final static int STATE_RED = 2;
	private final static int STATE_ADD = 3;
	private int mState;

	// Card矩形区域列表
	private List<Rect> mCardRects = new ArrayList<Rect>();
	// 放大
	private Rect mEnlargeRect;
	private int mEnlargeIndex = -1;

	// private DragFrame mDragFrame;
	protected PreviewDragHandler(int type, DragFrame dragFrame, IFrameManager frameManager,
			View draggedView) {
		super(type, dragFrame, frameManager, draggedView);
		// mDragFrame = dragFrame;
		mDragListener = IDiyFrameIds.SCREEN_FRAME;

		// 需求
		// 一进入预览拖动时，如果用户两秒还没作选择则提示前五次
		if (isNeedStartTimeout()) {
			mHandler.sendEmptyMessageDelayed(REQUEST_TIME_OUT, TIME_OUT_DURATION);
		}
	}

	@Override
	public void selfDestruct() {
		super.selfDestruct();

		mCardRects.clear();
		mCardRects = null;

		mEnlargeRect = null;

		mHandler.removeMessages(REQUEST_TIME_OUT);
		mHandler = null;
	}

	boolean isNeedStartTimeout() {
		int tipTime = new PreferencesManager(GoLauncher.getContext(),
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE).getInt(
				IPreferencesIds.PREVIEW_DRAG_TIP_TIME, 0);
		return tipTime < TIP_TIMES;
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case REQUEST_TIME_OUT : {
					makeDragTip();
				}
					break;

				default :
					break;
			}
		};
	};

	synchronized void makeDragTip() {
		int tipTime = new PreferencesManager(GoLauncher.getContext(),
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE).getInt(
				IPreferencesIds.PREVIEW_DRAG_TIP_TIME, 0);
		final int remainTipTimes = TIP_TIMES - tipTime;
		if (remainTipTimes > 0) {
			// DeskToast.makeText(mFrame.getActivity(),
			// mFrame.getActivity().getString(
			// R.string.screen_preview_drag_tip), Toast.LENGTH_SHORT).show();
			tipTime++;
			PreferencesManager sp = new PreferencesManager(GoLauncher.getContext(),
					IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
			sp.putInt(IPreferencesIds.PREVIEW_DRAG_TIP_TIME, tipTime);
			sp.commit();
		}
	}

	@Override
	public void leaveImmediatly() {
		super.leaveImmediatly();

		GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
				IDiyFrameIds.SCREEN_PREVIEW_FRAME, null, null);

		GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
				mFrame.getId(), null, null);
	}

	public void updateRects(List<Rect> rects) {
		mCardRects.clear();
		if (null != rects) {
			mCardRects = rects;
		}

	}

	private void enlargeCard(int index) {
		// 要求预览层放大指定索引的卡片
		Rect rect = new Rect();
		boolean success = GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_ENLARGE_CARD, index, rect, null);
		if (success) {
			// 获取到了正确的放大信息
			mEnlargeRect = rect;
			mState = STATE_ENLARGE;
			mEnlargeIndex = index;
		}
	}

	private void resumeCard() {
		boolean success = GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_RESUME_CARD, mEnlargeIndex, null, null);
		if (success) {
			mState = STATE_NONE;
			mEnlargeRect = null;
			mEnlargeIndex = -1;
		}
	}

	/*
	 * 让cardLayout变成一张卡片
	 */
	private void resumeNormal(boolean dragfinish) {
		boolean success = GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.CARDLAYOUTTURN_TO_NORMAL, mEnlargeIndex, dragfinish, null);
		if (success) {
			mState = STATE_NONE;
			mEnlargeRect = null;
			mEnlargeIndex = -1;
		}
	}

	/*
	 * 让cardLayout变成一个加号
	 */
	private void resumeAdd() {
		boolean success = GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.TURNCARD_TO_ADD, mEnlargeIndex, null, null);
		if (success) {
			mState = STATE_NONE;
			mEnlargeRect = null;
			mEnlargeIndex = -1;
		}
	}

	private boolean canSnapToNextScreen() {
		return GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_CAN_SNAP_NEXT, -1, null, null);
	}

	private boolean canSnapToPreScreen() {
		return GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_CAN_SNAP_PRE, -1, null, null);
	}

	private void snapToNextScreen() {
		resumeCard();
		GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_SNAP_NEXT, -1, null, mCardRects);
	}

	private void snapToPreScreen() {
		resumeCard();
		GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_SNAP_PRE, -1, null, mCardRects);
	}

	@Override
	public void onFingerPointF(IDragObject obj, float x, float y) {

	}

	/*** =====延迟1秒退回到主页面==== */
	private static final int BACK_TO_WORKSPACE_DELAY = 1000;
	private static final int BACKTOWORKSPACE = 0;
	private int mScreenIndex = -1;
	private ToWorkspaceRunnable mToWorkspaceRunnable;

	private void startTimer(int index) {
		if (mScreenIndex == index) {
			return;
		}
		mScreenIndex = index;
		clearTimer();
		mToWorkspaceRunnable = new ToWorkspaceRunnable();
		mHandler.postDelayed(mToWorkspaceRunnable, BACK_TO_WORKSPACE_DELAY);
	}

	private void clearTimer() {
		mScreenIndex = -1;
		if (mToWorkspaceRunnable != null) {
			mHandler.removeCallbacks(mToWorkspaceRunnable);
		}
	}

	/***
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  maxiaojun
	 * @date  [2012-9-19]
	 */
	private class ToWorkspaceRunnable implements Runnable {
		public ToWorkspaceRunnable() {
		}

		@Override
		public void run() {
			Message message = Message.obtain();
			message.what = BACKTOWORKSPACE;
			message.arg1 = mScreenIndex;
			mToWorkspaceHandler.sendMessage(message);
		}
	}

	private Handler mToWorkspaceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case BACKTOWORKSPACE :
					if (mFrame == null || mFrame.getContentView() == null) {
						return;
					}
					// 获取屏的绝对索引号
					Bundle bundle = new Bundle();
					bundle.putInt(SensePreviewFrame.FIELD_ABS_INDEX, mEnlargeIndex);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
							IDiyMsgIds.PREVIEW_GET_ABS_SCREEN_INDEX, -1, bundle, null);
					final int screenIndex = bundle.getInt(SensePreviewFrame.FIELD_ABS_INDEX);
					// 退出预览层前的处理
					doFinishState(screenIndex);
					// 退出屏幕预览层
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
							IDiyMsgIds.PREVIEW_LEAVE_ANIMATE, screenIndex, null, null);
					boolean trashGone = mFrame.isTrashGone();
					// 从屏幕预览拖拽转换到屏幕拖拽
					mFrame.previewToScreenSwitchHandle(mDraggedView);
					// 修复bug：从桌面文件夹中拖出的图标经预览页面，然后返回桌面，放手后图标消失。
					mFrame.setFolderReplace(false);

					if (trashGone) {
						GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME,
								IDiyMsgIds.TRASH_GONE, -1, null, null);
					} else {
						if (!StatusBarHandler.isHide()) {
							// 要求显示全屏并重新排版
							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, true, null);
						}
						// 显示垃圾箱
						GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME,
								IDiyMsgIds.SHOW_TRASH_DURING_DRAGING, -1, null, null);
						// 隐藏指示器
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.HIDE_INDICATOR, -1, null, null);
					}
					// 刷新桌面网格
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.CHECK_GRID_STATE, screenIndex, null, null);
					break;
			}
		}
	};

	/**
	 * 退出预览层前的处理
	 * 
	 * @param screenIndex
	 */
	private void doFinishState(int screenIndex) {
		switch (mState) {
			case STATE_ADD :// 如果放在+上，需先添加一个屏幕
				// 添加一个屏幕
				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						IDiyMsgIds.ADD_NEW_CARD, screenIndex, true, null);
				// 为加号的layout创建mOccupied
				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.NEW_ADD_MOCCUPIED, screenIndex, null, null);
				break;
			case STATE_RED :
				resumeNormal(true);
				break;
		}
	}

	@Override
	public void onCenterPointF(IDragObject obj, float x, float y, int dragTpye) {

		switch (mState) {
			case STATE_NONE : {
				final int count = mCardRects.size();

				for (int i = 0; i < count; i++) {
					Rect cardRect = mCardRects.get(i);
					if (i == 2 || i == 5 || i == 8) {
						cardRect.right = cardRect.right - 3;
					}
					if (cardRect.contains((int) x, (int) y)) {

						boolean isadd = GoLauncher.sendMessage(mFrame,
								IDiyFrameIds.SCREEN_PREVIEW_FRAME, IDiyMsgIds.IS_ADD_CARD, i, null,
								null);
						if (isadd) {
							// 如果是加号,则让这个加号变为一个屏幕
							GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
									IDiyMsgIds.TURNADD_TO_CARD, i, cardRect, null);
							mState = STATE_ADD;
							mEnlargeIndex = i;
							mEnlargeRect = cardRect;
							startTimer(i);
							break;
						}
						startTimer(i);
						// boolean enough = GoLauncher.sendMessage(mFrame,
						// IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						// IDiyMsgIds.IS_SET_CONTENT, i, mDraggedView,
						// null);
						//
						// if(!enough){
						// //让这个图片变红
						// GoLauncher.sendMessage(mFrame,
						// IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						// IDiyMsgIds.CARDLAYOUTTURN_TO_RED, i, cardRect, null);
						// //显示垃圾
						// //发消息显示垃圾箱
						// mState = STATE_RED;
						// mEnlargeIndex = i;
						// mEnlargeRect = cardRect;
						// break;
						// }
						// 否则放大
						enlargeCard(i);
						break;
					}
					if (i == 2 || i == 5 || i == 8) {
						cardRect.right = cardRect.right + 3;
					}
				}
			}
				break;

			case STATE_ENLARGE : {
				if (mEnlargeRect != null && !mEnlargeRect.contains((int) x, (int) y)) {
					clearTimer();
					// 取消放大的卡片
					resumeCard();
				}
			}
				break;

			case STATE_RED : {
				if (mEnlargeRect != null && !mEnlargeRect.contains((int) x, (int) y)) {
					clearTimer();
					resumeNormal(false);
				}
			}
				break;
			case STATE_ADD : {
				// 变为加号
				if (mEnlargeRect != null && !mEnlargeRect.contains((int) x, (int) y)) {
					clearTimer();
					resumeAdd();
				}
			}
				break;
			default :
				break;
		}
	}

	@Override
	public void onEnterEdge(IDragObject obj, int edgeType) {

	}

	@Override
	public void onLeaveEdge(IDragObject obj, int edgeType) {

	}

	@Override
	public void onEdge(IDragObject obj, int edgeType) {
		// 第一次略过，产品需求
		if (mFirstSnapScreen) {
			mFirstSnapScreen = false;
			return;
		}

		if ((edgeType & IDragListener.EDGE_LEFT) == IDragListener.EDGE_LEFT) {
			if (canSnapToPreScreen()) {
				snapToPreScreen();
			}
		}

		if ((edgeType & IDragListener.EDGE_RIGHT) == IDragListener.EDGE_RIGHT) {
			if (canSnapToNextScreen()) {
				snapToNextScreen();
			}
		}
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  maxiaojun
	 * @date  [2012-9-19]
	 */
	class ResetStatusThread extends Thread {
		@Override
		public void run() {
			// 要求显示全屏并重新排版
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);

			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_REQUEST_LAYOUT, Workspace.CHANGE_SOURCE_INDICATOR, 0, null);
			ScreenFrame.sForceHide = false;

			if (mDraggedView.getVisibility() != View.VISIBLE) {
				mDraggedView.setVisibility(View.VISIBLE);
			}
			//			// 显示指示器
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.DISPLAY_INDICATOR_IMMEDIATELY, -1, false, null);
		}
	};

	@Override
	public void onDragFinish(IDragObject obj, float x, float y) {
		clearTimer();
		switch (mState) {
			case STATE_NONE :
				int dragType = mFrame.getDragType();
				if (dragType != DragFrame.TYPE_APPFUNC_FOLDERITEM_DRAG
						&& dragType != DragFrame.TYPE_APPFUNC_ITEM_DRAG) {
					// 提示用户操作失败
					DeskToast.makeText(mFrame.getActivity(),
							mFrame.getActivity().getString(R.string.fail_drag_to_screen),
							Toast.LENGTH_SHORT).show();
				}
				if (DragFrame.TYPE_DOCK_DRAG == dragType
						|| DragFrame.TYPE_DOCK_FOLDERITEM_DRAG == dragType) {
					GoLauncher.sendMessage(mFrame, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DRAG_CANCEL,
							dragType, mDraggedView, null);
				}
				GoLauncher
						.sendMessage(mFrame, IDiyFrameIds.SCHEDULE_FRAME,
								IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
								null, null);

				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DRAG_CANCEL,
						-1, mDraggedView, null);

				// 未移到任何一个上面，则取消本次操作
				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, mFrame.getId(), null, null);

				if (!SensePreviewFrame.sScreenFrameStatus
						&& SensePreviewFrame.isEnterFromDragView()) {
					SensePreviewFrame.setIsEnterFromDragView(false);
					if (mDraggedView.getVisibility() == View.VISIBLE) {
						mDraggedView.setVisibility(View.INVISIBLE);
					}
					ScreenFrame.sForceHide = true;
					// 先隐藏指示器(透明通知栏的bug)
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.HIDDEN_INDICATOR, -1, false, null);

					ResetStatusThread resetStatusAndIndicator = new ResetStatusThread();
					mDraggedView.postDelayed(resetStatusAndIndicator, 100);
				}

				if (CardLayout.sDrawRoom) {
					CardLayout.sDrawRoom = false;
				}
				break;

			case STATE_ENLARGE : {
				final Rect imageRect = new Rect(obj.getLeft(), obj.getTop(), obj.getRight(),
						obj.getBottom());
				// 相对于屏的位置信息
				Rect rect = getRelativeRect(mEnlargeRect, imageRect);

				// 获取屏的绝对索引号
				Bundle bundle = new Bundle();
				bundle.putInt(SensePreviewFrame.FIELD_ABS_INDEX, mEnlargeIndex);
				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						IDiyMsgIds.PREVIEW_GET_ABS_SCREEN_INDEX, -1, bundle, null);
				final int screenIndex = bundle.getInt(SensePreviewFrame.FIELD_ABS_INDEX);

				// 本界面中移除
				mFrame.removeView(mDraggedView);

				// 将结果信息发送给屏幕层
				List<Rect> list = new ArrayList<Rect>();
				list.add(rect);

				final boolean enterFromDrag = !SensePreviewFrame.sScreenFrameStatus
						&& SensePreviewFrame.isEnterFromDragView();
				if (enterFromDrag) {
					// 显示指示器
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.DISPLAY_INDICATOR, -1, false, null);
				}
				boolean bool = GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.DRAG_OVER, screenIndex, mDraggedView, list);
				doAfterAddToScreen(bool);
				// 要求预览层作离开动画
				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						IDiyMsgIds.PREVIEW_LEAVE_ANIMATE, screenIndex, null, null);

				// 将选取信息发送给屏幕层并进入屏幕层
				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.DRAG_FRAME, null, null);

				if (CardLayout.sDrawRoom) {
					CardLayout.sDrawRoom = false;
				}
				if (enterFromDrag) {
					SensePreviewFrame.setIsEnterFromDragView(false);
					// SensePreviewFrame.previewOperate = true;
					// 要求显示全屏并重新排版
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
				}
			}
				break;

			case STATE_RED : {
				resumeNormal(true);
				// if (!SensePreviewFrame.screenFrameStatus
				// && SensePreviewFrame.isEnterFromDragView()) {
				// SensePreviewFrame.setIsEnterFromDragView(false);
				// // SensePreviewFrame.previewOperate = true;
				// // 要求显示全屏并重新排版
				// // GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				// // IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
				//
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_REQUEST_LAYOUT, Workspace.CHANGE_SOURCE_INDICATOR, 0,
						null);
				// 显示指示器
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.DISPLAY_INDICATOR, -1, false, null);
				// }
			}
				break;

			case STATE_ADD : {
				final Rect imageRect = new Rect(obj.getLeft(), obj.getTop(), obj.getRight(),
						obj.getBottom());
				// 相对于屏的位置信息
				Rect rect = getRelativeRect(mEnlargeRect, imageRect);
				// 获取屏的绝对索引号
				Bundle bundle = new Bundle();
				bundle.putInt(SensePreviewFrame.FIELD_ABS_INDEX, mEnlargeIndex);

				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						IDiyMsgIds.PREVIEW_GET_ABS_SCREEN_INDEX, -1, bundle, null);
				final int screenIndex = bundle.getInt(SensePreviewFrame.FIELD_ABS_INDEX);
				boolean stateAdd = true;
				// 添加一个屏幕
				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						IDiyMsgIds.ADD_NEW_CARD, screenIndex, stateAdd, null);
				// 为加号的layout创建mOccupied
				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.NEW_ADD_MOCCUPIED, screenIndex, null, null);

				// 本界面中移除
				mFrame.removeView(mDraggedView);
				// 将结果信息发送给屏幕层
				List<Rect> list = new ArrayList<Rect>();
				list.add(rect);
				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DRAG_OVER,
						screenIndex, mDraggedView, list);
				// 要求预览层作离开动画
				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						IDiyMsgIds.PREVIEW_LEAVE_ANIMATE, screenIndex, null, null);
				// 将选取信息发送给屏幕层并进入屏幕层
				GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.DRAG_FRAME, null, null);

				// 删除屏幕上原来图标
				if (null != mDraggedView && mDraggedView instanceof DockIconView) {
					DockIconView dockIconView = (DockIconView) mDraggedView;
					if (null != dockIconView.getInfo() && null != dockIconView.getInfo().mItemInfo) {
						GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
								IDiyMsgIds.DELETE_DOCK_ITEM, -1, mDragItemId, null);
					}
				}

			}
				if (CardLayout.sDrawRoom) {
					CardLayout.sDrawRoom = false;
				}
				if (!SensePreviewFrame.sScreenFrameStatus
						&& SensePreviewFrame.isEnterFromDragView()) {
					SensePreviewFrame.setIsEnterFromDragView(false);
					// SensePreviewFrame.previewOperate = true;
					// 要求显示全屏并重新排版
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
				}
				break;
			default :
				break;
		}
	}

	private void doAfterAddToScreen(boolean bool) {
		if (bool) {
			switch (mFrame.getDragTypeBeforePreview()) {
				case DragFrame.TYPE_DOCK_DRAG :
					DockIconView dockIconView = (DockIconView) mDraggedView;
					if (null != dockIconView && null != dockIconView.getInfo()
							&& null != dockIconView.getInfo().mItemInfo) {
						GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
								IDiyMsgIds.DELETE_DOCK_ITEM, -1, mDragItemId, null); // 删除屏幕上原来图标
					}
					break;

				case DragFrame.TYPE_DOCK_FOLDERITEM_DRAG :
					// 把旧inscreenid传过去，是因为加到screen时，iteminfo的inscreenid已经改变，必须用旧id才可以删除之前dock中的数据
					ArrayList<Long> list = new ArrayList<Long>();
					list.add(mDragItemId);

					GoLauncher.sendMessage(mFrame, IDiyFrameIds.DOCK_FRAME,
							IDiyMsgIds.DOCK_DELETE_FOLDERITEM, -1, mDraggedView, list);
					list.clear();
					list = null;
					break;

				default :
					break;
			}
		}
	}

	// 获取抓取放下的相对区域
	Rect getRelativeRect(Rect enlargeRect, Rect imageRect) {
		final float leftRate = (float) (imageRect.left + imageRect.width() / 2 - enlargeRect.left)
				/ enlargeRect.width();
		final float topRate = (float) (imageRect.top + imageRect.height() / 2 - enlargeRect.top)
				/ enlargeRect.height();

		final DisplayMetrics metrics = mFrame.getScreenSize();

		// 排除横屏右端dock条宽度
		int worspaceW = 0;
		if (GoLauncher.isPortait()) {
			worspaceW = metrics.widthPixels;
		} else {
			worspaceW = metrics.widthPixels - DockUtil.getBgHeight();
		}

		final int left = (int) (leftRate * worspaceW) - mDraggedView.getWidth() / 2;
		final int top = (int) (topRate * metrics.heightPixels) - mDraggedView.getHeight() / 2;
		final int right = left + mDraggedView.getWidth();
		final int bottom = top + mDraggedView.getHeight();

		return new Rect(left, top, right, bottom);
	}

	@Override
	public void onDragMove(IDragObject obj, float x, float y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDragFinish() {
		super.onDragFinish();
		int dragType = mFrame.getDragType();

		if (dragType != DragFrame.TYPE_APPFUNC_FOLDERITEM_DRAG
				&& dragType != DragFrame.TYPE_APPFUNC_ITEM_DRAG) {
			// 提示用户操作失败
			DeskToast.makeText(mFrame.getActivity(),
					mFrame.getActivity().getString(R.string.fail_drag_to_screen),
					Toast.LENGTH_SHORT).show();
		}

		if (DragFrame.TYPE_DOCK_DRAG == dragType || DragFrame.TYPE_DOCK_FOLDERITEM_DRAG == dragType) {
			GoLauncher.sendMessage(mFrame, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DRAG_CANCEL,
					dragType, mDraggedView, null);
		}

		GoLauncher.sendHandler(mFrame, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
				IDiyFrameIds.SCREEN_PREVIEW_FRAME, null, null);
		GoLauncher.sendMessage(mFrame, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DRAG_CANCEL, -1,
				mDraggedView, null);

		// 未移到任何一个上面，则取消本次操作
		GoLauncher.sendHandler(mFrame, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
				mFrame.getId(), null, null);
		if (dragType == DragFrame.TYPE_APPFUNC_ITEM_DRAG
				|| dragType == DragFrame.TYPE_APPFUNC_FOLDERITEM_DRAG) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
					IDiyFrameIds.APPFUNC_FRAME, null, null);
			// 提示用户操作失败
			DeskToast.makeText(GoLauncher.getContext(),
					GoLauncher.getContext().getString(R.string.fail_drag_to_screen),
					Toast.LENGTH_SHORT).show();
		}
		if (!SensePreviewFrame.sScreenFrameStatus && SensePreviewFrame.isEnterFromDragView()) {
			SensePreviewFrame.setIsEnterFromDragView(false);
			// SensePreviewFrame.previewOperate = true;
			// 显示非全屏并重新排版
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
		}

		if (CardLayout.sDrawRoom) {
			CardLayout.sDrawRoom = false;
		}
	}

	@Override
	public void focusTrash(IDragObject obj) {
		// TODO Auto-generated method stub

	}
}
