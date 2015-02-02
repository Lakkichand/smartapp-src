package com.zhidian.wifibox.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.smartapp.ex.cleanmaster.R;

/**
 * 自动旋转的view
 * 
 * @author xiedezhi
 * 
 */
public class RotationView extends ImageView {

	public RotationView(Context context) {
		super(context);
	}

	public RotationView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RotationView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 开始旋转
	 */
	public void rotate() {
		Animation operatingAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.rotation);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		this.startAnimation(operatingAnim);
	}

	/**
	 * 停止旋转
	 */
	public void stop() {
		this.clearAnimation();
	}

}
