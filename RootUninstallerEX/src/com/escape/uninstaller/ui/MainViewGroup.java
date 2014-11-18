package com.escape.uninstaller.ui;

import android.content.Context;
import android.widget.FrameLayout;

public class MainViewGroup extends FrameLayout {

	private TabManageView mTabManageView;

	public MainViewGroup(Context context) {
		super(context);
		init();
	}

	private void init() {
		mTabManageView = new TabManageView(getContext());
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		addView(mTabManageView, lp);
	}

	public void onDestroy() {
		mTabManageView.onDestroy();
	}

}
