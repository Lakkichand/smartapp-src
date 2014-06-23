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

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

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

		// 创建 adView
		adView = new AdView(this, AdSize.BANNER, "a15294415d895c7");

		// 查找 LinearLayout，假设其已获得
		// 属性 android:id="@+id/mainLayout"
		LinearLayout layout = (LinearLayout) findViewById(R.id.contianer);

		// 在其中添加 adView
		layout.addView(adView);

		// 启动一般性请求并在其中加载广告
		adView.loadAd(new AdRequest());

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
	public void onDestroy() {
		adView.destroy();
		super.onDestroy();
	}
}
