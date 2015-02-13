package com.zhidian.wifibox.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.ta.TAApplication;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.Setting;

/**
 * 版本说明
 * 
 */
public class ImprintActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imprint_);
		Setting setting = new Setting(this);
		TextView versionTv = (TextView) findViewById(R.id.version_tv);
		TextView dateTv = (TextView) findViewById(R.id.date_tv);
		versionTv.setText("版本信息：  "
				+ InfoUtil.getVersionName(TAApplication.getApplication()));
		dateTv.setText("发布日期： " + setting.getString(Setting.UPDATE_TIME));

		back();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// 页面统计
		StatService.trackBeginPage(this, "版本说明");
		XGPushClickedResult click = XGPushManager.onActivityStarted(this);
		if (click != null) {
			// TODO
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 页面统计
		StatService.trackEndPage(this, "版本说明");
		XGPushManager.onActivityStoped(this);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 必须要调用这句
		setIntent(intent);
	}

	/**
	 * 返回
	 */
	private void back() {
		findViewById(R.id.back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

}
