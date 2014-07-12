package com.smartapp.smarthosts;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private LinearLayout mFrame;

	private ScrollView mScrollView;

	private ProgressBar mBar;

	private TextView mHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mScrollView = (ScrollView) findViewById(R.id.scrollerx);
		mFrame = (LinearLayout) findViewById(R.id.frame);
		mBar = (ProgressBar) findViewById(R.id.progressbar);
		mHost = (TextView) findViewById(R.id.host);

		new Thread() {
			public void run() {
				String hosts = Util.readAssetsFile(getApplicationContext(),
						"hosts");
				if (hosts != null) {
					List<String> list = Util.readLine(hosts);
					for (String line : list) {
						line = line.trim();
						if (line.startsWith("#") || TextUtils.isEmpty(line)) {
							continue;
						}
						String[] array = line.split("\\s+");
						String ip = array[0].trim();
						final String host = array[1].trim();
						if (host.toLowerCase().equals("localhost")) {
							continue;
						}
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mHost.setText(host);
							}
						});
						String response = Util.connectHTTPS(host, ip);
						if (response != null) {
							final String ret = host + "===>" + ip + "("
									+ response + ")\n";
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									TextView text = new TextView(
											getApplicationContext());
									text.setTextColor(Color.BLACK);
									text.setText(ret);
									mFrame.addView(text);
									mScrollView.fullScroll(View.FOCUS_DOWN);
								}
							});
						}
					}
					runOnUiThread(new Runnable() {
						public void run() {
							mBar.setVisibility(View.INVISIBLE);
						}
					});
				}
			};
		}.start();
	}
}
