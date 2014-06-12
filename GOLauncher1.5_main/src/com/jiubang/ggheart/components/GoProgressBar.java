package com.jiubang.ggheart.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.gau.go.launcherex.R;

/**
 * 统一桌面的ProgressBar progressbarcolor : ProgressBar颜色 1代表白色 0代表绿色 background
 * 0表示没黑色罩子 1表示有黑色罩子
 * */
public class GoProgressBar extends LinearLayout {

	private int mBackground;
	private int mProgressbarcolor;

	public GoProgressBar(Context context) {
		super(context);
	}

	public GoProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar);
		mProgressbarcolor = a.getInt(R.styleable.ProgressBar_prossbar_color_selete, 0);
		mBackground = a.getInt(R.styleable.ProgressBar_prossbar_background_selete, 0);
		// TODO Auto-generated constructor stub
	}

	public GoProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		if (mBackground == 1) {
			setBackgroundResource(R.drawable.go_progressbar_background);
		}
		ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.go_progressbar);
		progressBar.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		Drawable drawable = null;
		if (mProgressbarcolor == 0) {
			drawable = getContext().getResources().getDrawable(R.drawable.go_progress_green);
		} else {
			drawable = getContext().getResources().getDrawable(R.drawable.go_progress_white);
		}
		progressBar.setIndeterminateDrawable(drawable);
	}
	
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		try {
			super.onRestoreInstanceState(state);
		} catch (Exception e) {
			Log.i("GoProgressBar", "onRestoreInstanceState has exception " + e.getMessage());
		}
	}
	
	@Override
	protected void dispatchRestoreInstanceState(
			SparseArray<Parcelable> container) {
		try {
			super.dispatchRestoreInstanceState(container);
		} catch (Exception e) {
			Log.i("GoProgressBar", "onRestoreInstanceState has exception " + e.getMessage());
		}
	}


}