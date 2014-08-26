package com.zhidian.wifibox.view;

import com.zhidian.wifibox.util.DrawUtil;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.BitmapDrawable;

/**
 * 仪表
 * 
 * @author xiedezhi
 * 
 */
public class MetersDrawable extends BitmapDrawable {

	/**
	 * 当前进度，0到100
	 */
	private int mProgress = 0;

	private int mDegree = 0;

	private int[] mArray = new int[360];

	public MetersDrawable(Resources res, Bitmap bitmap, int progress) {
		super(res, bitmap);
		if (progress < 0) {
			progress = 0;
		}
		if (progress > 100) {
			progress = 100;
		}
		mProgress = progress;
		mDegree = (int) (progress * 1.0 / 100.0 * 200.0 + 0.5);
		if (mDegree <= 1) {
			mDegree = 2;
		}
		double gap = (0xff - 0x06) * 1.0 / (mDegree - 1);
		int[] array = new int[360];
		for (int i = 0; i < mDegree; i++) {
			int color = (((int) (gap * i + 0.5 + 0x06)) << 24) + 0x00FFFFFF;
			array[i] = color;
		}
		for (int i = 0; i < array.length; i++) {
			int index = i + 170;
			if (index >= 360) {
				index = index - 360;
			}
			mArray[index] = array[i];
		}
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		// 画渐变扇形
		Rect rect = copyBounds();
		int width = rect.right - rect.left;
		int height = rect.bottom - rect.top;
		int radius = (int) (146.0 / 369.0 * height + 0.5);
		int centerX = rect.left + width / 2;
		int centerY = (int) (306.0 / 369.0 * height + 0.5);

		RectF rectf = new RectF(centerX - radius, centerY - radius, centerX
				+ radius, centerY + radius);
		Paint paint = new Paint();
		// 设置画笔为无锯齿
		paint.setAntiAlias(true);
		// 线宽
		paint.setStyle(Style.FILL);
		// 设置画笔颜色
		paint.setColor(0xFFFFFFFF);
		SweepGradient shader = new SweepGradient(centerX, centerY, mArray, null);
		paint.setShader(shader);
		canvas.drawArc(rectf, 170, mDegree, true, paint);
		// 画指针
		int savecount = canvas.save();
		canvas.translate(centerX, centerY);
		float degrees = 0;
		degrees = (mProgress - 50) * (100 / 50);
		canvas.rotate(degrees, 0, 0);
		canvas.drawBitmap(DrawUtil.sMeterPointer,
				-DrawUtil.sMeterPointer.getWidth() / 2,
				-DrawUtil.sMeterPointer.getHeight(), null);
		canvas.restoreToCount(savecount);
	}
}
