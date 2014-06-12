package com.jiubang.ggheart.apps.desks.diy.frames.animation;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

/**
 * 实现两个场景（视图）切换动画的视图类。
 * 
 * @author dengweiming
 * 
 */
public class TransitionView extends BinaryView {
	private final int DEFAULT_DURATION = 500;

	boolean mViewOpaque;
	TransitionAnimation mAnimation;
	Transition mTransition = new DefaultTransition();

	public TransitionView(Context context) {
		super(context);
	}

	/**
	 * 交换原始视图和目标视图。
	 */
	public void swapViews() {
		setViews(mView2, mView1);
	}

	/**
	 * 延迟交换原始视图和目标视图。
	 */
	public void postSwapViews() {
		post(new Runnable() {
			@Override
			public void run() {
				swapViews();

			}
		});
	}

	/**
	 * 设置动画。 插值时间为0和1的时候分别显示{@link #setViews(View, View)}中设置的两个视图。
	 * 
	 * @param animation
	 */
	public void setTransitionAnimation(TransitionAnimation animation) {
		mAnimation = animation;
		if (animation != null) {
			animation.setTransitonView(this);
			super.setAnimation(animation);
		}
	}

	/**
	 * 开始动画。 插值时间为0和1的时候分别显示{@link #setViews(View, View)}中设置的两个视图。
	 * 
	 * @param animation
	 */
	public void startTransition(TransitionAnimation animation) {
		mAnimation = animation;
		if (animation != null) {
			animation.setTransitonView(this);
			super.startAnimation(animation);
		}
	}

	/**
	 * 开始动画
	 * 
	 * @param durationMillis
	 *            指定的动画时间
	 */
	public void startTransition(int durationMillis) {
		if (durationMillis < 0) {
			durationMillis = DEFAULT_DURATION;
		}
		TransitionAnimation animation = mAnimation != null ? mAnimation : new TransitionAnimation();
		animation.setDuration(durationMillis);
		startTransition(animation);
	}

	@Override
	protected void onAnimationStart() {
		// Log.i("DWM", "Transition start");
		if (mView1 != null) {
			mView1.setDrawingCacheEnabled(true);
		}
		if (mView2 != null) {
			mView2.setDrawingCacheEnabled(true);
		}
		super.onAnimationStart();
	}

	@Override
	protected void onAnimationEnd() {
		// Log.i("DWM", "Transition end");
		if (mAnimation != null) {
			mAnimation.setTransitonView(null);
			mAnimation = null;
		}
		if (mView1 != null) {
			mView1.setDrawingCacheEnabled(false);
			mView1.clearAnimation();
		}
		if (mView2 != null) {
			mView2.setDrawingCacheEnabled(false);
			mView2.clearAnimation();
		}
		super.onAnimationEnd();
	}

	/**
	 * 设置{@link #setViews(View, View)}参数中的视图是否不透明的。 如果不透明有可能提高绘制效率。
	 * 
	 * @param opaque
	 */
	public void setViewOpaque(boolean opaque) {
		mViewOpaque = opaque;
	}

	public void setTransition(Transition transition) {
		mTransition = transition;
	}

	public Transition getTransition() {
		return mTransition;
	}

	void onAnimate(float t) {
		if (mTransition != null) {
			mTransition.onAnimate(t, this);
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		// Log.i("DWM", "TransitionView onDraw");
		if (mTransition != null) {
			mTransition.onDraw(canvas, this);
		} else {
			super.dispatchDraw(canvas);
		}
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
	}

	public void setTime(float t) {
		t = Math.max(0, Math.min(t, 1));
		onAnimate(t);
		postInvalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	static class DefaultTransition implements Transition {
		float mTx;

		@Override
		public void onAnimate(float t, BinaryView view) {
			mTx = t * view.getWidth();
		}

		@Override
		public void onDraw(Canvas canvas, BinaryView view) {
			final long drawingTime = view.getDrawingTime();
			canvas.translate(-mTx, 0);
			view.drawFirstView(canvas, drawingTime);
			canvas.translate(view.getWidth(), 0);
			view.drawSecondView(canvas, drawingTime);
		}
	}

}
