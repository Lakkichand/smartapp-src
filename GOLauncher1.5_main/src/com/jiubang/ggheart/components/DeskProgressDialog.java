package com.jiubang.ggheart.components;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;

public class DeskProgressDialog extends ProgressDialog implements ISelfObject, TextFontInterface {
	private TextFont mTextFont;
	private ArrayList<TextView> mTextViews = new ArrayList<TextView>();

	public DeskProgressDialog(Context context) {
		super(context);
		selfConstruct();
	}

	public DeskProgressDialog(Context context, int theme) {
		super(context, theme);
		selfConstruct();
	}

	public static ProgressDialog show(Context context, CharSequence title, CharSequence message) {
		return show(context, title, message, false);
	}

	public static ProgressDialog show(Context context, CharSequence title, CharSequence message,
			boolean indeterminate) {
		return show(context, title, message, indeterminate, false, null);
	}

	public static ProgressDialog show(Context context, CharSequence title, CharSequence message,
			boolean indeterminate, boolean cancelable) {
		return show(context, title, message, indeterminate, cancelable, null);
	}

	public static ProgressDialog show(Context context, CharSequence title, CharSequence message,
			boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
		ProgressDialog dialog = new DeskProgressDialog(context);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setIndeterminate(indeterminate);
		dialog.setCancelable(cancelable);
		dialog.setOnCancelListener(cancelListener);
		try {
			dialog.show();
		} catch (Throwable e) {
			// 在大多数情况下，这里是不会错的，错误多数因为内存不足引起空指针
			// 因此在这里做一次内存回收
			OutOfMemoryHandler.handle();
		}
		return dialog;
	}

	@Override
	public void selfConstruct() {

	}

	@Override
	public void selfDestruct() {
		dismiss();

		mTextViews.clear();
		mTextViews = null;

		onUninitTextFont();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mTextViews = new ArrayList<TextView>();
		ViewFinder.findView(getWindow().getDecorView(), mTextViews);

		onInitTextFont();
	}

	// TODO 析构再考虑

	@Override
	public void onInitTextFont() {
		if (null == mTextFont) {
			mTextFont = new TextFont(this);
		}
	}

	@Override
	public void onUninitTextFont() {
		if (null != mTextFont) {
			mTextFont.selfDestruct();
			mTextFont = null;
		}
	}

	@Override
	public void onTextFontChanged(Typeface typeface, int style) {
		int sz = mTextViews.size();
		for (int i = 0; i < sz; i++) {
			TextView textView = mTextViews.get(i);
			if (null == textView) {
				continue;
			}
			textView.setTypeface(typeface, style);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			dismiss();
		}

		return super.onKeyUp(keyCode, event);
	}
}
