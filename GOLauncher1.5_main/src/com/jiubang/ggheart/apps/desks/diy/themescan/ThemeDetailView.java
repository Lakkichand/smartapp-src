package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.gostore.base.component.AppsThemeDetailActivity;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IDetailScanHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IndicatorListner;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.InfoScreenIndicator;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.billing.ThemeAppInBillingManager;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.data.theme.broadcastReceiver.MyThemeReceiver;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;

/**
 * @author chenguanyu
 */
public class ThemeDetailView extends RelativeLayout
		implements
			ICleanable,
			IndicatorListner,
			IDetailScanHandler,
			OnClickListener,
			OnDismissListener,
			OnItemClickListener,
			OnKeyListener,
			BroadCasterObserver {

	private static final int GET_TEXT_COLOR = 0XFFECF3DC;
	private static final int GET_TEXT_SHADOW_COLOR = 0X80000000;
	private static final float GET_TEXT_SIZE = 13.3f;
	private static final int THEME_DOWNLOAD_FINISH = 1;

	public static final String GOLAUNCHER_ACTION = "com.gau.go.launcherex.MAIN";
	public static final String GOWIDGET_ACTION = "com.gau.go.launcherex.gowidget";
	public static final String GOLOCK_ACTION = "com.jiubang.goscreenlock";
	//	private static String ACTION_SEND_TO_GOLOCK = "com.gau.go.launcherex_action_send_to_golock";
	public static final String NEW_THEME_KEY = "newtheme";

	private Context mContext;
	private ThemeInfoBean mInfoBean = null; // 主题数据
	private ThemeDetailScan mThemeDetailScan; // 主题详情界面
	private InfoScreenIndicator mIndicator; // 主题详情的指示器

	private boolean mIsThemeModifyed;
	private ThemePrefSettingManager mThemePrefSettingManager;

	private RelativeLayout mTitleLayout;

	private View mBackButton = null; // 顶部返回按钮
	private View mShareButton = null; // 顶部分享按钮
	private View mMenuButton = null; // 顶部菜单按钮
	private View mApplyButton = null; // 应用按钮
	private View mApplyButtonLand = null;

	private View mLinearUpdate = null;
	private View mUpdateButton = null; // UpDate的按钮

	private PopupWindow mPopupWindow = null; // 弹出窗口
	private ListView mListView; // 弹出菜单项
	private float mClickTime; // 点击时间记录，因为快速点击会出错响应多次

	private String mResetMenuItemText; // 菜单-恢复默认
	private String mDelMenuItemText; // 菜单-删除
	private String mInfoMenuItemText; // 菜单-主题信息
	private String mPreviewMenuItemText; // 菜单-主题预览

	private boolean mIsInfoView;
	/*
	 * 分享的内容
	 */
	private String mExtraSubject = null;
	private String mExtraText = null;
	private Uri mPicUri = null;
	private String mSharePicPath = null;

	// 大主题

	private AlertDialog mDialog;
	private Resources mResources;
	private String mGolauncherText;
	private String mGowidgetText;
	private String mGolockText;

	private LayoutInflater mInflater;

	private Boolean mIsDownloading = false;
	private ThemePurchaseManager mPurchaseManager;
	private BroadcastReceiver mDownloadReceiver = null;
	private boolean mDownLoading = false;
	public ThemeDetailView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mResources = getResources();
		mListView = new ListView(context);
		mInflater = LayoutInflater.from(context);
		mPurchaseManager = ThemePurchaseManager.getInstance(GOLauncherApp.getApplication());
		mPurchaseManager.registerObserver(this);
		initDownloadReceiver();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mThemeDetailScan = (ThemeDetailScan) findViewById(R.id.theme_detail).findViewById(
				R.id.theme_detail_image);
		mThemeDetailScan.setIndicatorHandler(this);
		mThemeDetailScan.setParent(this);
		mIndicator = (InfoScreenIndicator) findViewById(R.id.theme_detail).findViewById(
				R.id.indicator);
		mIndicator.resetIndicatorPic();
		mIndicator.setListner(this);
	}

	/**
	 * 初始化菜单项
	 * */
	private void initMenuList() {

		MenuAdapter adapter = new MenuAdapter(getMenuItemNames());
		mListView.setAdapter(adapter);
		mListView.setCacheColorHint(Color.TRANSPARENT);
		mListView.setOnItemClickListener(this);
		mListView.setDrawingCacheEnabled(true);
		mListView.setBackgroundDrawable(mContext.getResources().getDrawable(
				R.drawable.theme_detail_menu_bg));
		mListView.setDivider(mContext.getResources().getDrawable(
				R.drawable.allfunc_allapp_menu_line));

	}

	/**
	 * 获取菜单项名称
	 * */
	private String[] getMenuItemNames() {
		ArrayList<String> names = new ArrayList<String>();
		if (mIsThemeModifyed) {
			names.add(mResetMenuItemText);
		}
		if (ThemeManager.isInstalledTheme(mContext, mInfoBean.getPackageName())) {
			if (isDefaultThemeOrUseingTheme()) {
				// 如果使用默认主题
				if (mResources != null
						&& mInfoBean != null
						&& ThemeManager.DEFAULT_THEME_PACKAGE_3_NEWER.equals(mInfoBean
								.getPackageName())
						&& !ThemeManager.getInstance(mContext).getCurThemePackage()
								.equals(mInfoBean.getPackageName())) {
					// 3.0以后主题则显示卸载更新
					mDelMenuItemText = mResources.getString(R.string.theme_detail_menu_del_update);
					names.add(mDelMenuItemText);
				}
			} else {
				// 非默认主题，则增加删除按钮
				names.add(mDelMenuItemText);
			}
		}
		if (mIsInfoView) {
			names.add(mPreviewMenuItemText);
		} else {
			names.add(mInfoMenuItemText);
		}
		return names.toArray(new String[] {});

	}

	/**
	 * 判断是否为默认主题和当前正在使用的主题
	 */
	private boolean isDefaultThemeOrUseingTheme() {

		if (ThemeManager.isAsDefaultThemeToDo(mInfoBean.getPackageName())
				|| ThemeManager.getInstance(mContext).getCurThemePackage()
						.equals(mInfoBean.getPackageName())) {
			return true;
		}
		return false;
	}

	/**
	 * 初始化监听点击对象
	 */
	private void initClickListener() {
		try {
			mTitleLayout = (RelativeLayout) findViewById(R.id.detail_title);
			// 返回按钮
			mBackButton = mTitleLayout.findViewById(R.id.back);
			mBackButton.setOnClickListener(this);
			// 分享按钮
			mShareButton = mTitleLayout.findViewById(R.id.theme_detail_share_btn);
			mShareButton.setOnClickListener(this);
			if (!ThemeManager.isInstalledTheme(mContext, mInfoBean.getPackageName())) {
				mShareButton.setVisibility(View.GONE);
			} else {
				mShareButton.setVisibility(View.VISIBLE);
			}
			// 菜单按钮
			mMenuButton = mTitleLayout.findViewById(R.id.theme_detail_menu_btn);
			mMenuButton.setOnClickListener(this);
			boolean isvip = ThemePurchaseManager.getCustomerLevel(mContext) == ThemeConstants.CUSTOMER_LEVEL0
					? false
					: true;
			// 应用按钮
			if (SpaceCalculator.sPortrait) {
				mTitleLayout.findViewById(R.id.theme_featured_detail_apply_btn).setVisibility(GONE);
				findViewById(R.id.detail_buttons).setVisibility(View.VISIBLE);
				mTitleLayout.findViewById(R.id.theme_detail_apply_btn).setVisibility(GONE);
				mApplyButton = findViewById(R.id.detail_buttons).findViewById(R.id.theme_apply);
				mApplyButton.setVisibility(VISIBLE);
				if (mInfoBean.getFeaturedId() != 0 || mInfoBean.getDownLoadUrl() != null) {
					mTitleLayout.findViewById(R.id.theme_detail_download_btn_land).setVisibility(
							GONE);
					((ImageView) mMenuButton).setImageResource(R.drawable.theme_detail_info);
					mApplyButton.setBackgroundResource(R.drawable.theme_detail_get_selector);
					if (isvip
							|| ThemePurchaseManager.queryPurchaseState(mContext,
									mInfoBean.getPackageName()) != null) {
						if (mPurchaseManager.hasDownloaded(mInfoBean.getThemeName(),
								mInfoBean.getPackageName())) {
							((Button) mApplyButton).setText(R.string.theme_pages_apply);
							((Button) mApplyButton)
									.setBackgroundResource(R.drawable.theme_paid_apply);;
						} else {
							((Button) mApplyButton).setText(R.string.gostore_download);
						}
					} else {
						((Button) mApplyButton).setText(R.string.theme_detail_getnow);
					}
					((Button) mApplyButton).setTextColor(GET_TEXT_COLOR);
					((Button) mApplyButton).setShadowLayer(0, 0, 1, GET_TEXT_SHADOW_COLOR);
					((Button) mApplyButton).setTextSize(GET_TEXT_SIZE);
					((Button) mApplyButton).setCompoundDrawablesWithIntrinsicBounds(null, null,
							null, null);
				}
				mApplyButton.setOnClickListener(this);

				// 目前已知最新版本的UI3.0主题的Version= 5 ，
				if (mInfoBean.getPackageName().equals(ThemeManager.DEFAULT_THEME_PACKAGE_3_NEWER)
						|| mInfoBean.getPackageName().equals(ThemeManager.DEFAULT_THEME_PACKAGE_3)) {

					if (mInfoBean.getVerId() < ThemeManager.NEW_UI3_THEME_VERSION) {

						mLinearUpdate = findViewById(R.id.detail_buttons).findViewById(
								R.id.linear_update);
						mLinearUpdate.setVisibility(View.VISIBLE);
						mUpdateButton = findViewById(R.id.detail_buttons).findViewById(
								R.id.theme_update);
						mUpdateButton.setOnClickListener(this);
					}
				}
			} else {
				findViewById(R.id.detail_buttons).setVisibility(GONE);

				if (mInfoBean.getPackageName().equals(ThemeManager.DEFAULT_THEME_PACKAGE_3_NEWER)
						|| mInfoBean.getPackageName().equals(ThemeManager.DEFAULT_THEME_PACKAGE_3)) {
					if (mInfoBean.getVerId() < ThemeManager.NEW_UI3_THEME_VERSION) {
						mUpdateButton = mTitleLayout.findViewById(R.id.theme_update_btn);
						mUpdateButton.setVisibility(View.VISIBLE);
						mUpdateButton.setOnClickListener(this);

					}
				}

				if (mInfoBean.getFeaturedId() != 0) {
					mTitleLayout.findViewById(R.id.theme_featured_detail_apply_btn).setVisibility(
							View.GONE);
					mApplyButton = mTitleLayout.findViewById(R.id.theme_detail_download_btn_land);
					mApplyButton.setOnClickListener(this);
					mApplyButton.setVisibility(VISIBLE);
				} else {
					mTitleLayout.findViewById(R.id.theme_detail_download_btn_land).setVisibility(GONE);
					mApplyButton = mTitleLayout.findViewById(R.id.theme_detail_apply_btn);
					mApplyButton.setVisibility(VISIBLE);
				}
				mApplyButton.setOnClickListener(this);
			}
		} catch (Exception e) {

		}
	}

	@Override
	public void updateIndicatorCurrent(int current) {
		if (null != mThemeDetailScan && current >= 0) {
			mIndicator.setCurrent(current);
		}
	}

	@Override
	public void clickIndicatorItem(int index) {
		if (null != mThemeDetailScan && index < mThemeDetailScan.getScreenCount() && index >= 0) {
			mThemeDetailScan.snapToScreen(index, false, -1);
		}
	}

	@Override
	public void sliding(float percent) {
		if (null != mThemeDetailScan && 0 <= percent && percent <= 100) {
			mThemeDetailScan.getScreenScroller().setScrollPercent(percent);
		}
	}

	@Override
	public void cleanup() {
		if (null != mThemeDetailScan) {
			mThemeDetailScan.cleanup();
			mThemeDetailScan = null;
		}
		mInfoBean = null;
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}
		if (mPurchaseManager != null) {
			mPurchaseManager.unRegisterObserver(this);
		}
		mPurchaseManager = null;
		mContext.unregisterReceiver(mDownloadReceiver);
	}

	@Override
	public void onClick(View v) {
		if (v == null) {
			return;
		}
		if (v == mBackButton) {
			goBack();
		} else if (v == mShareButton && isRespondClick(v)) {
			// 分享
			shareTheme();
			StatisticsData.countThemeTabData(StatisticsData.THEME_TAB_ID_LOCAL_THEME_SHARE);
		} else if (v == mMenuButton) {
			// 菜单
			if (mInfoBean.getFeaturedId() != 0) {
				if (mIsInfoView != true) {
					goToThemeInfoView();
				} else {
					gotoImageView();
				}
			} else {
				if (isMenuShowing()) {
					dismissMenu();
				} else {
					openMenu(v);
				}
			}
		} else if (v == mUpdateButton) {
			// 跳转到更新下载
			goUpdateDownload(mInfoBean);
		} else if (v == mApplyButton && isRespondClick(v)) {
			if (mInfoBean.getFeaturedId() != 0) {
				if (mInfoBean.isInAppPay()) {
					if (!mDownLoading) {
						mPurchaseManager.handleInAppClick(mInfoBean, (Activity) mContext, 0);
					} else {
						Toast.makeText(mContext, R.string.themestore_downloading, 600).show();
					}
				} else {
					mPurchaseManager.handleNormalFeaturedClickEvent(mContext, mInfoBean, 0);
				}
				PreferencesManager pm = new PreferencesManager(mContext,
						IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
				boolean bool = pm.getBoolean(IPreferencesIds.HAD_SHOW_VIP_TIP, false);
				if (!bool
						&& mInfoBean.getFeeType() != ThemeInfoBean.FEETYPE_FREE
						&& ThemePurchaseManager.getCustomerLevel(mContext) == ThemeConstants.CUSTOMER_LEVEL0) {
					ThemePurchaseManager.getInstance(mContext).savePaidThemePkg(
							mInfoBean.getPackageName());
				}
			} else {
				// 应用
				String type = mInfoBean.getThemeType();
				if (null != type && type.equals(ThemeInfoBean.THEMETYPE_GETJAR)
						&& AppUtils.isAppExist(mContext, mInfoBean.getPackageName())) {
					Intent intent = new Intent();
					int level = ThemePurchaseManager.getCustomerLevel(mContext);
					intent = mContext.getPackageManager().getLaunchIntentForPackage(
							mInfoBean.getPackageName());
					if (level != ThemeConstants.CUSTOMER_LEVEL0) {

						intent.putExtra("viplevel", level);
					}
					mContext.startActivity(intent);
				} else {
					applyTheme(mInfoBean.getPackageName());
				}
				StatisticsData.countThemeTabData(StatisticsData.THEME_TAB_ID_LOCAL_THEME_APPLY);
			}
		}
	}

	/**
	 * 返回
	 * */
	public void goBack() {
		if (mIsInfoView) {
			mIsInfoView = false;
			mThemeDetailScan.setmIsInfoTextView(mIsInfoView);
			initMenuList();
			mThemeDetailScan.removeAllViews();
			mThemeDetailScan.setInfoBean(mInfoBean);
			mIndicator.setVisibility(VISIBLE);
			mIndicator.setTotal(mThemeDetailScan.getScreenCount());
			mIndicator.setCurrent(mThemeDetailScan.getCurrentScreen());

		} else {
			((Activity) mContext).finish();
		}

	}

	/**
	 * 打开菜单
	 * */
	private void openMenu(View v) {

		int width = mContext.getResources().getDimensionPixelSize(R.dimen.theme_detail_menu_width);
		mPopupWindow = new PopupWindow(mListView, width,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, true);
		// 必须设置背景
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		// 设置焦点
		mPopupWindow.setFocusable(true);
		// 设置点击其他地方 就消失
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAsDropDown(v, 0, 0);
		mPopupWindow.update();

	}

	private boolean isRespondClick(View v) {
		float oldTime = mClickTime;
		mClickTime = SystemClock.uptimeMillis();
		if (mClickTime - oldTime > 1000) {
			return true;
		} else {
			// 在1S内快速点击不响应
			return false;
		}
	}

	public void setThemeInfoBean(ThemeInfoBean bean) {
		if (null == bean) {
			return;
		}
		mInfoBean = bean;
		if (null != mThemeDetailScan) {
			mThemeDetailScan.removeAllViews();
			mIndicator.removeAllViews();
			if (mIndicator.getVisibility() != View.VISIBLE) {
				mIndicator.setVisibility(View.VISIBLE);
			}
			mThemePrefSettingManager = new ThemePrefSettingManager(getContext(),
					mInfoBean.getPackageName());
			mIsThemeModifyed = mThemePrefSettingManager.isModifyed();
			mThemeDetailScan.setmIsThemeModifyed(mIsThemeModifyed);
			mThemeDetailScan.setInfoBean(bean);
			mIndicator.setTotal(mThemeDetailScan.getScreenCount());
			mIndicator.setCurrent(mThemeDetailScan.getCurrentScreen());
			mIsInfoView = false;
			TextView textView = (TextView) findViewById(R.id.detail_title).findViewById(
					R.id.detail_text);
			textView.setText(bean.getThemeName());
			initMenuItemText();
			initClickListener();
			initMenuList();
		}
	}

	public void reLayoutView(ThemeInfoBean bean) {
		if (!mIsInfoView) {
			setThemeInfoBean(bean);
		}
	}

	public void setCurrentScreen(int currentScreen) {
		mThemeDetailScan.setCurrentScreen(currentScreen);
	}

	/**
	 * 当未加载完主题信息时，只显示一张背景图和标题
	 * @param themeName
	 */
	public void setImageBackground(String themeName, String packageName) {
		if (null != mThemeDetailScan) {
			mThemeDetailScan.removeAllViews();
			mThemeDetailScan.setOnlyImageBackgroud();
		}
		TextView textView = (TextView) findViewById(R.id.detail_title).findViewById(
				R.id.detail_text);
		textView.setText(themeName);
		setApplyButtonBackground(themeName, packageName);
	}

	/**
	 * 设置应用按钮内容
	 */
	private void setApplyButtonBackground(String themeName, String packageName) {
		// 应用按钮
		mTitleLayout = (RelativeLayout) findViewById(R.id.detail_title);
		boolean isvip = ThemePurchaseManager.getCustomerLevel(mContext) == ThemeConstants.CUSTOMER_LEVEL0
				? false
				: true;
		if (SpaceCalculator.sPortrait) {
			mTitleLayout.findViewById(R.id.theme_detail_apply_btn).setVisibility(GONE);
			mTitleLayout.findViewById(R.id.theme_featured_detail_apply_btn).setVisibility(GONE);
			findViewById(R.id.detail_buttons).setVisibility(View.VISIBLE);
			mApplyButton = findViewById(R.id.detail_buttons).findViewById(R.id.theme_apply);
			mApplyButton.setBackgroundResource(R.drawable.theme_getnow);

			if (isvip || ThemePurchaseManager.queryPurchaseState(mContext, packageName) != null) {
				if (mPurchaseManager.hasDownloaded(themeName, packageName)) {
					((Button) mApplyButton).setBackgroundResource(R.drawable.theme_paid_apply);;
				}
			}
			((Button) mApplyButton).setTextColor(GET_TEXT_COLOR);
			((Button) mApplyButton).setShadowLayer(0, 0, 1, GET_TEXT_SHADOW_COLOR);
			((Button) mApplyButton).setTextSize(GET_TEXT_SIZE);
			((Button) mApplyButton).setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			((Button) mApplyButton).setText("");
			mApplyButton.setClickable(false);
			mApplyButton.setOnClickListener(null);

		} else {
			findViewById(R.id.detail_buttons).setVisibility(GONE);
			//			if(mInfoBean.getFeaturedId() == 0){
			//				mApplyButton = mTitleLayout.findViewById(R.id.theme_featured_detail_apply_btn);
			//				mApplyButton.setBackgroundResource(R.drawable.theme_getnow);
			//				if (isvip || ThemePurchaseManager.queryPurchaseState(mContext, packageName) != null) {
			//					if (mPurchaseManager.hasDownloaded(themeName, packageName)) {
			//						((Button) mApplyButton).setBackgroundResource(R.drawable.theme_paid_apply);;
			//					}
			//				}
			//				((Button) mApplyButton).setText("");
			//				mApplyButton.setVisibility(VISIBLE);
			//				mApplyButton.setClickable(false);
			//				mApplyButton.setOnClickListener(null);
			//			}

		}
	}
	/**
	 * 初始化更多菜单项文字
	 * */
	private void initMenuItemText() {
		mResetMenuItemText = mResources.getString(R.string.theme_detail_menu_reset);
		mDelMenuItemText = mResources.getString(R.string.theme_detail_menu_del);
		mInfoMenuItemText = mResources.getString(R.string.theme_detail_menu_info);
		mPreviewMenuItemText = mResources.getString(R.string.theme_detail_menu_preview);

	}

	/**
	 * 跳转到主题信息界面
	 */
	private void goToThemeInfoView() {
		mIsInfoView = true;
		mThemeDetailScan.setmIsInfoTextView(mIsInfoView);
		initMenuList();

		View mThemeInfoTextView = mInflater.inflate(R.layout.theme_info_text, null);
		TextView infoText = (TextView) mThemeInfoTextView.findViewById(R.id.info_text);
		String info = mInfoBean.getThemeInfo();
		infoText.setText(info);
		mIndicator.setVisibility(GONE);
		mThemeDetailScan.removeAllViews();
		mThemeDetailScan.setScreenNum();
		mThemeDetailScan.addView(mThemeInfoTextView);

	}

	/**
	 * 应用主题
	 */
	private void applyTheme(String pkgName) {
		if (mInfoBean == null) {
			return;
		}
		if (pkgName.equals(ThemeManager.getInstance(mContext).getCurThemePackage())) {
			Toast.makeText(getContext(), R.string.theme_already_using, Toast.LENGTH_SHORT).show();
			return;
		}
		if (!mInfoBean.isNewTheme()) {
			// 不是大主题
			Intent intentGoLauncher = new Intent();
			intentGoLauncher.setClass(getContext(), GoLauncher.class);
			getContext().startActivity(intentGoLauncher);
			Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
			intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING, MyThemeReceiver.CHANGE_THEME);
			intent.putExtra(MyThemeReceiver.PKGNAME_STRING, pkgName);
			getContext().sendBroadcast(intent);
			ThemeDetailActivity.exit();
		} else {
			// 大主题
			initNewThemeResource();
			showDialog(pkgName);
		}
	}

	/**
	 * 初始化大主题需要的一些资源
	 * */
	private void initNewThemeResource() {

		mGolauncherText = mResources.getString(R.string.new_theme_golauncher);
		mGowidgetText = mResources.getString(R.string.new_theme_gowidget);
		mGolockText = mResources.getString(R.string.new_theme_golock);

	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2012-9-13]
	 */
	class MyAdapter extends BaseAdapter {
		private ArrayList<String> mNewThemeTips = null;
		private HashMap<String, Boolean> mCheckBoxState = null;
		private LayoutInflater mInflater;

		public MyAdapter(Context context) {

			this.mInflater = LayoutInflater.from(context);

			mNewThemeTips = new ArrayList<String>();
			mCheckBoxState = new HashMap<String, Boolean>();
			mNewThemeTips.add(mGolauncherText);
			mNewThemeTips.add(mGowidgetText);
			mNewThemeTips.add(mGolockText);

			for (int i = 0; i < mNewThemeTips.size(); i++) {
				// 默认checkbox状态为选中
				mCheckBoxState.put(mNewThemeTips.get(i), true);
			}
		}

		public HashMap<String, Boolean> getmCheckBoxState() {
			return mCheckBoxState;
		}

		public void filterNotExistTheme() {
			if (!mInfoBean.ismExistGolauncher()) {
				mNewThemeTips.remove(mGolauncherText);
				mInfoBean.getNewThemeInfo().getNewThemePkg().remove(GOLAUNCHER_ACTION);
			}
			if (!mInfoBean.ismExistGolock()) {
				mNewThemeTips.remove(mGolockText);
				mInfoBean.getNewThemeInfo().getNewThemePkg().remove(GOLOCK_ACTION);
			}
			if (mInfoBean.getGoWidgetPkgName() == null) {
				mNewThemeTips.remove(mGowidgetText);
				mInfoBean.getNewThemeInfo().getNewThemePkg().remove(GOWIDGET_ACTION);
			}
		}

		@Override
		public int getCount() {
			return mNewThemeTips.size();
		}

		@Override
		public Object getItem(int position) {
			return mNewThemeTips.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = null;
			CheckBox checkBox = null;
			Button downloadButton = null;
			if (convertView != null) {
				textView = (TextView) convertView.findViewById(R.id.new_theme_tip);
				checkBox = (CheckBox) convertView.findViewById(R.id.new_theme_checkbox);
				downloadButton = (Button) convertView.findViewById(R.id.new_theme_download_button);
			} else {
				convertView = mInflater.inflate(R.layout.new_theme_tips_item, null);
				textView = (TextView) convertView.findViewById(R.id.new_theme_tip);
				checkBox = (CheckBox) convertView.findViewById(R.id.new_theme_checkbox);
				downloadButton = (Button) convertView.findViewById(R.id.new_theme_download_button);
			}
			textView.setText(mNewThemeTips.get(position));
			DeskSettingConstants.setTextViewTypeFace(textView);
			DeskSettingConstants.setTextViewTypeFace(downloadButton);
			final int pos = position;
			// final String newPkg =
			// mInfoBean.getNewThemeInfo().getNewThemePkg().get(position);
			final String newPkg = getNewPkg(position);
			if (newPkg != null && !newPkg.trim().equals("")) {
				Intent intent = new Intent(newPkg);
				if (!AppUtils.isAppExist(getContext(), intent)) {
					checkBox.setVisibility(GONE);
					downloadButton.setVisibility(VISIBLE);
				} else {
					checkBox.setVisibility(VISIBLE);
					downloadButton.setVisibility(GONE);
				}
			}

			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mCheckBoxState.put(mNewThemeTips.get(pos), isChecked);
				}
			});

			downloadButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (newPkg != null && !newPkg.trim().equals("")) {
						if (newPkg.trim().equals(GOWIDGET_ACTION)) {
							Intent toGoWidget = new Intent(ICustomAction.ACTION_GOTO_GOWIDGET_FRAME);
							getContext().sendBroadcast(toGoWidget);
							// 退出主题预览
							Intent goLauncherIntent = new Intent(mContext, GoLauncher.class);
							mContext.startActivity(goLauncherIntent);
							ThemeDetailActivity.exit();
						} else if (newPkg.trim().equals(GOLOCK_ACTION)) {
							AppsDetail.gotoDetailDirectly(getContext(),
									AppsDetail.START_TYPE_APPRECOMMENDED, newPkg);
							//							GoStoreOperatorUtil.gotoStoreDetailDirectly(getContext(), newPkg);
						}
					}
					dimissDialog();
				}
			});

			return convertView;
		}

		private String getNewPkg(int position) {
			String newPkg = null;
			try {
				newPkg = mInfoBean.getNewThemeInfo().getNewThemePkg().get(position);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return newPkg;
		}

	}

	private void showDialog(final String pkgName) {

		LayoutInflater factory = LayoutInflater.from(getContext());
		View view = factory.inflate(R.layout.theme_detail_alertdialog, null);

		final MyAdapter myAdapter = new MyAdapter(getContext());
		myAdapter.filterNotExistTheme();

		ListView myListView = (ListView) view.findViewById(R.id.Theme_detail_alertdialog_list);
		myListView.setAdapter(myAdapter);
		myListView.setCacheColorHint(Color.TRANSPARENT);
		myListView.setDivider(null);

		Button button = (Button) view.findViewById(R.id.theme_detail_alertdialog_sure);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// TODO Auto-generated method stub
				Context context = getContext();
				if (mInfoBean != null && context != null) {
					// 存在桌面主题且被选中
					if (mInfoBean.ismExistGolauncher()
							&& myAdapter.getmCheckBoxState().get(mGolauncherText)) {
						Intent it = new Intent();
						it.setClass(context, GoLauncher.class);
						context.startActivity(it);

						Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
						intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING,
								MyThemeReceiver.CHANGE_THEME);
						intent.putExtra(MyThemeReceiver.PKGNAME_STRING, pkgName);
						context.sendBroadcast(intent);
					}
					// 存在widget主题且被选中
					if (mInfoBean.getGoWidgetPkgName() != null
							&& myAdapter.getmCheckBoxState().get(mGowidgetText)) {
						Intent it = new Intent(ICustomAction.ACTION_CHANGE_WIDGETS_THEME);
						it.putExtra(ICustomAction.WIDGET_THEME_KEY, pkgName);
						context.sendBroadcast(it);
					}
					// 存在GO锁屏主题且被选中
					if (mInfoBean.ismExistGolock()
							&& myAdapter.getmCheckBoxState().get(mGolockText)) {
						if (AppUtils.isGoLockerExist(context)) {
							try {
								String newThemePkgName = mInfoBean.getPackageName();
								if (newThemePkgName != null) {
									Intent it = new Intent(
											ICustomAction.ACTION_SEND_TO_GOLOCK_FOR_THEME_DETAIL);
									it.putExtra(NEW_THEME_KEY, newThemePkgName);
									context.sendBroadcast(it);
								}
							} catch (Exception e) {
							}
						} else {

						}
					}
				}
				dimissDialog();
			}
		});

		mDialog = new AlertDialog.Builder(getContext()).create();
		mDialog.show();

		WindowManager.LayoutParams layoutParams = mDialog.getWindow().getAttributes();
		layoutParams.width = android.view.ViewGroup.LayoutParams.FILL_PARENT;
		layoutParams.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

		mDialog.getWindow().setGravity(Gravity.CENTER);
		mDialog.getWindow().setAttributes(layoutParams);

		Window window = mDialog.getWindow();
		window.setBackgroundDrawableResource(R.drawable.theme_detail_menu_bg);
		window.setContentView(view);

	}

	private void dimissDialog() {
		mDialog.dismiss();
	}

	/**
	 * 删除主题
	 * 
	 * @param themePkg
	 * @param curThemePkgName
	 */
	private void deleteTheme(ThemeInfoBean infoBean, String curThemePkgName) {
		if (infoBean.getPackageName() != null && infoBean.getPackageName().equals(curThemePkgName)) {
			uninstallCurrentTheme(infoBean);
		} else {
			gotoUninstall(infoBean);
		}
	}

	/**
	 * 
	 * 跳转到卸载界面
	 */
	private void gotoUninstall(ThemeInfoBean infoBean) {
		String packageName = infoBean.getPackageName();
		if (infoBean.isZipTheme()) {
			ThemeManager.uninstallZipTheme(packageName);
		}
		if (AppUtils.isAppExist(mContext, packageName)) {
			Uri packageURI = Uri.parse("package:" + packageName);
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
			Activity activity = (Activity) getContext();
			activity.startActivity(uninstallIntent);
		} else {
			Context context = GOLauncherApp.getContext();
			if (context != null) {
				ThemeManager.getInstance(context).onBCChange(IDiyMsgIds.EVENT_UNINSTALL_PACKAGE,
						-1, packageName, null);
			}
			Intent intent = new Intent(ICustomAction.ACTION_ZIP_THEME_REMOVED);
			intent.setData(Uri.parse("package://" + packageName));
			ThemeManager.getInstance(mContext.getApplicationContext()).onBCChange(
					IDiyMsgIds.EVENT_UNINSTALL_APP, -1, packageName, null);
			mContext.sendBroadcast(intent);
			((Activity) mContext).finish();
		}

	}
	/**
	 * 删除当前使用的主题
	 * 
	 */
	private void uninstallCurrentTheme(final ThemeInfoBean infoBean) {

		AlertDialog.Builder builder = new Builder(getContext());
		builder.setMessage(R.string.delete_current_theme_hint);
		builder.setTitle(R.string.hint);
		builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				gotoUninstall(infoBean);

			}

		});

		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		builder.create().show();

	}

	/**
	 * 分享主题
	 */
	private void shareTheme() {
		initShareData();
		Intent it = new Intent(Intent.ACTION_SEND);
		it.setType("text/plain");
		List<ResolveInfo> resInfo = getContext().getPackageManager().queryIntentActivities(it, 0);

		String[] sSNSPackageName = { PackageName.FACEBOOK, PackageName.TWITTER,
				PackageName.SINA_WEIBO };

		if (!resInfo.isEmpty()) {

			List<Intent> targetedShareIntents = new ArrayList<Intent>();

			// 事先判断是否有安装fackbook ,twitter 和新浪微博
			for (int i = 0; i < sSNSPackageName.length; i++) {
				Intent intent = null;
				intent = getIntentContent(sSNSPackageName[i]);
				targetedShareIntents.add(intent);
			}

			// 对分享列表里面程序进行过滤
			for (ResolveInfo info : resInfo) {

				ActivityInfo activityInfo = info.activityInfo;

				if (activityInfo.packageName.contains(sSNSPackageName[0])
						|| activityInfo.packageName.contains(sSNSPackageName[1])
						|| activityInfo.packageName.contains(sSNSPackageName[2])) {
					// 如果是这三个包，都不加进去了，因为之前已经加过了，不做操作

				} else {
					Intent shareIntent = getIntentContent(activityInfo.packageName);
					targetedShareIntents.add(shareIntent);

				}
			}

			// 这里面将分享最后一个应用生成chooser
			// ,这时候会在里面，所以就在targetedShareIntents删除最后一个，不然会重复最后一个
			Intent chooserIntent = Intent.createChooser(targetedShareIntents
					.get(targetedShareIntents.size() - 1),
					getContext().getString(R.string.choose_share_way));
			if (chooserIntent == null) {
				return;
			}

			targetedShareIntents.remove(targetedShareIntents.size() - 1);
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
					targetedShareIntents.toArray(new Parcelable[] {}));
			try {
				getContext().startActivity(chooserIntent);
			} catch (ActivityNotFoundException e) {
				// String webString = "http://www.goforandroid.com/go_le.html";
				Uri browserUri = Uri.parse(LauncherEnv.Url.GOLAUNCHER_THEME_SITE_URL);
				if (null != browserUri) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
					browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					AppUtils.safeStartActivity(getContext(), browserIntent);
				}
			} catch (Exception e) {
			}

		}

	}

	@Override
	public void onDismiss() {
		// removeView(mShadowView);
	}

	@Override
	public void updateSliderIndecator(int offset) {

	}

	@Override
	public void updateIndicatorTotal(int total) {
		if (null != mThemeDetailScan && total > 0) {
			mIndicator.setTotal(total);
		}
	}

	@Override
	public void loadFinish() {
		// TODO Auto-generated method stub

	}

	/**
	 * 取消菜单
	 * */
	private void dismissMenu() {
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}

	}

	/**
	 * 判断菜单是否显示
	 * */
	private boolean isMenuShowing() {
		return mPopupWindow != null && mPopupWindow.isShowing();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String viewTag = (String) view.getTag();
		if (mResetMenuItemText.equals(viewTag)) {
			// 恢复默认
			resetThemeSetting();
		} else if (mDelMenuItemText.equals(viewTag)) {
			// 删除
			String curThemePackage = ThemeManager.getInstance(mContext).getCurThemePackage();
			deleteTheme(mInfoBean, curThemePackage);
			StatisticsData.countThemeTabData(StatisticsData.THEME_TAB_ID_LOCAL_THEME_DELETE);
		} else if (mInfoMenuItemText.equals(viewTag)) {
			// 主题信息
			goToThemeInfoView();
			StatisticsData.countThemeTabData(StatisticsData.THEME_TAB_ID_LOCAL_THEME_INFO);
		} else if (mPreviewMenuItemText.equals(viewTag)) {
			gotoImageView();
		}
		if (isMenuShowing()) {
			dismissMenu();
		}
	}

	/**
	 * 恢复对主题的修改
	 * */
	private void resetThemeSetting() {
		mThemePrefSettingManager.resetSetting();
		mThemeDetailScan.removeAllViews();
		mIsThemeModifyed = false;
		mThemeDetailScan.setmIsThemeModifyed(mIsThemeModifyed);
		mThemeDetailScan.setInfoBean(mInfoBean);
		initMenuList();

	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 菜单项适配器
	 * */
	class MenuAdapter extends BaseAdapter {

		private LayoutInflater mLayoutInflater = null;
		private String[] mItems;

		public MenuAdapter(String[] items) {
			this.mItems = items;
			mLayoutInflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			return mItems == null ? 0 : mItems.length;
		}

		@Override
		public Object getItem(int position) {

			return mItems == null ? null : mItems[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView itemTextView = null;
			if (convertView == null) {
				itemTextView = (TextView) mLayoutInflater.inflate(R.layout.theme_detail_menu_item,
						null);
				DeskSettingConstants.setTextViewTypeFace(itemTextView);
			} else {
				itemTextView = (TextView) convertView;
			}
			String itemText = mItems[position];
			itemTextView.setText(itemText);
			itemTextView.setTag(itemText);
			return itemTextView;
		}

	}

	// 赋值Intent对象的分享内容
	public Intent getIntentContent(String packageName) {

		/*
		 * 对短信分享图片进行特殊处理 ，
		 */
		if (packageName.endsWith("com.android.mms")) {

			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
			intent.putExtra("subject", mExtraSubject);
			intent.putExtra("sms_body", mExtraText);

			String bitName = null;
			if (mInfoBean != null) {
				bitName = mInfoBean.getThemeName();
			}
			String fileName = "/sdcard/share_image/" + bitName + ".jpg";
			intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fileName));
			intent.setType("image/jpeg");

			return intent;
		}

		// 分享的文字和图片内容
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.setPackage(packageName);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, mExtraSubject);
		intent.putExtra(android.content.Intent.EXTRA_TEXT, mExtraText); // NOTE
		intent.putExtra(Intent.EXTRA_STREAM, mPicUri);

		return intent;

	}

	/**
	 * 初始化所分享的内容数据
	 * */
	private void initShareData() {
		try {
			mExtraSubject = mResources.getString(R.string.share_title);
			mExtraText = mResources.getString(R.string.share_content_theme1)
					+ mInfoBean.getThemeName()
					+ mResources.getString(R.string.share_content_theme2);

			String bitName = mInfoBean.getThemeName();
			mSharePicPath = "/sdcard/share_image/";
			String fileName = mSharePicPath + bitName + ".jpg";
			File newfile = new File(fileName);

			if (!newfile.exists()) {
				// 如果不存在图片的
				Bitmap bitmap = ImageExplorer.getInstance(mContext).createBitmap(
						mInfoBean.getPackageName(), mInfoBean.getFirstPreViewDrawableName());
				try {
					File file = ImageExplorer.getInstance(mContext).saveMyBitmap(
							mInfoBean.getThemeName(), bitmap);
					mPicUri = Uri.fromFile(file);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					bitmap.recycle();
				}
			} else {
				// 如果已经存在图片的路径的话，就转化为Uri
				mPicUri = Uri.fromFile(newfile);

			}
		} catch (Exception e) {

		}

	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		if (isMenuShowing()) {
			dismissMenu();
		}
		initClickListener();
	}

	public void setIndicatorLayout(int top) {
		// System.out.println(top);
		// top -= 10;
		// if (top > 0 && top != oldTop) {
		// oldTop = top;
		// ((RelativeLayout.LayoutParams)
		// mIndicator.getLayoutParams()).setMargins(0, top, 0, 0);
		// mIndicator.setTotal(mThemeDetailScan.getScreenCount());
		// mIndicator.setCurrent(mThemeDetailScan.getCurrentScreen());
		// }
	}

	private void showUpdateDialog(ThemeInfoBean themeInfoBean) {

		final ThemeInfoBean infoBean = themeInfoBean;
		new AlertDialog.Builder(mContext).setTitle(R.string.theme_pages_update)
				.setMessage(R.string.fav_content).setCancelable(false)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int i) {
						// ftp下载
						GoStoreOperatorUtil.downloadFileDirectly(mContext, infoBean.getThemeName(),
								LauncherEnv.Url.UI_3NEW_THEME_URL, 0, infoBean.getPackageName(),
								null, 0, null);
						mIsDownloading = true;

						Toast.makeText(mContext, mContext.getString(R.string.theme_download_tips),
								Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).show();
	}

	private void goUpdateDownload(ThemeInfoBean InfoBean) {

		// 首先判断是否为200渠道 ，如果是200渠道就跳到电子市场
		if ("200".equals(GoStorePhoneStateUtil.getUid(getContext()))
				|| !Machine.isCnUser(getContext())) { // 国外

			if (AppUtils.isMarketExist(getContext())) {
				// 跳转到market
				AppUtils.gotoMarket(getContext(), LauncherEnv.Market.APP_DETAIL
						+ ThemeManager.DEFAULT_THEME_PACKAGE_3_NEWER);
			} else {
				// DeskToast.makeText(getContext(),
				// R.string.no_googlemarket_tip,
				// Toast.LENGTH_SHORT).show();
				// 跳转到网页版market
				GoStoreOperatorUtil.gotoBrowser(getContext(), LauncherEnv.Market.BROWSER_APP_DETAIL
						+ ThemeManager.DEFAULT_THEME_PACKAGE_3_NEWER
						+ LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK);

			}

		} else { // 国内非200渠道，使用ftp下载
			// 弹出提示内容，提示用户下载
			if (!mIsDownloading) {
				showUpdateDialog(InfoBean);
			} else {
				// 特殊处理， 华为 U8860 在使用FTP下载的时候，点击通知栏的下载项，华为U8860偶尔会出现下载项消失
				// 其实只是显示上消失了，后台数据还在处于暂停状态，用户再次点击检查更新，通知栏的下载项还是之前的进度
				GoStoreOperatorUtil.downloadFileDirectly(mContext, InfoBean.getThemeName(),
						LauncherEnv.Url.UI_3NEW_THEME_URL, 0, InfoBean.getPackageName(), null, 0,
						null);
				Toast.makeText(mContext, mContext.getString(R.string.theme_download_tips),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//			super.handleMessage(msg);
			switch (msg.what) {
				case IDiyMsgIds.THEME_INAPP_PAID_FINISHED :
					if (msg.arg1 == ThemeAppInBillingManager.PURCHASE_STATE_PURCHASED) {
						removeMessages(IDiyMsgIds.THEME_INAPP_PAID_FINISHED);
						mPurchaseManager.startDownload(mInfoBean);
						GuiThemeStatistics.getInstance(mContext).onAppInstalled(
								mInfoBean.getPackageName());
					}
					break;
				case THEME_DOWNLOAD_FINISH :
					if (msg.obj != null && mInfoBean != null) {
						long id = (Long) msg.obj;
						if (id == mInfoBean.getFeaturedId()) {
							initClickListener();
						}
						removeMessages(THEME_DOWNLOAD_FINISH);
					}
					break;
				default :
					break;
			}
		}

	};

	@Override
	public void onBCChange(int msgId, int param, Object object, Object object2) {
		// TODO Auto-generated method stub
		switch (msgId) {
			case IDiyMsgIds.THEME_INAPP_PAID_FINISHED :
				Message msg = Message.obtain();
				msg.what = IDiyMsgIds.THEME_INAPP_PAID_FINISHED;
				msg.arg1 = param;
				msg.obj = object;
				mHandler.sendMessage(msg);
				break;

			default :
				break;
		}
	}

	private void gotoImageView() {
		mIsInfoView = false;
		mThemeDetailScan.setmIsInfoTextView(mIsInfoView);
		initMenuList();
		mThemeDetailScan.removeAllViews();
		mThemeDetailScan.setInfoBean(mInfoBean);
		mIndicator.setVisibility(VISIBLE);
		mIndicator.setTotal(mThemeDetailScan.getScreenCount());
		mIndicator.setCurrent(mThemeDetailScan.getCurrentScreen());
	}

	private void initDownloadReceiver() {
		mDownloadReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (mInfoBean == null) {
					return;
				}
				String action = intent.getAction();
				if (ICustomAction.ACTION_UPDATE_DOWNLOAD_PERCENT.equals(action)) {
					Bundle data = intent.getExtras();
					if (data != null) {
						long appId = data.getInt(AppsThemeDetailActivity.DOWNLOADING_APP_ID);
						if (appId == mInfoBean.getFeaturedId()) {
							mDownLoading = true;
						}
					}
				} else if (ICustomAction.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
					Bundle data = intent.getExtras();
					if (data != null) {
						long appId = data.getInt(AppsThemeDetailActivity.DOWNLOADING_APP_ID);
						if (appId == mInfoBean.getFeaturedId()) {
							mDownLoading = false;
						}
						if (mHandler != null) {
							Message msg = Message.obtain();
							msg.what = THEME_DOWNLOAD_FINISH;
							msg.obj = appId;
							mHandler.sendMessage(msg);
						}
						Intent it = new Intent(ICustomAction.ACTION_NEW_THEME_INSTALLED);
						it.setData(Uri.parse("package://"));
						context.sendBroadcast(it);
					}
				} else if (ICustomAction.ACTION_UPDATE_DOWNLOAD_STOP.equals(action)
						|| ICustomAction.ACTION_UPDATE_DOWNLOAD_FAILED.equals(action)) {
					Bundle data = intent.getExtras();
					if (data != null) {
						long appId = data.getInt(AppsThemeDetailActivity.DOWNLOADING_APP_ID);
						if (appId == mInfoBean.getFeaturedId()) {
							mDownLoading = true;
						}
					}
				}
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ICustomAction.ACTION_DOWNLOAD_COMPLETE);
		intentFilter.addAction(ICustomAction.ACTION_UPDATE_DOWNLOAD_PERCENT);
		intentFilter.addAction(ICustomAction.ACTION_UPDATE_DOWNLOAD_STOP);
		intentFilter.addAction(ICustomAction.ACTION_UPDATE_DOWNLOAD_FAILED);
		mContext.registerReceiver(mDownloadReceiver, intentFilter);
	}
}
