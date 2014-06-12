package com.jiubang.ggheart.apps.desks.appfunc.menu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.go.util.graphics.DrawUtils;

public class BaseHorizontalMenuView extends LinearLayout implements OnClickListener {
	private BaseMenu mParent;
	private BaseMenuAdapter mAdapter;
	private Drawable mDivider;
	private OnItemClickListener mListener;

	public BaseHorizontalMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BaseHorizontalMenuView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setOrientation(LinearLayout.HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL);
		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getX() > getLeft() && ev.getX() < getRight() && ev.getY() > getTop()
				&& ev.getY() < getBottom()) {
			return super.onTouchEvent(ev);
		} else {
			if (ev.getAction() == MotionEvent.ACTION_UP) {
				if (mParent != null && mParent.isShowing()) {
					mParent.dismiss();
				}
			}
		}
		return super.onTouchEvent(ev);
	}

	public void setParent(BaseMenu parent) {
		mParent = parent;
	}

	public void setAdapter(BaseMenuAdapter adapter) {
		mAdapter = adapter;
	}

	public void setDivider(Drawable divider) {
		mDivider = divider;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mListener = listener;
	}

	public static interface OnItemClickListener {
		public void onItemClick(int position);
	}

	@Override
	public void onClick(View v) {
		Object tag = v.getTag();
		if (tag != null) {
			int pos = mAdapter.mList.indexOf(tag);
			if (mListener != null && pos > -1) {
				mListener.onItemClick(pos);
			}
		}
	}

	public void refreshContent() {
		removeAllViews();
		if (mAdapter != null) {
			int size = mAdapter.getCount();
			for (int i = 0; i < size; i++) {
				View view = mAdapter.getView(i, null, this);
				view.setOnClickListener(this);
				this.addView(view, new LayoutParams(
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.FILL_PARENT));

				if (i < size - 1) {
					ImageView imageView = new ImageView(getContext());
					imageView.setImageDrawable(mDivider);
					imageView.setScaleType(ScaleType.FIT_XY);
					this.addView(imageView, new LayoutParams(DrawUtils.dip2px(0.5f),
							android.view.ViewGroup.LayoutParams.FILL_PARENT));
				}
			}
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (mParent.isShowing()) {
				mParent.dismiss();
				return true;
			}
		}
		return false;
	}
}
