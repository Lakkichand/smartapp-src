package com.jiubang.ggheart.apps.desks.diy;

import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.widget.ImageView;

/**
 * 内存溢出处理器
 * 
 * @author yuankai
 * @version 1.0
 */
public class OutOfMemoryHandler {
	/**
	 * 回收阈值，目前定为13M左右
	 */
	public final static long TRESHOLD_HEAP_SIZE = 20400000L;

	/**
	 * 内存溢出统一处理类，在此方法中加入对异常的处理
	 */
	public static void handle() {
		// LH 2011.12.1，去掉看看效果
		// System.gc();
	}

	/**
	 * 判断当前所分配的空间，是否达到一个阈值，如果是，则调用GC回收
	 */
	public static void gcIfAllocateOutOfHeapSize() {
		long heapSize = Debug.getNativeHeapAllocatedSize();
		// Log.i("luoph", "heap size = " + heapSize);
		if (heapSize >= TRESHOLD_HEAP_SIZE) {
			handle();
		}
	}

	public static void unbindDrawable(Drawable drawable) {
		if (drawable != null) {
			drawable.setCallback(null);
		}
	}

	public static void unbindImageView(ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			unbindDrawable(drawable);
		}
	}
}
