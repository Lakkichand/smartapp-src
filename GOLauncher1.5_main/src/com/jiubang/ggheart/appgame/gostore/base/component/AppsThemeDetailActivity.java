package com.jiubang.ggheart.appgame.gostore.base.component;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.appcenter.contorler.AppDetailController;
import com.jiubang.ggheart.appgame.base.bean.AppDetailInfoBean;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.SecurityInfo;
import com.jiubang.ggheart.appgame.base.bean.SecurityInfo.ThirdSecurityItem;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.component.AppsDetailContainer;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.base.data.AppJsonOperator;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.net.AppGameNetRecord;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.base.setting.AppGameSettingData;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.base.utils.AppGameConfigUtils;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IFinishNotice;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.theme.LockerManager;
import com.jiubang.ggheart.data.theme.broadcastReceiver.MyThemeReceiver;
import com.jiubang.ggheart.data.theme.zip.ZipResources;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:应用中心，应用、主题详情activity
 * <br>功能详细描述:
 * 
 * @author  lijunye
 * @date  [2012-12-13]
 */
public class AppsThemeDetailActivity extends Activity implements IFinishNotice {
	/**
	 * 主题类型
	 */
	public final static int THEME_TYPE = 21;
	/**
	 * 锁屏类型
	 */
	public final static int LOCKER_TYPE = 22;
	/**
	 * 超级主题类型
	 */
	public final static int SUPER_THEME_TYPE = 24;
	/**
	 * 动态壁纸类型
	 */
	public final static int LIVE_WALLPAPER = 20;
	/**
	 * 应用类型
	 */
	public final static int APP_TYPE = 0;

	public final static String KEY_DETAIL_GRADE = "Key_Detail_Grage"; // 应用星级评分
	public final static String KEY_APP_NAME = "Key_App_Name"; // 应用程序名
	/**
	 * 详情应用的下载方式，FTP下载
	 */
	public static final int DETAIL_DOWNLOAD_TYPE_FTP = 1;
	/**
	 * 详情应用下载方式，跳转电子市场
	 */
	public static final int DETAIL_DOWNLOAD_TYPE_MARKET = 2;
	/**
	 * 详情应用下载方式，跳转网页
	 */
	public static final int DETAIL_DOWNLOAD_TYPE_WEB = 3;
	/**
	 * 正在下载的app包名
	 */
	public final static String DOWNLOADING_APP_PACKAGE_NAME = "downloading_app_pkg_name";
	/**
	 * 正在下载的appid
	 */
	public final static String DOWNLOADING_APP_ID = "downloading_app_id";
	/**
	 * 打开此窗体的应用类型（应用管理/GoStore中进入）
	 */
	public final static String START_GOSTORE_TYPE = "start_gostore_type";
	/**
	 * 推荐分类ID(从应用推荐中进入时带此参数)
	 */
	public final static String START_RECOMMENDED_CATEGORYID = "start_commended_categoryid";
	/**
	 * 主题处于列表的位置
	 */
	public final static String BEAN_POSITION = "bean_position";
	/**
	 * 主题图标url列表
	 */
	public final static String ICON_LIST = "icon_list";
	/**
	 * 主题包名列表
	 */
	public final static String PKG_LIST = "pgk_list";
	/**
	 * 主题id列表
	 */
	public final static String ID_LIST = "id_list";
	/**
	 * 是否需要显示主题图标列表
	 */
	public final static String SHOW_LIST = "show_list";
	/**
	 * 详情风格
	 */
	public final static String DETAIL_STYLE = "detail_style";
	/**
	 * 数据bean
	 */
	public final static String KEY_DATA_BEAN = "KEY_DATA_BEAN";
	/**
	 * 进入类型(widget)
	 */
	public final static int ENTRY_ID_WIDGET = 1;
	/**
	 * 没有数据
	 */
	private static final int MSG_SHOW_NO_TIP = 220;
	/**
	 * 获取数据结束
	 */
	private static final int MSG_GET_DATA_FINISH = 221;
	/**
	 * 重新获取数据
	 */
	public static final int MSG_RE_GET_DATA = 234;
	/**
	 * 应用主题
	 */
	public static final int APPLY_THEME = 222;
	/**
	 * 应用锁屏主题
	 */
	public static final int APPLY_LOCKER_THEME = 223;
	/**
	 * 下载apk点击逻辑
	 */
	public static final int CLICK_DOWNLOAD_APK_NO_PAY = 224;
	/**
	 * 下载zip点击逻辑
	 */
	public static final int CLICK_DOWNLOAD_ZIP = 225;
	/**
	 * 下载统计
	 */
	public static final int DOWNLOAD_CLICK_COUNT = 226;
	/**
	 * 更新统计
	 */
	public static final int UPDATE_CLICK_COUNT = 227;
	/**
	 * 开始下载
	 */
	public static final int RESTART_DOWNLOAD = 228;
	/**
	 * 暂停下载
	 */
	public static final int STOP_DOWNLOAD = 229;
	/**
	 * 取消下载
	 */
	public static final int CANCEL_DOWNLOAD = 230;
	/**
	 * 打开安全认证对话框
	 */
	public static final int SHOW_SECURITY_DIALOG = 231;
	/**
	 * 打开应用
	 */
	public static final int OPEN_APK = 232;

	// 是否显示相关推荐，0就不显示，1就显示

	public static final String RECOMM_APP_SHOW_OR_NOT = "recommAppShowOrNot";
	/**
	 * 普通详情
	 */
	private static final int DEFAULT_DETAIL_STYLE = 0;
	/**
	 * 大图详情
	 */
	private static final int BIG_DETAIL_STYLE = 1;
	
	private static final int NETWORK_NO_CONNECTION = 0; // 无网络连接
	private static final int NETWORK_CONNECTION_TIMEOUT = 1; // 网络连接超时
	
	private boolean mIsDestory = false;
	
	// 初次初始化未拿到数据时的数据Bean
	private AppDetailInfoBean mParameInfoBean;

	// 更新进度条的action
	public final static String PERSENT_KEY = "persent_key"; // 进度百分比key
	public final static String DOWNLOADING_APP_NAME = "downloading_app_filename"; // 正在下载的app
	public final static String ENTRY_ID = "entry_id"; // 进入类型

	private LinearLayout mWholeView = null; // 整个view
	private TextView mThemeTitle = null; // 标题栏
	private View mContentView = null; // 内容view
	private LinearLayout mProgressLinearLayout; // 进度条
	private RelativeLayout mNoDataTipRelativeLayout; // 没有结果提示
	private int mDownloadId = 0; // 正在下载的APP ID
	private int mStartType = 0; // 入口类型(0 [默认]从Gostore，1 从应用更新，2从应用推荐)
	private int mPosition = 0; //当前详情位置
	private int mDetailStyle = 0;
	public static final int RECOMMAPPSHOW = 0;
	public static final int RECOMMAPPNOTSHOW = 1;
	private int mShowOrNot = RECOMMAPPNOTSHOW;
	private boolean mHasBindService = false; //是否已经绑定下载服务
	public static boolean sLoadImg = true;
	private String mDownloadPkg = null; // 正在下载的APP包名
	private String mCategoryID = null; // 推荐分类ID
	private ArrayList<String> mIconList = null; //主题图标url列表
	private ArrayList<String> mItemPkgList = null; //主题包名列表
	private ArrayList<String> mItemIdList = null; //主题id列表
	private Context mContext = null;
	private IDownloadService mDownloadController = null; //下载服务控制器
	private AppDetailController mController = null;
	private Handler mHandler = null;
	private AppsDetailContainer mAppsDetailContainer;
	private ThemeDetailContainer mThemeDetailContainer;
	private AsyncImageManager mImgManager;
	private Drawable mDefaultIcon;
	private RelativeLayout mAppTitle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = AppsThemeDetailActivity.this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
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
		mImgManager = AsyncImageManager.getInstance();
		mImgManager.restore();
		mDefaultIcon = mContext.getResources().getDrawable(R.drawable.default_icon);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			mDownloadPkg = bundle.getString(DOWNLOADING_APP_PACKAGE_NAME);
			mDownloadId = bundle.getInt(DOWNLOADING_APP_ID);
			mStartType = bundle.getInt(START_GOSTORE_TYPE);
			boolean showList = bundle.getInt(SHOW_LIST) == 1;
			if (showList) {
				//　如果是需要显示icon list的
				mIconList = bundle.getStringArrayList(ICON_LIST);
			}
			mItemPkgList = bundle.getStringArrayList(PKG_LIST);
			mItemIdList = bundle.getStringArrayList(ID_LIST);
			mPosition = bundle.getInt(BEAN_POSITION);
			mShowOrNot = bundle.getInt(RECOMM_APP_SHOW_OR_NOT);
			mDetailStyle = bundle.getInt(DETAIL_STYLE, 0);
			mCategoryID = bundle.getString(START_RECOMMENDED_CATEGORYID);
			Object infoBean = bundle.getSerializable(KEY_DATA_BEAN);
			initParameInfoBean(infoBean);
		}
		mController = new AppDetailController(this);
		if (mItemPkgList != null && mItemPkgList.indexOf(mDownloadPkg) != -1) {
			mPosition = mItemPkgList.indexOf(mDownloadPkg);
		}
		initView();
		initProgress();
		initNoDataTipRelativeLayout();
		initHandler();
		GOLauncherApp.getContext().startService(new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE));
		// 再bind服务
		if (!mHasBindService) {
			mHasBindService = GOLauncherApp.getContext().bindService(
					new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE), mConnenction,
					Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAppsDetailContainer != null) {
			mAppsDetailContainer.onResume();
		}
		if (mThemeDetailContainer != null) {
			mThemeDetailContainer.onResume();
		}
	}
	
	@Override
	public void onBackPressed() {
		if (mStartType == AppsDetail.START_TYPE_WIDGET_APP) {
			AppsManagementActivity.startAppCenter(mContext,
					MainViewGroup.ACCESS_FOR_SHORTCUT, false);
		}
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		
		mIsDestory = true;
		onSendFinishBocast();
		if (mWholeView != null) {
			mWholeView.removeAllViews();
			mWholeView = null;
		}
		if (mContentView != null) {
			mContentView = null;
		}
		mProgressLinearLayout = null;
		mNoDataTipRelativeLayout = null;
		if (mHasBindService) {
			GOLauncherApp.getContext().unbindService(mConnenction);
			mHasBindService = false;
		}
		if (mConnenction != null) {
			mConnenction = null;
		}
		if (mAppsDetailContainer != null) {
			mAppsDetailContainer.recycle();
			mAppsDetailContainer = null;
		}
		if (mThemeDetailContainer != null) {
			mThemeDetailContainer.recycle();
			mThemeDetailContainer = null;
		}
		if (mImgManager != null) {
			mImgManager.restore();
			mImgManager = null;
		}
		super.onDestroy();
	}

	private ServiceConnection mConnenction = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDownloadController = IDownloadService.Stub.asInterface(service);
			try {
				if (mDownloadController != null) {
					mDownloadController.addRunningActivityClassName(AppsThemeDetailActivity.class
							.getName());
					// 因为详情界面属于桌面进程
					// 而下载时候用的是GoLauncherApp里的DownloadController
					// 所以这里需要赋值
					GOLauncherApp.getApplication().setDownloadController(mDownloadController);

				}
				// 获取详情数据
				getDetailData();

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mDownloadController = null;
		}
	};

	private void initView() {
		mWholeView = new LinearLayout(this);
		mWholeView.setOrientation(LinearLayout.VERTICAL);
		mWholeView.setBackgroundColor(Color.parseColor("#F5F5F5"));
		this.setContentView(mWholeView);
	}

	/**
	 * 初始化进度条的方法
	 */
	private void initProgress() {
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		mProgressLinearLayout = (LinearLayout) layoutInflater.inflate(
				R.layout.appgame_btmprogress, null);
	}

	/**
	 * 初始化没有结果提示文本的方法
	 */
	private void initNoDataTipRelativeLayout() {
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		mNoDataTipRelativeLayout = (RelativeLayout) layoutInflater.inflate(
				R.layout.themestore_nodata_tip_full, null);
	}

	private void showNoDataTip() {
		if (mWholeView != null) {
			mWholeView.removeAllViews();
			if (mNoDataTipRelativeLayout != null) {
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				mWholeView.removeView(mNoDataTipRelativeLayout);
				mWholeView.addView(mNoDataTipRelativeLayout, params);
			}
		}
	}

	/**
	 * <br>功能简述:显示进度条
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void showProgress() {
		if (mWholeView != null) {
			mWholeView.removeAllViews();
			if (mProgressLinearLayout != null) {
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				mWholeView.addView(mProgressLinearLayout, params);
			}
		}
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
		mThemeTitle = (TextView) mAppTitle
				.findViewById(R.id.recomm_apps_management_detai_title_text);
		mThemeTitle.setText(text);

		ImageView ivLogo = (ImageView) mAppTitle
				.findViewById(R.id.recomm_apps_management_detai_title_logo);
		ivLogo.setVisibility(View.GONE);
		// 返回键
		ImageView ivBack = (ImageView) mAppTitle
				.findViewById(R.id.recomm_apps_management_detai_title_back);
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mStartType == AppsDetail.START_TYPE_WIDGET_APP) {
					AppsManagementActivity.startAppCenter(mContext,
							MainViewGroup.ACCESS_FOR_APPCENTER_APPS, false);
				}
				finish();
			}
		});
		ImageView ivSearch = (ImageView) mAppTitle
				.findViewById(R.id.recomm_apps_management_detai_title_search);
		ivSearch.setVisibility(View.GONE);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				DrawUtils.dip2px(44f));
		if (mWholeView != null) {
			mWholeView.addView(mAppTitle, params);
		}
	}

	private void initHandler() {
		if (mHandler == null) {
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					if (mIsDestory) {
						return;
					}
					if (msg == null) {
						return;
					}
					if (mContext == null) {
						return;
					}
					AppDetailInfoBean infoBean = null;
					switch (msg.what) {
						case MSG_SHOW_NO_TIP :
							if (mParameInfoBean == null || mDetailStyle != DEFAULT_DETAIL_STYLE) {
								showNoDataTip();
								Toast.makeText(getApplicationContext(), R.string.http_exception,
										Toast.LENGTH_SHORT).show();
							} else {
								showNetWorkErrorDialog(NETWORK_CONNECTION_TIMEOUT);
							}
							break;
						case MSG_GET_DATA_FINISH :
							if (msg.obj != null && msg.obj instanceof AppDetailInfoBean) {
								infoBean = (AppDetailInfoBean) msg.obj;
								if (mWholeView != null) {
									mWholeView.removeAllViews();
									if (mContext == null) {
										mContext = getApplicationContext();
									}
									int detailStyle = infoBean.mDetailStlye;
									if (mDetailStyle >= 0) {
										detailStyle = mDetailStyle;
									}
									if (detailStyle == BIG_DETAIL_STYLE) {
										initAppTitle(infoBean.mName);
										mThemeDetailContainer = new ThemeDetailContainer(mContext,
												mDownloadController);
										mThemeDetailContainer.initValue(infoBean, mThemeTitle,
												mItemPkgList, mIconList, mItemIdList, mPosition,
												mHandler);
										mContentView = mThemeDetailContainer.getView();
										mWholeView.addView(mContentView);
									} else {
										if (mAppsDetailContainer == null) {
											mAppsDetailContainer = new AppsDetailContainer(mContext,
													mDownloadController);
											mAppsDetailContainer.setHasObtainDetail(true);
											mAppsDetailContainer.initValue(infoBean, infoBean.mPkgName,
													infoBean.mAppId, mStartType, mCategoryID,
													infoBean.mGrade, infoBean.mName, mShowOrNot,
													mHandler);
											mContentView = mAppsDetailContainer.getView();
											mWholeView.addView(mContentView);
										} else {
											mAppsDetailContainer.setHasObtainDetail(true);
											mAppsDetailContainer.setAppDetailInfoBean(infoBean);
											mContentView = mAppsDetailContainer.getView();
											mWholeView.addView(mContentView);
										}
									}
								}
							}
							break;
						case MSG_RE_GET_DATA :
							getDetailData();
							break;
						case APPLY_THEME :
							if (msg.obj == null || !(msg.obj instanceof AppDetailInfoBean)) {
								return;
							}
							infoBean = (AppDetailInfoBean) msg.obj;
							String pkg = infoBean.mPkgName;
							Intent intentGoLauncher = new Intent();
							intentGoLauncher.setClass(AppsThemeDetailActivity.this,
									GoLauncher.class);
							AppsThemeDetailActivity.this.startActivity(intentGoLauncher);
							Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
							intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING,
									MyThemeReceiver.CHANGE_THEME);
							intent.putExtra(MyThemeReceiver.PKGNAME_STRING, pkg);
							AppsThemeDetailActivity.this.sendBroadcast(intent);
							AppsThemeDetailActivity.this.finish();
							break;
						case APPLY_LOCKER_THEME :
							if (msg.obj == null || !(msg.obj instanceof AppDetailInfoBean)) {
								return;
							}
							infoBean = (AppDetailInfoBean) msg.obj;
							String pkgName = infoBean.mPkgName;
							if (pkgName.startsWith("com.jiubang.goscreenlock.theme")) {
								boolean isZip = LockerManager.getInstance(mContext).isZipTheme(mContext, pkgName);
								if (isInstallGoLocker()) {
									if (isGoLockerVersionLow(isZip)) {
										showGoLockerUpdateTips(pkgName);
									} else {
										Intent lockerIntent = new Intent();
										lockerIntent.setAction(ICustomAction.ACTION_LOCKER_DETAIL);
										lockerIntent.putExtra("IS_ZIP_THEME", isZip);
										if (isZip) {
											String apkPath = LockerManager.getInstance(mContext).getZipThemeFileName(
													pkgName);
											lockerIntent.putExtra("ZIP_FILE_NAME", apkPath);
										}
										lockerIntent.putExtra(
												ThemeConstants.LOCKER_PACKAGE_NAME_EXTRA_KEY,
												pkgName);
										try {
											AppsThemeDetailActivity.this
													.startActivity(lockerIntent);
										} catch (ActivityNotFoundException e) {
											e.printStackTrace();
										}
									}
								} else {
									new AlertDialog.Builder(mContext)
											.setTitle(
													mContext.getResources().getString(
															R.string.locker_tip_title))
											.setMessage(
													mContext.getResources().getString(
															R.string.locker_tip_message))
											.setNegativeButton(
													mContext.getResources().getString(R.string.no),
													null)
											.setPositiveButton(
													mContext.getResources().getString(R.string.yes),
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog,
																int which) {
															CheckApplication
																	.downloadAppFromMarketGostoreDetail(
																			mContext,
																			LauncherEnv.GO_LOCK_PACKAGE_NAME,
																			LauncherEnv.Url.GOLOCKER_IN_THEME_WITH_GOOGLE_REFERRAL_LINK);
														}
													}).show();
								}
							}
							break;
						case CLICK_DOWNLOAD_APK_NO_PAY :
							if (msg.obj != null && msg.obj instanceof AppDetailInfoBean) {
								infoBean = (AppDetailInfoBean) msg.obj;
								downloadApkNoPay(mContext, infoBean);
							}
							break;
						case CLICK_DOWNLOAD_ZIP :
							if (msg.obj != null && msg.obj instanceof AppDetailInfoBean) {
								infoBean = (AppDetailInfoBean) msg.obj;
								downloadZip(mContext, infoBean);
							}
							break;
						case DOWNLOAD_CLICK_COUNT :
							if (msg.obj != null && msg.obj instanceof AppDetailInfoBean) {
								infoBean = (AppDetailInfoBean) msg.obj;
								countDownloadClick(infoBean);
							}
							break;
						case UPDATE_CLICK_COUNT :
							if (msg.obj != null && msg.obj instanceof AppDetailInfoBean) {
								infoBean = (AppDetailInfoBean) msg.obj;
								countUpdateClick(infoBean);
							}
							break;
						case RESTART_DOWNLOAD :
							if (msg.obj != null && msg.obj instanceof String) {
								String id = (String) msg.obj;
								restartDownload(mContext, id);
							}
							break;
						case STOP_DOWNLOAD :
							if (msg.obj != null && msg.obj instanceof String) {
								String id = (String) msg.obj;
								stopDownload(mContext, id);
							}
							break;
						case CANCEL_DOWNLOAD :
							if (msg.obj != null && msg.obj instanceof String) {
								String id = (String) msg.obj;
								cancelDownload(mContext, id);
							}
							break;
						case SHOW_SECURITY_DIALOG :
							if (msg.obj != null && msg.obj instanceof AppDetailInfoBean) {
								infoBean = (AppDetailInfoBean) msg.obj;
								showSecurityDialog(mContext, infoBean);
							}
						case OPEN_APK :
							if (msg.obj != null && msg.obj instanceof AppDetailInfoBean) {
								infoBean = (AppDetailInfoBean) msg.obj;
								startApp(mContext, infoBean.mPkgName);
							}
							break;
						default :
							break;
					}
				}
			};
		}
	}
	private void getDetailData() {
		if (mParameInfoBean == null || mDetailStyle != DEFAULT_DETAIL_STYLE || mWholeView == null) {
			showProgress();
			if (!Machine.isNetworkOK(this)) {
				showNoDataTip();
			}
		} else {
			mAppsDetailContainer = new AppsDetailContainer(mContext,
					mDownloadController);
			mAppsDetailContainer.setHasObtainDetail(false);
			mAppsDetailContainer.initValue(mParameInfoBean, mParameInfoBean.mPkgName,
					mParameInfoBean.mAppId, mStartType, mCategoryID,
					mParameInfoBean.mGrade, mParameInfoBean.mName, mShowOrNot,
					mHandler);
			mContentView = mAppsDetailContainer.getView();
			mWholeView.addView(mContentView);
			if (!Machine.isNetworkOK(this)) {
				showNetWorkErrorDialog(NETWORK_NO_CONNECTION);
				return;
			}
		}
		final int startType = AppsDetail.START_TYPE_APPRECOMMENDED;
		String url = mController.getRequestUrlByType(this);
		JSONObject postdata = mController.getPostJSON(this, startType, mDownloadId, mDownloadPkg,
				mDetailStyle);
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
							if (mHandler != null) {
								mHandler.sendEmptyMessage(MSG_SHOW_NO_TIP);
							}
						} else {
							if (mHandler != null) {
								Message msg = mHandler.obtainMessage();
								msg.obj = detailInfoBean;
								msg.what = MSG_GET_DATA_FINISH;
								mHandler.sendMessage(msg);
							}
						}
					} else {
						if (mHandler != null) {
							mHandler.sendEmptyMessage(MSG_SHOW_NO_TIP);
						}
					}
				}

				@Override
				public void onException(THttpRequest arg0, int arg1) {
					if (mHandler != null) {
						mHandler.sendEmptyMessage(MSG_SHOW_NO_TIP);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			if (mHandler != null) {
				mHandler.sendEmptyMessage(MSG_SHOW_NO_TIP);
			}
		}
		if (request != null) {
			/**
			 * 设置备选url
			 */
			try {
				request.addAlternateUrl(mController.getAlternativeUrl(this));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			/**
			 *  设置线程优先级，
			 *  读取数据的线程优先级应该最高（比拿取图片的线程优先级高）
			 */
			request.setRequestPriority(Thread.MAX_PRIORITY);
			request.setOperator(new AppJsonOperator());
			request.setNetRecord(new AppGameNetRecord(this, false));
			AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(this);
			httpAdapter.addTask(request);
		}
	}

	/**
	 * 是否安装go锁屏
	 * 
	 * */
	private boolean isInstallGoLocker() {
		return AppUtils.isAppExist(this, new Intent(ICustomAction.ACTION_LOCKER));
	}

	/**
	 * 判断本机安装的go锁屏软件版本是否过低
	 * 
	 */
	private boolean isGoLockerVersionLow(boolean isZip) {
		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(LauncherEnv.Plugin.LOCKER_PACKAGE, 0);
			int appVersionCode = info.versionCode;
			if (isZip && appVersionCode < ThemeConstants.REQUEST_LOCKER_GO_THEME_VERSION_NUM) {
				return true;
			} else if (!isZip && appVersionCode < ThemeConstants.REQUEST_LOCKER_VERSION_NUM) {
				return true;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 锁屏版本过低 提示
	 */
	private void showGoLockerUpdateTips(final String mPackageName) {
		AlertDialog.Builder builder = new Builder(AppsThemeDetailActivity.this);
		builder.setMessage(R.string.locker_low_verson_tips_content);
		builder.setTitle(R.string.locker_low_verson_tips_title);
		builder.setPositiveButton(R.string.locker_low_verson_update,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!AppUtils.gotoMarket(AppsThemeDetailActivity.this,
								LauncherEnv.Market.APP_DETAIL + LauncherEnv.Plugin.LOCKER_PACKAGE)) {
							AppUtils.gotoBrowser(AppsThemeDetailActivity.this,
									LauncherEnv.Url.GOLOCKER_DOWNLOAD_URL);
						}
					}
				});

		builder.setNegativeButton(R.string.locker_low_verson_use, null);
		builder.create().show();
	}

	/**
	 * <br>功能简述: 下载zip包
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param infoBean
	 */
	private void downloadZip(Context context, AppDetailInfoBean infoBean) {
		if (infoBean != null) {
			AppRecommendedStatisticsUtil.getInstance().saveDownloadComplete(mContext,
					infoBean.mPkgName, String.valueOf(infoBean.mAppId), 1);
			AppRecommendedStatisticsUtil.getInstance().saveDownloadSetup(mContext,
					infoBean.mPkgName);
			int[] payType = infoBean.mPayType;
			if (payType != null && payType.length > 0 && payType[0] == GoStoreOperatorUtil.PAY_VIP) {
				String path = LauncherEnv.Path.GOT_ZIP_HEMES_PATH;
				if (infoBean.mTag == LOCKER_TYPE) {
					path = LauncherEnv.Path.GOLOCKER_ZIP_HEMES_PATH;
				}
				GoStoreOperatorUtil.downloadFileDirectly(GOLauncherApp.getContext(),
						infoBean.mName, path, infoBean.mResourceUrl,
						infoBean.mAppId, infoBean.mPkgName + ZipResources.ZIP_POSTFIX,
						new Class[] { AppDownloadListener.class }, false,
						AppsDetail.START_TYPE_DOWNLOAD_GO);
			} else {
				GoStoreOperatorUtil.startDownloadZip(mContext, infoBean);
			}
		}
	}

	private void downloadApkNoPay(Context context, AppDetailInfoBean infoBean) {
		if (context == null || infoBean == null) {
			throw new IllegalArgumentException("args can not be null");
		}
		if (infoBean.mDownloadType == DETAIL_DOWNLOAD_TYPE_FTP) {
			/**
			 *  FTP下载
			 */
			try {
				long id = infoBean.mAppId;
				if (mDownloadController == null) {
					return;
				}

				// update by zhoujun 如果是木瓜移动的数据，需要回调url
				DownloadUtil.sendCBackUrl(BoutiqueApp.BoutiqueAppInfo.CBACK_URL_FOR_DOWNLOAD,
						infoBean.cback, infoBean.cbacktype, infoBean.cbackurl);

				DownloadTask task = mDownloadController.getDownloadTaskById(id);
				if (task == null) {
					String pkgName = infoBean.mPkgName;
					String version = infoBean.mVersion;
					if (version != null && version.trim().startsWith("V")) {
						version = version.trim().substring(1);
					}
					String fileName = GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH + pkgName + "_"
							+ version + ".apk";;
					File apk = new File(fileName);
					if (apk.exists()) {
						apk.delete();
					}
					fileName = apk.getName();
					GoStoreOperatorUtil.downloadFileDirectly(context, infoBean.mName,
							infoBean.mDownloadUrl, id, pkgName,
							new Class[] { AppDownloadListener.class }, fileName,
							DownloadTask.ICON_TYPE_URL, infoBean.mIconUrl, mStartType);
				} else {
					mDownloadController.startDownload(id);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else if (infoBean.mDownloadType == DETAIL_DOWNLOAD_TYPE_MARKET
				|| infoBean.mDownloadType == DETAIL_DOWNLOAD_TYPE_WEB) {
			/**
			 *  跳转到电子市场或网页
			 */
			if (mStartType == AppsDetail.START_TYPE_WIDGET_APP) {
				/**
				 *  如果是widget进入详情的话则不需要再做统计,因为已经在widget进入详情的时候做过统计了,防止重复统计
				 */
				AppsDetail.jumpToDetail(mContext, infoBean, mCategoryID, mStartType, 0, false);
			} else {
				/**
				 *  貌似不会出现这种情况，不过也补上
				 */
				AppsDetail.jumpToDetail(mContext, infoBean, mCategoryID, mStartType, 0, true);
			}
		}

	}

	/**
	 * FTP下载时统计下载点击数
	 */
	private void countDownloadClick(AppDetailInfoBean infoBean) {
		// 如果是从应用管理界面进入
		if (mStartType == AppsDetail.START_TYPE_APPMANAGEMENT) {
			// 统计应用管理更新应用界面入口
			AppManagementStatisticsUtil.getInstance().saveCurrentUIEnter(mContext,
					AppManagementStatisticsUtil.UIENTRY_TYPE_DETAIL);
			// 统计应用管理更新应用点击量
			AppManagementStatisticsUtil.getInstance().saveUpdataClick(mContext, infoBean.mPkgName,
					infoBean.mAppId, 1);
		} else if (mStartType == AppsDetail.START_TYPE_APPRECOMMENDED
				|| mStartType == AppsDetail.START_TYPE_WIDGET_APP
				|| mStartType == AppsDetail.START_TYPE_APPFUNC_SEARCH
				|| mStartType == AppsDetail.START_TYPE_GO_SEARCH_WIDGET) {
			// 统计:先保存推荐界面入口
			AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(mContext,
					AppRecommendedStatisticsUtil.UIENTRY_TYPE_DETAIL);
			// 统计:推荐:保存点击下载统计
			// 先判断包类型
			int[] payType = infoBean.mPayType;
			if (GoStoreOperatorUtil.isNoPay(payType)) {
				// 如果是一般包
				AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(mContext,
						infoBean.mPkgName, infoBean.mAppId, mCategoryID, 1, 0);
			} else {
				// 如果是收费包
				AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(mContext,
						infoBean.mPkgName, infoBean.mAppId, mCategoryID, 1, 1);
			}
		}
	}

	/**
	 * FTP下载时统计更新点击数
	 */
	private void countUpdateClick(AppDetailInfoBean infoBean) {
		// 如果是从应用管理界面进入
		if (mStartType == AppsDetail.START_TYPE_APPMANAGEMENT) {
			// 统计应用管理更新应用界面入口
			AppManagementStatisticsUtil.getInstance().saveCurrentUIEnter(mContext,
					AppManagementStatisticsUtil.UIENTRY_TYPE_DETAIL);
			// 统计应用管理更新应用点击量
			AppManagementStatisticsUtil.getInstance().saveUpdataClick(mContext, infoBean.mPkgName,
					infoBean.mAppId, 1);
		} else if (mStartType == AppsDetail.START_TYPE_APPRECOMMENDED
				|| mStartType == AppsDetail.START_TYPE_WIDGET_APP
				|| mStartType == AppsDetail.START_TYPE_GO_SEARCH_WIDGET
				|| mStartType == AppsDetail.START_TYPE_APPFUNC_SEARCH) {
			// 统计:先保存推荐界面入口
			AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(mContext,
					AppRecommendedStatisticsUtil.UIENTRY_TYPE_DETAIL);
			// 统计:推荐:保存点击更新统计
			AppRecommendedStatisticsUtil.getInstance().saveUpdataClick(mContext, infoBean.mPkgName,
					infoBean.mAppId, mCategoryID, 1);
		}
	}

	/**
	 * <br>功能简述:开始下载
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param id 当强下载项id
	 */
	private void restartDownload(Context context, String id) {
		try {
			if (mDownloadController == null) {
				return;
			}
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
	 * @param id 当前下载项id
	 */
	private void stopDownload(Context context, String id) {
		try {
			if (mDownloadController == null) {
				return;
			}
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
	 * @param id 当前下载项id
	 */
	private void cancelDownload(Context contex, String id) {
		try {
			if (mDownloadController == null) {
				return;
			}
			mDownloadController.removeDownloadTaskById(Long.parseLong(id));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void showSecurityDialog(Context context, final AppDetailInfoBean infoBean) {
		if (context == null || infoBean == null || infoBean.mSecurityInfo == null) {
			return;
		}

		SecurityInfo info = infoBean.mSecurityInfo;

		LayoutInflater inflater = LayoutInflater.from(context);
		if (inflater == null) {
			return;
		}

		final Dialog dialog = new Dialog(context, R.style.AppGameSettingDialog);
		dialog.setContentView(R.layout.security_result_dialog);

		String localPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;

		// Title
		ImageView titleIcon = (ImageView) dialog
				.findViewById(R.id.security_result_dialog_title_icon);
		if (!TextUtils.isEmpty(info.mIcon)) {
			setIcon(titleIcon, info.mIcon, localPath, String.valueOf(info.mIcon.hashCode()), true);
		}

		TextView titleName = (TextView) dialog
				.findViewById(R.id.security_result_dialog_title_content);
		titleName.setText(info.mName);

		// 验证结果
		ImageView resultIv = (ImageView) dialog
				.findViewById(R.id.security_result_dialog_header_icon);
		if (!TextUtils.isEmpty(info.mPic)) {
			setIcon(resultIv, info.mPic, localPath, String.valueOf(info.mPic.hashCode()), true);
		}

		// appIcon
		ImageView appIcon = (ImageView) dialog
				.findViewById(R.id.security_result_dialog_header_appicon);
		if (!TextUtils.isEmpty(infoBean.mIconUrl)) {
			setIcon(appIcon, infoBean.mIconUrl, localPath,
					String.valueOf(infoBean.mIconUrl.hashCode()), true);
		}

		// appName
		((TextView) dialog.findViewById(R.id.security_result_dialog_header_apptitle))
				.setText(infoBean.mName);

		// 认证结果
		TextView appStatus = (TextView) dialog
				.findViewById(R.id.security_result_dialog_header_appstatus);
		appStatus.setText(info.mResultMsg);
		appStatus.setTextColor(mContext.getResources().getColor(R.color.app_detail_security_info));

		LinearLayout listLayout = (LinearLayout) dialog
				.findViewById(R.id.security_result_dialog_container);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		// 分割线
		ImageView headLine = new ImageView(context);
		headLine.setBackgroundResource(R.drawable.allfunc_allapp_menu_line);
		listLayout.addView(headLine, params);
		ArrayList<ThirdSecurityItem> thirdSecurityList = infoBean.mSecurityInfo.mThirdSecurityList;
		if (thirdSecurityList == null) {
			return;
		}
		for (ThirdSecurityItem thirdItem : thirdSecurityList) {
			if (thirdItem == null) {
				continue;
			}
			RelativeLayout itemLayout = (RelativeLayout) inflater.inflate(
					R.layout.layout_app_management_detail_security_list_dialog, null);

			if (thirdItem.mThirdIconUrl != null) {
				ImageView icon = (ImageView) itemLayout
						.findViewById(R.id.app_detail_security_list_dialog_icon);
				if (!TextUtils.isEmpty(thirdItem.mThirdIconUrl)) {
					setIcon(icon, thirdItem.mThirdIconUrl, localPath,
							String.valueOf(thirdItem.mThirdIconUrl.hashCode()), true);
				}
			}

			if (thirdItem.mThirdName != null) {
				TextView name = (TextView) itemLayout
						.findViewById(R.id.app_detail_security_list_dialog_name);
				name.setText(thirdItem.mThirdName);
			}

			TextView score = (TextView) itemLayout
					.findViewById(R.id.app_detail_security_list_dialog_score);
			score.setText(thirdItem.mThirdResultMsg);
			score.setTextColor(mContext.getResources().getColor(R.color.app_detail_security_info));

			listLayout.addView(itemLayout, params);

			ImageView line = new ImageView(context);
			line.setBackgroundResource(R.drawable.allfunc_allapp_menu_line);

			listLayout.addView(line, params);
		}

		// 只显示单个确定按钮
		((LinearLayout) dialog.findViewById(R.id.security_result_dialog_bottom_view1))
				.setVisibility(View.GONE);
		((LinearLayout) dialog.findViewById(R.id.security_result_dialog_bottom_view2))
				.setVisibility(View.VISIBLE);
		Button btnOk = (Button) dialog.findViewById(R.id.security_result_dialog_button_ok);
		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void setIcon(final ImageView imageView, String imgUrl, String imgPath, String imgName,
			boolean setDefaultIcon) {
		if (mImgManager == null) {
			mImgManager = AsyncImageManager.getInstance();
		}
		imageView.setTag(imgUrl);
		// TODO:XIEDEZHI 这里能不能不要每次load图片都生成一个回调对象？
		Bitmap bm = mImgManager.loadImage(imgPath, imgName, imgUrl, true, false,
				AppGameDrawUtils.getInstance().mMaskIconOperator, new AsyncImageLoadedCallBack() {
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

	/**
	 * 根据包名启动程序
	 * 
	 * @param context
	 * @param pkgName
	 * @return
	 */
	private boolean startApp(Context context, String pkgName) {
		if (context == null) {
			throw new IllegalArgumentException("context can not be null");
		}
		if (pkgName == null) {
			return false;
		}
		PackageManager pm = getPackageManager();
		if (pm == null) {
			return false;
		}
		// 需要做这样的判断，否则用户删除该包之后，下面的startActivity会产生异常，导致程序挂掉
		Intent intent = pm.getLaunchIntentForPackage(pkgName);
		if (intent == null) {
			return false;
		}
		try {
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void onSendFinishBocast() {
		Intent intent = new Intent();
		intent.setAction(ICustomAction.ACTION_ACTIVITY_FINISH);
		intent.setData(Uri.parse("package://"));
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * <br>功能简述:网络提示对话框
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void showNetWorkErrorDialog(int type) {
		DialogConfirm dialog = new DialogConfirm(this);
		dialog.show();
		dialog.setTitle(R.string.appgame_network_error_title);
		if (type == NETWORK_NO_CONNECTION) {
			dialog.setMessage(R.string.appgame_network_error_no_connection);
		} else {
			dialog.setMessage(R.string.appgame_network_error_connect_timeout);
		}
		dialog.setPositiveButton(R.string.appgame_network_error_setting,
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							mContext.startActivity(new Intent(
									android.provider.Settings.ACTION_SETTINGS));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
		dialog.setNegativeButton(R.string.appgame_network_error_back,
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						AppsThemeDetailActivity.this.finish();
					}
				});
	}
	
	private void initParameInfoBean(Object infoBean) {
		if (infoBean == null) {
			return;
		}
		mParameInfoBean = new AppDetailInfoBean();
		if (infoBean instanceof AppBean) {
			AppBean info = (AppBean) infoBean;
			mParameInfoBean.mName = info.mAppName;
			mParameInfoBean.mPkgName = info.mPkgName;
			mParameInfoBean.mAppId = info.mAppId;
			mParameInfoBean.mAtype = null;
			mParameInfoBean.mIconUrl = null;
			mParameInfoBean.mVersion = info.mVersionName;
			mParameInfoBean.mVersionCode = null; // 无versionCode，默认设为null
			mParameInfoBean.mSize = info.mAppSize;
			mParameInfoBean.mUpdateTime = info.mUpdateTime;
			mParameInfoBean.mDownloadUrl = null;
			mParameInfoBean.mUpdateLog = null;
			mParameInfoBean.mDownloadCount = null;
			mParameInfoBean.mDetail = null;
			mParameInfoBean.mDeveloper = null;
			mParameInfoBean.mIsFree = true;
			mParameInfoBean.mPrice = null;
			mParameInfoBean.mRemdmsg = null;
		} else if (infoBean instanceof BoutiqueApp) {
			BoutiqueApp info = (BoutiqueApp) infoBean;
			mParameInfoBean.mName = info.info.name;
			mParameInfoBean.mPkgName = info.info.packname;
			mParameInfoBean.mAppId = Integer.valueOf(info.info.appid);
			mParameInfoBean.mAtype = info.info.typeinfo;
			mParameInfoBean.mIconUrl = info.info.icon;
			mParameInfoBean.mVersion = info.info.version;
			mParameInfoBean.mVersionCode = info.info.versioncode;
			mParameInfoBean.mSize = info.info.size;
			mParameInfoBean.mUpdateTime = null;
			mParameInfoBean.mDownloadUrl = info.info.downloadurl;
			mParameInfoBean.mUpdateLog = null;
			mParameInfoBean.mDownloadCount = info.info.dlcs; // 下载量字符串
			mParameInfoBean.mDetail = null;
			mParameInfoBean.mDeveloper = info.info.developer;
			mParameInfoBean.mGrade = info.info.grade;
			if (info.info.isfree == 1) {
				mParameInfoBean.mIsFree = false;
			} else {
				mParameInfoBean.mIsFree = true;
			}
			mParameInfoBean.mDownloadType = info.info.downloadtype;
			mParameInfoBean.mPrice = info.info.price;
			mParameInfoBean.mRemdmsg = info.info.summary; // 服务器列表信息中summary存放的实际是编辑推荐语
		} 
	}
}
