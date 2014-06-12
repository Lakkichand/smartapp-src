package com.jiubang.ggheart.plugin.mediamanagement;

import java.util.ArrayList;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.appfunc.XViewFrame;
import com.jiubang.ggheart.apps.desks.appfunc.menu.BaseListMenu;
import com.jiubang.ggheart.apps.desks.appfunc.menu.BaseMenuItemInfo;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaCommonMenu;

/**
 * 
 * <br>类描述: 资源管理公共菜单
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-23]
 */
public class MediaCommonMenu extends BaseListMenu implements IMediaCommonMenu {

	private OnItemClickListener mListener;

	public MediaCommonMenu(Activity activity) {
		super(activity);
	}

	@Override
	public void show(View parent, int x, int y, int width, int height, String[] itemNames,
			OnItemClickListener listener) {
		ArrayList<BaseMenuItemInfo> itemInfos = new ArrayList<BaseMenuItemInfo>(itemNames.length);
		for (String name : itemNames) {
			BaseMenuItemInfo info = new BaseMenuItemInfo();
			info.mText = name;
			itemInfos.add(info);
		}
		setItemResources(itemInfos);
		mListener = listener;
		mAdapter.setItemPadding(DrawUtils.dip2px(9), 0, 0, 0);
		if (!isShowing()) {
			if (parent instanceof XViewFrame) {
				((XViewFrame) parent).setDisplayedMenu(this);
			}
			show(parent, x, y, width, height);
		}
	}
	
	@Override
	public void showByVerticalAnimation(View parent, int x, int y, int width,
			int height, String[] itemNames, OnItemClickListener listener) {
		ArrayList<BaseMenuItemInfo> itemInfos = new ArrayList<BaseMenuItemInfo>(
				itemNames.length);
		for (String name : itemNames) {
			BaseMenuItemInfo info = new BaseMenuItemInfo();
			info.mText = name;
			itemInfos.add(info);
		}
		setItemResources(itemInfos);
		mListener = listener;
		mAdapter.setItemPadding(DrawUtils.dip2px(9), 0, 0, 0);
		if (!isShowing()) {
			if (parent instanceof XViewFrame) {
				((XViewFrame) parent).setDisplayedMenu(this);
			}
			showByVerticalAnimation(parent, x, y, width, height);
		}
	}
	
	@Override
	public boolean isShowing() {
		return super.isShowing();
	}

	@Override
	public void dismiss() {
		XViewFrame.getInstance().setDisplayedMenu(null);
		super.dismiss();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mListener != null) {
			mListener.onItemClick(parent, view, position, id);
		}
		dismiss();
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (v == mListView && keyCode == KeyEvent.KEYCODE_MENU
				|| keyCode == KeyEvent.KEYCODE_SEARCH) {
			if (isShowing()) {
				dismiss();
				return true;
			}
		}
		return false;
	}

	@Override
	public void show(View parent) {
//		if (!isShowing()) {
//			AppFuncMainView mainView = ((XViewFrame) parent).getAppFuncMainView();
//			mAdapter.setItemPadding(DrawUtils.dip2px(9), 0, 0, 0);
//			XViewFrame.getInstance().setDisplayedMenu(this);
//			FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
//			if (handler != null) {
//				if (GoLauncher.isPortait()) {
//					show(parent, 0, mainView.getCurrentContent().getHomeComponent().getHeight(),
//							(int) (GoLauncher.getScreenWidth() / 2.2), LayoutParams.WRAP_CONTENT);
//				} else {
//					show(parent, mainView.getCurrentContent().getHomeComponent().getWidth(), 0,
//							(int) (GoLauncher.getScreenWidth() / 3.5), LayoutParams.WRAP_CONTENT);
//				}
//			}
//		}
	}

}
