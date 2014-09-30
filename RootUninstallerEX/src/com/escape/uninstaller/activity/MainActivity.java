package com.escape.uninstaller.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;

import com.escape.uninstaller.ui.ActionBarFrame;
import com.escape.uninstaller.util.DrawUtil;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.smartapp.rootuninstaller.R;

public class MainActivity extends SlidingFragmentActivity {

	private Handler mHandler = new Handler(Looper.getMainLooper());

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

		final ActionBarFrame frame = (ActionBarFrame) findViewById(R.id.actionbarframe);

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				frame.up();
			}
		}, 2000);

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				frame.down();
			}
		}, 4000);

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
		findViewById(R.id.sort).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 排序
			}
		});
	}

	@Override
	protected void onDestroy() {
		ActionBarFrame frame = (ActionBarFrame) findViewById(R.id.actionbarframe);
		frame.stop();
		super.onDestroy();
	}

}
