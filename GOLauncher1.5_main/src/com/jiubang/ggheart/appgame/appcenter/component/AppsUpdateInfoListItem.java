package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.HashMap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.contorler.ApplicationManager.IDownloadInvoker;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.component.SimpleImageView;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>
 * 类描述: 应用更新列表项 <br>
 * 功能详细描述:
 * 
 * @author zhoujun
 * @date [2012-10-18]
 */
public class AppsUpdateInfoListItem extends RelativeLayout implements
		View.OnClickListener, IDownloadInvoker {

	private AppBean mAppBean;
	private SimpleImageView mAppImgView = null;
	private TextView mAppSizeTextView = null;
	private TextView mUpdateDatetimeTextView = null;
	private TextView mAppNameTextView = null;
	private TextView mProgressPercentTextView = null;
	// private Button mButton = null;

	private RelativeLayout mOperationLayout;
	private ImageView mOperationButton = null;
	private TextView mOperationStatusTextView = null;

	private LinearLayout mShowDetailAndUpdate = null;
	private LinearLayout mDetailPage = null;
	private LinearLayout mNoUpdate = null;
	private TextView mUpdateIntro = null;
	private TextView mUpdateInfo = null;
	private RelativeLayout mContentlayout = null;
	private AppsUpdateInfoListItem mSelf = null;
	private ImageSwitcher mImageSwitcher = null;
	private SimpleImageView mAppAnotherImgView = null;
	private Bitmap mDefaultBitmap = null;

	// private static final String ImgPath =
	// LauncherEnv.Path.APP_MANAGER_ICON_PATH;;
	public AppsUpdateInfoListItem(Context context, AttributeSet attrs,
			int defStyle) {
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
		mSelf = this;
	}

	/**
	 * 初始化方法
	 */
	private void init() {
		mAppSizeTextView = (TextView) findViewById(R.id.app_size_view);
		mUpdateDatetimeTextView = (TextView) findViewById(R.id.update_datetime_view);
		mAppImgView = (SimpleImageView) findViewById(R.id.update_app_icon);
		mAppNameTextView = (TextView) findViewById(R.id.app_name_view);
		mProgressPercentTextView = (TextView) findViewById(R.id.update_progress);
		mImageSwitcher = (ImageSwitcher) findViewById(R.id.app_update_image_switcher);
		mAppAnotherImgView = (SimpleImageView) findViewById(R.id.update_app_another_icon);
		// mButton = (Button) findViewById(R.id.update_operation_button);
		// mButton.setOnClickListener(this);

		mShowDetailAndUpdate = (LinearLayout) findViewById(R.id.intro);
		mDetailPage = (LinearLayout) findViewById(R.id.detail_page);
		mNoUpdate = (LinearLayout) findViewById(R.id.noUpdate);
		mUpdateIntro = (TextView) findViewById(R.id.update_intro);
		mUpdateInfo = (TextView) findViewById(R.id.update_intro_detail);
		mUpdateInfo.setVisibility(View.GONE);
		mUpdateIntro.setVisibility(View.GONE);
		mContentlayout = (RelativeLayout) findViewById(R.id.content_layout);
		mOperationLayout = (RelativeLayout) findViewById(R.id.update_operation_layout);
		mOperationButton = (ImageView) findViewById(R.id.update_operation);
		mOperationStatusTextView = (TextView) findViewById(R.id.update_operation_status);
		mOperationButton.setOnClickListener(this);
		mOperationLayout.setOnClickListener(this);
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

		if (mProgressPercentTextView != null) {
			mProgressPercentTextView.setText("");
			mProgressPercentTextView.setVisibility(View.INVISIBLE);
		}

		if (mOperationButton != null) {
			mOperationButton
					.setBackgroundResource(R.drawable.appsgame_update_selector);
		}

		if (mOperationStatusTextView != null) {
			mOperationStatusTextView.setText(R.string.update);
		}

		if (mShowDetailAndUpdate != null) {
			mShowDetailAndUpdate.setVisibility(View.GONE);
		}
		// if (mButton != null) {
		// mButton.setVisibility(View.VISIBLE);
		// // ButtonUtils.setButtonTextSize(mButton);
		// mButton.setEnabled(true);
		// //
		// mButton.setBackgroundResource(R.drawable.appgame_install_btn_selector);
		// mButton.setText(R.string.appgame_update);
		// // mButton.setShadowLayer(1, 0, -1, 0xffad7300);
		// }
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
		mProgressPercentTextView = null;
		if (mOperationButton != null) {
			mOperationButton.setOnClickListener(null);
			mOperationButton = null;
		}
		// if (mButton != null) {
		// mButton.setOnClickListener(null);
		// mButton = null;
		// }
		mCallBack = null;
		mAppBean = null;

	}

//	private void setAppImage(int position) {
//		if (mAppImgView != null) {
//			mAppImgView.setTag(mAppBean.mPkgName);
//			Bitmap bm = AsyncImageManager.getInstance().loadImageIconForList(
//					position, getContext(), mAppBean.mPkgName, true, mCallBack);
//			if (bm != null) {
//				mAppImgView.setImageBitmap(bm);;
//			} else {
//				mAppImgView.setImageDrawable(getContext().getResources()
//						.getDrawable(R.drawable.default_icon));
//			}
//		}
//	}
	
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

	private void setAlreadyDownloadPercent(int percent) {
		if (mProgressPercentTextView != null) {
			mProgressPercentTextView.setText(percent + "%");
		}
	}

	public void bindAppBean(final Context context, final int position,
			AppBean appBean, Bitmap defaultBitmap) {
		mDefaultBitmap = defaultBitmap;
		mAppBean = appBean;
		setTag(mAppBean);
		setStatus(mAppBean.getStatus());
		PackageManager pm = context.getPackageManager();
		String appName = mAppBean.getAppName(pm);
		setAppImage(position);
		setAppName(appName);
		setAppSize(mAppBean.mAppSize);
		if (mAppBean.mUpdateLog != null && mAppBean.mUpdateLog.length() > 0) {
			mUpdateInfo.setVisibility(View.VISIBLE);
			mUpdateIntro.setVisibility(View.VISIBLE);
			String updateContent = context.getString(R.string.app_update_intro);
			mUpdateIntro.setText(updateContent);
			mUpdateInfo.setText(mAppBean.mUpdateLog);
		} else {
			mUpdateInfo.setVisibility(View.GONE);
			mUpdateIntro.setVisibility(View.VISIBLE);
			String noUpdateContent = context
					.getString(R.string.app_no_update_intro);
			mUpdateIntro.setText(noUpdateContent);
		}
		mDetailPage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doOnItemClick(mAppBean);
			}
		});

		setUpdateDatetime(mAppBean.mUpdateTime);

		mShowDetailAndUpdate.setTag(position);
		mContentlayout.setTag(position);
		mNoUpdate.setTag(position);
	}

	protected void setStatus(int status) {
		switch (status) {
		case AppBean.STATUS_NORMAL:
			mProgressPercentTextView.setVisibility(View.INVISIBLE);
			// if (mButton != null) {
			// mButton.setVisibility(View.VISIBLE);
			// // ButtonUtils.setButtonTextSize(mButton);
			// mButton.setEnabled(true);
			// //
			// mButton.setBackgroundResource(R.drawable.appgame_install_btn_selector);
			// mButton.setText(R.string.appgame_update);
			// // 字体绿色投影
			// // mButton.setShadowLayer(1, 0, -1, 0xffad7300);
			// }
			mOperationLayout.setVisibility(View.VISIBLE);

			mOperationButton
					.setBackgroundResource(R.drawable.appsgame_update_selector);
			mOperationStatusTextView.setText(R.string.update);
			break;
		case AppBean.STATUS_WAITING_DOWNLOAD:
			mProgressPercentTextView.setVisibility(View.VISIBLE);
			mProgressPercentTextView.setText(R.string.download_manager_wait);
			mOperationLayout.setVisibility(View.INVISIBLE);
			// mButton.setVisibility(View.GONE);
			break;
		case AppBean.STATUS_DOWNLOADING:
			mOperationLayout.setVisibility(View.INVISIBLE);
			// mButton.setVisibility(View.GONE);
			mProgressPercentTextView.setVisibility(View.VISIBLE);
			setAlreadyDownloadPercent(mAppBean.getAlreadyDownloadPercent());
			break;
		case AppBean.STATUS_DOWNLOAD_COMPLETED:
			// change button style
			mProgressPercentTextView.setVisibility(View.INVISIBLE);
			mOperationLayout.setVisibility(View.VISIBLE);

			mOperationButton
					.setBackgroundResource(R.drawable.downloadmanager_install_selector);
			mOperationStatusTextView.setText(R.string.gostore_detail_install);

			// if (mButton != null) {
			// mButton.setVisibility(View.VISIBLE);
			// // ButtonUtils.setButtonTextSize(mButton);
			// mButton.setEnabled(true);
			// //
			// mButton.setBackgroundResource(R.drawable.appgame_download_btn_selector);
			// mButton.setText(R.string.appgame_install);
			// // 字体绿色投影
			// // mButton.setShadowLayer(1, 0, -1, 0xff83ad00);
			// }
			break;
		case AppBean.STATUS_DOWNLOAD_FAILED:
			// mProgressPercentTextView.setText(R.string.download_manager_failed);
//			mProgressPercentTextView.setText("");
//			mProgressPercentTextView.setVisibility(View.GONE);
			// if (mButton != null) {
			// mButton.setVisibility(View.VISIBLE);
			// // ButtonUtils.setButtonTextSize(mButton);
			// mButton.setEnabled(true);
			// //
			// mButton.setBackgroundResource(R.drawable.appgame_download_btn_selector);
			// // mButton.setText(R.string.appgame_install);
			// // 字体绿色投影
			// // mButton.setShadowLayer(1, 0, -1, 0xff83ad00);
			// }
			break;
		case AppBean.STATUS_STOP:
			// mButton.setVisibility(View.GONE);
			mProgressPercentTextView.setVisibility(View.VISIBLE);
			mProgressPercentTextView.setText(R.string.download_manager_pause);
			break;
		// case AppBean.STATUS_GET_READY:
		// mNormalInfoGroup.setVisibility(GONE);
		// mDownloadInfoGroup.setVisibility(GONE);
		// mHintsTextView.setVisibility(VISIBLE);
		// mHintsTextView.setText(R.string.apps_management_preparing);
		// mOperationButton.setEnabled(false);
		// break;
		case AppBean.STATUS_CANCELING:
			// mProgressPercentTextView.setText(R.string.apps_management_canceling);
			mProgressPercentTextView.setText("");
			mProgressPercentTextView.setVisibility(View.GONE);
			// if (mButton != null) {
			// mButton.setVisibility(View.VISIBLE);
			// // ButtonUtils.setButtonTextSize(mButton);
			// mButton.setEnabled(true);
			// //
			// mButton.setBackgroundResource(R.drawable.appgame_download_btn_selector);
			// // mButton.setText(R.string.appgame_install);
			// // 字体绿色投影
			// // mButton.setShadowLayer(1, 0, -1, 0xff83ad00);
			// }
			break;
		default:
			break;
		}
	}

	public AppBean getAppBean() {
		return mAppBean;
	}

	@Override
	public void onClick(View v) {
		if (!FileUtil.isSDCardAvaiable()) {
			Toast.makeText(
					getContext(),
					getContext().getString(
							R.string.import_export_sdcard_unmounted), 1000)
					.show();
			return;
		}
		if (mAppBean != null) {
			AppsManagementActivity.sendMessage(this,
					IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
					IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 2, mAppBean,
					null);
			switch (mAppBean.getStatus()) {
			case AppBean.STATUS_NORMAL:

			case AppBean.STATUS_DOWNLOAD_FAILED:
				// 保存界面入口
				AppManagementStatisticsUtil.getInstance().saveCurrentUIEnter(
						getContext(),
						AppManagementStatisticsUtil.UIENTRY_TYPE_LIST);
				// 保存点击更新统计
				AppManagementStatisticsUtil.getInstance().saveUpdataClick(
						getContext(), mAppBean.mPkgName, mAppBean.mAppId, 1);
				AppsManagementActivity.getApplicationManager().actionDownload(
						getContext(), this);

				break;
			// case AppBean.STATUS_WAITING_DOWNLOAD:
			// cancelDownload();
			// break;
			// case AppBean.STATUS_DOWNLOADING:
			// cancelDownload();
			// break;
			case AppBean.STATUS_DOWNLOAD_COMPLETED:
				// 统计安装量
				AppRecommendedStatisticsUtil.getInstance().saveReadyToInstall(
						v.getContext(), mAppBean.mPkgName,
						String.valueOf(mAppBean.mAppId), 0, null);
				AppsManagementActivity.sendHandler(this,
						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
						IDiyMsgIds.APPS_MANAGEMENT_INSTALL_APP, 0,
						mAppBean.getFilePath(), null);
				break;
			}
		}
	}

	@Override
	public void invokeDownload() {
		AppsManagementActivity.getApplicationManager().startDownload(mAppBean,
				DownloadTask.ICON_TYPE_LOCAL, mAppBean.mPkgName,
				AppsDetail.START_TYPE_APPMANAGEMENT);
		setStatus(mAppBean.getStatus());
	}

	private void doOnItemClick(AppBean appBean) {
		AppManagementStatisticsUtil.getInstance().saveCurrentUIEnter(
				getContext(), AppManagementStatisticsUtil.UIENTRY_TYPE_LIST);

		// 统计详细点击
		AppManagementStatisticsUtil.getInstance().saveDetailsClick(
				getContext(), appBean.mPkgName, appBean.mAppId, 1);

		HashMap<Integer, String> urlHashMap = appBean.mUrlMap;
		if (urlHashMap != null && urlHashMap.size() > 0) {
			// 走ftp，跳转到精品详情页面
			String detailUrl = urlHashMap
					.get(GoStorePublicDefine.URL_TYPE_DETAIL_ADDRESS);
			if (detailUrl != null && !"".equals(detailUrl)) {
				// //应用更新中，状态为等待下载和正在下载的应用，在详情里面，更新按钮不可点击
				// int downloadStatus = 0 ;
				// if (appBean.getStatus() == AppBean.STATUS_WAITING_DOWNLOAD ||
				// appBean.getStatus() == AppBean.STATUS_DOWNLOADING) {
				// downloadStatus = 1;
				// }
				// GoStoreOperatorUtil.gotoStoreDetailDirectly(
				// getContext(),
				// appBean.mAppId,downloadStatus,ItemDetailActivity.START_TYPE_APPMANAGEMENT,null);

				AppsDetail.gotoDetailDirectly(getContext(), AppsDetail.START_TYPE_APPMANAGEMENT,
						appBean.mAppId, appBean.mPkgName, appBean);

				// 统计：国内---不保存点击更新(times = 0)
				AppManagementStatisticsUtil.getInstance().saveUpdataClick(
						getContext(), appBean.mPkgName, appBean.mAppId, 0);

			} else {
				// 跳转到电子市场
				detailUrl = urlHashMap
						.get(GoStorePublicDefine.URL_TYPE_GOOGLE_MARKET);
				if (detailUrl != null && !"".equals(detailUrl)) {
					detailUrl = detailUrl.trim()
							+ LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
					// 统计：国外---保存点击更新统计(记作点击过更新)
//					AppManagementStatisticsUtil.getInstance().saveUpdataClick(
//							getContext(), appBean.mPkgName, appBean.mAppId, 1);
					GoStoreOperatorUtil.gotoMarket(getContext(), detailUrl);
				} else {
					// 跳转到web版电子市场
					detailUrl = urlHashMap
							.get(GoStorePublicDefine.URL_TYPE_WEB_GOOGLE_MARKET);
					if (detailUrl == null || "".equals(detailUrl)) {
						// 跳转到其他地址
						detailUrl = urlHashMap
								.get(GoStorePublicDefine.URL_TYPE_OTHER_ADDRESS);
					}
					if (detailUrl != null && !"".equals(detailUrl)) {
						detailUrl = detailUrl
								+ LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
						GoStoreOperatorUtil
								.gotoBrowser(getContext(), detailUrl);
					} else {
						// 跳转失败
						Toast.makeText(getContext(),
								R.string.themestore_url_fail, Toast.LENGTH_LONG)
								.show();
					}
				}

				// 统计：国外---保存点击更新统计(记作点击过更新)
				AppManagementStatisticsUtil.getInstance().saveUpdataClick(
						getContext(), appBean.mPkgName, appBean.mAppId, 1);
			}

			// 统计：应用更新：再保存UI入口：2
			AppManagementStatisticsUtil.getInstance().saveCurrentUIEnter(
					getContext(),
					AppManagementStatisticsUtil.UIENTRY_TYPE_DETAIL);

		}
	}

	public LinearLayout getmNoUpdate() {
		return mNoUpdate;
	}

	public RelativeLayout getmContentLayout() {
		return mContentlayout;
	}

	public LinearLayout getmShowDetailAndUpdate() {
		return mShowDetailAndUpdate;
	}
}