package com.jiubang.ggheart.apps.desks.appfunc.model;

import android.app.Activity;
import android.util.DisplayMetrics;

import com.go.util.graphics.DrawUtils;

/**
 * 界面参数结构布局类
 * 
 * @author tanshu
 * 
 */
public class AppFuncLayoutParams {
	/**
	 * 水平间距
	 */
	private int mHorizontalSpacing;
	/**
	 * 垂直间距
	 */
	private int mVerticalSpacing;
	/**
	 * 列宽
	 */
	private int mColumnWidth;
	/**
	 * 文本框高度
	 */
	private int mTextHeight;
	/**
	 * 图标大小
	 */
	private int mIconSize;
	/**
	 * 显示信息
	 */
	private DisplayMetrics mMetrics;
	/**
	 * 上下文
	 * 
	 * @param metrics
	 */
	private Activity mActivity;
	/**
	 * 行数
	 */
	private int mNumRows;
	/**
	 * 列数
	 */
	private int mNumColumns;
	/**
	 * 单列对象
	 */
	private static AppFuncLayoutParams mInstance;

	public static AppFuncLayoutParams getInstance(Activity activity) {
		if (mInstance == null) {
			mInstance = new AppFuncLayoutParams(activity);
		}
		return mInstance;
	}

	private AppFuncLayoutParams(Activity activity) {
		mActivity = activity;
		mMetrics = activity.getResources().getDisplayMetrics();
		mHorizontalSpacing = 0;
		mVerticalSpacing = 0;
		mColumnWidth = 0;
		mTextHeight = 0;
		mIconSize = 0;
	}

	public int getHorizontalSpacing() {
		return (int) (mHorizontalSpacing * mMetrics.density);
	}

	public int getVerticalSpacing() {
		return (int) (mVerticalSpacing * mMetrics.density);
	}

	public int getColumnWidth() {
		return (int) (mColumnWidth * mMetrics.density);
	}

	public int getTextHeight() {
		return (int) (mTextHeight * mMetrics.density);
	}

	public int getIconSize() {
		return (int) (mIconSize * mMetrics.density);
	}

	public int getNumColumns() {
		return mNumColumns;
	}

	public int getNumRows() {
		return mNumRows;
	}

	/**
	 * 根据行列数以及显示区域高度设置布局参数
	 * 
	 * @param numColumns
	 * @param numRows
	 */
	public void setLayoutParams(int numRows, int numColumns) {
		mNumRows = numRows;
		mNumColumns = numColumns;
		int screenWidthDip = DrawUtils.px2dip(mMetrics.widthPixels);
		int screenHeightDip = DrawUtils.px2dip(mMetrics.heightPixels);

		// 根据屏幕Dpi设置图标大小
		if (screenWidthDip <= 320) {
			int rule = (numColumns >= numRows) ? numColumns : numRows;
			if (screenHeightDip <= 480) {
				if (rule == 4) {
					mIconSize = 50;
				} else if (rule == 5) {
					mIconSize = 46;
				} else {
					mIconSize = 38;
				}
			} else {
				if (rule == 4) {
					mIconSize = 50;
				} else if (rule == 5) {
					mIconSize = 50;
				} else {
					mIconSize = 42;
				}
			}
		} else {
			mIconSize = 50;
		}

		if (numColumns == 4) {
			if (screenWidthDip <= 320) {
				mHorizontalSpacing = 5;
			} else {
				mHorizontalSpacing = 40;
			}
		} else if (numColumns == 5) {
			if (screenWidthDip <= 320) {
				mHorizontalSpacing = 2;
			} else {
				mHorizontalSpacing = 20;
			}
		} else {
			// numColumns == 6
			if (screenWidthDip <= 320) {
				mHorizontalSpacing = 2;
			} else {
				mHorizontalSpacing = 10;
			}
		}
		mColumnWidth = (screenWidthDip - (numColumns - 1) * mHorizontalSpacing) / numColumns;

		// TextView框高为18dp
		mTextHeight = 18;

		// 计算可显示区域的高度in dp
		int showHeight = showAreaHeight();
		mVerticalSpacing = (showHeight - (mIconSize + mTextHeight) * numRows) / numRows;

	}

	/**
	 * 计算可显示区域的高度
	 */
	private int showAreaHeight() {
		// Get the height of the Tab
		// NinePatchDrawable tabPic =
		// (NinePatchDrawable)mActivity.getResources().getDrawable
		// (R.drawable.appfunc_tab_focused);
		// int tabPicHeight= tabPic.getIntrinsicHeight();
		//
		// //Get the height of the status bar
		// Rect frame = new Rect();
		// mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		// int statusBarHeight = frame.top;
		//
		// //Calculate the height of the show area
		// //Top margin is 10dp and Bottom margin is 45dp
		// int showHeight = mMetrics.heightPixels - statusBarHeight -
		// tabPicHeight - (int)(50 * mMetrics.density) ;
		//
		// return (int)(showHeight/mMetrics.density);
		return 0;
	}
}
