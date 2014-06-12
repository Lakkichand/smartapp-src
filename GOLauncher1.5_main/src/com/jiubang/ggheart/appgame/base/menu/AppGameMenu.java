/*
 * 文 件 名:  AppGameMenu.java
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
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.gau.go.launcherex.R;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-7-23]
 */
public class AppGameMenu extends AppGameBaseListMenu {

	public AppGameMenu(Context context) {
		super(context);
	}

	/** {@inheritDoc} */

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (v == mListView && keyCode == KeyEvent.KEYCODE_MENU
				&& event.getAction() == KeyEvent.ACTION_UP) {
			if (isShowing()) {
				dismiss();
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc} */

	@Override
	public void show(View parent) {
		if (mAdapter.isDataEmpty()) {
			return;
		}
		int adaptercount = mAdapter.getCount();
		int width = (int) mContext.getResources().getDimension(R.dimen.appgame_menu_width);
		show(parent, 0, 0, width, LayoutParams.WRAP_CONTENT);
	}

}
