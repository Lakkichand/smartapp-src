package com.jiubang.ggheart.apps.desks.diy.frames.drag;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.go.util.graphics.BitmapUtility;
import com.go.util.window.OrientationControl;
import com.go.util.window.WindowControl;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppIconForMsg;
import com.jiubang.ggheart.apps.desks.appfunc.model.FolderIconForMsg;
import com.jiubang.ggheart.apps.desks.diy.DiyScheduler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.MyDragFrame.DragFrameListener;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.SensePreviewFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Workspace;
import com.jiubang.ggheart.components.BubbleTextView;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 编辑拖动层,用于屏幕的拖动响应和回调
 * 
 * @author yuankai
 * @version 1.0
 */

public class DragFrame extends AbstractFrame implements ISelfObject, DragFrameListener {
	// 当前的处理器
	public final static int TYPE_SCREEN_HANDLER = 1;
	public final static int TYPE_PREVIEW_HANDLER = 2;

	// 拖动类型
	public final static int TYPE_PREVIEW_DRAG = -1; // 屏幕预览层的拖拽
	public final static int TYPE_SCREEN_FOLDER_DRAG = 1; // 屏幕层文件夹里面的图标
	public final static int TYPE_DOCK_DRAG = 2; // DOCK条的图标（包括文件夹）
	public final static int TYPE_DOCK_FOLDERITEM_DRAG = 3; // DOCK条文件夹里面的图标
	public final static int TYPE_SCREEN_ITEM_DRAG = IDiyFrameIds.SCREEN_FRAME; // 屏幕层的图标（包括文件夹）
	public final static int TYPE_APPFUNC_ITEM_DRAG = 4; // 功能表的图标
	public final static int TYPE_APPFUNC_FOLDERITEM_DRAG = 5; // 功能表文件夹里面的图标
	public final static int TYPE_ADD_APP_DRAG = 6; // 桌面添加，添加app到桌面
	public final static int TYPE_ADD_ITEM_IN_FOLDER = 7; // 桌面添加，桌面新建文件夹，点击图标飞到文件夹中
	public final static int TYPE_ADD_WIDGET_DRAG = 8; // 桌面添加，添加widget到桌面
	public final static int TYPE_DOCK_ADD_ICON = 9; // dock添加添加图标
	
	private AbstractDragHandler mCurHandler;

	// 真实视图
	private View mTargetView;

	// Drag
	private Bitmap mDragBitmap;
	private IDragObject mDragObject;
	private DragLayout mDragLayout;
	private MyDragFrame mMyDragFrame;
	private int mDragType; // 拖动类型
	private int mDragTypeBeforePreview; // 拖拽进入屏幕预览前的拖拽类型

	private MotionEvent mEvent;
	
	public static float sScaleFactor; // 拖动图片与原图片的scale值
	private Rect mTempRect = null; // 拖拽进入屏幕预览时的缩放比例

	//指示器是否在上方
	private boolean mIsIndicatorOnTop = true;
			
	public DragFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);
		selfConstruct();

		GoSettingControler controler = GOLauncherApp.getSettingControler();
		ScreenSettingInfo mScreenInfo = controler.getScreenSettingInfo();
		mIsIndicatorOnTop = mScreenInfo.mIndicatorPosition
				.equals(ScreenIndicator.INDICRATOR_ON_TOP);
		// 显示桌面，隐藏dock条
		mFrameManager.setFrameVisiable(IDiyFrameIds.SCREEN_FRAME, View.VISIBLE);
		//指示器在上方，就隐藏
		if (mIsIndicatorOnTop) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.HIDE_INDICATOR, -1,
					null, null);
		}
	}

	@Override
	public void selfConstruct() {
		mDragLayout = (DragLayout) mActivity.getLayoutInflater().inflate(R.layout.drag_frame, null);
		mMyDragFrame = new MyDragFrame(mActivity);
		mMyDragFrame.setContentView(mDragLayout);
		mMyDragFrame.setDragFrameListener(this);
	}

	@Override
	public void selfDestruct() {
		if (null != mCurHandler) {
			mMyDragFrame.unregister(mCurHandler);
			mCurHandler.selfDestruct();
			mCurHandler = null;
		}

		mMyDragFrame.selfDestruct();
		mDragLayout.selfDestruct();
		if (null != mDragObject) {
			if (mDragObject instanceof DragView) {
				((DragView) mDragObject).selfDestruct();
			} else if (mDragObject instanceof DragImage) {
				((DragImage) mDragObject).selfDestruct();
			}
			mDragObject = null;
		}
		if (null != mDragBitmap && !mDragBitmap.isRecycled()) {
			mDragBitmap.recycle();
		}
		mDragBitmap = null;
		mTargetView = null;
	}

	@Override
	public void onForeground() {
		super.onForeground();

		// 需要时才注册
		mFrameManager.registDispatchEvent(this);
	}

	@Override
	public void onBackground() {
		super.onBackground();

		// 需要时才注册
		mFrameManager.unRegistDispatchEvent(this);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		super.handleMessage(who, type, msgId, param, object, objects);
		boolean ret = false;
		switch (msgId) {
			case IDiyMsgIds.DRAG_START : {
				if (mCurHandler == null || mCurHandler.mType != TYPE_SCREEN_HANDLER) {
					// 处理屏幕上图标的拖动
					if (object != null && object instanceof View) {
						mDragType = param;
						View view = (View) object;
						float scale = 1.0f;
						int marginLeft = 0;
						int marginTop = 0;
						if (objects != null && objects.size() >= 3) {
							scale = (Float) objects.get(0);
							marginLeft = (Integer) objects.get(1);
							marginTop = (Integer) objects.get(2);
						}
						/**
						 * change by dengdazhong date 2012.07.30
						 * 4.1系统效果，桌面图标点击放大
						 */
						else {
							scale = (view instanceof BubbleTextView || view instanceof DockIconView)
									? 1.15f/* 普通图标和dock条图标 */
									: 1.05f/* widgets */;
							marginLeft = (int) (view.getWidth() * (scale - 1) / 2);
						}
						// 创建缩放位图
						ret = handleScreenDrag(view, scale, marginLeft, marginTop);
					}
				}
			}
				break;

			case IDiyMsgIds.START_TO_AUTO_FLY : {
				// 处理屏幕上图标飞
				if (object != null && object instanceof View) {
					mMyDragFrame.setFlyState();
					mDragType = param;
					if (objects instanceof ArrayList && objects.size() == 2) {
						ret = handleAutoFly((View) object, new int[] { (Integer) objects.get(0),
								(Integer) objects.get(1) }, param);
					}
				}
			}
			
			    break;
			case IDiyMsgIds.TRASH_GONE : {
				mMyDragFrame.setTrashGone();
				if (mCurHandler != null) {
					mCurHandler.setTrashGone();
				}
			}
				break;

			case IDiyMsgIds.TRASH_VISIBLE : {
				mMyDragFrame.setTrashVisible();
			}
				break;
			case IDiyMsgIds.IS_TRASH_GONE : {
				ret = isTrashGone();
			}
				break;

			case IDiyMsgIds.APPFUNC_DRAG_START :
				if (mCurHandler == null || mCurHandler.mType != TYPE_PREVIEW_HANDLER) {
					// change by dengdazhong 2013.2.21
					// ADT-10831 从功能表拖出图标到dock条，合成的文件夹背景位置不对
					if (!StatusBarHandler.isHide() && Workspace.getLayoutScale() >= 1.0f) {
						// 要求显示全屏并重新排版
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, true, null);
					}
					if (type == IMsgType.SYNC && object != null && object instanceof AppIconForMsg) {
						mDragType = TYPE_APPFUNC_ITEM_DRAG;
						handleAppFuncAppDrag(param, (AppIconForMsg) object);
						ret = true;
					} else if (type == IMsgType.SYNC && object != null
							&& object instanceof FolderIconForMsg) {
						mDragType = TYPE_APPFUNC_FOLDERITEM_DRAG;
						handleAppFuncFolderDrag(param, (FolderIconForMsg) object);
						ret = true;
					}
				}
				break;

			case IDiyMsgIds.PREVIEW_LOAD_COMPLETE :
				doPreviewLoadComplete();
				break;

			case IDiyMsgIds.SCREEN_FOLDER_AREA_LIST :
				if (null != mCurHandler && mCurHandler instanceof ScreenDragHandler) {
					((ScreenDragHandler) mCurHandler).updateFolderRects(objects);
				}
				break;

			case IFrameworkMsgId.SYSTEM_HOME_CLICK :
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED :
			case IDiyMsgIds.SHOW_UPDATE_DIALOG :
			case IDiyMsgIds.BACK_TO_MAIN_SCREEN :
			case IFrameworkMsgId.SYSTEM_ON_PAUSE :
				if (null != mCurHandler) {
					if (mTargetView instanceof DockIconView) {
						GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
								IDiyMsgIds.DRAG_CANCEL, -1, mTargetView, null);
					}
					mCurHandler.leaveImmediatly();
				}
				ret = true;
				break;
			// case IDiyMsgIds.DRAG_MOVE:
			// if(mDragType==TYPE_DOCK_DRAG){
			// GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
			// IDiyMsgIds.DOCK_REMOVE_DRAG_ITEM, -1, null, null);
			// }
			case IDiyMsgIds.REPLACE_GET_DRAG_LOCATION : {
				if (object != null && object instanceof int[] && mDragObject != null) {
					int[] point = (int[]) object;
					point[0] = mDragObject.getLeft();
					point[1] = mDragObject.getTop();
				}
			}
				break;

			case IDiyMsgIds.SHOW_TRASH_DURING_DRAGING : {
				if (null != mCurHandler && mCurHandler instanceof ScreenDragHandler) {
					((ScreenDragHandler) mCurHandler).showTrash();
				}
			}
				break;

			case IDiyMsgIds.FINISH_DRAG_WHEN_NO_ROOM :
				if (mCurHandler != null) {
					mCurHandler.onDragFinish();
				}
				break;

			case IDiyMsgIds.SCALE_DRAG_FOR_MERGE_FOLDER : {
				if (null != mDragObject) {
					zoomDragViewForMergeFolder();
				}
			}
				break;

			case IDiyMsgIds.RESET_DRAG_FOR_MERGE_FOLDER : {
				if (null != mDragObject) {
					revertScaleDragViewForMergeFolder();
				}
			}
				break;

			case IDiyMsgIds.UPDATE_DRAG_POINTS : {
				if (null != objects) {
					if (mMyDragFrame != null) {
						final int x = (Integer) objects.get(0);
						final int y = (Integer) objects.get(1);
						mMyDragFrame.updateDragPoints(x, y);
					}
				}
			}
				break;

			case IDiyMsgIds.CHECK_SHOW_TRASH :
				if (null != mCurHandler && mCurHandler.mType == TYPE_SCREEN_HANDLER) {
					ret = ((ScreenDragHandler) mCurHandler).isShowTrash();
				}
				break;

			case IDiyMsgIds.CLOSE_SCREEN_DRAG_FRAME :
				if (mCurHandler instanceof ScreenDragHandler) {
					((ScreenDragHandler) mCurHandler).onFlyToDelFinish(mDragObject);
				}
				mCurHandler.leaveImmediatly();
				break;
			case IDiyMsgIds.SNAPSHOT_START:
			{
				if (null != mMyDragFrame) {
					mEvent.setAction(MotionEvent.ACTION_UP);
					mMyDragFrame.onTouchEvent(mEvent, mDragType);
				}
			}
				break;
			default :
				break;
		}
		return ret;
	}

	private void doPreviewLoadComplete() {
		// 获取各卡片区域
		List<Rect> rcList = new ArrayList<Rect>();
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_GET_CUR_CARDS_RECT, -1, null, rcList);

		// 第一次进入预览
		// if (null == mCurHandler || !(mCurHandler instanceof
		// PreviewDragHandler))
		// {
		// // 功能表
		// if (null != mDragData)
		// {
		// if (mDragData instanceof AppIconForMsg)
		// {
		// AppIconForMsg app = (AppIconForMsg)mDragData;
		// handleAppFuncAppDrag(app.mType, app);
		// }
		// else if (mDragData instanceof FolderIconForMsg)
		// {
		// FolderIconForMsg folder = (FolderIconForMsg)mDragData;
		// handleAppFuncFolderDrag(folder.mType, folder);
		// }
		// else
		// {
		// return;
		// }
		// mDragData = null;
		// }
		// }

		// 更新预览卡片区域
		if (null != mCurHandler && mCurHandler instanceof PreviewDragHandler) {
			if (rcList.size() > 0) {
				doResizeDragPreview(rcList.get(0));
			}
			((PreviewDragHandler) mCurHandler).updateRects(rcList);
		}
	}

	private void doResizeDragPreview(Rect rc) {
		IDragObject pDragObject = mMyDragFrame.getDragObject();
		Point pDragPointF = mMyDragFrame.getDragPoint();
		if (null == pDragObject || null == pDragPointF) {
			return;
		}

		// 屏幕客户区尺寸
		Rect screenRc = new Rect(rc);
		boolean bOk = getContentView().getGlobalVisibleRect(screenRc);
		if (!bOk) {
			return;
		}

		// 最小的尺寸
		int minWidth = 0;
		int minHeight = 0;
		if (rc.width() < rc.height()) // 竖屏
		{
			minWidth = mActivity.getResources().getDimensionPixelSize(R.dimen.cell_width_port);
			minHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.cell_height_port);
		} else {
			minWidth = mActivity.getResources().getDimensionPixelSize(R.dimen.cell_width_land);
			minHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.cell_height_land);
		}

		// 检验视图尺寸
		// 由于屏幕的放大效果
		if (pDragObject.getWidth() <= minWidth * 3 / 2
				&& pDragObject.getHeight() <= minHeight * 3 / 2) {
			return;
		}

		// 缩放比例
		int rate = 1000;

		int w_screenrate = rc.width() * rate / screenRc.width();
		int w_viewrate = minWidth * rate / pDragObject.getWidth();
		int w_rate = w_screenrate > w_viewrate ? w_screenrate : w_viewrate;

		int h_screenrate = rc.height() * rate / screenRc.height();
		int h_viewrate = minHeight * rate / pDragObject.getHeight();
		int h_rate = h_screenrate > h_viewrate ? h_screenrate : h_viewrate;

		// int wh_rate = w_rate > h_rate ? w_rate : h_rate;
		int wh_rate = w_rate < h_rate ? w_rate : h_rate;

		// 重新排版
		int w = pDragObject.getWidth() * wh_rate / rate;
		int h = pDragObject.getHeight() * wh_rate / rate;
		// int l = mDragObject.getLeft() + (mDragObject.getWidth() - w) / 2;
		// int t = mDragObject.getTop() + (mDragObject.getHeight() - h) / 2;
		// 居中到点击点, 防止抖动
		// 最好放到Object里面梳理
		int l = pDragPointF.x - w / 2;
		int t = pDragPointF.y - h / 2 - screenRc.top;
		int r = l + w;
		int b = t + h;

		float scale = (float) (wh_rate * 1.0 / 1000);
		// 记录进入屏幕预览前的size
		mTempRect = new Rect(pDragObject.getLeft(), pDragObject.getTop(), pDragObject.getRight(),
				pDragObject.getBottom());
		pDragObject.scale(scale, scale);
		pDragObject.layout(l, t, r, b);
		mMyDragFrame.setDragRect(new Rect(l, t, r, b));
	}

	@Override
	public ViewGroup getContentView() {
		return (ViewGroup) mMyDragFrame.getContentView();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		mEvent = event;
		return mMyDragFrame.onTouchEvent(event, mDragType);
	}

	/**
	 * 获取当前屏幕尺寸
	 * 
	 * @return 尺寸
	 */
	public DisplayMetrics getScreenSize() {
		DisplayMetrics metrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (mMyDragFrame.mIsFlying) {
				return true;
			}
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DRAG_CANCEL, -1,
					null, null);
			if (null != mCurHandler) {
				mCurHandler.leaveImmediatly();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mMyDragFrame.mIsFlying) {
			return true;
		}
		return false;
	}

	// private boolean handleAppFuncAppDrag(final int type, final AppIconForMsg
	// appIconForMsg)
	// {
	// if(type == IItemType.ITEM_TYPE_APPLICATION)
	// {
	// // 创建视图
	// mTargetView = DragData.createDragView(mActivity, appIconForMsg);
	// if (null == mTargetView)
	// {
	// return false;
	// }
	//
	// // 排版
	// RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
	// LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	// rlp.leftMargin = 0;
	// rlp.topMargin = 0;
	//
	// mTargetView.setLayoutParams(rlp);
	//
	// // 启动预览
	// enterPreview();
	//
	// // 切换响应类
	// mDragObject = new DragView(mTargetView);
	// Rect rc = new Rect(0, 0, appIconForMsg.mWidth, appIconForMsg.mHeight);
	// Point pt = new Point();
	// pt.x = appIconForMsg.mWidth / 2;
	// pt.y = appIconForMsg.mHeight / 2;
	// if(!WindowControl.getIsFullScreen(mActivity))
	// {
	// pt.y += StatusBarHandler.getStatusbarHeight();
	// }
	// mMyDragFrame.init(mDragObject, rc, pt);
	// mDragObject.setVisable(false);
	//
	// createHandler(TYPE_PREVIEW_HANDLER);
	// return true;
	// }
	// return false;
	// }

	private boolean handleAppFuncAppDrag(final int screenIndex, final AppIconForMsg appIconForMsg) {
		if (screenIndex > -1 && appIconForMsg != null) {
			// 创建视图
			mTargetView = DragData.createDragView(mActivity, appIconForMsg);
			if (null == mTargetView) {
				return false;
			}
			// 排版
			RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			rlp.leftMargin = 0;
			rlp.topMargin = 0;
			GoLauncher.sendHandler(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
					IDiyFrameIds.SCREEN_FRAME, null, null);
			GoLauncher.sendHandler(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_ENTER,
					screenIndex, -101, null);
			mDragObject = new DragView(getContentView(), mTargetView);

			final int startX = appIconForMsg.mX;
			final int startY = appIconForMsg.mY;
			Rect rc = new Rect(startX, startY, startX + appIconForMsg.mWidth, startY
					+ appIconForMsg.mHeight);
			Point pt = new Point();
			pt.x = startX + appIconForMsg.mWidth / 2;
			pt.y = startY + appIconForMsg.mHeight / 2;
			if (!WindowControl.getIsFullScreen(mActivity)) {
				pt.y += StatusBarHandler.getStatusbarHeight();
			}
			mMyDragFrame.init(mDragObject, rc, pt);
			// mDragObject.setVisable(false);
			createHandler(TYPE_SCREEN_HANDLER);
			// ScreenFrame screenFrame = (ScreenFrame) GoLauncher
			// .getFrame(IDiyFrameIds.SCREEN_FRAME);
			// if (screenFrame != null) {
			// screenFrame.sendFolderAreaInfo(screenIndex);
			// }
			return true;
		}
		return false;
	}

	// private boolean handleAppFuncFolderDrag(final int type, final
	// FolderIconForMsg folderIconForMsg)
	// {
	// if(type == IItemType.ITEM_TYPE_USER_FOLDER && folderIconForMsg != null)
	// {
	// // 创建视图
	// mTargetView = DragData.createDragView(mActivity, folderIconForMsg);
	// if (null == mTargetView)
	// {
	// return false;
	// }
	//
	// // 排版
	// RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
	// LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	// rlp.leftMargin = 0;
	// rlp.topMargin = 0;
	// mTargetView.setLayoutParams(rlp);
	//
	// // 启动预览
	// enterPreview();
	//
	// // 切换响应类
	// mDragObject = new DragView(mTargetView);
	// Rect rc = new Rect(0, 0, folderIconForMsg.mWidth,
	// folderIconForMsg.mHeight);
	// Point pt = new Point();
	// pt.x = folderIconForMsg.mWidth / 2;
	// pt.y = folderIconForMsg.mHeight / 2;
	// if(!WindowControl.getIsFullScreen(mActivity))
	// {
	// pt.y += StatusBarHandler.getStatusbarHeight();
	// }
	// mMyDragFrame.init(mDragObject, rc, pt);
	// mDragObject.setVisable(false);
	//
	// createHandler(TYPE_PREVIEW_HANDLER);
	// return true;
	// }
	// return false;
	// }

	private boolean handleAppFuncFolderDrag(final int screenIndex,
			final FolderIconForMsg folderIconForMsg) {
		if (screenIndex > -1 && folderIconForMsg != null) {
			// 创建视图
			mTargetView = DragData.createDragView(mActivity, folderIconForMsg);
			if (null == mTargetView) {
				return false;
			}
			// 排版
			RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			rlp.leftMargin = 0;
			rlp.topMargin = 0;
			mTargetView.setLayoutParams(rlp);

			GoLauncher.sendHandler(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
					IDiyFrameIds.SCREEN_FRAME, null, null);
			GoLauncher.sendHandler(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_ENTER,
					screenIndex, -101, null);
			mDragObject = new DragView(getContentView(), mTargetView);

			final int startX = folderIconForMsg.mX;
			final int startY = folderIconForMsg.mY;
			Rect rc = new Rect(startX, startY, startX + folderIconForMsg.mWidth, startY
					+ folderIconForMsg.mHeight);
			Point pt = new Point();
			pt.x = startX + folderIconForMsg.mWidth / 2;
			pt.y = startY + folderIconForMsg.mHeight / 2;

			if (!WindowControl.getIsFullScreen(mActivity)) {
				pt.y += StatusBarHandler.getStatusbarHeight();
			}
			mMyDragFrame.init(mDragObject, rc, pt);
			// mDragObject.setVisable(false);
			createHandler(TYPE_SCREEN_HANDLER);
			// ScreenFrame screenFrame = (ScreenFrame) GoLauncher
			// .getFrame(IDiyFrameIds.SCREEN_FRAME);
			// if (screenFrame != null) {
			// screenFrame.sendFolderAreaInfo(screenIndex);
			// }
			return true;
		}
		return false;
	}

	public void removeView(View view) {
		getContentView().removeView(view);
	}

	public void enterPreview() {
		// 把mTargetView传递给屏幕层
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SENDVIEWTOPREVIEW, -1,
				mTargetView, null);
		SensePreviewFrame.setIsEnterFromDragView(true);
		// 进入屏幕预览
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_SHOW_PREVIEW, 0,
				null, null);

		Bundle bundle = new Bundle();
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_SCROLL_DURATION, -1, bundle, null);
		int duration = bundle.getInt(SensePreviewFrame.FIELD_SCROLL_DURATION, 0);
		mMyDragFrame.setEdgeDuration(duration + MyDragFrame.EDGE_DURATION);
		bundle = null;

		// TODO 此处特殊处理，后期考虑修改,不能直接面对frameManager，而应该是面对scheduler调度层
		// 但是如果交给Scheduler层，经过消息一层会慢一点~哎，效率与结构不好调和。
		// 处于本层之下
		final int count = mFrameManager.getFrameCount();
		mFrameManager.resetFrameIndex(IDiyFrameIds.DRAG_FRAME, count - 1);

		// 注册预览层异步加载监听
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_REGISTER_LOAD_LISTENER, getId(), null, null);
	}

	private boolean handleScreenDrag(View view, float scale, int marginLeft, int marginTop) {
		// 获取其视图的缩略图
		mTargetView = view;

		int statusBarHeight = 0;
		final boolean isFullScreen = WindowControl.getIsFullScreen(mActivity)
				&& StatusBarHandler.isHide();
		if (!isFullScreen) {
			statusBarHeight += StatusBarHandler.getStatusbarHeight();
		}

		// final float scaleSize =
		// mActivity.getResources().getDimensionPixelSize(R.dimen.scale_size);
		// sScaleFactor = (view.getWidth() + scaleSize) / view.getWidth();
		// 长按后不进行放大处理
		sScaleFactor = 1.0f;
		view.destroyDrawingCache();
		mDragBitmap = BitmapUtility.createBitmap(view, sScaleFactor * scale);
		mDragObject = new DragImage(getContentView(), mDragBitmap);
		if (view.getTag() != null && view.getTag() instanceof ShortCutInfo) {
			((DragImage) mDragObject).setTranslucency();
		}
		// int disX = (int)(scaleSize * scale / 2);
		// int disY = (int)(view.getHeight() * (sScaleFactor - 1) / 2);
		int disY = 7;
		Rect rc = null;
		// Rect viewRc = new Rect();
		// boolean bOk = view.getGlobalVisibleRect(viewRc);

		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;

		// if (bOk)
		// {
		// // left = (int) ((viewRc.left - disX) * scale + pageSpaceX);
		// // top = (int) ((viewRc.top - disY - statusBarHeight) * scale +
		// pageSpaceY);
		// // right = (int) ((viewRc.right + disX) * scale + pageSpaceX);
		// // bottom = (int) ((viewRc.bottom + disY - statusBarHeight) * scale +
		// pageSpaceY);
		// left = (int) (viewRc.left * scale - disX);
		// top = (int) (viewRc.top * scale - disY - statusBarHeight);
		// right = (int) (viewRc.left * scale - disX + mDragObject.getWidth());
		// bottom = (int) (viewRc.top * scale - disY - statusBarHeight +
		// mDragObject.getHeight());
		// }
		// else
		// {
		// left = (int) ((view.getLeft() - disX) * scale + pageSpaceX);
		// top = (int) ((view.getTop() - disY - statusBarHeight) * scale +
		// pageSpaceY);
		// right = (int) ((view.getRight() + disX) * scale + pageSpaceX);
		// bottom = (int) ((view.getBottom() + disY - statusBarHeight) * scale +
		// pageSpaceY);
		int[] locXY = new int[2];
		view.getLocationOnScreen(locXY);
		left = locXY[0]/*- disX*/ - marginLeft;
		top = locXY[1] - disY - marginTop;

		if (scale < 1.0f || mMyDragFrame.isTrashGone()) {
			top -= statusBarHeight;
		}

		if (scale < 1.0f && mDragType != TYPE_ADD_APP_DRAG
				&& mDragType != DragFrame.TYPE_ADD_WIDGET_DRAG) {
			float[] matchPoint = new float[] { left * scale, top * scale };
			left = (int) (matchPoint[0] + marginLeft);
			top = (int) (matchPoint[1] + marginTop);
		}

		if (scale < 1.0f
				&& (mDragType == TYPE_ADD_APP_DRAG || mDragType == DragFrame.TYPE_ADD_WIDGET_DRAG)) {
			left += (int) (view.getWidth() * (1 - scale) / 2);
			top += (int) (view.getHeight() * (1 - scale) / 2);
		}

		right = left + mDragObject.getWidth();
		bottom = top + mDragObject.getHeight();
		// }
		rc = new Rect(left, top, right, bottom);
		Bundle bundle = new Bundle();
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.LAST_MOTION_POSITION,
				-1, bundle, null);
		int lastX = (int) bundle.getFloat(DiyScheduler.LAST_MOTION_X, 0f);
		int lastY = (int) bundle.getFloat(DiyScheduler.LAST_MOTION_Y, 0f);
		Point pt = new Point(lastX, lastY + statusBarHeight);
		mMyDragFrame.setSmallState(scale < 1.0f);
		mMyDragFrame.init(mDragObject, rc, pt);
		createHandler(TYPE_SCREEN_HANDLER);
		if (mMyDragFrame.isTrashGone()) {
			mCurHandler.setTrashGone();
		}
		return true;
	}

	private boolean handleAutoFly(View view, int[] destXY, int dragType) {
		//如果是DOCK添加图标层发送的的消息
		if (dragType == DragFrame.TYPE_DOCK_ADD_ICON) {
			// 获取其视图的缩略图
			mTargetView = view;
			mDragBitmap = BitmapUtility.createBitmap(view, 1.0f);

			mDragObject = new DragImage(getContentView(), mDragBitmap);
			if (view.getTag() != null && view.getTag() instanceof ShortCutInfo) {
				((DragImage) mDragObject).setTranslucency();
			}

			int[] locXY = new int[2];
			view.getLocationOnScreen(locXY);
			// 从原图标的中心点启动拖拽对象
//			locXY[0] += (int) (mTargetView.getWidth() * (1 - Workspace.getLayoutScale()) / 2);
			// locXY[1] += (int)
			// (mTargetView.getHeight()*(1-Workspace.getLayoutScale())/2);

			Rect rc = new Rect(locXY[0], locXY[1], locXY[0], locXY[1]);
			mMyDragFrame.init(mDragObject, rc, null);
			mMyDragFrame.mDragObject.autoFly(locXY, destXY, -1, view, dragType);
			return true;
		} else {
			// 获取其视图的缩略图
			mTargetView = view;
			mDragBitmap = BitmapUtility.createBitmap(view, Workspace.getLayoutScale());

			mDragObject = new DragImage(getContentView(), mDragBitmap);
			if (view.getTag() != null && view.getTag() instanceof ShortCutInfo) {
				((DragImage) mDragObject).setTranslucency();
			}

			int[] locXY = new int[2];
			view.getLocationOnScreen(locXY);
			// 从原图标的中心点启动拖拽对象
			locXY[0] += (int) (mTargetView.getWidth() * (1 - Workspace.getLayoutScale()) / 2);
			// locXY[1] += (int)
			// (mTargetView.getHeight()*(1-Workspace.getLayoutScale())/2);

			Rect rc = new Rect(locXY[0], locXY[1], locXY[0] + mDragObject.getWidth(), locXY[1]
					+ mDragObject.getHeight());
			mMyDragFrame.init(mDragObject, rc, null);
			mMyDragFrame.mDragObject.autoFly(locXY, destXY, -1, view, dragType);
			return true;
		}
	}

	private void createHandler(int handlerType) {
		mCurHandler = produce(handlerType, mFrameManager);
		mMyDragFrame.register(mCurHandler);
	}

	void screenToPreviewSwitchHandle() {
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREENTOPREW, -1, null,
				null);
		enterPreview();
		mDragTypeBeforePreview = mDragType;
		mDragType = DragFrame.TYPE_PREVIEW_DRAG;
		if (null != mCurHandler) {
			mCurHandler.selfDestruct();
			mCurHandler = null;
		}
		createHandler(TYPE_PREVIEW_HANDLER);
	}

	/***
	 * 从屏幕预览到桌面
	 */
	void previewToScreenSwitchHandle(View draggedView) {
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.PREVIEWWTOSCREEN, -1,
				null, null);
		setDragView(draggedView);
		mDragType = mDragTypeBeforePreview;
		if (mTempRect != null) {
			IDragObject pDragObject = mMyDragFrame.getDragObject();
			Point pDragPointF = mMyDragFrame.getDragPoint();
			if (pDragObject != null && pDragPointF != null) {
				final int width = mTempRect.width();
				final int height = mTempRect.height();
				int l = pDragPointF.x - width / 2;
				int t = pDragPointF.y - height / 2;
				int r = l + width;
				int b = t + height;
				pDragObject.scale(1, 1);
				pDragObject.layout(l, t, r, b);
				mMyDragFrame.setDragRect(new Rect(l, t, r, b));
				mTempRect = null;
			}
		}
		if (null != mCurHandler) {
			mCurHandler.selfDestruct();
			mCurHandler = null;
		}
		createHandler(TYPE_SCREEN_HANDLER);

	}

	void setFolderReplace(boolean folderReplace) {
		if (mCurHandler instanceof ScreenDragHandler) {
			((ScreenDragHandler) mCurHandler).setFolderReplace(folderReplace);
		}
	}

	private AbstractDragHandler produce(int handlerType, IFrameManager frameManager) {
		AbstractDragHandler handler = null;
		if (mCurHandler != null && mCurHandler.mType == handlerType) {
			return mCurHandler;
		}
		switch (handlerType) {
			case TYPE_SCREEN_HANDLER :
				handler = new ScreenDragHandler(TYPE_SCREEN_HANDLER, this, frameManager,
						mTargetView);
				break;

			case TYPE_PREVIEW_HANDLER :
				handler = new PreviewDragHandler(TYPE_PREVIEW_HANDLER, this, frameManager,
						mTargetView);
				break;

			default :
				break;
		}
		return handler;
	}

	@Override
	public void onRemove() {
		super.onRemove();
		selfDestruct();

		// 取消拖拽
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DRAG_CANCEL, -1, null,
				null);

		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_CLEAR_OUTLINE_BITMAP, 1, null, null);
		// //显示指示器
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
		// IDiyMsgIds.SHOW_INDICATOR, -1, null, null);

		GoLauncher.sendBroadcastHandler(this, IDiyMsgIds.DRAG_FINISH_BROADCAST, -1, null, null);
		
		/*
		 * ADT-9827 功能表文件夹二期优化，横屏下长按功能表中的图标将该图标拖入桌面后将手机竖放，屏幕不能切换回竖屏
		 */
		if (mDragType == TYPE_APPFUNC_ITEM_DRAG) {
			OrientationControl.setOrientation(getActivity());
		}
	}

	public Activity getActivity() {
		return mActivity;
	}

	public int getDragType() {
		return mDragType;
	}

	public int getDragTypeBeforePreview() {
		return mDragTypeBeforePreview;
	}

	public int getCurHandler() {
		if (mCurHandler != null) {
			return mCurHandler.mType;
		}

		return -1;
	}

	@Override
	public View getDragView() {
		return mTargetView;
	}

	public void setDragView(View targetView) {
		mTargetView = targetView;
	}

	public boolean isOverEdgeOfDock() {
		if (mCurHandler != null && mCurHandler.mType == TYPE_SCREEN_HANDLER) {
			return ((ScreenDragHandler) mCurHandler).isOverEdgeOfDock();
		}
		return false;
	}

	private void zoomDragViewForMergeFolder() {
		if (!mMyDragFrame.isMergeScale()) {
			if (GOLauncherApp.getSettingControler().getDesktopSettingInfo().isShowTitle()) {
				if (mDragObject instanceof DragImage) {
					if (mTargetView instanceof BubbleTextView) {
						((BubbleTextView) mTargetView).setText(null);
					}
					mTargetView.postInvalidate();
					mDragBitmap = BitmapUtility.createBitmap(mTargetView, sScaleFactor);
					((DragImage) mDragObject).setDragBitmap(mDragBitmap);

				}
			}
			IDragObject obj = mMyDragFrame.getDragObject();
			final float scale = OrientationControl.isSmallModle()
					? Workspace.getLayoutScale()
					: 0.8f;
			obj.scale(scale, scale);
			mMyDragFrame.setMergeScale(true);
		}
	}

	private void revertScaleDragViewForMergeFolder() {
		if (mMyDragFrame.isMergeScale()) {
			if (GOLauncherApp.getSettingControler().getDesktopSettingInfo().isShowTitle()) {
				Object tag = mTargetView.getTag();
				if (tag instanceof ShortCutInfo) {
					if (mDragObject instanceof DragImage) {
						if (mTargetView instanceof BubbleTextView) {
							((BubbleTextView) mTargetView).setText(((ShortCutInfo) tag).mTitle);
						}
						mTargetView.postInvalidate();
						mDragBitmap = BitmapUtility.createBitmap(mTargetView, sScaleFactor);
						((DragImage) mDragObject).setDragBitmap(mDragBitmap);
					}
				}
			}
			IDragObject obj = mMyDragFrame.getDragObject();
			final float scale = OrientationControl.isSmallModle()
					? Workspace.getLayoutScale()
					: 1.0f;
			obj.scale(scale, scale);
			mMyDragFrame.setMergeScale(false);
		}
	}

	protected boolean isTrashGone() {
		return mMyDragFrame.isTrashGone();
	}

	protected int getExtraDragX() {
		if (mMyDragFrame != null) {
			return mMyDragFrame.mExtraDragX;
		}
		return 0;
	}
}
