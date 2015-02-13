package com.zhidian.wifibox.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.DoCommentsController;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.CheckNetwork;
import com.zhidian.wifibox.view.dialog.LoadingDialog;

/**
 * 反馈界面
 * 
 * @author zhaoyl
 * 
 */
public class FeedbackActivity extends Activity {

	private EditText mContent;

	private EditText mMail;

	private ClipboardManager cmb;

	private LoadingDialog loadingDialog;// 加载对话框

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback_);
		loadingDialog = new LoadingDialog(this);
		init();
		initUI();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 页面统计
		StatService.trackBeginPage(this, "用户反馈");
		XGPushClickedResult click = XGPushManager.onActivityStarted(this);
		if (click != null) {
			// TODO
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 页面统计
		StatService.trackEndPage(this, "用户反馈");
		XGPushManager.onActivityStoped(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 必须要调用这句
		setIntent(intent);
	}

	private void initUI() {
		TextView tvGroupNum = (TextView) findViewById(R.id.feedback_num);
		String groupNum = tvGroupNum.getText().toString().trim();

		Button btnCopy = (Button) findViewById(R.id.feedback_copy);
		btnCopy.setTag(groupNum);
		cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

		btnCopy.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View arg0) {
				String num = (String) arg0.getTag();
				cmb.setText(num);
				Toast.makeText(FeedbackActivity.this, "成功复制到剪切板",
						Toast.LENGTH_SHORT).show();
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

		TextView titleText = (TextView) findViewById(R.id.title);
		titleText.setText(getString(R.string.title_feedback_main_drawer));
		findViewById(R.id.back).setOnClickListener(new OnClickListener() {

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
				if (TextUtils.isEmpty(email)) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(FeedbackActivity.this,
									"请留下联系方式，以便给您反馈处理结果", Toast.LENGTH_SHORT)
									.show();
						}
					});
					return;
				}
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
				showDialogMessage("宝贵意见提交ing…");
				// showToast(R.string.sumbit_tip);
				// 上传反馈内容
				String[] str = { email, content };
				TAApplication.getApplication().doCommand(
						getString(R.string.docommentscontroller),
						new TARequest(DoCommentsController.SEND_FEEDBACK, str),
						new TAIResponseListener() {

							@Override
							public void onStart() {

							}

							@Override
							public void onSuccess(TAResponse response) {
								int statusCode = (Integer) response.getData();
								if (statusCode == 0) {
									// 清空输入内容
									mContent.setText("");
									mMail.setText("");
									Toast.makeText(FeedbackActivity.this,
											R.string.feedbacksuccess,
											Toast.LENGTH_SHORT).show();

								} else {
									Toast.makeText(FeedbackActivity.this,
											R.string.feedbackfail,
											Toast.LENGTH_SHORT).show();
								}

								closeDialog();

							}

							@Override
							public void onRuning(TAResponse response) {

							}

							@Override
							public void onFailure(TAResponse response) {
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										closeDialog();
										showToast(R.string.feedbackfail);
									}
								});
							}

							@Override
							public void onFinish() {

							}

						}, true, false);
			}
		});
	}

	/***************************
	 * 对话框
	 ***************************/

	private void showDialogMessage(CharSequence message) {
		loadingDialog.setMessage(message);
		if (!loadingDialog.isShowing()) {
			loadingDialog.show();
		}
	}

	private void closeDialog() {
		try {
			if (loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
		} catch (Exception e) {
		}
	}

}
