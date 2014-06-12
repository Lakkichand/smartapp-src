package com.jiubang.ggheart.apps.desks.diy.themescan;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.view.WindowManager;

import com.gau.go.launcherex.R;

/**
 * 管理对控件高度，宽度，间距等的计算方法
 * 
 * @author yangbing
 * */
public class SpaceCalculator {

	/**
	 * 是否为竖屏
	 * */
	public static boolean sPortrait = true;
	/**
	 * 竖屏下，每行显示的主题个数
	 * */
	public static int sItemCountV = 0;
	/**
	 * 横屏下，每行显示的主题个数
	 * */
	public static int sItemCountH = 0;

	private Activity mContext;
	private static SpaceCalculator sInstance = null;
	private static int sEdgePadding;
	private static int sEachotherPadding;
	private int mStatusBarHight; // 状态栏高度
	private int mScreenHight; // 屏幕高度
	private int mScreenWidth; // 屏幕宽度
	private int mTopHight;
	private int mTabHight;
	private int mItemHeight = 0; // item项高度
	private int mItemWidth = 0; //item项宽度
	private int mItemImageHeight = 0; //item项图片高度
	private int mGoStoreBarHeight; // 去gostore下载更多bar的高度
	private int mListDividerHeight;
	private int mBannerHeight;

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
//		mListDividerHeight = 0; //去除掉分割线
	}

	public synchronized static SpaceCalculator getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new SpaceCalculator(context);
		}
		return sInstance;
	}

	/**
	 * 计算列表数据是否满一屏
	 * */
	public boolean isOverscreen(int adapterCount, boolean isShowBanner) {
		if (mItemHeight == 0) {
			calculateItemThemeScanViewHeight();
		}
		if (isShowBanner && mBannerHeight == 0) {
			mBannerHeight = mContext.getResources().getDimensionPixelSize(
					R.dimen.theme_list_banner_height);
		} else if (!isShowBanner) {
			mBannerHeight = 0;
		}
		if (sPortrait) {
			int listHeight = mScreenHight - mTabHight - mTopHight - mStatusBarHight;
			if (mItemHeight * adapterCount + mListDividerHeight * (adapterCount - 1)
					+ mBannerHeight < listHeight) {
				return false;
			}
			return true;
		} else {
			int listHeight = mScreenHight - mTabHight - mTopHight - mStatusBarHight;
			if (mItemHeight * adapterCount + mListDividerHeight * (adapterCount - 1)
					+ mBannerHeight < listHeight) {
				return false;
			}
			return true;
		}

	}

	/**
	 * 
	 * 获取文字高度
	 * */
	public int getFontHeight() {
		Paint paint = new Paint();
		paint.setTextSize(12);
		FontMetrics fm = paint.getFontMetrics();
		return (int) Math.ceil(fm.descent - fm.ascent) + 2;

	}

	/**
	 * <br>功能简述:计算item高度
	 * <br>功能详细描述:计算一个预览item的高度
	 * <br>注意:
	 * @param 
	 * @return
	 */
	public int calculateItemThemeScanViewHeight() {
		if (mItemHeight > 0) {
			return mItemHeight;
		}
		mItemHeight = mContext.getResources().getDimensionPixelSize(R.dimen.theme_list_item_height);
		return mItemHeight;
	}

	/**
	 * 获取去gostore下载更多bar的高度
	 * */
	public int getGoStoreBarHeight() {
		if (mGoStoreBarHeight > 0) {
			return mGoStoreBarHeight;
		}
		mGoStoreBarHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.theme_gostore_bar_height);
		return mGoStoreBarHeight;
	}

	/**
	 * 根据屏幕宽度计算每行显示的主题个数
	 * */
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

		// 图片宽度
		int picWidth = mContext.getResources().getDimensionPixelSize(R.dimen.mytheme_pic_width);
		// 图片离屏幕2边的距离
		sEdgePadding = mContext.getResources().getDimensionPixelSize(
				R.dimen.theme_list_item_padding_edge);
		// 图片之间的距离
		sEachotherPadding = mContext.getResources().getDimensionPixelSize(
				R.dimen.theme_list_item_padding_eachother);

		float countV = (mScreenWidth - 2 * sEdgePadding + sEachotherPadding)
				/ ((picWidth + sEachotherPadding) * 1.0f);
		setItemCountV(Math.round(countV));

		float countH = (mScreenHight - 2 * sEdgePadding + sEachotherPadding)
				/ ((picWidth + sEachotherPadding) * 1.0f);
		setItemCountH(Math.round(countH));

		mItemHeight = mContext.getResources().getDimensionPixelSize(R.dimen.theme_list_item_height);
	}

	/**
	 * 获取每一行显示的主题数量
	 * */
	public static int getThemeListItemCount() {
//		return sPortrait ? sItemCountV : sItemCountH;
		return sPortrait ? 3 : sItemCountH;
	}

	private static void setItemCountV(int value) {
		sItemCountV = value;
	}

	private static void setItemCountH(int value) {
		sItemCountH = value;
	}

	public static void setIsPortrait(boolean isPortrait) {
		sPortrait = isPortrait;
	}

	/**
	 * 计算主题缩略图当不满一屏时，是否会挡住  “ 去go精品下载 布局”的一部分
	 */
	public boolean calculateIsCover(int adapterCount, boolean isShowBanner) {
		if (sPortrait) {
			if (isShowBanner && mBannerHeight == 0) {
				mBannerHeight = mContext.getResources().getDimensionPixelSize(
						R.dimen.theme_list_banner_height);
			} else if (!isShowBanner) {
				mBannerHeight = 0;
			}
			int listHeight = mScreenHight - mTabHight - mTopHight - mStatusBarHight;
//			int contentHeight = mItemHeight * adapterCount + mListDividerHeight
//					* (adapterCount - 1) + mBannerHeight;
			//TODO
			int contentHeight = mItemHeight * adapterCount + mListDividerHeight
			* adapterCount + mBannerHeight;
			if (listHeight - contentHeight < mGoStoreBarHeight) {
				return true;
			}
		} else {
			int listHeight = mScreenWidth - mTabHight - mTopHight - mStatusBarHight;
//			int contentHeight = mItemHeight * adapterCount + mListDividerHeight
//					* (adapterCount - 1) + mBannerHeight;
			//TODO
			int contentHeight = mItemHeight * adapterCount + mListDividerHeight
					* adapterCount + mBannerHeight;
			if (listHeight - contentHeight < mGoStoreBarHeight) {
				return true;
			}
		}

		return false;
	}

	public void calculateItemViewInfo() {
		int showCountInRow = 3;	//每行排列的主题数目
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		mScreenWidth = wm.getDefaultDisplay().getWidth();
		mScreenHight = wm.getDefaultDisplay().getHeight();
		// 图片离屏幕2边的距离
		sEdgePadding = mContext.getResources().getDimensionPixelSize(
				R.dimen.theme_list_item_padding_edge_portrait);
		// 图片之间的距离
		sEachotherPadding = mContext.getResources().getDimensionPixelSize(
				R.dimen.theme_list_item_padding_eachother_portrait);
		// 图片距离每行分隔线高度
		int addHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.theme_list_item_height_add_space);
		mItemWidth = (mScreenWidth - 2 * sEdgePadding - (showCountInRow - 1) * sEachotherPadding) / showCountInRow;
		int width = showCountInRow * mItemWidth + 2 * sEdgePadding + (showCountInRow - 1) * sEachotherPadding;
		if (mScreenWidth - width > 1) {
			int extraPadding = (mScreenWidth - width) / 2;	//计算出往两边加的额外的边距
			sEdgePadding = sEdgePadding + extraPadding;
		}
		mItemImageHeight = 300 * mItemWidth / 180; //按比例计算图片高度
		mItemHeight = mItemImageHeight + addHeight;
	}
	
	public static int getEdgePadding() {
		return sEdgePadding;
	}
	
	public static int getEachotherPadding() {
		return sEachotherPadding;
	}
	
	public int getImageWidth() {
		return mItemWidth;
	}
	
	public int getImageHeight() {
		return mItemImageHeight;
	}
}
