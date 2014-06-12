package com.jiubang.ggheart.appgame.widget;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ViewSwitcher;

import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.appcenter.help.AppCacheManager;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;

/**
 * 显示应用或游戏icon
 * 
 * @author zhoujun
 * 
 */
public class AppGameWidgetIconView extends ViewSwitcher {

	private static final int UPDATE_DATA_FOR_CURRENT_VIEW = 0;
	private static final int UPDATE_DATA_FOR_PREFIX_VIEW = -1;
	private static final int UPDATE_DATA_FOR_NEXT_VIEW = 1;
	private Context mContext;
	private ArrayList<ClassificationDataBean> mWidgetDataList;
	private int mCurrPosition = 0;
	/**
	 * 上一次滑动方向，默认是向下
	 */
	private boolean mLastToNext = false;

	private Handler mHandler;

	public AppGameWidgetIconView(Context context, OnLongClickListener onlongClick, Handler handler) {
		super(context);
		mContext = context;
		mHandler = handler;
		initView(onlongClick);
	}

	private void initView(OnLongClickListener onlongClick) {
		AppGameWidgetAppIconView fisrtAppIconLinear = new AppGameWidgetAppIconView(mContext,
				onlongClick);
		AppGameWidgetAppIconView secordAppIconLinear = new AppGameWidgetAppIconView(mContext,
				onlongClick);
		this.addView(fisrtAppIconLinear);
		this.addView(secordAppIconLinear);
	}

	public void updateData(ArrayList<ClassificationDataBean> widgetDataList) {
		mWidgetDataList = widgetDataList;
		updateViewData(UPDATE_DATA_FOR_CURRENT_VIEW);
	}

	private void updateViewData(int viewType) {
		if (mWidgetDataList == null || mWidgetDataList.size() <= 0) {
			return;
		}
		AppGameWidgetAppIconView appIconView = (AppGameWidgetAppIconView) this.getCurrentView();
		if (viewType != UPDATE_DATA_FOR_CURRENT_VIEW) {
			appIconView = (AppGameWidgetAppIconView) this.getNextView();
			if (viewType == UPDATE_DATA_FOR_NEXT_VIEW) {
				mCurrPosition++;
				if (mCurrPosition >= mWidgetDataList.size()) {
					mCurrPosition = 0;
				}
			} else {
				mCurrPosition--;
				if (mCurrPosition < 0) {
					mCurrPosition = mWidgetDataList.size() - 1;
				}
			}
		}
		if (appIconView != null) {
			updateData(appIconView, mCurrPosition);
		}
	}

	private void updateData(AppGameWidgetAppIconView appIconView, int position) {
		if (mWidgetDataList != null && mWidgetDataList.size() > 0
				&& position < mWidgetDataList.size()) {
			ClassificationDataBean dataBean = mWidgetDataList.get(position);
			if (dataBean != null && dataBean.featureList != null) {
				appIconView.updateData(dataBean);
			}
		}
	}

	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private int mTouchState = TOUCH_STATE_REST;
	private float mLastMotionY;
	private int mTouchSlop = 10;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}

		final float y = ev.getY();

		switch (action) {
			case MotionEvent.ACTION_MOVE :
				final int yDiff = (int) Math.abs(y - mLastMotionY);
				boolean yMoved = yDiff > mTouchSlop;

				if (yMoved) {
					mTouchState = TOUCH_STATE_SCROLLING;
				}
				break;

			case MotionEvent.ACTION_DOWN :
				mLastMotionY = y;
				mTouchState = TOUCH_STATE_REST;
				break;

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP :
				// mTouchState = TOUCH_STATE_REST;
				break;
		}
		return mTouchState != TOUCH_STATE_REST;
	}

	private void scrollToNext(boolean isNext) {
		mLastToNext = isNext;
		if (!localDataIsExist() && !Machine.isNetworkOK(getContext())) {
			onNoWidgetData();
		} else {
			if (isNext) {
				// 设置View进入屏幕时候使用的动画
				this.setInAnimation(inFromBottomAnimation());
				// 设置View退出屏幕时候使用的动画
				this.setOutAnimation(outToZoomOutAnimation());
				//向上滑，应该是加载下一条，这里加载上一条
				updateViewData(UPDATE_DATA_FOR_PREFIX_VIEW);
				this.showPrevious();
			} else {
				// 向下滑动时
				this.setInAnimation(inFromZoomInAnimation());
				this.setOutAnimation(outToTopAnimation());
				//向下滑，应该是加载上一条，这里加载下一条
				updateViewData(UPDATE_DATA_FOR_NEXT_VIEW);
				this.showNext();
			}
		}
	}

	private boolean localDataIsExist() {
		//		if (!FileUtil.isFileExist(AppGameWidgetDataProvider.APPGAME_WIDGET_DATA_LOCAL_PATH)) {
		//			return false;
		//		}
		//		return true;
		AppCacheManager acm = AppCacheManager.getInstance();
		if (acm.isCacheExist(AppGameWidgetDataProvider.KEY_CACHE_WIDGET)) {
			return true;
		}
		return false;
	}

	/**
	 * <br>功能简述: 当widget数据不存在时，通知container显示网络异常界面
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void onNoWidgetData() {
		if (mHandler != null) {
			mHandler.sendEmptyMessage(1);
		}
	}
	/**
	 * <br>功能简述: 切换widget内容显示
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void refreshView() {
		scrollToNext(mLastToNext);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final float y = event.getY();
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				if (y - mLastMotionY > 0) {
					scrollToNext(false);
				} else {
					scrollToNext(true);
				}
			}
			mTouchState = TOUCH_STATE_REST;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 从上退出时，缩小动画
	 * 
	 * @return
	 */
	protected Animation outToZoomOutAnimation() {

		Animation inFromRight = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		inFromRight.setDuration(500);
		inFromRight.setInterpolator(new AccelerateInterpolator());

		return inFromRight;
	}

	/** * 定义从低边进入的动画效果 * @return */
	protected Animation inFromBottomAnimation() {
		// AnimationSet animationSet = new AnimationSet(false);
		Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setStartOffset(200);
		outtoLeft.setDuration(500);
		outtoLeft.setInterpolator(new AccelerateInterpolator());

		return outtoLeft;
	}

	/**
	 * 从上进入时，放大动画
	 * 
	 * @return
	 */
	protected Animation inFromZoomInAnimation() {

		Animation inFromRight = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		inFromRight.setDuration(500);
		inFromRight.setInterpolator(new AccelerateInterpolator());

		return inFromRight;
	}

	/** * 定义从上方退出时的动画效果 * @return */
	protected Animation outToTopAnimation() {
		Animation outtoRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, +1.0f);

		outtoRight.setDuration(500);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}

}
