package com.jiubang.ggheart.appgame.base.component;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.gau.go.launcherex.R;

/**
 * 精品推荐banner图，有点击效果
 * 
 * @author xiedezhi
 * @date [2012-9-20]
 */
public class FeatureBanner extends ImageView {
	/**
	 * 点击效果颜色
	 */
	private int mEffectColor = 0;
	/**
	 * 展示点击效果
	 */
	private boolean mShowEffect = false;

	public FeatureBanner(Context context) {
		super(context);
		init();
	}

	public FeatureBanner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FeatureBanner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mEffectColor = getResources().getColor(R.color.appgame_banner_press_color);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			mShowEffect = true;
			invalidate();
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_MASK:
		case MotionEvent.ACTION_OUTSIDE:
		default:
			mShowEffect = false;
			invalidate();
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mShowEffect) {
			canvas.drawColor(mEffectColor);
		}
	}
}
