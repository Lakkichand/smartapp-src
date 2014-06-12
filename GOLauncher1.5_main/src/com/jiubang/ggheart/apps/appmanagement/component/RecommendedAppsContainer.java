package com.jiubang.ggheart.apps.appmanagement.component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.appmanagement.bean.RecommendedApp;
import com.jiubang.ggheart.apps.appmanagement.bean.RecommendedAppCategory;
import com.jiubang.ggheart.apps.appmanagement.help.NetworkTipsTool;
import com.jiubang.ggheart.apps.appmanagement.help.RecommAppDownload;
import com.jiubang.ggheart.apps.appmanagement.help.RecommAppsUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 应用推荐处理类
 * 
 * @author zhoujun
 * 
 */
public class RecommendedAppsContainer extends LinearLayout
		implements
			IModeChangeListener,
			IMessageHandler,
			ICleanable {
	private static final String TAG = "RecommendedAppsContainer";
	private NetworkTipsTool mNetworkTipsTool;
	// private ImageView refreshImage;
	private DeskButton mRefreshButton;
	private RecommendedAppListView mRecommAppListView;
	private RecommendedAppsAdapter mRecommAppAdapter;
	private Context mContext;
	private ArrayList<RecommendedAppCategory> mRecommAppCtgList;
	private boolean mStopThread = false;
	/**
	 * 加载图片的操作是否完成
	 */
	private boolean mLoadIconEnd = true;
	/**
	 * 所有可更新的应用程序
	 */
	private ArrayList<AppBean> mUpdateListBeans;

	public RecommendedAppsContainer(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public RecommendedAppsContainer(Context context, AttributeSet attr) {
		super(context, attr);
		mContext = context;
		init();
	}

	private void init() {
		this.setOrientation(LinearLayout.VERTICAL);
		GoLauncher.registMsgHandler(this);
	}

	@Override
	protected void onFinishInflate() {
		initView();
	}

	private void initView() {
		ViewGroup tipsView = (ViewGroup) findViewById(R.id.error_tips_view);
		mNetworkTipsTool = new NetworkTipsTool(tipsView);

		mRefreshButton = (DeskButton) findViewById(R.id.recomm_app_refresh_button);
		mRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mRefreshButton.getVisibility() == View.VISIBLE) {
					mRefreshButton.setVisibility(View.GONE);
				}
				sendRequest();
			}
		});

		mRecommAppListView = (RecommendedAppListView) findViewById(R.id.recomm_app_list_view);
		mRecommAppAdapter = new RecommendedAppsAdapter(this.getContext());
		mRecommAppListView.setAdapter(mRecommAppAdapter);
		mRecommAppListView.setGroupIndicator(null);
		// mRecommAppListView.setHandler(handler);
		// mRecommAppListView.setDivider(null);
		mRecommAppListView.setSelector(R.drawable.recomm_app_list_item_selector);

		mRecommAppListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
					int childPosition, long id) {
				if (mRecommAppAdapter != null) {
					RecommendedApp recommApp = (RecommendedApp) mRecommAppAdapter.getChild(
							groupPosition, childPosition);
					if (recommApp != null) {
						// 统计:先保存推荐界面入口 为1
						AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(getContext(),
								AppRecommendedStatisticsUtil.UIENTRY_TYPE_LIST);

						// 统计：应用推荐--应用详细点击次数
						AppRecommendedStatisticsUtil.getInstance().saveDetailsClick(mContext,
								recommApp.mPackname, Integer.valueOf(recommApp.mAppId), 1);

						int type = recommApp.mDetailtype;
						if (type == RecommendedApp.DOWNLOAD_TYPE_FTP) {
							// 统计：国内---不保存点击下载(times = 0)
							// AppRecommendedStatisticsUtil.getInstance()
							// .saveDownloadClick(getContext(),recommApp.mPackname,
							// Integer.valueOf(recommApp.mAppId),
							// recommApp.mTypeId, 0);

							// 统计：国内---更新主表中详细点击次数
							AppRecommendedStatisticsUtil.getInstance().saveDetailsClick2MainTable(
									getContext(), recommApp.mPackname,
									Integer.valueOf(recommApp.mAppId), recommApp.mTypeId);

							// 推荐应用中，状态为等待下载和正在下载的应用，在详情里面，更新按钮不可点击
							int downloadStatus = 0;
							if (recommApp.getStatus() == RecommendedApp.STATUS_WAITING_DOWNLOAD
									|| recommApp.getStatus() == RecommendedApp.STATUS_DOWNLOADING) {
								downloadStatus = 1;
							}
							// 跳转到Go精品
//							GoStoreOperatorUtil.gotoStoreDetailDirectly(getContext(),
//									Integer.parseInt(recommApp.mAppId), downloadStatus,
//									ItemDetailActivity.START_TYPE_APPRECOMMENDED, recommApp.mTypeId);
						} else {
							String detailUrl = recommApp.mDetailurl;
							if (detailUrl != null && !"".equals(detailUrl)) {
								detailUrl = detailUrl.trim()
										+ LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
								if (type == RecommendedApp.DOWNLOAD_TYPE_MARKET) {
									// 跳转到电子市场页面
									GoStoreOperatorUtil.gotoMarket(getContext(), detailUrl);
								} else if (type == RecommendedApp.DOWNLOAD_TYPE_WEB) {
									// 3：电子市场web版页面
									GoStoreOperatorUtil.gotoBrowser(getContext(), detailUrl);
								}

								// 统计
								if (recommApp.getStatus() == RecommendedApp.STATUS_FOR_NOT_INSTALL) {
									// 统计：国外---保存点击下载统计(记作点击过更新)
									AppRecommendedStatisticsUtil.getInstance()
											.saveDownloadClick(getContext(), recommApp.mPackname,
													Integer.valueOf(recommApp.mAppId),
													recommApp.mTypeId, 1);
								} else {
									// 统计：国外---保存点击更新统计(记作点击过更新)
									AppRecommendedStatisticsUtil.getInstance()
											.saveUpdataClick(getContext(), recommApp.mPackname,
													Integer.valueOf(recommApp.mAppId),
													recommApp.mTypeId, 1);
								}
							}
						}
						// 统计:再保存推荐界面入口 为 2
						AppRecommendedStatisticsUtil.getInstance().saveCurrentUIEnter(getContext(),
								AppRecommendedStatisticsUtil.UIENTRY_TYPE_DETAIL);
					}
				}
				return false;
			}
		});

		// sdcard不存在时，加载提示页面
		if (!Machine.isSDCardExist()) {
			showExceptionView(false);
		} else if (!Machine.isNetworkOK(getContext())) {
			showExceptionView(true);
		} else {
			sendRequest();
		}
	}

	private void sendRequest() {
		mNetworkTipsTool.showProgress();

		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				// long l1 = System.currentTimeMillis();
				// mRecommAppCtgList = RecommAppDownload.getInstance(mContext)
				// .requestData();
				mRecommAppCtgList = new RecommAppDownload(mContext.getApplicationContext())
						.requestData();
				// add by zhaojunjie 判空（没网络，点retry按钮，报nullpoint错误）
				if (mRecommAppCtgList == null) {
					return;
				}
				// end add
				// Log.d(TAG,
				// "recomm load data time :"
				// + (System.currentTimeMillis() - l1));
				mHandler.sendEmptyMessage(0);
				loadAppIcon();

			}
		}.start();
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int what = msg.what;
			switch (what) {
				case 0 :
					mNetworkTipsTool.dismissProgress();
					if (mRecommAppAdapter != null && mRecommAppCtgList != null
							&& mRecommAppListView != null) {
						if (mRecommAppListView.getVisibility() == View.GONE) {
							mRecommAppListView.setVisibility(View.VISIBLE);
						}
						if (mRefreshButton.getVisibility() == View.VISIBLE) {
							mRefreshButton.setVisibility(View.GONE);
						}
						mRecommAppAdapter.refreshData(mRecommAppCtgList);
						mRecommAppAdapter.notifyDataSetChanged();
					} else {
						showExceptionView(true);
					}
					break;
				case 1 :
					// Object[] objs = (Object[]) msg.obj;
					// if (objs != null) {
					// String packageName = (String) objs[0];
					//
					// RecommendedApp recommApp =
					// getRecommAppByPackageName(packageName);
					// if (recommApp != null) {
					// BitmapDrawable drawable = (BitmapDrawable) objs[1];
					// recommApp.mIcon = drawable;
					// }
					// }
					if (mRecommAppAdapter != null) {
						mRecommAppAdapter.notifyDataSetChanged();
					}
					break;
				default :
					break;
			}
			super.handleMessage(msg);
		}
	};

	public void refreshData(String packageName, boolean isInstall) {
		if (mRecommAppCtgList != null && !mRecommAppCtgList.isEmpty()) {
			RecommendedApp recommApp = getRecommAppByPackageName(packageName);
			if (recommApp != null) {
				if (isInstall) {
					recommApp.setStatus(RecommendedApp.STATUS_FOR_INSTALL);
				} else {
					recommApp.setStatus(RecommendedApp.STATUS_FOR_NOT_INSTALL);
				}

				if (mRecommAppAdapter != null) {
					mRecommAppAdapter.notifyDataSetChanged();
				}
			}
		}

	}

	//	private void statisticsAndUpdateState(ArrayList<RecommendedAppCategory> recommAppCtgList) {
	//		if (recommAppCtgList != null && recommAppCtgList.size() > 0) {
	//			for (RecommendedAppCategory recomAppCtg : recommAppCtgList) {
	//				ArrayList<RecommendedApp> recommAppList = recomAppCtg.mRecommendedAppList;
	//				int index = 1; // 应用在列表中的位置（推荐位置）
	//				for (RecommendedApp recommApp : recommAppList) {
	//					if (mStopThread) {
	//						return;
	//					}
	//					setAppStatus(recommApp);
	//					// 统计：应用推荐--下发次数，推荐ID，推荐位置
	//					AppRecommendedStatisticsUtil.getInstance().saveIssued(mContext,
	//							recommApp.mPackname, recomAppCtg.mTypeId, index);
	//
	//					// 统计：应用推荐--更新主表中详细点击次数
	//					if (recommApp.mAppId.equals("")) {
	//						recommApp.mAppId = "0";
	//					}
	//					AppRecommendedStatisticsUtil.getInstance().saveDetailsClick2MainTable(
	//							getContext(), recommApp.mPackname, Integer.valueOf(recommApp.mAppId),
	//							recommApp.mTypeId);
	//					index++;
	//
	//					setUpdateState(recommApp);
	//
	//					//					ApplicationManager.getInstance(getContext()).setDownloadState(
	//					//							Long.valueOf(recommApp.mAppId));
	//				}
	//			}
	//			//			setUpdateStatus();
	//		}
	//	}

	//	private void setUpdateState(RecommendedApp recommApp) {
	//		if (mUpdateListBeans != null && !mUpdateListBeans.isEmpty()) {
	//			synchronized (mUpdateListBeans) {
	//				for (AppBean appBean : mUpdateListBeans) {
	//					if (recommApp != null) {
	//						setDownloadUrl(appBean, recommApp);
	//					}
	//				}
	//			}
	//		}
	//	}

	/**
	 * 加载推荐应用icon
	 */
	private void loadAppIcon() {
		mLoadIconEnd = false;
		if (mRecommAppCtgList != null && !mRecommAppCtgList.isEmpty()) {
			synchronized (mRecommAppCtgList) {
				if (mRecommAppCtgList != null) {
					//					DownloadManager downloadManager = DownloadManager.getInstance(mContext
					//							.getApplicationContext());
					//					IDownloadService downloadManager = GOLauncherApp.getApplication().getDownloadController();
					//					String iconUrl = null;
					for (RecommendedAppCategory recomAppCtg : mRecommAppCtgList) {
						ArrayList<RecommendedApp> recommAppList = recomAppCtg.mRecommendedAppList;
						int index = 1; // 应用在列表中的位置（推荐位置）

						if (recommAppList != null && !recommAppList.isEmpty()) {
							for (RecommendedApp recommApp : recommAppList) {
								if (mStopThread) {
									return;
								}
								//								iconUrl = recommApp.mIconUrl;
								//								String fileName = RecommAppsUtils.getSimpleName(iconUrl);
								//								String localPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH
								//										+ fileName;
								//								File file = new File(localPath);
								//								recommApp.mIconLocalPath = localPath;
								//								if (!file.exists()) {
								//									// Log.d("recommendedAppsContail",
								//									// "download iconUrl:"
								//									// + iconUrl);
								////									downloadAppIcon(downloadManager, iconUrl, localPath,
								////											System.currentTimeMillis(), fileName,
								////											recommApp.mPackname);
								//								}
								setAppStatus(recommApp);

								// 统计：应用推荐--下发次数，推荐ID，推荐位置
								AppRecommendedStatisticsUtil.getInstance().saveIssued(mContext,
										recommApp.mPackname, recomAppCtg.mTypeId, index);
								// 统计：应用推荐--不当作一次更新点击(times = 0)，但要保存在主表
								// AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(mContext,
								// recommApp.mPackname,
								// Integer.valueOf(recommApp.mAppId),
								// recomAppCtg.mTypeId,
								// 0);

								// 统计：应用推荐--更新主表中详细点击次数
								if (recommApp.mAppId.equals("")) {
									recommApp.mAppId = "0";
								}
								AppRecommendedStatisticsUtil.getInstance()
										.saveDetailsClick2MainTable(getContext(),
												recommApp.mPackname,
												Integer.valueOf(recommApp.mAppId),
												recommApp.mTypeId);

								index++;
							}
						}
					}
					setUpdateStatus();
				}
			}
		}
		mLoadIconEnd = true;
	}

	/**
	 * 本地图片不存在时，从服务器下载
	 * 
	 * @param downloadManager
	 * @param downloadUrl
	 * @param saveFilePath
	 * @param id
	 * @param downloadName
	 * @param packageName
	 */
	//	private void downloadAppIcon(DownloadManager downloadManager, String downloadUrl,
	//			String saveFilePath, long id, String downloadName, String packageName) {
	//
	//		if (downloadUrl == null || "".equals(downloadUrl.trim()) || saveFilePath == null
	//				|| "".equals(saveFilePath)) {
	//			return;
	//		}
	//
	//		DownloadTask downloadTask = new DownloadTask(id, downloadUrl, downloadName, 0, 0,
	//				saveFilePath, packageName);
	//		// 设置标识一次下载的ID，各次下载均不相同
	//		downloadTask.setDownloadId(System.currentTimeMillis());
	//		downloadTask.addDownloadListener(new RecommAppDownloadListenter(mContext
	//				.getApplicationContext(), mHandler));
	//
	//		downloadManager.startDownload(downloadTask);
	//	}

//	private void downloadAppIcon(IDownloadService downloadManager, String downloadUrl,
//			String saveFilePath, long id, String downloadName, String packageName) {
//
//		if (downloadUrl == null || "".equals(downloadUrl.trim()) || saveFilePath == null
//				|| "".equals(saveFilePath)) {
//			return;
//		}
//
//		DownloadTask downloadTask = new DownloadTask(id, downloadUrl, downloadName, 0, 0,
//				saveFilePath, packageName);
//		// 设置标识一次下载的ID，各次下载均不相同
//		try {
//			if (downloadManager != null) {
//				long taskId = downloadManager.addDownloadTask(downloadTask);
//				if (taskId != -1) {
//					downloadManager.addDownloadTaskListener(taskId, new RecommAppDownloadListenter(
//							mContext.getApplicationContext(), mHandler));
//					downloadManager.startDownload(taskId);
//				}
//			}
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		//		downloadTask.setDownloadId(System.currentTimeMillis());
//		//		downloadTask.addDownloadListener(new RecommAppDownloadListenter(mContext
//		//				.getApplicationContext(), mHandler));
//		//
//		//		downloadManager.startDownload(downloadTask);
//	}

	/**
	 * sd不可用，或网络不可用时，显示异常信息
	 */
	public void showExceptionView(boolean isNetworkError) {
		if (mRecommAppListView != null) {
			mRecommAppListView.setVisibility(View.GONE);
		}
		if (mNetworkTipsTool != null) {
			mNetworkTipsTool.showErrorTip(isNetworkError);
		}
		if (isNetworkError) {
			if (mRefreshButton != null) {
				if (mRefreshButton.getVisibility() == View.GONE) {
					mRefreshButton.setVisibility(View.VISIBLE);
				}
				mRefreshButton.setText(mContext.getString(R.string.apps_recomm_network_refresh));
			}
		} else {
			if (mRefreshButton.getVisibility() == View.VISIBLE) {
				mRefreshButton.setVisibility(View.GONE);
			}
		}

	}

	/**
	 * 显示可以更新的列表信息
	 */
	public void showUpdateView() {
		if (Machine.isNetworkOK(getContext())) {
			if (mNetworkTipsTool != null) {
				mNetworkTipsTool.showNothing();
			}

			if (mRecommAppListView != null) {
				if (mRecommAppListView.getVisibility() == View.GONE) {
					mRecommAppListView.setVisibility(View.VISIBLE);
					if (mRecommAppCtgList == null || mRecommAppCtgList.isEmpty()) {
						sendRequest();
					} else {
						// mRecommAppAdapter.refreshData(mRecommAppCtgList);
						mRecommAppAdapter.notifyDataSetChanged();
					}

				}
			}
		} else {
			if (mNetworkTipsTool != null) {
				mNetworkTipsTool.showErrorTip(true);
			}
			if (mRefreshButton != null) {
				if (mRefreshButton.getVisibility() == View.GONE) {
					mRefreshButton.setVisibility(View.VISIBLE);
				}
				mRefreshButton.setText(mContext.getString(R.string.apps_recomm_network_refresh));
			}
		}

	}

	@Override
	public void onModleChanged(int action, int state, Object value) {

	}

	@Override
	public int getId() {
		return IDiyFrameIds.APPS_MANAGEMENT_RECOMMENDED_APP_FRAME;
	}

	@Override
	public boolean handleMessage(Object who, int type, final int msgId, final int param,
			final Object object, final List objects) {
		switch (msgId) {
		//			case IDiyMsgIds.APPS_MANAGEMENT_WAIT_FOR_DOWNLOAD :
		//			case IDiyMsgIds.APPS_MANAGEMENT_START_DOWNLOAD :
		//			case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOADING :
		//			case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_COMPLETED :
		//			case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_FAILED :
		//			case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_CANCELED :
		//				updateItemStatus(msgId, param, object, objects);
		//				break;
			case IDiyMsgIds.APPS_MANAGEMENT_RECOMMENDED_APP :
				if (mRecommAppCtgList != null && !mRecommAppCtgList.isEmpty()) {
					mUpdateListBeans = (ArrayList<AppBean>) objects;
					if (mLoadIconEnd) {
						setUpdateStatus();
					}

				}

				break;
		}
		return true;
	}

	/**
	 * 设置更新跳转的url,应用推荐的更新url是从应用更新那里获取
	 * 
	 * @param appBean
	 * @param recommApp
	 */
	private void setDownloadUrl(AppBean appBean, RecommendedApp recommApp) {
		HashMap<Integer, String> urlHashMap = appBean.mUrlMap;
		if (urlHashMap != null && urlHashMap.size() > 0) {
			// 走ftp，跳转到精品详情页面
			String detailUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_HTTP_SERVER);
			if (detailUrl != null && !"".equals(detailUrl)) {
				// 应用更新中，状态为等待下载和正在下载的应用，在详情里面，更新按钮不可点击
				recommApp.mDownloadtype = RecommendedApp.DOWNLOAD_TYPE_FTP;
			} else {
				// 跳转到电子市场
				detailUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_GOOGLE_MARKET);
				if (detailUrl != null && !"".equals(detailUrl)) {
					detailUrl = detailUrl.trim() + LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;

					recommApp.mDownloadtype = RecommendedApp.DOWNLOAD_TYPE_MARKET;
				} else {
					// 跳转到web版电子市场
					detailUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_WEB_GOOGLE_MARKET);
					if (detailUrl == null || "".equals(detailUrl)) {
						// 跳转到其他地址
						detailUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_OTHER_ADDRESS);
					}
					if (detailUrl != null && !"".equals(detailUrl)) {
						detailUrl = detailUrl + LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
					}
					recommApp.mDownloadtype = RecommendedApp.DOWNLOAD_TYPE_WEB;
				}
			}
			recommApp.mDownloadurl = detailUrl;
			if (appBean.getStatus() == AppBean.STATUS_NORMAL) {
				recommApp.setStatus(RecommendedApp.STATUS_FOR_UPDATE);
			} else if (appBean.getStatus() == AppBean.STATUS_DOWNLOAD_COMPLETED) {
				recommApp.setStatus(RecommendedApp.STATUS_DOWNLOAD_COMPLETED);
			}
		}
	}

	//	public void updateItemStatus(int msgId, int id, Object obj, List objs) {
	//		if (mRecommAppAdapter == null) {
	//			return;
	//		}
	//		mRecommAppCtgList = mRecommAppAdapter.getRecommAppList();
	//		if (mRecommAppCtgList != null) {
	//			switch (msgId) {
	//				case IDiyMsgIds.APPS_MANAGEMENT_WAIT_FOR_DOWNLOAD :
	//					setAppDownloadStatus(String.valueOf(id),
	//							RecommendedApp.STATUS_WAITING_DOWNLOAD, 0, 0, null);
	//					break;
	//				case IDiyMsgIds.APPS_MANAGEMENT_START_DOWNLOAD :
	//					setAppDownloadStatus(String.valueOf(id), RecommendedApp.STATUS_DOWNLOADING, 0,
	//							0, null);
	//					break;
	//				case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOADING :
	//					if (objs != null && objs.size() == 2) {
	//						long downloadSize = (Long) objs.get(0);
	//						int percent = (Integer) objs.get(1);
	//						setAppDownloadStatus(String.valueOf(id), RecommendedApp.STATUS_DOWNLOADING,
	//								downloadSize, percent, null);
	//					}
	//					break;
	//
	//				case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_COMPLETED :
	//					String saveFilePath = (String) obj;
	//					long downloadSize = 0;
	//					int percent = 0;
	//					if (objs != null && objs.size() == 2) {
	//						downloadSize = (Long) objs.get(0);
	//						percent = (Integer) objs.get(1);
	//					}
	//					String packageName = setAppDownloadStatus(String.valueOf(id),
	//							RecommendedApp.STATUS_DOWNLOAD_COMPLETED, downloadSize, percent,
	//							saveFilePath);
	//					((AppsManagementActivity) mContext).addInstallApp(saveFilePath, packageName);
	//
	//					// ((AppsManagementActivity) mContext).addInstallApp(
	//					// saveFilePath, recommApp.mPackname);
	//					break;
	//				case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_CANCELED :
	//					setAppDownloadStatus(String.valueOf(id), RecommendedApp.STATUS_CANCELING, 0, 0,
	//							null);
	//					break;
	//				case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_FAILED :
	//					setAppDownloadStatus(String.valueOf(id), RecommendedApp.STATUS_DOWNLOAD_FAILED,
	//							0, 0, null);
	//					DeskToast.makeText(getContext(), R.string.apps_management_network_error,
	//							Toast.LENGTH_SHORT).show();
	//					break;
	//			}
	//			mRecommAppAdapter.notifyDataSetChanged();
	//		}
	//		// }
	//	}

	public void updateDownloadState(int downloadState, DownloadTask downloadTask) {
		if (mRecommAppAdapter == null) {
			return;
		}
		long id = downloadTask.getId();
		mRecommAppCtgList = mRecommAppAdapter.getRecommAppList();
		if (mRecommAppCtgList != null) {
			switch (downloadState) {
				case IDiyMsgIds.APPS_MANAGEMENT_WAIT_FOR_DOWNLOAD :
					setAppDownloadStatus(String.valueOf(id),
							RecommendedApp.STATUS_WAITING_DOWNLOAD, 0, 0, null);
					break;
				case IDiyMsgIds.APPS_MANAGEMENT_START_DOWNLOAD :
					setAppDownloadStatus(String.valueOf(id), RecommendedApp.STATUS_DOWNLOADING, 0,
							0, null);
					break;
				case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOADING :
					long downloadSize = downloadTask.getAlreadyDownloadSize();
					int percent = downloadTask.getAlreadyDownloadPercent();
					setAppDownloadStatus(String.valueOf(id), RecommendedApp.STATUS_DOWNLOADING,
							downloadSize, percent, null);
					break;

				case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_COMPLETED :
					String saveFilePath = downloadTask.getSaveFilePath();

					String packageName = setAppDownloadStatus(String.valueOf(id),
							RecommendedApp.STATUS_DOWNLOAD_COMPLETED,
							downloadTask.getAlreadyDownloadSize(),
							downloadTask.getAlreadyDownloadPercent(), saveFilePath);
					((AppsManagementActivity) mContext).addInstallApp(saveFilePath, packageName);

					// ((AppsManagementActivity) mContext).addInstallApp(
					// saveFilePath, recommApp.mPackname);
					break;
				case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_CANCELED :
					setAppDownloadStatus(String.valueOf(id), RecommendedApp.STATUS_CANCELING, 0, 0,
							null);
					break;
				case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_FAILED :
					setAppDownloadStatus(String.valueOf(id), RecommendedApp.STATUS_DOWNLOAD_FAILED,
							0, 0, null);
					DeskToast.makeText(getContext(), R.string.apps_management_network_error,
							Toast.LENGTH_SHORT).show();
					break;
			}
			mRecommAppAdapter.notifyDataSetChanged();
		}
		// }
	}

	private String setAppDownloadStatus(String appId, int status, long mAlreadyDownloadSize,
			int percent, String saveFilePath) {
		String packageName = null;
		if (mRecommAppCtgList != null) {
			for (RecommendedAppCategory recommAppCtg : mRecommAppCtgList) {
				ArrayList<RecommendedApp> recommAppList = recommAppCtg.mRecommendedAppList;
				if (recommAppList != null) {
					for (RecommendedApp recommApp : recommAppList) {
						if (recommApp.mAppId.equals(appId)) {
							if (status == RecommendedApp.STATUS_CANCELING) {
								if (RecommAppsUtils.isInstalled(getContext(), recommApp.mPackname,
										null)) {
									status = RecommendedApp.STATUS_FOR_UPDATE;
								} else {
									status = RecommendedApp.STATUS_FOR_NOT_INSTALL;
								}
								// setAppStatus(recommApp);
							}
							recommApp.setStatus(status);
							recommApp.mAlreadyDownloadSize = mAlreadyDownloadSize;
							recommApp.mPercent = percent;
							recommApp.mApkLocalPath = saveFilePath;
							packageName = recommApp.mPackname;
							break;
						}
					}
				}
			}
		}
		return packageName;
	}

	// private RecommendedApp getRecommAppById(String appId) {
	// Long l1 = System.currentTimeMillis();
	// if (mRecommAppCtgList != null) {
	// for (RecommendedAppCategory recommAppCtg : mRecommAppCtgList) {
	// ArrayList<RecommendedApp> recommAppList =
	// recommAppCtg.mRecommendedAppList;
	// if (recommAppList != null) {
	// for (RecommendedApp recommApp : recommAppList) {
	// if ((recommApp.mAppId+recommApp.mTypeId).equals(appId)) {
	// Log.d(TAG, appId+ " and  time is : "+ (System.currentTimeMillis()-l1));
	// return recommApp;
	// }
	// }
	// }
	// }
	// }
	// Log.d(TAG, " time is : "+ (System.currentTimeMillis()-l1));
	// return null;
	// }

	private RecommendedApp getRecommAppByPackageName(String packageName) {
		if (mRecommAppCtgList != null) {
			for (RecommendedAppCategory recommAppCtg : mRecommAppCtgList) {
				ArrayList<RecommendedApp> recommAppList = recommAppCtg.mRecommendedAppList;
				if (recommAppList != null) {
					for (RecommendedApp recommApp : recommAppList) {
						if (recommApp.mPackname.equals(packageName)) {
							return recommApp;
						}
					}
				}
			}
		}
		return null;
	}

	private void setUpdateStatus() {
		if (mUpdateListBeans != null && !mUpdateListBeans.isEmpty()) {
			synchronized (mUpdateListBeans) {
				for (AppBean appBean : mUpdateListBeans) {
					RecommendedApp recommApp = getRecommAppByPackageName(appBean.mPkgName);
					if (recommApp != null) {
						setDownloadUrl(appBean, recommApp);
					}
				}
				mUpdateListBeans = null;
			}
			mHandler.sendEmptyMessage(1);
		}
	}

	// /**
	// * 清空更新状态
	// */
	// private void resetUpdateStatus() {
	// if (mRecommAppCtgList != null) {
	// for (RecommendedAppCategory recommAppCtg : mRecommAppCtgList) {
	// ArrayList<RecommendedApp> recommAppList =
	// recommAppCtg.mRecommendedAppList;
	// if (recommAppList != null) {
	// for (RecommendedApp recommApp : recommAppList) {
	// if (recommApp.getStatus() == RecommendedApp.STATUS_FOR_UPDATE) {
	// recommApp.setStatus(RecommendedApp.STATUS_FOR_INSTALL);
	// }
	// }
	// }
	// }
	// }
	// }

	private void setAppStatus(RecommendedApp recommApp) {
		String packageName = recommApp.mPackname;
		String versionName = recommApp.mVersion;
		File downloadFile = new File(GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH + packageName
				+ "_" + versionName + ".apk");

		if (RecommAppsUtils.isInstalled(getContext(), packageName, versionName)) {
			if (downloadFile.exists()) {
				downloadFile.delete();
			}
			recommApp.setStatus(RecommendedApp.STATUS_FOR_INSTALL);
		} else {

			if (downloadFile.exists()) {
				// 已下载，未安装
				recommApp.setStatus(RecommendedApp.STATUS_DOWNLOAD_COMPLETED);
				recommApp.mApkLocalPath = downloadFile.getAbsolutePath();
			} else {
				if (RecommAppsUtils.isInstalled(getContext(), packageName, null)) {
					// if ("Varies with device".equals(versionName)) {
					recommApp.setStatus(RecommendedApp.STATUS_FOR_INSTALL);
					// } else {
					// recommApp.setStatus(RecommendedApp.STATUS_FOR_UPDATE);
					// }
				} else {
					recommApp.setStatus(RecommendedApp.STATUS_FOR_NOT_INSTALL);
				}

			}
		}
		// setStatus(recommApp.getStatus());
	}

	@Override
	public void cleanup() {
		cleanData();
		GoLauncher.unRegistMsgHandler(this);
	}

	private void cleanData() {
		if (mRecommAppAdapter != null) {
			mRecommAppAdapter.clean();
			mRecommAppAdapter = null;
		}

		if (mRecommAppListView != null) {
			int count = mRecommAppListView.getChildCount();
			if (count > 0) {
				RecommendedAppsUpdateListItem recommItem = null;
				for (int i = 0; i < count; i++) {
					View view = mRecommAppListView.getChildAt(i);
					if (view instanceof RecommendedAppsUpdateListItem) {
						recommItem = (RecommendedAppsUpdateListItem) view;
						recommItem.destory();
					}

				}
			}
			mRecommAppListView = null;
		}

		mStopThread = true;

		if (mRecommAppCtgList != null) {
			synchronized (mRecommAppCtgList) {
				for (RecommendedAppCategory recommAppCtg : mRecommAppCtgList) {
					ArrayList<RecommendedApp> recommAppList = recommAppCtg.mRecommendedAppList;
					if (recommAppList != null) {
						recommAppList.clear();
						recommAppList = null;
					}
				}
				mRecommAppCtgList.clear();
				mRecommAppCtgList = null;
			}

		}
		// 释放资源反注册mFinishButton，icon里包含的TextFont
		if (mRefreshButton != null) {
			mRefreshButton.selfDestruct();
			mRefreshButton = null;
		}
	}
}
