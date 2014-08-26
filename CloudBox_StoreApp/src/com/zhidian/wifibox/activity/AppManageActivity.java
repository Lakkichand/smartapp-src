package com.zhidian.wifibox.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ta.TAApplication;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;

/**
 * 软件管理Activity
 * 
 * @author zhaoyl
 * 
 */
public class AppManageActivity extends Activity implements OnClickListener {

	private Context mContext;
	private static final String TAG = AppManageActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_appmanage);
		initUI();
	}

	private void initUI() {
		TextView tvTitle = (TextView) findViewById(R.id.header_title_text);
		tvTitle.setText(R.string.app_manage);
		ImageView btnBack = (ImageView) findViewById(R.id.header_title_back);
		btnBack.setOnClickListener(this);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.appmanage_update);
		layout.setOnClickListener(this);
		layout = (RelativeLayout) findViewById(R.id.appmanage_remove);
		layout.setOnClickListener(this);
		layout = (RelativeLayout) findViewById(R.id.appmanage_move);
		layout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_title_back:
			onBackPressed();
			break;
		case R.id.appmanage_update:// 软件更新
			gotoUpdate();
			break;

		case R.id.appmanage_remove:// 软件卸载
			gotoRemove();
			break;

		case R.id.appmanage_move:// 软件搬家
			gotoMove();
			break;

		default:
			break;
		}

	}

	private void gotoMove() {
		// TODO Auto-generated method stub

	}

	private void gotoRemove() {
		Intent intent = new Intent();
		intent.setClass(mContext, AppRemoveActivity.class);
		startActivity(intent);

	}

	private void gotoUpdate() {
		Intent intent = new Intent();
		intent.setClass(mContext, AppUpdateActivity.class);
		startActivity(intent);

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("软件管理");
			MobclickAgent.onResume(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("软件管理");
			MobclickAgent.onPause(this);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent i = new Intent(AppManageActivity.this, MainActivity.class);
		startActivity(i);
	}
}
