package com.jiubang.ggheart.apps.desks.diy.frames.preview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.go.util.animation.MyAnimationUtils;
import com.go.util.graphics.BitmapUtility;
import com.go.util.graphics.DrawUtils;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragImage;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.IDragListener;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.IDragObject;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.MyDragFrame;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.TrashLayer;

/**
 * 预览层位置替换层
 * 
 * @author yuankai
 * @version 1.0
 */
public class ReplaceDragFrame extends AbstractFrame
		implements
			IDragListener,
			ISelfObject,
			AnimationListener {
	// Card参数
	// 起始参数
	// Card矩形参数
	private int mCurScreenStartIndex;
	private List<Rect> mCardRects = new ArrayList<Rect>();

	// 换位参数
	// 换位锁屏
	// 挤压忽略值
	private boolean mLocked = false;
	private final static float REPLACE_IGNORE = 10;

	// 真实View
	protected View mDraggedView;

	// 放大操作
	// 放大倍数
	private final static float SCALE_RATIO = 1.07f;

	// Drag
	private Bitmap mDragBitmap;
	private DragImage mDragImage;
	private MyDragFrame mMyDragFrame;

	private DragLayout mDragLayout;
	// 垃圾箱属性
	private ImageView mTrashIcon;
	private Drawable mTrashBgNormal;
	private Drawable mTrashBgHover;
	private Drawable mTrashCanNormal;
	private Drawable mTrashOpen;
	private int mIconFileterColor;
	private RelativeLayout mRelativeLayout;
	// 当前状态
	private final static int STATUS_NORMAL = 0; // 一般状态，表示简单的移动位置
	private final static int STATUS_DEL = 1; // 删除状态
	private final static int STATUS_NOSCREEN = 2;
	private int mStatus = STATUS_NORMAL;

	private DialogConfirm mDelDialog = null;

	private View mView = null;

	private int mDraggedViewStartId = 0;
	private int mDraggedViewEndId = 0;

	private MotionEvent mEvent;
	
	/**
	 * 替换拖动层构造方法
	 * 
	 * @param activity
	 *            Activity
	 * @param frameManager
	 *            帧管理器
	 * @param id
	 *            ID
	 */
	public ReplaceDragFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);

		selfConstruct();
		// 注册replace监听
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_REGISTER_REPLACE_LISTENER, mId, null, null);
	}

	@Override
	public void selfConstruct() {
		mMyDragFrame = new MyDragFrame(mActivity);
		mMyDragFrame.register(this);
		mDragLayout = (DragLayout) mActivity.getLayoutInflater().inflate(
				R.layout.relpacedrag_frame, null);
		if (null != mDragLayout) {
			mMyDragFrame.setContentView(mDragLayout);
			mDragLayout.setVisibility(View.GONE);
			mRelativeLayout = (RelativeLayout) mDragLayout.findViewById(R.id.trash_area_stub);
			mTrashIcon = (ImageView) mDragLayout.findViewById(R.id.trash_can);
		}
		initTrash();
	}

	@Override
	public void selfDestruct() {
		if (mCardRects != null) {
			mCardRects.clear();
			mCardRects = null;
		}
		mDraggedView = null;
		if (mMyDragFrame != null) {
			mMyDragFrame.unregister(this);
			mMyDragFrame.selfDestruct();
		}
		mMyDragFrame = null;
		if (mDragLayout != null) {
			mDragLayout.selfDestruct();
		}
		if (null != mDragImage) {
			mDragImage.selfDestruct();
			mDragImage = null;
		}

		if (null != mDragBitmap && !mDragBitmap.isRecycled()) {
			mDragBitmap.recycle();
			mDragBitmap = null;
		}
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

	@Override
	public void onRemove() {
		super.onRemove();

		if (!SensePreviewFrame.sPreLongscreenFrameStatus && SenseWorkspace.showStatusBar) {
			// SensePreviewFrame.previewOperate = true;
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
			SenseWorkspace.showStatusBar = false;
		}
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.DEPLAY_INDICATOR, -1, null, null);
		SensePreviewFrame.sPreviewLongClick = false;
		// 反注册replace监听
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_UNREGISTER_REPLACE_LISTENER, mId, null, null);

		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME, IDiyMsgIds.SET_ORIENTATION,
				-1, null, null);
		//		OrientationControl.setOrientation(mActivity);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		super.handleMessage(who, type, msgId, param, object, objects);
		boolean ret = false;
		switch (msgId) {
			case IDiyMsgIds.REPLACE_DRAG_INIT : {
				if (object != null && object instanceof View && objects != null
						&& objects.size() > 0) {
					Bundle bundle = new Bundle();
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
							IDiyMsgIds.PREVIEW_GET_START_SCREEN_WIDTH, -1, bundle, null);
					return init(bundle.getInt(SensePreviewFrame.FIELD_SCREEN_COUNT), param,
							(View) object, (List<Rect>) objects);
				}
			}
				break;

			case IDiyMsgIds.PREVIEW_REPLACE_COMPLETE : {
				mLocked = false;
			}
				break;

			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED :
			case IDiyMsgIds.SHOW_UPDATE_DIALOG :
			case IDiyMsgIds.BACK_TO_MAIN_SCREEN : {
				endDrag();
				ret = true;
			}
				break;
			case IDiyMsgIds.REPLACE_DRAG_FINISH : {
				GoLauncher.sendHandler(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, mId, null, null);
				ret = true;

			}
				break;
			case IDiyMsgIds.REPLACE_DRAG_HOME_CLICK : {

				if (!SensePreviewFrame.sPreLongscreenFrameStatus && SenseWorkspace.showStatusBar) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
					SenseWorkspace.showStatusBar = false;
				}
				// 显示指示器
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.DISPLAY_INDICATOR, -1, false, null);

				if (mDraggedView != null && mMyDragFrame != null) {
					if (mDraggedView instanceof CardLayout) {
						((CardLayout) mDraggedView).setNormal();
						mDraggedView.postInvalidate();
						mDragLayout.postInvalidate();
					}

					// 移动卡片结束
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
							IDiyMsgIds.REPLACE_DRAG_OVER_SYNC, mDraggedView.getId(), null, null);

					Rect mCurRc = mMyDragFrame.getDragRect();
					if (null != mCurRc) {
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
								IDiyMsgIds.PREVIEW_REPLACE_CARD_BACK, mDraggedView.getId(), mCurRc,
								mCardRects);
					}
					if (mDelDialog != null) {
						mDelDialog.dismiss();
					}
					selfDestruct();
				}

				ret = true;
			}
				break;
			case IDiyMsgIds.SNAPSHOT_START:
			{
				if (null != mMyDragFrame) {
					mEvent.setAction(MotionEvent.ACTION_UP);
					mMyDragFrame.onTouchEvent(mEvent, DragFrame.TYPE_PREVIEW_DRAG);
				}
			}
				break;
			default :
				break;
		}
		return ret;
	}

	private boolean init(final int startScreenWidth, final int startIndex, View draggedView,
			List<Rect> rects) {
		mCurScreenStartIndex = startIndex;
		mDraggedView = draggedView;
		mCardRects = rects;
		mDraggedViewStartId = mDraggedView.getId();

		if (null == mDragBitmap) {
			mDragBitmap = BitmapUtility.createBitmap(draggedView, SCALE_RATIO);
			mDragImage = new DragImage(getContentView(), mDragBitmap);
			int offsetX = (int) (mDraggedView.getWidth() * (SCALE_RATIO - 1) / 2);
			int offsetY = (int) (mDraggedView.getHeight() * (SCALE_RATIO - 1) / 2);
			Rect rc = null;
			if (SensePreviewFrame.sPreLongscreenFrameStatus) {
				rc = new Rect(mDraggedView.getLeft() - startScreenWidth - offsetX,
						mDraggedView.getTop() - offsetY, mDraggedView.getRight() - startScreenWidth
								+ offsetX, mDraggedView.getBottom() + offsetY);
			} else {
				rc = new Rect(mDraggedView.getLeft() - startScreenWidth - offsetX,
						mDraggedView.getTop() + StatusBarHandler.getStatusbarHeight() - offsetY,
						mDraggedView.getRight() - startScreenWidth + offsetX,
						mDraggedView.getBottom() + StatusBarHandler.getStatusbarHeight() + offsetY);
			}
			if (mMyDragFrame != null) {
				mMyDragFrame.init(mDragImage, rc, null);
			}
		}
		return true;
	}

	@Override
	public View getContentView() {
		if (mMyDragFrame != null) {
			return mMyDragFrame.getContentView();
		}
		return null;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (mMyDragFrame != null) {
			mEvent = event;
			return mMyDragFrame.onTouchEvent(event, DragFrame.TYPE_PREVIEW_DRAG);
		}
		return false;
	}

	private void endDrag() {
		Animation animation = MyAnimationUtils.getPopupAnimation(
				MyAnimationUtils.POP_TO_LONG_START_HIDE_2, -1);
		if (mRelativeLayout != null) {
			// 为使垃圾桶的消失动画与卡片的回归动画同时进行。
			if (GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
					IDiyMsgIds.PREVIEW_REPLACE_FINISH, -1, null, null)) { // 卡片替换动画是否完成
				onAnimationEnd(null);
			} else {
				animation.setAnimationListener(this);
			}

			animation.setDuration(200);
			mRelativeLayout.startAnimation(animation);
			mRelativeLayout.setVisibility(View.INVISIBLE);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}

	private void snapToNextScreen() {
		Bundle bundle = new Bundle();
		boolean success = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_SNAP_NEXT, -1, bundle, mCardRects);
		if (success && mCardRects.size() > 0) {
			mCurScreenStartIndex = bundle.getInt(SensePreviewFrame.FIELD_CUR_SCREEN_START_INDEX);
		}
	}

	private void snapToPreScreen() {
		Bundle bundle = new Bundle();
		boolean success = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_SNAP_PRE, -1, bundle, mCardRects);
		if (success && mCardRects.size() > 0) {
			mCurScreenStartIndex = bundle.getInt(SensePreviewFrame.FIELD_CUR_SCREEN_START_INDEX);
		}
	}

	@Override
	public void onCenterPointF(IDragObject obj, float x, float y, int dragType) {
		if (mLocked) {
			return;
		}

		int centerX = (int) x;
		int centerY = (int) y;

		final int count = mCardRects != null ? mCardRects.size() : 0;
		for (int i = 0; i < count; i++) {
			final Rect cardRect = mCardRects.get(i);
			if (cardRect.contains(centerX, centerY)) {
				// 要求屏幕预览层交换位置
				if (mDraggedView != null && (mCurScreenStartIndex + i) != mDraggedView.getId()) {
					Bundle bundle = new Bundle();
					bundle.putInt(SensePreviewFrame.FIELD_SRC_SCREEN, mDraggedView.getId());
					bundle.putInt(SensePreviewFrame.FIELD_DEST_SCREEN, i + mCurScreenStartIndex);
					// 替换位置，并更新位置信息
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
							IDiyMsgIds.PREVIEW_REPLACE_CARD, -1, bundle, mCardRects);
					mLocked = true;
					return;
				}
			}
		}

		// 挤压操作
		if (count > 0 && mMyDragFrame != null) {
			Rect rect = mMyDragFrame.getDragRect();
			if (null == rect) {
				return;
			}
			int ret = doHollowReplace(rect, mCardRects.get(0), mCardRects.get(count - 1),
					DrawUtils.dip2px(REPLACE_IGNORE));
			if (-1 == ret) {
				return;
			}
			if (0 == ret) {
				return;
			}
			int replaceIndex = mCurScreenStartIndex + ret * (count - 1);
			// 要求屏幕预览层交换位置
			if (replaceIndex != mDraggedView.getId()) {
				Bundle bundle = new Bundle();
				bundle.putInt(SensePreviewFrame.FIELD_SRC_SCREEN, mDraggedView.getId());
				bundle.putInt(SensePreviewFrame.FIELD_DEST_SCREEN, replaceIndex);
				// 替换位置，并更新位置信息
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						IDiyMsgIds.PREVIEW_REPLACE_CARD, -1, bundle, mCardRects);
				mLocked = true;
			}
		}
	}

	/*
	 * retrun 0 在第一个前 1 在最后个后 -1其他
	 */
	private int doHollowReplace(Rect curRc, Rect firstRc, Rect lastRc, int ignore) {
		// 检测参数
		if (0 == curRc.width() && 0 == curRc.height()) {
			return -1;
		}

		// 第一个左前
		if (curRc.centerX() <= firstRc.left - ignore || curRc.centerY() <= firstRc.top - ignore) {
			return 0;
		}

		// 最后一个后
		// 右
		// 下
		if ((curRc.centerX() >= lastRc.right + ignore && curRc.centerY() >= lastRc.top + ignore)
				|| curRc.centerY() >= lastRc.bottom + ignore) {
			return 1;
		}

		return -1;
	}

	@Override
	public void onFingerPointF(IDragObject obj, float x, float y) {
		final int centerX = (int) x;
		final int centerY = (int) y;
		// 垃圾箱操作
		if (isOverEdgeOfTrash(centerX, centerY)) {
			focusTrash(obj);
			mStatus = STATUS_DEL;
			if (SenseWorkspace.sCardCount <= 1) {
				mStatus = STATUS_NOSCREEN;
			}
		} else {
			unFocusTrash(obj);
			mStatus = STATUS_NORMAL;
		}
	}

	@Override
	public void onEdge(IDragObject obj, int edgeType) {
		if ((edgeType & IDragListener.EDGE_LEFT) == IDragListener.EDGE_LEFT) {
			snapToPreScreen();
		}

		if ((edgeType & IDragListener.EDGE_RIGHT) == IDragListener.EDGE_RIGHT) {
			snapToNextScreen();
		}
	}

	@Override
	public void onEnterEdge(IDragObject obj, int edgeType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLeaveEdge(IDragObject obj, int edgeType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDragFinish(IDragObject obj, float x, float y) {
		endDrag();
	}

	@Override
	public void onDragMove(IDragObject obj, float x, float y) {
		// TODO Auto-generated method stub

	}

	private void initTrash() {
		// 加载主题
		mIconFileterColor = mActivity.getResources().getColor(R.color.delete_color_filter);
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
					// 全屏时
					if (SensePreviewFrame.sPreLongscreenFrameStatus) {
						mTrashCanNormal = themeControler.getDrawable(normalLayer.mResImage,
								R.drawable.littledel);
						mTrashOpen = themeControler.getDrawable(normalLayer.mResImage,
								R.drawable.littledel_open);
					} else {
						mTrashCanNormal = themeControler.getDrawable(normalLayer.mResImage,
								R.drawable.del);
						mTrashOpen = themeControler.getDrawable(normalLayer.mResImage,
								R.drawable.del_open);
					}
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
							// mTrashCanFocus = themeControler.getDrawable(
							// hightLightLayer.mResImage,
							// R.drawable.trash_can);
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
					mTrashBgNormal = mActivity.getResources().getDrawable(
							R.drawable.trash_bg_normal);
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
					mTrashBgHover = mActivity.getResources().getDrawable(R.drawable.trash_bg_hover);
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
					if (SensePreviewFrame.sPreLongscreenFrameStatus) {
						mTrashCanNormal = mActivity.getResources()
								.getDrawable(R.drawable.littledel);
					} else {
						mTrashCanNormal = mActivity.getResources().getDrawable(R.drawable.del);
					}
					// mTrashCanNormal =
					// DrawUtils.zoomDrawable(mFrame.getActivity().getResources().getDrawable(R.drawable.trash_can),
					// 0.5f, 0.5f, GoLauncher.getContext().getResources());
				} catch (OutOfMemoryError e) {
					OutOfMemoryHandler.handle();
					mTrashCanNormal = null;
				}
			}
		}

		if (mTrashOpen == null) {
			while (mTrashOpen == null) {
				// 若因内存溢出取资源图片不成功，一直循环取
				try {
					if (SensePreviewFrame.sPreLongscreenFrameStatus) {
						mTrashOpen = mActivity.getResources()
								.getDrawable(R.drawable.littledel_open);
					} else {
						mTrashOpen = mActivity.getResources().getDrawable(R.drawable.del_open);
					}
					// mTrashCanNormal =
					// DrawUtils.zoomDrawable(mFrame.getActivity().getResources().getDrawable(R.drawable.trash_can),
					// 0.5f, 0.5f, GoLauncher.getContext().getResources());
				} catch (OutOfMemoryError e) {
					OutOfMemoryHandler.handle();
					mTrashOpen = null;
				}
			}
		}

		// if(mTrashCanFocus == null)
		// {
		// while (mTrashCanFocus == null)
		// {
		// //若因内存溢出取资源图片不成功，一直循环取
		// try
		// {
		// mTrashCanFocus =
		// mActivity.getResources().getDrawable(R.drawable.trash_can);
		// // mTrashCanFocus =
		// DrawUtils.zoomDrawable(mFrame.getActivity().getResources().getDrawable(R.drawable.trash_can),
		// // 0.5f, 0.5f, GoLauncher.getContext().getResources());
		// } catch (OutOfMemoryError e)
		// {
		// OutOfMemoryHandler.handle();
		// mTrashCanFocus = null;
		// }
		// }
		// }
		if (mRelativeLayout != null) {
			mRelativeLayout.setBackgroundDrawable(mTrashBgNormal);
		}

		if (mTrashIcon != null) {
			mTrashIcon.setImageDrawable(mTrashCanNormal);
			// mTrashIcon.setBackgroundDrawable(mTrashBgNormal);
		}

		startAnimation();
	}

	private void startAnimation() {
		if (mRelativeLayout == null) {
			return;
		}
		Animation animation = MyAnimationUtils.getPopupAnimation(
				MyAnimationUtils.POP_FROM_LONG_START_SHOW_2, -1);
		if (animation != null) {
			mRelativeLayout.startAnimation(animation);
		}
	}

	private boolean isOverEdgeOfTrash(int centerX, int centerY) {
		// 还没有初始化完成
		if (null == mRelativeLayout
				|| (0 == mRelativeLayout.getWidth() && 0 == mRelativeLayout.getHeight())) {
			return false;
		}

		// int statusBarHeight = 0;
		// if(!WindowControl.getIsFullScreen(mActivity))
		// {
		// statusBarHeight += StatusBarHandler.getStatusbarHeight();
		// }
		Rect trashRc = new Rect(mRelativeLayout.getLeft(), mRelativeLayout.getTop(),
				mRelativeLayout.getRight(), mRelativeLayout.getBottom());
		// return centerX < trashRc.right && centerY < trashRc.bottom;
		return centerY < trashRc.bottom * 0.7;
		// if (CellLayout.mPortrait)
		// {
		// //*0.7是因为垃圾箱底部与图标有重叠，视觉上重叠，但拖动操作位置上为了不重叠，所以*0.7
		// return centerY < trashRc.bottom * 0.7;
		// }else {
		// return centerY < trashRc.bottom * 0.7;
		// }
	}

	@Override
	public void focusTrash(IDragObject obj) {
		// 设置垃圾箱背景为警告
		if (null != mTrashIcon) {
			mRelativeLayout.setBackgroundDrawable(mTrashBgHover);
			// 垃圾箱设为开口
			mTrashIcon.setImageDrawable(mTrashOpen);
		}

		if (null != obj) {
			obj.setColor(mIconFileterColor, PorterDuff.Mode.SRC_ATOP);
		}
	}

	private void unFocusTrash(IDragObject obj) {
		if (null != mTrashIcon) {
			mRelativeLayout.setBackgroundDrawable(mTrashBgNormal);
			mTrashIcon.setImageDrawable(mTrashCanNormal);
		}
		if (null != obj) {
			obj.setColor(0, PorterDuff.Mode.SRC_ATOP);
		}
	}

	/**
	 * <br>功能简述:显示删除有内容的屏幕提示框
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param v
	 */
	void showDeleteDialog(View v) {
		mView = v;

		mDelDialog = null;
		mDelDialog = new DialogConfirm(mActivity);
		mDelDialog.show();
		mDelDialog.setTitle(R.string.del_title_tip);
		mDelDialog.setMessage(R.string.del_content_tip);
		mDelDialog.setPositiveButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final View view = mView;
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						IDiyMsgIds.PREVIEW_DELETE_SCREEN, -1, view, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SAVE_SCREEN_DATA, mDraggedViewEndId, true, null);
				//用户行为统计
				StatisticsData.countUserActionData(
						StatisticsData.DESK_ACTION_ID_SCREEN_PREVIEW_EDIT,
						StatisticsData.USER_ACTION_TWO, IPreferencesIds.DESK_ACTION_DATA);
			}
		});

		mDelDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (mDelDialog != null) {
					backToSeat();
					mDelDialog = null;
				}
			}
		});
	}

	/***
	 * 卡片回归
	 */
	private void backToSeat() {
		if (mDraggedView == null) {
			return;
		}

		if (mDraggedView instanceof CardLayout) {
			((CardLayout) mDraggedView).setNormal();
			mDraggedView.postInvalidate();
			mDragLayout.postInvalidate();
		}

		// 移动卡片结束
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.REPLACE_DRAG_OVER, mDraggedView.getId(), null, null);

		Rect mCurRc = mMyDragFrame.getDragRect();
		if (null != mCurRc) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
					IDiyMsgIds.PREVIEW_REPLACE_CARD_BACK, mDraggedView.getId(), mCurRc, mCardRects);
		}
		selfDestruct();
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		// 如果捉起的卡片为空,则移除该层
		if (mDraggedView == null || mMyDragFrame == null) {
			selfDestruct();
			GoLauncher.sendHandler(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					mId, null, null);
			// 返回屏幕层
			GoLauncher.sendHandler(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					IDiyFrameIds.SCREEN_PREVIEW_FRAME, null, null);
			return;
		}
		if (mDraggedView instanceof CardLayout && mStatus != STATUS_DEL) {
			((CardLayout) mDraggedView).setNormal();
		}
		switch (mStatus) {
			case STATUS_NORMAL : {
				saveData();
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						IDiyMsgIds.REPLACE_DRAG_OVER, mDraggedView.getId(), null, null);

				Rect curRc = mMyDragFrame.getDragRect();
				if (null != curRc) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
							IDiyMsgIds.PREVIEW_REPLACE_CARD_BACK, mDraggedView.getId(), curRc,
							mCardRects);
				}
				selfDestruct();
			}
				break;

			case STATUS_NOSCREEN :
				saveData();
				Rect curRc = mMyDragFrame.getDragRect();
				if (null != curRc) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
							IDiyMsgIds.PREVIEW_NOLESS_SCREEN, mDraggedView.getId(), curRc,
							mCardRects);
				}
				selfDestruct();
				break;
			case STATUS_DEL :
				if (((CardLayout) mDraggedView).hasContent()) {
					// 交换卡片
					mDraggedViewEndId = mDraggedView.getId();
					if (mDraggedViewEndId != mDraggedViewStartId) {
						Bundle bundle = new Bundle();
						bundle.putInt(SensePreviewFrame.FIELD_SRC_SCREEN, mDraggedViewEndId);
						bundle.putInt(SensePreviewFrame.FIELD_DEST_SCREEN, mDraggedViewStartId);
						// 替换位置，并更新位置信息
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
								IDiyMsgIds.PREVIEW_REPLACE_CARD, -1, bundle, mCardRects);
					}
					// 保存数据
					saveData();
					showDeleteDialog(mDraggedView);
				} else {
					saveData();
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
							IDiyMsgIds.PREVIEW_DELETE_SCREEN, -1, mDraggedView, null);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SAVE_SCREEN_DATA, mDraggedViewStartId, true, null);
					selfDestruct();
					//用户行为统计
					StatisticsData.countUserActionData(
							StatisticsData.DESK_ACTION_ID_SCREEN_PREVIEW_EDIT,
							StatisticsData.USER_ACTION_TWO, IPreferencesIds.DESK_ACTION_DATA);
				}
				break;
			default :
				selfDestruct();
				break;
		}
	}

	private void saveData() {

		// 保存数据库
		mDraggedViewEndId = mDraggedView.getId();
		Bundle bundle1 = new Bundle();
		bundle1.putInt(SensePreviewFrame.FIELD_SRC_SCREEN, mDraggedViewStartId);
		bundle1.putInt(SensePreviewFrame.FIELD_DEST_SCREEN, mDraggedViewEndId);
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SAVE_SCREEN_DATA, -1,
				bundle1, null);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}
}