package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.gau.go.launcherex.R.color;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.bean.AppDetailInfoBean;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.feedback.AppFeedbackActivity;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.setting.AppGameSettingData;
import com.jiubang.ggheart.appgame.base.utils.ApkInstallUtils;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.base.utils.WrapOnClickListener;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.appgame.gostore.base.component.AppsThemeDetailActivity;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.component.SimpleImageView;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreAppInforUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.billing.PurchaseStateManager;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.theme.zip.ZipResources;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
/**
 * 
 * <br>类描述:应用中心，应用详情container
 * <br>功能详细描述:
 * 
 * @author  lijunye
 * @date  [2012-12-13]
 */
public class AppsDetailContainer {
	// 数据Bean
	private AppDetailInfoBean mDetailInfoBean;
	// 包含mLinearLayout和 mSearchView的layout
	private FrameLayout mFrameLayout;
	// 整个Activity的View,不包括mSearchView
	private LinearLayout mLinearLayout;
	// 除标题外的页面内容的View
	private RelativeLayout mView;
	// 垂直滚动view
	private AppDetailScrollViewSlowSpeed mScrollView;
	// 价格TextView
	private TextView mContentPriceTextView;
	// 下载图标与文字的父框
	private LinearLayout mDownLoadCLick = null;
	// 下载图标更换
	private ImageView mDownloadIcon = null;
	// 描述TextView
	private TextView mContentDescriptionTextView;
	// 更多描述TextView
	private ImageView mMoreDescriptionTextView;
	// 更新TextView
	private TextView mContentUpdateContentTextView;
	// 更多更新TextView
	private ImageView mMoreUpdateTextView;
	// 详情界面缩略图SimpleImageView的集合
	private ArrayList<ImageView> mSimpleImageViews = null;
	// 图标缩略图
	private SimpleImageView mContentIconImageView = null;
	//	// 缩略图预览
	//	private AppDetailGallery mThumbnailGallery = null;
	// 缩略图指示器
	private AppDetailIndicators mAppDetailIndicators = null;
	// 意见反馈
	private LinearLayout mFeedBackEmail = null;
	private RelativeLayout mAppTitle;
	// 预览图新组件
	private GoStoreDetailScrollViewGroup mGoStoreDetailScrollViewGroup = null;
	private RelativeLayout mFootLoadingLayout = null;
	/**
	 * 搜索view
	 */
	private AppsManagementSearchView mSearchView;

	private LinearLayout mDownloadLayout = null; // 下载进度条
	private TextView mPercentText = null; // 百分比数字
	private ProgressBar mProgressBar = null; // 进度条
	private ImageView mImgBtnDownloadPauseOrResume = null; // 下载暂停或重试
	private ImageView mImgBtnDownloadCancel = null; // 下载取消
	private LinearLayout mLinearLayoutBtnDownloadPauseOrResume = null;
	private LinearLayout mLinearLayoutBtnDownloadCancel = null;

	// 按钮状态
	private final static int DOWNLOAD_POSSIBLE = 0; // 免费或收费下载
	private final static int UPDATE_POSSIBLE = 1; // 可更新
	private final static int INSTALLED = 2; // 已安装
	private final static int DOWNLOADING = 3; // 下载中
	private final static int DOWNLOAD_COMPLETE = 4; // 下载完成
	private final static int DOWNLOAD_IMPOSSIBLE = 5; // 不允许下载
	private final static int INSTALL = 6; // 下载完成，点击安装
	private final static int DOWNLOAD_PAUSE = 7; // 下载暂停
	private final static int DOWNLOAD_WAIT = 8; // 下载连接中

	private boolean mIsDownloading = false;
	
	/**
	 * 是否已经获得详情的数据Bean
	 */
	private boolean mHasObtainDetail = false;

	// 正在下载的APP包名
	private String mDownloadPkg = null;
	// 正在下载的APP ID
	private int mDownloadId = 0;
	// 入口类型(0 [沉默]从Gostore，1 从应用更新，2从应用推荐)
	private int mStartType = 0;
	// 推荐分类ID
	private String mCategoryID = null;
	// 应用评分
	private int mGrade = -1;

	private Handler mHandler = null;

	private static final int MSG_DOWNLOAD_START = 201;
	private static final int MSG_DOWNLOAD_STOP = 202;
	private static final int MSG_DOWNLOAD_DESTORY = 203;
	private static final int MSG_DOWNLOAD_FAILED = 204;
	private static final int MSG_DOWNLOAD_WAIT = 205;
	private static final int MSG_DOWNLOAD_UPDATE_PROGRESS = 206;
	private static final int MSG_DOWNLOAD_COMPLETE = 207;

	private static final int LINE_COUNT = 4;
	private Drawable mDefaultIcon;

	private AsyncImageManager mImgManager;

	private static final long REFRESH_INTERVAL = 500; // 进度条时间间隔

	private long mPrevRefreshTime = 0;

	private BroadcastReceiver mInstallReceiver; // 应用安装卸载监听

	private IDownloadService mDownloadController = null;

	public static final int RECOMMAPPSHOW = 0;

	public static final int RECOMMAPPNOTSHOW = 1;

	private int mShowOrNot = RECOMMAPPNOTSHOW;

	private Context mContext = null;
	private Handler mActivityHandler = null;

	private Drawable mDefaultBanner = null;
	
	private PurchaseStateManager mPurchaseStateManager = null;

	public AppsDetailContainer(Context context,
			IDownloadService downloadController) {
		mContext = context;
		mImgManager = AsyncImageManager.getInstance();
		mDefaultIcon = mContext.getResources().getDrawable(
				R.drawable.default_icon);
		mDownloadController = downloadController;
		mPurchaseStateManager = new PurchaseStateManager(mContext);
	}
	public void initValue(AppDetailInfoBean detailInfoBean, String pkg,
			int appId, int startType, String categoryId, int grade,
			String name, int showOrNot, Handler handler) {
		mDetailInfoBean = detailInfoBean;
		mDownloadPkg = pkg;
		mDownloadId = appId;
		mStartType = startType;
		mCategoryID = categoryId;
		mGrade = grade;
		mShowOrNot = showOrNot;
		mActivityHandler = handler;
		if (mStartType == AppsDetail.START_TYPE_GO_SEARCH_WIDGET) {
			// 由于GO搜索widget是从外部APK进入的，所以在此处统计入口
			AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(
					mContext,
					AppRecommendedStatisticsUtil.ENTRY_TYPE_GOWIDGET_SEARCH);
			AppRecommendedStatisticsUtil.getInstance().saveDetailsClick(
					mContext, mDownloadPkg, mDownloadId, 1);
			AppRecommendedStatisticsUtil.getInstance()
					.saveDetailsClick2MainTable(mContext, mDownloadPkg,
							mDownloadId, mCategoryID);
		}
		initView();
		initDownloadLayout();
		initHandler();
		registerDownloadReceiver();
		initInstallReceiver(mContext);
		initProgressByTask();
		if (mDetailInfoBean != null) {
			initAppTitle(name);
			initViewByBean(mDetailInfoBean);
		}
	}
	/**
	 * 初始化界面的方法
	 */
	private void initView() {
		mFrameLayout = new FrameLayout(mContext);
		mLinearLayout = new LinearLayout(mContext);
		mLinearLayout.setOrientation(LinearLayout.VERTICAL);
		mLinearLayout.setBackgroundColor(Color.WHITE);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mFrameLayout.addView(mLinearLayout, params);
		initNewProgress();
	}
	
	private void initNewProgress() {
		if (mView == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			mView = (RelativeLayout) layoutInflater.inflate(
					R.layout.layout_app_management_detail, null);
		}
		mFootLoadingLayout = (RelativeLayout) mView.findViewById(R.id.apps_detail_foot_loading);
	}

	public void onResume() {
		if (mHasObtainDetail) {
			int priceState = getPriceStateByBean(mDetailInfoBean);
			resetButtonState(priceState);
			initProgressByTask();
		} else {
			Message msg = mHandler.obtainMessage();
			msg.what = AppsThemeDetailActivity.MSG_RE_GET_DATA;
			mActivityHandler.sendMessage(msg);
		}
	}

	public View getView() {
		return mFrameLayout;
	}
	/**
	 * 初始化标题的方法
	 * 
	 * @param text
	 */
	private void initAppTitle(String text) {
		LayoutInflater layoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mAppTitle = (RelativeLayout) layoutInflater.inflate(
				R.layout.recomm_apps_management_detail_title, null);
		((TextView) mAppTitle
				.findViewById(R.id.recomm_apps_management_detai_title_text))
				.setText(text);

		ImageView ivLogo = (ImageView) mAppTitle
				.findViewById(R.id.recomm_apps_management_detai_title_logo);
		ivLogo.setVisibility(View.GONE);
		if (mStartType == AppsDetail.START_TYPE_APPMANAGEMENT
				|| mStartType == AppsDetail.START_TYPE_APPRECOMMENDED
				|| mStartType == AppsDetail.START_TYPE_GO_SEARCH_WIDGET
				|| mStartType == AppsDetail.START_TYPE_APPFUNC_SEARCH) {
			ivLogo.setBackgroundDrawable(mContext.getResources().getDrawable(
					R.drawable.app_center_icon_large));
		} else {
			ivLogo.setVisibility(View.GONE);
		}

		// 返回键
		ImageView ivBack = (ImageView) mAppTitle
				.findViewById(R.id.recomm_apps_management_detai_title_back);
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 如果是从应用游戏中心得widget中进入的,点击返回键需要调整应用游戏中心
				if (mStartType == AppsDetail.START_TYPE_WIDGET_APP) {
					// 统计入口
					AppRecommendedStatisticsUtil
							.getInstance()
							.saveCurrentEnter(
									mContext,
									AppRecommendedStatisticsUtil.ENTRY_TYPE_WIDGET);
					// 入口值尚未分配，暂时模拟入口值
					AppsManagementActivity.startAppCenter(mContext,
							MainViewGroup.ACCESS_FOR_SHORTCUT, false);
				}
			}
		});

		ImageView ivSearch = (ImageView) mAppTitle
				.findViewById(R.id.recomm_apps_management_detai_title_search);
		ivSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showSearchView();
			}
		});
	}
	private void showSearchView() {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		if (inflater == null) {
			return;
		}
		if (mSearchView != null) {
			removeSearchView();
		}

		mSearchView = (AppsManagementSearchView) inflater.inflate(
				R.layout.apps_management_search, null);
		// 填充搜索关键字
		if (mFrameLayout != null) {
			mFrameLayout.addView(mSearchView, new LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT));
		}
		mSearchView.setClickable(true);
		mSearchView.setVisibility(View.VISIBLE);
		mSearchView.setBackClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeSearchView();
			}
		});
		// 展示热门搜索关键字
//		mSearchView.showHotSearchKeyword();
		// 使输入框获取焦点
		EditText et = (EditText) mSearchView
				.findViewById(R.id.apps_management_search_edt);
		if (!et.isFocused()) {
			OnFocusChangeListener listener = et.getOnFocusChangeListener();
			et.setOnFocusChangeListener(null);
			et.requestFocus();
			et.setOnFocusChangeListener(listener);
		}
	}

	private void removeSearchView() {
		if (mSearchView != null && mFrameLayout != null) {
			mFrameLayout.removeView(mSearchView);
			mSearchView = null;
		}
	}

	/**
	 * 初始化下载进度条
	 */
	private void initDownloadLayout() {
		if (mView == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			mView = (RelativeLayout) layoutInflater.inflate(
					R.layout.layout_app_management_detail, null);
		}
		if (mDownloadLayout == null) {
			mDownloadLayout = (LinearLayout) mView
					.findViewById(R.id.app_detail_download_layout);
		}
		if (mPercentText == null) {
			mPercentText = (TextView) mView
					.findViewById(R.id.app_detail_download_percent);
		}

		// 取消
		if (mImgBtnDownloadCancel == null) {
			mImgBtnDownloadCancel = (ImageView) mView
					.findViewById(R.id.app_detail_download_cancel);
			mLinearLayoutBtnDownloadCancel = (LinearLayout) mView
					.findViewById(R.id.app_detail_download_cancel_click);
			mLinearLayoutBtnDownloadCancel
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (mDetailInfoBean == null) {
								return;
							}
							if (mActivityHandler == null) {
								return;
							}
							Message msg = mActivityHandler.obtainMessage();
							msg.what = AppsThemeDetailActivity.CANCEL_DOWNLOAD;
							msg.obj = String.valueOf(mDownloadId);
							mActivityHandler.sendMessage(msg);
							if (mProgressBar != null && mPercentText != null) {
								mProgressBar.setProgress(0);
								mPercentText.setText("0%");
							}
						}
					});
		}
		// 暂停或恢复
		if (mImgBtnDownloadPauseOrResume == null) {
			mImgBtnDownloadPauseOrResume = (ImageView) mView
					.findViewById(R.id.app_detail_download_pause_or_resume);
			mLinearLayoutBtnDownloadPauseOrResume = (LinearLayout) mView
					.findViewById(R.id.app_detail_download_pause_or_resume_click);
			mLinearLayoutBtnDownloadPauseOrResume
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (mActivityHandler == null) {
								return;
							}
							Message msg = mActivityHandler.obtainMessage();
							if (mIsDownloading) {
								msg.what = AppsThemeDetailActivity.STOP_DOWNLOAD;
							} else {
								msg.what = AppsThemeDetailActivity.RESTART_DOWNLOAD;
							}
							msg.obj = String.valueOf(mDownloadId);
							mActivityHandler.sendMessage(msg);
						}
					});
		}

		if (mProgressBar == null) {
			mProgressBar = (ProgressBar) mView
					.findViewById(R.id.app_detail_download_progress);

		}
	}

	private void initInstallReceiver(Context context) {
		if (context == null) {
			return;
		}
		if (mInstallReceiver == null) {
			mInstallReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					// 	GOLauncherEX v3.26 Fix Report 28597Error NullPointerException-at com.jiubang.ggheart.appgame.base.component.l.a(AppsDetailContainer.java:1576)
					// 	这里LayoutInflater.from(mContext)有可能报空指针，初步怀疑是调用recycle()后还收到安装广播导致的，加入context空指针保护 add by xiedezhi 2013.01.10
					if (mContext == null) {
						return;
					}
					if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
						// 应用程序安装事件
						resetButtonState(getPriceStateByBean(mDetailInfoBean));
					} else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent
							.getAction())) {
						// 应用程序卸载事件
						resetButtonState(getPriceStateByBean(mDetailInfoBean));
					}
				}
			};
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		context.registerReceiver(mInstallReceiver, intentFilter);
	}

	/**
	 * 根据下载任务设置是否是否显示下载进度条
	 */
	private void initProgressByTask() {
		if (mDetailInfoBean == null) {
			return;
		}
		// 判断任务是否正在下载中
		try {
			long id = mDetailInfoBean.mAppId;
			if (mDownloadController == null) {
				return;
			}
			DownloadTask task = mDownloadController.getDownloadTaskById(id);
			if (task != null) {
				if (mDownloadLayout == null) {
					initDownloadLayout();
				}
				if (task.getState() == DownloadTask.STATE_STOP) {
					mDownloadLayout.setVisibility(View.VISIBLE);
					int persent = task.getAlreadyDownloadPercent();
					mImgBtnDownloadPauseOrResume.setBackgroundDrawable(mContext
							.getResources().getDrawable(
									R.drawable.detail_download_resume_new));
					// 设置按钮状态为“取消下载”
					mIsDownloading = false;
					mPercentText.setText(persent + "%");
					mProgressBar.setProgress(persent);
					resetButtonState(DOWNLOAD_PAUSE);
				} else if (task.getState() == DownloadTask.STATE_WAIT) {
					mHandler.sendEmptyMessage(MSG_DOWNLOAD_WAIT);
				}
				if (mDownloadLayout != null) {
					mDownloadLayout.setVisibility(View.VISIBLE);
				}
			} else {
				if (mDownloadLayout != null) {
					mDownloadLayout.setVisibility(View.GONE);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void registerDownloadReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ICustomAction.ACTION_APP_DOWNLOAD);
		mContext.registerReceiver(mDownloadReceiver, intentFilter);
	}

	private void unregisterDownloadReceiver() {
		mContext.unregisterReceiver(mDownloadReceiver);
	}

	private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 加入context空指针保护 add by xiedezhi 2013.01.10
			if (mContext == null) {
				return;
			}
			if (intent != null) {
				DownloadTask task = intent
						.getParcelableExtra(AppDownloadListener.UPDATE_DOWNLOAD_INFO);
				if (task != null) {
					if (mDetailInfoBean != null) {
						long id = mDetailInfoBean.mAppId;
						if (task.getId() != id) {
							return;
						}
						resetDownloadState(task);
					}
				}
			}
		}
	};

	private void resetDownloadState(DownloadTask task) {
		if (mDownloadLayout == null) {
			initDownloadLayout();
		}
		if (task.getState() == DownloadTask.STATE_STOP) {
			mHandler.sendEmptyMessage(MSG_DOWNLOAD_STOP);
		} else if (task.getState() == DownloadTask.STATE_DOWNLOADING) {
			long currentTime = System.currentTimeMillis();
			if (currentTime - mPrevRefreshTime > REFRESH_INTERVAL) {
				mPrevRefreshTime = currentTime;
				int percent = task.getAlreadyDownloadPercent();
				Message.obtain(mHandler, MSG_DOWNLOAD_UPDATE_PROGRESS, percent,
						0).sendToTarget();
			}
		} else if (task.getState() == DownloadTask.STATE_START) {
			int percent = task.getAlreadyDownloadPercent();
			Message.obtain(mHandler, MSG_DOWNLOAD_START, percent, 0)
					.sendToTarget();
		} else if (task.getState() == DownloadTask.STATE_WAIT) {
			mHandler.sendEmptyMessage(MSG_DOWNLOAD_WAIT);
		} else if (task.getState() == DownloadTask.STATE_DELETE) {
			mHandler.sendEmptyMessage(MSG_DOWNLOAD_DESTORY);
		} else if (task.getState() == DownloadTask.STATE_FAIL) {
			mHandler.sendEmptyMessage(MSG_DOWNLOAD_FAILED);
		} else if (task.getState() == DownloadTask.STATE_FINISH) {
			int[] payTypes = mDetailInfoBean.mPayType;
			String path = LauncherEnv.Path.GOT_ZIP_HEMES_PATH;
			if (payTypes != null && payTypes.length > 0
					&& payTypes[0] == GoStoreOperatorUtil.PAY_VIP) {
				if (mDetailInfoBean.mTag == AppsThemeDetailActivity.LOCKER_TYPE) {
					path = LauncherEnv.Path.GOLOCKER_ZIP_HEMES_PATH;
				}
				mPurchaseStateManager.save(mDetailInfoBean.mPkgName, path);
			}
			mHandler.sendEmptyMessage(MSG_DOWNLOAD_COMPLETE);
		}
	}

	private void setIcon(final ImageView imageView, String imgUrl,
			String imgPath, String imgName, boolean setDefaultIcon) {
		if (mImgManager == null) {
			mImgManager = AsyncImageManager.getInstance();
		}
		imageView.setTag(imgUrl);
		// TODO:XIEDEZHI 这里能不能不要每次load图片都生成一个回调对象？
		Bitmap bm = mImgManager.loadImage(imgPath, imgName, imgUrl, true,
				false, AppGameDrawUtils.getInstance().mMaskIconOperator,
				new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageView.getTag().equals(imgUrl)) {
							imageView.setImageBitmap(imageBitmap);
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		if (bm != null) {
			imageView.setImageBitmap(bm);
			// imageView.setImageResource(R.drawable.app_center_back);
		} else {
			if (setDefaultIcon) {
				imageView.setImageDrawable(mDefaultIcon);
			} else {
				imageView.setImageDrawable(null);
			}
		}
	}

	private void setScreenShot(final ImageView imgView, String imgUrl,
			String imgPath, final int width, final int height) {
		if (mImgManager == null) {
			mImgManager = AsyncImageManager.getInstance();
		}
		if (TextUtils.isEmpty(imgUrl)) {
			return;
		}
		String imgName = String.valueOf(imgUrl.hashCode());
		Bitmap bm = mImgManager.loadImage(imgPath, imgName, imgUrl, true,
				false, null, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap != null && mContext != null) {
							AppGameDrawUtils
									.setImagePressDrawable(
											mContext,
											imgView,
											new BitmapDrawable(imageBitmap),
											new ColorDrawable(
													mContext.getResources()
															.getColor(
																	R.color.appgame_banner_press_color)));
						}
					}
				});
		if (bm != null) {
			if (mContext == null) {
				return;
			}
			AppGameDrawUtils.setImagePressDrawable(
					mContext,
					imgView,
					new BitmapDrawable(bm),
					new ColorDrawable(mContext.getResources().getColor(
							R.color.appgame_banner_press_color)));
		} else {
			if (mContext == null) {
				return;
			}
			AppGameDrawUtils.setImagePressDrawable(
					mContext,
					imgView,
					getDefalutThumbnailDrawable(),
					new ColorDrawable(mContext.getResources().getColor(
							R.color.appgame_banner_press_color)));
		}
	}

	private Drawable getDefalutThumbnailDrawable() {
		if (mDefaultBanner == null) {
			// 国内某些渠道没有应用游戏中心，所以要进行渠道控制，默认图片里面有名字，要跟着换
			// Add by wangzhuobin 2012.12.20
			int id = R.drawable.appcenter_default_banner;
			mDefaultBanner = mContext.getResources().getDrawable(id);
		}
		return mDefaultBanner;
	}

	private void initViewByBean(final AppDetailInfoBean infoBean) {
		if (infoBean == null) {
			return;
		}
		if (mView == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			mView = (RelativeLayout) layoutInflater.inflate(
					R.layout.layout_app_management_detail, null);
		}
		if (mView != null) {

			// TODO:图标
			mContentIconImageView = (SimpleImageView) mView
					.findViewById(R.id.contentIconImageView);
			// 设置默认图片资源ID，必须在setImgId之前设置
			String localPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
			if (!TextUtils.isEmpty(infoBean.mIconUrl)
					&& TextUtils.isEmpty(infoBean.mLocalIconName)) {
				infoBean.mLocalIconName = String.valueOf(infoBean.mIconUrl
						.hashCode());
			}
			// 如果预加载已经加载过图片，即图片不为空或不是默认图片时，则不进行图片设置，防止出现加载2次图片有闪烁的现象 add by zhengxiangcan
			if (mContentIconImageView.getDrawable() == null
					|| mContentIconImageView.getDrawable() == mDefaultIcon) {
				setIcon(mContentIconImageView, infoBean.mIconUrl, localPath,
						infoBean.mLocalIconName, true);
			}
			// 名称
			ScrollForeverTextView contentNameTextView = (ScrollForeverTextView) mView
					.findViewById(R.id.contentNameTextView);
			contentNameTextView.setText(infoBean.mName);

			// 更新日期
			TextView contentUpdateTimeTextView = (TextView) mView
					.findViewById(R.id.contentUpateTimeTextView);
			// 服务器支持，去掉时分秒部分。
			String updateTime = infoBean.mUpdateTime;
			if (updateTime != null && !"".equals(updateTime.trim())) {
				int endIndex = updateTime.indexOf(" ");
				if (endIndex > 0) {
					updateTime = updateTime.substring(0, endIndex);
				}
				contentUpdateTimeTextView.setText(mContext
						.getString(R.string.themestore_item_updatetime)
						+ " "
						+ updateTime);
			} else {
				contentUpdateTimeTextView.setText(mContext
						.getString(R.string.themestore_item_updatetime)
						+ " "
						+ mContext.getString(R.string.themestore_unknow));
			}
			// 软件类别
			TextView contentPackageCategory = (TextView) mView
					.findViewById(R.id.contentPackageCategory);
			contentPackageCategory.setText(infoBean.mAtype);

			// 安装包大小
			TextView contentPackageSizeTextView = (TextView) mView
					.findViewById(R.id.contentPackageSizeTextView);
			contentPackageSizeTextView.setText(infoBean.mSize);

			RelativeLayout securityLayout = (RelativeLayout) mView
					.findViewById(R.id.app_detail_security_layout);
			if (infoBean.mSecurityInfo != null
					&& infoBean.mSecurityInfo.mScore == 1) {
				((TextView) mView.findViewById(R.id.app_detail_security_tip))
						.setText(infoBean.mSecurityInfo.mName);
				ImageView securityIcon = (ImageView) mView
						.findViewById(R.id.app_detail_security_icon);
				if (!TextUtils.isEmpty(infoBean.mSecurityInfo.mIcon)) {
					setIcon(securityIcon, infoBean.mSecurityInfo.mIcon,
							localPath,
							String.valueOf(infoBean.mSecurityInfo.mIcon
									.hashCode()), true);
				}
				securityLayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mActivityHandler != null) {
							Message msg = mActivityHandler.obtainMessage();
							msg.what = AppsThemeDetailActivity.SHOW_SECURITY_DIALOG;
							msg.obj = infoBean;
							mActivityHandler.sendMessage(msg);
						}
					}
				});
			} else {
				securityLayout.setVisibility(View.GONE);
			}

			// 编辑推荐语,推荐理由
			RelativeLayout recommendReasonLayout = (RelativeLayout) mView
					.findViewById(R.id.apps_item_detail_recommend_reason_layout);
			if (TextUtils.isEmpty(infoBean.mRemdmsg)) {
				recommendReasonLayout.setVisibility(View.GONE);
			} else {
				recommendReasonLayout.setVisibility(View.VISIBLE);
				((TextView) mView
						.findViewById(R.id.apps_item_detail_recommend_reason))
						.setText(infoBean.mRemdmsg);
			}

			// 下载次数
			TextView contentDownloadCountTextView = (TextView) mView
					.findViewById(R.id.contentDownloadCountTextView);
			String downloadCount = infoBean.mDownloadCount;
			if (downloadCount == null || "".equals(downloadCount.trim())) {
				downloadCount = mContext.getResources().getString(
						R.string.themestore_infor_no_data);
			}
			contentDownloadCountTextView.setText(downloadCount);

			mScrollView = (AppDetailScrollViewSlowSpeed) mView
					.findViewById(R.id.scrollview);

			// 价格
			initPriceTextViewByBean(infoBean);

			// 星级
			float grade = 0.0f;
			if (mGrade == 0) {
				grade = (float) infoBean.mGrade / 2;
			} else {
				grade = (float) mGrade / 2;
			}
			RatingBar ratingBar = (RatingBar) mView
					.findViewById(R.id.app_detail_ratingbar);
			if (grade > 0) {
				ratingBar.setVisibility(View.VISIBLE);
				ratingBar.setRating(grade);
			} else {
				ratingBar.setVisibility(View.INVISIBLE);
			}

			// 版本
			TextView contentVersionTextView = (TextView) mView
					.findViewById(R.id.app_detail_version);
			contentVersionTextView.setText(mContext
					.getString(R.string.app_detail_version_tip)
					+ infoBean.mVersion);

			// 开发商
			TextView contentDeveloperTextView = (TextView) mView
					.findViewById(R.id.contentDeveloperTextView);
			contentDeveloperTextView.setText(mContext
					.getString(R.string.themestore_detail_developer)
					+ infoBean.mDeveloper);

			if (TextUtils.isEmpty(infoBean.mDetail)) {
				RelativeLayout layout = (RelativeLayout) mView
						.findViewById(R.id.descriptionItemRelativeLayout);
				layout.setVisibility(View.GONE);
			} else {
				RelativeLayout layout = (RelativeLayout) mView
						.findViewById(R.id.descriptionItemRelativeLayout);
				layout.setVisibility(View.VISIBLE);
				// 简介内容
				mContentDescriptionTextView = (TextView) mView
						.findViewById(R.id.contentDescriptionTextView);
				mContentDescriptionTextView.setText(infoBean.mDetail);
				// 更多简介内容
				mMoreDescriptionTextView = (ImageView) mView
						.findViewById(R.id.moreDescriptionTextView);
				int descriptionLineCount = checkTextViewLineCount(mContentDescriptionTextView);
				if (descriptionLineCount > LINE_COUNT) {
					mMoreDescriptionTextView.setVisibility(View.VISIBLE);
					mContentDescriptionTextView.setMaxLines(LINE_COUNT);
					mContentDescriptionTextView
							.setEllipsize(TextUtils.TruncateAt.END);
					OnClickListener listener = new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (mContentDescriptionTextView.getEllipsize() == null) {
								mContentDescriptionTextView
										.setMaxLines(LINE_COUNT);
								mContentDescriptionTextView
										.setEllipsize(TextUtils.TruncateAt.END);
								mMoreDescriptionTextView
										.setBackgroundResource(R.drawable.app_detail_more_arrow_down);
							} else {
								mContentDescriptionTextView
										.setMaxLines(Integer.MAX_VALUE);
								mContentDescriptionTextView.setEllipsize(null);
								mMoreDescriptionTextView
										.setBackgroundResource(R.drawable.app_detail_more_arrow_up);
							}
						}
					};
					mMoreDescriptionTextView.setOnClickListener(listener);
					mContentDescriptionTextView.setOnClickListener(listener);
				}

			}

			if (TextUtils.isEmpty(infoBean.mUpdateLog)) {
				RelativeLayout layout = (RelativeLayout) mView
						.findViewById(R.id.updateItemRelativeLayout);
				layout.setVisibility(View.GONE);
			} else {
				RelativeLayout layout = (RelativeLayout) mView
						.findViewById(R.id.updateItemRelativeLayout);
				layout.setVisibility(View.VISIBLE);
				// 更新内容
				mContentUpdateContentTextView = (TextView) mView
						.findViewById(R.id.contentUpdateContentTextView);
				mContentUpdateContentTextView.setText(infoBean.mUpdateLog);

				// 更多更新内容
				mMoreUpdateTextView = (ImageView) mView
						.findViewById(R.id.moreUpdateTextView);
				int updateLineCount = checkTextViewLineCount(mContentUpdateContentTextView);
				if (updateLineCount > LINE_COUNT) {
					mMoreUpdateTextView.setVisibility(View.VISIBLE);
					mContentUpdateContentTextView.setMaxLines(LINE_COUNT);
					mContentUpdateContentTextView
							.setEllipsize(TextUtils.TruncateAt.END);
					OnClickListener listener = new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (mContentUpdateContentTextView.getEllipsize() == null) {
								mContentUpdateContentTextView
										.setMaxLines(LINE_COUNT);
								mContentUpdateContentTextView
										.setEllipsize(TextUtils.TruncateAt.END);
								mMoreUpdateTextView
										.setBackgroundResource(R.drawable.app_detail_more_arrow_down);
							} else {
								mContentUpdateContentTextView
										.setMaxLines(Integer.MAX_VALUE);
								mContentUpdateContentTextView
										.setEllipsize(null);
								mMoreUpdateTextView
										.setBackgroundResource(R.drawable.app_detail_more_arrow_up);
							}
						}
					};
					mMoreUpdateTextView.setOnClickListener(listener);
					mContentUpdateContentTextView.setOnClickListener(listener);
				}
			}
			// 意见反馈
			mFeedBackEmail = (LinearLayout) mView
					.findViewById(R.id.feedbackemail);
			if (mHasObtainDetail) {
				mFeedBackEmail.setVisibility(View.VISIBLE);
				mFeedBackEmail.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(mContext, AppFeedbackActivity.class);
						intent.putExtra(AppFeedbackActivity.ENTRANCE,
								AppFeedbackActivity.ENTRANCE_FROM_APP_DETAIL);
						intent.putExtra(AppFeedbackActivity.PACKAGENAME,
								infoBean.mPkgName);
						mContext.startActivity(intent);
					}
				});
			} else {
				mFeedBackEmail.setVisibility(View.GONE);
			}

			// 缩略图
			ArrayList<String> thumbnailUrlsArrayList = infoBean.mSmallPicUrls;
			mGoStoreDetailScrollViewGroup = (GoStoreDetailScrollViewGroup) mView
					.findViewById(R.id.goStoreDetailScrollViewGroup);
			
			mAppDetailIndicators = (AppDetailIndicators) mView
					.findViewById(R.id.pointerView);
			mSimpleImageViews = new ArrayList<ImageView>();
			
			mGoStoreDetailScrollViewGroup.setScrollView(mScrollView);
			
			int bitmapWidth = mContext.getResources()
					.getDimensionPixelSize(
							R.dimen.app_detail_scroll_view_width);
			int bitmapHeight = mContext.getResources()
					.getDimensionPixelSize(
							R.dimen.app_detail_scroll_view_height);
			mGoStoreDetailScrollViewGroup.setChildWidth(bitmapWidth);
			mGoStoreDetailScrollViewGroup.setChildHeight(bitmapHeight);
			mGoStoreDetailScrollViewGroup.setGap(DrawUtils.dip2px(11f));

			if (thumbnailUrlsArrayList != null
					&& thumbnailUrlsArrayList.size() > 0) {

				//				LinearLayout thumbnailLinearLayout = (LinearLayout) mView
				//						.findViewById(R.id.thumbnailLinearLayout);

				int size = thumbnailUrlsArrayList.size();
				mGoStoreDetailScrollViewGroup.removeAllViews();
				mAppDetailIndicators.removeAllViews();
				mAppDetailIndicators.setIndicatorCount(size);
				mGoStoreDetailScrollViewGroup
						.setIndicators(mAppDetailIndicators);
				ImageView simpleImageView = null;
				// flag为true,加载截图
				boolean flag = true;
				if (GoStorePhoneStateUtil.isWifiEnable(GOLauncherApp
						.getContext())
						|| AppGameSettingData.getInstance(mContext)
								.getTrafficSavingMode() == AppGameSettingData.LOADING_ALL_IMAGES) {
					flag = true;
				} else {
					flag = false;
				}
				for (int i = 0; i < size; i++) {
					final int curIndex = i;
					// 由于此处的都是JPG图片,无法直接通过StateListDrawable的方式来实现点击效果,所以用德志的View来实现点击效果
					simpleImageView = new ImageView(mContext);
					if (flag) {
						String imgUrl = thumbnailUrlsArrayList.get(i);
						simpleImageView.setScaleType(ScaleType.FIT_XY);
						setScreenShot(simpleImageView, imgUrl,
								LauncherEnv.Path.APP_MANAGER_ICON_PATH,
								bitmapWidth, bitmapHeight);
						// 为每个缩略图添加处理
						simpleImageView
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										Intent intent = new Intent();
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										intent.setClass(mContext,
												AppDetailImageActivity.class);
										intent.putExtra(
												GoStorePublicDefine.SCAN_IMAGE_IDS_KEY,
												mDetailInfoBean.mLargePicUrls);
										// 添加当前图片索引值
										intent.putExtra(
												GoStorePublicDefine.SCAN_IMAGE_CUR_INDEX_KEY,
												curIndex);
										mContext.startActivity(intent);
									}
								});
					} else {
						AppGameDrawUtils
								.setImagePressDrawable(
										mContext,
										simpleImageView,
										getDefalutThumbnailDrawable(),
										new ColorDrawable(
												mContext.getResources()
														.getColor(
																R.color.appgame_banner_press_color)));
					}

					// // 设置缩略图左右间距
					// 把SimpleImageView添加到集合中，以便回收
					mSimpleImageViews.add(simpleImageView);
					mGoStoreDetailScrollViewGroup.addView(simpleImageView);
				}
				mGoStoreDetailScrollViewGroup.goToScreen(0, true);
			} else {
				mGoStoreDetailScrollViewGroup.removeAllViews();
				mAppDetailIndicators.setIndicatorCount(2);
				mGoStoreDetailScrollViewGroup
						.setIndicators(mAppDetailIndicators);
				ImageView simpleImageView = null;
				for (int i = 0; i < 2; i++) {
					simpleImageView = new ImageView(mContext);
					AppGameDrawUtils
					.setImagePressDrawable(
							mContext,
							simpleImageView,
							getDefalutThumbnailDrawable(),
							new ColorDrawable(
									mContext
									.getResources()
									.getColor(
											R.color.appgame_banner_press_color)));
					mSimpleImageViews.add(simpleImageView);
					mGoStoreDetailScrollViewGroup.addView(simpleImageView);
				}
				mGoStoreDetailScrollViewGroup.goToScreen(0, true);
			}
		}

		// 初始化相关推荐,由于外层已经有一个ScrollView,不适宜再加入ListView
		// 所以手动添加LinearLayout
		if (mHasObtainDetail) {
//			mView.findViewById(R.id.apps_item_detail_another_layout)
//			.setVisibility(View.VISIBLE);
			initAnotherViewByBean(infoBean);
			hideFootLoading();
		} else {
			mView.findViewById(R.id.apps_item_detail_another_layout)
			.setVisibility(View.GONE);
		}

		if (mLinearLayout != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			// 暂时先加入保护
			if (mView.getParent() != null) {
				try {
					((ViewGroup) mView.getParent()).removeView(mView);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (mView.getParent() == null) {
				mLinearLayout.addView(mView, params);
			}
		}
	}

	private void initAnotherViewByBean(final AppDetailInfoBean infoBean) {
		if (infoBean == null) {
			return;
		}

		final List<BoutiqueApp> recomApps = infoBean.mRecomApps;

		if (recomApps == null || recomApps.size() == 0) {
			return;
		}
		
		String localPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;

		LinearLayout anotherLayout = (LinearLayout) mView
				.findViewById(R.id.app_item_detail_another_list);
		RelativeLayout moreSimilarLayout = (RelativeLayout) mView
				.findViewById(R.id.more_similar_app);
		moreSimilarLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String pkgName = infoBean.mPkgName;
				if (pkgName == null || "".equals(pkgName)) {
					return;
				}
				Intent in = new Intent(mContext,
						MoreRecommendedAppsActivity.class);
				in.putExtra(MoreRecommendedAppsActivity.sPACKAGE_NAME, pkgName);
				mContext.startActivity(in);
			}
		});
//		moreSimilarLayout.setVisibility(View.GONE);
		LayoutInflater inflater = LayoutInflater.from(mContext);
		if (inflater == null) {
			return;
		}
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		int size = recomApps.size();

		int itemCount = size / 2;

		for (int i = 0; i < itemCount; ++i) {

			LinearLayout recomLayout = (LinearLayout) inflater.inflate(
					R.layout.apps_detail_twocell, null);
			// 左边
			final View viewLeft = recomLayout
					.findViewById(R.id.container_detail_left);
			final BoutiqueApp recomAppLeft = recomApps.get(i * 2);

			if (!TextUtils.isEmpty(recomAppLeft.info.icon)
					&& TextUtils.isEmpty(recomAppLeft.picLocalFileName)) {
				recomAppLeft.picLocalFileName = String
						.valueOf(recomAppLeft.info.icon.hashCode());
			}
			// 设置ICON
			ImageView iconViewLeft = (ImageView) viewLeft
					.findViewById(R.id.app_detail_recommend_icon);

			iconViewLeft.setImageDrawable(mDefaultIcon);

			setIcon(iconViewLeft, recomAppLeft.info.icon, localPath,
					recomAppLeft.picLocalFileName, true);

			// 设置应用名
			TextView nameViewLeft = (TextView) viewLeft
					.findViewById(R.id.app_detail_recommend_name);
			nameViewLeft.setText(recomAppLeft.info.name);

			// 设置星级
			RatingBar ratingBarViewLeft = (RatingBar) viewLeft
					.findViewById(R.id.app_detail_recommend_ratingbar);
			float gradeLeft = (float) (recomAppLeft.info.grade) / 2.0f;
			ratingBarViewLeft.setRating(gradeLeft);

			// 右边
			final View viewRight = recomLayout
					.findViewById(R.id.container_detail_right);
			final BoutiqueApp recomAppRight = recomApps.get(i * 2 + 1);

			if (!TextUtils.isEmpty(recomAppRight.info.icon)
					&& TextUtils.isEmpty(recomAppRight.picLocalFileName)) {
				recomAppRight.picLocalFileName = String
						.valueOf(recomAppRight.info.icon.hashCode());
			}

			// 设置ICON
			ImageView iconViewRight = (ImageView) viewRight
					.findViewById(R.id.app_detail_recommend_icon);
			iconViewRight.setImageDrawable(mDefaultIcon);

			setIcon(iconViewRight, recomAppRight.info.icon, localPath,
					recomAppRight.picLocalFileName, true);

			// 设置应用名
			TextView nameViewRight = (TextView) viewRight
					.findViewById(R.id.app_detail_recommend_name);
			nameViewRight.setText(recomAppRight.info.name);

			// 设置星级
			RatingBar ratingBarViewRight = (RatingBar) viewRight
					.findViewById(R.id.app_detail_recommend_ratingbar);
			float gradeRight = (float) (recomAppLeft.info.grade) / 2.0f;

			ratingBarViewRight.setRating(gradeRight);

			final int indexleft = i * 2 + 1;

			final int indexright = i * 2 + 2;

			OnTouchListener listenerLeft = new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_MOVE) {
						viewLeft.setBackgroundResource(color.center_background);
						return false;
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						viewLeft.setBackgroundResource(color.center_background);
						AppsDetail.jumpToDetailNew(mContext, recomAppLeft,
								String.valueOf(infoBean.mRecmdId), mStartType,
								indexleft, true, RECOMMAPPNOTSHOW);
					} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
						viewLeft.setBackgroundResource(R.drawable.tab_press);
					} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
						viewLeft.setBackgroundResource(color.center_background);
					}
					return true;
				}
			};

			viewLeft.setOnTouchListener(listenerLeft);

			OnTouchListener listenerRight = new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_MOVE) {
						viewRight
								.setBackgroundResource(color.center_background);
						return false;
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						viewRight
								.setBackgroundResource(color.center_background);
						AppsDetail.jumpToDetailNew(mContext, recomAppRight,
								String.valueOf(infoBean.mRecmdId), mStartType,
								indexright, true, RECOMMAPPNOTSHOW);
					} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
						viewRight.setBackgroundResource(R.drawable.tab_press);
					} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
						viewRight
								.setBackgroundResource(color.center_background);
					}
					return true;
				}
			};

			viewRight.setOnTouchListener(listenerRight);

			if (mShowOrNot == RECOMMAPPNOTSHOW) {
				mView.findViewById(R.id.apps_item_detail_another_layout)
						.setVisibility(View.GONE);
			} else {
				mView.findViewById(R.id.apps_item_detail_another_layout)
						.setVisibility(View.VISIBLE);
				anotherLayout.addView(recomLayout, params);
				ImageView divider = new ImageView(mContext);
				divider.setBackgroundDrawable(mContext.getResources()
						.getDrawable(R.drawable.listview_divider));
				anotherLayout.addView(divider, LayoutParams.FILL_PARENT, 1);
			}

		}

	}

	private void initPriceTextViewByBean(final AppDetailInfoBean infoBean) {
		if (infoBean != null && mView != null) {
			// 价格
			if (mContentPriceTextView == null) {
				mContentPriceTextView = (TextView) mView
						.findViewById(R.id.contentPriceTextView);
			}
			if (mDownLoadCLick == null) {
				mDownLoadCLick = (LinearLayout) mView
						.findViewById(R.id.downloadclick);
			}
			if (mDownloadIcon == null) {
				mDownloadIcon = (ImageView) mView
						.findViewById(R.id.downloadimage);
			}
			int priceState = getPriceStateByBean(infoBean);
			resetButtonState(priceState);
		}
	}

	/**
	 * @param infoBean
	 * @return
	 */
	private int getPriceStateByBean(final AppDetailInfoBean infoBean) {
		int priceState = -1;
		if (infoBean == null) {
			return priceState;
		}
		int downloadState = getDownloadState(mDetailInfoBean,
				mDownloadController);
		if (downloadState == DownloadTask.STATE_STOP) {
			// 暂停
			priceState = DOWNLOAD_PAUSE;
		} else if (downloadState == DownloadTask.STATE_WAIT
				|| downloadState == DownloadTask.STATE_DOWNLOADING
				|| downloadState == DownloadTask.STATE_RESTART
				|| downloadState == DownloadTask.STATE_START
				) {
			priceState = DOWNLOADING;
//		} else if (downloadState == DownloadTask.STATE_START) {
//			priceState = DOWNLOAD_WAIT;
		} else {
			String packageName = infoBean.mPkgName;
			if (GoStoreAppInforUtil.isApplicationExsit(mContext, packageName)) {
				// 如果应用程序已安装
				if (infoBean.mVersionCode != null && !"".equals(infoBean.mVersionCode)) {
					int versionCode = Integer.parseInt(infoBean.mVersionCode);
					if (GoStoreAppInforUtil.isNewToAlreadyInstall(mContext,
							packageName, versionCode)) {
						// 应用程序可更新
						if (isApkExist(infoBean)) {
							// 应用程序可更新且安装包已存在
							priceState = INSTALL;
						} else {
							priceState = UPDATE_POSSIBLE;
						}
					} else {
						priceState = INSTALLED;
					}
				} else {
					// 应用程序已安装，但无法判断是否最新
					if (isApkExist(infoBean)) {
						// 应用程序可更新且安装包已存在
						priceState = INSTALL;
					} else {
						priceState = UPDATE_POSSIBLE;
					}
				}
			} else {
				if (isApkExist(infoBean)) {
					// 应用程序未安装且安装包存在
					priceState = INSTALL;
				} else if (ZipResources.isZipThemeExist(infoBean.mPkgName)) {
					priceState = INSTALLED;
				} else {
					priceState = DOWNLOAD_POSSIBLE;
				}
			}
		}
		return priceState;
	}

	private int getDownloadState(final AppDetailInfoBean infoBean,
			IDownloadService downloadController) {
		int downloadState = -1;
		if (infoBean == null || downloadController == null) {
			return downloadState;
		}
		long id = infoBean.mAppId;
		DownloadTask task = null;
		try {
			task = downloadController.getDownloadTaskById(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (task == null) {
			return downloadState;
		}
		downloadState = task.getState();
		return downloadState;
	}

	/**
	 * 根据包名启动程序
	 * 
	 * @param context
	 * @param pkgName
	 * @return
	 */
	public void startApp(final AppDetailInfoBean infoBean) {
		if (infoBean == null) {
			throw new IllegalArgumentException("context can not be null");
		}
		String pkgName = infoBean.mPkgName;
		int tag = infoBean.mTag;
		if (pkgName.startsWith("com.jiubang.goscreenlock.theme")) {
			if (mActivityHandler != null) {
				Message msg = mActivityHandler.obtainMessage();
				msg.what = AppsThemeDetailActivity.APPLY_LOCKER_THEME;
				msg.obj = infoBean;
				mActivityHandler.sendMessage(msg);
			}
		} else if (tag == AppsThemeDetailActivity.THEME_TYPE
				|| tag == AppsThemeDetailActivity.SUPER_THEME_TYPE) {
			//主题
			if (mActivityHandler != null) {
				Message msg = mActivityHandler.obtainMessage();
				msg.what = AppsThemeDetailActivity.APPLY_THEME;
				msg.obj = infoBean;
				mActivityHandler.sendMessage(msg);
			}
		} else if (tag <= 2
				|| tag == AppsThemeDetailActivity.LIVE_WALLPAPER) {
			//应用
			if (mActivityHandler != null) {
				Message msg = mActivityHandler.obtainMessage();
				msg.what = AppsThemeDetailActivity.OPEN_APK;
				msg.obj = infoBean;
				mActivityHandler.sendMessage(msg);
			}
		};
	}

	public static void uninstallApp(Context context, String packageName) {
		if (context != null && packageName != null) {
			Uri packageURI = Uri.parse("package:" + packageName);
			if (packageURI == null) {
				return;
			}
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
					packageURI);
			uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(uninstallIntent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void initHandler() {
		if (mHandler == null) {
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
						case MSG_DOWNLOAD_START :
							if (mView != null && mContentPriceTextView != null
									&& mDownloadLayout != null
									&& mImgBtnDownloadPauseOrResume != null
									&& mImgBtnDownloadCancel != null
									&& mDownLoadCLick != null
									&& mDownloadIcon != null) {
								resetButtonState(DOWNLOADING);
								mDownloadLayout.setVisibility(View.VISIBLE);
								mImgBtnDownloadPauseOrResume
										.setBackgroundDrawable(mContext
												.getResources()
												.getDrawable(
														R.drawable.detail_download_pause_new_unable));
								mImgBtnDownloadCancel.setBackgroundDrawable(mContext
												.getResources()
												.getDrawable(
														R.drawable.app_detail_download_cancel_new_unable));
								mLinearLayoutBtnDownloadCancel.setClickable(false);
								mLinearLayoutBtnDownloadPauseOrResume.setClickable(false);
								mIsDownloading = true;
							}
							break;
						case MSG_DOWNLOAD_DESTORY :
							if (mView != null && mContentPriceTextView != null
									&& mDownloadLayout != null
									&& mDownLoadCLick != null
									&& mDownloadIcon != null) {
								mIsDownloading = false;
								mDownloadLayout.setVisibility(View.GONE);
								resetButtonState(getPriceStateByBean(mDetailInfoBean));
							}
							break;
						case MSG_DOWNLOAD_UPDATE_PROGRESS :
							if (mContentPriceTextView != null
									&& mDownloadLayout != null
									&& mImgBtnDownloadPauseOrResume != null
									&& mImgBtnDownloadCancel != null
									&& mPercentText != null
									&& mProgressBar != null
									&& mDownLoadCLick != null
									&& mDownloadIcon != null) {
								mDownLoadCLick.setVisibility(View.GONE);
								mDownloadIcon.setVisibility(View.GONE);
								mContentPriceTextView
										.setVisibility(View.INVISIBLE);
								mDownloadLayout.setVisibility(View.VISIBLE);
								mImgBtnDownloadPauseOrResume
										.setBackgroundDrawable(mContext
												.getResources()
												.getDrawable(
														R.drawable.detail_download_pause_new));
								mImgBtnDownloadCancel.setBackgroundDrawable(mContext
												.getResources()
												.getDrawable(
														R.drawable.app_detail_download_cancel_new));
								mLinearLayoutBtnDownloadCancel.setClickable(true);
								mLinearLayoutBtnDownloadPauseOrResume.setClickable(true);
								int persent = msg.arg1;
								// 设置按钮状态为“取消下载”
								mIsDownloading = true;
								mPercentText.setText(persent + "%");
								mProgressBar.setProgress(persent);
							}
							break;
						case MSG_DOWNLOAD_FAILED :
							if (mView != null && mContentPriceTextView != null
									&& mDownloadLayout != null
									&& mDownLoadCLick != null
									&& mDownloadIcon != null) {
								Toast.makeText(
										mContext,
										mContext.getString(R.string.apps_management_download_failed),
										Toast.LENGTH_LONG).show();
								mIsDownloading = false;
								mDownloadLayout.setVisibility(View.GONE);
								resetButtonState(getPriceStateByBean(mDetailInfoBean));
							}
							break;
						case MSG_DOWNLOAD_STOP :
							if (mView != null && mContentPriceTextView != null
									&& mImgBtnDownloadPauseOrResume != null
									&& mDownLoadCLick != null
									&& mDownloadIcon != null) {
								// 设置为暂停
								mIsDownloading = false;
								mDownloadLayout.setVisibility(View.VISIBLE);
								mImgBtnDownloadPauseOrResume
										.setBackgroundDrawable(mContext
												.getResources()
												.getDrawable(
														R.drawable.detail_download_resume_new));
								resetButtonState(DOWNLOAD_PAUSE);
							}
							break;
						case MSG_DOWNLOAD_COMPLETE :
							if (mView != null && mContentPriceTextView != null
									&& mDownloadLayout != null
									&& mDownLoadCLick != null
									&& mDownloadIcon != null) {
								mIsDownloading = false;

								resetButtonState(INSTALL);
								mDownloadLayout.setVisibility(View.GONE);
							}
							break;
						case MSG_DOWNLOAD_WAIT :
							if (mView != null && mContentPriceTextView != null
									&& mDownloadLayout != null
									&& mImgBtnDownloadPauseOrResume != null
									&& mDownLoadCLick != null
									&& mDownloadIcon != null) {
								// 下载等待暂时跟开始下载一致
								resetButtonState(DOWNLOADING);
								mDownloadLayout.setVisibility(View.VISIBLE);
								mImgBtnDownloadPauseOrResume
										.setBackgroundDrawable(mContext
												.getResources()
												.getDrawable(
														R.drawable.detail_download_pause_new));
								mIsDownloading = true;
							}
							break;
						default :
							break;
					}
				}
			};
		}
	}
	private static boolean isApkExist(AppDetailInfoBean infoBean) {
		if (infoBean != null) {
			String version = infoBean.mVersion;
			if (version != null && version.trim().startsWith("V")) {
				version = version.trim().substring(1);
			}
			String apkName = GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH
					+ infoBean.mPkgName + "_" + version + ".apk";
			return FileUtil.isFileExist(apkName);
		}
		return false;
	}

	protected void resetButtonState(int state) {
		if (mView == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			mView = (RelativeLayout) layoutInflater.inflate(
					R.layout.layout_app_management_detail, null);
		}
		if (mContentPriceTextView == null) {
			mContentPriceTextView = (TextView) mView
					.findViewById(R.id.contentPriceTextView);
		}
		if (mDownLoadCLick == null) {
			mDownLoadCLick = (LinearLayout) mView
					.findViewById(R.id.downloadclick);
		}
		if (mDownloadIcon == null) {
			mDownloadIcon = (ImageView) mView.findViewById(R.id.downloadimage);
		}
		switch (state) {
			case DOWNLOAD_POSSIBLE :
				if (mDetailInfoBean == null) {
					return;
				}

				mContentPriceTextView.setVisibility(View.VISIBLE);
				mDownloadIcon.setVisibility(View.VISIBLE);
				mDownLoadCLick.setVisibility(View.VISIBLE);

				String price = "";
				if (mDetailInfoBean.mIsFree) {
					price = mContext.getString(R.string.widget_choose_download);
				} else {
					price = mDetailInfoBean.mPrice;
				}
				mDownloadIcon
						.setBackgroundResource(R.drawable.app_detail_download_icon);
				mContentPriceTextView.setText(price);
				mContentPriceTextView.setTextColor(Color.WHITE);
				mDownLoadCLick
						.setBackgroundResource(R.drawable.appgame_download_btn_selector);
				mDownLoadCLick.setOnClickListener(new WrapOnClickListener() {

					@Override
					public void withoutSDCard(View v) {
						// 点击响应处理。
						mDownloadPkg = mDetailInfoBean.mPkgName;
						mDownloadId = mDetailInfoBean.mAppId;
						if (mDetailInfoBean.mDownloadType == AppsThemeDetailActivity.DETAIL_DOWNLOAD_TYPE_FTP) {
							mContentPriceTextView.setVisibility(View.INVISIBLE);
							mDownLoadCLick.setVisibility(View.INVISIBLE);
							mContentPriceTextView.setVisibility(View.INVISIBLE);
							mDownloadLayout.setVisibility(View.VISIBLE);
							if (mActivityHandler != null) {
								Message msg = mActivityHandler.obtainMessage();
								msg.what = AppsThemeDetailActivity.DOWNLOAD_CLICK_COUNT;
								msg.obj = mDetailInfoBean;
								mActivityHandler.sendMessage(msg);
							}
						}
						// 根据服务器下发的下载类型下载应用
						downloadApk(mContext, mDetailInfoBean, false);
					}

					@Override
					public void withSDCard(View v) {
						// 点击响应处理。
						mDownloadPkg = mDetailInfoBean.mPkgName;
						mDownloadId = mDetailInfoBean.mAppId;
						if (mDetailInfoBean.mDownloadType == AppsThemeDetailActivity.DETAIL_DOWNLOAD_TYPE_FTP) {
							mContentPriceTextView.setVisibility(View.INVISIBLE);
							mDownLoadCLick.setVisibility(View.INVISIBLE);
							mContentPriceTextView.setVisibility(View.INVISIBLE);
							mDownloadLayout.setVisibility(View.VISIBLE);
							if (mActivityHandler != null) {
								Message msg = mActivityHandler.obtainMessage();
								msg.what = AppsThemeDetailActivity.DOWNLOAD_CLICK_COUNT;
								msg.obj = mDetailInfoBean;
								mActivityHandler.sendMessage(msg);
							}
						}
						// 根据服务器下发的下载类型下载应用
						downloadApk(mContext, mDetailInfoBean, true);
					}
				});
				break;

			case UPDATE_POSSIBLE :
				mDownloadIcon.setVisibility(View.VISIBLE);
				mDownLoadCLick.setVisibility(View.VISIBLE);
				mContentPriceTextView.setVisibility(View.VISIBLE);
				mDownloadIcon
						.setBackgroundResource(R.drawable.app_detail_update_icon);
				mContentPriceTextView.setText(R.string.themestore_can_update);
				mContentPriceTextView.setTextColor(Color.WHITE);
				mDownLoadCLick
						.setBackgroundResource(R.drawable.appgame_update_btn_selector);
				mDownLoadCLick.setOnClickListener(new WrapOnClickListener() {
					@Override
					public void withoutSDCard(View v) {
						// 点击响应处理。
						mDownloadPkg = mDetailInfoBean.mPkgName;
						mDownloadId = mDetailInfoBean.mAppId;
						// 统计更新点击
						if (mDetailInfoBean.mDownloadType == AppsThemeDetailActivity.DETAIL_DOWNLOAD_TYPE_FTP) {
							mContentPriceTextView.setVisibility(View.INVISIBLE);
							mDownLoadCLick.setVisibility(View.INVISIBLE);
							mContentPriceTextView.setVisibility(View.INVISIBLE);
							mDownloadLayout.setVisibility(View.VISIBLE);
							if (mActivityHandler != null) {
								Message msg = mActivityHandler.obtainMessage();
								msg.what = AppsThemeDetailActivity.UPDATE_CLICK_COUNT;
								msg.obj = mDetailInfoBean;
								mActivityHandler.sendMessage(msg);
							}
						}
						// 根据服务器下发的下载类型下载应用
						downloadApk(mContext, mDetailInfoBean, false);
					}

					@Override
					public void withSDCard(View v) {
						// 点击响应处理。
						mDownloadPkg = mDetailInfoBean.mPkgName;
						mDownloadId = mDetailInfoBean.mAppId;
						// 统计更新点击
						if (mDetailInfoBean.mDownloadType == AppsThemeDetailActivity.DETAIL_DOWNLOAD_TYPE_FTP) {
							mContentPriceTextView.setVisibility(View.INVISIBLE);
							mDownLoadCLick.setVisibility(View.INVISIBLE);
							mContentPriceTextView.setVisibility(View.INVISIBLE);
							mDownloadLayout.setVisibility(View.VISIBLE);
							if (mActivityHandler != null) {
								Message msg = mActivityHandler.obtainMessage();
								msg.what = AppsThemeDetailActivity.UPDATE_CLICK_COUNT;
								msg.obj = mDetailInfoBean;
								mActivityHandler.sendMessage(msg);
							}
						}
						// 根据服务器下发的下载类型下载应用
						downloadApk(mContext, mDetailInfoBean, true);
					}
				});
				break;

			case INSTALLED :
				mDownloadIcon.setVisibility(View.VISIBLE);
				mDownLoadCLick.setVisibility(View.VISIBLE);
				mContentPriceTextView.setVisibility(View.VISIBLE);
				mDownloadIcon
						.setBackgroundResource(R.drawable.app_detail_open_icon);
				mContentPriceTextView
						.setText(R.string.app_detail_installed_open);
				mContentPriceTextView.setTextColor(mContext.getResources()
						.getColor(R.color.appgame_download_btn_white));
				mDownLoadCLick
						.setBackgroundResource(R.drawable.appgame_download_btn_selector);
				mDownLoadCLick.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						startApp(mDetailInfoBean);
					}
				});
				break;
			case INSTALL :
				// 应用程序未安装且安装包已经存在
				if (mDetailInfoBean == null) {
					return;
				}
				mDownloadIcon.setVisibility(View.VISIBLE);
				mDownLoadCLick.setVisibility(View.VISIBLE);
				mContentPriceTextView.setVisibility(View.VISIBLE);
				mDownloadIcon
						.setBackgroundResource(R.drawable.app_detail_install_icon);

				mDownLoadCLick.setBackgroundDrawable(mContext.getResources()
						.getDrawable(R.drawable.appgame_download_btn_selector));
				mContentPriceTextView.setText(R.string.gostore_detail_install);
				mContentPriceTextView.setTextColor(Color.WHITE);
				mDownLoadCLick.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String version = mDetailInfoBean.mVersion;
						if (version != null && version.trim().startsWith("V")) {
							version = version.trim().substring(1);
						}
						String apkName = GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH
								+ mDetailInfoBean.mPkgName
								+ "_"
								+ version + ".apk";
						if (FileUtil.isFileExist(apkName)) {
							// 安装统计
							if (mStartType == AppsDetail.START_TYPE_APPMANAGEMENT) {
								AppManagementStatisticsUtil
										.getInstance()
										.saveReadyToInstall(
												mContext,
												mDetailInfoBean.mPkgName,
												String.valueOf(mDetailInfoBean.mAppId),
												0, String.valueOf(mCategoryID));
							} else if (mStartType == AppsDetail.START_TYPE_APPRECOMMENDED
									|| mStartType == AppsDetail.START_TYPE_WIDGET_APP
									|| mStartType == AppsDetail.START_TYPE_GO_SEARCH_WIDGET
									|| mStartType == AppsDetail.START_TYPE_APPFUNC_SEARCH) {
								AppRecommendedStatisticsUtil
										.getInstance()
										.saveReadyToInstall(
												mContext,
												mDetailInfoBean.mPkgName,
												String.valueOf(mDetailInfoBean.mAppId),
												0, String.valueOf(mCategoryID));
							}
							try {
								ApkInstallUtils.installApk(apkName);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
				break;
			case DOWNLOAD_PAUSE :
				// 下载暂停
				if (mDetailInfoBean == null) {
					return;
				}
				mDownloadIcon.setVisibility(View.GONE);
				mDownLoadCLick.setVisibility(View.GONE);
				mContentPriceTextView.setVisibility(View.GONE);
				mDownLoadCLick.setBackgroundDrawable(null);
				mContentPriceTextView.setText(R.string.download_manager_pause);
				mContentPriceTextView.setTextColor(mContext.getResources()
						.getColor(R.color.appgame_download_btn_black));
				mDownLoadCLick.setOnClickListener(null);
				break;
			case DOWNLOADING :
				mContentPriceTextView.setVisibility(View.GONE);
				mDownLoadCLick.setVisibility(View.GONE);
				mDownloadIcon.setVisibility(View.GONE);
				break;
			case DOWNLOAD_WAIT :
				mDownloadIcon.setVisibility(View.GONE);
				mDownloadLayout.setVisibility(View.GONE);
				mContentPriceTextView.setVisibility(View.VISIBLE);
				mDownLoadCLick.setVisibility(View.VISIBLE);
				mContentPriceTextView.setText(R.string.themestore_download_connecting);
				mDownloadIcon.setImageDrawable(null);
				mDownLoadCLick
						.setBackgroundResource(R.drawable.gostore_theme_gallery_download_button_no);
				mDownLoadCLick.setOnClickListener(null);
				break;
			default :
				break;
		}
	}

	private void downloadApk(Context context, AppDetailInfoBean infoBean,
			boolean storeInSd) {
		if (mActivityHandler != null) {
			int[] payType = infoBean.mPayType;
			if (payType != null && payType.length > 0 && payType[0] == GoStoreOperatorUtil.PAY_VIP) {
				Message msg = mActivityHandler.obtainMessage();
				msg.what = AppsThemeDetailActivity.CLICK_DOWNLOAD_ZIP;
				msg.obj = infoBean;
				mActivityHandler.sendMessage(msg);
				resetButtonState(DOWNLOAD_WAIT);
			} else {
				Message msg = mActivityHandler.obtainMessage();
				msg.what = AppsThemeDetailActivity.CLICK_DOWNLOAD_APK_NO_PAY;
				msg.obj = infoBean;
				mActivityHandler.sendMessage(msg);
				resetButtonState(DOWNLOAD_WAIT);
			}
		}
	}
	
	private int checkTextViewLineCount(TextView textView) {
		int result = 0;
		if (textView != null) {
			float textWidth = textView.getTextSize();
			int displayWidth = DrawUtils.sWidthPixels;
			int rowCount = (int) (displayWidth / textWidth);
			String text = textView.getText().toString();
			String[] textSplit = text.split("\n");
			int textSplitLength = textSplit.length;
			int splitRowCount = 0;
			for (int i = 0; i < textSplitLength; i++) {
				splitRowCount = (textSplit[i].length() / rowCount) + 1;
				result += splitRowCount;
			}
		}
		return result;
	}

	public void recycle() {
		if (mInstallReceiver != null) {
			mContext.unregisterReceiver(mInstallReceiver);
		}
		unregisterDownloadReceiver();
		mDownLoadCLick = null;
		mDownloadIcon = null;
		mContentPriceTextView = null;
		mContentDescriptionTextView = null;
		mContentUpdateContentTextView = null;
		mMoreUpdateTextView = null;
		mScrollView = null;

		if (mAppDetailIndicators != null) {
			mAppDetailIndicators = null;

		}
		if (mMoreDescriptionTextView != null) {
			mMoreDescriptionTextView.setOnClickListener(null);
			mMoreDescriptionTextView.setBackgroundDrawable(null);
			mMoreDescriptionTextView = null;
		}
		if (mMoreUpdateTextView != null) {
			mMoreUpdateTextView.setOnClickListener(null);
			mMoreUpdateTextView.setBackgroundDrawable(null);
			mMoreUpdateTextView = null;
		}
		if (mContentIconImageView != null) {
			mContentIconImageView.recycle();
			mContentIconImageView = null;
		}
		if (mView != null) {
			mView.removeAllViews();
			mView.setBackgroundDrawable(null);
			mView = null;
		}
		if (mLinearLayout != null) {
			mLinearLayout.removeAllViews();
			mLinearLayout.setBackgroundDrawable(null);
			mLinearLayout = null;
		}
		if (mDetailInfoBean != null) {
			mDetailInfoBean.recycle();
			mDetailInfoBean = null;
		}
		if (mAppTitle != null) {
			mAppTitle.removeAllViews();
			mAppTitle.setBackgroundDrawable(null);
			mAppTitle = null;
		}
		if (mSimpleImageViews != null) {
			for (ImageView simpleImageView : mSimpleImageViews) {
				if (simpleImageView != null) {
					simpleImageView.setBackgroundDrawable(null);
					simpleImageView.setImageDrawable(null);
					simpleImageView.setOnClickListener(null);
					simpleImageView = null;
				}
			}
			mSimpleImageViews.clear();
			mSimpleImageViews = null;
		}

		if (mActivityHandler != null) {
			mActivityHandler = null;
		}
		if (mContext != null) {
			mContext = null;
		}
		// 图片管理器的清理
		mImgManager = null;
	}
	
	/**
	 * <br>功能简述:隐藏局部加载loading
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void hideFootLoading() {
		if (mFootLoadingLayout != null) {
			mFootLoadingLayout.setVisibility(View.GONE);
		}
	}
	
	public void setAppDetailInfoBean(AppDetailInfoBean bean) {
		mDetailInfoBean = bean;
		initViewByBean(mDetailInfoBean);
	} 
	
	public void setHasObtainDetail(Boolean hasObtainDetail) {
		mHasObtainDetail = hasObtainDetail;
	}
}
