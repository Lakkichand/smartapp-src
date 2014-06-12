package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.data.AppCore;

/**
 * 正在运行tab页，编辑状态下的dock视图
 * 
 * @author yangbing
 */
public class ProManageEditDock extends AppFuncDockContent implements IMsgHandler {

	private Drawable mRunningInfoImg; // 程序信息图标
	private Drawable mRunningLockImg; // 锁定程序图标
	private Drawable mRunningUnLockImg; // 解锁程序图标
	private Drawable mLineV; // 分割线
	private Drawable mLineH; // 分割线
	private Drawable editDockBgV; // 编辑状态下的背景(竖屏)
	private Drawable editDockBgH; // 编辑状态下的背景（横屏）
	private Drawable editDockTouchBgV; // 编辑状态下被触摸的背景(竖屏)
	private Drawable editDockTouchBgH; // 编辑状态下被触摸的背景（横屏）
	private String mRunningInfoText;
	private String mRunningLockText;
	private Rect mLineRect = new Rect(); // 分割线绘制区域
	private Rect mInfoRect = new Rect(); // 程序信息图标绘制区域
	private Rect mLockRect = new Rect(); // 锁定程序图标绘制区域
	private Rect mEditTouchBgRect = new Rect(); // 被触摸背景绘制区域
	private Rect mEditBgRect = new Rect(); // 背景绘制区域
	private int itemSize = 0;
	private Activity mActivity;
	private Paint mPaint;
	private int mFontHeight; // 文字的高度
	private AppFuncUtils mUtils = null;
	private boolean mInvalidated;
	private boolean isLockingText = true; // 是否为锁定程序
	private boolean isDrawEditBg = false; // 拖动图标到底部时绘制背景
	private int bgType = 0; // 背景色绘制区域（1-程序信息，2-锁定程序）
	private int mTopPadding; // 上边距
	private int mBottomPadding; // 下边距
	private int mPadding; // 边距30
	private int mPicSize; // 图片的大小
	private float mPortraitInfoStartX;
	private float mPortraitLockStartX;
	private float mPortraitStartY;
	private float mLandInfoStartY;
	private float mLandLockStartY;
	private float mLandStartX;

	public ProManageEditDock(Activity activity, int tickCount, int x, int y, int width, int height) {
		super(activity, tickCount, x, y, width, height, HIDE_TYPE_TOP);
		mActivity = activity;
		mTotalStep = 10;
		mCurrentStep = 0;
		mUtils = AppFuncUtils.getInstance(mActivity);
		
		DeliverMsgManager.getInstance().registerMsgHandler(AppFuncConstants.PRO_MANAGE_EDIT_DOCK,
				this);
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		super.layout(left, top, right, bottom);
		calculatePadding();
		mEditTouchBgRect.setEmpty();
		if (mUtils.isVertical()) {
			// 竖屏
			mInfoRect.set(mPadding, mTopPadding, mPadding + mPicSize, mBottomPadding);
			mLineRect.set(mWidth / 2 - itemSize, 0, mWidth / 2 + itemSize, mHeight);
			mLockRect.set(mWidth / 2 + mPadding, mTopPadding, mWidth / 2 + mPadding + mPicSize,
					mBottomPadding);
			mEditBgRect.set(0, 0, mWidth, mHeight);
		} else {
			// 横屏
			mLockRect.set(mTopPadding, mPadding, mBottomPadding, mPadding + mPicSize);
			mLineRect.set(0, mHeight / 2 - itemSize, mWidth, mHeight / 2 + itemSize);
			mInfoRect.set(mTopPadding, mHeight / 2 + mPadding, mBottomPadding, mHeight / 2
					+ mPadding + mPicSize);
			mEditBgRect.set(0, 0, mWidth, mHeight);

		}
		calculateTextStartXY();
	}

	/**
	 * 背景色绘制区域
	 * */
	private void BgRectlayout() {
		if (mUtils.isVertical()) {
			if (bgType == 1) {
				mEditTouchBgRect.set(0, 0, mWidth / 2, mHeight);
				// mEditBgRect.set(mWidth/2, 0, mWidth, mHeight);
			} else if (bgType == 2) {
				mEditTouchBgRect.set(mWidth / 2, 0, mWidth, mHeight);
				// mEditBgRect.set(0, 0, mWidth/2, mHeight);
			}
		} else {
			if (bgType == 1) {
				mEditTouchBgRect.set(0, 0, mWidth, mHeight / 2);
				// mEditBgRect.set(0, mHeight/2, mWidth, mHeight);
			} else if (bgType == 2) {
				mEditTouchBgRect.set(0, mHeight / 2, mWidth, mHeight);
				// mEditBgRect.set(0, 0, mWidth, mHeight/2);
			}

		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		canvas.save();

		if (mUtils.isVertical()) {
			editDockBgV.setBounds(mEditBgRect);
			editDockBgV.draw(canvas);
			if (isDrawEditBg) {
				editDockTouchBgV.setBounds(mEditTouchBgRect);
				editDockTouchBgV.draw(canvas);
			}
		} else {
			editDockBgH.setBounds(mEditBgRect);
			editDockBgH.draw(canvas);
			if (isDrawEditBg) {
				editDockTouchBgH.setBounds(mEditTouchBgRect);
				editDockTouchBgH.draw(canvas);
			}
		}

		if (mRunningInfoImg != null) {
			mRunningInfoImg.setBounds(mInfoRect);
			mRunningInfoImg.draw(canvas);
		}

		if (mUtils.isVertical()) {
			// mLineV.setBounds(mLineRect);
			// mLineV.draw(canvas);
			if (mLineV != null) {
				ImageUtil.drawImage(canvas, mLineV, ImageUtil.CENTERMODE, mLineRect.left,
						mLineRect.top, mLineRect.right, mLineRect.bottom, null);
			} else if (mLineH != null) {
				canvas.save();
				canvas.translate(mLineRect.left, mLineRect.top);
				canvas.rotate(-90.0f);
				canvas.translate(-mLineRect.height(), 0);
				ImageUtil.drawImage(canvas, mLineH, ImageUtil.CENTERMODE, 0, 0, mLineRect.height(),
						mLineRect.width(), null);
				canvas.restore();
			}
		} else {
			// mLineH.setBounds(mLineRect);
			// mLineH.draw(canvas);
			if (mLineH != null) {
				ImageUtil.drawImage(canvas, mLineH, ImageUtil.CENTERMODE, mLineRect.left,
						mLineRect.top, mLineRect.right, mLineRect.bottom, null);
			} else if (mLineV != null) {
				canvas.save();
				canvas.translate(mLineRect.left, mLineRect.top);
				canvas.rotate(-90.0f);
				canvas.translate(-mLineRect.height(), 0);
				ImageUtil.drawImage(canvas, mLineV, ImageUtil.CENTERMODE, 0, 0, mLineRect.height(),
						mLineRect.width(), null);
				canvas.restore();
			}
		}
		if (isLockingText && mRunningLockImg != null) {
			mRunningLockImg.setBounds(mLockRect);
			mRunningLockImg.draw(canvas);
		} else if (mRunningUnLockImg != null) {
			mRunningUnLockImg.setBounds(mLockRect);
			mRunningUnLockImg.draw(canvas);
		}
		drawText(canvas);

	}

	/**
	 * 绘制文字
	 * */
	private void drawText(Canvas canvas) {

		if (mUtils.isVertical()) {
			// 竖屏
			canvas.drawText(mRunningInfoText, mPortraitInfoStartX, mPortraitStartY, mPaint);
			canvas.drawText(mRunningLockText, mPortraitLockStartX, mPortraitStartY, mPaint);

		} else {
			// 横屏
			canvas.save();
			canvas.rotate(-90);
			canvas.drawText(mRunningLockText, mLandLockStartY, mLandStartX, mPaint);
			canvas.drawText(mRunningInfoText, mLandInfoStartY, mLandStartX, mPaint);
			canvas.restore();
		}
	}

	/**
	 * dock锁定程序，取消锁定之间的切换
	 * 
	 * @param changeToLock
	 *            true，切换到锁定程序 false ,切换到取消锁定
	 * 
	 * */
	public void changeLockText(boolean changeToLock) {
		if (changeToLock) {
			if (!isLockingText) {
				mRunningLockText = mActivity.getResources().getString(R.string.btns_runninglock);
				isLockingText = true;
				mInvalidated = true;
			}
		} else {
			if (isLockingText) {
				mRunningLockText = mActivity.getResources().getString(R.string.btns_runningunlock);
				isLockingText = false;
				mInvalidated = true;
			}
		}

	}

	/**
	 * 加载资源
	 * */
	private void initResource(String packageName) {
		// 初始化图片
		mRunningInfoImg = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeRunningInfoImg, packageName);
		mRunningLockImg = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeRunningLockImg, packageName);
		mLineV = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mRuningDockBean.mHomeLineImgV,
				packageName);
		mLineH = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mRuningDockBean.mHomeLineImgH,
				packageName);
		editDockBgV = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeEditDockBgV, packageName);
		editDockBgH = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeEditDockBgH, packageName);
		editDockTouchBgV = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeEditDockTouchBgV, packageName);
		editDockTouchBgH = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeEditDockTouchBgH, packageName);
		mRunningUnLockImg = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeRunningUnLockImg, packageName);
		// 初始化文字
		mRunningInfoText = mActivity.getResources().getString(R.string.btns_runninginfo);
		mRunningLockText = mActivity.getResources().getString(R.string.btns_runninglock);
		// 初始化边距
		itemSize = mUtils.getStandardSize(1);
		mPicSize = mUtils.getStandardSize(48);
		mPadding = mUtils.getScaledSize(30);
		// 初始化画笔
		int mTxtSize = mUtils.getStandardSize(22.0f);
		int mTxtColor = 0xFFFFFFFF;
		mPaint = new Paint();
		mPaint.setTextSize(mTxtSize);
		mPaint.setColor(mTxtColor);
		mPaint.setAntiAlias(true);
		mFontHeight = getFontHeight(mPaint) * itemSize;

	}

	/**
	 * 
	 * 计算边距
	 * */
	private void calculatePadding() {
		if (mUtils.isVertical()) {
			mTopPadding = (mHeight - mPicSize) / 2;
			mBottomPadding = mTopPadding + mPicSize;
		} else {
			mTopPadding = (mWidth - mPicSize) / 2;
			mBottomPadding = mTopPadding + mPicSize;
		}
	}

	@Override
	public void resetResource() {
		isDrawEditBg = false;

	}

	@Override
	public void loadResource(String packageName) {
		initResource(packageName);

	}

	@Override
	protected boolean animate() {
		boolean ret = false;
		if (mInvalidated) {
			mInvalidated = false;
			ret = true;
		}

		return super.animate() || ret;
	}

	/**
	 * 绘制背景色
	 * */
	public void drawEditDockBg(int x, int y) {
		if (mUtils.isVertical()) {
			if (x < mWidth / 2) {
				bgType = 1;
			} else {
				bgType = 2;
			}
		} else {
			if (y < mHeight / 2) {
				bgType = 1;
			} else {
				bgType = 2;
			}

		}
		BgRectlayout();
		isDrawEditBg = true;
		mInvalidated = true;

	}

	/**
	 * 消除背景色
	 * */
	public void clearEditDockBg() {
		isDrawEditBg = false;
		mInvalidated = true;

	}

	/**
	 * 处理图片拖动到下方事件
	 * */
	public void dealOverlapEvent(boolean isToInfo, Intent intent) {
		if (isToInfo) {
			goToInfo(intent);
		} else {
			if (isLockingText) {
				lockApp(intent);
			} else {
				unLockApp(intent);
			}
		}
		clearEditDockBg();
	}

	/**
	 * 
	 * 初始化文字绘制坐标
	 * */
	private void calculateTextStartXY() {
		if (mUtils.isVertical()) {
			// 竖屏
			mPortraitInfoStartX = mInfoRect.right + 20 * itemSize;
			mPortraitLockStartX = mLockRect.right + 20 * itemSize;
			mPortraitStartY = mHeight / 2 + mFontHeight / 3;

		} else {
			// 横屏
			mLandStartX = mWidth / 2 + mFontHeight / 3;
			float infoTextLength = mPaint.measureText(mRunningInfoText);
			float lockIextLength = mPaint.measureText(mRunningLockText);
			mLandLockStartY = -mLockRect.bottom - 20 * itemSize - lockIextLength;
			mLandInfoStartY = -mInfoRect.bottom - 20 * itemSize - infoTextLength;

		}

	}

	/**
	 * 
	 * 获取文字高度
	 * */
	public int getFontHeight(Paint paint) {
		FontMetrics fm = paint.getFontMetrics();
		return (int) Math.ceil(fm.descent - fm.ascent);

	}

	/**
	 * 查看程序信息
	 * */
	public void goToInfo(Intent intent) {
		ApplicationIcon.sIsStartApp = true;
		AppCore.getInstance().getTaskMgrControler().skipAppInfobyIntent(intent);
	}

	/**
	 * 锁定程序
	 * */
	public void lockApp(Intent intent) {
		AppCore.getInstance().getTaskMgrControler().addIgnoreAppItem(intent);
	}

	/**
	 * 解除锁定
	 * */
	public void unLockApp(Intent intent) {
		AppCore.getInstance().getTaskMgrControler().delIgnoreAppItem(intent);
	}

	@Override
	public void notify(int key, Object obj) {
		switch (key) {
		case AppFuncConstants.PRO_MANAGE_EDIT_DOCK_CLEAR_EDIT_DOCK_BG:
			clearEditDockBg();
			break;
		case AppFuncConstants.PRO_MANAGE_EDIT_DOCK_CHANGE_LOCK_TEXT:
			changeLockText((Boolean) obj);
			break;
		}
	}
}
