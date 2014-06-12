package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;
import com.jiubang.core.mars.IAnimateListener;
import com.jiubang.core.mars.XALinear;
import com.jiubang.core.mars.XAnimator;
import com.jiubang.ggheart.apps.appfunc.common.component.CommonProgressBar;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager.AnimationInfo;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.data.AppCore;

/**
 * 内存条
 * @author yangguanxiang
 *
 */
public class MemoryBar extends CommonProgressBar {
	private AppFuncUtils mUtils;
	private AppFuncThemeController mThemeCtrl;
	private Drawable mMemoryProcessLow;
	private Drawable mMemoryProcessMiddle;
	private Drawable mMemoryProcessHigh;
	private long mTotalMemory;
	private long mUsedMemory;
	private float mMemoryPercent;
	private IAnimateListener mRefreshListener;
	private XALinear mRefreshMotion;
	private float mMinProgressRate = 0.1f;
	private long mTargetMemory;
	private boolean mLastAnimation;
	private Rect mTextRect = new Rect();
	private StringBuilder mInfoBuilder = new StringBuilder();

	public MemoryBar(Activity activity, int tickCount, int x, int y, int width, int height,
			int orientation) {
		super(activity, tickCount, x, y, width, height, orientation, 0, 0);
		init();
	}

	private void init() {
		mUtils = AppFuncUtils.getInstance(mContext);
		mThemeCtrl = AppFuncFrame.getThemeController();
		int txtSize = mUtils.getStandardSize(18);
		int txtColor = mThemeCtrl.getThemeBean().mRuningDockBean.mHomeTextColor;
		mPaint.setTextSize(txtSize);
		mPaint.setColor(txtColor);
		mPaint.setAntiAlias(true);
		mRefreshListener = new IAnimateListener() {

			@Override
			public void onStart(XAnimator animator) {
			}

			@Override
			public void onProgress(XAnimator animator, int progress) {
				updateProgress();
			}

			@Override
			public void onFinish(XAnimator animator) {
				if (mLastAnimation) {
					calculateMemory();
					requestLayout();
					mRefreshMotion = null;
					mLastAnimation = false;
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.PROMANAGEHOMEICON,
							AppFuncConstants.MEMORY_REFRESH_FINISHED, null);
				} else {
					mLastAnimation = true;
					updateProgress();
					startAnimation(mUsedMemory, mTargetMemory);
				}
			}
		};
	}

	private void updateProgress() {
		if (mRefreshMotion != null) {
			mProgress = mUsedMemory = mRefreshMotion.GetCurX();
			requestLayout();
		}
	}

	public void initResource(String packageName) {
		mMaxDrawable = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeMemoryBg, packageName);
		mMemoryProcessLow = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeMemoryProcessLow, packageName);
		mMemoryProcessMiddle = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeMemoryProcessMiddle, packageName);
		mMemoryProcessHigh = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeMemoryProcessHigh, packageName);
	}

	private Drawable getMemoryProgressDrawable() {
		if (mMemoryPercent < 0.7f) {
			return mMemoryProcessLow;
		} else if (mMemoryPercent > 0.7f && mMemoryPercent < 0.9f) {
			if (mMemoryProcessMiddle != null) {
				return mMemoryProcessMiddle;
			} else {
				return mMemoryProcessLow;
			}

		} else {
			if (mMemoryProcessHigh != null) {
				return mMemoryProcessHigh;
			} else {
				return mMemoryProcessLow;
			}
		}
	}

	/**
	 * 计算内存值
	 * */
	private void calculateMemory() {
		long availableMemory = AppCore.getInstance().getTaskMgrControler().retriveAvailableMemory() / 1024;
		while (mTotalMemory == 0) {
			mTotalMemory = AppCore.getInstance().getTaskMgrControler().retriveTotalMemory() / 1024;
		}

		mUsedMemory = mTotalMemory - availableMemory;
		mMemoryPercent = 1.0f * mUsedMemory / mTotalMemory;
		mMaxProgress = mTotalMemory;
		mProgress = mUsedMemory;
	}

	public void refresh(boolean needAnimation) {
		if (needAnimation) {
			if (mRefreshMotion != null) {
				AnimationManager.getInstance(mActivity).cancelAnimation(this, mRefreshMotion);
//				detachAnimator(mRefreshMotion);
				mRefreshMotion = null;
				mLastAnimation = false;
				calculateMemory();
				requestLayout();
			} else {
				long oldValue = mUsedMemory;
				calculateMemory();
				mTargetMemory = mUsedMemory;
				long minMemory = (long) (mTotalMemory * mMinProgressRate);
				if (oldValue < minMemory) {
					mLastAnimation = true;
					startAnimation(minMemory, mTargetMemory);
				} else {
					startAnimation(oldValue, minMemory);
				}
			}
		} else {
			calculateMemory();
			requestLayout();
		}
	}

	private void startAnimation(long oldValue, long newValue) {
		if (mRefreshMotion != null) {
			AnimationManager.getInstance(mActivity).cancelAnimation(this, mRefreshMotion);
//			detachAnimator(mRefreshMotion);
		}
		mRefreshMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, Long.valueOf(oldValue)
				.intValue(), 0, Long.valueOf(newValue).intValue(), 0, 35, 2, 0);
		AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this, mRefreshMotion,
				mRefreshListener);
		AnimationManager.getInstance(mActivity).attachAnimation(animInfo, null);
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		if (mOrientation == ORIENTATION_HORIZONTAL) {
			mPaddingLeft = mPaddingRight = mUtils.getStandardSize(15);
			mPaddingBottom = mPaddingTop = 0;
		} else {
			mPaddingBottom = mPaddingTop = mUtils.getStandardSize(15);
			mPaddingLeft = mPaddingRight = 0;
		}
		super.layout(left, top, right, bottom);
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		mPrgDrawable = getMemoryProgressDrawable();
		super.drawCurrentFrame(canvas);
		drawText(canvas);
	}

	private void drawText(Canvas canvas) {
		if (GoLauncher.isPortait()) {
			// 竖屏
			mInfoBuilder.delete(0, mInfoBuilder.length());
			mInfoBuilder.append(mContext.getString(R.string.btns_memory));
			mInfoBuilder.append(mUsedMemory).append("M");
			mInfoBuilder.append("/");
			mInfoBuilder.append(mTotalMemory).append("M");
			String info = mInfoBuilder.toString();
			mPaint.getTextBounds(info, 0, info.length(), mTextRect);
			int textX = mPaddingLeft + mUtils.getStandardSize(10);
			int textY = Math.round(mHeight - (mHeight - mTextRect.height()) / 2.0f);
			canvas.drawText(info, textX, textY, mPaint);
		} else {
			// 横屏
			mInfoBuilder.delete(0, mInfoBuilder.length());
			String totalMemory = mInfoBuilder.append(mTotalMemory).append("M").toString();
			mPaint.getTextBounds(totalMemory, 0, totalMemory.length(), mTextRect);
			int textX = (int) ((mWidth - mTextRect.width()) / 2.0f);
			int textY = mMaxDrawableRect.bottom - mUtils.getStandardSize(5);
			canvas.drawText(totalMemory, textX, textY, mPaint);

			mInfoBuilder.delete(0, mInfoBuilder.length());
			String usedMemory = mInfoBuilder.append(mUsedMemory).append("M").toString();
			mPaint.getTextBounds(usedMemory, 0, usedMemory.length(), mTextRect);
			textX = (int) ((mWidth - mTextRect.width()) / 2.0f);
			textY = mMaxDrawableRect.bottom - mProgressHeightV + mUtils.getStandardSize(5)
					+ mTextRect.height();
			canvas.drawText(usedMemory, textX, textY, mPaint);
		}

	}

}
