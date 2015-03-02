package com.zhidian.wifibox.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.smartapp.ex.cleanmaster.R;

public class RamAppDialog extends Dialog {

	public RamAppDialog(Context context, final View.OnClickListener sure,
			final View.OnClickListener cancle, String title, String msg) {
		super(context, R.style.Dialog);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		setContentView(R.layout.deletesysappdialog);
		TextView tfilename = (TextView) findViewById(R.id.filename);
		tfilename.setVisibility(View.GONE);
		findViewById(R.id.gap).setVisibility(View.VISIBLE);
		TextView msgTx = (TextView) findViewById(R.id.hint_count);
		msgTx.setText(msg);
		TextView titleTx = (TextView) findViewById(R.id.title);
		titleTx.setText(title);
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
