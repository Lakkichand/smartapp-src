package com.zhidian.wifibox.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;

/**
 * 存储空间不足提示框
 * 
 * @author xiedezhi
 * 
 */
public class SpaceDialog extends Dialog {

	public static SpaceDialog sDialog;

	static {
		sDialog = new SpaceDialog(TAApplication.getApplication());
		sDialog.getWindow().setType(
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	}

	public SpaceDialog(Context context) {
		super(context, R.style.Dialog);
		setContentView(R.layout.switch_mode_dialog);
		TextView title = (TextView) findViewById(R.id.dialog_title);
		title.setText("系统提示");
		TextView msg = (TextView) findViewById(R.id.dialog_msg);
		msg.setText("您手机存储空间已满，请清理后再继续下载。");
		TextView yes = (TextView) findViewById(R.id.dialog_update);
		yes.setText("确定");
		yes.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
				// 跳转到存储界面
				try {
					Intent intent = new Intent(
							Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					TAApplication.getApplication().startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		TextView no = (TextView) findViewById(R.id.dialog_cancel);
		no.setText("取消");
		no.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

}
