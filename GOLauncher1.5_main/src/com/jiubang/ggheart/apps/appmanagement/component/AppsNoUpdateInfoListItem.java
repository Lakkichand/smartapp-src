package com.jiubang.ggheart.apps.appmanagement.component;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.gowidget.gostore.component.SimpleImageView;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;

public class AppsNoUpdateInfoListItem extends RelativeLayout {

	private AppBean mAppBean;
	// private RelativeLayout mNormalInfoGroup = null;
	private SimpleImageView mAppImgView = null;
	private TextView mAppSizeTextView = null;
	private TextView mUpdateDatetimeTextView = null;
	private TextView mAppNameTextView = null;
	private ImageView mOperationButton = null;

	// private OnClickListener mOnclickListener;

	public AppsNoUpdateInfoListItem(Context context, AttributeSet attrs, int defStyle) {
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
		mAppImgView = (SimpleImageView) findViewById(R.id.app_no_update_image_view);
		mAppNameTextView = (TextView) findViewById(R.id.app_no_update_name_view);
		mOperationButton = (ImageView) findViewById(R.id.app_no_update_operation_button);
		mOperationButton
				.setBackgroundResource(R.drawable.apps_management_no_update_operator_selector);
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
		if (mAppNameTextView != null) {
			mAppNameTextView.setText("");
		}
		if (mAppSizeTextView != null) {
			mAppSizeTextView.setText("");
		}
		if (mUpdateDatetimeTextView != null) {
			mUpdateDatetimeTextView.setText("");
		}
		// if (mOperationButton != null) {
		// mOperationButton
		// .setBackgroundResource(R.drawable.apps_management_no_update_operator_selector);
		// // mOperationButton.setText(R.string.update);
		// }
	}

	public void destory() {
		mAppBean = null;
		if (mAppImgView != null) {
			mAppImgView.recycle();
			mAppImgView.clearIcon();
			mAppImgView = null;
		}
		mAppNameTextView = null;
		if (mOperationButton != null) {
			mOperationButton.setOnClickListener(null);
			mOperationButton = null;
		}
	}

	private void setAppImage(Drawable drawable) {
		if (mAppImgView != null) {
			// drawable = Utilities.createIconThumbnail(drawable, getContext());
			mAppImgView.setImageDrawable(drawable);
		}
	}

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
			String label = getResources().getString(R.string.update_time);
			mUpdateDatetimeTextView.setText(label + datetime);
		}
	}

	public void bindAppBean(final Context context, final int position, AppBean appBean,
			ArrayList<AppItemInfo> appItemInfos) {
		mAppBean = appBean;
		setTag(mAppBean);
		setId(position);
		if (appBean == null) {
			return;
		}

		Drawable drawable = null;
		String appName = null;
		PackageManager pm = context.getPackageManager();
		try {
			if (appItemInfos != null && !appItemInfos.isEmpty()) {
				drawable = getDrawableFromAppItemInfo(appItemInfos, appBean.mPkgName);
			}
			if (drawable == null) {
				drawable = pm.getApplicationIcon(appBean.mPkgName);
				if (drawable == null || drawable.getIntrinsicWidth() <= 0
						|| drawable.getIntrinsicHeight() <= 0) {
					drawable = context.getResources().getDrawable(
							android.R.drawable.sym_def_app_icon);
				}
				drawable = AppDataEngine.getInstance(context).createBitmapDrawable(drawable);
			}

			ApplicationInfo info = pm.getApplicationInfo(appBean.mPkgName, 0);
			if (info != null && pm.getApplicationLabel(info) != null) {
				appName = pm.getApplicationLabel(info).toString();
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		setAppImage(drawable);
		setAppName(appName);
		setAppSize(appBean.mAppSize);
		setUpdateDatetime(appBean.mUpdateTime);
	}

	// private AppBean getAppBeanByPkgName (String packageName ,
	// ArrayList<AppBean> listBeans) {
	// if (listBeans != null && listBeans.size() > 0) {
	// for (AppBean appBean : listBeans) {
	// if (appBean.mPkgName.equals(packageName)) {
	// return appBean;
	// }
	// }
	// }
	// return null;
	// }

	private BitmapDrawable getDrawableFromAppItemInfo(ArrayList<AppItemInfo> appItemInfos,
			String pkgName) {
		if (pkgName == null || appItemInfos == null || appItemInfos.size() == 0) {
			return null;
		}
		for (AppItemInfo appItemInfo : appItemInfos) {
			if (pkgName.equals(appItemInfo.getAppPackageName())) {
				return appItemInfo.mIcon;
			}
		}
		return null;
	}

	// public void setmOnclickListener(OnClickListener mOnclickListener) {
	// this.mOnclickListener = mOnclickListener;
	// }

}