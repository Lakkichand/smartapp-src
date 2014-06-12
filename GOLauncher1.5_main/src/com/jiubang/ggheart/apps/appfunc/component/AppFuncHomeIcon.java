package com.jiubang.ggheart.apps.appfunc.component;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.common.component.CommonActionBar;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncImageButton.OnClickListener;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;

/**
 * 功能表所有程序Tab对应内容
 * @author yangguanxiang
 *
 */
public class AppFuncHomeIcon extends CommonActionBar implements OnClickListener, IMsgHandler {

	private static final int HOME_WIDTH_ID = R.dimen.appfunc_home_width;
	private static final int HOME_HEIGTH_ID = R.dimen.appfunc_home_heigth;

	private AppFuncSwitchButton mSwitchButton;
	private AppFuncHomeButton mHomeButton;
	private AppFuncImageButton mMenuButton;
	private AppFuncImageButton mSearchButton;
	private boolean mIsRepaint = false;

	public AppFuncHomeIcon(Context context, int tickCount, int x, int y, int width, int height) {
		super(context, tickCount, x, y, width, height, HIDE_TYPE_BOTTOM);
		init(tickCount);
		DeliverMsgManager.getInstance().registerMsgHandler(AppFuncConstants.APP_FUNC_HOME_ICON, this);
	}

	private void init(int tickCount) {
		int buttonWidth = mUtils.getDimensionPixelSize(HOME_WIDTH_ID);
		int buttonHeight = mUtils.getDimensionPixelSize(HOME_HEIGTH_ID);
		mSwitchButton = new AppFuncSwitchButton(mActivity, tickCount, 0, 0, buttonWidth,
				buttonHeight);
		mHomeButton = new AppFuncHomeButton(mActivity, tickCount, 0, 0, buttonWidth, buttonHeight);
		mMenuButton = new AppFuncImageButton(mActivity, tickCount, 0, 0, buttonWidth, buttonHeight);
		mMenuButton.setClickListener(this);
		mSearchButton = new AppFuncImageButton(mActivity, tickCount, 0, 0, buttonWidth, buttonHeight);
		mSearchButton.setClickListener(this);
	}

	@Override
	public void resetResource() {
		super.resetResource();
		mSwitchButton.setIcon(null);
		mSwitchButton.setIconPressed(null);
		mHomeButton.setIcon(null);
		mHomeButton.setIconPressed(null);
		mMenuButton.setIcon(null);
		mMenuButton.setIconPressed(null);
		mSearchButton.setIcon(null);
		mSearchButton.setIconPressed(null);
	}

	@Override
	public void loadResource(String packageName) {
		super.loadResource(packageName);
		Drawable dra = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mHomeBean.mHomeUnSelected,
				packageName);
		if (dra != null) {
			mHomeButton.setIcon(dra);
		}
		dra = mThemeCtrl
				.getDrawable(mThemeCtrl.getThemeBean().mHomeBean.mHomeSelected, packageName);
		if (dra != null) {
			mHomeButton.setIconPressed(dra);
		}

		dra = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mAllAppDockBean.mHomeMenu,
				packageName);
		if (dra != null) {
			mMenuButton.setIcon(dra);
		}
		dra = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mAllAppDockBean.mHomeMenuSelected,
				packageName);
		if (dra != null) {
			mMenuButton.setIconPressed(dra);
		}
		mSwitchButton.checkAndloadResource();
		
		dra = mThemeCtrl
				.getDrawable(mThemeCtrl.getThemeBean().mSwitchButtonBean.mSearchIcon);
		if (dra != null) {
			mSearchButton.setIcon(dra);
		}
		dra = mThemeCtrl
				.getDrawable(mThemeCtrl.getThemeBean().mSwitchButtonBean.mSearchIconLight);
		if (dra != null) {
			mSearchButton.setIconPressed(dra);
		}
	}

	@Override
	public void onClick(XComponent view) {
		if (AppFuncFrame.sCurrentState != AppFuncFrame.STATE_NORMAL) {
			return;
		}
		if (view == mMenuButton) {
			DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
					AppFuncConstants.ALL_APP_MENU_HIDE, null);

			DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
					AppFuncConstants.ALL_APP_MENU_SHOW, null);
		} else if (view == mSearchButton) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.APPFUNC_SEARCH_FRAME, null, null);
		}
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		boolean showHomeOnly = AppFuncFrame.getDataHandler().isShowHomeKeyOnly();
		if (showHomeOnly) {
			setType(TYPE_AVERAGE_ALL);
			removeAllComponent();
			addComponent(mHomeButton);
		} else {
			setType(TYPE_AVERAGE_INSIDE);
			removeAllComponent();
			if (MediaPluginFactory.isMediaPluginExist(mContext)) {
				addComponent(mSwitchButton);
			} else {
				addComponent(mSearchButton);
			}
			addComponent(mHomeButton);
			addComponent(mMenuButton);
		}
		super.layout(left, top, right, bottom);
	}

	@Override
	public void notify(int key, Object obj) {
		if (key == AppFuncConstants.MEDIA_PLUGIN_CHANGE) {
			requestLayout();
			invalidate();
		}
	}
	
	@Override
	protected boolean animate() {
		boolean ret = false;
		if (mIsRepaint) {
			mIsRepaint = false;
			ret = true;
		}
		return super.animate() || ret;
	}

	@Override
	public void invalidate() {
		mIsRepaint = true;
	}
}
