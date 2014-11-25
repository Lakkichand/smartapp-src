package com.escape.uninstaller.activity;

import android.os.Bundle;

import com.escape.uninstaller.ui.MainViewGroup;
import com.escape.uninstaller.util.DrawUtil;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.smartapp.rootuninstaller.R;

public class MainActivity extends SlidingFragmentActivity {

	private MainViewGroup mMainViewGroup;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMainViewGroup = new MainViewGroup(this);
		setContentView(mMainViewGroup);
		setBehindContentView(R.layout.sliding);

		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidth(DrawUtil.dip2px(this, 0));
		sm.setShadowDrawable(null);
		sm.setBehindOffset(DrawUtil.dip2px(this, 80));
		sm.setFadeEnabled(true);
		sm.setFadeDegree(0.5f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

	}

	@Override
	protected void onDestroy() {
		mMainViewGroup.onDestroy();
		super.onDestroy();
	}

}
