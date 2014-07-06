package com.smartapp.escape_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.smartapp.easyproxyforandroid.R;

public class LearnActivity extends Activity {

	private RelativeLayout mADContainer;
	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.learn);

		// 创建adView。
		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-6335053266754945/7291198911");
		adView.setAdSize(AdSize.BANNER);
		mADContainer = (RelativeLayout) findViewById(R.id.adcontainer);
		mADContainer.addView(adView);
		// 启动一般性请求。
		AdRequest adRequest = new AdRequest.Builder().build();

		// 在adView中加载广告请求。
		adView.loadAd(adRequest);

		findViewById(R.id.mobilenet).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent(Settings.ACTION_APN_SETTINGS);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		findViewById(R.id.wifinet).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
				startActivity(intent);
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
		mADContainer.removeAllViews();
		adView.destroy();
		super.onDestroy();
	}

}
