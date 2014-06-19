package com.smartapp.autostartmanager;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;

public class MainActivity extends Activity {

	private AdView adView;

	private ListView mListView;
	private MainAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 创建adView。
		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-6335053266754945/7051389713");
		adView.setAdSize(AdSize.BANNER);
		FrameLayout layout = (FrameLayout) findViewById(R.id.adcontianer);
		// 在其中添加adView。
		layout.addView(adView);
		// 启动一般性请求。
		AdRequest adRequest = new AdRequest.Builder().build();
		// 在adView中加载广告请求。
		adView.loadAd(adRequest);

		mListView = (ListView) findViewById(R.id.listview);
		mAdapter = new MainAdapter();
		mListView.setAdapter(mAdapter);

		// 获取运行应用列表
		TAApplication.getApplication().doCommand("maincontroller",
				new TARequest(MainController.SCAN_COMMAND, null),
				new TAIResponseListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onSuccess(TAResponse response) {
						List<DataBean> retList = (List<DataBean>) response
								.getData();
						mAdapter.update(retList);
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFailure(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}
				}, true, false);

	}

	@Override
	protected void onResume() {
		adView.resume();
		super.onResume();
	}

	@Override
	public void onPause() {
		adView.pause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		adView.destroy();
		super.onDestroy();
	}
}
