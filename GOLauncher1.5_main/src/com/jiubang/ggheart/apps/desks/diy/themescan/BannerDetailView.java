package com.jiubang.ggheart.apps.desks.diy.themescan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.data.theme.bean.SpecThemeViewConfig;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-10-18]
 */
public class BannerDetailView extends RelativeLayout implements View.OnClickListener {

	private LayoutInflater mInflater;
	private ThemeContainer mBannerThemeContainer; // 专题主题的容器
	private int mWidth;
	private int mHeight;
	private int mId; //分类主题ID
	private Bitmap mBackground;
	private int mBackgroundColor;

	public BannerDetailView(Context context, int id) {
		super(context);
		// TODO Auto-generated constructor stub
		// 初始化界面
		mInflater = LayoutInflater.from(context);
		mId = id;
		initView();
		startLoadThemeData();
	}

	public void startLoadThemeData() {
		// TODO Auto-generated method stub
		mBannerThemeContainer.loadSpecThemeData(mId);
	}

	public BannerDetailView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	private void initView() {
		if (mBannerThemeContainer == null) {
			initThemeContainer();
		}
	}

	private void initThemeContainer() {
		mBannerThemeContainer = (ThemeContainer) mInflater.inflate(
				R.layout.theme_manage_container_layout, null);
		this.addView(mBannerThemeContainer);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	public void changeOrientation() {
		if (mBannerThemeContainer != null) {
			mBannerThemeContainer.changeOrientation();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth = MeasureSpec.getSize(widthMeasureSpec);
		mHeight = MeasureSpec.getSize(heightMeasureSpec);

	}

	public void onDestory() {
		if (mBannerThemeContainer != null) {
			mBannerThemeContainer.onDestroy();
		}
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
			mBannerThemeContainer.setBackgroundDrawable(new BitmapDrawable(mBackground));
		}
	}
	private void setMyBackground(int color) {
		mBackgroundColor = color;
		if (mBackground == null) {
			mBannerThemeContainer.setBackgroundColor(mBackgroundColor);
		}
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
