package com.escape.uninstaller.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.escape.uninstaller.util.DrawUtil;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.smartapp.rootuninstaller.ex.R;

public class MainActivity extends SlidingFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		setBehindContentView(R.layout.sliding);
		initUI();

		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidth(DrawUtil.dip2px(this, 0));
		sm.setShadowDrawable(null);
		sm.setBehindOffset(DrawUtil.dip2px(this, 80));
		sm.setFadeEnabled(true);
		sm.setFadeDegree(0.5f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	private void initUI() {
		findViewById(R.id.sliding).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggle();
			}
		});
		findViewById(R.id.search).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 搜索
			}
		});
		findViewById(R.id.share).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 分享
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
