package com.jiubang.ggheart.appgame.base.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncNetBitmapOperator;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * 应用游戏中心绘制工具类
 * 
 * @author xiedezhi
 * @date [2012-9-27]
 */
public class AppGameDrawUtils {
	/**
	 * 单实例
	 */
	private volatile static AppGameDrawUtils sInstance = null;
	/**
	 * 图标加上遮罩时的缩放系数
	 */
	private final float mScale = 0.95f;
	/**
	 * 实现遮罩层 (mask)
	 */
	private PorterDuffXfermode mXfermode;
	/**
	 * 画布
	 */
	private Canvas mCanvas = null;
	/**
	 * 画笔
	 */
	private Paint mPaint = null;
	/**
	 * 缩放矩阵
	 */
	private Matrix mMatrix = null;
	/**
	 * 遮罩图标处理器
	 */
	public AsyncNetBitmapOperator mMaskIconOperator = new AsyncNetBitmapOperator() {

		@Override
		public Bitmap operateBitmap(Context context, Bitmap imageBitmap) {
			return AppGameDrawUtils.getInstance().createMaskBitmap(GOLauncherApp.getContext(),
					imageBitmap);
		}
	};
	/**
	 * coverflow图片宽度
	 */
	private int mCoverflowWidth;
	/**
	 * coverflow图片高度
	 */
	private int mCoverflowHeight;
	/**
	 * coverflow左右边框宽度
	 */
	private int mCoverflowPaddingLeft;
	/**
	 * coverflow上下边框宽度
	 */
	private int mCoverflowPaddingTop;
	/**
	 * coverflow倒影占原图的高度比例
	 */
	private float mCoverflowReflectHeightScale;
	/**
	 * coverflow倒影渐变开始颜色
	 */
	private int mCoverflowShaderStartColor;
	/**
	 * coverflow倒影渐变结束颜色
	 */
	private int mCoverflowShaderEndColor;
	/**
	 * coverflow生成倒影的处理器
	 */
	public AsyncNetBitmapOperator mCoverflowOperator = new AsyncNetBitmapOperator() {

		@Override
		public Bitmap operateBitmap(Context context, Bitmap imageBitmap) {
			return createReflectionCoverflow(context, imageBitmap);
		}
	};
	/*
	 * 列表底部loading条渐变动画
	 */
	public Animation mCommonProgressAnimation;

	private AppGameDrawUtils() {
		mCanvas = new Canvas();
		mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mMatrix = new Matrix();
		mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

		mCoverflowWidth = DrawUtils.dip2px(172);
		mCoverflowHeight = DrawUtils.dip2px(112);
		mCoverflowPaddingLeft = DrawUtils.dip2px(6f);
		mCoverflowPaddingTop = DrawUtils.dip2px(6.66666f);
		mCoverflowReflectHeightScale = 0.07f;
		mCoverflowShaderStartColor = 0x53ffffff;
		mCoverflowShaderEndColor = 0x06ffffff;
		
		mCommonProgressAnimation = new AlphaAnimation(0, 1);
		mCommonProgressAnimation.setDuration(400);
	}

	public static AppGameDrawUtils getInstance() {
		if (sInstance == null) {
			synchronized (AppGameDrawUtils.class) {
				if (sInstance == null) {
					sInstance = new AppGameDrawUtils();
				}
			}
		}
		return sInstance;
	}

	/**
	 * 应用游戏中心，为图标加上遮罩
	 * 
	 * @param bitmap
	 *            原始bitmap
	 * @return 处理后的bitmap
	 */
	public synchronized Bitmap createMaskBitmap(Context context, Bitmap bitmap) {
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
			mMatrix.reset();
			mPaint.reset();
			drawable = composeIcon(context, base, cover, drawable, mask,
					mCanvas, mMatrix, mPaint, mXfermode, mScale);
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
	 * 应用游戏中心，创建有倒影的coverflow图片
	 * 
	 * @param context
	 * @param bitmap
	 * @return
	 */
	public synchronized Bitmap createReflectionCoverflow(Context context,
			Bitmap bitmap) {
		try {
			try {
				Drawable background = context.getResources().getDrawable(
						R.drawable.coverflow_bg);
				Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, mCoverflowWidth - 2
						* mCoverflowPaddingLeft,
						(int) (mCoverflowHeight - 1.4f * mCoverflowPaddingLeft), false);
				Bitmap retBitmap = Bitmap.createBitmap(mCoverflowWidth,
						mCoverflowHeight, Config.ARGB_8888);
				mCanvas.setBitmap(retBitmap);
				background.setBounds(0, 0, mCoverflowWidth, mCoverflowHeight);
				background.draw(mCanvas);
				int saveId = mCanvas.save();
				mCanvas.drawBitmap(newBitmap, mCoverflowPaddingLeft,
						0.78f * mCoverflowPaddingTop, null);
				mCanvas.restoreToCount(saveId);
				bitmap = retBitmap;
				newBitmap = null;
				retBitmap = null;
				background = null;
			} catch (Throwable e) {
				e.printStackTrace();
			}
			mMatrix.reset();
			mPaint.reset();
			Bitmap ret = createReflectionImageWithOrigin(bitmap,
					mCoverflowReflectHeightScale, mCanvas, mMatrix, mPaint,
					mXfermode, mCoverflowShaderStartColor,
					mCoverflowShaderEndColor);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError error) {
			error.printStackTrace();
		}
		return bitmap;
	}
	
	// -----------------下面是静态方法-----------------------//

	/**
	 * 合成图片
	 * 
	 * @author huyong
	 * @param base
	 *            ：合成图片底图
	 * @param cover
	 *            ：合成图片罩子
	 * @param drawable
	 *            ： 待合成的源图
	 * @param drawable
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
			PorterDuffXfermode xfermode, float scale) {
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
		// 加上mask蒙版
		if (mask != null) {
			paint.setXfermode(xfermode);
			canvas.drawBitmap(mask.getBitmap(), 0, 0, paint);
		}
		return new BitmapDrawable(base);
	}

	/**
	 * 生成一个有倒影的bitmap
	 * 
	 * @param bitmap
	 * @param scaleH
	 *            获得倒影高度比例值;
	 * @return
	 * 
	 * 
	 */
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap,
			float reflectHeightScale, Canvas canvas, Matrix matrix,
			Paint deafalutPaint, PorterDuffXfermode pMode,
			int shaderStartColor, int shaderEndColor) {
		try {
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			int reflectHeight = (int) (height * reflectHeightScale + 0.5);
			matrix.preScale(1, -1);

			Bitmap bitmapWithReflection = Bitmap.createBitmap(width, height
					+ reflectHeight, Config.ARGB_8888);
			canvas.setBitmap(bitmapWithReflection);
			canvas.drawBitmap(bitmap, 0, 0, null);
			LinearGradient shader = new LinearGradient(0, height, 0,
					bitmapWithReflection.getHeight(), shaderStartColor,
					shaderEndColor, TileMode.CLAMP);
			deafalutPaint.setShader(shader);
			deafalutPaint.setXfermode(pMode);
			Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height
					- reflectHeight, width, reflectHeight, matrix, false);
			int saveId = canvas.save();
			canvas.drawBitmap(reflectionImage, 0, height, null);
			canvas.drawRect(0, height, width, bitmapWithReflection.getHeight(),
					deafalutPaint);
			canvas.restoreToCount(saveId);
			return bitmapWithReflection;
		} catch (Throwable e) {
			e.printStackTrace();
			return bitmap;
		}
	}

	/**
	 * 给ImageView设置点击效果,由于ImageView点击效果无法通过setBackgroundDrawable来实现,所以反过来,
	 * 将src当初background
	 * 
	 * @param view
	 *            需要设置的ImageView
	 * @param normalDrawable
	 *            正常显示的图片
	 * @param pressDrawable
	 *            点击效果
	 */
	public static void setImagePressDrawable(Context context, ImageView view,
			Drawable normalDrawable, Drawable pressDrawable) {
		if (context == null || view == null) {
			return;
		}
		if (normalDrawable != null) {
			view.setBackgroundDrawable(normalDrawable);
		}
		StateListDrawable sd = new StateListDrawable();
		sd.addState(new int[] { android.R.attr.state_pressed,
				android.R.attr.state_enabled }, pressDrawable);
		view.setImageDrawable(sd);
	}

}
