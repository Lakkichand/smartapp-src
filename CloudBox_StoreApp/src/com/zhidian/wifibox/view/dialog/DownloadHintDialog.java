package com.zhidian.wifibox.view.dialog;

import com.zhidian.wifibox.R;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

/**
 * 下载提示对话框
 * @author zhaoyl
 *
 */
public class DownloadHintDialog extends Dialog  implements android.view.View.OnClickListener{

	private Button btnGoon; //继续下载
	private Button btnCancle; //取消下载
	private GoonCallBackListener goonListener;
	private CancleCallBackListener canclelListener;
	
	
	public interface GoonCallBackListener{
		void onClick();
	}
	
	public interface CancleCallBackListener{
		void onClick();
	}
	
	public DownloadHintDialog(Context context) {
		super(context,R.style.Dialog);
		setContentView(R.layout.dialog_download_hint);
		btnGoon = (Button) findViewById(R.id.downloadhit_dialog_goon);
		btnGoon.setOnClickListener(this);
		btnCancle = (Button) findViewById(R.id.downloadhit_dialog_cancle);
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
		case R.id.downloadhit_dialog_goon:
			if (goonListener != null) {
				goonListener.onClick();
			}
			dismiss();
			break;
			
		case R.id.downloadhit_dialog_cancle:
			if (canclelListener != null) {
				canclelListener.onClick();
			}
			dismiss();
			break;

		default:
			break;
		}
		
	}

}
