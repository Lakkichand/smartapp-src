package com.go.util.graphics;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

/**
 * 图像处理类
 * 
 * @author Deng Weiming
 * 
 */
public class ImageFilter {

	final static PorterDuffXfermode XFERMODE_SCREEN = new PorterDuffXfermode(PorterDuff.Mode.SCREEN);
	protected static PaintFlagsDrawFilter DRAW_FILTER = new PaintFlagsDrawFilter(0,
			Paint.FILTER_BITMAP_FLAG);

	/**
	 * 将位图转成ARGB8888格式
	 * 
	 * @param bitmap
	 * @return 如果转换失败，返回null
	 */
	public static Bitmap convertToARGB8888(Bitmap bitmap) {
		if (bitmap.getConfig() == Config.ARGB_8888) {
			return bitmap;
		}
		Bitmap buffer = null;
		try {
			buffer = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		} catch (OutOfMemoryError e) {
			return null;
		}
		if (buffer != null) {
			buffer.setDensity(bitmap.getDensity());
			Canvas bufferCanvas = new Canvas(buffer);
			bufferCanvas.drawBitmap(bitmap, 0, 0, null);
		}
		return buffer;
	}

	/**
	 * 将位图转成RGB_565格式
	 * 
	 * @param bitmap
	 * @return 如果转换失败，返回null
	 */
	public static Bitmap convertToRGB565(Bitmap bitmap) {
		if (bitmap.getConfig() == Config.RGB_565) {
			return bitmap;
		}
		Bitmap buffer = null;
		try {
			buffer = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.RGB_565);
		} catch (OutOfMemoryError e) {
			return null;
		}
		buffer.setDensity(bitmap.getDensity());
		Canvas bufferCanvas = new Canvas(buffer);
		bufferCanvas.drawBitmap(bitmap, 0, 0, null);
		return buffer;
	}

	/**
	 * 图片模糊效果
	 * 
	 * @param bitmap
	 *            最好为ARGB8888格式的图片，RGB565会越来越暗，注意原图会被修改
	 * @param step
	 *            迭代次数，越大越模糊，也就越耗时间，推荐值4
	 * @param sampleSize
	 *            采样比例（不小于2），越大越模糊，也就越节约时间，但过大也会产生马赛克，推荐值4
	 * @return 是否处理成功
	 */
	public static boolean trickyBlur(Bitmap bitmap, int step, float sampleSize) {
		sampleSize = Math.max(2, sampleSize);
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		final int smllWidth = Math.round(width / sampleSize);
		final int smallHeight = Math.round(height / sampleSize);
		Bitmap buffer = null;
		try {
			buffer = Bitmap.createBitmap(smllWidth, smallHeight, bitmap.getConfig());
		} catch (OutOfMemoryError e) {
			return false;
		}
		buffer.setDensity(bitmap.getDensity());
		Canvas canvas = new Canvas(bitmap);
		canvas.setDrawFilter(DRAW_FILTER);
		Canvas bufferCanvas = new Canvas(buffer);
		bufferCanvas.setDrawFilter(DRAW_FILTER);
		for (int i = 0; i < step; ++i) {
			int w = Math.max(1, smllWidth - i);
			int h = Math.max(1, smallHeight - i);
			bufferCanvas.save();
			bufferCanvas.scale(w / (float) (width), h / (float) height);
			// buffer.eraseColor(0);
			bufferCanvas.drawBitmap(bitmap, 0, 0, null);
			bufferCanvas.restore();
			canvas.save();
			canvas.scale(width / (float) w, height / (float) h);
			// bitmap.eraseColor(0);
			canvas.drawBitmap(buffer, 0, 0, null);
			canvas.restore();
		}
		buffer.recycle();
		return true;
	}

	/**
	 * 图片辉光效果
	 * 
	 * @param bitmap
	 *            注意原图会被修改
	 * @param blurStep
	 *            模糊的迭代次数，越大越模糊，也就越耗时间，推荐值4~8
	 * @param sampleSize
	 *            模糊采样比例（不小于2），越大越模糊，也就越节约时间，但过大也会产生马赛克，推荐值4
	 * @param glowStep
	 *            辉光的迭代次数，越大越亮，也就越耗时间，推荐值2（如果使用1并且调用本函数两次，得到的结果会更朦胧）
	 * @param glow
	 *            辉光的亮度，越大越亮，范围是[0, 1]，推荐值0.8
	 * @return 是否处理成功
	 */
	public static boolean trickyGlow(Bitmap bitmap, int blurStep, float sampleSize, int glowStep,
			float glow) {
		Bitmap buffer = null;
		try {
			buffer = Bitmap.createBitmap(bitmap);
		} catch (OutOfMemoryError e) {
			return false;
		}
		buffer.setDensity(bitmap.getDensity());
		if (trickyBlur(buffer, blurStep, sampleSize)) {
			Paint paint = new Paint();
			glow = Math.max(0, Math.min(glow, 1));
			paint.setAlpha((int) (glow * 255));
			paint.setXfermode(XFERMODE_SCREEN);
			Canvas canvas = new Canvas(bitmap);
			for (int i = 0; i < glowStep; ++i) {
				canvas.drawBitmap(buffer, 0, 0, paint);
				if (i + 1 < glowStep) {
					canvas.setBitmap(buffer);
					canvas.drawBitmap(bitmap, 0, 0, paint);
					canvas.setBitmap(bitmap);
					++i;
				}
			}
		}
		buffer.recycle();
		return true;
	}

	/**
	 * 
	 * @param bitmap
	 *            最好为ARGB8888格式的图片，RGB565会越来越暗，注意原图会被修改
	 * @param step
	 *            模糊的迭代次数
	 * @param radius
	 *            模糊半径
	 * @return 是否处理成功
	 */
	public static boolean trickyGaussianBlur(Bitmap bitmap, int step, int radius) {
		Bitmap buffer = null;
		try {
			buffer = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
		} catch (OutOfMemoryError e) {
			return false;
		}
		buffer.setDensity(bitmap.getDensity());
		Canvas canvas = new Canvas();
		Paint paint = new Paint();

		radius = Math.max(1, radius);
		final float[] weight = new float[radius * 2];
		// 生成卷积核，这里用了帐篷式过滤器
		float sumOfWeight = radius + 0.5f;
		for (int i = 0; i < radius; ++i) {
			float w = i + 0.5f;
			weight[i] = weight[i + radius] = w;
			sumOfWeight += w * 2;
		}

		// printArray(weight);
		final float oneOverSumOfWeight = 1 / sumOfWeight;
		float mul = 1;
		for (int i = 0; i < radius * 2; ++i) {
			weight[i] *= oneOverSumOfWeight;
			mul *= 1 - weight[i];
		}
		mul = 1 / mul;
		// printArray(weight);
		for (int i = 0; i < radius * 2; ++i) {
			mul *= 1 - weight[i];
			weight[i] *= mul;
		}
		// Log.i("DWM", "sum=" + sumOfWeight);
		// printArray(weight);

		for (int j = 0; j < step; ++j) {
			// do horizontal blur
			canvas.setBitmap(buffer);
			canvas.drawBitmap(bitmap, 0, 0, null);
			for (int i = 0; i < radius; ++i) {
				paint.setAlpha(Math.round(weight[i] * 255));
				canvas.drawBitmap(bitmap, -i, 0, paint);
			}
			for (int i = 0; i < radius; ++i) {
				paint.setAlpha(Math.round(weight[i + radius] * 255));
				canvas.drawBitmap(bitmap, i, 0, paint);
			}
			// do vertical blur
			canvas.setBitmap(bitmap);
			canvas.drawBitmap(buffer, 0, 0, null);
			for (int i = 0; i < radius; ++i) {
				paint.setAlpha(Math.round(weight[i] * 255));
				canvas.drawBitmap(buffer, 0, -i, paint);
			}
			for (int i = 0; i < radius; ++i) {
				paint.setAlpha(Math.round(weight[i + radius] * 255));
				canvas.drawBitmap(buffer, 0, i, paint);
			}
		}
		buffer.recycle();
		return true;
	}

	static void printArray(float[] array) {
		String info = "";
		for (int i = 0; i < array.length; ++i) {
			info += " " + array[i];
		}
		Log.i("DWM", info);
	}
}
