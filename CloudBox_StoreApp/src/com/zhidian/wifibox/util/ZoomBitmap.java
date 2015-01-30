package com.zhidian.wifibox.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ZoomBitmap {
	/**
	 * 
	 * Drawable-->Bitmap
	 * 
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;

	}	
	

	/**
	 * 
	 * 不同屏幕，获取字体大小
	 * 
	 * 1. 在视图的 onsizechanged里获取视图宽度，一般情况下默认宽度是320，所以计算一个缩放比率 rate = (float)
	 * w/320 w是实际宽度 2.然后在设置字体尺寸时 paint.setTextSize((int)(8*rate)); 8是在分辨率宽为320
	 * 下需要设置的字体大小 实际字体大小 = 默认字体大小 x rate
	 */

	public static int changeDMSize(Context context, int size) {
		InfoUtil infor = new InfoUtil(context);
		int screenWidth = infor.getWidth();
		int screenHeight = infor.getHeight();
		screenWidth = screenWidth < screenHeight ? screenWidth : screenHeight;
		int rate = (int) (size * (float) screenWidth / 480); // 我自己测试这个倍数比较适合，当然你可以测试后再修改
		Log.d("ZoomBitmap", "textSize:" + rate + "screenWidth:" + screenWidth
				+ "screenHeight: " + screenHeight);
		return rate < 16 ? 16 : rate; // 字体太小也不好看的
	}

}
