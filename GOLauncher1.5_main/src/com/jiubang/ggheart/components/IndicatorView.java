package com.jiubang.ggheart.components;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;

public class IndicatorView extends LinearLayout {

	private int mSize;
	private int mFocusId;
	private Context mContext;
	private List<ImageView> pointList;
	private int mUnFocusResId = R.drawable.gostore_bannerindicator_point_unfocus;
	private int mFocusResId = R.drawable.gostore_bannerindicator_point_focus;
	private int mMargin = 0;

	public IndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public void setMargin(int margin) {
		mMargin = margin;
	}

	public IndicatorView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public void setSize(int size) {
		mSize = size;
		initView(mSize);
	}

	public void setFocusItem(int item) {
		if (pointList.size() > 0) {
			pointList.get(mFocusId).setBackgroundResource(mUnFocusResId);
			pointList.get(item).setBackgroundResource(mFocusResId);
			mFocusId = item;
		}
	}

	private void initView(int size) {
		if (pointList != null) {
			pointList.clear();
		} else {
			pointList = new ArrayList<ImageView>();
		}
		for (int i = 0; i < size; i++) {
			ImageView view = new ImageView(mContext);
			view.setBackgroundResource(mUnFocusResId);
			pointList.add(view);
		}

		layout();
	}

	private void layout() {
		removeAllViews();
		LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(mMargin, 0, mMargin, 0);
		for (ImageView view : pointList) {
			addView(view, params);
		}
	}

	public void recycle() {
		removeAllViews();
		if (pointList != null) {
			pointList.clear();
			pointList = null;
		}
		mContext = null;
	}
}
