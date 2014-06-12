package com.zhidian.wifibox.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

import com.ta.TAApplication;
import com.zhidian.wifibox.util.DrawUtil;

/**
 * 页面进度条显示
 * 
 * @author xiedezhi
 * 
 */
public class PageProgressBitmapDrawable extends BitmapDrawable {

	private int mProgress = 0;

	private int mGap = DrawUtil.dip2px(TAApplication.getApplication(), 45);
	/**
	 * 线宽
	 */
	private int mStrokeWidth = DrawUtil.dip2px(TAApplication.getApplication(),
			35.0f);

	/**
	 * 构造函数
	 */
	public PageProgressBitmapDrawable(Resources res, Bitmap bitmap, int progress) {
		super(res, bitmap);
		mProgress = progress;
	}

	@Override
	public void draw(Canvas canvas) {
		Rect rect = copyBounds();
		Paint paint = getPaint();
		// 设置画笔为无锯齿
		paint.setAntiAlias(true);
		// 线宽
		paint.setStrokeWidth(mStrokeWidth);
		paint.setStyle(Style.STROKE);
		// 设置画笔颜色
		paint.setColor(0xFF2e80f3);
		// 画底图
		RectF rectf = new RectF(rect.left + mGap, rect.top + mGap, rect.right
				- mGap, rect.bottom - mGap);
		// 画进度
		canvas.drawArc(rectf, 270, (int) (mProgress / 100.0 * 360 + 0.5),
				false, paint);
		super.draw(canvas);
	}
}
