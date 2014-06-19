package com.jiubang.go.backup.pro.ui;

import android.content.Context;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;

/**
 * 自定义SingleButtonPreference
 *
 * @author WenCan
 */
public class ExtraTextPreference extends Preference {

	private View mWidgetView = null;
	private CharSequence mRightSummary = null;
	private TextView mRightView = null;

	public ExtraTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ExtraTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ExtraTextPreference(Context context) {
		super(context);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mWidgetView = view.findViewById(android.R.id.widget_frame);
		mRightView = (TextView) mWidgetView.findViewById(R.id.right_summary);
		final CharSequence rightSummary = getRightSummary();
		if (!TextUtils.isEmpty(rightSummary)) {
			if (mWidgetView.getVisibility() != View.VISIBLE) {
				mWidgetView.setVisibility(View.VISIBLE);
			}
			if (mRightView != null) {
				mRightView.setText(rightSummary);
			}
		} else {
			if (mWidgetView.getVisibility() != View.GONE) {
				mWidgetView.setVisibility(View.GONE);
			}
		}
	}

	public CharSequence getRightSummary() {
		return mRightSummary;
	}

	public void setRightSummary(CharSequence rightSummary) {
		if (rightSummary == null && mRightSummary != null || rightSummary != null
				&& !rightSummary.equals(mRightSummary)) {
			mRightSummary = rightSummary;
			notifyChanged();
		}
	}
}
