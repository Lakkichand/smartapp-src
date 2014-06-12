package com.jiubang.ggheart.apps.desks.diy.frames.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class TransitionAnimation extends Animation {
	TransitionView mTransitionView;

	@Override
	public boolean willChangeBounds() {
		return false;
	}

	@Override
	public boolean willChangeTransformationMatrix() {
		return false;
	}

	final void setTransitonView(TransitionView view) {
		mTransitionView = view;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		if (mTransitionView != null && !hasEnded()) { // 避免两次的interpolatedTime为1的调用
			mTransitionView.onAnimate(interpolatedTime);
		}
	}
}
