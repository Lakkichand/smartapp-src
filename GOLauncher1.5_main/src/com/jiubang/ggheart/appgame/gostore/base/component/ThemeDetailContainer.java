package com.jiubang.ggheart.appgame.gostore.base.component;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.appcenter.contorler.AppDetailController;
import com.jiubang.ggheart.appgame.base.bean.AppDetailInfoBean;
import com.jiubang.ggheart.appgame.base.component.AppDetailImageActivity;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.data.AppJsonOperator;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.net.AppGameNetRecord;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.appgame.base.utils.ApkInstallUtils;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.component.GoStoreDetailScrollViewGroup;
import com.jiubang.ggheart.apps.gowidget.gostore.component.GoStoreDetailScrollViewGroup.ViewGroupListener;
import com.jiubang.ggheart.apps.gowidget.gostore.component.GoStoreIndicators;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreAppInforUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.billing.IPurchaseStateListener;
import com.jiubang.ggheart.billing.PurchaseStateManager;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.theme.zip.ZipResources;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
/**
 * 
 * <br>类描述:应用中心，主题详情container
 * <br>功能详细描述:
 * 
 * @author  lijunye
 * @date  [2012-12-13]
 */
public class ThemeDetailContainer
		implements
			GoStoreDetailRefreshListener,
			IPurchaseStateListener,
			ViewGroupListener {
	/** 
	 * 按钮状态
	 */
	private final static int DOWNLOAD_POSSIBLE = 0; // 免费或收费下载
	private final static int UPDATE_POSSIBLE = 1; // 可更新
	private final static int INSTALLED = 2; // 已安装
	private final static int DOWNLOAD_CANCEL = 3; // 取消下载或下载中
	private final static int DOWNLOAD_IMPOSSIBLE = 5; // 不允许下载
	private final static int INSTALL = 6; // 安装
	private final static int DOWNLOAD_WAIT = 7; // 等待
	private final static int DOWNLOAD_NONE = 8; // 按钮不可用

	/**
	 * 没有数据
	 */
	private static final int MSG_SHOW_NO_TIP = 220;
	/**
	 * 获取数据结束
	 */
	private static final int MSG_GET_DATA_FINISH = 221;

	private TextView mThemeTitle = null; // 标题栏
	private RelativeLayout mContentView = null; // 内容view（gallery，按钮）
	private LinearLayout mDownloadButton = null; // 下载按钮
	private TextView mDownloadButtonText = null; // 下载按钮
	private ImageView mDownloadButtonImg = null; // 下载按钮
	private GoStoreIndicators mGoStoreIndicators = null;
	private GoStoreDetailScrollViewGroup mScrooViewGroup = null;
	private BroadcastReceiver mDownloadReceiver = null;
	private RelativeLayout mDownloadLayout = null;
	private TextView mDownloadText = null; // 下载进度
	private ProgressBar mDownloadBar = null; // 下载进度条
	private RelativeLayout mDownloadButtonLayout;
	private RelativeLayout mNoDataTipRelativeLayout; // 没有结果提示
	private LinearLayout mResumeOrStop = null; //下载暂停或继续按钮
	private LinearLayout mCancelDownload = null; //下载取消按钮
	private ImageView mResumeOrStopImg = null; //下载暂停或继续图标
	private Drawable mPriceBGDrawable = null; // 按钮背景图
	private GoStoreDetailHorizontalScrollView mHorizontalScrollView = null; //图标列表横滑view
	private GoStoreThemeDetailItemView mDetailItemView = null; //主题详情文字介绍view
	private LinearLayout mBottomIconsLayout = null; //图标列表view

	private int mButtonState = -1; // 按钮处于的状态，默认为-1，不处于任何状态
	private int mDownloadId = 0; // 正在下载的APP ID
	private int mPosition = 0; //当前详情位置

	private boolean mIsInstall = false; // 应用下载完是否安装
	private boolean mIsDownloading = false; //是否正在下载
	private boolean mIsZip = false; // 是否ZIP包
	private boolean mIsFirst = true; //是否第一次进入详情
	private boolean mIsNeedIconsView = true; //是否需要显示图标列表

	private String mPriceState = null; // 价格的状态，免费或收费
	private String mDownloadPkg = null; // 正在下载的APP包名

	private HashMap<Integer, AppDetailInfoBean> mDetailHashMap = null; //保存已经加载过的详情bean
	private ArrayList<String> mIconList = null; //主题图标url列表
	private ArrayList<String> mItemPkgList = null; //主题包名列表
	private ArrayList<String> mItemIdList = null; //主题id列表
	
	private Context mContext = null;
	private IDownloadService mDownloadController = null; //下载服务控制器
	private AppDetailController mController = null;
	private AppDetailInfoBean mDetailInfoBean = null;
	private Handler mHandler = null;

	private Handler mActivityHandler = null;
	private AsyncImageManager mImgManager = null;
	private PurchaseStateManager mPurchaseStateManager = null;
	public ThemeDetailContainer(Context context, IDownloadService downloadService) {
		mContext = context;
		mDetailHashMap = new HashMap<Integer, AppDetailInfoBean>();
		mDetailItemView = new GoStoreThemeDetailItemView(mContext);
		mController = new AppDetailController(mContext);
		mDownloadController = downloadService;
		mImgManager = AsyncImageManager.getInstance();
		mPurchaseStateManager = new PurchaseStateManager(mContext);
	}
	public void initValue(AppDetailInfoBean detailInfoBean, TextView themeTitle,
			ArrayList<String> pkgs, ArrayList<String> icons, ArrayList<String> appIds,
			int position, Handler handler) {
		mDownloadId = detailInfoBean.mAppId;
		mDetailInfoBean = detailInfoBean;
		mThemeTitle = themeTitle;
		mIconList = icons;
		mItemPkgList = pkgs;
		mItemIdList = appIds;
		mPosition = position;
		
		mActivityHandler = handler;
		if (mIconList == null || mIconList.size() == 0 || mItemPkgList == null
				|| mItemPkgList.size() == 0 || mItemIdList == null || mItemIdList.size() == 0) {
			mIsNeedIconsView = false;
		}
		initHandler();
		initView();
		initNoDataTipRelativeLayout();
		initDownloadBar();
		initDownloadReceiver();
		initViewByBean(detailInfoBean);
	}
          
	public View getView() {
		return mContentView;
	}
	private void initView() {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		if (mIsNeedIconsView) {
			mContentView = (RelativeLayout) layoutInflater.inflate(
					R.layout.gostore_theme_detail_view_new, null);
		} else {
			mContentView = (RelativeLayout) layoutInflater.inflate(
					R.layout.gostore_theme_detail_view_new_noneicons, null);
		}
		mGoStoreIndicators = (GoStoreIndicators) mContentView.findViewById(R.id.gallery_mark);
		mDownloadButtonLayout = (RelativeLayout) mContentView
				.findViewById(R.id.gostore_theme_detail_button_layout);
		mScrooViewGroup = (GoStoreDetailScrollViewGroup) mContentView
				.findViewById(R.id.gostore_theme_detail_gallery);
		mScrooViewGroup.setScrollFinish(this);
		if (mIsNeedIconsView) {
			mScrooViewGroup.setChildWidth((int) mContext.getResources().getDimension(
					R.dimen.item_width));
			mScrooViewGroup.setChildHeight((int) mContext.getResources().getDimension(
					R.dimen.item_height));
			mScrooViewGroup.setGap(GoStoreDisplayUtil.scalePxToMachine(mContext, 35));
		}
		mBottomIconsLayout = (LinearLayout) mContentView.findViewById(R.id.bottom_imgs);
		mHorizontalScrollView = (GoStoreDetailHorizontalScrollView) mContentView
				.findViewById(R.id.bottom_thumbnails_scrollview);
		if (mIsNeedIconsView) {
			mDownloadButton = (LinearLayout) mContentView
					.findViewById(R.id.gostore_theme_detail_button);
			mDownloadButtonText = (TextView) mContentView
					.findViewById(R.id.gostore_theme_detail_button_text);
			mDownloadButtonImg = (ImageView) mContentView
					.findViewById(R.id.gostore_theme_detail_button_img);
		} else {
			mBottomIconsLayout.setVisibility(View.GONE);
			mDownloadButton = (LinearLayout) mContentView
					.findViewById(R.id.gostore_theme_detail_button_big);
			mDownloadButtonText = (TextView) mContentView
					.findViewById(R.id.gostore_theme_detail_button_big_text);
			mDownloadButtonImg = (ImageView) mContentView
					.findViewById(R.id.gostore_theme_detail_button_big_img);
		}
		mHorizontalScrollView.setChildWidth(GoStoreDisplayUtil.scalePxToMachine(mContext, 72));
		mHorizontalScrollView.setChildHeight(GoStoreDisplayUtil.scalePxToMachine(mContext, 72));
	}
	
	/**
	 * <br>功能简述:初始化下载接收器
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initDownloadReceiver() {
		if (mDownloadReceiver != null) {
			return;
		}
		mDownloadReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent != null) {
					DownloadTask task = intent
							.getParcelableExtra(AppDownloadListener.UPDATE_DOWNLOAD_INFO);
					if (task != null) {
						if (mDetailInfoBean != null) {
							long id = mDetailInfoBean.mAppId;
							if (task.getId() != id) {
								return;
							}
							setDownloadState(task.getState(), task.getAlreadyDownloadPercent());
						}
					}
				}

			}
		};
		if (mContext == null) {
			return;
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ICustomAction.ACTION_APP_DOWNLOAD);
		mContext.registerReceiver(mDownloadReceiver, intentFilter);
	}
	
	public void onResume() {
		if (mScrooViewGroup != null && !mIsFirst) {
			mScrooViewGroup.fixView();
		}
		try {
			if (mDownloadController == null) {
				mDownloadButtonLayout.setVisibility(View.VISIBLE);
				updateButtonState();
				mDownloadLayout.setVisibility(View.GONE);
			} else {
				DownloadTask task = mDownloadController.getDownloadTaskById(mDownloadId);
				if (task != null) {
					int state = task.getState();
					setDownloadState(state, task.getAlreadyDownloadPercent());
				} else {
					mDownloadButtonLayout.setVisibility(View.VISIBLE);
					updateButtonState();
					mDownloadLayout.setVisibility(View.GONE);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	private void initHandler() {
		if (mHandler == null) {
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
						case MSG_SHOW_NO_TIP :
							if (mActivityHandler != null) {
								Message message = mActivityHandler.obtainMessage();
								message.what = MSG_SHOW_NO_TIP;
								mActivityHandler.sendMessage(message);
							}
							break;
						case MSG_GET_DATA_FINISH :
							if (msg.obj != null && msg.obj instanceof AppDetailInfoBean) {
								AppDetailInfoBean infoBean = (AppDetailInfoBean) msg.obj;
								if (mDownloadId == infoBean.mAppId) {
									initViewByBean(infoBean);
									AppRecommendedStatisticsUtil.getInstance().saveDetailsClick(mContext,
											infoBean.mPkgName, infoBean.mAppId, 1);
								}
								if (mDetailHashMap != null) {
									mDetailHashMap.put(infoBean.mAppId, infoBean);
								}
							}
							initDownloadReceiver();
							break;
						default :
							break;
					}
				}
			};
		}
	}

	/**
	 * 初始化没有结果提示文本的方法
	 */
	private void initNoDataTipRelativeLayout() {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		mNoDataTipRelativeLayout = (RelativeLayout) layoutInflater.inflate(
				R.layout.themestore_nodata_tip_full, null);
	}

	private void showNoDataTip() {
		if (mContentView != null) {
			mContentView.removeAllViews();
			if (mNoDataTipRelativeLayout != null) {
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				mContentView.removeView(mNoDataTipRelativeLayout);
				mContentView.addView(mNoDataTipRelativeLayout, params);
			}
		}
	}

	/**
	 * 初始化下载进度条的方法
	 */
	private void initDownloadBar() {
		mDownloadLayout = (RelativeLayout) mContentView
				.findViewById(R.id.gostore_theme_detail_download_progress);
		mDownloadBar = (ProgressBar) mContentView
				.findViewById(R.id.gostore_theme_detail_progressbar);
		mDownloadText = (TextView) mContentView
				.findViewById(R.id.gostore_theme_detail_progresstext);
		mResumeOrStop = (LinearLayout) mContentView
				.findViewById(R.id.app_detail_download_pause_or_resume_click);
		mResumeOrStopImg = (ImageView) mContentView
				.findViewById(R.id.app_detail_download_pause_or_resume);
		mResumeOrStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mActivityHandler == null) {
					return;
				}
				Message msg = mActivityHandler.obtainMessage();
				if (mIsDownloading) {
					mIsDownloading = false;
					msg.what = AppsThemeDetailActivity.STOP_DOWNLOAD;
					mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_pause_new);
				} else {
					mIsDownloading = true;
					msg.what = AppsThemeDetailActivity.RESTART_DOWNLOAD;
					mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_resume_new);
				}
				msg.obj = String.valueOf(mDownloadId);
				mActivityHandler.sendMessage(msg);
			}
		});
		mCancelDownload = (LinearLayout) mContentView
				.findViewById(R.id.app_detail_download_cancel_click);
		mCancelDownload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mActivityHandler == null) {
					return;
				}
				Message msg = mActivityHandler.obtainMessage();
				msg.what = AppsThemeDetailActivity.CANCEL_DOWNLOAD;
				msg.obj = String.valueOf(mDownloadId);
				mActivityHandler.sendMessage(msg);
				mIsDownloading = false;
				updateButtonState();
			}
		});
	}

	/**
	 * <br>功能简述:通过详情bean拼凑数据显示
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param detailItemBean
	 */
	private void initViewByBean(final AppDetailInfoBean detailItemBean) {
		if (detailItemBean == null) {
			return;
		}
		if (mScrooViewGroup == null) {
			return;
		}
		if (mDownloadId != detailItemBean.mAppId) {
			return;
		}
		mDetailInfoBean = detailItemBean;
		if (mScrooViewGroup != null && mGoStoreIndicators != null) {
			if (!mIsFirst) {
				mThemeTitle.setText(detailItemBean.mName);
				int count = mScrooViewGroup.getChildCount();
				for (int i = 0; i < count; i++) {
					View v = mScrooViewGroup.getChildAt(i);
					if (v instanceof GoStoreDetailGalleryImage) {
						((GoStoreDetailGalleryImage) v).recycle();
					}
				}
				mScrooViewGroup.removeAllViews();
				mGoStoreIndicators.removeAllViews();
			}

			initPriceTextViewByBean(detailItemBean);
			try {
				if (mDownloadController != null) {
					DownloadTask task = mDownloadController.getDownloadTaskById(mDownloadId);
					if (task != null) {
						int state = task.getState();
						setDownloadState(state, task.getAlreadyDownloadPercent());
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			/**
			 * 主题详情截图url或id列表
			 */
			ArrayList<String> arrayList = detailItemBean.mLargePicUrls;
			if (mGoStoreIndicators != null && arrayList != null) {
				int count = arrayList.size();
				mGoStoreIndicators.setIndicatorCount(count + 1);
				mScrooViewGroup.setIndicators(mGoStoreIndicators);
				View view = null;
				if (mDetailItemView == null) {
					mDetailItemView = new GoStoreThemeDetailItemView(mContext);
				}
				mDetailItemView.setIsNeedIconsView(mIsNeedIconsView);
				view = mDetailItemView.getView();
				mDetailItemView.getmScrollView(detailItemBean);
				mScrooViewGroup.addView(view);
				LayoutInflater layoutInflater = LayoutInflater.from(mContext);
				for (int i = 0; i < count; i++) {
					String imgUrl = arrayList.get(i);
					GoStoreDetailGalleryImage goStoreDetailGalleryImage = null;
					if (mIsNeedIconsView) {
						goStoreDetailGalleryImage = (GoStoreDetailGalleryImage) layoutInflater
								.inflate(R.layout.gostore_theme_detail_gallery_item_image_new, null);
					} else {
						goStoreDetailGalleryImage = (GoStoreDetailGalleryImage) layoutInflater
								.inflate(
										R.layout.gostore_theme_detail_gallery_item_image_noneicons,
										null);
					}
					if (i == 1) {
						mScrooViewGroup.goToScreenDirectly(1);
					}
					if (i <= 1) {
						goStoreDetailGalleryImage.setImgUrl(imgUrl);
					} else {
						goStoreDetailGalleryImage.initView();
					}
					setImgListener(detailItemBean, i, goStoreDetailGalleryImage);
					mScrooViewGroup.addView(goStoreDetailGalleryImage);
				}
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mScrooViewGroup != null) {
							mScrooViewGroup.goToScreenDirectly(1);
						}
					}
				}, 0);
				if (mIsFirst && mIsNeedIconsView) {
					mHorizontalScrollView.setListBean(mIconList, mPosition, this);
					mHorizontalScrollView.setHightLight(mPosition);
				}
			}
			if (mDetailItemView != null) {
				mDetailItemView.setDescriptionListener();
				mDetailItemView.setUpdateListener();
			}
			mIsFirst = false;
		}
	}

	/**
	 * <br>功能简述:设置每个详情图片的点击效果
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param infoBean
	 * @param position
	 * @param galleryImage
	 * @param isTheme
	 */
	private void setImgListener(final AppDetailInfoBean infoBean, final int position,
			GoStoreDetailGalleryImage galleryImage) {
		OnClickListener imgClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int curIndex = position + 1;
				if (mScrooViewGroup != null) {
					int scrollCurentPage = mScrooViewGroup.getCurrentPage();
					if (curIndex != scrollCurentPage) {
						mScrooViewGroup.goToScreen(curIndex);
						return;
					}
				}
				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setClass(mContext, AppDetailImageActivity.class);
				intent.putExtra(GoStorePublicDefine.SCAN_IMAGE_IDS_KEY, infoBean.mLargePicUrls);
				intent.putExtra(GoStorePublicDefine.SCAN_IMAGE_CUR_INDEX_KEY, curIndex - 1);
				mContext.startActivity(intent);
			}
		};
		galleryImage.setImgListener(imgClickListener);
	}

	/**
	 * 通过BEAN信息，刷新价格按钮状态的方法
	 * 
	 * @param detailItemBean
	 */
	private void initPriceTextViewByBean(final AppDetailInfoBean detailItemBean) {
		if (detailItemBean != null && mDownloadButton != null) {
			/**
			 *  价格背景
			 */
			Drawable priceBgDrawable = null;
			String price = null;
			String packageName = detailItemBean.mPkgName;
			int verCode = Integer.parseInt(detailItemBean.mVersionCode);
			if (GoStoreAppInforUtil.isApplicationExsit(mContext, packageName)) {
				/**
				 *  如果应用已经安装
				 */
				if (GoStoreAppInforUtil.isNewToAlreadyInstall(mContext, packageName, verCode)) {
					/**
					 *  如果应用可更新
					 */
					mButtonState = UPDATE_POSSIBLE;
					price = mContext.getResources().getString(R.string.themestore_can_update);
					mDownloadButtonText.setText(price);
					mDownloadButtonImg.setImageResource(R.drawable.app_detail_update_icon);
					priceBgDrawable = mContext.getResources().getDrawable(
							R.drawable.appgame_update_btn_selector);
					mDownloadButton.setBackgroundDrawable(priceBgDrawable);
					mPriceState = price;
					mPriceBGDrawable = priceBgDrawable;
					mDownloadButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							/**
							 * 如果是一般的付费类型
							 * 此处可要求后台服务器修改协议，以同于list的格式下发链接请求。
							 */
							if (mActivityHandler != null) {
								Message msg = mActivityHandler.obtainMessage();
								msg.what = AppsThemeDetailActivity.UPDATE_CLICK_COUNT;
								msg.obj = detailItemBean;
								mActivityHandler.sendMessage(msg);
							}
							downloadApk(mContext, detailItemBean);
						}
					});
				} else {
					/**
					 *  应用程序已安装,但不可更新
					 */
					mButtonState = INSTALLED;
					int appType = detailItemBean.mTag;
					if (appType == AppsThemeDetailActivity.THEME_TYPE) {
						price = mContext.getResources().getString(R.string.gostore_can_apply);
					} else {
						price = mContext.getResources().getString(R.string.app_detail_installed_open);
					}
					mDownloadButtonText.setText(price);
					mDownloadButtonImg.setImageResource(R.drawable.app_detail_open_icon);
					priceBgDrawable = mContext.getResources().getDrawable(
							R.drawable.appgame_download_btn_selector);
					mPriceState = price;
					mPriceBGDrawable = priceBgDrawable;
					mDownloadButton.setBackgroundDrawable(priceBgDrawable);
					mDownloadButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							applyTheme(detailItemBean);
						}
					});
				}

			} else if (isApkExist(detailItemBean)) {
				/**
				 *  APK包已经下载，点击直接安装
				 */
				mButtonState = INSTALL;
				price = mContext.getResources().getString(R.string.gostore_detail_install);
				mDownloadButtonText.setText(price);
				mDownloadButtonImg.setImageResource(R.drawable.app_detail_install_icon);
				priceBgDrawable = mContext.getResources().getDrawable(
						R.drawable.appgame_download_btn_selector);
				mPriceState = price;
				mPriceBGDrawable = priceBgDrawable;
				mDownloadButton.setBackgroundDrawable(priceBgDrawable);
				/**
				 *  添加按钮点击响应
				 */
				mDownloadButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String version = detailItemBean.mVersion;
						if (version != null && version.trim().startsWith("V")) {
							version = version.trim().substring(1);
						}
						String apkName = GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH
								+ detailItemBean.mPkgName
								+ "_"
								+ version + ".apk";
						if (FileUtil.isFileExist(apkName)) {
							try {
								ApkInstallUtils.installApk(apkName);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
			} else if (PurchaseStateManager.query(mContext, packageName) != null) {
				/**
				 *  如果已经付费
				 */
				mIsZip = true;
				if (ZipResources.isZipThemeExist(packageName)) {
					/**
					 *  本地存在Zip包
					 */
					mButtonState = INSTALL;
					mDownloadButtonImg.setImageResource(R.drawable.app_detail_open_icon);
					price = mContext.getResources().getString(R.string.gostore_can_apply);
					priceBgDrawable = mContext.getResources().getDrawable(
							R.drawable.appgame_download_btn_selector);
					mDownloadButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							applyTheme(detailItemBean);
						}
					});
				} else {
					mButtonState = DOWNLOAD_POSSIBLE;
					mDownloadButtonImg.setImageResource(R.drawable.app_detail_download_icon);
					price = mContext.getResources().getString(R.string.gostore_download);
					priceBgDrawable = mContext.getResources().getDrawable(
							R.drawable.appgame_download_btn_selector);
					/**
					 *  添加按钮点击响应
					 */
					mDownloadButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (mActivityHandler != null) {
								Message msg = mActivityHandler.obtainMessage();
								msg.what = AppsThemeDetailActivity.CLICK_DOWNLOAD_ZIP;
								msg.obj = detailItemBean;
								mActivityHandler.sendMessage(msg);
								resetButtonState(DOWNLOAD_WAIT);
							}
						}
					});
				}

				mPriceState = price;
				mPriceBGDrawable = priceBgDrawable;
				mDownloadButtonText.setText(price);
				mDownloadButton.setBackgroundDrawable(mPriceBGDrawable);
			} else {
				/**
				 *  应用程序未安装
				 */
				mButtonState = DOWNLOAD_POSSIBLE;
				price = detailItemBean.mPrice;
				if (detailItemBean.mIsFree) {
					price = mContext.getString(R.string.gostore_download);
				}
				priceBgDrawable = mContext.getResources().getDrawable(
						R.drawable.appgame_download_btn_selector);
				mPriceState = price;
				mPriceBGDrawable = priceBgDrawable;
				mDownloadButtonText.setText(price);
				mDownloadButtonImg.setImageResource(R.drawable.app_detail_download_icon);
				mDownloadButton.setBackgroundDrawable(priceBgDrawable);
				mDownloadButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						/**
						 *  点击响应处理。
						 */
						mDownloadPkg = detailItemBean.mPkgName;
						mDownloadId = detailItemBean.mAppId;
						if (mActivityHandler != null) {
							Message msg = mActivityHandler.obtainMessage();
							msg.what = AppsThemeDetailActivity.DOWNLOAD_CLICK_COUNT;
							msg.obj = detailItemBean;
							mActivityHandler.sendMessage(msg);
						}
						downloadApk(mContext, detailItemBean);
					}
				});
			}
		}
		if (!mIsFirst) {
			updateButtonState();
		} else {
			onResume();
		}
	}

	/**
	 * <br>功能简述:更新按钮状态
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void updateButtonState() {
		mIsInstall = GoStoreAppInforUtil.isApplicationExsit(mContext, mDetailInfoBean.mPkgName);
		if (mIsInstall) {
			/**
			 *  如果已经安装
			 */
			if (!GoStoreAppInforUtil.isNewToAlreadyInstall(mContext, mDetailInfoBean.mPkgName,
					Integer.parseInt(mDetailInfoBean.mVersionCode))) {
				/**
				 *  不可更新，显示已安装
				 */
				resetButtonState(INSTALLED);
			} else if (isApkExist(mDetailInfoBean)) {
				/**
				 *  可以更新，并且本地有APK包，显示安装
				 */
				resetButtonState(INSTALL);
			} else {
				/**
				 *  可以更新，且本地无APK包，显示更新
				 */
				resetButtonState(UPDATE_POSSIBLE);
			}
		} else if (mIsZip) {
			if (ZipResources.isZipThemeExist(mDetailInfoBean.mPkgName) 
					&& PurchaseStateManager.query(mContext, mDetailInfoBean.mPkgName) != null) {
				resetButtonState(INSTALL);
			} else {
				mPriceState = mContext.getResources().getString(R.string.gostore_download);
				resetButtonState(DOWNLOAD_POSSIBLE);
			}
		} else {
			if (mButtonState == DOWNLOAD_POSSIBLE) {
				if (isApkExist(mDetailInfoBean)) {
					resetButtonState(INSTALL);
				} else {
					resetButtonState(DOWNLOAD_POSSIBLE);
				}
			} else if (mButtonState == UPDATE_POSSIBLE) {
				if (isApkExist(mDetailInfoBean)) {
					resetButtonState(INSTALL);
				} else {
					resetButtonState(UPDATE_POSSIBLE);
				}
			} else if (mButtonState == INSTALL) {
				mIsInstall = GoStoreAppInforUtil.isApplicationExsit(mContext, mDownloadPkg);
				resetButtonState(INSTALL);
			}
		}
	}

	/**
	 * <br>功能简述:按钮状态设置
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param state 按钮状态
	 */
	protected void resetButtonState(int state) {
		if (mDownloadButton == null) {
			return;
		}
		switch (state) {
			case DOWNLOAD_POSSIBLE :
				mDownloadButtonLayout.setVisibility(View.VISIBLE);
				mDownloadLayout.setVisibility(View.GONE);
				mDownloadButtonText.setText(mPriceState);
				mDownloadButtonImg.setImageResource(R.drawable.app_detail_download_icon);
				mDownloadButton.setBackgroundDrawable(mPriceBGDrawable);
				mDownloadButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mActivityHandler != null) {
							Message msg = mActivityHandler.obtainMessage();
							msg.what = AppsThemeDetailActivity.DOWNLOAD_CLICK_COUNT;
							msg.obj = mDetailInfoBean;
							mActivityHandler.sendMessage(msg);
						}
						downloadApk(mContext, mDetailInfoBean);
					}
				});
				break;

			case UPDATE_POSSIBLE :
				mDownloadButtonLayout.setVisibility(View.VISIBLE);
				mDownloadLayout.setVisibility(View.GONE);
				mDownloadButtonText.setText(mPriceState);
				mDownloadButtonImg.setImageResource(R.drawable.app_detail_update_icon);
				mDownloadButton.setBackgroundDrawable(mPriceBGDrawable);
				mDownloadButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mDownloadPkg = mDetailInfoBean.mPkgName;
						mDownloadId = mDetailInfoBean.mAppId;
						if (mActivityHandler != null) {
							Message msg = mActivityHandler.obtainMessage();
							msg.what = AppsThemeDetailActivity.UPDATE_CLICK_COUNT;
							msg.obj = mDetailInfoBean;
							mActivityHandler.sendMessage(msg);
						}
						downloadApk(mContext, mDetailInfoBean);
					}
				});
				break;

			case INSTALLED :
				mDownloadButtonLayout.setVisibility(View.VISIBLE);
				mDownloadLayout.setVisibility(View.GONE);
				int appType = mDetailInfoBean.mTag;
				if (appType == AppsThemeDetailActivity.THEME_TYPE) {
					mPriceState = mContext.getResources().getString(R.string.gostore_can_apply);
				} else {
					mPriceState = mContext.getResources().getString(R.string.app_detail_installed_open);
				}
				mDownloadButtonText.setText(mPriceState);
				mDownloadButtonImg.setImageResource(R.drawable.app_detail_open_icon);
				mDownloadButton.setBackgroundDrawable(mPriceBGDrawable);
				mDownloadButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						applyTheme(mDetailInfoBean);
					}
				});
				break;

			case DOWNLOAD_CANCEL :
				mDownloadButtonLayout.setVisibility(View.VISIBLE);
				mDownloadLayout.setVisibility(View.GONE);
				mDownloadButtonText.setText(R.string.gostore_detail_cancel_download);
				mDownloadButtonImg.setImageDrawable(null);
				mDownloadButton
						.setBackgroundResource(R.drawable.gostore_theme_gallery_download_button_no);
				mDownloadButton.setOnClickListener(null);
				break;
				
			case DOWNLOAD_IMPOSSIBLE :
				mDownloadButtonLayout.setVisibility(View.VISIBLE);
				mDownloadLayout.setVisibility(View.GONE);
				mDownloadButtonText.setText(mPriceState);
				mDownloadButtonImg.setImageDrawable(null);
				mDownloadButton
						.setBackgroundResource(R.drawable.gostore_theme_gallery_download_button_no);
				mDownloadButton.setOnClickListener(null);
				break;
			case INSTALL :
				if (mIsZip) {
					mDownloadButtonText.setText(R.string.gostore_can_apply);
					mDownloadButtonImg.setImageResource(R.drawable.app_detail_open_icon);
					mDownloadButton.setBackgroundDrawable(mPriceBGDrawable);
					mDownloadButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							applyTheme(mDetailInfoBean);
						}
					});
				} else {
					mDownloadButtonText.setText(R.string.gostore_detail_install);
					mDownloadButtonImg.setImageResource(R.drawable.app_detail_install_icon);
					mDownloadButton.setBackgroundDrawable(mPriceBGDrawable);
					mDownloadButton.setOnClickListener(new OnClickListener() {

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
								try {
									ApkInstallUtils.installApk(apkName);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					});
				}
				break;
			case DOWNLOAD_WAIT :
				mDownloadButtonLayout.setVisibility(View.VISIBLE);
				mDownloadLayout.setVisibility(View.GONE);
				mDownloadButtonText.setText(R.string.themestore_download_connecting);
				mDownloadButtonImg.setImageDrawable(null);
				mDownloadButton
						.setBackgroundResource(R.drawable.gostore_theme_gallery_download_button_no);
				mDownloadButton.setOnClickListener(null);
				break;
			case DOWNLOAD_NONE :
				mDownloadButtonLayout.setVisibility(View.VISIBLE);
				mDownloadLayout.setVisibility(View.GONE);
				mDownloadButtonText.setText(R.string.loading);
				mDownloadButtonImg.setImageDrawable(null);
				mDownloadButton
						.setBackgroundResource(R.drawable.gostore_theme_gallery_download_button_no);
				mDownloadButton.setOnClickListener(null);
				break;
			default :
				break;
		}
	}

	/**
	 * <br>功能简述:应用主题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param pkgName
	 */
	private void applyTheme(final AppDetailInfoBean infoBean) {
		if (infoBean == null) {
			throw new IllegalArgumentException("context can not be null");
		}
		String pkgName = infoBean.mPkgName;
		int appType = infoBean.mTag;
		if (pkgName.startsWith("com.jiubang.goscreenlock.theme")) {
			if (mActivityHandler != null) {
				Message msg = mActivityHandler.obtainMessage();
				msg.what = AppsThemeDetailActivity.APPLY_LOCKER_THEME;
				msg.obj = infoBean;
				mActivityHandler.sendMessage(msg);
			}
		} else if (appType == AppsThemeDetailActivity.THEME_TYPE
				|| appType == AppsThemeDetailActivity.SUPER_THEME_TYPE) {
			//主题
			if (mActivityHandler != null) {
				Message msg = mActivityHandler.obtainMessage();
				msg.what = AppsThemeDetailActivity.APPLY_THEME;
				msg.obj = infoBean;
				mActivityHandler.sendMessage(msg);
			}
		} else if (appType <= 2
				|| appType == AppsThemeDetailActivity.LIVE_WALLPAPER) {
			//应用
			if (mActivityHandler != null) {
				Message msg = mActivityHandler.obtainMessage();
				msg.what = AppsThemeDetailActivity.OPEN_APK;
				msg.obj = infoBean;
				mActivityHandler.sendMessage(msg);
			}
		}
	}

	

	

	private void downloadApk(Context context, AppDetailInfoBean infoBean) {
		if (context == null || infoBean == null) {
			throw new IllegalArgumentException("args can not be null");
		}
		int[] payType = infoBean.mPayType;
		if (GoStoreOperatorUtil.isNoPay(payType)) {
			if (mActivityHandler != null) {
				Message msg = mActivityHandler.obtainMessage();
				msg.what = AppsThemeDetailActivity.CLICK_DOWNLOAD_APK_NO_PAY;
				msg.obj = infoBean;
				mActivityHandler.sendMessage(msg);
				resetButtonState(DOWNLOAD_WAIT);
			}
		} else if (payType != null
				&& payType.length > 0
				&& payType[0] == GoStoreOperatorUtil.PAY_VIP) {
			if (mActivityHandler != null) {
				mIsZip = true;
				Message msg = mActivityHandler.obtainMessage();
				msg.what = AppsThemeDetailActivity.CLICK_DOWNLOAD_ZIP;
				msg.obj = infoBean;
				mActivityHandler.sendMessage(msg);
				resetButtonState(DOWNLOAD_WAIT);
			}
		} else {
			IPurchaseStateListener listener = (IPurchaseStateListener) this;
			GoStoreOperatorUtil.gotoPay(context, infoBean, listener);
		}

	}

	/**
	 * <br>功能简述: 下载状态设置
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param state 下载状态
	 * @param percent 已经下载的百分比
	 */
	private void setDownloadState(int state, int percent) {
		switch (state) {
			case DownloadTask.STATE_WAIT :
				mIsDownloading = false;
				if (percent <= 0) {
					mDownloadButtonText.setText(R.string.themestore_download_waiting);
					mDownloadButton
							.setBackgroundResource(R.drawable.gostore_theme_gallery_download_button_no);
					mDownloadButton.setOnClickListener(null);
					mDownloadButtonLayout.setVisibility(View.VISIBLE);
					mDownloadLayout.setVisibility(View.GONE);

				} else {
					mDownloadButtonLayout.setVisibility(View.GONE);
					mDownloadLayout.setVisibility(View.VISIBLE);
					mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_pause_new);
					updateDownloadProgress(percent);
				}
				break;
			case DownloadTask.STATE_DOWNLOADING :
				mIsDownloading = true;
				mDownloadButtonLayout.setVisibility(View.GONE);
				mDownloadLayout.setVisibility(View.VISIBLE);
				mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_pause_new);
				updateDownloadProgress(percent);
				break;
			case DownloadTask.STATE_FINISH :
				mIsDownloading = false;
				int[] payTypes = mDetailInfoBean.mPayType;
				String path = LauncherEnv.Path.GOT_ZIP_HEMES_PATH;
				if (payTypes != null && payTypes.length > 0
						&& payTypes[0] == GoStoreOperatorUtil.PAY_VIP) {
					if (mDetailInfoBean.mTag == AppsThemeDetailActivity.LOCKER_TYPE) {
						path = LauncherEnv.Path.GOLOCKER_ZIP_HEMES_PATH;
					}
					mPurchaseStateManager.save(mDetailInfoBean.mPkgName, path);
				}
				mDownloadButtonLayout.setVisibility(View.VISIBLE);
				updateButtonState();
				mDownloadLayout.setVisibility(View.GONE);
				break;
			case DownloadTask.STATE_STOP :
				mIsDownloading = false;
				mDownloadButtonLayout.setVisibility(View.GONE);
				mDownloadLayout.setVisibility(View.VISIBLE);
				mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_resume_new);
				updateDownloadProgress(percent);
				break;
			case DownloadTask.STATE_START :
				mIsDownloading = true;
				mDownloadButtonLayout.setVisibility(View.GONE);
				mDownloadLayout.setVisibility(View.VISIBLE);
				mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_pause_new);
				updateDownloadProgress(percent);
				break;
			case DownloadTask.STATE_RESTART :
				mIsDownloading = true;
				mDownloadButtonLayout.setVisibility(View.GONE);
				mDownloadLayout.setVisibility(View.VISIBLE);
				mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_pause_new);
				updateDownloadProgress(percent);
				break;
			case DownloadTask.STATE_FAIL :
				mIsDownloading = false;
				mDownloadButtonLayout.setVisibility(View.VISIBLE);
				updateButtonState();
				mDownloadLayout.setVisibility(View.GONE);
				break;
			default :
				break;
		}
	}

	private void updateDownloadProgress(int progress) {
		if (mDownloadBar != null) {
			mDownloadBar.setProgress(progress);
		}
		if (mDownloadText != null) {
			mDownloadText.setText(progress + "%");
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
	
	/**
	 * 获取商品详情数据
	 */
	private void getDetailData() {
		if (!Machine.isNetworkOK(mContext)) {
			showNoDataTip();
		}
		final int startType = AppsDetail.START_TYPE_APPRECOMMENDED;
		String url = mController.getRequestUrlByType(mContext);
		JSONObject postdata = mController.getPostJSON(mContext, startType, mDownloadId,
				mDownloadPkg, 0);
		final int fDownloadId = mDownloadId;
		THttpRequest request = null;
		try {
			request = new THttpRequest(url, postdata.toString().getBytes(), new IConnectListener() {
				@Override
				public void onStart(THttpRequest arg0) {
				}

				@Override
				public void onFinish(THttpRequest request, IResponse response) {
					if (response != null && response.getResponse() != null
							&& (response.getResponse() instanceof JSONObject)) {
						JSONObject json = (JSONObject) response.getResponse();
						if (fDownloadId != mDownloadId) {
							return;
						}
						AppDetailInfoBean detailInfoBean = mController.getDetailData(json,
								mContext, mDownloadId, mDownloadPkg);
						if (detailInfoBean == null) {
							mHandler.sendEmptyMessage(MSG_SHOW_NO_TIP);
						} else {
							Message msg = mHandler.obtainMessage();
							msg.obj = detailInfoBean;
							msg.what = MSG_GET_DATA_FINISH;
							mHandler.sendMessage(msg);
						}
					} else {
						mHandler.sendEmptyMessage(MSG_SHOW_NO_TIP);
					}
				}

				@Override
				public void onException(THttpRequest arg0, int arg1) {
					mHandler.sendEmptyMessage(MSG_SHOW_NO_TIP);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			mHandler.sendEmptyMessage(MSG_SHOW_NO_TIP);
		}
		if (request != null) {
			/**
			 * 设置备选url
			 */
			try {
				request.addAlternateUrl(mController.getAlternativeUrl(mContext));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			/**
			 *  设置线程优先级，
			 *  读取数据的线程优先级应该最高（比拿取图片的线程优先级高）
			 */
			request.setRequestPriority(Thread.MAX_PRIORITY);
			request.setOperator(new AppJsonOperator());
			request.setNetRecord(new AppGameNetRecord(mContext, false));
			AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(mContext);
			httpAdapter.addTask(request);
		}
	}

	private void refreshValue(int position) {
		if (mItemIdList == null || mItemPkgList == null) {
			return;
		}
		if (position >= mItemIdList.size() || position >= mItemPkgList.size()) {
			return;
		}
		mDownloadId = Integer.parseInt(mItemIdList.get(position));
		mDownloadPkg = mItemPkgList.get(position);
		try {
			if (mDownloadController == null) {
				mDownloadButtonLayout.setVisibility(View.VISIBLE);
				updateButtonState();
				mDownloadLayout.setVisibility(View.GONE);
			} else {
				DownloadTask task = mDownloadController.getDownloadTaskById(mDownloadId);
				if (task != null) {
					int state = task.getState();
					setDownloadState(state, task.getAlreadyDownloadPercent());
				} else {
					mDownloadButtonLayout.setVisibility(View.VISIBLE);
					updateButtonState();
					mDownloadLayout.setVisibility(View.GONE);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void refresh(int position) {
		mPosition = position;
//		mHorizontalScrollView.setHightLight(position);
		if (mScrooViewGroup != null) {
			int count = mScrooViewGroup.getChildCount();
			for (int i = 0; i < count; i++) {
				View v = mScrooViewGroup.getChildAt(i);
				if (v instanceof GoStoreDetailGalleryImage) {
					((GoStoreDetailGalleryImage) v).recycle();
				}
			}
			mScrooViewGroup.removeAllViews();
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			GoStoreDetailGalleryImage goStoreDetailGalleryImage = (GoStoreDetailGalleryImage) layoutInflater
					.inflate(R.layout.gostore_theme_detail_gallery_item_image_new, null);
			goStoreDetailGalleryImage.getProgressView();
			mScrooViewGroup.addView(goStoreDetailGalleryImage);
			mScrooViewGroup.goToScreenDirectly(0);
		}
		refreshValue(position);
		resetButtonState(DOWNLOAD_NONE);
		if (mDetailHashMap != null) {
			AppDetailInfoBean infoBean = mDetailHashMap.get(mDownloadId);
			if (infoBean != null) {
				initViewByBean(infoBean);
				AppRecommendedStatisticsUtil.getInstance().saveDetailsClick(mContext,
						infoBean.mPkgName, infoBean.mAppId, 1);
			} else {
				getDetailData();
			}
		} else {
			getDetailData();
		}
	}
	@Override
	public void purchaseState(int purchaseState, String packageName) {
		if (purchaseState == IPurchaseStateListener.PURCHASE_STATE_PURCHASED) {
			if (mActivityHandler != null) {
				mIsZip = true;
				Message msg = mActivityHandler.obtainMessage();
				msg.what = AppsThemeDetailActivity.CLICK_DOWNLOAD_ZIP;
				msg.obj = mDetailInfoBean;
				mActivityHandler.sendMessage(msg);
				resetButtonState(DOWNLOAD_WAIT);
			}
		}

	}
	
	
	@Override
	public void scrollFinish(final int page) {
		if (page > 0 && mScrooViewGroup != null) {
			int childCount = mScrooViewGroup.getChildCount();
			if (childCount <= 3) {
				return;
			}
			if (!AppsThemeDetailActivity.sLoadImg) {
				return;
			}
			for (int i = 1; i < childCount; i++) {
				final View v = mScrooViewGroup.getChildAt(i);
				if (v != null && v instanceof GoStoreDetailGalleryImage) {
					final GoStoreDetailGalleryImage galleryImage = (GoStoreDetailGalleryImage) v;
					Bitmap bmp = galleryImage.getBitmap();
					if (i < page - 1 || i > page + 1) {
						if (bmp != null) {
							galleryImage.setImageViewDefault();
							String imgUrl = mDetailInfoBean.mLargePicUrls.get(i - 1);
							mImgManager.recycle(imgUrl);
						}
					} else {
						if (!galleryImage.isBitmapRecycled() && bmp != null) {
							continue;
						}
						if (bmp == null) {
							String imgUrl = mDetailInfoBean.mLargePicUrls.get(i - 1);
							String imgName = String.valueOf(imgUrl.hashCode());
							galleryImage.setUrl(imgUrl);
							bmp = mImgManager.loadImage(LauncherEnv.Path.GOSTORE_ICON_PATH,
									imgName, imgUrl, true, false, null,
									new AsyncImageLoadedCallBack() {
										@Override
										public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
											if (galleryImage.getBitmap() == null) {
												if (imageBitmap != null) {
													galleryImage.setImageViewBitmap(imageBitmap);
													galleryImage.setBitmap(imageBitmap);
												}
											}
										}
									});
							if (bmp != null) {
								galleryImage.setImageViewBitmap(bmp);
								galleryImage.setBitmap(bmp);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * <br>功能简述:资源回收
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void recycle() {
		if (mDownloadReceiver != null) {
			/**
			 *  应不应该反注册
			 */
			mContext.unregisterReceiver(mDownloadReceiver);
			mDownloadReceiver = null;
		}
		mNoDataTipRelativeLayout = null;
		if (mScrooViewGroup != null) {
			int count = mScrooViewGroup.getChildCount();
			for (int i = 0; i < count; i++) {
				View v = mScrooViewGroup.getChildAt(i);
				if (v != null && v instanceof GoStoreDetailGalleryImage) {
					((GoStoreDetailGalleryImage) v).recycle();
				}
			}
			mScrooViewGroup.removeAllViews();
		}
		
		if (mHorizontalScrollView != null) {
			mHorizontalScrollView.recycle();
			mHorizontalScrollView = null;
		}
		if (mDetailItemView != null) {
			mDetailItemView.recycle();
			mDetailItemView = null;
		}
		if (mDetailHashMap != null) {
			Iterator iter = mDetailHashMap.keySet().iterator();
			while (iter.hasNext()) {
				Object key = iter.next();
				Object val = mDetailHashMap.get(key);
				if (val instanceof AppDetailInfoBean) {
					((AppDetailInfoBean) val).recycle();
				}
			}
			mDetailHashMap.clear();
			mDetailHashMap = null;
		}
		if (mScrooViewGroup != null) {
			mScrooViewGroup.removeAllViews();
			mScrooViewGroup = null;
		}
		if (mDownloadButton != null) {
			mDownloadButton.setOnClickListener(null);
			mDownloadButton.setBackgroundDrawable(null);
			mDownloadButton = null;
		}
		if (mDownloadButtonText != null) {
			mDownloadButtonText.setText(null);
			mDownloadButton = null;
		}
		if (mDownloadButtonImg != null) {
			mDownloadButtonImg.setImageDrawable(null);
			mDownloadButton = null;
		}
		if (mDetailInfoBean != null) {
			mDetailInfoBean.recycle();
			mDetailInfoBean = null;
		}
		if (mThemeTitle != null) {
			mThemeTitle.setText(null);
			mThemeTitle = null;
		}
		if (mContentView != null) {
			mContentView.removeAllViews();
			mContentView.setBackgroundDrawable(null);
			mContentView = null;
		}
		if (mResumeOrStop != null) {
			mResumeOrStop.setBackgroundDrawable(null);
			mResumeOrStop.setOnClickListener(null);
			mResumeOrStop = null;
		}
		if (mCancelDownload != null) {
			mCancelDownload.setBackgroundDrawable(null);
			mCancelDownload.setOnClickListener(null);
			mCancelDownload = null;
		}
		if (mResumeOrStopImg != null) {
			mResumeOrStopImg = null;
		}
		mIconList = null;
		mItemIdList = null;
		mItemPkgList = null;
		
		if (mActivityHandler != null) {
			mActivityHandler = null;
		}
		
		if (mImgManager != null) {
			mImgManager.removeAllTask();
			mImgManager = null;
		}
		
		if (mContext != null) {
			mContext = null;
		}
	}
}
