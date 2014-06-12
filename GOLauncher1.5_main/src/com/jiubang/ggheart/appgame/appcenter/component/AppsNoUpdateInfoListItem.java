package com.jiubang.ggheart.appgame.appcenter.component;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.apps.gowidget.gostore.component.SimpleImageView;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;

/**
 * 
 * @author zhujian 忽略更新
 */
public class AppsNoUpdateInfoListItem extends RelativeLayout {

	private AppBean mAppBean;
	// private RelativeLayout mNormalInfoGroup = null;
	private SimpleImageView mAppImgView = null;
	private TextView mAppSizeTextView = null;
	private TextView mUpdateDatetimeTextView = null;
	private TextView mAppNameTextView = null;
	private ImageView mOperationButton = null;
	private RelativeLayout mRelativeLayout = null;
	private RelativeLayout mContentLayout = null;
	private ImageSwitcher mImageSwitcher = null;
	private SimpleImageView mAppAnotherImgView = null;
	private Bitmap mDefaultBitmap = null;

	// private OnClickListener mOnclickListener;

	public AppsNoUpdateInfoListItem(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public AppsNoUpdateInfoListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AppsNoUpdateInfoListItem(Context context) {
		super(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		init();
	}

	/**
	 * 初始化方法
	 */
	private void init() {
		// mNormalInfoGroup = (RelativeLayout)
		// findViewById(R.id.app_no_update_normal_info_layout);
		mAppSizeTextView = (TextView) findViewById(R.id.app_no_update_size_view);
		mUpdateDatetimeTextView = (TextView) findViewById(R.id.app_no_update_datetime_view);
		mImageSwitcher = (ImageSwitcher) findViewById(R.id.app_no_update_image_switcher);
		mAppImgView = (SimpleImageView) findViewById(R.id.no_update_app_icon);
		mAppAnotherImgView = (SimpleImageView) findViewById(R.id.no_update_app_another_icon);
		mAppNameTextView = (TextView) findViewById(R.id.app_no_update_name_view);
		mOperationButton = (ImageView) findViewById(R.id.app_no_update_operation_button);
		mRelativeLayout = (RelativeLayout) findViewById(R.id.intro);
		mContentLayout = (RelativeLayout) findViewById(R.id.content_layout);
		mOperationButton
				.setBackgroundResource(R.drawable.apps_management_no_update_operator_selector_new);
	}

	/**
	 * 重置默认状态的方法
	 */
	public void resetDefaultStatus() {
		this.setTag(null);
		mAppBean.setAppBeanStatusChangeListener(null);
		mAppBean.setAppBeanDownloadListener(null);
		mAppBean = null;
		if (mAppImgView != null) {
			mAppImgView.clearIcon();
		}
		if (mAppAnotherImgView != null) {
			mAppAnotherImgView.clearIcon();
		}
		if (mAppNameTextView != null) {
			mAppNameTextView.setText("");
		}
		if (mAppSizeTextView != null) {
			mAppSizeTextView.setText("");
		}
		if (mUpdateDatetimeTextView != null) {
			mUpdateDatetimeTextView.setText("");
		}
		if (mOperationButton != null) {
			mOperationButton
					.setBackgroundResource(R.drawable.apps_management_no_update_operator_selector_new);
			// mOperationButton.setText(R.string.update);
		}
	}

	public void destory() {
		if (mAppImgView != null) {
			mAppImgView.recycle();
			mAppImgView.clearIcon();
			mAppImgView = null;
		}
		if (mAppAnotherImgView != null) {
			mAppAnotherImgView.recycle();
			mAppAnotherImgView.clearIcon();
			mAppAnotherImgView = null;
		}
		if (mImageSwitcher != null) {
			mImageSwitcher = null;
		}
		mAppNameTextView = null;

		if (mOperationButton != null) {
			mOperationButton.setOnClickListener(null);
			mOperationButton = null;
		}

		// AppsManageImageManager.getInstance(getContext()).unregisterImageObverser(mAppBean.mPkgName,
		// this);
		mAppBean = null;
	}

//	private void setAppImage(int position) {
//		if (mAppImgView != null) {
//			mAppImgView.setTag(mAppBean.mPkgName);
//			Bitmap bm = AsyncImageManager.getInstance().loadImageIconForList(
//					position, getContext(), mAppBean.mPkgName, true, mCallBack);
//			if (bm != null) {
//				mAppImgView.setImageBitmap(bm);
//				;
//			} else {
//				mAppImgView.setImageDrawable(getContext().getResources()
//						.getDrawable(R.drawable.default_icon));
//			}
//		}
//	}
//
//	private AsyncImageLoadedCallBack mCallBack = new AsyncImageLoadedCallBack() {
//		@Override
//		public void imageLoaded(Bitmap bm, String url) {
//			if (mAppImgView != null && mAppImgView.getTag().equals(url)) {
//				mAppImgView.setImageBitmap(bm);
//			} else {
//				bm = null;
//			}
//		}
//	};
	
	private void setAppImage(int position) {
		if (mImageSwitcher != null) {
			mImageSwitcher.setTag(mAppBean.mPkgName);
			mImageSwitcher.getCurrentView().clearAnimation();
			mImageSwitcher.getNextView().clearAnimation();
			Bitmap bm = AsyncImageManager.getInstance().loadImageIconForList(
			position, getContext(), mAppBean.mPkgName, true, mCallBack);
			ImageView imageView = (ImageView) mImageSwitcher.getCurrentView();
			if (bm != null) {
				imageView.setImageBitmap(bm);
			} else {
				imageView.setImageBitmap(mDefaultBitmap);
			}
		}
	}
	
	private AsyncImageLoadedCallBack mCallBack = new AsyncImageLoadedCallBack() {
		@Override
		public void imageLoaded(Bitmap bm, String url) {
			if (mImageSwitcher != null && mImageSwitcher.getTag().equals(url)) {
				Drawable drawable = ((ImageView) mImageSwitcher
						.getCurrentView()).getDrawable();
				if (drawable instanceof BitmapDrawable) {
					Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
					if (bitmap == mDefaultBitmap) {
						mImageSwitcher.setImageDrawable(new BitmapDrawable(bm));
					}
				}
			} else {
				bm = null;
			}
		}
	};

	private void setAppName(String name) {
		if (mAppNameTextView != null) {
			mAppNameTextView.setText(name);
		}
	}

	private void setAppSize(String size) {
		if (mAppSizeTextView != null) {
			mAppSizeTextView.setText(size);
		}
	}

	private void setUpdateDatetime(String datetime) {
		if (mUpdateDatetimeTextView != null) {
			// String label = getResources().getString(R.string.update_time);
			mUpdateDatetimeTextView.setText(datetime);
		}
	}

	public void bindAppBean(final Context context, final int position,
			AppBean appBean, Bitmap bitmap) {
		mAppBean = appBean;
		mDefaultBitmap = bitmap;
		if (appBean == null) {
			return;
		}

		// Drawable drawable = getIconDrawable(mAppBean.mPkgName);

		PackageManager pm = context.getPackageManager();
		String appName = mAppBean.getAppName(pm);
		setAppImage(position);
		setAppName(appName);
		setAppSize(appBean.mAppSize);
		setUpdateDatetime(appBean.mUpdateTime);

		mOperationButton.setTag(position);
		mContentLayout.setTag(position);
		// mOperationButton.setId(position);
	}

	// private Drawable getIconDrawable(String packageName) {
	// Drawable drawable =
	// AppsManageImageManager.getInstance(getContext()).loadImage(packageName,
	// this);
	// if (drawable == null || drawable.getIntrinsicWidth() <= 0
	// || drawable.getIntrinsicHeight() <= 0) {
	// drawable = getContext().getResources().getDrawable(
	// android.R.drawable.sym_def_app_icon);
	// }
	// return drawable;
	// }

	public ImageView getmOperationButton() {
		return mOperationButton;
	}

	public RelativeLayout getmContentLayout() {
		return mContentLayout;
	}
	// public void setmOnclickListener(OnClickListener mOnclickListener) {
	// this.mOnclickListener = mOnclickListener;
	// }

}