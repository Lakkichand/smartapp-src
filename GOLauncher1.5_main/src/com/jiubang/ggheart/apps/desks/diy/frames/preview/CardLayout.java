package com.jiubang.ggheart.apps.desks.diy.frames.preview;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 预览界面上的一个卡片组件视图
 * 
 * @author yuankai
 * @version 1.0
 */
public class CardLayout extends ViewGroup
		implements
			OnClickListener,
			OnLongClickListener,
			OnTouchListener {
	private static final String TAG = "CardLayout";

	final static int TYPE_ADD = 1;     // 卡片类型：+号卡片
	final static int TYPE_PREVIEW = 2; // 卡片类型：预览卡片

	final static int STATE_NORMAL = 1;
	final static int STATE_ENLARGE = 2;

	final static int ZOOM_IN_DURATION = 250;   // 动画时间
	final static float ENLARGE_FACTOR = 1.07f; // 长按放大视图时的比例

	private int mType;   // 卡片类型
	private boolean mCanDelete; //卡片中是否有子视图，有子视图需要弹出提示框，没有就直接删除，
	private boolean mIsHome; //是否是主屏
	private boolean mIsCurrent; //是否是当前屏
	private ImageButton mHome; //设置主屏按钮
	private View mPreview;
	private ICardEventListener mListener;

	private int mState = STATE_NORMAL;
	private Rect mEnlargeRect;
	private boolean mDrawingCacheEnabled;

	private PreviewController mPreviewController; // 预览控制
	private Runnable mRunnable;

	private ImageView mRoom;

	public static boolean sDrawRoom = false;
	private SenseWorkspace mWorkspace;

	/***
	 * <默认构造函数>
	 */
	public CardLayout(Context context, int cardType, View preView, boolean canDelete,
			SenseWorkspace workspace) {
		super(context);
		mType = cardType;
		if (workspace == null) {
			return;
		}
		mWorkspace = workspace;
		if (mType == TYPE_ADD) {
			setBackgroundDrawable(mWorkspace.mAddImg);
		} else {
			mPreview = preView;
			mCanDelete = canDelete;
			setBackgroundDrawable(mWorkspace.mBorderImg);

			// 初始化Home
			mHome = new ImageButton(context);
			mHome.setBackgroundColor(0);
			mHome.setImageDrawable(mWorkspace.mHomeImg);
			mHome.setOnClickListener(this);
			mHome.setFocusable(false);

			addView(mHome);

			mRoom = new ImageView(context);
			mRoom.setFocusable(false);
			addView(mRoom);

		}
		setOnTouchListener(this);
		setClickable(true);
		setOnClickListener(this);
		setLongClickable(true);
		setOnLongClickListener(this);
		mRunnable = new RefrashCardThread();
	}

	public void setPreViewInfo(View preView, boolean canDelete) {
		mPreview = preView;
		mCanDelete = canDelete;
	}

	public void setPreviewController(PreviewController controller) {
		mPreviewController = controller;
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
//		if (gainFocus) {
			// 获取到了焦点，则把焦点给预览屏
//		}
	}

	/**
	 * 设置是否是主屏
	 * 
	 * @param isHome
	 *            是否主屏
	 */
	public void setHome(boolean isHome) {
		if (mType == TYPE_ADD || mIsHome == isHome) {
			return;
		}
		mIsHome = isHome;
		mHome.setImageDrawable(mIsHome ? mWorkspace.mLightHomeImg : mWorkspace.mHomeImg);
		postInvalidate();
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		int canvasId = canvas.save();
		if (mType != TYPE_ADD) {
			// 绘制桌面VIEW
			if (mPreview != null) {
				final int saveid = canvas.save();
				final float factor = SenseWorkspace.sPreViewScaleFactor;
				canvas.translate(SenseWorkspace.sCardPaddingLeft, SenseWorkspace.sCardPaddingTop);
				canvas.scale(factor, factor);
				// 设置裁剪区域防止5×5下超出卡片位置
				canvas.clipRect(0, 0, SensePreviewFrame.sScreenW, SensePreviewFrame.sScreenH);
				mPreview.draw(canvas);
				canvas.restoreToCount(saveid);
			}
			final int homeLeft = mHome.getLeft();
			final int homeTop = mHome.getTop();
			canvas.translate(homeLeft, homeTop);
			mHome.draw(canvas);
			if (null != mRoom && sDrawRoom) {
				final int mRoomLeft = mRoom.getLeft();
				final int mRoomTop = mRoom.getTop();
				canvas.translate(mRoomLeft - homeLeft, mRoomTop - homeTop);
				mRoom.draw(canvas);
			}
		}
		canvas.restoreToCount(canvasId);
	}

	/**
	 * 设置监听
	 * 
	 * @param listener
	 *            监听者
	 */
	public void setEventListener(ICardEventListener listener) {
		mListener = listener;
	}

	/**
	 * 设置是否是主屏
	 * 
	 * @return 是否是主屏
	 */
	public boolean isHome() {
		return mIsHome;
	}

	/**
	 * preview中是否有子视图
	 * 
	 */
	public boolean hasContent() {
		return !mCanDelete;
	}

	public void checkCanDelete() {
		mCanDelete = !((mPreview != null) && (mPreview instanceof ViewGroup) && (((ViewGroup) mPreview)
				.getChildCount() > 0));
	}

	/**
	 * 放大
	 * 
	 * @param left
	 *            当前卡片在屏幕上的横坐标
	 * @param top
	 *            当前卡片在屏幕上的纵坐标
	 * 
	 * @return 返回放大后在屏幕上的区域
	 */
	public Rect enlarge(int left, int top) {
		if (mState == STATE_ENLARGE) {
			return mEnlargeRect;
		}

		mState = STATE_ENLARGE;
		// 区域
		final int cardWidth = getWidth();
		final int cardHeight = getHeight();
		final float diffScale = (1 - ENLARGE_FACTOR) * 0.5f;
		mEnlargeRect = new Rect(0, 0, cardWidth, cardHeight);
		mEnlargeRect.inset((int) (cardWidth * diffScale), (int) (cardHeight * diffScale));
		// 需要减去变大的区域
		mEnlargeRect.offset((int) (left - cardWidth * diffScale), (int) (top - cardHeight
				* diffScale));
		setBackgroundDrawable(mWorkspace.mBorderDragImg); // 让整个卡片变亮
		startAnimation(getZoomInAnimation());
		postInvalidate();
		return mEnlargeRect;
	}

	/**
	 * 恢复
	 */
	public void resume() {
		if (mState == STATE_ENLARGE) {
			mEnlargeRect = null;
			clearClickState(); // 清除enlarge()方法中设置的加亮底图
			startAnimation(getZoomOutAnimation()); // 设置恢复大小的动画，否则不能在父容器里清除之前放大的部分
		}
		mState = STATE_NORMAL;
		postInvalidate();
	}

	/**
	 * 回收,退出预览层才会调用此函数，删除一屏不会调用此函数
	 */
	public void recycle() {
		resume();
		clearAnimation();
		removeCallbacks(mRunnable);
	}

	/**
	 * 设置当前屏
	 * 
	 * @param isCurrent
	 */
	public void setCurrent(boolean isCurrent) {
		if (mType == TYPE_ADD || mIsCurrent == isCurrent) {
			return;
		}
		mIsCurrent = isCurrent;
		setBackgroundDrawable(mIsCurrent ? mWorkspace.mBorderLightImg : mWorkspace.mBorderImg);
		postInvalidate();
	}

	/**
	 * 是否当前屏
	 * 
	 * @return 是否当前屏
	 */
	public boolean isCurrent() {
		return mIsCurrent;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (null == mListener) {
			Log.i(TAG, "no register listener");
			return false;
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN : {
				if (v == this && mType == TYPE_ADD) {
					setBackgroundDrawable(mWorkspace.mLightAddImg); // 让整个卡片变亮
				}
				// else if(v == mDel){
				// setBackgroundDrawable(BorderDelImg); // 让整个卡片变红
				// mDel.setImageDrawable(LightDelImg);
				// }
				mListener.onCardEvent(this, ICardEvent.TOUCH_DOWN);
			}
				break;
		}
		return false;
	}

	@Override
	public boolean onLongClick(View v) {
		//add by jiang 设置为默认桌面后  按home键跳屏幕预览
		if (SensePreviewFrame.sIsHOME) {
			return false;
		}
		if (null == mListener) {
			Log.i(TAG, "no register listener");
			return false;
		}
		// 判断当前是否锁屏
		if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(getContext());
			return true;
		}

		if (mType != TYPE_ADD) {
			// 设置长按状态的图片，之后让父容器清理
			setBackgroundDrawable(mWorkspace.mBorderDragImg); // 让整个卡片变亮
			mListener.onCardEvent(this, ICardEvent.PREVIEW_LONG_CLICK);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (null == mListener) {
			Log.i(TAG, "no register listener");
			return;
		}
		if (v == mHome) {
			mListener.onCardEvent(this, ICardEvent.HOME_CLICK);
		}
		// else if (v == mDel)
		// {
		// // 先不恢复，让父容器恢复
		// // clearClickState();
		// mListener.onCardEvent(this, ICardEvent.DEL_CLICK);
		// }
		else if (v == this) {
			//add by jiang 设置为默认桌面后  按home键跳屏幕预览
			if (SensePreviewFrame.sIsHOME) {
				mListener.onCardEvent(this, ICardEvent.HOME_CLICK);
				SensePreviewFrame.sIsHOME = false;
			}
			clearClickState();
			if (mType != TYPE_ADD) {
				// 如果屏幕正在加载中，点击直接返回。不响应点击添加事件
				if (!mListener.ismEnableUpdate()) {
					return;
				}
				mListener.onCardEvent(this, ICardEvent.PREVIEW_CLICK);
			} else {
				mListener.onCardEvent(this, ICardEvent.ADD_CLICK);
			}
		}
	}

	/**
	 * 卡片事件监听
	 * 
	 * @author yuankai
	 * @version 1.0
	 */
	public interface ICardEventListener {
		/**
		 * 发生了事件回调
		 * 
		 * @param layout
		 *            卡片View
		 * @param event
		 *            事件ID
		 */
		public void onCardEvent(CardLayout layout, int event);

		public Bundle getGoWidgetData();

		public boolean ismEnableUpdate();
	}

	void enableDrawingCache() {
		if (mDrawingCacheEnabled) {
			return;
		}
		setDrawingCacheEnabled(true);
		setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		try {
			buildDrawingCache(true);
			mDrawingCacheEnabled = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (!changed || mType == TYPE_ADD) {
			return;
		}
//		Point delAnchor = mPreviewController.getmDelTopAndLeftPoint();
		if (null != mRoom) {
			mRoom.layout(0, 0, mWorkspace.mRoomWidth, mWorkspace.mRoomHeight);
		}
		Point homeAnchor = mPreviewController.getmHomeTopAndLeftPoint();
		if (homeAnchor.x == 0 && homeAnchor.y == 0) {
			// 主题没指定margin,以默认方式计算
			mHome.layout(mWorkspace.mHomeImageViewLeft, SenseWorkspace.sHomeImageViewTop,
					mWorkspace.mHomeImageViewLeft + mWorkspace.mHomeImageWidth,
					SenseWorkspace.sHomeImageViewTop + mWorkspace.mHomeImageHeight);
		} else {
			mHome.layout(homeAnchor.x, homeAnchor.y, homeAnchor.x + mWorkspace.mHomeImageWidth,
					homeAnchor.y + mWorkspace.mHomeImageHeight);
		}

	}

	final int getType() {
		return mType;
	}

	void clearClickState() {
		if (mType != TYPE_ADD) {
			setBackgroundDrawable(mIsCurrent ? mWorkspace.mBorderLightImg : mWorkspace.mBorderImg);
		} else {
			setBackgroundDrawable(mWorkspace.mAddImg);
		}
	}

	private Animation getZoomInAnimation() {
		if (mWorkspace.mZoomInAnimation == null) {
			mWorkspace.mZoomInAnimation = new ScaleAnimation(1, ENLARGE_FACTOR, 1, ENLARGE_FACTOR,
					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			mWorkspace.mZoomInAnimation.setInterpolator(new OvershootInterpolator(0));
			mWorkspace.mZoomInAnimation.setDuration(250);
			mWorkspace.mZoomInAnimation.setFillAfter(true);
			mWorkspace.mZoomInAnimation.setFillEnabled(true);
		}
		return mWorkspace.mZoomInAnimation;
	}

	private Animation getZoomOutAnimation() {
		if (mWorkspace.mZoomOutAnimation == null) {
			mWorkspace.mZoomOutAnimation = new ScaleAnimation(ENLARGE_FACTOR, 1, ENLARGE_FACTOR, 1,
					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			mWorkspace.mZoomOutAnimation.setInterpolator(new OvershootInterpolator(0));
			mWorkspace.mZoomOutAnimation.setDuration(1); // 设置较小的值，立即缩小
		}
		return mWorkspace.mZoomOutAnimation;
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		postDelayed(mRunnable, 2000); // 强制刷新一次更新UI
	}

	/**
	 * 
	 * <br>类描述:刷新卡片
	 * <br>功能详细描述:
	 * 
	 * @author  maxiaojun
	 * @date  [2012-10-9]
	 */
	private class RefrashCardThread implements Runnable {
		@Override
		public void run() {
			CardLayout.this.invalidate();
		}
	}

	public void setRed() {
		setBackgroundDrawable(mWorkspace.mLightNoRoomImg);
	}

	public void setNormal() {
		setBackgroundDrawable(mWorkspace.mBorderImg);
	}

	public void setAdd() {
		setBackgroundDrawable(mWorkspace.mAddImg);
	}

	public void setNoRoom() {
		synchronized (this) {
			sDrawRoom = true;
		}
		mRoom.setImageDrawable(null);
		mRoom.setBackgroundDrawable(mWorkspace.mNoRoomImg);
		mRoom.postInvalidate();
	}

	public void setLightNoRoom() {
		synchronized (this) {
			sDrawRoom = true;
		}
		mRoom.setImageDrawable(null);
		mRoom.setBackgroundDrawable(mWorkspace.mLightNoRoomImg);
		mRoom.postInvalidate();
	}

	public void selfDestruct() {
		setEventListener(null);
		if (mWorkspace.mBorderDragImg != null) {
			mWorkspace.mBorderDragImg.setCallback(null);
		}
		if (mWorkspace.mAddImg != null) {
			mWorkspace.mAddImg.setCallback(null);
		}
		if (mWorkspace.mBorderLightImg != null) {
			mWorkspace.mBorderLightImg.setCallback(null);
		}
		if (mWorkspace.mBorderImg != null) {
			mWorkspace.mBorderImg.setCallback(null);
		}
		// if(BorderDelImg != null){
		// BorderDelImg.setCallback(null);
		// }
		if (mWorkspace.mLightAddImg != null) {
			mWorkspace.mLightAddImg.setCallback(null);
		}
		if (mWorkspace.mLightNoRoomImg != null) {
			mWorkspace.mLightNoRoomImg.setCallback(null);
		}
	}
}
