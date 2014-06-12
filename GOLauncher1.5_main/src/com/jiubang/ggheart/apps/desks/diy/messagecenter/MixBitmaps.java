package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

/**
 * 
 * 类描述:  消息中心强制弹出框的背景创建工具类
 * @author  shenjinbao
 * @date  [2012-9-28]
 */
public class MixBitmaps {

	/**
	 * 将多个Bitmap合并成一个图片。
	 * 
	 * @param int 将多个图合成多少列
	 * @param Bitmaps
	 *            [][] 要合成的图片
	 * @return
	 */
	public static Bitmap combineBitmaps(int columns, Bitmap[][] bitmaps, int width, int height) {

		if (columns <= 0 || bitmaps == null || bitmaps.length == 0) {
			throw new IllegalArgumentException(
					"Wrong parameters: columns must > 0 and bitmaps.length must > 0.");
		}

		int rows = bitmaps.length;
		Bitmap newBitmap = Bitmap.createBitmap(width, height, Config.ARGB_4444);

		int wholeHeight = 0;
		int wholeWidth = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				newBitmap = mixtureBitmap(newBitmap, bitmaps[i][j], new PointF(wholeWidth,
						wholeHeight));
				wholeWidth = wholeWidth + bitmaps[i][j].getWidth();
			}
			wholeWidth = 0;
			wholeHeight = bitmaps[i][0].getHeight() + wholeHeight;
		}
		return newBitmap;
	}

	/**
	 * Mix two Bitmap as one.
	 * 
	 * @return
	 */
	public static Bitmap mixtureBitmap(Bitmap background, Bitmap bitmap, PointF fromPoint) {
		if (background == null || bitmap == null || fromPoint == null) {
			return null;
		}
		Canvas cv = new Canvas(background);
		cv.drawBitmap(bitmap, fromPoint.x, fromPoint.y, null);
		cv.save(Canvas.ALL_SAVE_FLAG);
		cv.restore();
		return background;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

}
