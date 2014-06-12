package com.zhidian.wifibox.view.dialog;


import android.app.Dialog;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhidian.wifibox.R;

/**
 * 加载对话框
 * @author zhaoyl
 *
 */
public class LoadingDialog extends Dialog{

	public TextView mTvMessage;
	public ProgressBar mBar;
	public LoadingDialog(Context context) {
		super(context,R.style.Dialog);
		this.setCanceledOnTouchOutside(false);
		setContentView(R.layout.dialog_loading);
		mTvMessage = (TextView) findViewById(R.id.loading_message);
		mBar = (ProgressBar)findViewById(R.id.loading_pb);
	}
	
	public LoadingDialog(Context context, CharSequence message) {
		this(context);
		setMessage(message);
	}

	public void setMessage(CharSequence message) {
		mTvMessage.setText(message);
	}

}
