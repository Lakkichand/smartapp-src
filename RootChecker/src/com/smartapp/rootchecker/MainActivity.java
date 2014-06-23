package com.smartapp.rootchecker;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	private final int mRootColor = 0xFF00FF00;
	private final int mNoRootColor = 0xFFFF0000;

	private FrameLayout mADContainer;

	/**
	 * 广告条
	 */
	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mADContainer = (FrameLayout) findViewById(R.id.adcontainer);

		// 创建adView。
		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-6335053266754945/2860999319");
		adView.setAdSize(AdSize.BANNER);
		// 在其中添加 adView
		mADContainer.addView(adView);
		// 启动一般性请求。
		AdRequest adRequest = new AdRequest.Builder().build();
		// 在adView中加载广告请求。
		adView.loadAd(adRequest);

		TextView device = (TextView) findViewById(R.id.device);
		TextView version = (TextView) findViewById(R.id.version);

		device.setText(getString(R.string.device) + android.os.Build.MODEL);
		version.setText(getString(R.string.version)
				+ android.os.Build.VERSION.RELEASE);

		final TextView result = (TextView) findViewById(R.id.result);
		result.setText(getString(R.string.tips));

		Button btn = (Button) findViewById(R.id.btn);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final ProgressDialog dialog = new ProgressDialog(
						MainActivity.this);
				dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				dialog.setMessage(getString(R.string.wait));
				dialog.setCancelable(false);
				dialog.show();

				Thread thread = new Thread() {
					@Override
					public void run() {
						boolean root = true;
						try {
							RootShell shell = RootShell.getInstance();
							if (shell != null) {
								root = shell.isRootValid();
							} else {
								root = false;
							}
						} catch (Exception e) {
							e.printStackTrace();
							root = false;
						}
						final boolean root_ = root;
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								dialog.dismiss();
								if (root_) {
									result.setTextColor(mRootColor);
									result.setText("Root");
								} else {
									result.setTextColor(mNoRootColor);
									result.setText("No Root");
								}
							}
						});
					}
				};
				thread.setPriority(Thread.MAX_PRIORITY);
				thread.start();
			}
		});
	}

	@Override
	public void onPause() {
		adView.pause();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		adView.resume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		adView.destroy();
	}

}
