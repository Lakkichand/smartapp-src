package com.jiubang.ggheart.appgame.gostore.base.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher.ViewFactory;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>
 * 类描述:Go精品主题详情gallery图片展示区域 <br>
 * 功能详细描述:
 * 
 * @author lijunye
 * @date [2012-10-29]
 */
public class GoStoreDetailGalleryImage extends RelativeLayout {
	/**
	 * 获取图片结束
	 */
	private static final int FINISH = 0;
	/**
	 * 加载页面
	 */
	private RelativeLayout mLoadingLayout = null;
	/**
	 * 加载失败，重试页面
	 */
	private RelativeLayout mRetryLayout = null;
	/**
	 * 重试按钮
	 */
	private RelativeLayout mRetryText = null;
	/**
	 * 显示图片
	 */
	private ImageSwitcher mImageView = null;
	/**
	 * 图片下载地址（或id）
	 */
	
//	private UiHandler mHandler = null;
	private AsyncImageManager mImgManager;
	
	private int mBitmapWidth = -1;
	private int mBitmapHeight = -1;
	private int mPadding = GoStoreDisplayUtil.scalePxToMachine(getContext(), 6);
	
	public boolean mIsRetry = false;
	private boolean mBitmapRecycled = false;
	
	private Drawable mDefaultIcon = null;
	private Bitmap mBmp = null;
	private String mUrl = null;
	
	
	public GoStoreDetailGalleryImage(Context context) {
		super(context);
	}

	public GoStoreDetailGalleryImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mImgManager = AsyncImageManager.getInstance();
		mDefaultIcon = getResources().getDrawable(R.drawable.appcenter_feature_default_banner);
	}

	/**
	 * <br>功能简述:初始化加载页面
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initLoadingView() {
		if (mLoadingLayout == null) {
			mLoadingLayout = (RelativeLayout) this
					.findViewById(R.id.loading_View);
		}
	}
	
	/**
	 * <br>功能简述:外部调用，获得加载页面
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void getProgressView() {
		if (mLoadingLayout == null) {
			mLoadingLayout = (RelativeLayout) this.findViewById(R.id.loading_View);
		}
		setLoadingViewVisibility(VISIBLE);
		setReryViewVisibility(GONE);
		setImgVisibility(GONE);
	}
	
	/**
	 * <br>功能简述:初始化加载失败，重试页面
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initRetryView() {
		if (mRetryLayout == null) {
			mRetryLayout = (RelativeLayout) this.findViewById(R.id.retry_View);
			mRetryText = (RelativeLayout) this
					.findViewById(R.id.retry_btn_text);
			if (mRetryText != null) {
				mRetryText.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						int action = event.getAction();
						switch (action) {
						case MotionEvent.ACTION_MOVE:
							return false;
						case MotionEvent.ACTION_UP:
							break;
						default:
							break;
						}
						return true;
					}
				});
			}
		}
	}

	/**
	 * <br>功能简述:初始化图片显示页面
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initImgView() {
		if (mImageView == null) {
			mImageView = (ImageSwitcher) this.findViewById(R.id.gallery_image_View);
			mImageView.setFactory(new ViewFactory() {
				@Override
				public View makeView() {
					ImageView iv = new ImageView(getContext());
					iv.setScaleType(ScaleType.FIT_XY);
					iv.setPadding(mPadding, mPadding, mPadding, mPadding);
					iv.setLayoutParams(new ImageSwitcher.LayoutParams(
							ImageSwitcher.LayoutParams.FILL_PARENT, 
							ImageSwitcher.LayoutParams.FILL_PARENT));
					return iv;
				}
			});
		} 
		if (mImageView != null) {
			mImageView.setBackgroundDrawable(mDefaultIcon);
			mImageView.setImageDrawable(null);
		}
	}
	
	public ImageSwitcher getImageView() {
		return mImageView;
	}
	
	public void setImageViewBitmap(Bitmap bmp) {
		if (bmp != null) {
			mBmp = bmp;
			if (mImageView != null) {
				int bmpWidth = bmp.getWidth();
				int bmpHeight = bmp.getHeight();
				if ((mBitmapWidth < 0 && mBitmapHeight < 0)
						|| (bmpWidth == mBitmapWidth && bmpHeight == mBitmapHeight)) {
					mImageView.setBackgroundDrawable(null);
					mImageView.setImageDrawable(new BitmapDrawable(bmp));
				} else {
					Bitmap scaleBitmap = scaleBitmap(bmp);
					mImageView.setBackgroundDrawable(null);
					mImageView.setImageDrawable(new BitmapDrawable(scaleBitmap));
					scaleBitmap = null;
				}
				setLoadingViewVisibility(GONE);
				setReryViewVisibility(GONE);
				setImgVisibility(VISIBLE);
				
			}
		}
	}
	
	public void setImageViewDefault() {
		if (mImageView != null) {
			mImageView.setBackgroundDrawable(mDefaultIcon);
			View view = mImageView.getCurrentView();
			if (view != null && view instanceof ImageView) {
				ImageView imageView = (ImageView) view;
				imageView.setImageDrawable(null);
			}
			view = mImageView.getNextView();
			if (view != null && view instanceof ImageView) {
				ImageView imageView = (ImageView) view;
				imageView.setImageDrawable(null);
			}
			mImageView.setImageDrawable(null);
			mBmp = null;
			mBitmapRecycled = true;
		}
	}
	
	public boolean isBitmapRecycled() {
		if (mBmp == null) {
			mBitmapRecycled = true;
		} else {
			mBitmapRecycled = false;
		}
		return mBitmapRecycled;
	}
	
	/**
	 * <br>功能简述:设置loading页面可见状态
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param visibility
	 */
	public void setLoadingViewVisibility(int visibility) {
		if (mLoadingLayout != null) {
			mLoadingLayout.setVisibility(visibility);
		}
	}

	/**
	 * <br>功能简述:设置重试页面可见状态
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param visibility
	 */
	private void setReryViewVisibility(int visibility) {
		if (mRetryLayout != null) {
			mRetryLayout.setVisibility(visibility);
			if (visibility == VISIBLE) {
				mIsRetry = true;
			} else {
				mIsRetry = false;
			}
		}
	}

	/**
	 * <br>功能简述:设置图片页面可见状态
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param visibility
	 */
	private void setImgVisibility(final int visibility) {
		if (mImageView != null) {
			mImageView.setVisibility(visibility);
		}
	}
	
	/**
	 * <br>功能简述: 外部调用，设置图片点击事件
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param clickListener
	 */
	public void setImgListener(OnClickListener clickListener) {
		mImageView.setOnClickListener(clickListener);
	}
	
	/**
	 * <br>功能简述:外部调用，设置图片url
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param imgUrl
	 */
	public void setImgUrl(String imgUrl) {
		mUrl = imgUrl;
		initRetryView();
		initLoadingView();
		initImgView();
		setLoadingViewVisibility(GONE);
		setReryViewVisibility(GONE);
		setImageViewDefault();
		setScreenShot(mImageView, imgUrl, LauncherEnv.Path.GOSTORE_ICON_PATH);
	}
	
	public void initView() {
		initRetryView();
		initLoadingView();
		initImgView();
		setLoadingViewVisibility(GONE);
		setReryViewVisibility(GONE);
		setImgVisibility(VISIBLE);
		
	}
	
	/**
	 * <br>功能简述:加载图片
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param imgView
	 * @param imgUrl
	 * @param imgPath
	 */
	public void setScreenShot(final ImageSwitcher imgView, String imgUrl, String imgPath) {
		if (mImgManager == null) {
			mImgManager = AsyncImageManager.getInstance();
		}
		setImgVisibility(VISIBLE);
		if (!AppsThemeDetailActivity.sLoadImg) {
			return;
		}
		String imgName = String.valueOf(imgUrl.hashCode());
		imgView.setTag(imgUrl);
		mBmp = mImgManager.loadImage(imgPath, imgName, imgUrl, true, false, null,
				new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imgView.getTag().equals(imgUrl)) {
							setImageViewBitmap(imageBitmap);
						}
					}
				});
		if (mBmp != null) {
			setImageView();
		}
	}
	
	
	private void setImageView() {
		if (mBmp != null && mImageView != null) {
			int bmpWidth = mBmp.getWidth();
			int bmpHeight = mBmp.getHeight();
			if ((mBitmapWidth < 0 && mBitmapHeight < 0)
					|| (bmpWidth == mBitmapWidth && bmpHeight == mBitmapHeight)) {
				mImageView.setImageDrawable(new BitmapDrawable(mBmp));
				mImageView.setBackgroundDrawable(null);
			} else {
				Bitmap scaleBitmap = scaleBitmap(mBmp);
				mImageView.setImageDrawable(new BitmapDrawable(scaleBitmap));
				mImageView.setBackgroundDrawable(null);
				scaleBitmap = null;
			}
			setLoadingViewVisibility(GONE);
			setReryViewVisibility(GONE);
			setImgVisibility(VISIBLE);
			
		}
	}

	/**
	 * 缩放图片。若不能缩放，则直接返回原图。
	 * 
	 * @author wangzhuobin
	 * @param sourceBitmap
	 * @return
	 */
	private Bitmap scaleBitmap(Bitmap sourceBitmap) {
		Bitmap destBitmap = sourceBitmap;
		if (sourceBitmap != null) {
			if (mBitmapWidth > 0 && mBitmapHeight > 0) {
				destBitmap = GoStoreDisplayUtil
						.scaleBitmapToDisplay(getContext(), sourceBitmap,
								mBitmapWidth, mBitmapHeight);
			}
		}
		return destBitmap;
	}
	
	public Bitmap getBitmap() {
		return mBmp;
	}
	
	
	public void setUrl(String url) {
		mUrl = url;
	}
	public void setBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			mBitmapRecycled = true;
		} else {
			mBitmapRecycled = false;
		}
		mBmp = bitmap;
	}
	/**
	 * <br>功能简述:清除图片
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void clearImg() {
		if (mBmp != null) {
			mBmp = null;
		}
		if (mImageView != null) {
			mImageView.setAnimation(null);
			mImageView.setImageDrawable(null);
			mImageView.setBackgroundDrawable(null);
			mImageView.setOnClickListener(null);
			mImageView = null;
		}
	}
	
	/**
	 * <br>功能简述:资源回收
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void recycle() {
		clearImg();
		setBackgroundDrawable(null);
		if (mRetryText != null) {
			mRetryText.setBackgroundDrawable(null);
			mRetryText.setOnTouchListener(null);
			mRetryText = null;
		}
		if (mRetryLayout != null) {
			mRetryLayout.setBackgroundDrawable(null);
			mRetryLayout = null;
		}
		if (mLoadingLayout != null) {
			mLoadingLayout.setBackgroundDrawable(null);
			mLoadingLayout = null;
		}
		// 图片管理器的清理
		if (mImgManager != null) {
			mImgManager.recycle(mUrl);
			mImgManager.removeAllTask();
			mImgManager = null;
		}
	}
}
