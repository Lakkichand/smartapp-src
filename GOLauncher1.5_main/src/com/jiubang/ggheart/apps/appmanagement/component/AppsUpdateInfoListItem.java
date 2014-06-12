package com.jiubang.ggheart.apps.appmanagement.component;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.jiubang.ggheart.apps.appmanagement.controler.ApplicationManager.IDownloadInvoker;
import com.jiubang.ggheart.apps.gowidget.gostore.component.SimpleImageView;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;

public class AppsUpdateInfoListItem extends RelativeLayout
		implements
			View.OnClickListener,
			IDownloadInvoker {

	private AppBean mAppBean;
	private RelativeLayout mNormalInfoGroup = null;
	private RelativeLayout mDownloadInfoGroup = null;
	private SimpleImageView mAppImgView = null;
	private TextView mAppSizeTextView = null;
	private TextView mUpdateDatetimeTextView = null;
	private TextView mAppNameTextView = null;
	private TextView mProgressSizeTextView = null;
	private TextView mProgressPercentTextView = null;
	private ProgressBar mProgressBar = null;
	private ImageView mOperationButton = null;
	private TextView mHintsTextView = null;

	public AppsUpdateInfoListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AppsUpdateInfoListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AppsUpdateInfoListItem(Context context) {
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
		mNormalInfoGroup = (RelativeLayout) findViewById(R.id.normal_info_layout);
		mDownloadInfoGroup = (RelativeLayout) findViewById(R.id.download_info_layout);
		mAppSizeTextView = (TextView) findViewById(R.id.app_size_view);
		mUpdateDatetimeTextView = (TextView) findViewById(R.id.update_datetime_view);
		mAppImgView = (SimpleImageView) findViewById(R.id.app_image_view);
		mAppNameTextView = (TextView) findViewById(R.id.app_name_view);
		mProgressSizeTextView = (TextView) findViewById(R.id.progress_size_view);
		mProgressPercentTextView = (TextView) findViewById(R.id.progress_percent_view);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mOperationButton = (ImageView) findViewById(R.id.operation_button);
		mOperationButton.setOnClickListener(this);
		mHintsTextView = (TextView) findViewById(R.id.hints_view);
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
		if (mProgressSizeTextView != null) {
			mProgressSizeTextView.setText("");
		}
		if (mProgressPercentTextView != null) {
			mProgressPercentTextView.setText("");
		}
		if (mProgressBar != null) {
			mProgressBar.setProgress(0);
		}
		if (mOperationButton != null) {
			mOperationButton.setBackgroundResource(R.drawable.appsmanagement_update_selector);
			// mOperationButton.setText(R.string.update);
		}
	}

	public void destory() {
		mAppBean = null;
		if (mAppImgView != null) {
			mAppImgView.recycle();
			mAppImgView.clearIcon();
			mAppImgView = null;
		}
		mAppNameTextView = null;
		mProgressSizeTextView = null;
		mProgressPercentTextView = null;
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

	private void setAlreadyDownloadSize(long size) {
		if (mProgressSizeTextView != null) {
			mProgressSizeTextView.setText(ConvertUtils.convertSizeToString(size,
					ConvertUtils.FORM_DECIMAL_WITH_TWO) + "/" + mAppBean.mAppSize);
		}
	}

	private void setAlreadyDownloadPercent(int percent) {
		if (mProgressPercentTextView != null) {
			mProgressPercentTextView.setText(percent + "%");
		}
		if (mProgressBar != null) {
			mProgressBar.setProgress(percent);
		}
	}

	public void bindAppBean(final Context context, final int position, AppBean appBean,
			ArrayList<AppItemInfo> appItemInfos) {

		mAppBean = appBean;
		setTag(mAppBean);
		// checkStatus(context, appBean);
		setStatus(mAppBean.getStatus());
		Drawable drawable = null;
		String appName = null;
		PackageManager pm = context.getPackageManager();
		try {
			if (appItemInfos != null && !appItemInfos.isEmpty()) {
				drawable = getDrawableFromAppItemInfo(appItemInfos, mAppBean.mPkgName);
			}
			if (drawable == null) {
				drawable = pm.getApplicationIcon(mAppBean.mPkgName);
				// if (drawable == null) {
				// drawable = getResources().getDrawable(
				// R.drawable.appsmanagement_default_icon);
				// }
				if (drawable == null || drawable.getIntrinsicWidth() <= 0
						|| drawable.getIntrinsicHeight() <= 0) {
					drawable = context.getResources().getDrawable(
							android.R.drawable.sym_def_app_icon);
				}
				// drawable = createBitmapDrawable(drawable,context);
				drawable = AppDataEngine.getInstance(context).createBitmapDrawable(drawable);
			}

			ApplicationInfo info = pm.getApplicationInfo(mAppBean.mPkgName, 0);
			if (info != null && pm.getApplicationLabel(info) != null) {
				appName = pm.getApplicationLabel(info).toString();
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		setAppImage(drawable);
		setAppName(appName);
		setAppSize(mAppBean.mAppSize);

		// update by zhoujun 更新时间由服务器以字符串的时间下发， 客户端不需要做判断
		// try {
		// Log.d("AppsUpdateInfoListItem", "mUpdateTime is : "+
		// mAppBean.mUpdateTime);
		// SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat
		// .getInstance();
		// format.applyPattern("yyyy-MM-dd HH:mm:ss");
		// Date datetime = format.parse(mAppBean.mUpdateTime);
		// format.applyPattern("yyyy-MM-dd");
		// String datetimeStr = format.format(datetime);
		// setUpdateDatetime(datetimeStr);
		// } catch (ParseException e) {
		// e.printStackTrace();
		// setUpdateDatetime(mAppBean.mUpdateTime);
		// }
		setUpdateDatetime(mAppBean.mUpdateTime);
		// update by zhoujun 2012-05-24 end
	}

	// private BitmapDrawable createBitmapDrawable(Drawable drawable,Context
	// context) {
	// BitmapDrawable bmpDrawable= Utilities.createBitmapDrawableFromDrawable(
	// drawable, context);
	// if (bmpDrawable != null && bmpDrawable.getBitmap() != null)
	// {
	// final Bitmap bitmap =
	// Utilities.createBitmapThumbnail(bmpDrawable.getBitmap(), context);
	// bmpDrawable = new BitmapDrawable(context.getResources(), bitmap);
	// }
	// return bmpDrawable;
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

	private void cancelDownload() {
		mAppBean.setStatus(AppBean.STATUS_CANCELING);
		setStatus(mAppBean.getStatus());
		AppsManagementActivity.getApplicationManager().cancelDownload(mAppBean);
	}

	protected void setStatus(int status) {
		switch (status) {
			case AppBean.STATUS_NORMAL :
				mNormalInfoGroup.setVisibility(VISIBLE);
				mDownloadInfoGroup.setVisibility(GONE);
				mHintsTextView.setVisibility(GONE);
				setAlreadyDownloadSize(mAppBean.getAlreadyDownloadSize());
				setAlreadyDownloadPercent(mAppBean.getAlreadyDownloadPercent());
				mOperationButton.setEnabled(true);
				mOperationButton.setBackgroundResource(R.drawable.appsmanagement_update_selector);
				break;
			case AppBean.STATUS_WAITING_DOWNLOAD :
				mNormalInfoGroup.setVisibility(GONE);
				mDownloadInfoGroup.setVisibility(VISIBLE);
				mHintsTextView.setVisibility(GONE);
				mProgressSizeTextView.setText(R.string.apps_management_waiting);
				mProgressPercentTextView.setText("0%");
				// change button style
				mOperationButton.setEnabled(true);
				mOperationButton.setBackgroundResource(R.drawable.appsmanagement_cancel_selector);
				break;
			case AppBean.STATUS_DOWNLOADING :
				mNormalInfoGroup.setVisibility(GONE);
				mDownloadInfoGroup.setVisibility(VISIBLE);
				mHintsTextView.setVisibility(GONE);
				setAlreadyDownloadSize(mAppBean.getAlreadyDownloadSize());
				setAlreadyDownloadPercent(mAppBean.getAlreadyDownloadPercent());
				// mOperationButton.setText(R.string.cancel);
				// mProgressPercentTextView.setText("0/"
				// + ConvertUtils.convertSizeToString(mAppBean.mAppSize));
				// mProgressSizeTextView.setText("0%");
				// mProgressBar.setProgress(0);
				mOperationButton.setEnabled(true);
				mOperationButton.setBackgroundResource(R.drawable.appsmanagement_cancel_selector);
				break;
			case AppBean.STATUS_DOWNLOAD_COMPLETED :
				mNormalInfoGroup.setVisibility(GONE);
				mDownloadInfoGroup.setVisibility(GONE);
				mHintsTextView.setVisibility(VISIBLE);
				mHintsTextView.setText(R.string.apps_management_download_completed);
				// change button style
				mOperationButton.setEnabled(true);
				mOperationButton.setBackgroundResource(R.drawable.appsmanagement_install_selector);
				break;
			case AppBean.STATUS_DOWNLOAD_FAILED :
				mNormalInfoGroup.setVisibility(GONE);
				mDownloadInfoGroup.setVisibility(GONE);
				mHintsTextView.setVisibility(VISIBLE);
				mHintsTextView.setText(R.string.apps_management_download_failed);
				mOperationButton.setEnabled(true);
				mOperationButton.setBackgroundResource(R.drawable.appsmanagement_update_selector);
				break;
			case AppBean.STATUS_GET_READY :
				mNormalInfoGroup.setVisibility(GONE);
				mDownloadInfoGroup.setVisibility(GONE);
				mHintsTextView.setVisibility(VISIBLE);
				mHintsTextView.setText(R.string.apps_management_preparing);
				mOperationButton.setEnabled(false);
				break;
			case AppBean.STATUS_CANCELING :
				mNormalInfoGroup.setVisibility(GONE);
				mDownloadInfoGroup.setVisibility(GONE);
				mHintsTextView.setVisibility(VISIBLE);
				mHintsTextView.setText(R.string.apps_management_canceling);
				mOperationButton.setEnabled(false);
				break;
		}
	}

	public AppBean getAppBean() {
		return mAppBean;
	}

	@Override
	public void onClick(View v) {
		if (mAppBean != null) {
			switch (mAppBean.getStatus()) {
				case AppBean.STATUS_NORMAL :
				case AppBean.STATUS_DOWNLOAD_FAILED :
					// 保存界面入口
					AppManagementStatisticsUtil.getInstance().saveCurrentUIEnter(getContext(),
							AppManagementStatisticsUtil.UIENTRY_TYPE_LIST);
					// 保存点击更新统计
					AppManagementStatisticsUtil.getInstance().saveUpdataClick(getContext(),
							mAppBean.mPkgName, mAppBean.mAppId, 1);
					AppsManagementActivity.getApplicationManager().actionDownload(getContext(),
							this);

					break;
				case AppBean.STATUS_WAITING_DOWNLOAD :
					cancelDownload();
					break;
				case AppBean.STATUS_DOWNLOADING :
					cancelDownload();
					break;
				case AppBean.STATUS_DOWNLOAD_COMPLETED :
					//统计安装量
					AppRecommendedStatisticsUtil.getInstance().saveReadyToInstall(v.getContext(),
							mAppBean.mPkgName, String.valueOf(mAppBean.mAppId), 0, null);
					((AppsManagementActivity) v.getContext()).addInstallApp(mAppBean.getFilePath(),
							mAppBean.mPkgName);
					break;
			}
		}
	}

	@Override
	public void invokeDownload() {
		AppsManagementActivity.getApplicationManager().startDownload(mAppBean);
		setStatus(mAppBean.getStatus());
	}
}