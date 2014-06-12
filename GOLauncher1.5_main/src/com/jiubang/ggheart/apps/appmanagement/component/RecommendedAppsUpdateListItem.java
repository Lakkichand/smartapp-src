package com.jiubang.ggheart.apps.appmanagement.component;

import java.io.File;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.utils.MD5;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.appmanagement.bean.RecommendedApp;
import com.jiubang.ggheart.apps.appmanagement.controler.ApplicationManager.IDownloadInvoker;
import com.jiubang.ggheart.apps.appmanagement.download.ApplicationDownloadListener;
import com.jiubang.ggheart.apps.appmanagement.help.RecommAppsUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * @author zhoujun
 * 
 */
public class RecommendedAppsUpdateListItem extends RelativeLayout
		implements
			View.OnClickListener,
			IDownloadInvoker {

	private RecommendedApp mRecommApp;
	private TextView mSummaryTextView;
	private RelativeLayout mDownloadInfoGroup = null;
	// private SimpleImageView mAppImgView = null;
	private ImageView mAppImgView;
	private TextView mAppSizeTextView = null;
	private TextView mAppNameTextView = null;
	private TextView mProgressSizeTextView = null;
	private TextView mProgressPercentTextView = null;
	private ProgressBar mProgressBar = null;
	private ImageView mOperationButton = null;
	private TextView mHintsTextView = null;

	public RecommendedAppsUpdateListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public RecommendedAppsUpdateListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RecommendedAppsUpdateListItem(Context context) {
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
		mSummaryTextView = (TextView) this.findViewById(R.id.recomm_app_summary_view);
		mDownloadInfoGroup = (RelativeLayout) findViewById(R.id.recomm_download_info_layout);
		mAppSizeTextView = (TextView) findViewById(R.id.recomm_app_size_view);
		mAppImgView = (ImageView) findViewById(R.id.recomm_app_image_view);
		mAppNameTextView = (TextView) findViewById(R.id.recomm_app_name_view);
		mProgressSizeTextView = (TextView) findViewById(R.id.recomm_progress_size_view);
		mProgressPercentTextView = (TextView) findViewById(R.id.recomm_progress_percent_view);
		mProgressBar = (ProgressBar) findViewById(R.id.recomm_progress_bar);
		mOperationButton = (ImageView) findViewById(R.id.recomm_operation_button);
		mOperationButton.setOnClickListener(this);
		mHintsTextView = (TextView) findViewById(R.id.recomm_hints_view);
	}

	/**
	 * 重置默认状态的方法
	 */
	public void resetDefaultStatus() {
		this.setTag(null);

		// if (mAppImgView != null) {
		// mAppImgView.clearIcon();
		// }
		if (mAppNameTextView != null) {
			mAppNameTextView.setText("");
		}
		if (mAppSizeTextView != null) {
			mAppSizeTextView.setText("");
		}
		if (mSummaryTextView != null) {
			mSummaryTextView.setText("");
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
			mOperationButton.setBackgroundResource(R.drawable.appsmanagement_download_selector);
		}
	}

	public void destory() {
		mRecommApp = null;
		if (mAppImgView != null) {
			mAppImgView.setImageDrawable(null);
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

	//	private void setAppIcon(BitmapDrawable drawable) {
	//		if (mAppImgView != null) {
	//			if (drawable == null) {
	//				// mAppImgView.setImageDrawable(android.R.drawable.sym_def_app_icon);
	//				mAppImgView.setImageResource(android.R.drawable.sym_def_app_icon);
	//			} else {
	//				mAppImgView.setImageDrawable(drawable);
	//			}
	//
	//		}
	//	}

	private void setAppIcon() {
		if (mAppImgView != null && mRecommApp != null) {
			String iconUrl = mRecommApp.mIconUrl;
			mAppImgView.setTag(iconUrl);
			setIcon(iconUrl, LauncherEnv.Path.APP_MANAGER_ICON_PATH,
					RecommAppsUtils.getSimpleName(iconUrl));
		}
	}

	/**
	 * 读取图标，然后设到imageview里
	 * 
	 */
	private void setIcon(String imgUrl, String imgPath, String imgName) {
		imgName = MD5.encode(imgUrl);
		// TODO 这里能不能不要每次load图片都生成一个回调对象？
		Bitmap bm = AsyncImageManager.getInstance().loadImage(imgPath, imgName, imgUrl, true,
				false, null, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (mAppImgView != null && mAppImgView.getTag().equals(imgUrl)) {
							mAppImgView.setImageBitmap(imageBitmap);
						} else {
							imageBitmap = null;
						}

					}
				});
		if (bm != null) {
			mAppImgView.setImageBitmap(bm);
		} else {
			mAppImgView.setImageDrawable(getContext().getResources().getDrawable(
					R.drawable.default_icon));
		}
	}

	// private void setAppImage(String iconPath) {
	//
	// Drawable drawable = null;
	// if (iconPath != null && !"".equals(iconPath)) {
	// // bitmap = BitmapFactory.decodeFile(iconPath);
	// drawable = Drawable.createFromPath(iconPath);
	// }
	//
	// if (drawable == null) {
	// System.out.println(iconPath + " : is not exist");
	// drawable = this.getContext().getResources().getDrawable(
	// android.R.drawable.sym_def_app_icon);
	// }
	//
	// drawable = createBitmapDrawable(drawable,this.getContext());
	// if (mAppImgView != null) {
	// mAppImgView.setImageDrawable(drawable);
	// }
	// // if (bitmap == null) {
	// // System.out.println(iconPath + " : is not exist");
	// // bitmap = BitmapFactory.decodeResource(getContext().getResources(),
	// // android.R.drawable.sym_def_app_icon);
	// // }
	// // bitmap = Utilities.createBitmapThumbnail(bitmap, getContext());
	// // if (mAppImgView != null) {
	// // mAppImgView.setImageDrawable(new BitmapDrawable(getContext()
	// // .getResources(), bitmap));
	// // }
	// }

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

	private void setAppSummary(String summary) {
		if (mSummaryTextView != null) {
			mSummaryTextView.setText(summary);
		}
	}

	private void setAlreadyDownloadSize(long size) {
		if (mProgressSizeTextView != null) {
			mProgressSizeTextView.setText(ConvertUtils.convertSizeToString(size,
					ConvertUtils.FORM_DECIMAL_WITH_TWO) + "/" + mRecommApp.mSize);
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

	public void bindAppInfo(final Context context, RecommendedApp recommApp) {
		mRecommApp = recommApp;

		setAppIcon();
		setStatus(recommApp.getStatus());
		setAppName(mRecommApp.mName);
		setAppSize(mRecommApp.mSize);
		setAppSummary(mRecommApp.mSummary);

	}

	protected void setStatus(int status) {
		switch (status) {
			case RecommendedApp.STATUS_FOR_NOT_INSTALL :
			case RecommendedApp.STATUS_FOR_INSTALL :
			case RecommendedApp.STATUS_FOR_UPDATE :
				mSummaryTextView.setVisibility(VISIBLE);
				mDownloadInfoGroup.setVisibility(GONE);
				mHintsTextView.setVisibility(GONE);
				setAlreadyDownloadSize(mRecommApp.mAlreadyDownloadSize);
				setAlreadyDownloadPercent(mRecommApp.mPercent);
				mOperationButton.setEnabled(true);
				if (status == RecommendedApp.STATUS_FOR_NOT_INSTALL) {
					mOperationButton
							.setBackgroundResource(R.drawable.appsmanagement_download_selector);
				} else if (status == RecommendedApp.STATUS_FOR_INSTALL) {
					mOperationButton
							.setBackgroundResource(R.drawable.recomm_app_installed_selector);
				} else {
					mOperationButton
							.setBackgroundResource(R.drawable.appsmanagement_update_selector);
				}

				break;
			case RecommendedApp.STATUS_WAITING_DOWNLOAD :
				mSummaryTextView.setVisibility(GONE);
				mDownloadInfoGroup.setVisibility(VISIBLE);
				mHintsTextView.setVisibility(GONE);
				mProgressSizeTextView.setText(R.string.apps_management_waiting);
				mProgressPercentTextView.setText("0%");
				mOperationButton.setEnabled(true);
				mOperationButton.setBackgroundResource(R.drawable.appsmanagement_cancel_selector);
				break;
			case RecommendedApp.STATUS_DOWNLOADING :
				mSummaryTextView.setVisibility(GONE);
				mDownloadInfoGroup.setVisibility(VISIBLE);
				mHintsTextView.setVisibility(GONE);
				setAlreadyDownloadSize(mRecommApp.mAlreadyDownloadSize);
				setAlreadyDownloadPercent(mRecommApp.mPercent);
				mOperationButton.setEnabled(true);
				mOperationButton.setBackgroundResource(R.drawable.appsmanagement_cancel_selector);
				break;
			case RecommendedApp.STATUS_DOWNLOAD_COMPLETED :
				mSummaryTextView.setVisibility(GONE);
				mDownloadInfoGroup.setVisibility(GONE);
				mHintsTextView.setVisibility(VISIBLE);
				mHintsTextView.setText(R.string.apps_management_download_completed);
				mOperationButton.setEnabled(true);
				mOperationButton.setBackgroundResource(R.drawable.appsmanagement_install_selector);
				break;
			case RecommendedApp.STATUS_DOWNLOAD_FAILED :
				mSummaryTextView.setVisibility(GONE);
				mDownloadInfoGroup.setVisibility(GONE);
				mHintsTextView.setVisibility(VISIBLE);
				mHintsTextView.setText(R.string.apps_management_download_failed);
				mOperationButton.setEnabled(true);

				setOperationButtonBg();
				break;
			case RecommendedApp.STATUS_GET_READY :
				mSummaryTextView.setVisibility(GONE);
				mDownloadInfoGroup.setVisibility(GONE);
				mHintsTextView.setVisibility(VISIBLE);
				mHintsTextView.setText(R.string.apps_management_preparing);
				mOperationButton.setEnabled(false);
				break;
			case RecommendedApp.STATUS_CANCELING :
				mSummaryTextView.setVisibility(GONE);
				mDownloadInfoGroup.setVisibility(GONE);
				mHintsTextView.setVisibility(VISIBLE);
				mHintsTextView.setText(R.string.apps_management_canceling);
				mOperationButton.setEnabled(false);
				break;
		}
	}

	@Override
	public void onClick(View v) {
		if (mRecommApp != null) {
			switch (mRecommApp.getStatus()) {
				case RecommendedApp.STATUS_FOR_NOT_INSTALL :
					// downloadApp();
					// break;
				case RecommendedApp.STATUS_FOR_UPDATE :
				case RecommendedApp.STATUS_DOWNLOAD_FAILED :
					AppsManagementActivity.getApplicationManager().actionDownload(getContext(),
							this);
					break;
				case RecommendedApp.STATUS_WAITING_DOWNLOAD :
					cancelDownload();
					break;
				case RecommendedApp.STATUS_DOWNLOADING :
					cancelDownload();
					break;
				case RecommendedApp.STATUS_DOWNLOAD_COMPLETED :
					AppsManagementActivity.getApplicationManager().installApp(
							new File(mRecommApp.mApkLocalPath));
					break;
			}
		}
	}

	/**
	 * 下载推荐的应用
	 */
	public void downloadApp() {
		if (mRecommApp != null) {
			int downloadType = mRecommApp.mDownloadtype;
			// 统计:先保存推荐界面入口 为1
			AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(getContext(),
					AppRecommendedStatisticsUtil.UIENTRY_TYPE_LIST);
			if (downloadType == RecommendedApp.DOWNLOAD_TYPE_FTP) {
				// ftp下载，
				String downloadFileName = mRecommApp.mPackname + "_" + mRecommApp.mVersion + ".apk";
				GoStoreOperatorUtil.downloadFileDirectly(getContext(), mRecommApp.mName,
						mRecommApp.mDownloadurl, Long.parseLong(mRecommApp.mAppId),
						mRecommApp.mPackname, new Class[] { ApplicationDownloadListener.class },
						downloadFileName, 0, null);

				// public static void downloadFileDirectly(Context context,
				// String fileName,
				// String downloadUrl, long id, String packageName,
				// Class<? extends SimpleDownloadListener>[] listenerClazzArray,
				// String customDownloadFileName, int iconType, String iconInfo)
				// {
				//
				mRecommApp.setStatus(RecommendedApp.STATUS_GET_READY);
				setStatus(mRecommApp.getStatus());

			} else {
				String downloadUrl = mRecommApp.mDownloadurl;
				if (downloadUrl != null && !"".equals(downloadUrl)) {
					downloadUrl = downloadUrl.trim() + LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
					if (downloadType == RecommendedApp.DOWNLOAD_TYPE_MARKET) {
						// 2：电子市场下载
						GoStoreOperatorUtil.gotoMarket(getContext(), downloadUrl);
					} else if (downloadType == RecommendedApp.DOWNLOAD_TYPE_WEB) {
						// 3：电子市场web版页面
						GoStoreOperatorUtil.gotoBrowser(getContext(), downloadUrl);
					}

					// 如果跳到电子市场
					if (mRecommApp.mAppId.equals("")) {
						mRecommApp.mAppId = "0";
					}
					// 统计：应用推荐--应用详细点击次数
					AppRecommendedStatisticsUtil.getInstance().saveDetailsClick(getContext(),
							mRecommApp.mPackname, Integer.valueOf(mRecommApp.mAppId), 1);

					// 统计：应用推荐---更新主表中详细点击次数 (暂时不统计)
					// AppRecommendedStatisticsUtil.getInstance()
					// .saveDetailsClick2MainTable(getContext(),mRecommApp.mPackname,
					// Integer.valueOf(mRecommApp.mAppId),
					// mRecommApp.mTypeId);

					// 统计:再保存推荐界面入口 为2
					AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(getContext(),
							AppRecommendedStatisticsUtil.UIENTRY_TYPE_DETAIL);
				}
			}

			if (mRecommApp.getStatus() == RecommendedApp.STATUS_FOR_NOT_INSTALL) {
				// 统计:应用推荐--保存点击下载统计
				AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(getContext(),
						mRecommApp.mPackname, Integer.valueOf(mRecommApp.mAppId),
						mRecommApp.mTypeId, 1);
			} else {
				// 统计:应用推荐--保存点击更新统计
				AppRecommendedStatisticsUtil.getInstance().saveUpdataClick(getContext(),
						mRecommApp.mPackname, Integer.valueOf(mRecommApp.mAppId),
						mRecommApp.mTypeId, 1);
			}
		}
	}

	@Override
	public void invokeDownload() {
		// 更新推荐应用
		// String downloadFileName = mRecommApp.mPackname + "_"
		// + mRecommApp.mVersion + ".tmp";
		// GoStoreOperatorUtil.downloadFileDirectly(getContext(),
		// mRecommApp.mName, mRecommApp.mDownloadurl,
		// Long.parseLong(mRecommApp.mAppId), mRecommApp.mPackname,
		// new Class[] { ApplicationDownloadListener.class },
		// downloadFileName);
		// mRecommApp.setStatus(RecommendedApp.STATUS_GET_READY);
		// setStatus(mRecommApp.getStatus());
		downloadApp();
	}

	/**
	 * 取消下载
	 */
	private void cancelDownload() {
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				mDownloadController.removeDownloadTaskById(Long.parseLong(mRecommApp.mAppId));
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		setStatus(mRecommApp.getStatus());
	}

	/**
	 * 下载失败时，设置操作按钮的状态
	 */
	private void setOperationButtonBg() {
		PackageManager manager = getContext().getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(mRecommApp.mPackname, 0);
			if (info != null) {
				mRecommApp.setStatus(RecommendedApp.STATUS_FOR_UPDATE);
				mOperationButton.setBackgroundResource(R.drawable.appsmanagement_update_selector);
			} else {
				mRecommApp.setStatus(RecommendedApp.STATUS_FOR_NOT_INSTALL);
				mOperationButton.setBackgroundResource(R.drawable.appsmanagement_download_selector);
			}

		} catch (NameNotFoundException e) {
			// e.printStackTrace();
		}
	}
}
