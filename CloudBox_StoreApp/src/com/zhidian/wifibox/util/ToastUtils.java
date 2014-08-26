package com.zhidian.wifibox.util;

import com.ta.TAApplication;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtils {
	private static Handler mHandler = new Handler(Looper.getMainLooper());

	public static void showToast(Context context, String text) {
		showShortToast(context.getApplicationContext(), text);
	}

	public static void showToast(Context context, int strId) {
		showShortToast(context.getApplicationContext(), strId);
	}

	public static void showShortToast(Context context, final String text) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(TAApplication.getApplication(), text,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	public static void showShortToast(Context context, int strId) {
		showShortToast(context, context.getResources().getString(strId));
	}

	public static void showLongToast(Context context, final String text) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(TAApplication.getApplication(), text,
						Toast.LENGTH_LONG).show();
			}
		});
	}

	public static void showLongToast(Context context, int strId) {
		showShortToast(context, context.getResources().getString(strId));
	}

}
