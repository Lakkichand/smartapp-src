package com.smartapp.colorrun;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

	private FrameLayout mFrame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFrame = new FrameLayout(getApplicationContext());
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);

		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		RunView view = new RunView(getApplicationContext(),
				mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);
		mFrame.addView(view, lp);
		setContentView(mFrame);
	}

	@Override
	protected void onDestroy() {
		mFrame.removeAllViews();
		super.onDestroy();
	}

}
