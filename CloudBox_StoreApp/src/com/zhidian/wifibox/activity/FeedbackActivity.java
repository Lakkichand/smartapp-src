package com.zhidian.wifibox.activity;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.CheckNetwork;
import com.zhidian.wifibox.util.InfoUtil;

public class FeedbackActivity extends Activity {

	private EditText mContent;

	private EditText mMail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback_);
		init();
		back();
	}

	public void onResume() {
		super.onResume();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("反馈界面");
			MobclickAgent.onResume(this);
		}
	}

	public void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("反馈界面");
			MobclickAgent.onPause(this);
		}
	}

	/**
	 * 返回
	 */
	private void back() {
		findViewById(R.id.leftBtn).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	/**
	 * toast
	 */
	private void showToast(final int resid) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(FeedbackActivity.this, resid, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	private void init() {

		TextView titleText = (TextView) findViewById(R.id.titleText_tv);
		titleText.setText(getString(R.string.title_feedback_main_drawer));
		findViewById(R.id.leftBtn).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mContent = (EditText) findViewById(R.id.feedback_et);
		mMail = (EditText) findViewById(R.id.email_et);
		findViewById(R.id.submit).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String content = mContent.getText().toString().trim();
				if (TextUtils.isEmpty(content)) {
					Toast.makeText(FeedbackActivity.this, R.string.feedbacktip,
							Toast.LENGTH_SHORT).show();
					return;
				}
				// 检查网络是否连接
				if (!CheckNetwork.isConnect(FeedbackActivity.this)) {
					Toast.makeText(FeedbackActivity.this,
							R.string.networknotconnect, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				final String email = mMail.getText().toString().trim();
				if (!TextUtils.isEmpty(email)) {
					boolean legal = AppUtils.emailFormat(email);
					if (!legal) {
						showToast(R.string.email_tip);
						return;
					}
				}
				// 隐藏软键盘
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(findViewById(R.id.submit)
						.getWindowToken(), 0);
				// 清空输入内容
				mContent.setText("");
				mMail.setText("");
				showToast(R.string.sumbit_tip);
				// 上传反馈内容
				new Thread("feedback") {
					public void run() {
						String url = CDataDownloader.getFeedbackUrl();
						RequestParams params = new RequestParams();
						params.put("contact", email);
						params.put("content", content);
						params.put("uuid",
								InfoUtil.getUUID(FeedbackActivity.this));
						params.put("imei",
								InfoUtil.getIMEI(FeedbackActivity.this));
						CDataDownloader.getPostData(url, params,
								new AsyncHttpResponseHandler() {
									@Override
									public void onSuccess(String content) {
										try {
											JSONObject json = new JSONObject(
													content);
											int statusCode = json.optInt(
													"statusCode", -1);
											if (statusCode == 0) {
												showToast(R.string.feedbacksuccess);
												return;
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
										showToast(R.string.feedbackfail);
									}

									@Override
									public void onStart() {
									}

									@Override
									public void onFailure(Throwable error) {
										showToast(R.string.feedbackfail);
									}

									@Override
									public void onFinish() {
									}
								});
					};
				}.start();
			}
		});
	}

}
