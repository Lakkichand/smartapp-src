package com.jiubang.ggheart.components;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;

public class DeskCheckedTextView extends CheckedTextView implements ISelfObject, TextFontInterface {
	private TextFont mTextFont;

	public DeskCheckedTextView(Context context) {
		super(context);
		selfConstruct();
	}

	public DeskCheckedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		selfConstruct();

		if (null != DeskResourcesConfiguration.getInstance()) {
			DeskResourcesConfiguration.getInstance().configurationPreference(this, attrs);
		}
	}

	public DeskCheckedTextView(Context context, AttributeSet attrs, int defStyle) {
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
		setTypeface(typeface, style);
	}
}