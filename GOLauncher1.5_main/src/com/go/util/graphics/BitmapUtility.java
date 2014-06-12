package com.go.util.graphics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.go.util.Utilities;

/**
 * 
 * 类描述:bitmap处理工具类
 * 功能详细描述:
 * 
 * @author  huyong
 * @date  [2012-8-25]
 */
public class BitmapUtility {
	private static final String TAG = "BitmapUtility";

	/**
	 * 功能简述:创建一张当前的view的bitmap截图
	 * 功能详细描述:根据指定的缩放比例，对当前view进行截图，并返回截图bitmap
	 * 注意:
	 * @param view：待画的view
	 * @param scale：缩放比例
	 * @return：view的截图，若当前view为null或宽高<=0，则返回null。
	 */
	public static final Bitmap createBitmap(View view, float scale) {
		Bitmap pRet = null;
		if (null == view) {
			Log.i(TAG, "create bitmap function param view is null");
			return pRet;
		}

		int scaleWidth = (int) (view.getWidth() * scale);
		int scaleHeight = (int) (view.getHeight() * scale);
		if (scaleWidth <= 0 || scaleHeight <= 0) {
			Log.i(TAG, "create bitmap function param view is not layout");
			return pRet;
		}

		boolean bViewDrawingCacheEnable = view.isDrawingCacheEnabled();
		if (!bViewDrawingCacheEnable) {
			view.setDrawingCacheEnabled(true);
		}
		try {
			Bitmap viewBmp = view.getDrawingCache(true);
			// 如果拿到的缓存为空
			if (viewBmp == null) {
				pRet = Bitmap.createBitmap(scaleWidth, scaleHeight, view.isOpaque()
						? Config.RGB_565
						: Config.ARGB_8888);
				Canvas canvas = new Canvas(pRet);
				canvas.scale(scale, scale);
				view.draw(canvas);
				canvas = null;
			} else {
				pRet = Bitmap.createScaledBitmap(viewBmp, scaleWidth, scaleHeight, true);
			}
			viewBmp = null;
		} catch (OutOfMemoryError e) {
			pRet = null;
			Log.i(TAG, "create bitmap out of memory");
		} catch (Exception e) {
			pRet = null;
			Log.i(TAG, "create bitmap exception");
		}
		if (!bViewDrawingCacheEnable) {
			view.setDrawingCacheEnabled(false);
		}

		return pRet;
	}

	/**
	 * 功能简述:创建一张已有bmp居中显示的指定宽高的新Bitmap
	 * 功能详细描述:需要传入已有bmp、新创建Bitmap的宽、高，三个条件，从而创建一张新的Bitmap，使得传入的bmp位于新Bitmap的居中显示。
	 * 注意:新创建的Bitmap的宽高，因不小于原有bmp的宽高。
	 * @param bmp：已有将要拿来居中显示的位图。
	 * @param desWidth：新创建位图的宽度
	 * @param desHeight：新创建位图的高度
	 * @return
	 */
	public static final Bitmap createBitmap(Bitmap bmp, int desWidth, int desHeight) {
		Bitmap pRet = null;
		if (null == bmp) {
			Log.i(TAG, "create bitmap function param bmp is null");
			return pRet;
		}

		try {
			pRet = Bitmap.createBitmap(desWidth, desHeight, Config.ARGB_8888);
			Canvas canvas = new Canvas(pRet);
			int left = (desWidth - bmp.getWidth()) / 2;
			int top = (desHeight - bmp.getHeight()) / 2;
			canvas.drawBitmap(bmp, left, top, null);
			canvas = null;
		} catch (OutOfMemoryError e) {
			pRet = null;
			Log.i(TAG, "create bitmap out of memory");
		} catch (Exception e) {
			pRet = null;
			Log.i(TAG, "create bitmap exception");
		}

		return pRet;
	}

	/**
	 * 功能简述:创建缩放处理后的新图，若缩放后大小与原图大小相同，则直接返回原图。
	 * 功能详细描述:
	 * 注意:若缩放目标尺寸与原图尺寸相等，则直接返回原图，不再创建新的bitmap
	 * @param bmp：待处理bmp
	 * @param scaleWidth：缩放目标宽
	 * @param scaleHeight：缩放目标高
	 * @return
	 */
	public static final Bitmap createScaledBitmap(Bitmap bmp, int scaleWidth, int scaleHeight) {
		Bitmap pRet = null;
		if (null == bmp) {
			Log.i(TAG, "create scale bitmap function param bmp is null");
			return pRet;
		}
		// 这里有待改进，这里直接返回原图，有可能原图会在后面recycle，导致创建出来的都会被recycle
		if (scaleWidth == bmp.getWidth() && scaleHeight == bmp.getHeight()) {
			return bmp;
		}

		try {
			pRet = Bitmap.createScaledBitmap(bmp, scaleWidth, scaleHeight, true);
		} catch (OutOfMemoryError e) {
			pRet = null;
			Log.i(TAG, "create scale bitmap out of memory");
		} catch (Exception e) {
			pRet = null;
			Log.i(TAG, "create scale bitmap exception");
		}

		return pRet;
	}

	/**
	 * 功能简述:将位图保存为指定文件名的文件。
	 * 功能详细描述:
	 * 注意:若已存在同名文件，则首先删除原有文件，若删除失败，则直接退出，保存失败
	 * @param bmp：待保存位图
	 * @param bmpName：保存位图内容的目标文件路径
	 * @return true for 保存成功，false for 保存失败。
	 */
	public static final boolean saveBitmap(Bitmap bmp, String bmpName) {
		return saveBitmap(bmp, bmpName, Bitmap.CompressFormat.PNG);
	}

	public static final boolean saveBitmap(Bitmap bmp, String bmpName, CompressFormat format) {
		if (null == bmp) {
			Log.i(TAG, "save bitmap to file bmp is null");
			return false;
		}
		FileOutputStream stream = null;
		try {
			File file = new File(bmpName);
			if (file.exists()) {
				boolean bDel = file.delete();
				if (!bDel) {
					Log.i(TAG, "delete src file fail");
					return false;
				}
			} else {
				File parent = file.getParentFile();
				if (null == parent) {
					Log.i(TAG, "get bmpName parent file fail");
					return false;
				}
				if (!parent.exists()) {
					boolean bDir = parent.mkdirs();
					if (!bDir) {
						Log.i(TAG, "make dir fail");
						return false;
					}
				}
			}
			boolean bCreate = file.createNewFile();
			if (!bCreate) {
				Log.i(TAG, "create file fail");
				return false;
			}
			stream = new FileOutputStream(file);
			boolean bOk = bmp.compress(format, 100, stream);

			if (!bOk) {
				Log.i(TAG, "bitmap compress file fail");
				return false;
			}
		} catch (Exception e) {
			Log.i(TAG, e.toString());
			return false;
		} finally {
			if (null != stream) {
				try {
					stream.close();
				} catch (Exception e2) {
					Log.i(TAG, "close stream " + e2.toString());
				}
			}
		}
		return true;
	}

	/**
	 * 功能简述:根据指定的图片文件的uri，创建图片。
	 * 功能详细描述:
	 * 注意:
	 * @param context
	 * @param uri：目标图片文件的uri
	 * @return
	 */
	public static Bitmap loadBitmap(Context context, Uri uri, int simpleSize) {
		Bitmap pRet = null;
		if (null == context) {
			Log.i(TAG, "load bitmap context is null");
			return pRet;
		}
		if (null == uri) {
			Log.i(TAG, "load bitmap uri is null");
			return pRet;
		}

		InputStream is = null;
		int sampleSize = simpleSize;
		Options opt = new Options();

		boolean bool = true;
		while (bool) {
			try {
				is = context.getContentResolver().openInputStream(uri);
				opt.inSampleSize = sampleSize;
				pRet = null;
				pRet = BitmapFactory.decodeStream(is, null, opt);
				bool = false;
			} catch (OutOfMemoryError e) {
				sampleSize *= 2;
				if (sampleSize > (1 << 10)) {
					bool = false;
				}
			} catch (Throwable e) {
				bool = false;
				Log.i(TAG, e.getMessage());
			} finally {
				try {
					if (is != null) {
						is.close();
					}
				} catch (Exception e2) {
					Log.i(TAG, e2.getMessage());
					Log.i(TAG, "load bitmap close uri stream exception");
				}
			}
		}

		return pRet;
	}

	/**
	 * 功能简述:对指定drawable进行指定的高宽缩放后，创建一张新的BitmapDrawable。
	 * 功能详细描述:
	 * 注意:
	 * @param context
	 * @param drawable：待处理的drawable
	 * @param w:期望缩放后的BitmapDrawable的宽
	 * @param h：期望缩放后的BitmapDrawable的高
	 * @return 经缩放处理后的新的BitmapDrawable
	 */
	public static BitmapDrawable zoomDrawable(Context context, Drawable drawable, int w, int h) {
		if (drawable != null) {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			Bitmap oldbmp = null;
			// drawable 转换成 bitmap
			if (drawable instanceof BitmapDrawable) {
				// 如果传入的drawable是BitmapDrawable,就不必要生成新的bitmap
				oldbmp = ((BitmapDrawable) drawable).getBitmap();
			} else {
				oldbmp = Utilities.createBitmapFromDrawable(drawable);
			}

			Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象
			float scaleWidth = (float) w / width; // 计算缩放比例
			float scaleHeight = (float) h / height;
			matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例

			//建立新的bitmap，其内容是对原bitmap的缩放后的图
			Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);
			matrix = null;

			//把bitmap转换成drawable并返回
			return new BitmapDrawable(context.getResources(), newbmp);
		}
		return null;
	}

	/**
	 * 功能简述:
	 * 功能详细描述:
	 * 注意:
	 * @param drawable
	 * @param wScale
	 * @param hScale
	 * @param res
	 * @return
	 */
	public static BitmapDrawable zoomDrawable(Drawable drawable, float wScale, float hScale,
			Resources res) {
		if (drawable != null) {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			Bitmap oldbmp = null;
			// drawable 转换成 bitmap
			if (drawable instanceof BitmapDrawable) {
				// 如果传入的drawable是BitmapDrawable,就不必要生成新的bitmap
				oldbmp = ((BitmapDrawable) drawable).getBitmap();
			} else {
				oldbmp = Utilities.createBitmapFromDrawable(drawable);
			}

			Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象
			matrix.postScale(wScale, hScale); // 设置缩放比例

			//建立新的bitmap，其内容是对原bitmap的缩放后的图
			Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);
			matrix = null;

			// 把 bitmap 转换成 drawable 并返回
			return new BitmapDrawable(res, newbmp);
		}
		return null;
	}

	/**
	 * 功能简述:
	 * 功能详细描述:
	 * 注意:
	 * @param drawable
	 * @param w
	 * @param h
	 * @param res
	 * @return
	 */
	public static BitmapDrawable clipDrawable(BitmapDrawable drawable, int w, int h, Resources res) {
		if (drawable != null) {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			if (width < w) {
				w = width;
			}
			if (height < h) {
				h = height;
			}
			int x = (width - w) >> 1;
			int y = (height - h) >> 1;
			Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象

			//建立新的bitmap，其内容是对原bitmap的缩放后的图
			Bitmap newbmp = Bitmap.createBitmap(drawable.getBitmap(), x, y, w, h, matrix, true);
			matrix = null;
			// 把 bitmap 转换成 drawable 并返回
			return new BitmapDrawable(res, newbmp);
		}
		return null;
	}

	/**
	 * 重叠合并两张图片，合并后的大小等同于作为底图的图片大小
	 * 
	 * @param background：下层图，即底图
	 * @param foreground：上层图，即前置图
	 * @return 合并后的Bitmap
	 */
	public static Bitmap toConformBitmap(Bitmap background, Bitmap foreground, Paint paint) {
		if (null == background) {
			return null;
		}

		int bgWidth = background.getWidth();
		int bgHeight = background.getHeight();
		// int fgWidth = foreground.getWidth();
		// int fgHeight = foreground.getHeight();
		// create the new blank bitmap 创建一个新的和SRC长度宽度一样的位图
		Bitmap newbmp = null;
		try {
			newbmp = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
		} catch (OutOfMemoryError e) {
			// OOM,return null
			return null;
		}
		Canvas cv = new Canvas(newbmp);
		// draw bg into
		cv.drawBitmap(background, 0, 0, paint); // 在 0，0坐标开始画入bg
		// draw fg into
		if (null != foreground) {
			cv.drawBitmap(foreground, 0, 0, paint); // 在 0，0坐标开始画入fg ，可以从任意位置画入
		}
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG); // 保存
		// store
		cv.restore(); // 存储
		return newbmp;
	}

	/**
	 * 对图标进行灰色处理
	 * 
	 * @param srcDrawable
	 *            源图
	 * @return 非彩色的图片
	 */
	public static Drawable getNeutralDrawable(Drawable srcDrawable) {
		if (srcDrawable != null) {
			ColorMatrix colorMatrix = new ColorMatrix();
			colorMatrix.setSaturation(0f);
			// 设为黑白
			srcDrawable.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
			// 设为阴影
			// srcDrawable.setColorFilter(new
			// PorterDuffColorFilter(0x87000000,PorterDuff.Mode.SRC_ATOP));
			return srcDrawable;
		}
		return null;
	}

	/**
	 * 还原图标
	 * 
	 * @param 灰阶处理过得图标
	 * @return 原来的图标
	 */
	public static Drawable getOriginalDrawable(Drawable neturalDrawable) {
		if (neturalDrawable != null) {
			neturalDrawable.setColorFilter(null);
			return neturalDrawable;
		}
		return null;
	}

	public static Drawable composeDrawableText(Context context, Drawable src, String text,
			int textSize) {
		if (src == null) {
			return null;
		}
		if (text == null) {
			return src;
		}
		try {
			Bitmap srcBitmap = null;
			if (src instanceof BitmapDrawable) {
				srcBitmap = ((BitmapDrawable) src).getBitmap();
			} else {
				srcBitmap = createBitmapFromDrawable(src);
			}
			if (srcBitmap == null) {
				return null;
			}
			int width = srcBitmap.getWidth();
			int height = srcBitmap.getHeight();
			Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(temp);
			canvas.drawBitmap(srcBitmap, 0, 0, null);

			Paint paint = new Paint();
			paint.setTextSize(textSize);
			paint.setStyle(Style.FILL_AND_STROKE);
			paint.setColor(Color.WHITE);
			paint.setAntiAlias(true); // 抗锯齿
			paint.setTextAlign(Paint.Align.CENTER);
			int size = text.length();
			int length = (int) paint.measureText(text);
			int center = length / size / 2;
			int offX = width / 2;
			int offY = height / 2 + center + 1;
			canvas.drawText(text, offX, offY, paint);

			return new BitmapDrawable(context.getResources(), temp);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	public static Bitmap createBitmapFromDrawable(final Drawable drawable) {
		if (drawable == null) {
			return null;
		}

		Bitmap bitmap = null;
		final int intrinsicWidth = drawable.getIntrinsicWidth();
		final int intrinsicHeight = drawable.getIntrinsicHeight();

		try {
			Config config = drawable.getOpacity() != PixelFormat.OPAQUE
					? Bitmap.Config.ARGB_8888
					: Bitmap.Config.RGB_565;
			bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, config);
		} catch (OutOfMemoryError e) {
			return null;
		}

		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
		drawable.draw(canvas);
		canvas = null;
		return bitmap;
	}
}
