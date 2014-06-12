package com.jiubang.ggheart.apps.desks.appfunc.search;

import android.app.Activity;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.menu.BaseListMenu;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;

/**
 * 
 * @author YeJijiong
 * @version 创建时间：2012-10-19 下午2:45:37
 * 功能表搜索菜单类
 */
public class AppFuncSearchMenu extends BaseListMenu {
	private Handler mSeachVIewHandler;
	// 功能表搜索菜单
	public static final int APP_FUNC_SEARCH_MENU[] = { R.string.appfunc_search_clear_history };

	public AppFuncSearchMenu(Activity activity, Handler seachVIewHandler) {
		super(activity);
		setItemResources(APP_FUNC_SEARCH_MENU);
		mSeachVIewHandler = seachVIewHandler;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int positon, long arg3) {
		switch (positon) {
			case 0 :
				mSeachVIewHandler.sendEmptyMessage(AppFuncSearchView.MSG_DELETE_SEARCH_HISTORY);
				break;
			default :
				break;
		}
		mPopupWindow.dismiss();
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public void show(View parent, int x, int y, int width, int height) {
		super.show(parent, x, y, width, height);
	}

	@Override
	public void show(View parent) {
		mAdapter.setItemPadding(DrawUtils.dip2px(9), 0, 0, 0);
		FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
		if (handler != null) {
			AppFuncUtils utils = AppFuncUtils.getInstance(GoLauncher.getContext());
			if (GoLauncher.isPortait()) {
				show(parent, 0, utils.getDimensionPixelSize(R.dimen.appfunc_bottomheight),
						(int) (GoLauncher.getScreenWidth() / 2.2), LayoutParams.WRAP_CONTENT);
			} else {
				show(parent, utils.getDimensionPixelSize(R.dimen.appfunc_bottomheight), 0,
						(int) (GoLauncher.getScreenWidth() / 3.5), LayoutParams.WRAP_CONTENT);
			}
		}
	}
}
