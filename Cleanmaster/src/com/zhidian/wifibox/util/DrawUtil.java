package com.zhidian.wifibox.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;

import com.smartapp.ex.cleanmaster.R;
import com.ta.TAApplication;

/**
 * UI相关工具类
 * 
 * @author xiedezhi
 * 
 */
public class DrawUtil {

	/**
	 * 默认图标
	 */
	public static final Bitmap sDefaultIcon = ((BitmapDrawable) TAApplication
			.getApplication().getResources()
			.getDrawable(R.drawable.default_logo)).getBitmap();

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 为图标加上遮罩
	 * 
	 * @param bitmap
	 *            原始bitmap
	 * @return 处理后的bitmap
	 */
	public static Bitmap createMaskBitmap(Context context, Bitmap bitmap) {
		if (context == null || bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		// 取罩子和底座和mask蒙版
		Bitmap base = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.category_icon_base)).getBitmap();
		base = base.copy(Bitmap.Config.ARGB_8888, true);
		BitmapDrawable cover = null;
		BitmapDrawable mask = (BitmapDrawable) context.getResources()
				.getDrawable(R.drawable.appgame_mask);
		try {
			Canvas canvas = new Canvas();
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
					Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			drawable = composeIcon(context, base, cover, drawable, mask,
					canvas, paint, new PorterDuffXfermode(
							PorterDuff.Mode.DST_IN), 0.94f, false, 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError error) {
			error.printStackTrace();
		}
		base = null;
		cover = null;
		if (drawable == null) {
			return bitmap;
		} else {
			return drawable.getBitmap();
		}
	}

	/**
	 * 为图标加上下载进度
	 * 
	 * @param bitmap
	 *            原始bitmap
	 * @return 处理后的bitmap
	 */
	public static Bitmap createProgressBitmap(Context context, Bitmap bitmap,
			int progress) {
		if (context == null || bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		// 取罩子和底座和mask蒙版
		Bitmap base = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.category_icon_base)).getBitmap();
		base = base.copy(Bitmap.Config.ARGB_8888, true);
		BitmapDrawable cover = null;
		BitmapDrawable mask = (BitmapDrawable) context.getResources()
				.getDrawable(R.drawable.appgame_mask);
		try {
			Canvas canvas = new Canvas();
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
					Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			drawable = composeIcon(context, base, cover, drawable, mask,
					canvas, paint, new PorterDuffXfermode(
							PorterDuff.Mode.DST_IN), 1, true, progress,
					0x69000000);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError error) {
			error.printStackTrace();
		}
		base = null;
		cover = null;
		if (drawable == null) {
			return bitmap;
		} else {
			return drawable.getBitmap();
		}
	}

	/**
	 * 合成图片
	 * 
	 * @param base
	 *            ：合成图片底图
	 * @param cover
	 *            ：合成图片罩子
	 * @param drawable
	 *            ： 待合成的源图
	 * @param mask
	 *            ： 合成图片蒙版
	 * @param canvas
	 *            ：画布
	 * @param matrix
	 *            ：缩放matrix
	 * @param paint
	 *            ：画笔
	 * @param scale
	 *            ：缩放比率
	 * @return
	 */
	public static BitmapDrawable composeIcon(Context context, Bitmap base,
			BitmapDrawable cover, BitmapDrawable drawable, BitmapDrawable mask,
			Canvas canvas, Paint paint, PorterDuffXfermode xfermode,
			float scale, boolean showProgress, int progress, int progressColor) {
		if (context == null || canvas == null || paint == null
				|| drawable == null || drawable.getBitmap() == null) {
			return drawable;
		}
		// 有底图或罩子
		if (base == null) {
			if (cover != null && cover.getBitmap() != null) {
				final Bitmap.Config config = cover.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
						: Bitmap.Config.RGB_565;
				base = Bitmap.createBitmap(cover.getBitmap().getWidth(), cover
						.getBitmap().getHeight(), config);
			}
			if (base == null) {
				return drawable;
			}
		}
		int width = base.getWidth();
		int height = base.getHeight();

		float scaleWidth = scale * width; // 缩放后的宽大小
		float scaleHeight = scale * height; // 缩放后的高大小
		Bitmap midBitmap = drawable.getBitmap();
		canvas.setBitmap(base);
		int saveId = canvas.save();
		paint.setAntiAlias(true);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
		midBitmap = Bitmap.createScaledBitmap(midBitmap,
				(int) (scaleWidth + 0.5), (int) (scaleHeight + 0.5), true);
		canvas.drawBitmap(midBitmap,
				(base.getWidth() - midBitmap.getWidth()) / 2,
				(base.getHeight() - midBitmap.getHeight()) / 2, paint);
		canvas.restoreToCount(saveId);
		if (cover != null) {
			canvas.drawBitmap(cover.getBitmap(), 0, 0, paint);
		}
		if (showProgress) {
			int top = 0;
			int left = 0;
			int right = width;
			int bottom = (int) ((100.0 - progress) / 100.0 * height + 0.5);
			paint.setColor(progressColor);
			canvas.drawRect(left, top, right, bottom, paint);
			paint.reset();
			paint.setAntiAlias(true);
		}
		// 加上mask蒙版
		if (mask != null) {
			paint.setXfermode(xfermode);
			canvas.drawBitmap(mask.getBitmap(), 0, 0, paint);
		}
		return new BitmapDrawable(base);
	}

}
