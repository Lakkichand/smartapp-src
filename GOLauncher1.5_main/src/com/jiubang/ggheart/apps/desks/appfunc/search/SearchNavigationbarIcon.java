package com.jiubang.ggheart.apps.desks.appfunc.search;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.search.SearchNavigationbar.NavigationBarListener;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-10-25]
 */
public class SearchNavigationbarIcon extends RelativeLayout implements NavigationBarListener {
	private RelativeLayout mIconVIew;
	private TextView mInitialText;
	private Context mContext;
	private float mPos;
	private String mInitial = "";
	private boolean mIsMove;
	private boolean mIsVertical;
	private InitialListener mInitialListener;
	private float mOffset;
	private ImageView mHistoryIcon;
	public SearchNavigationbarIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mIsVertical = AppFuncUtils.getInstance(context).isVertical();
	}
	@Override
	protected void onFinishInflate() {
		mIconVIew = (RelativeLayout) View.inflate(mContext, R.layout.search_navigation_bar_icon,
				null);
		mIconVIew.setDrawingCacheEnabled(true);
		mInitialText = (TextView) mIconVIew
				.findViewById(R.id.appfunc_search_navigation_bar_initial);
		mHistoryIcon = (ImageView) mIconVIew
				.findViewById(R.id.appfunc_search_navigation_bar_history);
		computeOffset();
		addView(mIconVIew);
		super.onFinishInflate();
	}
	private void computeOffset() {
			mOffset = (float) DrawUtils.dip2px(20);
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mIsVertical = AppFuncUtils.getInstance(mContext).isVertical();
		computeOffset();
		super.onSizeChanged(w, h, oldw, oldh);
	}
	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (mIsMove) {
			Bitmap bitmap = mIconVIew.getDrawingCache(true);
			if (mIsVertical) {
				float y = mPos - mOffset;
				if (y < 0) {
					y = 0;
				}
				if (y > getBottom() - mIconVIew.getHeight()) {
					y = getBottom() - mIconVIew.getHeight();
				}
				canvas.save();
				canvas.drawBitmap(bitmap, 0, y, null);
				canvas.restore();
			} else {
				float x = mPos - mOffset;
				if (x < 0) {
					x = 0;
				}
				if (x > getRight() - mIconVIew.getWidth()) {
					x = getRight() - mIconVIew.getWidth();
				}
				canvas.save();
				canvas.drawBitmap(bitmap, x, 0, null);
				canvas.restore();
			}
		}
	}
	@Override
	public void onNavigationMove(String initial, float pos, int action, boolean isVertical) {
		mPos = pos;
		mInitial = initial;
		mIsVertical = isVertical;
		mInitialText.setText(mInitial);
		mHistoryIcon.setVisibility(View.GONE);
		if (SearchNavigationbar.HISTORY.equals(mInitial)) {
			mInitialText.setText("");
			mHistoryIcon.setVisibility(View.VISIBLE);
		}
		if (action == MotionEvent.ACTION_MOVE) {
			mIsMove = true;
		} else {
			mIsMove = false;
			if (mInitialListener != null && initial != null) {
				mInitialListener.onNavigationUp(initial);
			}
		}
		requestLayout();
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2012-10-25]
	 */
	public interface InitialListener {
		public void onNavigationUp(String initial);
		public void onNavigationDown(String initial);
	}

	public void setInitialListener(InitialListener listener) {
		mInitialListener = listener;
	}

	public boolean isMove() {
		return mIsMove;
	}
	@Override
	public void onNavigationDown(String initial) {
		mInitialListener.onNavigationDown(initial);
	}
}
