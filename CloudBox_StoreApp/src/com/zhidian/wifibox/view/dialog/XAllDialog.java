package com.zhidian.wifibox.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.zhidian.wifibox.R;

/**
 * 极速模式全部界面的说明对话框
 * 
 * @author xiedezhi
 * 
 */
public class XAllDialog extends Dialog {

	public XAllDialog(Context context) {
		super(context, R.style.Dialog);
		setCancelable(true);
		setContentView(R.layout.xalldialog);

		findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				XAllDialog.this.dismiss();
			}
		});
	}

}
