package com.smartapp.funmirror.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.View;

public class FileUtil {
	private static final String TAG = "FileUtilBitmapUtility";

	public static boolean sLevelUnder3 = Build.VERSION.SDK_INT < 11;

	/**
	 * Calculates the free memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 * 
	 * @return Number of bytes available.
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * Calculates the total memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 * 
	 * @return Total number of bytes.
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 获取Android中的Linux内核版本号
	 * 
	 */
	public static String getLinuxKernel() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("cat /proc/version");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (null == process) {
			return null;
		}

		// get the output line
		InputStream outs = process.getInputStream();
		InputStreamReader isrout = new InputStreamReader(outs);
		BufferedReader brout = new BufferedReader(isrout, 8 * 1024);
		String result = "";
		String line;

		// get the whole standard output string
		try {
			while ((line = brout.readLine()) != null) {
				result += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (result != "") {
			String Keyword = "version ";
			int index = result.indexOf(Keyword);
			line = result.substring(index + Keyword.length());
			if (null != line) {
				index = line.indexOf(" ");
				return line.substring(0, index);
			}
		}
		return null;
	}

	public static Bitmap readBitmapFromResource(Context context, int rid, Options opts) {
		Bitmap bitmap = null;
		InputStream is = null;
		try {
			is = context.getResources().openRawResource(rid);
			bitmap = BitmapFactory.decodeStream(is, null, opts);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	public static Bitmap readBitmapDIY(String filename, String skey, Options opts) {
		long t1 = System.currentTimeMillis();
		Bitmap bitmap = null;
		InputStream is = null;
		List<Byte> list = new ArrayList<Byte>();
		int key = 0;
		for (int i = 0; i < skey.length(); i++) {
			char c = skey.charAt(i);
			key += c;
		}
		skey = null;
		try {
			is = new FileInputStream(filename);
			byte[] buf = new byte[1024 * 16];
			int read;
			while ((read = is.read(buf)) > -1) {
				for (int i = 0; i < read; i++) {
					byte bt = buf[i];
					list.add((byte) (((int) bt) ^ key));
				}
			}
			buf = null;
			byte[] arr = new byte[list.size()];
			int i = 0;
			for (Byte item : list) {
				arr[i++] = item;
			}
			list.clear();
			list = null;
			bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length, opts);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	public static Bitmap readBitmapFromAsset(Context context, String fileName, Options opts) {
		// TODO 能不能不要生成两个对象
		Bitmap bitmap = null;
		InputStream is = null;
		List<Byte> list = new ArrayList<Byte>();
		int key = 0;
		for (int jj = 0; jj < fileName.length(); jj++) {
			char c = fileName.charAt(jj);
			key += c;
		}
		try {
			is = context.getAssets().open(fileName);
			int read;
			byte[] buf = new byte[1024 * 4];
			while ((read = is.read(buf)) > -1) {
				for (int i = 0; i < read; i++) {
					byte bt = buf[i];
					list.add((byte) (((int) bt) ^ key));
				}
			}
			buf = null;
			byte[] arr = new byte[list.size()];
			int i = 0;
			for (Byte item : list) {
				arr[i++] = item;
			}
			list.clear();
			bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length, opts);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	public static boolean isSDCardAvaiable() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

	public static boolean saveBitmapToSDFile(final Bitmap bitmap, final String filePathName, CompressFormat iconFormat) {
		boolean result = false;
		try {
			createNewFile(filePathName, false);
			OutputStream outputStream = new FileOutputStream(filePathName);
			result = bitmap.compress(iconFormat, 100, outputStream);
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static boolean isFileExist(String filePath) {
		boolean result = false;
		try {
			File file = new File(filePath);
			result = file.exists();
			file = null;
		} catch (Exception e) {
		}
		return result;
	}

	public static File createNewFile(String path, boolean append) {
		File newFile = new File(path);
		if (!append) {
			if (newFile.exists()) {
				newFile.delete();
			}
		}
		if (!newFile.exists()) {
			try {
				File parent = newFile.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
				newFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return newFile;
	}

	public static boolean isInstalled(Context context, String packageName, String versionName) {
		PackageManager manager = context.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(packageName, 0);
			if (info != null) {
				if (versionName == null || "".equals(versionName) || versionName.equals(info.versionName)
						|| "Varies with device".equals(info.versionName)) {
					return true;
				}
			}
		} catch (NameNotFoundException e) {
			// e.printStackTrace();
		}
		return false;
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static boolean isNetworkOK(Context context) {
		boolean result = false;
		if (context != null) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cm != null) {
				NetworkInfo networkInfo = cm.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					result = true;
				}
			}
		}

		return result;
	}

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
				pRet = Bitmap.createBitmap(scaleWidth, scaleHeight, view.isOpaque() ? Config.RGB_565 : Config.ARGB_8888);
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

	public static final Bitmap createScaledBitmap(Bitmap bmp, int scaleWidth, int scaleHeight) {
		Bitmap pRet = null;
		if (null == bmp) {
			Log.i(TAG, "create scale bitmap function param bmp is null");
			return pRet;
		}

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

	public static final boolean saveBitmap(Bitmap bmp, String bmpName) {
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
			boolean bOk = bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
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

	public static Bitmap loadBitmap(Context context, Uri uri) {
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
		int sampleSize = 1;
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
				// OutOfMemoryHandler.handle();
				sampleSize *= 2;
				if (sampleSize > (1 << 10)) {
					bool = false;
				}
			} catch (Throwable e) {
				bool = false;
				Log.i(TAG, e.getMessage());
			} finally {
				try {
					is.close();
				} catch (Exception e2) {
					Log.i(TAG, e2.getMessage());
					Log.i(TAG, "load bitmap close uri stream exception");
				}
			}
		}

		return pRet;
	}

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
				oldbmp = createBitmapFromDrawable(drawable);
			}

			Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象
			float scaleWidth = ((float) w / width); // 计算缩放比例
			float scaleHeight = ((float) h / height);
			matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例
			Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true); // 建立新的
																							// bitmap
																							// ，其内容是对原
																							// bitmap
																							// 的缩放后的图
			matrix = null;
			return new BitmapDrawable(context.getResources(), newbmp); // 把
																		// bitmap
																		// 转换成
																		// drawable
																		// 并返回
		}
		return null;
	}

	public static BitmapDrawable zoomDrawable(Drawable drawable, int w, int h, Resources res) {
		if (drawable != null) {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			Bitmap oldbmp = null;
			// drawable 转换成 bitmap
			if (drawable instanceof BitmapDrawable) {
				// 如果传入的drawable是BitmapDrawable,就不必要生成新的bitmap
				oldbmp = ((BitmapDrawable) drawable).getBitmap();
			} else {
				oldbmp = createBitmapFromDrawable(drawable);
			}
			Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象
			float scaleWidth = ((float) w / width); // 计算缩放比例
			float scaleHeight = ((float) h / height);
			matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例
			Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true); // 建立新的
																							// bitmap
																							// ，其内容是对原
																							// bitmap
																							// 的缩放后的图
			matrix = null;
			return new BitmapDrawable(res, newbmp); // 把 bitmap 转换成 drawable 并返回
		}
		return null;
	}

	public static BitmapDrawable zoomDrawable(Drawable drawable, float wScale, float hScale, Resources res) {
		if (drawable != null) {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			Bitmap oldbmp = null;
			// drawable 转换成 bitmap
			if (drawable instanceof BitmapDrawable) {
				// 如果传入的drawable是BitmapDrawable,就不必要生成新的bitmap
				oldbmp = ((BitmapDrawable) drawable).getBitmap();
			} else {
				oldbmp = createBitmapFromDrawable(drawable);
			}

			Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象
			matrix.postScale(wScale, hScale); // 设置缩放比例
			Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true); // 建立新的
																							// bitmap
																							// ，其内容是对原
																							// bitmap
																							// 的缩放后的图
			matrix = null;
			return new BitmapDrawable(res, newbmp); // 把 bitmap 转换成 drawable 并返回
		}
		return null;
	}

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
			Bitmap newbmp = Bitmap.createBitmap(drawable.getBitmap(), x, y, w, h, matrix, true); // 建立新的
																									// bitmap
																									// ，其内容是对原
																									// bitmap
																									// 的缩放后的图
			matrix = null;
			return new BitmapDrawable(res, newbmp); // 把 bitmap 转换成 drawable 并返回
		}
		return null;
	}

	/**
	 * 有时候会遇到这样的需求，将两个bitmap对象整合并保存为一张图片，代码如下：
	 * 
	 * @param background
	 * @param foreground
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
		cv.drawBitmap(background, 0, 0, paint);// 在 0，0坐标开始画入bg
		// draw fg into
		if (null != foreground) {
			cv.drawBitmap(foreground, 0, 0, paint);// 在 0，0坐标开始画入fg ，可以从任意位置画入
		}
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		// store
		cv.restore();// 存储
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

	public static BitmapDrawable createBitmapDrawableFromDrawable(final Drawable drawable, Context context) {
		Bitmap bitmap = createBitmapFromDrawable(drawable);
		if (bitmap == null) {
			return null;
		}

		BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
		return bitmapDrawable;
	}

	public static Bitmap createBitmapFromDrawable(final Drawable drawable) {

		if (drawable == null) {
			return null;
		}

		Bitmap bitmap = null;
		final int intrinsicWidth = drawable.getIntrinsicWidth();
		final int intrinsicHeight = drawable.getIntrinsicHeight();

		try {
			Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
			bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, config);
		} catch (OutOfMemoryError e) {
			// OutOfMemoryHandler.handle();
			return null;
		}

		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
		drawable.draw(canvas);
		canvas = null;
		return bitmap;
	}

	public static void delFile(String filePath) {
		File file = new File(filePath);
		if (file != null && file.exists()) {// 文件是否存在
			if (file.isFile()) {// 如果是文件
				file.delete();
			} else if (file.isDirectory()) {// 如果是目录
				File[] subFiles = file.listFiles();
				if (subFiles.length == 0) {
					file.delete();
				} else {
					for (int i = 0; i < subFiles.length; i++) {
						File subFile = subFiles[i];
						if (subFile.isDirectory()) {
							delFile(subFile.getAbsolutePath());// 递归调用del方法删除子目录和子文件
						}
						subFile.delete();
					}
				}
			}
		}
	}

	public static int getExifOrientation(String filepath) {
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filepath);
		} catch (IOException ex) {
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				// We only recognize a subset of orientation tag values.
				switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
				}

			}
		}
		return degree;
	}

	public static boolean copyFile(String srcStr, String decStr) {
		// 前提
		File srcFile = new File(srcStr);
		if (!srcFile.exists()) {
			return false;
		}
		File decFile = new File(decStr);
		if (!decFile.exists()) {
			File parent = decFile.getParentFile();
			parent.mkdirs();

			try {
				decFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(srcFile);
			output = new FileOutputStream(decFile);
			byte[] data = new byte[4 * 1024]; // 4k
			while (true) {
				int len = input.read(data);
				if (len <= 0) {
					break;
				}
				output.write(data);
			}
		} catch (Exception e) {
			return false;
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (Exception e2) {
				}
			}
			if (null != output) {
				try {
					output.close();
				} catch (Exception e2) {
				}
			}
		}
		return true;
	}

	public static boolean decryption(String src, int key, String dst) {
		// long t1 = System.currentTimeMillis();
		// TODO 压缩保存?
		File srcFile = new File(src);
		if (!srcFile.exists() || !srcFile.isFile()) {
			return false;
		}
		File decFile = new File(dst);
		if (!decFile.exists()) {
			File parent = decFile.getParentFile();
			parent.mkdirs();
			try {
				decFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(srcFile);
			output = new FileOutputStream(decFile);
			byte[] data = new byte[4 * 1024];
			while (true) {
				int len = input.read(data);
				for (int i = 0; i < len; i++) {
					data[i] = (byte) (((int) data[i]) ^ key);
				}
				if (len <= 0) {
					break;
				}
				output.write(data);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (Exception e2) {
				}
			}
			if (null != output) {
				try {
					output.close();
				} catch (Exception e2) {
				}
			}
		}
		// (System.currentTimeMillis() - t1) + "ms");
		return true;
	}

	public static byte[] Bitmap2Bytes(Bitmap bm) {
		if (bm == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static boolean encrImg(byte[] iconb, String skey, String dest) {
		if (iconb == null || iconb.length == 0) {
			return false;
		}
		int key = 0;
		for (int i = 0; i < skey.length(); i++) {
			char c = skey.charAt(i);
			key += c;
		}
		for (int i = 0; i < iconb.length; i++) {
			iconb[i] = (byte) (((int) iconb[i]) ^ key);
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(dest);
			fos.write(iconb);
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
				}
			}
		}
		return true;
	}
}
