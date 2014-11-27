package com.youle.gamebox.ui.activity;

import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.api.FeedbackApi;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.YouleUtils;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 意见反馈
 * 
 * @author zhaoyl
 * 
 */
public class FeedbackActivity extends Activity implements OnClickListener {

	// @InjectView(R.id.feedback_et)
	private EditText etEditContent; // 意见内容
	// @InjectView(R.id.email_et)
	private EditText etEmail; // 邮箱
	// @InjectView(R.id.submit)
	private Button btnSubmit; // 提交

	private Context mContext;
	private View ivBack; // 返回按钮
	private TextView tvTitle;// 标题

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		mContext = this;
		initUI();

	}

	private void initUI() {
		ivBack =  findViewById(R.id.back);
		tvTitle = (TextView) findViewById(R.id.title);
		etEditContent = (EditText) findViewById(R.id.feedback_et);
		etEmail = (EditText) findViewById(R.id.email_et);
		btnSubmit = (Button) findViewById(R.id.submit);

		tvTitle.setText("意见反馈");
		ivBack.setOnClickListener(this);
		btnSubmit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.submit:
			sendData();
		default:
			break;
		}

	}

	/********************
	 * 发送数据到服务端
	 ********************/
	private void sendData() {
		// TODO Auto-generated method stub
		String contact = etEmail.getText().toString();
		String content = etEditContent.getText().toString();
		if ("".equals(content)) {
			Toast.makeText(mContext, "反馈内容不能为空！", Toast.LENGTH_SHORT).show();
			return;
		}

		if (!TextUtils.isEmpty(contact)) {
			boolean legal = YouleUtils.emailFormat(contact);
			if (!legal) {
				Toast.makeText(mContext, "请输入正确的邮箱！", Toast.LENGTH_SHORT)
						.show();
				return;
			}
		}
		FeedbackApi api = new FeedbackApi();
		api.setContact(contact);
		api.setContent(content);
		api.setSid(new UserInfoCache().getSid());
		ZhidianHttpClient.request(api, new JsonHttpListener(this, "正在发送") {
			
			@Override
			public void onRequestSuccess(String jsonString) {
				super.onRequestSuccess(jsonString);
				etEmail.setText("");
				etEditContent.setText("");
				Toast.makeText(mContext, "发送成功！", Toast.LENGTH_SHORT).show();
			}		
			
		});

	}
}
