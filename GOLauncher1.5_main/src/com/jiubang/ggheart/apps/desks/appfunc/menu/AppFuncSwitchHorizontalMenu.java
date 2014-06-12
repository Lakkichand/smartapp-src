package com.jiubang.ggheart.apps.desks.appfunc.menu;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.appfunc.XViewFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;
import com.jiubang.ggheart.plugin.mediamanagement.inf.OnSwitchMenuItemClickListener;

/**
 * 
 * 选项卡横屏菜单
 */
public class AppFuncSwitchHorizontalMenu extends BaseHorizontalMenu {

	private OnSwitchMenuItemClickListener mListener;

	public AppFuncSwitchHorizontalMenu(Activity activity) {
		super(activity);
	}

	@Override
	public void onItemClick(int position) {
		AppFuncSwitchMenuItemInfo itemInfo = (AppFuncSwitchMenuItemInfo) mAdapter.getItem(position);
		if (itemInfo != null) {
			if (mListener != null) {
				mListener.preMenuItemClick(itemInfo.mActionId);
			}
			switch (itemInfo.mActionId) {
				case AppFuncSwitchMenuItemInfo.ACTION_GO_TO_APP :
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
							new Object[] { AppFuncContentTypes.APP });
					break;
				case AppFuncSwitchMenuItemInfo.ACTION_GO_TO_IMAGE :
					StatisticsData.countMenuData(mActivity, StatisticsData.FUNTAB_KEY_IMAGE);
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
							new Object[] { AppFuncContentTypes.IMAGE });
					break;
				case AppFuncSwitchMenuItemInfo.ACTION_GO_TO_AUDIO :
					StatisticsData.countMenuData(mActivity, StatisticsData.FUNTAB_KEY_AUDIO);

					//					int type = GOLauncherApp.getSettingControler().getFunAppSetting()
					//							.getMusicDefaulOpenType();
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
							new Object[] { AppFuncContentTypes.MUSIC });

					break;
				case AppFuncSwitchMenuItemInfo.ACTION_GO_TO_VIDEO :
					StatisticsData.countMenuData(mActivity, StatisticsData.FUNTAB_KEY_VIDEO);
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
							new Object[] { AppFuncContentTypes.VIDEO });
					break;
				case AppFuncSwitchMenuItemInfo.ACTION_GO_TO_SEARCH :
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
							new Object[] { AppFuncContentTypes.SEARCH });
					break;
				default :
					break;
			}
			if (mListener != null) {
				mListener.postMenuItemClick(itemInfo.mActionId);
			}
		}
		mPopupWindow.dismiss();
	}

	@Override
	public void show(View parent) {
		if (!isShowing()) {
			XViewFrame.getInstance().setDisplayedMenu(this);
			int offset = AppFuncUtils.getInstance(mActivity).getStandardSize(10);
			show(parent, AppFuncUtils.getInstance(GOLauncherApp.getContext())
					.getDimensionPixelSize(R.dimen.appfunc_bottomheight)
					- offset, GoLauncher.getDisplayHeight(),
					LayoutParams.WRAP_CONTENT,
					AppFuncUtils.getInstance(mActivity).getStandardSize(100));
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
		mAdapter.setItemPadding(0, DrawUtils.dip2px(4), 0, 0);
		mAdapter.setItemTextSize(0);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SEARCH) {
			if (isShowing()) {
				dismiss();
				return true;
			}
		}
		return false;
	}

	@Override
	public void dismiss() {
		XViewFrame.getInstance().setDisplayedMenu(null);
		super.dismiss();
	}

	@Override
	protected void loadThemeResource() {
		String curPackageName = ThemeManager.getInstance(mActivity).getCurThemePackage();
		String packageName = null;
		if (!curPackageName.equals(GOLauncherApp.getSettingControler().getFunAppSetting()
				.getTabHomeBgSetting())) {
			packageName = GOLauncherApp.getSettingControler().getFunAppSetting()
					.getTabHomeBgSetting();
		}
		if (!AppUtils.isAppExist(mActivity, packageName)) {
			packageName = GOLauncherApp.getThemeManager().getCurThemePackage();
		}

		mMenuBg = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mSwitchMenuBean.mMenuBgH,
				packageName);
		if (mMenuBg == null) {
			mMenuBg = mActivity.getResources().getDrawable(R.drawable.appfunc_switch_menu_bg_h);
		}
		mMenuDivider = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mSwitchMenuBean.mMenuDividerH, packageName);
		if (mMenuDivider == null) {
			mMenuDivider = mActivity.getResources()
					.getDrawable(R.drawable.allfunc_allapp_menu_line);
		}
		mTextColor = mThemeCtrl.getThemeBean().mSwitchMenuBean.mMenuTextColor;
		if (mTextColor == 0) {
			mTextColor = 0xff000000;
		}
		mContainer.setDivider(mMenuDivider);
	}

	@Override
	protected void initAnimationStyle(PopupWindow popupWindow) {
		if (popupWindow != null) {
			if (GoLauncher.isPortait()) {
				popupWindow.setAnimationStyle(R.style.SwtichMenuAnimationZoom);
			} else {
				popupWindow.setAnimationStyle(R.style.SwtichMenuAnimationZoomH);
			}
		}
	}

	public void setOnSwitchMenuItemClickListener(OnSwitchMenuItemClickListener listener) {
		mListener = listener;
	}
}
