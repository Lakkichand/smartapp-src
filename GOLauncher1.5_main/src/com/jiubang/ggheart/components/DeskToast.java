package com.jiubang.ggheart.components;

import android.content.Context;
import android.content.res.Resources;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class DeskToast {
	public static Toast makeText(Context context, CharSequence text, int duration) {
		Toast ret = Toast.makeText(context, text, duration);
		TextView view = (TextView) ret.getView().findViewById(android.R.id.message);
		LayoutParams lp = view.getLayoutParams();
		((ViewGroup) ret.getView()).removeView(view);
		DeskTextView deskTextView = new DeskTextView(context);
		((ViewGroup) ret.getView()).addView(deskTextView, lp);
		deskTextView.setText(text);
		return ret;
	}

	public static Toast makeText(Context context, int resId, int duration)
			throws Resources.NotFoundException {
		Toast ret = Toast.makeText(context, resId, duration);
		TextView view = (TextView) ret.getView().findViewById(android.R.id.message);
		LayoutParams lp = view.getLayoutParams();
		((ViewGroup) ret.getView()).removeView(view);
		DeskTextView deskTextView = new DeskTextView(context);
		((ViewGroup) ret.getView()).addView(deskTextView, lp);
		deskTextView.setText(resId);
		return ret;
	}
}
