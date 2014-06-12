package com.jiubang.ggheart.appgame.base.component;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

/**
 * 
 * <br>
 * 类描述:应用详情gallery <br>
 * 功能详细描述:
 * 
 * @author zhujian
 */
public class AppDetailGallery extends Gallery {
	private float mLastX = 0;
	private float mLastY = 0;
	public final static int HORIZEN_MOVE = 0;
	public final static int VERTICAL_MOVE = 1;
	public final static int NO_MOVE = 3;
	public final static int CRITICAL_VALUE = 5;

	public AppDetailGallery(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AppDetailGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AppDetailGallery(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setAnimationDuration(int animationDurationMillis) {
		// TODO Auto-generated method stub
		super.setAnimationDuration(animationDurationMillis);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return super.onFling(e1, e2, velocityX / 8, velocityY);
	}

	@Override
	public void setSelection(int position) {
		// TODO Auto-generated method stub
		super.setSelection(position);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	public void setSelected(boolean selected) {
		// TODO Auto-generated method stub
		super.setSelected(selected);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return super.onScroll(e1, e2, distanceX, distanceY);
	}
	@Override
	protected void onAnimationStart() {
		// TODO Auto-generated method stub
		super.onAnimationStart();
	}

	@Override
	protected void onAnimationEnd() {
		// TODO Auto-generated method stub
		super.onAnimationEnd();
	}

	@Override
	public void setOnItemSelectedListener(
			android.widget.AdapterView.OnItemSelectedListener listener) {
		// TODO Auto-generated method stub
		super.setOnItemSelectedListener(listener);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastX = event.getX();
			mLastY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float x = event.getX();
			float y = event.getY();
			int state = mathXY(x, mLastX, y, mLastY);
			if (getSelectedItemPosition() == 0) {
				if (x > mLastX) {
					return false;
				}
			}
			if (state == HORIZEN_MOVE) {
				mLastX = event.getX();
				mLastY = event.getY();
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	public static int mathXY(float x, float lastX, float y, float lastY) {
		int state = -1;
		if ((Math.abs(y - lastY) / Math.abs(x - lastX)) >= 0.58) {
			state = VERTICAL_MOVE;
		} else {
			state = HORIZEN_MOVE;
		}
		if (Math.sqrt(Math.pow(y - lastY, 2) + Math.pow(x - lastX, 2)) <= CRITICAL_VALUE) {
			state = NO_MOVE;
		}
		return state;
	}
}
