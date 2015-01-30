package com.zhidian.wifibox.view.dialog;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import com.zhidian.wifibox.R;

/**
 * 等待加载框
 * 
 * @author shihuajian
 *
 */
public class WaitingDialog extends Dialog{

	public ProgressBar mBar;
	
	public WaitingDialog(Context context) {
		super(context, R.style.Dialog);
		this.setCanceledOnTouchOutside(false);
		setContentView(R.layout.image_loading);
		mBar = (ProgressBar)findViewById(R.id.loading_pb);
		
		setCanceledOnTouchOutside(false);
		setCancelable(true);
	}
	
	/**
	 * 实现延迟关闭
	 */
	public void close() {
		Runnable run = new Runnable() {
			
			@Override
			public void run() {
				dismiss();
			}
		};
		new Handler(Looper.getMainLooper()).postDelayed(run, 1000);
	}

}
