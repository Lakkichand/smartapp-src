/*
 * 文 件 名:  AppGameBaseListMenu.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-7-23
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.menu;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow;

import com.gau.go.launcherex.R;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-7-23]
 */
public abstract class AppGameBaseListMenu extends AppGameBaseMenu {

	/**
	 * 构建popupwindow的listview
	 */
	protected AppGameMenuListView mListView;
	/**
	 * 构建Menu选项的Adapter
	 */
	protected AppGameMenuAdapter mAdapter;

	/** {@inheritDoc} */

	public AppGameBaseListMenu(Context context) {
		mContext = context;
		initialize(context);
	}

	/**
	 * 功能简述:初始化listview和adapter 功能详细描述: 注意:
	 * 
	 * @param context
	 */
	private void initialize(Context context) {
		// ListView初始化
		mListView = new AppGameMenuListView(context);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mListView.setLayoutParams(layoutParams);
		mListView.setOnKeyListener(this);
		mListView.setBackgroundResource(R.drawable.allapp_menu_bg_vertical);
		mListView.setDivider(mContext.getResources().getDrawable(
				R.drawable.allfunc_allapp_menu_line));
		mListView.setVerticalScrollBarEnabled(false);
		mListView.setHorizontalScrollBarEnabled(false);
		mListView.setAlwaysDrawnWithCacheEnabled(true);
		mListView.setSelectionAfterHeaderView();
		mListView.setSmoothScrollbarEnabled(true);
		// Adapter初始化
		mAdapter = new AppGameMenuAdapter(context);
		mListView.setAdapter(mAdapter);
	}

	public void show(View parent, int x, int y, int width, int height) {
		mListView.clearFocus();
		// 处于显示状态,则隐藏
		if (mPopupWindow != null && isShowing()) {
			dismiss();
		} else {
			mPopupWindow = new PopupWindow(mListView, width, height, true);
			initAnimationStyle(mPopupWindow);
			mListView.setParent(this);
			mPopupWindow.setFocusable(false);
			mPopupWindow.showAtLocation(parent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, x, y);
			mPopupWindow.setFocusable(true);
			mPopupWindow.update();
		}
	}

	/**
	 * 功能简述:设置popupwindow的动画属性 功能详细描述: 注意:
	 * 
	 * @param popupwindow
	 */
	private void initAnimationStyle(PopupWindow popupwindow) {
		if (popupwindow != null) {
			popupwindow.setAnimationStyle(R.style.AppGameMenuAnim);
		}
	}

	/**
	 * 功能简述:设置Menu Adatper的显示数据 功能详细描述: 注意:id 是资源数组 ，添加的字符串信息需在R.string进行声明
	 * 
	 * @param id
	 */
	public void setResourceId(int[] id) {
		if (id != null) {
			mAdapter.setResourceData(id);
		}
		mAdapter.notifyDataSetChanged();
	}

	/** {@inheritDoc} */

	@Override
	public void dismiss() {
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}
	}

	/** {@inheritDoc} */

	@Override
	public boolean isShowing() {
		if (mPopupWindow != null) {
			return mPopupWindow.isShowing();
		}
		return false;
	}

	/** {@inheritDoc} */

	@Override
	public void recyle() {
		mContext = null;
		mListView = null;
	}

	/** {@inheritDoc} */

	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		mListView.setOnItemClickListener(listener);
	}

}
