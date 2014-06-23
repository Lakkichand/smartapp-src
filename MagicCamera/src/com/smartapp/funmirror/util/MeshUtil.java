package com.smartapp.funmirror.util;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.util.Log;

/**
 * BitmapMesh工具类
 * 
 * @author xiedezhi
 * 
 */
public class MeshUtil {
	private static final int COUNT = 4096;
	private static final int HEIGHT = 63;
	private static final int WIDTH = 63;

	public static float[] getOrigArray(Bitmap bitmap) {
		if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
			return null;
		}
		float bwUtil = bitmap.getWidth() / 63.0f;
		float bhUtil = bitmap.getHeight() / 63.0f;
		float[] ret = new float[8192];
		for (int i = 0; i <= 63; i++) {
			for (int j = 0; j <= 63; j++) {
				ret[i * 128 + j * 2] = i * bwUtil;
				ret[i * 128 + j * 2 + 1] = j * bhUtil;
			}
		}
		return ret;
	}

	private static int exifToDegrees(int exifOrientation) {
		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
			return 90;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			return 180;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
			return 270;
		}
		return 0;
	}

	public static int getBitmapOrientation(Context context, Bitmap bmp) {
		if (bmp == null) {
			return Integer.MIN_VALUE;
		}
		String filePath = context.getCacheDir() + "/tmp/tmp/tmp.jpg";
		boolean b = FileUtil.saveBitmapToSDFile(bmp, filePath,
				CompressFormat.JPEG);
		if (!b) {
			return Integer.MIN_VALUE;
		}

		try {
			ExifInterface exif = new ExifInterface(filePath);
			int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			int rotationInDegrees = exifToDegrees(rotation);
			Log.e("Test", "rotationInDegrees = " + rotationInDegrees);
			return rotationInDegrees;
		} catch (Exception e) {
			e.printStackTrace();
			return Integer.MIN_VALUE;
		}
	}
}
