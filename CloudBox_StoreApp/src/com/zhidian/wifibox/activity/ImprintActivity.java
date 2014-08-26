package com.zhidian.wifibox.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.ta.TAApplication;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.ModeManager;
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

	public void onResume() {
		super.onResume();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("版本说明");
			MobclickAgent.onResume(this);
		}
	}

	public void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("版本说明");
			MobclickAgent.onPause(this);
		}
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
