package com.jiubang.ggheart.screen.back;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * 
 * @author jiangxuwen
 *
 */
public interface IWallpaperDrawer {
	
	public void updateXY(int x, int y);
	
	public void updateOffsetX(int offsetX, boolean drawCycloid);
	
	public void updateOffsetY(int offsetY, boolean drawCycloid);
	
	public void updateScreen(int newScreen, int oldScreen);
	
	public void doDraw(Canvas canvas, int bgX, int bgY);
	
	public void drawBackground(Canvas canvas);

	public void setBackground(Drawable drawable, Bitmap bitmap);
	
	public Drawable getBackgroundDrawable();
	
	public Bitmap getBackgroundBitmap();
	
	public void setAlpha(int alpha);

	public void setCycloidDrawListener(CycloidDrawListener listener);
	
	public void setUpdateBackground(boolean bool);
	
	public boolean needUpdateBackground();
	
	public void setMiddleScrollEnabled(boolean enable);
}
