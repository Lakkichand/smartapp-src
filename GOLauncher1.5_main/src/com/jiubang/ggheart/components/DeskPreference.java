package com.jiubang.ggheart.components;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
import com.jiubang.ggheart.launcher.PreferenceConfigurationHandler;

public class DeskPreference extends Preference implements ISelfObject, TextFontInterface {
	private TextFont mTextFont;
	private ArrayList<TextView> mTextViews = new ArrayList<TextView>();

	public DeskPreference(Context context) {
		super(context);
		selfConstruct();
	}

	public DeskPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		selfConstruct();

		if (null != DeskResourcesConfiguration.getInstance()) {
			DeskResourcesConfiguration.getInstance().configurationPreference(this, attrs);
		}
	}

	public DeskPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		selfConstruct();

		if (null != DeskResourcesConfiguration.getInstance()) {
			DeskResourcesConfiguration.getInstance().configurationPreference(this, attrs);
		}
	}

	@Override
	public void selfConstruct() {

	}

	@Override
	public void selfDestruct() {
		mTextViews.clear();
		mTextViews = null;

		onUninitTextFont();
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		mTextViews.clear();
		ViewFinder.findView(view, mTextViews);

		onUninitTextFont();
		onInitTextFont();

		PreferenceConfigurationHandler.handlePreferenceItem(view);
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
