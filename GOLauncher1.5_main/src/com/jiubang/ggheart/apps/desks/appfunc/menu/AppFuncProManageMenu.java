package com.jiubang.ggheart.apps.desks.appfunc.menu;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncMainView;
import com.jiubang.ggheart.apps.desks.appfunc.LockList;
import com.jiubang.ggheart.apps.desks.appfunc.XViewFrame;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 正在运行菜单
 * @author yangguanxiang
 *
 */
public class AppFuncProManageMenu extends BaseListMenu {

	/**
	 * 功能表设置信息
	 */
	private FunAppSetting mFunAppSetting;

	private XViewFrame mFrame;

	private AppFuncMainView mMainView;

	// 正在运行页菜单
	public static final int PROMANAGE_MENU_HIDE[] = { R.string.promanage_menuitem_refresh,
			R.string.promanage_menuitem_lock_list, R.string.promanage_menuitem_hide_lock_program, };

	public static final int PROMANAGE_MENU_SHOW[] = { R.string.promanage_menuitem_refresh,
			R.string.promanage_menuitem_lock_list, R.string.promanage_menuitem_show_lock_program };

	public AppFuncProManageMenu(Activity activity) {
		super(activity);
		setItemResources(PROMANAGE_MENU_HIDE);
		mFunAppSetting = GOLauncherApp.getSettingControler().getFunAppSetting(); // 取出功能表设置
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int positon, long arg3) {
		switch (positon) {
			case 0 :
				mFrame.setIsForceLayout(true);
				mFrame.requestLayout();
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.PROMANAGEHOMEICON,
						AppFuncConstants.PRO_MANAGE_REFRESH, null);
				break;
			case 1 : // 锁定列表
				openLockList();
				break;
			case 2 : // 显示/隐藏锁定程序
				if (mFunAppSetting.getShowNeglectApp() == 0) { // 当前不显示锁定程序，则设置显示锁定程序
					mFunAppSetting.setShowNeglectApp(1);
				} else { // 当前显示锁定程序，则设置不显示锁定程序
					mFunAppSetting.setShowNeglectApp(0);
				}
				mFrame.setIsForceLayout(true);
				mFrame.requestLayout();
				break;
			default :
				break;
		}
		mPopupWindow.dismiss();
	}

	/**
	 * 打开锁定列表
	 * */
	private void openLockList() {
		Intent lockIntent = new Intent(mActivity, LockList.class);
		if (lockIntent != null) {
			mActivity.startActivity(lockIntent);
		}

	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void show(View parent, int x, int y, int width, int height) {
		int[] menuTexts;
		// 根据当前锁定程序是否显示使用不同的菜单
		if (mFunAppSetting.getShowNeglectApp() == 0) { // 当前不显示锁定程序
			menuTexts = PROMANAGE_MENU_SHOW;
		} else { // 当前显示锁定程序
			menuTexts = PROMANAGE_MENU_HIDE;
		}
		setItemResources(menuTexts);
		super.show(parent, x, y, width, height);
	}

	@Override
	public void show(View parent) {
		if (mFrame == null) {
			mFrame = (XViewFrame) parent;
		}
		if (mMainView == null) {
			mMainView = ((XViewFrame) parent).getAppFuncMainView();
		}
		mAdapter.setItemPadding(DrawUtils.dip2px(9), 0, 0, 0);
		FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
		if (handler != null) {
			if (GoLauncher.isPortait()) {
				show(parent, 0, handler.isShowActionBar() ? mMainView.getCurrentContent()
						.getHomeComponent().getHeight() : 0,
						(int) (GoLauncher.getScreenWidth() / 2.2), LayoutParams.WRAP_CONTENT);
			} else {
				show(parent, handler.isShowActionBar() ? mMainView.getCurrentContent()
						.getHomeComponent().getWidth() : 0, 0,
						(int) (GoLauncher.getScreenWidth() / 3.5), LayoutParams.WRAP_CONTENT);
			}
		}
	}
}
