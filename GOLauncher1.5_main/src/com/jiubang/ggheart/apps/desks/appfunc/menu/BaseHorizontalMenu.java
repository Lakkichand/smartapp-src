package com.jiubang.ggheart.apps.desks.appfunc.menu;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.menu.BaseHorizontalMenuView.OnItemClickListener;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public abstract class BaseHorizontalMenu extends BaseMenu
		implements
			OnItemClickListener,
			IMsgHandler {
	protected BaseHorizontalMenuView mContainer;

	protected Drawable mMenuBg;
	protected Drawable mMenuDivider;// 分割线
	protected int mTextColor;// 颜色
	protected BaseMenuAdapter mAdapter;
	private boolean mInitialized;

	public BaseHorizontalMenu(Activity activity) {
		mActivity = activity;
		mThemeCtrl = AppFuncFrame.getThemeController();
		mContainer = new BaseHorizontalMenuView(activity);
		// 注册主题变更事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
		loadThemeResource();
	}

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
		mMenuBg = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuBgV,
				packageName);
		mMenuDivider = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuDividerV, packageName);

		mTextColor = mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuTextColor;
		mContainer.setDivider(mMenuDivider);
	}

	protected void initialize() {
		mAdapter.setItemLayout(R.layout.app_func_switch_horizontal_menu);
		mAdapter.setTextColor(mTextColor);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.FILL_PARENT);
		mContainer.setLayoutParams(layoutParams);
		mContainer.setOnKeyListener(this);
		mContainer.setAlwaysDrawnWithCacheEnabled(true);
		mContainer.setOnItemClickListener(this);
		mInitialized = true;
	}

	public void show(View parent, int x, int y, int width, int height) {
		if (mPopupWindow != null && isShowing()) {
			dismiss();
		}
		if (!mInitialized) {
			initialize();
		}
		mContainer.refreshContent();
		if (GoLauncher.isPortait()) {
			mContainer.setBackgroundDrawable(mMenuBg);
			mPopupWindow = new PopupWindow(mContainer, width, height, true);
			mContainer.setParent(this);
			initAnimationStyle(mPopupWindow);
			mPopupWindow.setFocusable(false);
			mPopupWindow.showAtLocation(parent, Gravity.RIGHT | Gravity.BOTTOM, x, y);
			mPopupWindow.setFocusable(true);
			mPopupWindow.update();
		} else {
			mContainer.setBackgroundDrawable(mMenuBg);
			mPopupWindow = new PopupWindow(mContainer, width, height, true);
			mContainer.setParent(this);
			initAnimationStyle(mPopupWindow);
			mPopupWindow.setFocusable(false);
			mPopupWindow.showAtLocation(parent, Gravity.RIGHT | Gravity.TOP, x, y);
			mPopupWindow.setFocusable(true);
			mPopupWindow.update();
		}

	}

	protected void initAnimationStyle(PopupWindow popupWindow) {
		if (popupWindow != null) {
			if (GoLauncher.isPortait()) {
				popupWindow.setAnimationStyle(R.style.AnimationZoom);
			} else {
				popupWindow.setAnimationStyle(R.style.AnimationZoomH);
			}
		}
	}

	@Override
	public void dismiss() {
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}
	}

	@Override
	public boolean isShowing() {
		if (mPopupWindow != null) {
			return mPopupWindow.isShowing();
		}
		return false;
	}

	public void setItemResources(ArrayList<BaseMenuItemInfo> itemInfos) {
		if (mAdapter == null) {
			mAdapter = new BaseMenuAdapter(mActivity, itemInfos);
			mContainer.setAdapter(mAdapter);
		} else {
			mAdapter.setItemList(itemInfos);
		}
	}

	public void setItemResources(int[] textResIds) {
		int size = textResIds.length;
		ArrayList<BaseMenuItemInfo> itemInfos = new ArrayList<BaseMenuItemInfo>(size);
		for (int resId : textResIds) {
			BaseMenuItemInfo itemInfo = new BaseMenuItemInfo();
			itemInfo.mTextId = resId;
			itemInfos.add(itemInfo);
		}
		setItemResources(itemInfos);
	}

	@Override
	public void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.LOADTHEMERES :
				loadThemeResource();
				mInitialized = false;
				break;

			default :
				break;
		}
	}

	@Override
	public void recyle() {
		mActivity = null;
		mThemeCtrl = null;
		// 反注册主题变更事件
		DeliverMsgManager.getInstance().unRegisterDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
	}
}
