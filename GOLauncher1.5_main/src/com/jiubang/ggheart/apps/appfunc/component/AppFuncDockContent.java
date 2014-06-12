package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;
import android.content.Context;

import com.jiubang.core.mars.XALinear;
import com.jiubang.core.mars.XMotion;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
/**
 * 
 * <br>类描述:功能表组件
 * <br>功能详细描述:
 * 
 * @author  huangshaotao
 * @date  [2012-9-26]
 */
public abstract class AppFuncDockContent extends XPanel {
	public static final int HIDE_TYPE_TOP = 0; //隐藏方向，向上移动隐藏
	public static final int HIDE_TYPE_BOTTOM = 1; //隐藏方向，向下移动隐藏
	
	protected Activity mActivity;
	/**
	 * 点击时的动画
	 */
	protected XMotion mMotion;
	/**
	 * 动画总步长
	 */
	protected int mTotalStep;
	/**
	 * 当前步数
	 */
	protected int mCurrentStep;

	/**
	 * 主题控制器
	 */
	protected AppFuncThemeController mThemeCtrl;

	protected AppFuncUtils mUtils;

	/**
	 * 隐藏方向类型，是在屏幕顶部还是在屏幕底部
	 */
	private int mHideType = HIDE_TYPE_BOTTOM;
	
	public AppFuncDockContent(Context context, int tickCount, int x, int y, int width, int height, int type) {
		super(tickCount, x, y, width, height);
		mHideType = type;
		mActivity = (Activity) context;
		mTotalStep = 3;
		mCurrentStep = 0;
		mUtils = AppFuncUtils.getInstance(mActivity);
		mThemeCtrl = AppFuncFrame.getThemeController();
	}

	public void startAnimation(int dstX, int dstY) {
		if ((mMotion != null) && (mMotion.isFinished() == false)) {
			mMotion.stop();
			mMotion = null;
		}
		if (mX == dstX && mY == dstY) {
			return;
		}
		mCurrentStep = 0;
		mMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, mX, mY, dstX, dstY, mTotalStep, 0, 0);
		setMotionFilter(mMotion);
	}

	public XMotion getMotion() {
		return mMotion;
	}

	@Override
	protected boolean animate() {
		boolean ret = false;
		if (mMotion != null) {
			if (mCurrentStep < mTotalStep) {
				mCurrentStep++;
				ret = true;
			} else {
				detachAnimator(mMotion);
				if (mListener != null) {
					mListener.onFinish(mMotion);
				}
				mMotion = null;
				mCurrentStep = 0;
			}
		}
		return ret;
	}

	public boolean isInMotion() {
		if ((mMotion != null) && (mMotion.isFinished() == false)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isInScreen() {
		// AppFuncUtils instance = AppFuncUtils.getInstance(mActivity);
		
		if (mHideType == HIDE_TYPE_TOP) {
			if ((mX > -getAttachPanel().getWidth()) && (mY > -getAttachPanel().getHeight())) {
				return true;
			}
		} else {
			if ((mX < getAttachPanel().getWidth()) && (mY < getAttachPanel().getHeight())) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * 复位到某一位置
	 */
	public void reset(int x, int y) {
		if ((mMotion != null) && (mMotion.isFinished() == false)) {
			mMotion.stop();
			mMotion = null;
		}
		mX = x;
		mY = y;
	}

	public void resetMotion() {
		if (mMotion != null) {
			detachAnimator(mMotion);
			mMotion.stop();
			mMotion = null;
			mCurrentStep = 0;
		}
	}

	/**
	 * 重置资源文件
	 */
	public abstract void resetResource();

	/**
	 * 加载资源文件
	 */
	public abstract void loadResource(String packageName);
	
	/**
	 * <br>功能简述:获取该组件的隐藏方向类型
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public int getHideType() {
		return mHideType;
	}
}
