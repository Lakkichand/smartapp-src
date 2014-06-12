package com.jiubang.ggheart.components.renamewindow;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.jiubang.ggheart.components.DeskTextView;

public class RenameAdapter extends BaseAdapter {
	private Context mContext;
	private String[] mNames;
	private OnTouchListener mListener;

	public RenameAdapter(Context contexts, String[] names, OnTouchListener listener) {
		mContext = contexts;
		mNames = names;
		mListener = listener;
	}

	@Override
	public int getCount() {
		return (null != mNames) ? mNames.length : 0;
	}

	@Override
	public Object getItem(int position) {
		return (null != mNames) ? mNames[position] : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DeskTextView view = null;
		if (null == convertView) {
			view = new DeskTextView(mContext);
			view.setTextSize(1, 16);
			view.setTextColor(0xff656565);
			view.setGravity(Gravity.CENTER_VERTICAL & Gravity.LEFT);
			view.setSingleLine();
			view.setEllipsize(TruncateAt.MARQUEE);
			view.setHorizontalFadingEdgeEnabled(true);

			view.setOnTouchListener(mListener);

		} else {
			view = (DeskTextView) convertView;
		}
		view.setText((String) getItem(position));

		return view;
	}
}
