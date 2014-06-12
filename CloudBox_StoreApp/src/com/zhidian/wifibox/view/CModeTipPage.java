package com.zhidian.wifibox.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.Setting;

/**
 * 普通模式提示页
 * 
 * @author xiedezhi
 */
public class CModeTipPage extends RelativeLayout {

	private Bitmap mBackground = null;

	private volatile boolean animating = false;

	public CModeTipPage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CModeTipPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CModeTipPage(Context context) {
		super(context);
	}

	@Override
	protected void onFinishInflate() {
		this.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (animating) {
					return;
				}
				Animation animation = new AlphaAnimation(1.0f, 0);
				animation.setDuration(700);
				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						MainActivity.sendHandler(null,
								IDiyFrameIds.MAINVIEWGROUP,
								IDiyMsgIds.REMOVE_C_TIPS_PAGE, -1, null, null);
					}
				});
				animating = true;
				startAnimation(animation);
			}
		});
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (mBackground == null || mBackground.getWidth() != getWidth()
				|| mBackground.getHeight() != getHeight()
				|| mBackground.isRecycled()) {
			mBackground = DrawUtil.getCTipPageBackground(getWidth(),
					getHeight(), findViewById(R.id.dian1),
					findViewById(R.id.dian2), findViewById(R.id.dian3),
					findViewById(R.id.dian4));
		}
		canvas.drawBitmap(mBackground, 0, 0, null);
		super.dispatchDraw(canvas);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			performClick();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

}
