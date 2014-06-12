package com.zhidian.wifibox.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;

/**
 * UI相关工具类
 * 
 * @author xiedezhi
 * 
 */
@SuppressWarnings("static-access")
public class DrawUtil {
	/**
	 * 默认图标
	 */
	public static final Bitmap sDefaultIcon = ((BitmapDrawable) TAApplication
			.getApplication().getResources()
			.getDrawable(R.drawable.default_logo)).getBitmap();
	/**
	 * 默认banner
	 */
	public static final Bitmap sDefaultBanner = ((BitmapDrawable) TAApplication
			.getApplication().getResources()
			.getDrawable(R.drawable.banner_default)).getBitmap();

	/**
	 * 专题默认banner
	 */
	public static final Bitmap sTopicDefaultBanner = ((BitmapDrawable) TAApplication
			.getApplication().getResources()
			.getDrawable(R.drawable.topic_banner)).getBitmap();
	/**
	 * 专题默认banner（大）
	 */
	public static final Bitmap sTopicDefaultBannerBig = ((BitmapDrawable) TAApplication
			.getApplication().getResources()
			.getDrawable(R.drawable.topic_banner_big)).getBitmap();

	/**
	 * 页面圆形进度背景
	 */
	public static final Bitmap sPageProgressBitmap = ((BitmapDrawable) TAApplication
			.getApplication().getResources()
			.getDrawable(R.drawable.progress_bg)).getBitmap();
	/**
	 * 咪表背景蓝色
	 */
	public static final Bitmap sMeterBitmap = ((BitmapDrawable) TAApplication
			.getApplication().getResources().getDrawable(R.drawable.meter_bg))
			.getBitmap();
	
	/**
	 * 咪表背景橙色
	 */
	public static final Bitmap oMeterBitmap = ((BitmapDrawable) TAApplication
			.getApplication().getResources().getDrawable(R.drawable.meter_bg_orange))
			.getBitmap();
	
	/**
	 * 咪表背景红色
	 */
	public static final Bitmap rMeterBitmap = ((BitmapDrawable) TAApplication
			.getApplication().getResources().getDrawable(R.drawable.meter_bg_red))
			.getBitmap();
	/**
	 * 咪表指针
	 */
	public static final Bitmap sMeterPointer;

	static {
		Bitmap pointer = ((BitmapDrawable) TAApplication.getApplication()
				.getResources().getDrawable(R.drawable.pointer)).getBitmap();
		sMeterPointer = pointer.createScaledBitmap(pointer,
				DrawUtil.dip2px(TAApplication.getApplication(), 10),
				DrawUtil.dip2px(TAApplication.getApplication(), 115), true);
	}
	
	/**
	 * 火箭指针
	 */
	public static final Bitmap sMeterRocket;

	static {
		Bitmap pointer = ((BitmapDrawable) TAApplication.getApplication()
				.getResources().getDrawable(R.drawable.rocket_icon)).getBitmap();
		sMeterRocket = pointer.createScaledBitmap(pointer,
				DrawUtil.dip2px(TAApplication.getApplication(), 50),
				DrawUtil.dip2px(TAApplication.getApplication(), 50), true);
	}
	
	/**
	 * 单车指针
	 */
	public static final Bitmap sMeterBike;

	static {
		Bitmap pointer = ((BitmapDrawable) TAApplication.getApplication()
				.getResources().getDrawable(R.drawable.bike_icon)).getBitmap();
		sMeterBike = pointer.createScaledBitmap(pointer,
				DrawUtil.dip2px(TAApplication.getApplication(), 50),
				DrawUtil.dip2px(TAApplication.getApplication(), 50), true);
	}
	
	/**
	 * 汽车指针
	 */
	public static final Bitmap sMeterCar;

	static {
		Bitmap pointer = ((BitmapDrawable) TAApplication.getApplication()
				.getResources().getDrawable(R.drawable.car_icon)).getBitmap();
		sMeterCar = pointer.createScaledBitmap(pointer,
				DrawUtil.dip2px(TAApplication.getApplication(), 50),
				DrawUtil.dip2px(TAApplication.getApplication(), 50), true);
	}

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
					canvas, new Matrix(), paint, new PorterDuffXfermode(
							PorterDuff.Mode.DST_IN), 1, false, 0, 0);
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
					canvas, new Matrix(), paint, new PorterDuffXfermode(
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
			Canvas canvas, Matrix matrix, Paint paint,
			PorterDuffXfermode xfermode, float scale, boolean showProgress,
			int progress, int progressColor) {
		if (context == null || canvas == null || matrix == null
				|| paint == null || drawable == null
				|| drawable.getBitmap() == null) {
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
		float scaleFactorW = 0f; // 缩放后较原图的宽的比例
		float scaleFactorH = 0f; // 缩放后较原图的高的比例
		if (midBitmap != null) {
			int realWidth = midBitmap.getWidth();
			int realHeight = midBitmap.getHeight();
			scaleFactorW = scaleWidth / realWidth;
			scaleFactorH = scaleHeight / realHeight;
		}
		canvas.setBitmap(base);
		int saveId = canvas.save();
		paint.setAntiAlias(true);
		matrix.setScale(scaleFactorW, scaleFactorH);
		matrix.postTranslate((width - scaleWidth) / 2f,
				(height - scaleHeight) / 2f);
		canvas.drawBitmap(drawable.getBitmap(), matrix, paint);
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

	/**
	 * 普通模式提示页背景板
	 */
	public static Bitmap getCTipPageBackground(int width, int height,
			View dian1, View dian2, View dian3, View dian4) {
		int length = width * height;
		int[] colors = new int[length];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = 0xaf000000;
		}
		// 背景颜色
		Bitmap bm = Bitmap
				.createBitmap(colors, width, height, Config.ARGB_8888);
		bm = bm.copy(Config.ARGB_8888, true);
		Canvas canvas = new Canvas();
		canvas.setBitmap(bm);

		Paint mPaint = new Paint();
		PorterDuffXfermode mXfermode = new PorterDuffXfermode(
				PorterDuff.Mode.DST_OUT);
		mPaint.setXfermode(mXfermode);

		Bitmap dian1b = ((BitmapDrawable) (TAApplication.getApplication()
				.getResources().getDrawable(R.drawable.cmask1))).getBitmap();
		dian1b = Bitmap.createScaledBitmap(dian1b, dian1.getWidth(),
				dian1.getHeight(), true);
		canvas.drawBitmap(dian1b, dian1.getLeft(), dian1.getTop(), mPaint);

		Bitmap dian2b = ((BitmapDrawable) (TAApplication.getApplication()
				.getResources().getDrawable(R.drawable.cmask2))).getBitmap();
		dian2b = Bitmap.createScaledBitmap(dian2b, dian2.getWidth(),
				dian2.getHeight(), true);
		canvas.drawBitmap(dian2b, dian2.getLeft(), dian2.getTop(), mPaint);

		Bitmap dian3b = ((BitmapDrawable) (TAApplication.getApplication()
				.getResources().getDrawable(R.drawable.cmask3))).getBitmap();
		dian3b = Bitmap.createScaledBitmap(dian3b, dian3.getWidth(),
				dian3.getHeight(), true);
		canvas.drawBitmap(dian3b, dian3.getLeft(),
				((ViewGroup) (dian3.getParent())).getTop(), mPaint);

		Bitmap dian4b = ((BitmapDrawable) (TAApplication.getApplication()
				.getResources().getDrawable(R.drawable.cmask4))).getBitmap();
		dian4b = Bitmap.createScaledBitmap(dian4b, dian4.getWidth(),
				dian4.getHeight(), true);
		canvas.drawBitmap(dian4b, dian4.getLeft(),
				((ViewGroup) (dian4.getParent())).getTop(), mPaint);

		return bm;
	}

	/**
	 * 极速模式提示页背景板
	 */
	public static Bitmap getXTipPageBackground(int width, int height,
			View dian1, View dian2, View dian3) {
		int length = width * height;
		int[] colors = new int[length];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = 0xaf000000;
		}
		// 背景颜色
		Bitmap bm = Bitmap
				.createBitmap(colors, width, height, Config.ARGB_8888);
		bm = bm.copy(Config.ARGB_8888, true);
		Canvas canvas = new Canvas();
		canvas.setBitmap(bm);

		Paint mPaint = new Paint();
		PorterDuffXfermode mXfermode = new PorterDuffXfermode(
				PorterDuff.Mode.DST_OUT);
		mPaint.setXfermode(mXfermode);

		Bitmap dian1b = ((BitmapDrawable) (TAApplication.getApplication()
				.getResources().getDrawable(R.drawable.dian1))).getBitmap();
		dian1b = Bitmap.createScaledBitmap(dian1b, dian1.getWidth(),
				dian1.getHeight(), true);
		canvas.drawBitmap(dian1b, dian1.getLeft(), dian1.getTop(), mPaint);

		Bitmap dian2b = ((BitmapDrawable) (TAApplication.getApplication()
				.getResources().getDrawable(R.drawable.dian2))).getBitmap();
		dian2b = Bitmap.createScaledBitmap(dian2b, dian2.getWidth(),
				dian2.getHeight(), true);
		canvas.drawBitmap(dian2b, dian2.getLeft(), dian2.getTop(), mPaint);

		Bitmap dian3b = ((BitmapDrawable) (TAApplication.getApplication()
				.getResources().getDrawable(R.drawable.dian3))).getBitmap();
		dian3b = Bitmap.createScaledBitmap(dian3b, dian3.getWidth(),
				dian3.getHeight(), true);
		canvas.drawBitmap(dian3b, ((ViewGroup) (dian3.getParent())).getLeft(),
				((ViewGroup) (dian3.getParent().getParent())).getTop(), mPaint);

		return bm;
	}
}
