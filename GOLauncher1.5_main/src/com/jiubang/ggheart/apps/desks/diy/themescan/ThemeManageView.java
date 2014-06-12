package com.jiubang.ggheart.apps.desks.diy.themescan;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.gau.utils.cache.encrypt.CryptTool;
import com.go.util.AppUtils;
import com.go.util.ConvertUtils;
import com.go.util.device.Machine;
import com.go.util.file.FileUtil;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingVisualActivity;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.views.ScrollerViewGroup;
import com.jiubang.ggheart.billing.PurchaseStateManager;
import com.jiubang.ggheart.billing.ThemeAppInBillingManager;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.components.facebook.GoFacebookUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.LockerManager;
import com.jiubang.ggheart.data.theme.OnlineThemeGetter;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 主题管理主界面
 * 
 * @author yangbing
 * 
 * */
public class ThemeManageView extends RelativeLayout
		implements
			ScreenScrollerListener,
			View.OnClickListener {
	/**
	 * 精选界面
	 * */
	public static final int FEATURED_THEME_VIEW_ID = 0;
	/**
	 * 热门主题界面
	 */
	public static final int HOT_THEME_VIEW_ID = 1;
	/**
	 * 本地界面
	 * */
	public static final int INSTALLED_THEME_VIEW_ID = 2;
	/**
	 * 桌面主题界面
	 * */
	public static final int LAUNCHER_THEME_VIEW_ID = 4;
	/**
	 * 锁屏主题界面
	 * */
	public static final int LOCKER_THEME_VIEW_ID = 3;

	public static final String NEWSETTINGOPTION = "newSettingOption";

	// 子视图id
	private static final int TOP_LAYOUT_ID = 0x1;
	private static final int TAB_LAYOUT_ID = 0x2;
	private static final int BANNER_LAYOUT_ID = 0x3;

	private LayoutInflater mInflater;
	// 可左右滑动的ViewGroup，在精选主题容器和本地主题容器之间滑动
	private ScrollerViewGroup mScrollerViewGroup = null;
	private RelativeLayout mTabLayout = null;
	private RelativeLayout mBannerScrollerViewGroup = null; //有可能Banner中会出现滚动的可能
	private View mNoGolockerTipsView = null;
	private TextView mDeskThemeTitle = null; // 桌面主题
	private TextView mLockerThemeTitle = null; // 锁屏主题
	private TextView mFeaturedThemeTab = null;
	private TextView mHotThemeTab = null;
	private TextView mInstalledThemeTab = null;
	private ImageView mFeaturedThemeImg = null;
	private ImageView mHotThemeImg = null;
	private ImageView mInstalledThemeImg = null;
	private ImageView mSettingImg = null;
	private RelativeLayout mDeskLayout = null;
	private RelativeLayout mLockerLayout = null;
	private RelativeLayout mFeaturedLayout = null;
	private RelativeLayout mHotLayout = null;
	private RelativeLayout mInstalledLayout = null;
	private RelativeLayout mSettingLayout = null;
	private int mEntranceId = -1; // 入口id,当前是桌面tab还是锁屏tab
	private int mCurTabId = -1; // 当前是精选tab还是本地tab
	private ThemeContainer mFeaturedThemeContainer; // 精选主题容器
	private ThemeContainer mInstalledThemeContainer; // 本地主题容器
	private ThemeContainer mHotThemeContainer; //热门主题容器
	private View mDeskPoint;
	private View mLockerPoint;
	private ImageView mNewThemeLog;
	private boolean mHasNewTheme = false;
	private ImageView mNewLockerSettingLogo = null;
	private TextView mLokerSettingNotifyView;
	public ThemeManageView(Context context, int entranceId, int tab) {
		super(context);
		this.mEntranceId = entranceId;
		mInflater = LayoutInflater.from(context);
		if (tab == OnlineThemeGetter.TAB_LAUNCHER_FEATURED_ID) {
			mCurTabId = FEATURED_THEME_VIEW_ID;
		} else {
			mCurTabId = HOT_THEME_VIEW_ID;
		}

		// 初始化界面
		initView();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ICustomAction.ACTION_PURCHASE_STATE_RESULT);
		context.registerReceiver(mReceiver, filter);
		openVipUpgradePage();
	}

	private void openVipUpgradePage() {
		int l = ThemePurchaseManager.getCustomerLevel(getContext());
		if (l == ThemeConstants.CUSTOMER_LEVEL1) {
			PreferencesManager pm = new PreferencesManager(getContext(),
					IPreferencesIds.THEME_SETTING_CONFIG, Context.MODE_PRIVATE);
			boolean bool = pm.getBoolean(IPreferencesIds.HAS_SHOW_VIPUPGRADE, false);
			if (!bool) {
				onVipClick();
				pm.putBoolean(IPreferencesIds.HAS_SHOW_VIPUPGRADE, true);
				pm.commit();
			}
		}
	}

	/**
	 * 初始化界面
	 */
	public void initView() {
		setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
		setBackgroundResource(R.color.theme_bg);
		initTopView(); // 桌面主题和锁屏主题tab栏
		if (mEntranceId == ThemeManageView.LAUNCHER_THEME_VIEW_ID) {
			initLauncherTabView(); // 桌面主题精选、热门和本地tab栏
			PreferencesManager sp = new PreferencesManager(getContext(),
					IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
			sp.putBoolean(IPreferencesIds.HASNEWTHEME, false);
			sp.commit();
		} else {
			initLockTabView(); // 锁屏主题精选和本地tab栏
			PreferencesManager sp = new PreferencesManager(getContext(),
					IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
			sp.putBoolean(IPreferencesIds.LOCKER_HASNEWTHEME, false);
			sp.commit();
		}

		if (!isInstallGoLocker() && mEntranceId == LOCKER_THEME_VIEW_ID) {
			if (mTabLayout != null) {
				mTabLayout.setVisibility(GONE);
			}
			notInstallGoLockerView();
		} else {
			if (mEntranceId == ThemeManageView.LAUNCHER_THEME_VIEW_ID) {
				initLauncherMainView();
			} else {
				initLockMainView();
				addLockerSettingTip();
			}
			startLoadThemeData();
		}

		// 下载图片网络信息收集
		//		TestAppGameNetLogControll.getInstance().sendNetLog(getContext());

	}

	/**
	 * 开始加载数据
	 * */
	private void startLoadThemeData() {
		if (mEntranceId == LAUNCHER_THEME_VIEW_ID) {
			// 桌面主题
			if (mCurTabId == FEATURED_THEME_VIEW_ID) {
				// 精选
				mFeaturedThemeContainer.loadThemeData(ThemeConstants.LAUNCHER_FEATURED_THEME_ID);

			} else if (mCurTabId == INSTALLED_THEME_VIEW_ID) {
				// 本地
				mInstalledThemeContainer.loadThemeData(ThemeConstants.LAUNCHER_INSTALLED_THEME_ID);
			} else {
				//热门
				mHotThemeContainer.loadThemeData(ThemeConstants.LAUNCHER_HOT_THEME_ID);
			}
		} else if (mEntranceId == LOCKER_THEME_VIEW_ID) {
			// 锁屏主题
			if (mCurTabId == FEATURED_THEME_VIEW_ID) {
				// 精选
				mFeaturedThemeContainer.loadThemeData(ThemeConstants.LOCKER_FEATURED_THEME_ID);
			} else {
				// 本地
				mInstalledThemeContainer.loadThemeData(ThemeConstants.LOCKER_INSTALLED_THEME_ID);
			}
		}
	}

	private void initLauncherTabView() {
		if (mTabLayout != null) {
			this.removeView(mTabLayout);
		}

		if (SpaceCalculator.sPortrait) {
			mTabLayout = (RelativeLayout) mInflater.inflate(R.layout.theme_manage_tab_v, null);
		} else {
			mTabLayout = (RelativeLayout) mInflater.inflate(R.layout.theme_manage_tab_h, null);
		}

		mTabLayout.findViewById(R.id.vipgroup).setOnClickListener(this);
		ImageView vipBtn = (ImageView) mTabLayout.findViewById(R.id.vipbtn);
		int vip = ThemePurchaseManager.getCustomerLevel(getContext());
		if (vip == ThemeConstants.CUSTOMER_LEVEL1) {
			vipBtn.setImageResource(R.drawable.theme_vip_level1);
		} else if (vip == ThemeConstants.CUSTOMER_LEVEL2) {
			vipBtn.setImageResource(R.drawable.theme_vip_level2);
		} else {
			vipBtn.setImageResource(R.drawable.theme_vip_level0);
		}

		mFeaturedThemeTab = (TextView) mTabLayout.findViewById(R.id.featured_theme_text);
		mFeaturedThemeImg = (ImageView) mTabLayout.findViewById(R.id.featured_theme_image);
		mFeaturedLayout = (RelativeLayout) mTabLayout.findViewById(R.id.featured_layout);
		mFeaturedThemeTab.setOnClickListener(this);
		mFeaturedThemeImg.setOnClickListener(this);
		mFeaturedLayout.setOnClickListener(this);

		mHotThemeTab = (TextView) mTabLayout.findViewById(R.id.hot_theme_text);
		mHotThemeImg = (ImageView) mTabLayout.findViewById(R.id.hot_theme_image);
		mHotLayout = (RelativeLayout) mTabLayout.findViewById(R.id.hot_layout);
		mHotThemeTab.setOnClickListener(this);
		mHotThemeImg.setOnClickListener(this);
		mHotLayout.setOnClickListener(this);

		mInstalledThemeTab = (TextView) mTabLayout.findViewById(R.id.installed_theme_text);
		mInstalledThemeImg = (ImageView) mTabLayout.findViewById(R.id.installed_theme_image);
		mInstalledLayout = (RelativeLayout) mTabLayout.findViewById(R.id.installed_layout);
		mInstalledThemeTab.setOnClickListener(this);
		mInstalledThemeImg.setOnClickListener(this);
		mInstalledLayout.setOnClickListener(this);
		mSettingLayout = (RelativeLayout) mTabLayout.findViewById(R.id.theme_setting_layout);
		mSettingImg = (ImageView) mTabLayout.findViewById(R.id.theme_setting_image);
		mSettingLayout.setOnClickListener(this);
		mSettingImg.setOnClickListener(this);
		switch (mCurTabId) {
			case FEATURED_THEME_VIEW_ID :
				focusFeaturedThemeTab();
				break;
			case INSTALLED_THEME_VIEW_ID :
				focusInstalledThemeTab();
				break;
			case HOT_THEME_VIEW_ID :
				focusHotThemeTab();
				break;
			default :
				break;
		}
		// 添加tab视图
		mTabLayout.setId(TAB_LAYOUT_ID);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, TOP_LAYOUT_ID);
		addView(mTabLayout, params);

	}

	/**
	 * 功能简述:初始化锁屏主题的Tab（精选、本地）
	 */
	private void initLockTabView() {
		if (mTabLayout != null) {
			mTabLayout.removeAllViews();
			this.removeView(mTabLayout);
		}
		if (SpaceCalculator.sPortrait) {
			mTabLayout = (RelativeLayout) mInflater.inflate(R.layout.theme_manage_tab2_v, null);
		} else {
			mTabLayout = (RelativeLayout) mInflater.inflate(R.layout.theme_manage_tab2_h, null);
		}
		mTabLayout.findViewById(R.id.vipgroup).setOnClickListener(this);
		ImageView vipBtn = (ImageView) mTabLayout.findViewById(R.id.vipbtn);
		int vip = ThemePurchaseManager.getCustomerLevel(getContext());
		if (vip == ThemeConstants.CUSTOMER_LEVEL1) {
			vipBtn.setImageResource(R.drawable.theme_vip_level1);
		} else if (vip == ThemeConstants.CUSTOMER_LEVEL2) {
			vipBtn.setImageResource(R.drawable.theme_vip_level2);
		} else {
			vipBtn.setImageResource(R.drawable.theme_vip_level0);
		}

		boolean hasUpdate = hasNewLockerSetting();
		if (mNewLockerSettingLogo != null) {
			mTabLayout.removeView(mNewLockerSettingLogo);
		}
		if (hasUpdate) {
			mNewLockerSettingLogo = new ImageView(getContext());
			mNewLockerSettingLogo.setImageResource(R.drawable.locker_setting_new);
			RelativeLayout.LayoutParams pa = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			pa.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			pa.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			pa.setMargins(0, 10, 10, 0);
			mTabLayout.addView(mNewLockerSettingLogo, pa);
		}
		mFeaturedThemeTab = (TextView) mTabLayout.findViewById(R.id.featured_theme_text);
		mFeaturedThemeImg = (ImageView) mTabLayout.findViewById(R.id.featured_theme_image);
		mFeaturedLayout = (RelativeLayout) mTabLayout.findViewById(R.id.featured_layout);
		mFeaturedThemeTab.setOnClickListener(this);
		mFeaturedThemeImg.setOnClickListener(this);
		mFeaturedLayout.setOnClickListener(this);

		mInstalledThemeTab = (TextView) mTabLayout.findViewById(R.id.installed_theme_text);
		mInstalledThemeImg = (ImageView) mTabLayout.findViewById(R.id.installed_theme_image);
		mInstalledLayout = (RelativeLayout) mTabLayout.findViewById(R.id.installed_layout);
		mInstalledThemeTab.setOnClickListener(this);
		mInstalledThemeImg.setOnClickListener(this);
		mInstalledLayout.setOnClickListener(this);
		mSettingLayout = (RelativeLayout) mTabLayout.findViewById(R.id.theme_setting_layout);
		mSettingImg = (ImageView) mTabLayout.findViewById(R.id.theme_setting_image);
		mSettingLayout.setOnClickListener(this);
		mSettingImg.setOnClickListener(this);
		switch (mCurTabId) {
			case FEATURED_THEME_VIEW_ID :
				focusFeaturedThemeTab();
				break;
			case INSTALLED_THEME_VIEW_ID :
				focusInstalledThemeTab();
				break;
			case HOT_THEME_VIEW_ID :
				focusHotThemeTab();
				break;

			default :
				break;
		}
		// 添加tab视图
		mTabLayout.setId(TAB_LAYOUT_ID);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, TOP_LAYOUT_ID);
		addView(mTabLayout, params);

	}

	/**
	 * 初始化桌面主题的主界面
	 * */
	private void initLauncherMainView() {
		mScrollerViewGroup = new ScrollerViewGroup(getContext(), this);
		if (mFeaturedThemeContainer == null || mInstalledThemeContainer == null
				|| mHotThemeContainer == null) {
			initThemeContainer();
		}
		LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mScrollerViewGroup.addView(mFeaturedThemeContainer, linearParams);
		mScrollerViewGroup.addView(mHotThemeContainer, linearParams);
		mScrollerViewGroup.addView(mInstalledThemeContainer, linearParams);
		mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		relativeParams.addRule(RelativeLayout.BELOW, TAB_LAYOUT_ID);
		addView(mScrollerViewGroup, relativeParams);
		mScrollerViewGroup.gotoViewByIndex(mCurTabId);
		mScrollerViewGroup.setCircle(true);
		mScrollerViewGroup.getScreenScroller().setOvershootPercent(0);
	}

	/**
	 * 初始化锁屏主题主界面
	 * */
	private void initLockMainView() {
		mScrollerViewGroup = new ScrollerViewGroup(getContext(), this);
		if (mFeaturedThemeContainer == null || mInstalledThemeContainer == null
				|| mHotThemeContainer == null) {
			initThemeContainer();
		}
		LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mScrollerViewGroup.addView(mFeaturedThemeContainer, linearParams);
		mScrollerViewGroup.addView(mInstalledThemeContainer, linearParams);
		mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		relativeParams.addRule(RelativeLayout.BELOW, TAB_LAYOUT_ID);
		addView(mScrollerViewGroup, relativeParams);
		mScrollerViewGroup.gotoViewByIndex(mCurTabId);
		mScrollerViewGroup.setCircle(false);

	}

	private void addLockerSettingTip() {
		boolean hasClick = false;
		boolean hasEnterLockTab = false;
		PreferencesManager pm = new PreferencesManager(getContext(),
				IPreferencesIds.LOCKER_SETTING_PRF, Context.MODE_PRIVATE);
		hasClick = pm.getBoolean(IPreferencesIds.CLICK_LOCKER_SETTING, false);
		hasEnterLockTab = pm.getBoolean(IPreferencesIds.ENTER_LOCKER_TAB, false);
		if (!hasClick) {
			if (hasEnterLockTab) {
				if (mLokerSettingNotifyView != null) {
					removeView(mLokerSettingNotifyView);
				}
				mLokerSettingNotifyView = new TextView(getContext());
				mLokerSettingNotifyView.setBackgroundResource(R.drawable.locker_setting_new_tips);
				mLokerSettingNotifyView.setText(R.string.locker_setting_tip);
				mLokerSettingNotifyView.setTextColor(Color.WHITE);
				mLokerSettingNotifyView.setTextSize(16);
				mLokerSettingNotifyView.setSingleLine();
				mLokerSettingNotifyView.setShadowLayer(2, 0, -1, 0x80000000);
				mLokerSettingNotifyView.setPadding(12, 5, 12, 0);
				mLokerSettingNotifyView.setGravity(Gravity.CENTER);
				mLokerSettingNotifyView.setHeight(getContext().getResources()
						.getDimensionPixelSize(R.dimen.locker_tip_height));
				//					mLokerSettingNotifyView.set
				mLokerSettingNotifyView.setOnClickListener(this);
				//				}
				RelativeLayout.LayoutParams pa = new RelativeLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				pa.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				pa.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				int marginTop = getContext().getResources().getDimensionPixelSize(
						R.dimen.locker_tip_margin_top);
				int marginRight = getContext().getResources().getDimensionPixelSize(
						R.dimen.locker_tip_margin_right);
				pa.setMargins(0, marginTop, marginRight, 0);
				addView(mLokerSettingNotifyView, pa);
			} else {
				pm.putBoolean(IPreferencesIds.ENTER_LOCKER_TAB, true);
				pm.commit();
			}
		}

	}

	/**
	 * 初始化顶部tab栏
	 * */
	private void initTopView() {

		RelativeLayout mTopLayout = (RelativeLayout) mInflater.inflate(R.layout.theme_manage_top,
				null);
		mDeskPoint = mTopLayout.findViewById(R.id.desk_lightpoint);
		mLockerPoint = mTopLayout.findViewById(R.id.lock_lightpoint);
		mDeskLayout = (RelativeLayout) mTopLayout.findViewById(R.id.desk_theme_layout);
		mDeskLayout.setOnClickListener(this);
		mDeskThemeTitle = (TextView) mTopLayout.findViewById(R.id.desk_theme);
		// mDeskThemeTitle.setOnClickListener(this);
		mLockerLayout = (RelativeLayout) mTopLayout.findViewById(R.id.locker_theme_layout);
		mLockerLayout.setOnClickListener(this);
		mLockerThemeTitle = (TextView) mTopLayout.findViewById(R.id.lock_theme);
		// mLockerThemeTitle.setOnClickListener(this);
		switch (mEntranceId) {
			case LAUNCHER_THEME_VIEW_ID :
				changeTopFocus(mDeskThemeTitle, mLockerThemeTitle);
				// mDeskPoint.setVisibility(VISIBLE);
				mLockerPoint.setVisibility(GONE);
				break;
			case LOCKER_THEME_VIEW_ID :
				changeTopFocus(mLockerThemeTitle, mDeskThemeTitle);
				mDeskPoint.setVisibility(GONE);
				// mLockerPoint.setVisibility(VISIBLE);
				break;
			default :
				break;
		}
		// 添加tab视图
		mTopLayout.setId(TOP_LAYOUT_ID);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		addView(mTopLayout, params);

	}

	/**
	 * 改变标题栏聚集状态(桌面主题和锁屏主题)
	 * 
	 * @param getFocus
	 *            :获取焦点的对象
	 * @param lostFocus
	 *            :失去焦点对象
	 * */
	private void changeTopFocus(TextView getFocus, TextView lostFocus) {
		getFocus.setTextColor(getResources().getColor(R.color.theme_top_cur_text));
		// getFocus.setBackgroundResource(R.drawable.theme_tab_current_selector);
		lostFocus.setTextColor(getResources().getColor(R.color.theme_top_text));
		// lostFocus.setBackgroundResource(R.drawable.theme_tab_default_selector);
	}

	/**
	 * 初始化主题容器
	 */
	private void initThemeContainer() {
		// LinearLayout.LayoutParams params = new
		// LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		// 精选主题
		mFeaturedThemeContainer = (ThemeContainer) mInflater.inflate(
				R.layout.theme_manage_container_layout, null);
		// 本地主题
		mInstalledThemeContainer = (ThemeContainer) mInflater.inflate(
				R.layout.theme_manage_container_layout, null);

		mHotThemeContainer = (ThemeContainer) mInflater.inflate(
				R.layout.theme_manage_container_layout, null);
		mHotThemeContainer.getNoThemeTextView().setText(
				getResources().getString(R.string.no_hot_themes));
		// mScrollerViewGroup.addView(mFeaturedThemeContainer, params);
		// mScrollerViewGroup.addView(mInstalledThemeContainer, params);
		// if (mEntranceId == LAUNCHER_THEME_VIEW_ID) {
		// //入口是桌面主题
		// if(mCurTabId==FEATURED_THEME_VIEW_ID){
		// mFeaturedThemeContainer.loadThemeData(ThemeConstants.LAUNCHER_FEATURED_THEME_ID);
		// }else{
		// mFeaturedThemeContainer.loadThemeData(ThemeConstants.LAUNCHER_FEATURED_THEME_ID);
		// }
		// mScrollerViewGroup.addView(mFeaturedThemeContainer, params);
		// mScrollerViewGroup.addView(mInstalledThemeContainer, params);
		//
		// } else if (mEntranceId == LOCKER_THEME_VIEW_ID) {
		// //入口是锁屏主题
		// if (isInstallGoLocker()) {
		//
		// mScrollerViewGroup.addView(mFeaturedThemeContainer, params);
		// mScrollerViewGroup.addView(mInstalledThemeContainer, params);
		// }else{
		// mTabLayout.setVisibility(GONE);
		// notInstallGoLockerView();
		//
		// }
		// }
		// mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());

	}

	/**
	 * 是否安装go锁屏
	 * 
	 * */
	private boolean isInstallGoLocker() {
		return AppUtils.isGoLockerExist(getContext());
	}

	// -----------------滚动器事件-----------------------//
	@Override
	public ScreenScroller getScreenScroller() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStart() {

	}

	@Override
	public void onFlingStart() {

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mCurTabId = newScreen;
	}

	@Override
	public void onScrollFinish(int currentScreen) {

		if (mEntranceId != ThemeManageView.LOCKER_THEME_VIEW_ID) {
			if (mCurTabId == FEATURED_THEME_VIEW_ID) {
				// 滚动到精选tab
				focusFeaturedThemeTab();
				StatisticsData.countThemeTabData(StatisticsData.THEME_TAB_ID_CHOICENESS_TAB);
			} else if (mCurTabId == INSTALLED_THEME_VIEW_ID) {
				// 滚动到本地tab
				focusInstalledThemeTab();
				StatisticsData.countThemeTabData(StatisticsData.THEME_TAB_ID_LOCAL_TAB);
			} else if (mCurTabId == HOT_THEME_VIEW_ID) {
				//滚到热门Tab
				focusHotThemeTab();
			}
		} else {
			if (mCurTabId == FEATURED_THEME_VIEW_ID) {
				// 滚动到精选tab
				focusFeaturedThemeTab();
				//GO锁屏那边特殊处理，因为精选、热门、本地分别是 0 ，1, 2 ，
				//但是锁屏中只有精选和本地，所以当前如果是锁屏那边，本地主题 INSTALLED_THEME_VIEW_ID-=1
			} else if (mCurTabId == INSTALLED_THEME_VIEW_ID - 1) {
				// 滚动到本地tab
				focusInstalledThemeTab();
			}
		}
		startLoadThemeData();
	}

	/**
	 * 跳转到设置界面
	 * */
	private void gotoSettingView() {

		if (mEntranceId == LAUNCHER_THEME_VIEW_ID) {
			// 桌面主题个性化设置
			Intent intent = new Intent(getContext(), DeskSettingVisualActivity.class);
			try {
				getContext().startActivity(intent);
			} catch (ActivityNotFoundException e) {
				// e.printStackTrace();
			}
		} else if (mEntranceId == LOCKER_THEME_VIEW_ID) {
			// 锁屏设置
			if (mNewLockerSettingLogo != null && mTabLayout != null) {
				mTabLayout.removeView(mNewLockerSettingLogo);
			}
			Intent intent = new Intent();
			intent.setAction(ICustomAction.ACTION_LOCKER_SETTING);
			try {
				getContext().startActivity(intent);
				StatisticsData.countThemeTabData(StatisticsData.THEME_TAB_ID_SETTING);
			} catch (ActivityNotFoundException e) {
				// e.printStackTrace();
			}

		}

	}

	@Override
	public void onClick(View v) {
		if (v == mDeskLayout) {
			if (mFeaturedThemeContainer != null) {
				mFeaturedThemeContainer.hideNoThemesTips();
			}
			if (mEntranceId == LAUNCHER_THEME_VIEW_ID) {
				return;
			}
			removeNewThemeLog();
			mEntranceId = LAUNCHER_THEME_VIEW_ID;
			mCurTabId = FEATURED_THEME_VIEW_ID;
			gotoLauncherTab(mCurTabId);
		} else if (v == mLockerLayout) {
			if (mFeaturedThemeContainer != null) {
				mFeaturedThemeContainer.hideNoThemesTips();
			}
			if (mEntranceId == LOCKER_THEME_VIEW_ID) {
				return;
			}
			removeNewThemeLog();
			mEntranceId = LOCKER_THEME_VIEW_ID;
			mCurTabId = FEATURED_THEME_VIEW_ID;
			gotoLockerTab();
			addLockerSettingTip();

		} else if (v == mFeaturedThemeTab || v == mFeaturedLayout || v == mFeaturedThemeImg) {
			if (mCurTabId == FEATURED_THEME_VIEW_ID || mScrollerViewGroup == null) {
				return;
			}
			removeNewThemeLog();
			mCurTabId = FEATURED_THEME_VIEW_ID;
			mScrollerViewGroup.gotoViewByIndex(mCurTabId);
			focusFeaturedThemeTab();
			int id = 0;
			if (mEntranceId == LAUNCHER_THEME_VIEW_ID) {
				id = ThemeConstants.STATICS_ID_FEATURED;
			} else {
				id = ThemeConstants.STATICS_ID_LOCKER;
			}
			StatisticsData.saveGuiTabStat(getContext(), String.valueOf(id));
		} else if (v == mInstalledThemeTab || v == mInstalledThemeImg || v == mInstalledLayout) {
			if (mFeaturedThemeContainer != null) {
				mFeaturedThemeContainer.hideNoThemesTips();
			}
			if (mCurTabId == INSTALLED_THEME_VIEW_ID || mScrollerViewGroup == null) {
				return;
			}
			mCurTabId = INSTALLED_THEME_VIEW_ID;
			//GO锁屏那边特殊处理，因为精选、热门、本地分别是 0 ，1, 2 ，
			//但是锁屏中只有精选和本地，所以当前如果是锁屏那边，本地主题 INSTALLED_THEME_VIEW_ID-=1
			if (mEntranceId == LOCKER_THEME_VIEW_ID) {
				mCurTabId--;
			}

			mScrollerViewGroup.gotoViewByIndex(mCurTabId);
			if (mHasNewTheme) {
				updateMainView(-1, -1);
			}
			if (mNewThemeLog != null) {
				mInstalledLayout.removeView(mNewThemeLog);
			}
			focusInstalledThemeTab();
		} else if (v == mHotThemeTab || v == mHotThemeImg || v == mHotLayout) {
			if (mCurTabId == HOT_THEME_VIEW_ID || mScrollerViewGroup == null) {
				return;
			}
			mCurTabId = HOT_THEME_VIEW_ID;
			mScrollerViewGroup.gotoViewByIndex(mCurTabId);
			focusHotThemeTab();
			StatisticsData.saveGuiTabStat(getContext(),
					String.valueOf(ThemeConstants.STATICS_ID_HOT));

		} else if (v == mSettingLayout || v == mSettingImg) {
			gotoSettingView();

			if (mEntranceId == LOCKER_THEME_VIEW_ID) {
				if (mLokerSettingNotifyView != null) {
					removeView(mLokerSettingNotifyView);
					mLokerSettingNotifyView = null;
				}
				PreferencesManager pm = new PreferencesManager(getContext(),
						IPreferencesIds.LOCKER_SETTING_PRF, Context.MODE_PRIVATE);
				boolean has = pm.getBoolean(IPreferencesIds.CLICK_LOCKER_SETTING, false);
				if (!has) {
					pm.putBoolean(IPreferencesIds.CLICK_LOCKER_SETTING, true);
					pm.commit();
				}
			}
		} else if (v.getId() == R.id.vipgroup) {
			onVipClick();
		} else if (v == mLokerSettingNotifyView) {
			removeView(v);
			mLokerSettingNotifyView = null;
			gotoSettingView();
			PreferencesManager pm = new PreferencesManager(getContext(),
					IPreferencesIds.LOCKER_SETTING_PRF, Context.MODE_PRIVATE);
			pm.putBoolean(IPreferencesIds.CLICK_LOCKER_SETTING, true);
			pm.commit();
		}

	}

	/**
	 * 没有安装go锁屏，显示提示安装go锁屏的界面
	 * */
	private void notInstallGoLockerView() {

		if (mNoGolockerTipsView != null) {
			this.removeView(mNoGolockerTipsView);
		}

		// 没有安装
		if (SpaceCalculator.sPortrait) {
			mNoGolockerTipsView = mInflater.inflate(R.layout.theme_manage_no_golocker_v, null);
		} else {
			mNoGolockerTipsView = mInflater.inflate(R.layout.theme_manage_no_golocker_h, null);
		}
		Button downloadBtn = (Button) mNoGolockerTipsView.findViewById(R.id.download_golocker);
		DeskSettingConstants.setTextViewTypeFace(downloadBtn);
		downloadBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//				if (!AppUtils.gotoMarket(getContext(), LauncherEnv.Market.BY_PKGNAME
				//						+ LauncherEnv.Plugin.LOCKER_PACKAGE)) {
				//					AppUtils.gotoBrowser(getContext(), LauncherEnv.Url.GOLOCKER_DOWNLOAD_URL);
				//				}
				// modify by Ryan 2012.08.29
				CheckApplication.downloadAppFromMarketGostoreDetail(getContext(),
						LauncherEnv.GO_LOCK_PACKAGE_NAME,
						LauncherEnv.Url.GOLOCKER_IN_THEME_WITH_GOOGLE_REFERRAL_LINK);
				// end
			}
		});
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, TOP_LAYOUT_ID);
		params.topMargin = getResources().getDimensionPixelSize(R.dimen.logo_padding_top_v);
		// params.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(mNoGolockerTipsView, params);
	}

	/**
	 * 本地tab选中状态
	 * */
	private void focusInstalledThemeTab() {
		mFeaturedThemeTab.setTextColor(getResources().getColor(R.color.theme_tab_no_focus));
		mFeaturedThemeImg.setImageResource(R.drawable.theme_tab_normal);
		mInstalledThemeTab.setTextColor(getResources().getColor(R.color.theme_tab_focus));
		mInstalledThemeImg.setImageResource(R.drawable.theme_tab_light);
		if (mHotThemeTab != null) {
			mHotThemeTab.setTextColor(getResources().getColor(R.color.theme_tab_no_focus));
			mHotThemeImg.setImageResource(R.drawable.theme_tab_normal);
		}
	}

	/**
	 * 精选tab选中状态
	 * */
	private void focusFeaturedThemeTab() {
		mFeaturedThemeTab.setTextColor(getResources().getColor(R.color.theme_tab_focus));
		mFeaturedThemeImg.setImageResource(R.drawable.theme_tab_light);
		mInstalledThemeTab.setTextColor(getResources().getColor(R.color.theme_tab_no_focus));
		mInstalledThemeImg.setImageResource(R.drawable.theme_tab_normal);
		if (mHotThemeTab != null) {
			mHotThemeTab.setTextColor(getResources().getColor(R.color.theme_tab_no_focus));
			mHotThemeImg.setImageResource(R.drawable.theme_tab_normal);
		}

	}

	/**
	 * 热门tab选中状态
	 */
	private void focusHotThemeTab() {
		if (mHotThemeTab != null) {
			mHotThemeTab.setTextColor(getResources().getColor(R.color.theme_tab_focus));
			mHotThemeImg.setImageResource(R.drawable.theme_tab_light);
		}
		mInstalledThemeTab.setTextColor(getResources().getColor(R.color.theme_tab_no_focus));
		mInstalledThemeImg.setImageResource(R.drawable.theme_tab_normal);
		mFeaturedThemeTab.setTextColor(getResources().getColor(R.color.theme_tab_no_focus));
		mFeaturedThemeImg.setImageResource(R.drawable.theme_tab_normal);

	}

	public void changeOrientation() {
		removeView(mTabLayout);
		if (mEntranceId == LOCKER_THEME_VIEW_ID) {
			initLockTabView();
			if (mLokerSettingNotifyView != null) {
				addLockerSettingTip();
			}
		} else {
			initLauncherTabView();
		}
		if (mEntranceId == LOCKER_THEME_VIEW_ID && !isInstallGoLocker()) {
			mTabLayout.setVisibility(GONE);
			removeView(mNoGolockerTipsView);
			mNoGolockerTipsView = null;
			notInstallGoLockerView();
		}

		if (mFeaturedThemeContainer != null) {
			mFeaturedThemeContainer.changeOrientation();
		}
		if (mInstalledThemeContainer != null) {
			mInstalledThemeContainer.changeOrientation();
		}
		if (mHotThemeContainer != null) {
			mHotThemeContainer.changeOrientation();
		}
	}

	/**
	 * 刷新界面
	 * 
	 * @param entranceId
	 * @param curTabId
	 * 
	 * */
	public void updateMainView(int entranceId, int curTabId) {
		//		ThemeDataManager.getInstance(getContext()).clearup();
		if (entranceId == -1) {
			entranceId = mEntranceId;
		} else {
			mEntranceId = entranceId;
		}
		if (curTabId == -1) {
			curTabId = mCurTabId;
		} else {
			mCurTabId = curTabId;
		}
		if (entranceId == LAUNCHER_THEME_VIEW_ID) {
			// 更新桌面主题界面
			gotoLauncherTab(curTabId);
		} else if (entranceId == LOCKER_THEME_VIEW_ID) {
			// 更新锁屏主题界面
			gotoLockerTab();
			if (mLokerSettingNotifyView != null) {
				addLockerSettingTip();
			}
		}

	}

	/**
	 * 切换到桌面主题界面
	 * */
	private void gotoLauncherTab(int curTabId) {
		if (mTabLayout.getVisibility() == GONE) {
			mTabLayout.setVisibility(VISIBLE);
		}
		if (mNoGolockerTipsView != null) {
			removeView(mNoGolockerTipsView);
		}
		changeTopFocus(mDeskThemeTitle, mLockerThemeTitle);
		mLockerPoint.setVisibility(GONE);
		mDeskPoint.setVisibility(VISIBLE);
		if (curTabId == this.FEATURED_THEME_VIEW_ID) {
			PreferencesManager sp = new PreferencesManager(getContext(),
					IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
			sp.putBoolean(IPreferencesIds.HASNEWTHEME, false);
			sp.commit();
			focusFeaturedThemeTab();
		} else {
			focusHotThemeTab();
		}

		initLauncherTabView();
		clearup();
		clearScrollerViewGroup();
		//		if(mEntranceId == ThemeManageView.LAUNCHER_THEME_VIEW_ID) {
		//			if(mHotLayout!=null&&!mHotLayout.isShown()){
		//				mHotLayout.setVisibility(View.VISIBLE);
		//			}
		//		}
		initLauncherMainView();
		startLoadThemeData();
	}

	/**
	 * 切换到锁屏主题界面
	 * */
	private void gotoLockerTab() {
		PreferencesManager sp = new PreferencesManager(getContext(),
				IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
		sp.putBoolean(IPreferencesIds.LOCKER_HASNEWTHEME, false);
		sp.commit();
		changeTopFocus(mLockerThemeTitle, mDeskThemeTitle);
		mDeskPoint.setVisibility(GONE);
		mLockerPoint.setVisibility(VISIBLE);
		focusFeaturedThemeTab();
		initLockTabView();
		clearup();
		// 判断是否安装go锁屏
		if (isInstallGoLocker()) {
			if (mNoGolockerTipsView != null && mNoGolockerTipsView.getVisibility() == View.VISIBLE) {
				mNoGolockerTipsView.setVisibility(View.GONE);
			}
			clearScrollerViewGroup();
			if (mEntranceId == ThemeManageView.LOCKER_THEME_VIEW_ID) {
				if (mHotLayout != null && mHotLayout.isShown()) {
					mHotLayout.setVisibility(View.GONE);
				}
			}
			initLockMainView();
			startLoadThemeData();
		} else {
			mTabLayout.setVisibility(GONE);
			clearScrollerViewGroup();
			notInstallGoLockerView();
		}
	}

	/**
	 * 清空ViewGroup
	 * */
	private void clearScrollerViewGroup() {
		if (mScrollerViewGroup != null) {
			mScrollerViewGroup.removeAllViews();
			mScrollerViewGroup = null;
		}
	}

	/**
	 * 清空缓存数据
	 * */
	public void clearup() {
		ThemeImageManager.getInstance(getContext()).clearup();
		if (mFeaturedThemeContainer != null) {
			mFeaturedThemeContainer.cleanup();
		}
		if (mInstalledThemeContainer != null) {
			mInstalledThemeContainer.cleanup();
		}
		if (mHotThemeContainer != null) {
			mHotThemeContainer.cleanup();
		}
	}
	/**
	 * 清空缓存数据和释放字体
	 * */
	public void selfDestruct() {
		ThemeImageManager.getInstance(getContext()).clearup();
		if (mFeaturedThemeContainer != null) {
			DeskSettingConstants.selfDestruct(mFeaturedThemeContainer);
			mFeaturedThemeContainer.cleanup();
		}
		if (mInstalledThemeContainer != null) {
			DeskSettingConstants.selfDestruct(mInstalledThemeContainer);
			mInstalledThemeContainer.cleanup();
		}
		if (mHotThemeContainer != null) {
			DeskSettingConstants.selfDestruct(mHotThemeContainer);
			mHotThemeContainer.cleanup();
		}
		if (mDeskThemeTitle != null && mDeskThemeTitle instanceof DeskTextView) {
			((DeskTextView) mDeskThemeTitle).selfDestruct();
			mDeskThemeTitle = null;
		}
		if (mLockerThemeTitle != null && mLockerThemeTitle instanceof DeskTextView) {
			((DeskTextView) mLockerThemeTitle).selfDestruct();
			mLockerThemeTitle = null;
		}
		if (mFeaturedThemeTab != null && mFeaturedThemeTab instanceof DeskTextView) {
			((DeskTextView) mFeaturedThemeTab).selfDestruct();
			mFeaturedThemeTab = null;
		}
		if (mHotThemeTab != null && mHotThemeTab instanceof DeskTextView) {
			((DeskTextView) mHotThemeTab).selfDestruct();
			mHotThemeTab = null;
		}
		if (mInstalledThemeTab != null && mInstalledThemeTab instanceof DeskTextView) {
			((DeskTextView) mInstalledThemeTab).selfDestruct();
			mInstalledThemeTab = null;
		}

		if (mReceiver != null) {
			getContext().unregisterReceiver(mReceiver);
		}
	}
	/**
	 * 跳转到gostore
	 * */
	public void gotoGoStore() {
		//		Intent intent = new Intent();
		//		intent.setClass(getContext(), GoStore.class);
		AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(getContext(),
				AppRecommendedStatisticsUtil.ENTRY_TYPE_THEMEMANAGE);
		if (mEntranceId == LAUNCHER_THEME_VIEW_ID) {
			// 跳转到主题tab页
			//			intent.putExtra("sort", SortsBean.SORT_THEME + "");
			//			StatisticsData.countStatData(getContext(), StatisticsData.ENTRY_KEY_THEMEMANAGE);
			AppsManagementActivity.startAppCenter(getContext(),
					MainViewGroup.ACCESS_FOR_APPCENTER_THEME_PAY, false);
			//			GoStoreStatisticsUtil.setCurrentEntry(GoStoreStatisticsUtil.ENTRY_TYPE_THEMEMANAGE,
			//					getContext());
		} else if (mEntranceId == LOCKER_THEME_VIEW_ID) {
			// 跳转到锁屏tab页
			//			intent.putExtra("sort", SortsBean.SORT_LOCKER + "");
			AppsManagementActivity.startAppCenter(getContext(),
					MainViewGroup.ACCESS_FOR_APPCENTER_LOCKER, false);
		}
		//		getContext().startActivity(intent);
		StatisticsData.countThemeTabData(StatisticsData.THEME_TAB_ID_GET_MORE_THEME);
	}

	/**
	 * 获得当前view EntranceId
	 * 
	 * @return
	 */
	public int getEntranceId() {
		return mEntranceId;
	}
	public int getCurTabId() {
		return mCurTabId;
	}

	public void onDestroy() {
		selfDestruct();
		if (mFeaturedThemeContainer != null) {
			mFeaturedThemeContainer.onDestroy();
		}
	}

	public void addNewLog() {
		//		if (mEntranceId == LAUNCHER_THEME_VIEW_ID) {
		if (mNewThemeLog == null) {
			mNewThemeLog = new ImageView(getContext());
			mNewThemeLog.setImageResource(R.drawable.theme_new_log);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			mInstalledLayout.addView(mNewThemeLog, params);
		}
		//		}
		mHasNewTheme = true;
	}

	public void removeNewThemeLog() {
		if (mNewThemeLog != null && mInstalledLayout != null) {
			mInstalledLayout.removeView(mNewThemeLog);
			mHasNewTheme = false;
		}
	}

	public Boolean isTabLayoutShown() {
		if (mTabLayout.isShown()) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 *  回传处理ThemeManagerActivity   KeyDown事件
	 */
	protected Boolean keyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
			if (isTabLayoutShown()) {
				gotoSettingView();
				return true;
			}
		}
		return false;
	}

	public void reLoadBannerData(int type) {
		if (mFeaturedThemeContainer != null) {
			mFeaturedThemeContainer.reLoadBannerData(type);
		}
	}
	/**
	 * <br>功能简述:查询锁屏界面是否有更新
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private boolean hasNewLockerSetting() {
		boolean result = false;
		ContentResolver contentResolver = getContext().getContentResolver();
		if (contentResolver != null) {
			Uri golockerUri = Uri.parse(ThemeManager.LOCKER_THEME_QUERY_URI);
			Cursor cursor = null;
			try {
				cursor = contentResolver.query(golockerUri, null, null, null, null);
				if (cursor != null && cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(NEWSETTINGOPTION);
					result = ConvertUtils.int2boolean(cursor.getInt(index));
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		return result;
	}

	public void onResume() {
		if (mTabLayout != null && isInstallGoLocker() && mEntranceId == LOCKER_THEME_VIEW_ID
				&& hasNewLockerSetting()) {
			if (mNewLockerSettingLogo == null) {
				mNewLockerSettingLogo = new ImageView(getContext());
				mNewLockerSettingLogo.setImageResource(R.drawable.locker_setting_new);
			} else {
				mTabLayout.removeView(mNewLockerSettingLogo);
			}
			RelativeLayout.LayoutParams pa = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			pa.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			pa.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			pa.setMargins(0, 10, 10, 0);
			mTabLayout.addView(mNewLockerSettingLogo, pa);
		}
	}

	private void onVipClick() {
		int vip = ThemePurchaseManager.getCustomerLevel(getContext());
		if (ThemeConstants.CUSTOMER_LEVEL2 == vip) {
			showVipTipDialog(vip);

		} else {

			ThemePurchaseManager.getInstance(getContext()); //init ThemePurchaseManager instance here
			Intent intent = new Intent(getContext(), ThemeVipPage.class);

			intent.putExtra("url", ThemeManager.getVipPayPageUrl(getContext()));
			getContext().startActivity(intent);
		}
	}

	private void showVipTipDialog(int level) {

		DialogConfirm dialog = new DialogConfirm(getContext());
		dialog.show();
		if (level == ThemeConstants.CUSTOMER_LEVEL1) {
			dialog.setTitle(R.string.vip_level1_title);
			dialog.setMessage(R.string.vip_level1_tip);
		} else {
			dialog.setTitle(R.string.vip_level2_title);
			dialog.setMessage(R.string.vip_level2_tip);
		}
		dialog.setNegativeButtonVisible(View.GONE);
		dialog.setPositiveButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});

	}

	private void showPaySeccessDialog(final int level) {

		DialogConfirm dialog = new DialogConfirm(getContext());
		dialog.show();
		dialog.setTitle(R.string.vip_paid_success);
		if (level == ThemeConstants.CUSTOMER_LEVEL1) {
			dialog.setMessage(R.string.vip_level1_tip);
		} else {
			dialog.setMessage(R.string.vip_level2_tip);
		}
		int btnResId = R.string.ok;
		if (GoFacebookUtil.isEnable()) {
			btnResId = R.string.facebook_share_on_facebook;
		}
		dialog.setNegativeButtonVisible(View.GONE);
		dialog.setPositiveButton(btnResId, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (GoFacebookUtil.isEnable()) {
					shareVipOnFaceBook(level);
				}
			}
		});

	}

	private void shareVipOnFaceBook(int level) {
		int shareResId = R.string.vip_level2_facebook_share_content;
		if (level == ThemeConstants.CUSTOMER_LEVEL1) {
			shareResId = R.string.vip_level1_facebook_share_content;
		}
		GoFacebookUtil.postAMsg((Activity) getContext(), getContext().getString(shareResId));
	}

	private void checkVipLogo() {
		if (mEntranceId == LAUNCHER_THEME_VIEW_ID && mTabLayout != null) {
			mTabLayout.findViewById(R.id.vipbtn).setOnClickListener(this);
			ImageView vipBtn = (ImageView) mTabLayout.findViewById(R.id.vipbtn);
			int vip = ThemePurchaseManager.getCustomerLevel(getContext());
			if (vip == ThemeConstants.CUSTOMER_LEVEL1) {
				vipBtn.setImageResource(R.drawable.theme_vip_level1);
			} else if (vip == ThemeConstants.CUSTOMER_LEVEL2) {
				vipBtn.setImageResource(R.drawable.theme_vip_level2);
			} else {
				vipBtn.setImageResource(R.drawable.theme_vip_level0);
			}
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (ICustomAction.ACTION_PURCHASE_STATE_RESULT.equals(intent.getAction())) {
				String itemId = intent.getStringExtra(ThemeAppInBillingManager.EXTRA_FOR_ITEMID);
				int status = intent.getIntExtra(ThemeAppInBillingManager.EXTRA_FOR_STATE, 0);
				if ((itemId.equals(ThemeConstants.VIP_LEVE1_PAY_ID)
						|| itemId.equals(ThemeConstants.VIP_LEVE2_PAY_ID) || itemId
							.equals(ThemeConstants.VIP_LEVE1_UPGRADE_PAY_ID))
						&& ThemeAppInBillingManager.PURCHASE_STATE_PURCHASED == status) {
					int vip = 1;
					int payItem = 0;
					if (itemId.equals(ThemeConstants.VIP_LEVE1_PAY_ID)) {
						vip = ThemeConstants.CUSTOMER_LEVEL1;
						payItem = 1;
					} else if (itemId.equals(ThemeConstants.VIP_LEVE2_PAY_ID)) {
						vip = ThemeConstants.CUSTOMER_LEVEL2;
						payItem = 2;
					} else if (itemId.equals(ThemeConstants.VIP_LEVE1_UPGRADE_PAY_ID)) {
						vip = ThemeConstants.CUSTOMER_LEVEL2;
						payItem = 3;
					}
					if (itemId.equals(ThemeConstants.VIP_LEVE1_PAY_ID)) {
						String id = Machine.getAndroidId();
						String level = CryptTool
								.encrypt(IPreferencesIds.THEME_CUSTOMER_LEVEL_1, id);
						if (ThemePurchaseManager.queryPurchaseState(context, level) == null) {
							new PurchaseStateManager(getContext()).save(level, level);
						}

					} else if (itemId.equals(ThemeConstants.VIP_LEVE2_PAY_ID)
							|| itemId.equals(ThemeConstants.VIP_LEVE1_UPGRADE_PAY_ID)) {
						String id = Machine.getAndroidId();
						String level = CryptTool
								.encrypt(IPreferencesIds.THEME_CUSTOMER_LEVEL_2, id);
						if (ThemePurchaseManager.queryPurchaseState(context, level) == null) {
							new PurchaseStateManager(getContext()).save(level, level);
						}
					}
					if (ThemeVipPage.sPayItem == payItem) {
						showPaySeccessDialog(vip);
					}
					checkVipLogo();
					FileUtil.deleteFile(ThemeManager.XMLFILE);
					FileUtil.deleteFile(ThemeManager.HOT_XMLFILE);
					FileUtil.deleteFile(LockerManager.XMLFILE);
					PreferencesManager pm = new PreferencesManager(context,
							IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
					pm.putLong(OnlineThemeGetter.LAUNCHER_HOTTHEME_STAMP, 0);
					pm.putLong(IPreferencesIds.LAUNCHER_FEATUREDTHEME_STAMP, 0);
					pm.putLong(OnlineThemeGetter.LCOKER_FEATUREDTHEME_STAMP, 0);
					pm.commit();
					ThemeDataManager.getInstance(context).clearup();
					Intent it = new Intent(ICustomAction.ACTION_FEATURED_THEME_CHANGED);
					it.setData(Uri.parse("package://"));
					getContext().sendBroadcast(it);
				}
			}
		}
	};

}
