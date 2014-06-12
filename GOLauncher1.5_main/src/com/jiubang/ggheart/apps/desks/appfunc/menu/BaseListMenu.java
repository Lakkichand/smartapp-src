package com.jiubang.ggheart.apps.desks.appfunc.menu;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 菜单基类
 */
public abstract class BaseListMenu extends BaseMenu implements OnItemClickListener, IMsgHandler {

	protected BaseListMenuView mListView;

	protected Drawable mMenuBgV; // 背景(竖屏)
	protected Drawable mMenuBgH; // 背景（横屏）
	protected Drawable mMenuDividerV; // 分割线（竖屏）
	protected Drawable mMenuDividerH; // 分割线（横屏）
	/**
	 * 菜单项被选中后的背景图
	 */
	protected Drawable mItemSelectedBg;
	protected int mTextColor; // 颜色
	protected BaseMenuAdapter mAdapter;
	private boolean mInitialized;

	protected FunAppSetting mFunAppSetting;

	public BaseListMenu(Activity activity) {
		mActivity = activity;
		mThemeCtrl = AppFuncFrame.getThemeController();
		mListView = new BaseListMenuView(mActivity);
		mFunAppSetting = GOLauncherApp.getSettingControler().getFunAppSetting();
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
		mMenuBgV = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuBgV,
				packageName);
		mMenuBgH = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuBgH,
				packageName);
		mMenuDividerV = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuDividerV, packageName);
		mMenuDividerH = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuDividerH, packageName);
		mTextColor = mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuTextColor;
		mItemSelectedBg = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuItemSelected, packageName);

	}

	private void initialize() {
		mAdapter.setTextColor(mTextColor);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		if (mItemSelectedBg != null) {
			mListView.setSelector(mItemSelectedBg);
		}
		mListView.setLayoutParams(layoutParams);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnKeyListener(this);
		mListView.setAlwaysDrawnWithCacheEnabled(true);
		mListView.setSelectionAfterHeaderView();
		mListView.setSmoothScrollbarEnabled(true);
		mInitialized = true;
	}

	public void show(View parent, int x, int y, int width, int height) {
		mListView.clearFocus(); // 防止快速双击菜单残留点击颜色
		if (mPopupWindow != null && isShowing()) {
			dismiss();
		}
		if (!mInitialized) {
			initialize();
		}
		if (GoLauncher.isPortait()) {
			if (mMenuBgV != null) {
				mListView.setBackgroundDrawable(mMenuBgV);
			} else if (mMenuBgH != null) {
				mListView.setBackgroundDrawable(mMenuBgH);
			}

			if (mMenuDividerV != null) {
				mListView.setDivider(mMenuDividerV);
			} else if (mMenuDividerH != null) {
				mListView.setDivider(mMenuDividerH);
			}

			mPopupWindow = new PopupWindow(mListView, width, height, true);
			mListView.setParent(this);
			// mPopupWindow.setAnimationStyle(R.style.AnimationZoom);
			initAnimationStyle(mPopupWindow);
			mPopupWindow.setFocusable(false);
			mPopupWindow.showAtLocation(parent, Gravity.RIGHT | Gravity.BOTTOM, x, y);
			mPopupWindow.setFocusable(true);
			mPopupWindow.update();
		} else {
			if (mMenuBgH != null) {
				mListView.setBackgroundDrawable(mMenuBgH);
			} else if (mMenuBgV != null) {
				mListView.setBackgroundDrawable(mMenuBgV);
			}

			if (mMenuDividerH != null) {
				mListView.setDivider(mMenuDividerH);
			} else if (mMenuDividerV != null) {
				mListView.setDivider(mMenuDividerV);
			}

			mPopupWindow = new PopupWindow(mListView, width, height, true);
			mListView.setParent(this);
			// mPopupWindow.setAnimationStyle(R.style.AnimationZoomH);
			initAnimationStyle(mPopupWindow);
			mPopupWindow.setFocusable(false);
			mPopupWindow.showAtLocation(parent, Gravity.RIGHT | Gravity.TOP, x, y);
			mPopupWindow.setFocusable(true);
			mPopupWindow.update();
		}

	}
	
	/**
	 * 以竖屏的菜单动作方式弹出菜单
	 * @param parent
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void showByVerticalAnimation(View parent, int x, int y, int width, int height) {
		mListView.clearFocus(); // 防止快速双击菜单残留点击颜色
		if (mPopupWindow != null && isShowing()) {
			dismiss();
		}
		if (!mInitialized) {
			initialize();
		}
		if (mMenuBgV != null) {
			mListView.setBackgroundDrawable(mMenuBgV);
		} else if (mMenuBgH != null) {
			mListView.setBackgroundDrawable(mMenuBgH);
		}

		if (mMenuDividerV != null) {
			mListView.setDivider(mMenuDividerV);
		} else if (mMenuDividerH != null) {
			mListView.setDivider(mMenuDividerH);
		}

		mPopupWindow = new PopupWindow(mListView, width, height, true);
		mListView.setParent(this);
		mPopupWindow.setAnimationStyle(R.style.AnimationZoom);
		mPopupWindow.setFocusable(false);
		mPopupWindow.showAtLocation(parent, Gravity.RIGHT | Gravity.BOTTOM, x,
				y);
		mPopupWindow.setFocusable(true);
		mPopupWindow.update();
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

	public void setAdapter(BaseMenuAdapter adapter) {
		mAdapter = adapter;
	}

	public void setItemResources(ArrayList<BaseMenuItemInfo> itemInfos) {
		if (mAdapter == null) {
			mAdapter = new BaseMenuAdapter(mActivity, itemInfos);
		} else {
			mAdapter.setItemList(itemInfos);
			mAdapter.notifyDataSetChanged();
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
		// TODO Auto-generated method stub
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
		mListView = null;
		mFunAppSetting = null;
		// 反注册主题变更事件
		DeliverMsgManager.getInstance().unRegisterDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
	}
}
