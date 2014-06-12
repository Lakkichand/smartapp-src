package com.jiubang.ggheart.apps.desks.snapshot;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;

/**
 * 
 * @author dengdazhong
 *
 */
public class RootScreenshot {
	private static boolean sIsEmpty = true;
	// Motorola Milestone may return 5 for Display.getPixelFormat.
	private static final int BGRA_8888 = 5;

//	public static boolean saveScreenShot(Context context, String path) throws Exception {
//		Bitmap bitmap = getScreenBitmap(context);
//		File file = new File(path);
//		if (!file.exists()) {
//			file.createNewFile();
//		}
//		FileOutputStream fout = new FileOutputStream(file);
//		bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fout);
//		fout.flush();
//		fout.close();
//		bitmap.recycle();
//		//end = System.currentTimeMillis();
//		//Log.d("RootScreenshot", "save time: " + (end - start));
//		return true;
//	}

	public static boolean saveScreenShot(Context context, String path, Bitmap bitmap)
			throws Exception {
		File file = new File(path);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fout = new FileOutputStream(file);
		bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fout);
		fout.flush();
		fout.close();
		//end = System.currentTimeMillis();
		//Log.d("RootScreenshot", "save time: " + (end - start));
		return true;
	}

	public static Bitmap getScreenBitmap(Context context, float scale) throws Exception {
		sIsEmpty = true;
		//long start = System.currentTimeMillis();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		int width = GoLauncher.isPortait() ? metrics.widthPixels : metrics.heightPixels;
		int height = GoLauncher.isPortait() ? metrics.heightPixels : metrics.widthPixels;
		if (width > height) {
			int tmp = width;
			width = height;
			height = tmp;
		}
		int format = display.getPixelFormat();
		PixelFormat info = new PixelFormat();
		PixelFormat.getPixelFormatInfo(format, info);
		int depth = info.bytesPerPixel;
		byte[] buffer = new byte[width * height * depth];
		Process process = Runtime.getRuntime().exec("cat /dev/graphics/fb0\n");
		InputStream is = process .getInputStream();
		DataInputStream dis = new DataInputStream(is);
		dis.readFully(buffer);
		dis.close();
		int[] colors = new int[width * height];
		convertColors(buffer, colors, format);
		if (sIsEmpty) {
			return null;
		}
		buffer = null;
		return Bitmap.createBitmap(colors, width, height, Config.ARGB_8888);
	}
	
	/**
	 * 非root截屏，只能截自己的
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getScreenBitmapWithoutRoot(Context context, float scale) {
		if (GoLauncher.getContext() == null) {
			return null;
		}
		Bitmap bitmap = null;
		try {
			Rect rect = new Rect();
			((Activity) GoLauncher.getContext()).getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
			int width = (int) (GoLauncher.getScreenWidth() * scale);
			int height = (int) (GoLauncher.getScreenHeight() * scale);
			if (width > height) {
				int tmp = width;
				width = height;
				height = tmp;
			}
			if (GoLauncher.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				int tmp = width;
				width = height;
				height = tmp;
			}
			int statusBarHeight = rect.top > 0 ? rect.top : 0;
			bitmap = Bitmap.createBitmap(width, (int) (height - (statusBarHeight * scale)),
					Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			canvas.scale(scale, scale);
			GoLauncher.sendMessage(context, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.SNAPSHOT_DRAW_THROUND_LAUNCHER, -1, canvas, null);
			canvas = null;
		} catch (OutOfMemoryError e) {
			Log.v("snapshot_error", "getScreenBitmapWithoutRoot()" + e.getMessage());
			throw e;
		}
		return bitmap;
	}

//	public static Bitmap getScreenBitmapWithoutRoot(Context context, float scale) {
//		Bitmap bitmap = null;
//		View windowView = ((Activity) GoLauncher.getContext()).getWindow().getDecorView().getRootView();
//		if (windowView != null) {
//			boolean isDrawingCacheEnabled = windowView.isDrawingCacheEnabled();
//			try {
//				windowView.setDrawingCacheEnabled(true);
//				windowView.destroyDrawingCache();
//				Bitmap cache = windowView.getDrawingCache();
//				if (cache != null) {
//					int width = (int) (cache.getWidth() * scale);
//					int height = (int) (cache.getHeight() * scale);
//					bitmap = Bitmap.createScaledBitmap(cache, width, height, false);
//				}
//			} catch (OutOfMemoryError e) {
//				Log.v("snapshot_error", "getScreenBitmapWithoutRoot()" + e.getMessage());
//				throw e;
//			}finally{
//				windowView.setDrawingCacheEnabled(isDrawingCacheEnabled);				
//			}
//		
//		}
//		return bitmap;
//	}
	
	private static int constructInt(byte[] oct, int start) {
		int value = 0;
		int i0 = oct[start] & 0xff;
		int i1 = oct[start + 1] & 0xff;
		int i2 = oct[start + 2] & 0xff;
		int i3 = oct[start + 3] & 0xff;
		value = (i3 << 24) | (i2 << 16) | (i1 << 8) | i0;
		return value;
	}

	private static int constructInt3(byte[] oct, int start) {
		int value = 0;
		int i0 = oct[start] & 0xff;
		int i1 = oct[start + 1] & 0xff;
		int i2 = oct[start + 2] & 0xff;
		value = 0xff000000 | (i2 << 16) | (i1 << 8) | i0;
		return value;
	}

	private static short constructShort(byte[] oct, int start) {
		short value = 0;
		int i0 = oct[start] & 0xff;
		int i1 = oct[start + 1] & 0xff;
		value = (short) (i1 << 8 | i0);
		return value;
	}

	private static void convertColors(byte[] buffer, int[] colors, int format) {
		if (format == BGRA_8888) {
			convertColorsBGRA8888(buffer, colors);
		} else if (format == PixelFormat.RGBA_8888) {
			convertColorsRGBA8888(buffer, colors);
		} else if (format == PixelFormat.RGBX_8888) {
			convertColorsRGBX8888(buffer, colors);
		} else if (format == PixelFormat.RGB_888) {
			convertColorsRGB888(buffer, colors);
		} else if (format == PixelFormat.RGB_565) {
			convertColorsRGB565(buffer, colors);
		} else if (format == PixelFormat.RGBA_4444) {
			convertColorsRGBA4444(buffer, colors);
		} else if (format == PixelFormat.RGBA_5551) {
			convertColorsRGBA5551(buffer, colors);
		} else {
			// Is there such a display format in real world for Android device ? Ignore it.
		}
	}

	private static void convertColorsBGRA8888(byte[] buffer, int[] colors) {
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			int value = constructInt(buffer, 4 * i);
			colors[i] = value;
			if (value != 0) {
				sIsEmpty = false;
			}
		}
	}

	private static void convertColorsRGBA8888(byte[] buffer, int[] colors) {
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			int value = constructInt(buffer, 4 * i);
			int alpha = value & 0xff000000;
			int red = (value & 0x000000ff) << 16;
			int green = value & 0x0000ff00;
			int blue = (value & 0x00ff0000) >> 16;
			int color = alpha | red | green | blue;
			colors[i] = color;
			if (color != 0) {
				sIsEmpty = false;
			}
		}
	}

	private static void convertColorsRGBX8888(byte[] buffer, int[] colors) {
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			int value = constructInt(buffer, 4 * i);
			int alpha = 0xff000000;
			int red = (value & 0x000000ff) << 16;
			int green = value & 0x0000ff00;
			int blue = (value & 0x00ff0000) >> 16;
			int color = alpha | red | green | blue;
			colors[i] = color;
			if (color != 0) {
				sIsEmpty = false;
			}
		}
	}

	private static void convertColorsRGB888(byte[] buffer, int[] colors) {
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			int value = constructInt3(buffer, 3 * i);
			int alpha = 0xff000000;
			int red = (value & 0x000000ff) << 16;
			int green = value & 0x0000ff00;
			int blue = (value & 0x00ff0000) >> 16;
			int color = alpha | red | green | blue;
			colors[i] = color;
			if (color != 0) {
				sIsEmpty = false;
			}
		}
	}

	private static void convertColorsRGB565(byte[] buffer, int[] colors) {
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			short value = constructShort(buffer, 2 * i);
			int alpha = 0xff000000;
			int red = (value & 0xf800) << 8;
			int green = (value & 0x07e0) << 5;
			int blue = (value & 0x001f) << 3;
			int color = alpha | red | green | blue;
			colors[i] = color;
			if (color != 0) {
				sIsEmpty = false;
			}
		}
	}

	private static void convertColorsRGBA4444(byte[] buffer, int[] colors) {
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			short value = constructShort(buffer, 2 * i);
			int alpha = (value & 0xf000) << 16;
			int red = (value & 0x0f00) << 12;
			int green = (value & 0x00f0) << 8;
			int blue = (value & 0x000f) << 4;
			int color = alpha | red | green | blue;
			colors[i] = color;
			if (color != 0) {
				sIsEmpty = false;
			}
		}
	}

	private static void convertColorsRGBA5551(byte[] buffer, int[] colors) {
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			short value = constructShort(buffer, 2 * i);
			int alpha = (value & 0x8000) != 0 ? 0xff000000 : 0x00000000;
			int red = (value & 0x7c00) << 9;
			int green = (value & 0x03e0) << 6;
			int blue = (value & 0x001f) << 3;
			int color = alpha | red | green | blue;
			colors[i] = color;
			if (color != 0) {
				sIsEmpty = false;
			}
		}
	}
}
