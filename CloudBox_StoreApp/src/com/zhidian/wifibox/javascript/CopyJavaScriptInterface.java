package com.zhidian.wifibox.javascript;

import android.content.Context;
import android.os.Handler;
import android.text.ClipboardManager;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * 复制文本接口，提供给js调用
 * @author zhaoyl
 *
 */
@SuppressWarnings("deprecation")
public class CopyJavaScriptInterface {

	private Context mContext;
	private ClipboardManager cmb;
	private Handler mHandler;

	public CopyJavaScriptInterface(Context context) {
		mContext = context;
		mHandler = new Handler();
		cmb = (ClipboardManager) mContext
				.getSystemService(Context.CLIPBOARD_SERVICE);
	}

	/**
	 * This is not called on the UI thread. Post a runnable to invoke loadUrl on
	 * the UI thread.
	 */
	@JavascriptInterface
	public void copyOnAndroid(final String number) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				cmb.setText(number);
				Toast.makeText(mContext, "成功复制到剪切板", Toast.LENGTH_SHORT).show();

			}
		});
	}
}
