package com.jiubang.ggheart.apps.desks.appfunc.search;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-10-23]
 */
public class SearchNavigationbar extends LinearLayout {
	private Context mContext;
	public static final String HISTORY = "history";
	public String[] sALPHABETS = { HISTORY, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
			"L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#" };
	float mBlockSize = 0;
	private NavigationBarListener mBarListener;
	private AppFuncUtils mUtils;
	private boolean mIsVertical;
	public SearchNavigationbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mUtils = AppFuncUtils.getInstance(context);
		mIsVertical = mUtils.isVertical();
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		for (String alphabet : sALPHABETS) {
			View view = null;
			if (alphabet.equals(HISTORY)) {
				ImageView imageView = new ImageView(mContext);
				imageView.setLayoutParams(new LayoutParams(
						android.view.ViewGroup.LayoutParams.FILL_PARENT,
						android.view.ViewGroup.LayoutParams.FILL_PARENT));
				imageView.setImageResource(R.drawable.appfunc_search_history);
				imageView.setScaleType(ScaleType.CENTER_INSIDE);
				view = imageView;
			} else {
				view = View.inflate(mContext, R.layout.search_navigation_bar_textview, null);
				((TextView) view).setText(alphabet);
			}
			addView(view, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT, 1));
		}
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mUtils.isVertical()) {
			mBlockSize = b / sALPHABETS.length;
		} else {
			mBlockSize = r / sALPHABETS.length;
		}
		super.onLayout(changed, l, t, r, b);

	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mIsVertical = mUtils.isVertical();
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				resetTextColor();
				int i = 0;
				if (mIsVertical) {
					i = (int) ((event.getY() - getScrollY()) / mBlockSize);
				} else {
					i = (int) ((event.getX() - getScrollX()) / mBlockSize);
				}
				mBarListener.onNavigationDown(getInitial(i));
				break;
			}
			case MotionEvent.ACTION_MOVE : {
				int i = 0;
				float pos = 0;
				if (mIsVertical) {
					pos = (float) (event.getY() - getScrollY());
					i = (int) ((event.getY() - getScrollY()) / mBlockSize);
				} else {
					pos = (float) (event.getX() - getScrollX());
					i = (int) ((event.getX() - getScrollX()) / mBlockSize);
				}
				mBarListener.onNavigationMove(getInitial(i), pos, MotionEvent.ACTION_MOVE,
						mIsVertical);
			}
				break;
			case MotionEvent.ACTION_CANCEL : {

			}
				break;
			case MotionEvent.ACTION_UP : {
				int i = 0;
				float pos = 0;
				if (mIsVertical) {
					pos = (float) (event.getY() - getScrollY());
					i = (int) ((event.getY() - getScrollY()) / mBlockSize);
				} else {
					pos = (float) (event.getX() - getScrollX());
					i = (int) ((event.getX() - getScrollX()) / mBlockSize);
				}
				setTextColor(i);
				mBarListener.onNavigationMove(getInitial(i), pos, MotionEvent.ACTION_UP,
						mIsVertical);
				break;
			}
			default :
				break;
		}
		return true;
	}
	private String getInitial(int i) {
		String key = null;
		if (i < 0) {
			i = 0;
		} else if (i >= sALPHABETS.length) {
			i = sALPHABETS.length - 1;
		}
		if (mBarListener != null) {
			key = sALPHABETS[i];
		}
		return key;
	}

	public void resetTextColor() {
		for (int i = 0; i < sALPHABETS.length; i++) {
			View view = getChildAt(i);
			if (view instanceof TextView) {
				((TextView) view).setTextColor(0xFFBDBCBB);
			} else if (view instanceof ImageView) {
				((ImageView) view).setImageResource(R.drawable.appfunc_search_history);
			}
		}
	}

	private void setTextColor(int i) {
		if (i < 0) {
			i = 0;
		} else if (i >= sALPHABETS.length) {
			i = sALPHABETS.length - 1;
		}
		View view = getChildAt(i);
		if (view instanceof TextView) {
			((TextView) view).setTextColor(0xFF85B100);
		} else if (view instanceof ImageView) {
			((ImageView) view).setImageResource(R.drawable.appfunc_search_history_light);
		}
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		return true;
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2012-10-25]
	 */
	protected interface NavigationBarListener {
		public void onNavigationMove(String initial, float pos, int action, boolean isVertical);
		public void onNavigationDown(String initial);
	}
	public void setInitialListener(NavigationBarListener listener) {
		mBarListener = listener;
	}

}
