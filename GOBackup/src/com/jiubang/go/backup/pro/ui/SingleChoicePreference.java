package com.jiubang.go.backup.pro.ui;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;

/**
 * @author maiyongshen
 *
 */
public class SingleChoicePreference extends ListPreference {
	private View mContentView;
	private CharSequence mExtraText;

	public SingleChoicePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SingleChoicePreference(Context context) {
		this(context, null);
	}

	@Override
	public View getView(View convertView, ViewGroup parent) {
		mContentView = super.getView(convertView, parent);
		return mContentView;
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		TextView extraText = (TextView) view.findViewById(R.id.text);
		if (extraText != null) {
			extraText.setText(mExtraText);
		}
	}

	public void setExtraText(CharSequence text) {
		mExtraText = text;
		notifyChanged();
	}

}
