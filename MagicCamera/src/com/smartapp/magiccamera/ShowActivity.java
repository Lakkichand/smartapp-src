package com.smartapp.magiccamera;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class ShowActivity extends Activity {

	public static final String IMAGE_KEY = "IMAGE_KEY";

	private ImageView mImage;

	private ImageView mShare;

	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.show);

		// 创建adView。
		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-6335053266754945/5814465712");
		adView.setAdSize(AdSize.BANNER);

		// 查找 LinearLayout，假设其已获得
		// 属性 android:id="@+id/mainLayout"
		LinearLayout layout = (LinearLayout) findViewById(R.id.contianer);

		// 在其中添加 adView
		layout.addView(adView);

		// 启动一般性请求。
		AdRequest adRequest = new AdRequest.Builder().build();

		// 在adView中加载广告请求。
		adView.loadAd(adRequest);

		mImage = (ImageView) findViewById(R.id.image);
		mShare = (ImageView) findViewById(R.id.share);

		final String fileName = getIntent().getStringExtra(IMAGE_KEY);

		Bitmap bmp = BitmapFactory.decodeFile(fileName);

		mImage.setImageBitmap(bmp);

		mShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_STREAM,
						Uri.fromFile(new File(fileName)));
				shareIntent.setType("image/*");
				shareIntent
						.putExtra(
								Intent.EXTRA_TEXT,
								getString(R.string.app_name)
										+ "  https://play.google.com/store/apps/details?id="
										+ getPackageName());
				// 系统默认标题
				startActivity(shareIntent);
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
	public void onDestroy() {
		adView.destroy();
		super.onDestroy();
	}
}
