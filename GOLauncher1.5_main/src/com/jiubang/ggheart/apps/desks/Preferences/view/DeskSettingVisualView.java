package com.jiubang.ggheart.apps.desks.Preferences.view;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.go.util.SortUtils;
import com.go.util.graphics.DrawUtils;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.appgame.appcenter.component.PagerActionBar;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingVisualActivity;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.gowidget.gostore.views.ScrollerViewGroup;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ScreenStyleConfigInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>
 * 类描述:个性化设置根view <br>
 * 功能详细描述:
 * 
 * @author ruxueqin
 * @date [2012-9-25]
 */
public class DeskSettingVisualView extends LinearLayout implements
		ScreenScrollerListener {
	private LayoutInflater mInflater;
	private TitlePagerActionBar mTitlePager; // tab标题栏
	private ScrollerViewGroup mScrollerViewGroup; // tab横向滚动器

	// 各tab view
	private DeskSettingVisualFontTabView mFontTabView; // 字体
	private DeskSettingVisualBackgroundTabView mBackgroundTabView; // 背景
	private DeskSettingVisualIconTabView mIconTabView; // 图标
	private DeskSettingVisualIndicatorTabView mIndicatorTabView; // 指示器

	// 设置信息infos
	private DesktopSettingInfo mDesktopInfo; // 桌面设置信息
	private FunAppSetting mFunAppSetting; // 功能表设置信息
	private ShortCutSettingInfo mShortcutInfo; // 快捷条设置信息
	private ScreenSettingInfo mScreenInfo; // 屏幕设置信息
	private ScreenStyleConfigInfo mScreenStyleInfo; // 屏幕风格设置

	public DeskSettingVisualView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setOrientation(LinearLayout.VERTICAL);
		DrawUtils.resetDensity(getContext());
		mInflater = LayoutInflater.from(getContext());
		// 启动异步扫描主题包
		GetAllThemesTask task = new GetAllThemesTask();
		task.execute();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		initView();
		initInfos();
	}

	/**
	 * <br>
	 * 功能简述:统一获取个性化设置里需要用到的数据，然后设置到各个tab <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void initInfos() {
		// 获取
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		mDesktopInfo = controler.createDesktopSettingInfo();
		mShortcutInfo = controler.getShortCutSettingInfo();
		mFunAppSetting = controler.getFunAppSetting();
		mScreenInfo = controler.getScreenSettingInfo();
		mScreenStyleInfo = controler.getScreenStyleSettingInfo();

		// 设置
		mFontTabView.setInfo(mDesktopInfo);
		mBackgroundTabView.setInfos(mShortcutInfo, mFunAppSetting, mScreenInfo);
		mIconTabView.setInfos(mDesktopInfo, mShortcutInfo, mScreenStyleInfo);
		mIndicatorTabView.setInfos(mScreenInfo, mFunAppSetting,
				mScreenStyleInfo);
	}

	/**
	 * <br>
	 * 功能简述:初始化滚动器及各tab <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void initView() {
		initTab();
		initScrollerViewGroup();
		mTitlePager.attachToViewPager(mScrollerViewGroup);
	}

	/**
	 * <br>
	 * 功能简述:初始化tab栏要显示的tab名称 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void initTab() {
		mTitlePager = (TitlePagerActionBar) mInflater.inflate(
				R.layout.recomm_apps_management_tab, null);
		List<String> titleList = new ArrayList<String>();
		titleList.add(getContext().getString(R.string.font_setting_title));
		titleList.add(getContext().getString(R.string.bg_setting));
		titleList.add(getContext().getString(R.string.icon_style));
		titleList.add(getContext().getString(R.string.indicators_setting));
		mTitlePager.setmTitleList(titleList);
		mTitlePager.setmIsNeedLandscape(true);
		addView(mTitlePager);
	}

	/**
	 * <br>
	 * 功能简述:初始化各tab <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void initScrollerViewGroup() {
		View fontView = mInflater.inflate(
				R.layout.desk_setting_visual_tab_font, null);
		mFontTabView = (DeskSettingVisualFontTabView) fontView
				.findViewById(R.id.font);

		View backgroundView = mInflater.inflate(
				R.layout.desk_setting_visual_tab_background, null);
		mBackgroundTabView = (DeskSettingVisualBackgroundTabView) backgroundView
				.findViewById(R.id.background);

		View iconView = mInflater.inflate(
				R.layout.desk_setting_visual_tab_icon, null);
		mIconTabView = (DeskSettingVisualIconTabView) iconView
				.findViewById(R.id.icon);

		View indicatorView = mInflater.inflate(
				R.layout.desk_setting_visual_tab_indicator, null);
		mIndicatorTabView = (DeskSettingVisualIndicatorTabView) indicatorView
				.findViewById(R.id.indicator);

		mScrollerViewGroup = new ScrollerViewGroup(getContext(), this);
		mScrollerViewGroup.addView(fontView);
		mScrollerViewGroup.addView(backgroundView);
		mScrollerViewGroup.addView(iconView);
		mScrollerViewGroup.addView(indicatorView);

		mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
		mScrollerViewGroup.gotoViewByIndex(1);
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
		if (mScrollerViewGroup.getCurrentViewIndex() != mTitlePager
				.getmCurrentPage()) {
			mTitlePager.setmCurrentPage(mScrollerViewGroup
					.getCurrentViewIndex());
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
			mTitlePager
					.onPageScrolled(-1, positionOffset, positionOffsetPixels);
			return;
		} else {
			mTitlePager.onPageScrolled(position, positionOffset,
					positionOffsetPixels);
		}
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {

	}

	@Override
	public void onScrollFinish(int currentScreen) {
		mScrollerViewGroup.getChildAt(currentScreen)
				.setVisibility(View.VISIBLE);
		mTitlePager.onPageScrollStateChanged(PagerActionBar.SCROLL_STATE_IDLE);
		mTitlePager.onPageSelected(currentScreen);
	}

	/**
	 * <br>
	 * 功能简述:退出时回收资源 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void recycle() {
		if (mScrollerViewGroup != null) {
			mScrollerViewGroup.destory();
		}
	}

	/**
	 * <br>
	 * 功能简述:加载各tab内view数据 <br>
	 * 功能详细描述: <br>
	 * 注意:在所有view加载完后，才调用
	 */
	public void load() {
		mFontTabView.load();
		mBackgroundTabView.load();
		// mIconTabView.load();
		mIndicatorTabView.load();
	}

	public void changeOrientation() {
		DrawUtils.resetDensity(getContext());

		mFontTabView.changeOrientation();
		mBackgroundTabView.changeOrientation();
		mIconTabView.changeOrientation();
		mIndicatorTabView.changeOrientation();
	}

	/**
	 * <br>
	 * 功能简述:保存数据 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void save() {
		mFontTabView.save();
		mIconTabView.save();
		mBackgroundTabView.save();
		mIndicatorTabView.save();
	}

	/**
	 * <br>
	 * 功能简述:更新dock背景图片 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void showAndUpdateDockPic() {
		// 显示
		mBackgroundTabView.showDockPic(true);
		// 更新
		mBackgroundTabView.updateDockWallpaper();
	}

	/**
	 * <br>
	 * 功能简述:更新dock背景图片 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void showAndUpdateDockPicTransparent() {
		// 显示
		mBackgroundTabView.showDockPic(false);
		// 更新
		mBackgroundTabView.updateDockWallpaper();
	}

	/**
	 * <br>
	 * 功能简述:更新功能表背景图片 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void showAndUpdateAppdrawerPic() {
		// 显示
		mBackgroundTabView.showAppdrawerPic(true);
		// 更新
		mBackgroundTabView.updateAppdrawerWallpaper();
	}

	/**
	 * <br>
	 * 功能简述:更新功能表背景图片 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void showAndUpdateAppdrawerPicTransparent() {
		// 显示
		mBackgroundTabView.showAppdrawerPic(false);
		// 更新
		mBackgroundTabView.updateAppdrawerWallpaper();
	}

	public void clipAppdrawerPic(Uri imageUri) {
		mBackgroundTabView.clipAppdrawerPic(imageUri);
	}

	public void saveAppdrawerPic(int setting) {
		mBackgroundTabView.saveAppdrawerPic(setting);
	}

	public void saveAppdrawerPic(int setting, String path) {
		mBackgroundTabView.saveAppdrawerPic(setting, path);
	}

	public void restoreDockBgType() {
		mBackgroundTabView.restoreDockBgType();
	}

	public void restoreAppdrawerBgType() {
		mBackgroundTabView.restoreAppdrawerBgType();
	}

	public void onResume() {
		mBackgroundTabView.onResume();
		mIconTabView.onResume();
	}

	/**
	 * 排序比较器，中英文按a~z进行混排
	 */
	private Comparator<ThemeInfoBean> mComparator = new Comparator<ThemeInfoBean>() {
		@Override
		public int compare(ThemeInfoBean object1, ThemeInfoBean object2) {
			int result = 0;
			String str1 = object1.getThemeName();
			String str2 = object2.getThemeName();
			str1 = SortUtils.changeChineseToSpell(getContext(), str1);
			str2 = SortUtils.changeChineseToSpell(getContext(), str2);
			Collator collator = null;
			if (Build.VERSION.SDK_INT < 16) {
				collator = Collator.getInstance(Locale.CHINESE);
			} else {
				collator = Collator.getInstance(Locale.ENGLISH);
			}

			if (collator == null) {
				collator = Collator.getInstance(Locale.getDefault());
			}
			result = collator.compare(str1.toUpperCase(), str2.toUpperCase());
			return result;
		}
	};

	/**
	 * 
	 * <br>
	 * 类描述:扫描获取所有主题包 <br>
	 * 功能详细描述:
	 * 
	 * @author ruxueqin
	 * @date [2012-9-24]
	 */
	class GetAllThemesTask extends AsyncTask<Void, Void, String> {
		String[] mAllThemePackage;
		String[] mAllThemeName;

		@Override
		protected void onPreExecute() {
			// 显示扫描等待提示框
			((Activity) getContext())
					.showDialog(DeskSettingVisualActivity.DIALOG_ID_INIT_LIST);
		}

		@Override
		protected String doInBackground(Void... params) {
			// 扫描全部主题
			ArrayList<ThemeInfoBean> themeInfos = GOLauncherApp
					.getThemeManager().getAllThemeInfosWithoutDefaultTheme();
			//A-Z排序
			Collections.sort(themeInfos, mComparator);
			
			int themeSize = 0;
			if (themeInfos != null) {
				themeSize = themeInfos.size();
			}

			// entries初始化
			mAllThemePackage = new String[themeSize];
			mAllThemeName = new String[themeSize];

			for (int i = 0; i < themeSize; i++) {
				mAllThemePackage[i] = themeInfos.get(i).getPackageName();
				mAllThemeName[i] = themeInfos.get(i).getThemeName();
			}

			if (null != themeInfos) {
				themeInfos.clear();
				themeInfos = null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// 1:取消扫描提示框
			((Activity) getContext())
					.removeDialog(DeskSettingVisualActivity.DIALOG_ID_INIT_LIST);

			// 2:设置各tab主题内容
			mBackgroundTabView.setThemesData(mAllThemePackage, mAllThemeName);
			mIconTabView.setThemesData(mAllThemePackage, mAllThemeName);
			mIndicatorTabView.setThemesData(mAllThemePackage, mAllThemeName);
		}
	}
}
