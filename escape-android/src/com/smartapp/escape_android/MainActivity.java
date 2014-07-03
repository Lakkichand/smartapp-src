package com.smartapp.escape_android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.smartapp.easyproxyforandroid.R;
import com.smartapp.escape_android.service.ProxyService;
import com.smartapp.escapeandroid.nativeutil.Util;

public class MainActivity extends Activity {

	private RelativeLayout mADContainer;
	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		findViewById(R.id.browser).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent viewIntent = new Intent(
						"android.intent.action.VIEW",
						Uri.parse("http://cdn-w.fapdu.com/FapDu/v/rubateen-masseur-fucking-19-1_m.mp4"));
				startActivity(viewIntent);
			}
		});

		// Uri.parse("http://r6---sn-a8au-p5qs.googlevideo.com/videoplayback?ipbits=0&mt=1386731216&itag=18&app=youtube_mobile&yms=alg01hj-zn4&fexp=934701%2C937502%2C921404%2C932267%2C930102%2C914922%2C929209%2C916623%2C909717%2C936912%2C936910%2C923305%2C936913%2C907231%2C907240%2C3300131%2C3300137%2C3310870&ip=8.35.201.49&ms=au&source=youtube&upn=XFj7uoCWGYs&mv=u&el=watch&dnc=1&ratebypass=yes&sver=3&expire=1386754914&key=yt5&id=cdfbcf3cf73f513b&signature=79222DB2A18FF3395E2CB7A0B00EB856A23AA90E.F6A56491C3A0CB90656829D140E7E82EE8017520&sparams=id%2Cip%2Cipbits%2Citag%2Cratebypass%2Csource%2Cupn%2Cexpire&cpn=SJvq2u050YKCcW3w"));

		// 创建 adView
		adView = new AdView(this, AdSize.BANNER, "a152a8123157992");
		mADContainer = (RelativeLayout) findViewById(R.id.adcontainer);
		mADContainer.addView(adView);
		adView.loadAd(new AdRequest());

		View swtichFrame = findViewById(R.id.switchframe);
		final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
		swtichFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 开启／关闭服务
				checkBox.setChecked(!checkBox.isChecked());
			}
		});

		if (ProxyService.isWorked(this)) {
			checkBox.setChecked(true);
		} else {
			checkBox.setChecked(false);
		}
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					Intent intent = new Intent(MainActivity.this,
							ProxyService.class);
					startService(intent);
				} else {
					Intent intent = new Intent(MainActivity.this,
							ProxyService.class);
					stopService(intent);

				}
			}
		});

		View teachFrame = findViewById(R.id.easytouchframe);
		teachFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 简易教程
				Intent intent = new Intent(MainActivity.this,
						LearnActivity.class);
				startActivity(intent);
			}
		});

		View shareFrame = findViewById(R.id.shareframe);
		shareFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 分享给好友
				String link = "Google Play : https://play.google.com/store/apps/details?id="
						+ getPackageName();
				final String extraText = getString(R.string.sharein) + "  "
						+ getString(R.string.app_name) + "  " + link;
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT,
						getResources().getText(R.string.app_name));
				intent.putExtra(Intent.EXTRA_TEXT, extraText);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(Intent.createChooser(intent, "Share"));
			}
		});

		View reviewFrame = findViewById(R.id.reviewframe);
		reviewFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 评分
				try {
					Uri uri = Uri.parse("market://details?id="
							+ getPackageName());
					Intent it = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(it);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		View feedbackFrame = findViewById(R.id.feedbackframe);
		feedbackFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 意见反馈
				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				String[] receiver = new String[] { "yijiajia1988@gmail.com" };
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						receiver);
				String subject = "App Freezer Feedback";
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						subject);
				String body = "\n\n";
				body += "\nTotalMemSize="
						+ (Util.getTotalInternalMemorySize() / 1024 / 1024)
						+ "MB";
				body += "\nAndroidVersion=" + android.os.Build.VERSION.RELEASE;
				body += "\nBoard=" + android.os.Build.BOARD;
				body += "\nFreeMemSize="
						+ (Util.getAvailableInternalMemorySize() / 1024 / 1024)
						+ "MB";
				body += "\nRom App Heap Size="
						+ Integer.toString((int) (Runtime.getRuntime()
								.maxMemory() / 1024L / 1024L)) + "MB";
				body += "\nROM=" + android.os.Build.DISPLAY;
				body += "\nKernel=" + Util.getLinuxKernel();
				body += "\nwidthPixels="
						+ getResources().getDisplayMetrics().widthPixels;
				body += "\nheightPixels="
						+ getResources().getDisplayMetrics().heightPixels;
				body += "\nDensity="
						+ getResources().getDisplayMetrics().density;
				body += "\ndensityDpi="
						+ getResources().getDisplayMetrics().densityDpi;
				body += "\nPackageName=" + getPackageName();
				body += "\nProduct=" + android.os.Build.PRODUCT;
				body += "\nPhoneModel=" + android.os.Build.MODEL;
				body += "\nDevice=" + android.os.Build.DEVICE + "\n\n";
				body += getString(R.string.feedbackin);
				emailIntent.putExtra(Intent.EXTRA_TEXT, body);
				emailIntent.setType("plain/text");
				try {
					startActivity(emailIntent);
				} catch (Exception e) {
					e.printStackTrace();
				}
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
