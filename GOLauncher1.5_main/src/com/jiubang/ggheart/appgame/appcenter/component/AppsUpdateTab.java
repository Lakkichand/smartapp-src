package com.jiubang.ggheart.appgame.appcenter.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;

public class AppsUpdateTab extends TextView {

	private final static int ICON_OFFSET_X = DrawUtils.dip2px(2);
	private final static int ICON_OFFSET_Y = DrawUtils.dip2px(13);
	private int mUpdateCount;
	private int mIconLeft;
	private int mIconTop;
	private Paint mPaint;

	public AppsUpdateTab(Context context) {
		super(context);
	}

	public AppsUpdateTab(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
		mPaint.setColor(Color.WHITE);
		mPaint.setTextSize(getContext().getResources().getDimension(
				R.dimen.apps_management_update_count_text_size));
		mPaint.setTypeface(Typeface.DEFAULT_BOLD);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		String content = (String) getText();
		Rect rect = new Rect();
		mPaint.getTextBounds(content, 0, content.length(), rect);
		int contentWidth = rect.width();
		int contentHeight = rect.height();
		mIconLeft = width - (width - contentWidth) / 2 - ICON_OFFSET_X;
		mIconTop = (height - contentHeight) / 2 - ICON_OFFSET_Y;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mUpdateCount > 0) {
			NinePatchDrawable bgNine = (NinePatchDrawable) getContext().getResources().getDrawable(
					R.drawable.appsmanagement_update_count_notification_orange);
			int size = (int) getContext().getResources().getDimension(R.dimen.message_notify_size);
			bgNine.setBounds(new Rect(0, 0, size, size));

			Bitmap bgIcon = Bitmap.createBitmap(bgNine.getBounds().width(), bgNine.getBounds()
					.height(), Config.ARGB_8888);
			Canvas iconCanvas = new Canvas(bgIcon);
			bgNine.draw(iconCanvas);
			String updateCountStr = String.valueOf(mUpdateCount);
			float w = mPaint.measureText(updateCountStr);
			iconCanvas.drawText(updateCountStr, (bgIcon.getWidth() - w) / 2,
					bgIcon.getHeight() * 2 / 3, mPaint);
			canvas.drawBitmap(bgIcon, mIconLeft, mIconTop, mPaint);

		}
	}

	public void setUpdateCount(int count) {
		mUpdateCount = count;
		invalidate();
	}
}
