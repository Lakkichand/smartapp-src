package com.zhidian.wifibox.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhidian.wifibox.R;

/**
 * 加载对话框
 * 
 * @author zhaoyl
 * 
 */
public class ConfirmDialog extends Dialog {

	private TextView mTvMessage;
	private TextView mTitle;
	public Button mConfirmBtn;
	private android.view.View.OnClickListener mListenerImpl;

	public ConfirmDialog(Context context) {
		super(context, R.style.Dialog);
		setContentView(R.layout.dialog_one_button);
		mTvMessage = (TextView) findViewById(R.id.dialog_message);
		mTitle = (TextView)findViewById(R.id.dialog_title);
		mConfirmBtn = (Button) findViewById(R.id.dialog_confirm);
		mConfirmBtn.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	public ConfirmDialog(Context context, CharSequence message) {
		this(context);
		mTvMessage.setText(message);
	}

	public ConfirmDialog(Context context, CharSequence title,
			CharSequence message) {
		this(context);
		mTitle.setText(title);
		mTvMessage.setText(message);
	}

}
