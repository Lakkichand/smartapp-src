package com.jiubang.ggheart.components;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;

public class DeskAlertDialog extends AlertDialog implements ISelfObject, TextFontInterface {
	private TextFont mTextFont;
	private ArrayList<TextView> mTextViews = new ArrayList<TextView>();

	public DeskAlertDialog(Context context) {
		super(context);
		selfConstruct();
	}

	public DeskAlertDialog(Context context, int theme) {
		super(context, theme);
		selfConstruct();
	}

	public DeskAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		selfConstruct();
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
}
