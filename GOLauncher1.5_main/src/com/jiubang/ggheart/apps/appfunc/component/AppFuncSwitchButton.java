package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncImageButton.OnClickListener;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;
import com.jiubang.ggheart.plugin.mediamanagement.inf.ISwitchMenuControler;

/**
 * 
 * 功能选项卡按钮
 */
public class AppFuncSwitchButton extends AppFuncImageButton implements OnClickListener {

	private AppFuncThemeController mThemeController;
	private String mIconId;
	private String mIconPressedId;
	private String mThemePackageName;

	public AppFuncSwitchButton(Activity activity, int i, int j, int k, int l, int i1) {
		super(activity, i, j, k, l, i1);
		mThemeController = AppFuncFrame.getThemeController();
		setClickListener(this);
	}

	public void checkAndloadResource() {
		Resources res = mActivity.getResources();
		Drawable drawable = null;
		String packageName = GOLauncherApp.getThemeManager().getCurThemePackage();
		boolean load = false;
		if (mIconId == null || mIconPressedId == null || mIcon == null || mIconPressed == null) {
			load = true;
		} else {
			switch (AppFuncContentTypes.sType) {
				case AppFuncContentTypes.APP :
					if (!mIconId.equals(mThemeController.getThemeBean().mSwitchButtonBean.mAppIcon)
							|| !mIconPressedId
									.equals(mThemeController.getThemeBean().mSwitchButtonBean.mAppIconLight)) {
						load = true;
					}
					break;
				case AppFuncContentTypes.SEARCH :
					if (!mIconId
							.equals(mThemeController.getThemeBean().mSwitchButtonBean.mSearchIcon)
							|| !mIconPressedId
									.equals(mThemeController.getThemeBean().mSwitchButtonBean.mSearchIconLight)) {
						load = true;
					}
					break;
				default :
					break;
			}
			if (!load) {
				if (packageName == null && mThemePackageName != null) {
					load = true;
				} else if (mThemePackageName == null && packageName != null) {
					load = true;
				} else if (mThemePackageName != null && packageName != null
						&& !mThemePackageName.equals(packageName)) {
					load = true;
				}
			}
		}
		if (load) {
			mThemePackageName = packageName;
			switch (AppFuncContentTypes.sType) {
				case AppFuncContentTypes.APP :
					mIconId = mThemeController.getThemeBean().mSwitchButtonBean.mAppIcon;
					mIconPressedId = mThemeController.getThemeBean().mSwitchButtonBean.mAppIconLight;
					drawable = mThemeController.getDrawable(mIconId, packageName);
					if (drawable == null) {
						drawable = res
								.getDrawable(R.drawable.appfunc_switch_button_app);
					}
					setIcon(drawable);
					drawable = mThemeController.getDrawable(mIconPressedId, packageName);
					if (drawable == null) {
						drawable = res
								.getDrawable(R.drawable.appfunc_switch_button_app_light);
					}
					setIconPressed(drawable);
					break;
				case AppFuncContentTypes.SEARCH :
					mIconId = mThemeController.getThemeBean().mSwitchButtonBean.mSearchIcon;
					mIconPressedId = mThemeController.getThemeBean().mSwitchButtonBean.mSearchIconLight;
					drawable = mThemeController.getDrawable(mIconId, packageName);
					if (drawable == null) {
						drawable = res
								.getDrawable(R.drawable.appfunc_switch_button_search);
					}
					setIcon(drawable);
					drawable = mThemeController.getDrawable(mIconPressedId, packageName);
					if (drawable == null) {
						drawable = res
								.getDrawable(R.drawable.appfunc_switch_button_search_light);
					}
					setIconPressed(drawable);
					break;
				default :
					break;
			}
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		checkAndloadResource();
		super.drawCurrentFrame(canvas);
	}

	@Override
	public void onClick(XComponent view) {
		if (AppFuncFrame.sCurrentState == AppFuncFrame.STATE_NORMAL) {
			showMenu();
		}
	}

	public void showMenu() {
		ISwitchMenuControler controler = MediaPluginFactory.getSwitchMenuControler();
		switch (AppFuncContentTypes.sType) {
			case AppFuncContentTypes.APP :
				controler.popupAppMenu(null);
				break;
			case AppFuncContentTypes.SEARCH :
				controler.popupSearchMenu(null);
				break;
		}
	}
}
