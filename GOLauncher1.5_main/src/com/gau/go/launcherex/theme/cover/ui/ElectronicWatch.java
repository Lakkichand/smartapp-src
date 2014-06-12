package com.gau.go.launcherex.theme.cover.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  
 * @date  [2012-11-10]
 */
public class ElectronicWatch implements OnResponTouchListener, ICleanable, IMovable {
	private Context mContext;
	public boolean mAllowDrag; //是否允许拖动

	private int mScreenWidth;
	private int mScreenHeight;

	private float mCurrY; //组件Y坐标
	private float mCurrX; //组件X坐标

	public float mStartPerX; //开始位置（相对表盘图片的比例值）
	public float mStartPerY;

	public boolean mLimit; //是否有限制拖动范围
	public float mLimitLeftPer; //限制可拖动范围
	public float mLimitRightPer;
	public float mLimitTopPer;
	public float mLimitBottomPer;

	public int mLimitLeft; //限制拖动范围
	public int mLimitRight;
	public int mLimitTop;
	public int mLimitBottom;

	private int mStartX; //开始位置
	private int mStartY;

	public Bitmap mEleWatchImg; //热气球
	private int mEleWatchWidth;
	private int mEleWatchHeight;

	public ElectronicWatch(int screenWidth, int screenHeight) {

	}

	public void init(Context context) {
		mContext = context;

		if (mEleWatchImg != null) {
			mEleWatchWidth = mEleWatchImg.getWidth();
			mEleWatchHeight = mEleWatchImg.getHeight();
		}

		mStartX = (int) (mStartPerX * mScreenWidth);
		mStartY = (int) (mStartPerY * mScreenHeight);

		mCurrX = mStartX;
		mCurrY = mStartY;

		if (mLimit) {
			mLimitLeft = (int) (mScreenWidth * mLimitLeftPer);
			mLimitRight = (int) (mScreenWidth * mLimitRightPer - mEleWatchWidth);
			mLimitTop = (int) (mScreenHeight * mLimitTopPer);
			mLimitBottom = (int) (mScreenHeight * mLimitBottomPer - mEleWatchHeight);
		} else {
			mLimitLeft = 0;
			mLimitRight = mScreenWidth - mEleWatchWidth;
			mLimitTop = 0;
			mLimitBottom = mScreenHeight - mEleWatchHeight;
		}

	}

	/**
	 * 重新设置数据
	 */
	public void resetData(int screenWidth, int screenHeight) {
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;

		mStartX = (int) (mStartPerX * mScreenWidth);
		mStartY = (int) (mStartPerY * mScreenHeight);

		mCurrX = mStartX;
		mCurrY = mStartY;

		if (mLimit) {
			mLimitLeft = (int) (mScreenWidth * mLimitLeftPer);
			mLimitRight = (int) (mScreenWidth * mLimitRightPer - mEleWatchWidth);
			mLimitTop = (int) (mScreenHeight * mLimitTopPer);
			mLimitBottom = (int) (mScreenHeight * mLimitBottomPer - mEleWatchHeight);
		} else {
			mLimitLeft = 0;
			mLimitRight = mScreenWidth - mEleWatchWidth;
			mLimitTop = 0;
			mLimitBottom = mScreenHeight - mEleWatchHeight;
		}
	}

	public void moving() {
		// TODO Auto-generated method stub

	}

	public boolean isTaped(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

	public void cleanUp() {
		// TODO Auto-generated method stub

	}

	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}
