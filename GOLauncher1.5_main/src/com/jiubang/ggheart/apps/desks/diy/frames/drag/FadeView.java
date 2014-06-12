package com.jiubang.ggheart.apps.desks.diy.frames.drag;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.go.util.graphics.FadePainter;

public class FadeView extends View implements ISelfObject {
	private int mMode;
	private int mColor;
	private FadePainter mFadePainter;

	public FadeView(Context context) {
		super(context);
		selfConstruct();
	}

	public FadeView(Context context, AttributeSet attr) {
		super(context, attr);
		selfConstruct();
	}

	public FadeView(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
		selfConstruct();
	}

	@Override
	public void selfConstruct() {
		mFadePainter = new FadePainter();
	}

	@Override
	public void selfDestruct() {
		mFadePainter.recycle();
		mFadePainter = null;
	}

	public void initFade(int mode, int color) {
		mMode = mode;
		mColor = color;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		Rect rc = new Rect(0, 0, getWidth(), getHeight());
		mFadePainter.drawFadeColor(canvas, rc, mMode, mColor);
	}
}
