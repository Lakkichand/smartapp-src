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
 * 有圆形进度显示的BitmapDrawable
 * 
 * @author xiedezhi
 * 
 */
public class ProgressBitmapDrawable extends BitmapDrawable {

	/**
	 * 当前进度，0到100
	 */
	private int mProgress = 0;
	/**
	 * 底色
	 */
	private int mNormalColor = 0;
	/**
	 * 进度颜色
	 */
	private int mProgresColor = 0;
	/**
	 * 线宽
	 */
	private int mStrokeWidth = DrawUtil.dip2px(TAApplication.getApplication(),
			1.5f);
	private int mGap = DrawUtil.dip2px(TAApplication.getApplication(), 0.5f);

	/**
	 * 构造函数
	 */
	public ProgressBitmapDrawable(Resources res, Bitmap bitmap, int progress,
			int normalColor, int progressColor) {
		super(res, bitmap);
		mProgress = progress;
		mNormalColor = normalColor;
		mProgresColor = progressColor;
		if (mProgress < 0) {
			mProgress = 0;
		}
		if (mProgress > 100) {
			mProgress = 100;
		}
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		Rect rect = copyBounds();
		Paint paint = getPaint();
		// 设置画笔为无锯齿
		paint.setAntiAlias(true);
		// 线宽
		paint.setStrokeWidth(mStrokeWidth);
		paint.setStyle(Style.STROKE);
		// 设置画笔颜色
		paint.setColor(mNormalColor);
		// 画底图
		RectF rectf = new RectF(rect.left + mGap, rect.top + mGap, rect.right
				- mGap, rect.bottom - mGap);
		canvas.drawArc(rectf, 0, 360, false, paint);
		// 设置画笔颜色
		paint.setColor(mProgresColor);
		// 画进度
		canvas.drawArc(rectf, 270, (int) (mProgress / 100.0 * 360 + 0.5),
				false, paint);
	}
}
