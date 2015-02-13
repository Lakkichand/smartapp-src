package com.zhidian.wifibox.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhidian.wifibox.R;

/**
 * 下载提示对话框
 * @author zhaoyl
 *
 */
public class DeleteHintDialog extends Dialog  implements android.view.View.OnClickListener{

	private Button btnGoon;
	private Button btnCancle;
	private TextView mTipCount;	// 提示删除的数量
	private GoonCallBackListener goonListener;
	private CancleCallBackListener canclelListener;
	
	
	public interface GoonCallBackListener{
		void onClick();
	}
	
	public interface CancleCallBackListener{
		void onClick();
	}
	
	public DeleteHintDialog(Context context) {
		super(context,R.style.Dialog);
		setContentView(R.layout.dialog_delete_hint);
		btnGoon = (Button) findViewById(R.id.delethit_dialog_goon);
		btnGoon.setOnClickListener(this);
		btnCancle = (Button) findViewById(R.id.delethit_dialog_cancle);
		btnCancle.setOnClickListener(this);
				
	}
	
	public DeleteHintDialog(Context context, String count) {
		super(context,R.style.Dialog);
		setContentView(R.layout.dialog_delete_hint);
		mTipCount = (TextView) findViewById(R.id.hint_count);
		mTipCount.setText(count);
		btnGoon = (Button) findViewById(R.id.delethit_dialog_goon);
		btnGoon.setOnClickListener(this);
		btnCancle = (Button) findViewById(R.id.delethit_dialog_cancle);
		btnCancle.setOnClickListener(this);
		
	}
	
	public void setGoonCallBackListener(GoonCallBackListener listener){
		goonListener = listener;
	}
	
	public void setCancleCallBackListener(CancleCallBackListener listener){
		canclelListener = listener;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.delethit_dialog_goon:
			dismiss();
			if (goonListener != null) {
				goonListener.onClick();
			}
			break;
			
		case R.id.delethit_dialog_cancle:
			dismiss();
			if (canclelListener != null) {
				canclelListener.onClick();
			}
			break;

		default:
			break;
		}
		
	}

}
