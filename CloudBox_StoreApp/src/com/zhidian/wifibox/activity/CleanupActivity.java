package com.zhidian.wifibox.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.ta.TAApplication;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;

/**
 * 手机清理
 * 
 * @author xiedezhi
 * 
 */
public class CleanupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cleanup);
		TextView tvTitle = (TextView) findViewById(R.id.header_title_text);
		tvTitle.setText(R.string.phone_clear);
		ImageView btnBack = (ImageView) findViewById(R.id.header_title_back);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CleanupActivity.this.finish();
			}
		});

		findViewById(R.id.process_manager).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 进程管理
						Intent intent = new Intent(CleanupActivity.this,
								ProcessManagerActivity.class);
						startActivity(intent);
					}
				});
		findViewById(R.id.cache_cleaner).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 缓存清理
						Intent intent = new Intent(CleanupActivity.this,
								CacheCleanerActivity.class);
						startActivity(intent);
					}
				});
		findViewById(R.id.residualfiles).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 残留文件
						Intent intent = new Intent(CleanupActivity.this,
								TrashScanActivity.class);
						startActivity(intent);
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("手机清理");
			MobclickAgent.onResume(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("手机清理");
			MobclickAgent.onPause(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
