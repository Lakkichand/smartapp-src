package com.jiubang.go.backup.pro.ui;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jiubang.go.backup.ex.R;

/**
 * @author maiyongshen
 *
 */
public class SingleButtonPreference extends Preference {
	private OnClickListener mButtOnClickListener;
	private String mButtonText;
	private boolean mButtonVisible;
	private View mWidgetView;

	public SingleButtonPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SingleButtonPreference(Context context) {
		this(context, null);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mWidgetView = view.findViewById(android.R.id.widget_frame);
		if (mWidgetView != null) {
			mWidgetView.setVisibility(mButtonVisible ? View.VISIBLE : View.GONE);
		}
		Button button = (Button) view.findViewById(R.id.button);
		if (button != null) {
//			button.setText(mButtonText);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mButtOnClickListener != null) {
						mButtOnClickListener.onClick(v);
					}
				}
			});
		}
	}

	public void setButtonVisibility(boolean visible) {
		mButtonVisible = visible;
		notifyChanged();
	}

	public void setOnButtonClickListener(View.OnClickListener listener) {
		mButtOnClickListener = listener;
	}

//	public void setButtonText(String text) {
//		mButtonText = text;
//		notifyChanged();
//	}
}
