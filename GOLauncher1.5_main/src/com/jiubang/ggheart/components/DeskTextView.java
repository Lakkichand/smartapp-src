package com.jiubang.ggheart.components;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;

public class DeskTextView extends TextView implements ISelfObject, TextFontInterface {
	private TextFont mTextFont;
	private Typeface mTypeface;
	private int mStyle;

	public DeskTextView(Context context) {
		super(context);
		selfConstruct();
	}

	public DeskTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		selfConstruct();

		if (null != DeskResourcesConfiguration.getInstance()) {
			DeskResourcesConfiguration.getInstance().configurationPreference(this, attrs);
		}
	}

	public DeskTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		selfConstruct();

		if (null != DeskResourcesConfiguration.getInstance()) {
			DeskResourcesConfiguration.getInstance().configurationPreference(this, attrs);
		}
	}

	@Override
	public void selfConstruct() {
		onInitTextFont();
	}

	@Override
	public void selfDestruct() {
		onUninitTextFont();
	}

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
		mTypeface = typeface;
		mStyle = style;
		setTypeface(mTypeface, mStyle);
	}

	public void reInitTypeface() {
		setTypeface(mTypeface, mStyle);
	}
}
