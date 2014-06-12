package com.jiubang.ggheart.apps.appfunc.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.common.component.CommonActionBar;
import com.jiubang.ggheart.apps.appfunc.common.component.CommonProgressBar;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncImageButton.OnClickListener;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.appfunc.model.ProManageAdapter;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.components.DeskToast;

/**
 * 功能表正在运行操作栏
 * @author yangguanxiang
 *
 */
public class ProManageHomeIcon extends CommonActionBar implements OnClickListener, IMsgHandler {
	private static final int HOME_WIDTH_ID = R.dimen.appfunc_home_width;
	private static final int HOME_HEIGTH_ID = R.dimen.appfunc_home_heigth;
	private static final int MEMORY_BAR_HEIGHT_ID = R.dimen.appfunc_memory_bar_height;
	private static final int MSG_SHOW_TOAST = 0;
	private MemoryBar mMemoryBar;
	private AppFuncImageButton mCleanupButton;
	private AppFuncImageButton mMenuButton;
	private ProManageAdapter mAdapter;
	private boolean mShowRefreshToast;
	private Handler mHandler;

	public ProManageHomeIcon(Context context, int tickCount, int x, int y, int width, int height) {
		super(context, tickCount, x, y, width, height, TYPE_SIMPLE, HIDE_TYPE_BOTTOM);
		int buttonWidth = mUtils.getDimensionPixelSize(HOME_WIDTH_ID);
		int buttonHeight = mUtils.getDimensionPixelSize(HOME_HEIGTH_ID);
		int memoryBarHeight = mUtils.getDimensionPixelSize(MEMORY_BAR_HEIGHT_ID);
		mMemoryBar = new MemoryBar(mActivity, tickCount, 0, 0, 0, memoryBarHeight,
				CommonProgressBar.ORIENTATION_HORIZONTAL);
		mCleanupButton = new AppFuncImageButton(mActivity, tickCount, 0, 0, buttonWidth,
				buttonHeight);
		mCleanupButton.setClickListener(this);
		mMenuButton = new AppFuncImageButton(mActivity, tickCount, 0, 0, buttonWidth, buttonHeight);
		mMenuButton.setClickListener(this);
		MemoryManageGroup group = new MemoryManageGroup(context, tickCount, 0, 0, 0, 0);
		addComponent(group, LAYOUT_GRAVITY_LEFT_STRETCH);
		addComponent(mMenuButton, LAYOUT_GRAVITY_RIGHT);
		showDivider(true);
		initHandler();
		// 注册监听器
		DeliverMsgManager.getInstance()
				.registerMsgHandler(AppFuncConstants.PROMANAGEHOMEICON, this);
	}

	public void initResource(String packageName) {
		super.loadResource(packageName);
		Drawable cleanNormal = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeCleanNormal, packageName);
		Drawable cleanLight = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeCleanLight, packageName);

		Drawable menuNormal = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mAllAppDockBean.mHomeMenu, packageName);
		Drawable menuLight = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mAllAppDockBean.mHomeMenuSelected, packageName);
		mCleanupButton.setIcon(cleanNormal);
		mCleanupButton.setIconPressed(cleanLight);
		mMenuButton.setIcon(menuNormal);
		mMenuButton.setIconPressed(menuLight);
		mMemoryBar.initResource(packageName);
	}

	@Override
	public void loadResource(String packageName) {
		initResource(packageName);
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_SHOW_TOAST :
						DeskToast.makeText(mActivity, R.string.pro_manage_refresh_finish,
								Toast.LENGTH_SHORT).show();
						break;
				}
			}
		};
	}

	@Override
	public void onClick(XComponent view) {
		if (view == mCleanupButton) {
			killAllRunningApp();
			refreshMemory(true);
		} else if (view == mMenuButton) {
			DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
					AppFuncConstants.PRO_MANAGE_MENU_HIDE, null);
			DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
					AppFuncConstants.PRO_MANAGE_MENU_SHOW, null);
		}
	}

	private void killAllRunningApp() {
		try {
			mAdapter.teminateCurApps();
		} catch (Throwable t) {
			// do nothing
		}
	}

	public void setAdapter(ProManageAdapter mAdapter) {
		this.mAdapter = mAdapter;
	}

	public void refreshMemory(boolean needAnimation) {
		mMemoryBar.refresh(needAnimation);
	}

	@Override
	public void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.PRO_MANAGE_REFRESH : {
				mShowRefreshToast = true;
				mMemoryBar.refresh(true);
				break;
			}
			case AppFuncConstants.MEMORY_REFRESH_FINISHED : {
				showToast();
				break;
			}
			default :
				break;
		}
	}

	/**
	 * 显示刷新完成的Toast
	 */
	public void showToast() {
		if (mShowRefreshToast) {
			if (mHandler != null) {
				Message msg = new Message();
				msg.what = MSG_SHOW_TOAST;
				mHandler.sendMessage(msg);
			}
			mShowRefreshToast = false;
		}
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		super.layout(left, top, right, bottom);
		if (!AppFuncFrame.getDataHandler().isShowActionBar()) {
			showToast();
		}
	}

	/**
	 * 正在运行操作栏中的内存管理组
	 * @author yangguanxiang
	 *
	 */
	private class MemoryManageGroup extends CommonActionBar {

		public MemoryManageGroup(Context context, int tickCount, int x, int y, int width, int height) {
			super(context, tickCount, x, y, width, height, TYPE_SIMPLE, HIDE_TYPE_BOTTOM);
			addComponent(mMemoryBar, LAYOUT_GRAVITY_LEFT_STRETCH);
			addComponent(mCleanupButton, LAYOUT_GRAVITY_RIGHT);
		}

		@Override
		public void layout(int left, int top, int right, int bottom) {
			int memoryBarHeight = mUtils.getDimensionPixelSize(MEMORY_BAR_HEIGHT_ID);
			if (GoLauncher.isPortait()) {
				mMemoryBar.setOrientation(CommonProgressBar.ORIENTATION_HORIZONTAL);
				mMemoryBar.setSize(mMemoryBar.getWidth(), memoryBarHeight);
			} else {
				mMemoryBar.setOrientation(CommonProgressBar.ORIENTATION_VERTICAL);
				mMemoryBar.setSize(memoryBarHeight, mMemoryBar.getHeight());
			}
			super.layout(left, top, right, bottom);
		}
	}

}
