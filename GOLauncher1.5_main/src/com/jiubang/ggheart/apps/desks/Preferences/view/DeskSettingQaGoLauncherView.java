package com.jiubang.ggheart.apps.desks.Preferences.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.appgame.appcenter.component.PagerActionBar;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingQaTutorialActivity;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.gowidget.gostore.views.ScrollerViewGroup;

/**
 * 
 * <br>类描述:桌面设置-关于-QA-Go桌面
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-10-15]
 */
public class DeskSettingQaGoLauncherView extends LinearLayout implements ScreenScrollerListener {
	private TitlePagerActionBar mTitlePager; //tab标题栏
	private ScrollerViewGroup mScrollerViewGroup; //tab横向滚动器

	private String[] mPageList;
	private final int[] mPageNames = { R.array.help_pages_names_cn_ch,
			R.array.help_pages_names_en_us, R.array.help_pages_names_cn_hk,
			R.array.help_pages_names_cn_hk };

	public DeskSettingQaGoLauncherView(Context context) {
		super(context);
		setOrientation(LinearLayout.VERTICAL); //设置布局方向
		DrawUtils.resetDensity(getContext());
		mPageList = DeskSettingQaTutorialActivity.getPageList(mPageNames, 1, context);
		if (mPageList == null || mPageList.length <= 0) {
			return;
		}
		initView();
	}

	/**
	 * <br>功能简述:初始化滚动器及各tab
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initView() {
		initTab();
		initScrollerViewGroup();
		mTitlePager.attachToViewPager(mScrollerViewGroup);
	}

	/**
	 * <br>功能简述:初始化tab栏要显示的tab名称
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initTab() {
		mTitlePager = (TitlePagerActionBar) LayoutInflater.from(getContext()).inflate(
				R.layout.recomm_apps_management_tab, null);
		List<String> titleList = new ArrayList<String>();
		//加空格是未了增大点击范围。不然点击起来有困难
		titleList.add("   " + getContext().getString(R.string.desk_setting_performance) + "   ");
		titleList.add("   " + getContext().getString(R.string.desk_setting_optesthesia) + "   ");
		titleList.add("   " + getContext().getString(R.string.desk_setting_operate) + "   ");
		mTitlePager.setmTitleList(titleList);
		mTitlePager.setmIsNeedLandscape(true);
		addView(mTitlePager);
	}

	/**
	 * <br>功能简述:初始化各tab
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initScrollerViewGroup() {
		mScrollerViewGroup = new ScrollerViewGroup(getContext(), this);
		for (int i = 0; i < 3; i++) {
			if (i < mPageList.length) {
				String url = DeskSettingQaTutorialActivity.URL_BASE + mPageList[i];
				DeskSettingQaWebView qaWebView = new DeskSettingQaWebView(getContext());
				qaWebView.loadUrl(url);
				mScrollerViewGroup.addView(qaWebView);
			}
		}
		mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
		mScrollerViewGroup.gotoViewByIndex(0);
		addView(mScrollerViewGroup);
	}

	@Override
	public ScreenScroller getScreenScroller() {
		return null;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {

	}

	@Override
	public void onFlingIntercepted() {

	}

	@Override
	public void onScrollStart() {

	}

	@Override
	public void onFlingStart() {

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		if (mScrollerViewGroup.getCurrentViewIndex() != mTitlePager.getmCurrentPage()) {
			mTitlePager.setmCurrentPage(mScrollerViewGroup.getCurrentViewIndex());
		}
		int scrollX = newScroll - mScrollerViewGroup.getCurrentViewIndex()
				* GoLauncher.getScreenWidth();
		final int width = GoLauncher.getScreenWidth();
		final int widthWithMargin = width;
		final int position = newScroll / widthWithMargin;
		final int positionOffsetPixels = scrollX % widthWithMargin;
		final float positionOffset = Math.abs(scrollX * 1.0f / widthWithMargin);
		if (newScroll < 0 && mScrollerViewGroup.getCurrentViewIndex() <= 0) {
			// 说明是第一屏向右滑动
			mTitlePager.onPageScrolled(-1, positionOffset, positionOffsetPixels);
			return;
		} else {
			mTitlePager.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {

	}

	@Override
	public void onScrollFinish(int currentScreen) {
		mScrollerViewGroup.getChildAt(currentScreen).setVisibility(View.VISIBLE);
		mTitlePager.onPageScrollStateChanged(PagerActionBar.SCROLL_STATE_IDLE);
		mTitlePager.onPageSelected(currentScreen);
	}

	/**
	 * <br>功能简述:退出时回收资源
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void recycle() {
		//要循环销毁每个Webview，有可能webview没有加载完会一直加载。导致Activity注销不了
		if (mScrollerViewGroup != null) {
			for (int i = 0; i < mScrollerViewGroup.getChildCount(); i++) {
				DeskSettingQaWebView webView = (DeskSettingQaWebView) mScrollerViewGroup
						.getChildAt(i);
				webView.onDestroy();
				webView = null;
			}
			mScrollerViewGroup.destory();
		}
	}
}
