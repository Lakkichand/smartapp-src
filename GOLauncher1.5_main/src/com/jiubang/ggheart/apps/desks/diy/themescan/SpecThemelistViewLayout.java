package com.jiubang.ggheart.apps.desks.diy.themescan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.jiubang.ggheart.data.theme.bean.SpecThemeViewConfig;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-21]
 */
public class SpecThemelistViewLayout extends RelativeLayout {
	private int mWidth;
	private int mHeight;
	private Bitmap mBackground;
	public SpecThemelistViewLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SpecThemelistViewLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SpecThemelistViewLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth = MeasureSpec.getSize(widthMeasureSpec);
		mHeight = MeasureSpec.getSize(heightMeasureSpec);
	}
	private void setMyBackground(Drawable drawable) {
		if (drawable == null) {
			return;
		}
		if (mBackground == null || mBackground.getWidth() != mWidth
				|| mBackground.getHeight() != mHeight) {
			mBackground = combinBackground(drawable);
		}

		if (mBackground != null) {
			setBackgroundDrawable(new BitmapDrawable(mBackground));
		}
	}
	private void setMyBackground(int color) {
		setBackgroundColor(color);
	}
	public Bitmap combinBackground(Drawable drawable) {
		Bitmap bmp = null;
		if (drawable != null && mWidth > 0 && mHeight > 0) {
			Bitmap cell = ((BitmapDrawable) drawable).getBitmap();
			int w = drawable.getIntrinsicWidth();
			int h = drawable.getIntrinsicHeight();
			if (w > 0 && h > 0) {
				int wCount = mWidth / w + (mWidth % w > 0 ? 1 : 0);
				int hCount = mHeight / h + (mHeight % h > 0 ? 1 : 0);
				Bitmap bg = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
				Canvas canvas = new Canvas(bg);
				for (int i = 0; i < wCount; i++) {
					for (int j = 0; j < hCount; j++) {
						canvas.drawBitmap(cell, i * w, j * h, null);
					}
				}
				bmp = bg;
			}
		}

		return bmp;
	}

	public void configView(SpecThemeViewConfig config) {
		if (config.mListViewBgImgDrawable != null) {
			setMyBackground(config.mListViewBgImgDrawable);
		} else if (config.mListViewBgColor != -1) {
			setMyBackground(config.mListViewBgColor);
		}
	}
}
