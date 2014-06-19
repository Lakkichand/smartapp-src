package com.jiubang.go.backup.pro.ui.ScrollerView;

import com.jiubang.go.backup.ex.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.WindowManager;

/**
 * 管理对控件高度，宽度，间距等的计算方法
 * 
 * @author yangbing
 */
public class SpaceCalculator implements ICleanable {

	/**
	 * 是否为竖屏
	 */
	public static boolean sPortrait = true;
	/**
	 * 竖屏下，每行显示的主题个数
	 */
	public static int sItemCountV = 0;
	/**
	 * 横屏下，每行显示的主题个数
	 */
	public static int sItemCountH = 0;

	private Activity mContext;
	private static SpaceCalculator sInstance = null;
	// 状态栏高度
	private int mStatusBarHight;
	// 屏幕高度
	private int mScreenHight;
	// 屏幕宽度
	private int mScreenWidth;
	private int mTopHight;
	private int mTabHight;
	// item项高度
	private int mItemHeight = 0;
	// 去gostore下载更多bar的高度
	private int mGoStoreBarHeight;
	private int mListDividerHeight;

	private SpaceCalculator(Context context) {
		this.mContext = (Activity) context;
		mContext = (Activity) context;
		Rect frame = new Rect();
		mContext.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		mStatusBarHight = frame.top;
		mTopHight = mContext.getResources().getDimensionPixelSize(R.dimen.theme_top_height);
		mTabHight = mContext.getResources().getDimensionPixelSize(R.dimen.theme_tab_height);
		mListDividerHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.theme_list_item_divider_height);

		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		mScreenWidth = wm.getDefaultDisplay().getWidth();
		mScreenHight = wm.getDefaultDisplay().getHeight();
		int temp = 0;
		if (mScreenWidth > mScreenHight) {
			temp = mScreenWidth;
			mScreenWidth = mScreenHight;
			mScreenHight = temp;
		}
	}

	public synchronized static SpaceCalculator getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new SpaceCalculator(context);
		}
		return sInstance;
	}

	public void refreshData(Context context) {
		this.mContext = (Activity) context;
		mContext = (Activity) context;
		Rect frame = new Rect();
		mContext.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		mStatusBarHight = frame.top;
		mTopHight = mContext.getResources().getDimensionPixelSize(R.dimen.theme_top_height);
		mTabHight = mContext.getResources().getDimensionPixelSize(R.dimen.theme_tab_height);
		mListDividerHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.theme_list_item_divider_height);

		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		mScreenWidth = wm.getDefaultDisplay().getWidth();
		mScreenHight = wm.getDefaultDisplay().getHeight();
		int temp = 0;
		if (mScreenWidth > mScreenHight) {
			temp = mScreenWidth;
			mScreenWidth = mScreenHight;
			mScreenHight = temp;
		}
	}

	/**
	 * 计算列表数据是否满一屏
	 */
	public boolean isOverscreen(int adapterCount) {
		if (true) {
			// 91市场以及三星定制包，去除引导下载GO桌面
			return false;
		}
		if (mItemHeight == 0) {
			calculateItemThemeScanViewHeight();
		}
		// if()
		if (sPortrait) {
			int listHeight = mScreenHight - mTabHight - mTopHight - mStatusBarHight;
			if (mItemHeight * adapterCount + mListDividerHeight * (adapterCount - 1) < listHeight) {
				return false;
			}
			return true;
		} else {
			int listHeight = mScreenWidth - mTabHight - mTopHight - mStatusBarHight;
			if (mItemHeight * adapterCount + mListDividerHeight * (adapterCount - 1) < listHeight) {
				return false;
			}
			return true;
		}

	}

	/**
	 * 获取文字高度
	 */
	// public int getFontHeight() {
	// Paint paint = new Paint();
	// paint.setTextSize(12);
	// FontMetrics fm = paint.getFontMetrics();
	// return (int) Math.ceil(fm.descent - fm.ascent) + 2;
	//
	// }

	/**
	 * 计算ThemeListview的item项ItemThemeScanView控件的高度
	 */
	public int calculateItemThemeScanViewHeight() {
		if (mItemHeight > 0) {
			return mItemHeight;
		}
		// //缩略图的高度
		// int
		// picHeight=mContext.getResources().getDimensionPixelSize(R.dimen.mytheme_pic_height);
		// System.out.println("缩略图的高度"+picHeight);
		// //缩略图和主题名之间的间距
		// int
		// padding=mContext.getResources().getDimensionPixelSize(R.dimen.theme_list_item_text_padding);
		// System.out.println("缩略图和主题名之间的间距"+padding);
		// //主题名文字的高度
		// int textHeight=(int)(getFontHeight()*DrawUtils.sDensity);
		// System.out.println("主题名文字的高度"+textHeight);
		// mItemHeight=picHeight+padding+textHeight;
		mItemHeight = mContext.getResources().getDimensionPixelSize(R.dimen.theme_list_item_height);
		return mItemHeight;

	}

	/**
	 * 获取去gostore下载更多bar的高度
	 */
	public int getGoStoreBarHeight() {
		if (mGoStoreBarHeight > 0) {
			return mGoStoreBarHeight;
		}
		mGoStoreBarHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.theme_gostore_bar_height);
		return mGoStoreBarHeight;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		mContext = null;
		sInstance = null;
	}

	/**
	 * 根据屏幕宽度计算每行显示的主题个数
	 */
	public void calculateThemeListItemCount() {
		// 取屏幕高宽
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		mScreenWidth = wm.getDefaultDisplay().getWidth();
		mScreenHight = wm.getDefaultDisplay().getHeight();
		int temp = 0;
		if (mScreenWidth > mScreenHight) {
			temp = mScreenWidth;
			mScreenWidth = mScreenHight;
			mScreenHight = temp;
		}
		//
		// // 图片宽度
		// int picWidth = mContext.getResources().getDimensionPixelSize(
		// R.dimen.mytheme_pic_width);
		// // 图片离屏幕2边的距离
		// int edgePadding = mContext.getResources().getDimensionPixelSize(
		// R.dimen.theme_list_item_padding_edge);
		// // 图片之间的距离
		// int eachotherPadding = mContext.getResources().getDimensionPixelSize(
		// R.dimen.theme_list_item_padding_eachother);
		//
		// float countV = (mScreenWidth - 2 * edgePadding + eachotherPadding)
		// / ((picWidth + eachotherPadding) * 1.0f);
		// sItemCountV = Math.round(countV);
		//
		// float countH = (mScreenHight - 2 * edgePadding + eachotherPadding)
		// / ((picWidth + eachotherPadding) * 1.0f);
		// sItemCountH = Math.round(countH);
		//
	}

	/**
	 * 获取每一行显示的主题数量
	 */
	// public static int getThemeListItemCount() {
	// return sPortrait ? sItemCountV : sItemCountH;
	//
	// }

}
