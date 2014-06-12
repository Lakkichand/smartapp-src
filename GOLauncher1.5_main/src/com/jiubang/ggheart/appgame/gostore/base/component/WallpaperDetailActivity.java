package com.jiubang.ggheart.appgame.gostore.base.component;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.setting.AppGameSettingData;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.base.utils.AppGameConfigUtils;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.component.OnImageChangeListener;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.views.ScrollerViewGroup;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  lijunye
 * @date  [2012-9-12]
 */
public class WallpaperDetailActivity extends Activity
		implements
			GoStoreDetailRefreshListener,
			ScreenScrollerListener {

	public static final String APP_ID = "app_id";
	public static final String TITLE = "title";
	public static final String APP_ID_LIST = "app_id_list";
	public static final String APP_IMG_URL_LIST = "app_img_url_list";
	public static final String APP_URL_LIST = "app_url_list";
	public static final String APP_ICON_URL_LIST = "app_icon_url_list";
	public static final String POSITION = "position";
	
	/**
	 *  可下载
	 */
	private final static int DOWNLOAD_POSSIBLE = 0;
	/**
	 * 可取消下载或下载中
	 */
	private final static int DOWNLOAD_CANCEL = 1;
	/**
	 * 下载完成
	 */
	private final static int DOWNLOAD_COMPLETE = 2;
	/**
	 * 暂停下载
	 */
	private final static int DOWNLOAD_STOP = 3;
	/**
	 * 继续下载
	 */
	private final static int DOWNLOAD_CONTINUE = 4;
	/**
	 * 等待
	 */
	private final static int DOWNLOAD_WAIT = 7;
	/**
	 * 进度百分比key
	 */
	public final static String PERSENT_KEY = "persent_key";
	/**
	 * 正在下载的图片
	 */
	public final static String DOWNLOADING_ID = "downloading_app_id";
	/**
	 * 正在下载的图片文件名
	 */
	public final static String DOWNLOADING_FILE_NAME = "downloading_app_filename";
	/**
	 * 图片格式
	 */
	private final static String PICTURE_FORMAT = ".png";
	/**
	 *  壁纸保存目录
	 */
	private final static String DOWNLOAD_PIC_DIRECTORY_PATH = Environment.getExternalStorageDirectory() + "/GoStore/download/Picture/";
	/**
	 * 标准图
	 */
	private static final int DEF_PIC_TYPE = 0;
	/**
	 * 大图
	 */
	private static final int BIG_PIC_TYPE = 1;
	/**
	 * 标题名字
	 */
	private String mTitleName = "";
	/**
	 * 下载完成后的图片名字
	 */
	private String mDownloadFinishName = "";
	/**
	 * 图片URL
	 */
	private String mDownloadURL = "";
	
	private int mPosition = 0;
	private int mPercent = 0;
	private long mDownloadId = 0; // 正在下载的图片 ID
	private String mAppId = null;
	
	/**
	 *  所显示的图片是否下载中
	 */
	private boolean mIsDownloading = false;
	/**
	 * 是否第一次进入详情
	 */
	private boolean mIsFirst = false;
	private boolean mHasBindService = false;
	private boolean mPreView = false;
	
	public static boolean sLoadImg = true;
	
	private ArrayList<String> mIds = null; // 保存图片唯一ID的LIST
	private ArrayList<String> mImageIdList = null; // 保存图片显示ID的LIST
	private ArrayList<String> mDownloadUrlList = null; // 保存图片下载URL的LIST
	private ArrayList<String> mIcons = null; //保存图标URL的LIST
	private ArrayList<ImageView> mImageViewList = null;
	
	private Context mContext = null;
	private TextView mThemeTitle;
	private LinearLayout mDownloadTextView = null; // 下载壁纸按钮
	private TextView mDownloadTextViewText = null; // 下载壁纸按钮文字
	private ImageView mDownloadTextViewImg = null; // 下载壁纸按钮文字
	private Button mSetButton = null;
	private RelativeLayout mDownloadProgressLayout = null; // 下载进度条
	private RelativeLayout mSetLayout = null;
	private RelativeLayout mDownloadLayout = null;
	private RelativeLayout mTitle = null;
	private LinearLayout mResumeOrStop = null; //暂停继续按钮
	private LinearLayout mCancelDownload = null; //取消下载按钮
	private ScrollerViewGroup mScrollerViewGroup = null; // 可手势滑动
	private TextView mDownloadText = null; // 下载进度
	private ProgressBar mDownloadBar = null; // 下载进度条
	private WallPaperDetailHorizontalScrollView mIconViews = null;
	private ImageView mResumeOrStopImg = null; //暂停继续按钮图片
	private RelativeLayout mAppTitle;
	
	private IDownloadService mDownloadController = null;
	private BroadcastReceiver mDownloadReceiver = null; // 进度接收器
	private AsyncImageManager mImgManager;
	private Drawable mDefaultIcon;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gostore_wallpaper_detail_view);
		
	    //根据SIM卡国家，设置语言信息
		AppGameConfigUtils.updateResourcesLocaleBySim(this, getResources());
		if (GoStorePhoneStateUtil.isWifiEnable(GOLauncherApp
				.getContext())
				|| AppGameSettingData.getInstance(mContext)
						.getTrafficSavingMode() == AppGameSettingData.LOADING_ALL_IMAGES) {
			sLoadImg = true;
		} else {
			sLoadImg = false;
		}
		mContext = WallpaperDetailActivity.this;
		mImgManager = AsyncImageManager.getInstance();
		mImgManager.restore();
		mDefaultIcon = mContext.getResources().getDrawable(R.drawable.appcenter_default_banner);
		Bundle bundle = getIntent().getExtras();
		mTitleName = bundle.getString(TITLE);
		mIds = bundle.getStringArrayList(APP_ID_LIST);
		mImageIdList = bundle.getStringArrayList(APP_IMG_URL_LIST);
		mDownloadUrlList = bundle.getStringArrayList(APP_URL_LIST);
		mAppId = bundle.getString(APP_ID);
		if (mDownloadUrlList == null || mDownloadUrlList.isEmpty()) {
			mDownloadUrlList = getPicUrlList(mImageIdList, BIG_PIC_TYPE);
		}
		mIcons = bundle.getStringArrayList(APP_ICON_URL_LIST);
		mPosition = bundle.getInt(POSITION, 0);
		if (mAppId != null && mIds != null && (mIds.indexOf(mAppId) != -1)) {
			mPosition = mIds.indexOf(mAppId);
		}
		GOLauncherApp.getContext().startService(
				new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE));
		/**
		 *  再bind服务
		 */
		if (!mHasBindService) {
			mHasBindService = GOLauncherApp.getContext().bindService(
					new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE),
					mConnenction, Context.BIND_AUTO_CREATE);
		}
		/**
		 *  初始化一些变量
		 */
		initValue();
		/**
		 *  初始化下载进度条
		 */
		initDownloadBar();
		/**
		 *  初始化设置按钮
		 */
		initSetLayout();
		/**
		 * 初始化标题
		 */
		initAppTitle();
		/**
		 *  初始化界面
		 */
		initView();
		/**
		 *  初始化下载接收器
		 */
		initDownloadReceiver();
		/**
		 *  重设下载按钮状态
		 */
		resetDownloadBtn();
	}

	/**
	 * 初始化界面
	 */
	private void initView() {
		mIsFirst = true;
		LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.scrollerview);
		
		mIconViews = (WallPaperDetailHorizontalScrollView) findViewById(R.id.bottom_thumbnails_scrollview);
		mDownloadTextView = (LinearLayout) findViewById(R.id.gostore_theme_detail_button);
		mDownloadTextViewText = (TextView) findViewById(R.id.gostore_theme_detail_button_text);
		mDownloadTextViewImg = (ImageView) findViewById(R.id.gostore_theme_detail_button_img);
		mDownloadLayout = (RelativeLayout) findViewById(R.id.gostore_theme_detail_button_layout);
		mDownloadTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				downloadPicture();
			}
		});
		mScrollerViewGroup = new ScrollerViewGroup(mContext, this);
		mScrollerViewGroup.setGapWidth(DrawUtils.dip2px(10f));
		mScrollerViewGroup.setIsNeedGap(true);
		mScrollerViewGroup.setGapColor(Color.parseColor("#f5f5f5"));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		linearLayout.addView(mScrollerViewGroup, params);
		final int size = mImageIdList.size();
		mScrollerViewGroup.setScreenCount(size);
		mImageViewList = new ArrayList<ImageView>();
		for (int i = 0; i < size; i++) {
			final ImageView imgView = new ImageView(mContext);
			imgView.setBackgroundColor(Color.parseColor("#f5f5f5"));
			if (mPosition == i && sLoadImg) {
				String url = getPicUrl(mImageIdList, i, DEF_PIC_TYPE);
				String imgName = String.valueOf(url.hashCode());
				Bitmap bmp = mImgManager.loadImage(LauncherEnv.Path.GOSTORE_ICON_PATH, imgName, url, true,
						false, null, new AsyncImageLoadedCallBack() {
							@Override
							public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
								imgView.setScaleType(ScaleType.CENTER_CROP);
								imgView.setImageBitmap(imageBitmap);
							}
						});
				if (bmp != null) {
					imgView.setScaleType(ScaleType.CENTER_CROP);
					imgView.setImageBitmap(bmp);
				} else {
					imgView.setScaleType(ScaleType.FIT_XY);
					imgView.setImageDrawable(mDefaultIcon);
				}
			} else {
				imgView.setScaleType(ScaleType.FIT_XY);
				imgView.setImageDrawable(mDefaultIcon);
			}
			mScrollerViewGroup.addScreenView(imgView);
			mImageViewList.add(imgView);
		}
		mIconViews.setChildWidth(GoStoreDisplayUtil.scalePxToMachine(mContext, 72));
		mIconViews.setChildHeight(GoStoreDisplayUtil.scalePxToMachine(mContext, 72));
		mIconViews.setListBean(mIcons, mPosition, this);
		if (mPosition >= 0 && mPosition < size) {
			mScrollerViewGroup.gotoViewByIndex(mPosition);
			if (mPosition == 0) {
				refreshScrollView();
			}
		}
		mScrollerViewGroup.setOnImageChangeListener(new OnImageChangeListener() {

			@Override
			public void onImageChange() {
				initValue();
				resetDownloadBtn();
			}
		});
	}
	
	/**
	 * 初始化标题的方法
	 * 
	 * @param text
	 */
	private void initAppTitle() {
		mTitle = (RelativeLayout) findViewById(R.id.title);
		if (mContext != null) {
			LayoutInflater layoutInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mAppTitle = (RelativeLayout) layoutInflater.inflate(
					R.layout.recomm_apps_management_detail_title, null);
			mThemeTitle = (TextView) mAppTitle.findViewById(R.id.recomm_apps_management_detai_title_text);
			if (mTitleName == null || mTitleName.trim().equals("")) {
				mTitleName = getResources().getString(R.string.menuitem_wallpaper);
			}
			mThemeTitle.setText(mTitleName);

			ImageView ivLogo = (ImageView) mAppTitle
					.findViewById(R.id.recomm_apps_management_detai_title_logo);
			ivLogo.setVisibility(View.GONE);
			// 返回键
			ImageView ivBack = (ImageView) mAppTitle
					.findViewById(R.id.recomm_apps_management_detai_title_back);
			ivBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					finish();
				}
			});
			ImageView ivSearch = (ImageView) mAppTitle
					.findViewById(R.id.recomm_apps_management_detai_title_search);
			ivSearch.setVisibility(View.GONE);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, DrawUtils.dip2px(44f));
			mTitle.addView(mAppTitle, params);
		}
	}

	/**
	 * 初始化下载进度条的方法
	 */
	private void initDownloadBar() {
		mDownloadProgressLayout = (RelativeLayout) this
				.findViewById(R.id.gostore_theme_detail_download_progress);
		mDownloadBar = (ProgressBar) this
				.findViewById(R.id.gostore_theme_detail_progressbar);
		mDownloadText = (TextView) this
				.findViewById(R.id.gostore_theme_detail_progresstext);
		mResumeOrStop = (LinearLayout) this
				.findViewById(R.id.app_detail_download_pause_or_resume_click);
		mResumeOrStopImg = (ImageView) this
				.findViewById(R.id.app_detail_download_pause_or_resume);
		mResumeOrStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsDownloading) {
					stopDownload(WallpaperDetailActivity.this, String.valueOf(mDownloadId));
					mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_resume_new);
				} else {
					restartDownload(WallpaperDetailActivity.this, String.valueOf(mDownloadId));
					mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_pause_new);
				}

			}
		});
		mCancelDownload = (LinearLayout) this
				.findViewById(R.id.app_detail_download_cancel_click);
		mCancelDownload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cancelDownload(WallpaperDetailActivity.this, String.valueOf(mDownloadId));
			}
		});
	}
	
	/**
	 * <br>功能简述:开始下载
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param id
	 */
	private void restartDownload(Context context, String id) {
		try {
			if (mDownloadController == null) {
				return;
			}
			mIsDownloading = true;
			PreferencesManager sp = new PreferencesManager(mContext, IPreferencesIds.DOWNLOAD_MANAGER_TASK_STATE,
					Context.MODE_PRIVATE);
			sp.remove(id);
			mDownloadController.restartDownloadById(Long.parseLong(id));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <br>功能简述:暂停下载
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param id
	 */
	private void stopDownload(Context context, String id) {
		try {
			if (mDownloadController == null) {
				return;
			}
			mIsDownloading = false;
			PreferencesManager sp = new PreferencesManager(mContext, IPreferencesIds.DOWNLOAD_MANAGER_TASK_STATE,
					Context.MODE_PRIVATE);
			sp.putString(id, String.valueOf(DownloadTask.TASK_STATE_NORMAL));
			sp.commit();
			mDownloadController.stopDownloadById(Long.parseLong(id));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <br>功能简述:取消下载
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param contex
	 * @param id
	 */
	private void cancelDownload(Context contex, String id) {
		try {
			if (mDownloadController == null) {
				return;
			}
			mIsDownloading = false;
			mDownloadController.removeDownloadTaskById(Long.parseLong(id));
			resetButtonState(DOWNLOAD_CANCEL);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <br>功能简述:下载完成后的设置按钮
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initSetLayout() {
		mSetLayout = (RelativeLayout) this
				.findViewById(R.id.gostore_theme_detail_set_layout);
		mSetButton = (Button) this
				.findViewById(R.id.gostore_wallpaper_detail_set_wallpaper_button_TextView);
	}

	/**
	 * 下载进度接收器
	 * 
	 * @author zhaojunjie
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
						if (mDownloadId >= 0) {
							long appId = task.getId();
							if (mDownloadId != appId) {
								return;
							} else {
								setDownloadState(task.getState(), task.getAlreadyDownloadPercent());
							}
						}
					}
				}
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ICustomAction.ACTION_APP_DOWNLOAD);
		this.registerReceiver(mDownloadReceiver, intentFilter);
	}
	
	private void setDownloadState(int state, int percent) {
		switch (state) {
			case DownloadTask.STATE_WAIT :
				mIsDownloading = false;
				if (percent <= 0) {
					mDownloadTextViewText.setText(R.string.themestore_download_waiting);
					mDownloadTextViewImg.setImageDrawable(null);
					mDownloadTextView
							.setBackgroundResource(R.drawable.gostore_theme_gallery_download_button_no);
					mDownloadTextView.setOnClickListener(null);
					mDownloadProgressLayout.setVisibility(View.GONE);
					mDownloadLayout.setVisibility(View.VISIBLE);
					mSetLayout.setVisibility(View.GONE);
				} else {
					mDownloadProgressLayout.setVisibility(View.VISIBLE);
					mDownloadLayout.setVisibility(View.GONE);
					mSetLayout.setVisibility(View.GONE);
					mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_pause_new);
					updateDownloadProgress(percent);
				}
				mIsDownloading = false;
				resetButtonState(DOWNLOAD_WAIT);
				break;
			case DownloadTask.STATE_DOWNLOADING :
				mIsDownloading = true;
				mDownloadLayout.setVisibility(View.GONE);
				mDownloadProgressLayout.setVisibility(View.VISIBLE);
				mSetLayout.setVisibility(View.GONE);
				mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_pause_new);
				updateDownloadProgress(percent);
				break;
			case DownloadTask.STATE_FINISH :
				resetButtonState(DOWNLOAD_COMPLETE);
				break;
			case DownloadTask.STATE_STOP :
				mIsDownloading = false;
				mDownloadLayout.setVisibility(View.GONE);
				mDownloadProgressLayout.setVisibility(View.VISIBLE);
				mSetLayout.setVisibility(View.GONE);
				mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_resume_new);
				updateDownloadProgress(percent);
				break;
			case DownloadTask.STATE_START :
				mIsDownloading = true;
				mDownloadLayout.setVisibility(View.GONE);
				mDownloadProgressLayout.setVisibility(View.VISIBLE);
				mSetLayout.setVisibility(View.GONE);
				mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_pause_new);
				updateDownloadProgress(percent);
				break;
			case DownloadTask.STATE_RESTART :
				mIsDownloading = true;
				mDownloadLayout.setVisibility(View.GONE);
				mDownloadProgressLayout.setVisibility(View.VISIBLE);
				mSetLayout.setVisibility(View.GONE);
				mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_pause_new);
				updateDownloadProgress(percent);
				break;
			case DownloadTask.STATE_FAIL :
				mIsDownloading = false;
				mDownloadLayout.setVisibility(View.VISIBLE);
				mDownloadProgressLayout.setVisibility(View.GONE);
				mSetLayout.setVisibility(View.GONE);
				resetDownloadBtn();
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

	/**
	 * 初始化下载变量
	 * 
	 * @author zhaojunjie
	 * @throws RemoteException 
	 */
	private void initValue() {
		if (mIds.size() > mPosition) {
			mDownloadId = Long.parseLong(mIds.get(mPosition));
			mDownloadFinishName = mIds.get(mPosition) + PICTURE_FORMAT;
			mDownloadURL = mDownloadUrlList.get(mPosition);
		}
		
	}
	private ServiceConnection mConnenction = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDownloadController = IDownloadService.Stub.asInterface(service);
			try {
				if (mDownloadController != null) {
					mDownloadController.addRunningActivityClassName(WallpaperDetailActivity.class
							.getName());
					// 因为详情界面属于桌面进程
					// 而下载时候用的是GoLauncherApp里的DownloadController
					// 所以这里需要赋值
					GOLauncherApp.getApplication().setDownloadController(mDownloadController);
					resetDownloadBtn();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mDownloadController = null;
		}
	};

	/**
	 * 设置下载按钮样式
	 * 
	 * @param state
	 * @author zhaojunjie
	 */
	protected void resetButtonState(int state) {
		if (mDownloadProgressLayout == null || mDownloadLayout == null || mSetLayout == null
				|| mDownloadTextView == null || mDownloadText == null) {
			return;
		}
		switch (state) {
			case DOWNLOAD_POSSIBLE :
				mDownloadProgressLayout.setVisibility(View.GONE);
				mDownloadLayout.setVisibility(View.VISIBLE);
				mSetLayout.setVisibility(View.GONE);
				mDownloadTextView.setBackgroundResource(R.drawable.appgame_download_btn_selector);
				mDownloadTextViewText.setText(getResources().getString(R.string.gostore_download));
				mDownloadTextViewImg.setImageResource(R.drawable.app_detail_download_icon);
				mDownloadTextView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 点击响应处理。
						mIsDownloading = true;
						showProgress();
						downloadPicture();
					}
				});
				break;

			case DOWNLOAD_STOP :
				mDownloadProgressLayout.setVisibility(View.VISIBLE);
				mDownloadLayout.setVisibility(View.GONE);
				mSetLayout.setVisibility(View.GONE);
				mDownloadText.setText(mPercent + "%");
				mDownloadBar.setProgress(mPercent);
				break;

			case DOWNLOAD_CONTINUE :
				mDownloadProgressLayout.setVisibility(View.VISIBLE);
				mResumeOrStopImg.setBackgroundResource(R.drawable.detail_download_resume_new);
				mDownloadLayout.setVisibility(View.GONE);
				mSetLayout.setVisibility(View.GONE);
				mDownloadText.setText(mPercent + "%");
				mDownloadBar.setProgress(mPercent);
				break;

			case DOWNLOAD_CANCEL :
				mDownloadProgressLayout.setVisibility(View.GONE);
				mSetLayout.setVisibility(View.GONE);
				mDownloadLayout.setVisibility(View.VISIBLE);
				mDownloadTextViewText.setText(R.string.gostore_download);
				mDownloadTextViewImg.setImageResource(R.drawable.app_detail_download_icon);
				mDownloadTextView.setBackgroundResource(R.drawable.appgame_download_btn_selector);
				mDownloadTextView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mIsDownloading = true;
						showProgress();
						downloadPicture();
					}
				});
				break;

			case DOWNLOAD_COMPLETE :
				mDownloadProgressLayout.setVisibility(View.GONE);
				mDownloadLayout.setVisibility(View.GONE);
				mSetLayout.setVisibility(View.VISIBLE);
				mIsDownloading = false;
				mSetButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						setWallpaper();
					}
				});
				break;
			case DOWNLOAD_WAIT :
				mDownloadLayout.setVisibility(View.VISIBLE);
				mDownloadProgressLayout.setVisibility(View.GONE);
				mSetLayout.setVisibility(View.GONE);
				mDownloadTextViewText.setText(R.string.themestore_download_connecting);
				mDownloadTextViewImg.setImageDrawable(null);
				mDownloadTextView.setBackgroundResource(R.drawable.gostore_theme_gallery_download_button_no);
				mDownloadTextView.setOnClickListener(null);
				break;
			default :
				break;
		}
	}

	/**
	 * 下载壁纸
	 * 
	 * @author zhaojunjie
	 */
	public void downloadPicture() {
		/**
		 *  判断sd卡是否存在
		 */
		if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			Toast.makeText(mContext, R.string.gostore_no_sdcard, Toast.LENGTH_LONG).show();
			return;
		}
		if (mDownloadProgressLayout != null) {
			mDownloadProgressLayout.setVisibility(View.VISIBLE);
			mDownloadLayout.setVisibility(View.GONE);
			mSetLayout.setVisibility(View.GONE);
		}
		GoStoreOperatorUtil.downloadFileDirectly(getApplicationContext(), mDownloadFinishName, // 在通知条显示的文件名字
				DOWNLOAD_PIC_DIRECTORY_PATH, mDownloadURL, mDownloadId, mDownloadFinishName);

	}


	/**
	 * 判断文件是否存在
	 * 
	 * @return
	 * @author zhaojunjie
	 */
	private boolean isFileExists(String filename) {
		File f = new File(DOWNLOAD_PIC_DIRECTORY_PATH + filename);
		if (!f.exists()) {
			return false;
		}
		return true;
	}

	/**
	 * 重设下载按钮状态(是否已下载)
	 * 
	 * @author zhaojunjie
	 */
	private void resetDownloadBtn() {
		if (mDownloadController != null) {
			try {
				DownloadTask task = mDownloadController.getDownloadTaskById(mDownloadId);
				if (task != null) {
					mPercent = task.getAlreadyDownloadPercent();
					setDownloadState(task.getState(), task.getAlreadyDownloadPercent());
				} else {
					if (isFileExists(mDownloadFinishName)) {
						resetButtonState(DOWNLOAD_COMPLETE);
					} else {
						resetButtonState(DOWNLOAD_POSSIBLE);
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
		}
//		if (isFileExists(mDownloadFinishName)) {
//			resetButtonState(DOWNLOAD_COMPLETE);
//		} else if (isFileExists(mDownloadFinishName + PICTURE_DOWNLOADING_FORMAT)) {
//			resetButtonState(DOWNLOAD_CONTINUE);
//		} else {
//			resetButtonState(DOWNLOAD_POSSIBLE);
//		}
	}


	/**
	 * 显示进度条和百分比数字
	 */
	private void showProgress() {
		// 显示下载进度栏
		mDownloadProgressLayout.setVisibility(View.VISIBLE);
		mDownloadLayout.setVisibility(View.GONE);
		mSetLayout.setVisibility(View.GONE);
	}

	/**
	 * 设置图片为桌面壁纸（调试中）
	 * 
	 * @author zhaojunjie
	 */
	private void setWallpaper() {
		if (isFileExists(mDownloadFinishName)) {
			WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			/**
			 *  由于现在壁纸大小不规范，所以先压缩再设置
			 */
			Bitmap bitmap = BitmapFactory.decodeFile(DOWNLOAD_PIC_DIRECTORY_PATH
					+ mDownloadFinishName, opts);
			opts.inJustDecodeBounds = false;
			/**
			 *  计算缩放比
			 */
			int be = (int) (opts.outHeight / (float) 280);
			if (be <= 0) {
				be = 1;
			}
			/**
			 *  表示缩略图大小为原始图片大小的几分之一，
			 *  即如果这个值为2，则取出的缩略图的宽和高都是原始图片的1/2，图片大小就为原始大小的1/4
			 */
			opts.inSampleSize = be;
			/**
			 *  重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false
			 */
			bitmap = BitmapFactory.decodeFile(DOWNLOAD_PIC_DIRECTORY_PATH + mDownloadFinishName,
					opts);

			if (bitmap == null) {
				Toast.makeText(this,
						getResources().getString(R.string.gostore_set_wallpaper_failed),
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				wallpaperManager.setBitmap(bitmap);
				Toast.makeText(this,
						getResources().getString(R.string.gostore_set_wallpaper_succeed),
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(this,
						getResources().getString(R.string.gostore_set_wallpaper_failed),
						Toast.LENGTH_SHORT).show();
			}

			bitmap.recycle();
		} else {
			downloadPicture();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		resetDownloadBtn();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		recycle();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}


	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		try {
			super.startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.activitynofound, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	private void recycle() {
		mContext = null;
		mTitle = null;
		if (mThemeTitle != null) {
			mThemeTitle = null;
		}

		if (mDownloadReceiver != null) {
			this.unregisterReceiver(mDownloadReceiver);
			mDownloadReceiver = null;
		}

		if (mScrollerViewGroup != null) {
			mScrollerViewGroup.destory();
			mScrollerViewGroup = null;
		}

		if (mImageViewList != null && mImageViewList.size() > 0) {
			for (ImageView imageView : mImageViewList) {
				if (imageView != null) {
					imageView.setImageBitmap(null);
					imageView = null;
				}
			}
		}
		
		if (mDownloadTextView != null) {
			mDownloadTextView.setBackgroundDrawable(null);
			mDownloadTextView.setOnClickListener(null);
			mDownloadTextView = null;
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
		if (mSetLayout != null) {
			mSetLayout = null;
		}
		if (mImgManager != null) {
			int size = mImageIdList.size();
			for (int i = 0; i < size; i++) {
				mImgManager.recycle(getPicUrl(mImageIdList, i, DEF_PIC_TYPE));
			}
			mImgManager.removeAllTask();
			mImgManager.restore();
			mImgManager = null;
		}
		if (mIds != null) {
			mIds.clear();
			mIds = null;
		}
		if (mImageIdList != null) {
			mImageIdList.clear();
			mImageIdList = null;
		}
		if (mDownloadUrlList != null) {
			mDownloadUrlList.clear();
			mDownloadUrlList = null;
		}
		if (mIcons != null) {
			mIcons.clear();
			mIcons = null;
		}
		if (mDownloadLayout != null) {
			mDownloadLayout = null;
		}
		if (mDownloadProgressLayout != null) {
			mDownloadProgressLayout = null;
		}
		if (mSetLayout != null) {
			mSetLayout = null;
		}
	}

	@Override
	public ScreenScroller getScreenScroller() {
		return null;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {

	}

	@Override
	public void onFlingIntercepted() {

	}

	@Override
	public void onScrollStart() {

	}

	@Override
	public void onFlingStart() {

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mPosition = newScreen;
		if (mIconViews != null) {
			if (mImageViewList != null && mImageViewList.size() > 0 && sLoadImg) {
				int listSize = mImageViewList.size();
				int bmpMaxSide = 1;
				if (listSize > bmpMaxSide * 2 + 1) {
					for (int i = 0; i < listSize; i++) {
						if (i < mPosition - bmpMaxSide || i > mPosition + bmpMaxSide) {
							ImageView bmp = mImageViewList.get(i);
							if (bmp != null) {
								bmp.setScaleType(ScaleType.FIT_XY);
								bmp.setImageDrawable(mDefaultIcon);
								if (mImgManager != null) {
									mImgManager.recycle(getPicUrl(mImageIdList, i, DEF_PIC_TYPE));
								}
							}
						}
					}
				}
			}
			if (mIconViews != null) {
				mIsFirst = false;
				mIconViews.setHightLight(mPosition);
			}
		}
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		refreshScrollView();
		resetDownloadBtn();
	}

	private void refreshScrollView() {
		if (mScrollerViewGroup != null) {
			mPosition = mScrollerViewGroup.getCurrentViewIndex();
			if (mPosition >= 0 && mPosition < mScrollerViewGroup.getChildCount()) {
				final ImageView mCurrentImageView = (ImageView) mScrollerViewGroup.getChildAt(mPosition);
				if (mCurrentImageView != null && sLoadImg) {
					String url = getPicUrl(mImageIdList, mPosition, DEF_PIC_TYPE);
					String imgName = String.valueOf(url.hashCode());
					Bitmap bmp = mImgManager.loadImage(LauncherEnv.Path.GOSTORE_ICON_PATH, imgName, url, true,
							false, null, new AsyncImageLoadedCallBack() {
								@Override
								public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
									mCurrentImageView.setScaleType(ScaleType.CENTER_CROP);
									mCurrentImageView.setImageBitmap(imageBitmap);
								}
							});
					if (bmp != null) {
						mCurrentImageView.setScaleType(ScaleType.CENTER_CROP);
						mCurrentImageView.setImageBitmap(bmp);
					} else {
						mCurrentImageView.setScaleType(ScaleType.FIT_XY);
						mCurrentImageView.setImageDrawable(mDefaultIcon);
					}
				}
				if (mIconViews != null && mIsFirst) {
					mIsFirst = false;
					mIconViews.setHightLight(mPosition);
				}
			}
		}
	}

	@Override
	public void scrollBy(int x, int y) {

	}

	@Override
	public int getScrollX() {
		return 0;
	}

	@Override
	public int getScrollY() {
		return 0;
	}

	@Override
	public void invalidate() {

	}


	@Override
	public void refresh(int position) {
		if (mScrollerViewGroup != null) {
			mPosition = position;
			mScrollerViewGroup.gotoViewByIndexImmediately(position);
		}
	}
	
	private String getPicUrl(ArrayList<String> urls, int position, int type) {
		String url = null;
		if (urls == null || position >= urls.size()) {
			return null;
		}
		url = urls.get(position);
		if (type == DEF_PIC_TYPE) {
			url = url.substring(0, url.indexOf("##"));
		} else if (type == BIG_PIC_TYPE) {
			if (url.indexOf("@@") == -1) {
				url = url.substring(url.indexOf("##") + 2, url.length());
			} else {
				url = url.substring(url.indexOf("##") + 2, url.indexOf("@@"));
			}
		}
		return url;
	}
	
	private ArrayList<String> getPicUrlList(ArrayList<String> urls, int type) {
		if (urls == null) {
			return null;
		}
		int size = urls.size();
		ArrayList<String> urlList = new ArrayList<String>(size);
		for (String s : urls) {
			if (type == DEF_PIC_TYPE) {
				s = s.substring(0, s.indexOf("##"));
			} else if (type == BIG_PIC_TYPE) {
				if (s.indexOf("@@") == -1) {
					s = s.substring(s.indexOf("##") + 2, s.length());
				} else {
					s = s.substring(s.indexOf("##") + 2, s.indexOf("@@"));
				}
			}
			urlList.add(s);
		}
		return urlList;
	}
}