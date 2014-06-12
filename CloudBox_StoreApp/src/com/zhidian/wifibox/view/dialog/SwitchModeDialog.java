package com.zhidian.wifibox.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;

/**
 * 提示用户切换到普通模式的dialog
 * 
 * @author xiedezhi
 * 
 */
public class SwitchModeDialog extends Dialog {

	private Context mContext;

	public SwitchModeDialog(Context context) {
		super(context, R.style.Dialog);
		mContext = context;
		this.setCancelable(false);
		setContentView(R.layout.switch_mode_dialog);

		findViewById(R.id.dialog_update).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mContext.startActivity(new Intent(
								Settings.ACTION_WIFI_SETTINGS));
					}
				});

		findViewById(R.id.dialog_cancel).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						ModeManager.getInstance().setRapidly(
								!ModeManager.getInstance().isRapidly());
						MainActivity.sendHandler(null,
								IDiyFrameIds.MAINVIEWGROUP,
								IDiyMsgIds.SWITCH_MODE, -1, null, null);
						dismiss();
					}
				});

	}
}
