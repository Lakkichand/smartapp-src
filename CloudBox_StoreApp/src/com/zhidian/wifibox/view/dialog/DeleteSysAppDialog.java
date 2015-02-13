package com.zhidian.wifibox.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.zhidian.wifibox.R;

/**
 * 删除系统应用提示框
 * 
 * @author xiedezhi
 * 
 */
public class DeleteSysAppDialog extends Dialog {

	public DeleteSysAppDialog(Context context, final View.OnClickListener sure,
			final View.OnClickListener cancle) {
		super(context, R.style.Dialog);
		setContentView(R.layout.deletesysappdialog);
		findViewById(R.id.delethit_dialog_cancle).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dismiss();
						cancle.onClick(v);
					}
				});
		findViewById(R.id.delethit_dialog_goon).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dismiss();
						sure.onClick(v);
					}
				});
	}

}
