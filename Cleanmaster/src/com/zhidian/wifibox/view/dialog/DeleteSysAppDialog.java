package com.zhidian.wifibox.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.smartapp.ex.cleanmaster.R;

/**
 * 删除系统应用提示框
 * 
 * @author xiedezhi
 * 
 */
public class DeleteSysAppDialog extends Dialog {

	public DeleteSysAppDialog(Context context, final View.OnClickListener sure,
			final View.OnClickListener cancle, String filename) {
		super(context, R.style.Dialog);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		setContentView(R.layout.deletesysappdialog);
		TextView tfilename = (TextView) findViewById(R.id.filename);
		tfilename.setText(filename);
		if (TextUtils.isEmpty(filename)) {
			tfilename.setVisibility(View.GONE);
			findViewById(R.id.gap).setVisibility(View.VISIBLE);
		} else {
			tfilename.setVisibility(View.VISIBLE);
			findViewById(R.id.gap).setVisibility(View.GONE);
		}
		findViewById(R.id.delethit_dialog_cancle).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dismiss();
						if (cancle != null) {
							cancle.onClick(v);
						}
					}
				});
		findViewById(R.id.delethit_dialog_goon).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dismiss();
						if (sure != null) {
							sure.onClick(v);
						}
					}
				});
	}

}
