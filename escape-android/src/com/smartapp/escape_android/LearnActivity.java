package com.smartapp.escape_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.smartapp.easyproxyforandroid.R;

public class LearnActivity extends Activity {

	private RelativeLayout mADContainer;
	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.learn);

		// 创建 adView
		adView = new AdView(this, AdSize.BANNER, "a152a8123157992");
		mADContainer = (RelativeLayout) findViewById(R.id.adcontainer);
		mADContainer.addView(adView);
		adView.loadAd(new AdRequest());

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
	protected void onDestroy() {
		mADContainer.removeAllViews();
		adView.destroy();
		super.onDestroy();
	}

}
