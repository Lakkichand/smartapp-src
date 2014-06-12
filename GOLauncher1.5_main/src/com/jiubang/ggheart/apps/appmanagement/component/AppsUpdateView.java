package com.jiubang.ggheart.apps.appmanagement.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.appmanagement.bean.NoPromptUpdateInfo;
import com.jiubang.ggheart.apps.appmanagement.controler.ApplicationManager;
import com.jiubang.ggheart.apps.appmanagement.controler.AppsManageViewController;
import com.jiubang.ggheart.apps.appmanagement.help.NetworkTipsTool;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.components.DeskBuilder;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述: 应用更新列表界面
 * <br>功能详细描述:
 * 
 * @author  zhoujun
 * @date  [2012-10-8]
 */
public class AppsUpdateView extends ListView {

	// private AppsBean mAppsBean = null;
	/**
	 * 所有可更新的应用
	 */
	private ArrayList<AppBean> mAppBeanList;

	/**
	 * 忽略更新的应用
	 */
	private ArrayList<AppBean> mNoUpdateAppBeanList;
	private AppsUpdateViewAdapter mAppsManageViewAdapter = null;
	private NetworkTipsTool mNetworkTipsTool;

	// private Context mContext;

	public AppsUpdateView(Context context) {
		super(context);
		// mContext = context;
		init();
	}

	public AppsUpdateView(Context context, AttributeSet attr) {
		super(context, attr);
		// mContext = context;
		init();

	}

	private void init() {
		mAppBeanList = new ArrayList<AppBean>();
		initView();
		this.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mAppsManageViewAdapter != null) {
					AppBean appBean = (AppBean) mAppsManageViewAdapter.getItem(position);
					if (appBean != null) {
						// 跳转到详情页
						doOnItemClick(appBean);
					}
				}
			}
		});
		this.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (view instanceof AppsUpdateInfoListItem) {
					AppBean appBean = (AppBean) view.getTag();
					if (appBean.getStatus() == AppBean.STATUS_NORMAL
							|| appBean.getStatus() == AppBean.STATUS_DOWNLOAD_COMPLETED) {

						// 显示弹出框，选择不提示更新
						showNoUpdateDialog(appBean, position);
					}
				}
				return false;
			}
		});
	}

	/**
	 * 初始化列表的方法
	 */
	private void initView() {
		mAppsManageViewAdapter = new AppsUpdateViewAdapter(getContext(), getAppInfoData());
		this.setSelector(R.drawable.recomm_app_list_item_selector);
		setAdapter(mAppsManageViewAdapter);
		// this.setDivider(null);

	}

	private void doOnItemClick(AppBean appBean) {
		// 统计：应用更新：先保存UI入口：1
		AppManagementStatisticsUtil.getInstance().saveCurrentUIEnter(getContext(),
				AppManagementStatisticsUtil.UIENTRY_TYPE_LIST);

		// 统计详细点击
		AppManagementStatisticsUtil.getInstance().saveDetailsClick(getContext(), appBean.mPkgName,
				appBean.mAppId, 1);

		HashMap<Integer, String> urlHashMap = appBean.mUrlMap;
		if (urlHashMap != null && urlHashMap.size() > 0) {
			// 走ftp，跳转到精品详情页面
			String detailUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_DETAIL_ADDRESS);
			if (detailUrl != null && !"".equals(detailUrl)) {
				// 应用更新中，状态为等待下载和正在下载的应用，在详情里面，更新按钮不可点击
				int downloadStatus = 0;
				if (appBean.getStatus() == AppBean.STATUS_WAITING_DOWNLOAD
						|| appBean.getStatus() == AppBean.STATUS_DOWNLOADING) {
					downloadStatus = 1;
				}
//				GoStoreOperatorUtil.gotoStoreDetailDirectly(getContext(), appBean.mAppId,
//						downloadStatus, ItemDetailActivity.START_TYPE_APPMANAGEMENT, null);

				// 统计：国内---不保存点击更新(times = 0)
				AppManagementStatisticsUtil.getInstance().saveUpdataClick(getContext(),
						appBean.mPkgName, appBean.mAppId, 0);

			} else {
				// 跳转到电子市场
				detailUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_GOOGLE_MARKET);
				if (detailUrl != null && !"".equals(detailUrl)) {
					detailUrl = detailUrl.trim() + LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
					GoStoreOperatorUtil.gotoMarket(getContext(), detailUrl);
				} else {
					// 跳转到web版电子市场
					detailUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_WEB_GOOGLE_MARKET);
					if (detailUrl == null || "".equals(detailUrl)) {
						// 跳转到其他地址
						detailUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_OTHER_ADDRESS);
					}
					if (detailUrl != null && !"".equals(detailUrl)) {
						detailUrl = detailUrl + LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
						GoStoreOperatorUtil.gotoBrowser(getContext(), detailUrl);
					} else {
						// 跳转失败
						Toast.makeText(getContext(), R.string.themestore_url_fail,
								Toast.LENGTH_LONG).show();
					}
				}

				// 统计：国外---保存点击更新统计(记作点击过更新)
				AppManagementStatisticsUtil.getInstance().saveUpdataClick(getContext(),
						appBean.mPkgName, appBean.mAppId, 1);
			}

			// 统计：应用更新：再保存UI入口：2
			AppManagementStatisticsUtil.getInstance().saveCurrentUIEnter(getContext(),
					AppManagementStatisticsUtil.UIENTRY_TYPE_DETAIL);

		}
	}

	/**
	 * 获取数据的方法
	 * 
	 * @return
	 */
	private ArrayList<AppBean> getAppInfoData() {
		return mAppBeanList;
	}

	protected void recycle() {
		int count = this.getChildCount();
		if (count > 0) {
			AppsUpdateInfoListItem appListItemView = null;
			for (int i = 0; i < count; i++) {
				appListItemView = (AppsUpdateInfoListItem) this.getChildAt(i);
				appListItemView.destory();
			}
		}
		if (mAppBeanList != null) {
			mAppBeanList.clear();
			mAppBeanList = null;
		}
		if (mNoUpdateAppBeanList != null) {
			mNoUpdateAppBeanList.clear();
			mNoUpdateAppBeanList = null;
		}
		setAdapter(null);
		setOnItemClickListener(null);
	}

	protected void handleRequestChange(int state, Object value) {
		switch (state) {
			case AppsManageViewController.MSG_ID_START :
				break;

			case AppsManageViewController.MSG_ID_FINISH :
				mNetworkTipsTool.dismissProgress();
				onLoadIsFinished(value);
				break;

			case AppsManageViewController.MSG_ID_EXCEPTION : {
				mNetworkTipsTool.dismissProgress();
				mNetworkTipsTool.showErrorTip(true);
				GoLauncher.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
						IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 0, null, null);
				// DeskToast.makeText(getContext(),
				// R.string.apps_management_network_error, Toast.LENGTH_SHORT)
				// .show();
			}
				break;

			default :
				break;
		}
	}

	/**
	 * 从服务器请求数据后，对数据进行处理
	 * 
	 * @param value
	 */
//	private void onLoadIsFinished(Object value) {
//		if (mAppBeanList != null && value != null && value instanceof AppsBean) {
//			AppsBean mAppsBean = (AppsBean) value;
//
//			ArrayList<AppBean> allAppBeanList = mAppsBean.mListBeans;
//			if (allAppBeanList != null) {
//				// filterAppBeans(allAppBeanList);
//				// sortAppBeans(allAppBeanList);
//
//				filterNoUpdateApp(allAppBeanList);
//
//				int size = mAppBeanList.size();
//				// 保存现在可更新应用的个数
//				if (AppFuncFrame.getFunControler() != null) {
//					AppFuncFrame.getFunControler().setmBeancount(size);
//				}
//				// 修改应用更新tab头上更新的数字
//				GoLauncher.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME,
//						IDiyMsgIds.APPS_MANAGEMENT_UPDATE_COUNT, size, null, null);
//
//				if (mAppsManageViewAdapter != null && size > 0) {
//					AppsManagementActivity.getApplicationManager().setUpdateAppsCount(size);
//					mNetworkTipsTool.showNothing();
//					mAppsManageViewAdapter.setDataSet(mAppBeanList);
//					GoLauncher.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
//							IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 1, null, null);
//					return;
//				}
//			}
//			//			AppsManagementActivity.getApplicationManager().setUpdateAppsCount(0);
//			//			mNetworkTipsTool.showNoUpdateDataTip();
//			//			GoLauncher.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
//			//					IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 0, null, null);
//		}
//		// 修改应用更新tab头上更新的数字
//		GoLauncher.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME,
//				IDiyMsgIds.APPS_MANAGEMENT_UPDATE_COUNT, 0, null, null);
//		AppsManagementActivity.getApplicationManager().setUpdateAppsCount(0);
//		mAppsManageViewAdapter.setDataSet(null);
//		mNetworkTipsTool.showNoUpdateDataTip();
//		GoLauncher.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
//				IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 0, null, null);
//	}

	/**
	 * 从服务器请求数据后，对数据进行处理
	 * 
	 * @param value
	 */
	private void onLoadIsFinished(Object value) {
		int size = 0;
		if (mAppBeanList != null && value != null && value instanceof AppsBean) {
			AppsBean mAppsBean = (AppsBean) value;
			ArrayList<AppBean> allAppBeanList = mAppsBean.mListBeans;
			if (allAppBeanList != null) {
				filterNoUpdateApp(allAppBeanList);
				size = mAppBeanList.size();
			}
		}

		// 保存现在可更新应用的个数
		if (AppFuncFrame.getFunControler() != null) {
			AppFuncFrame.getFunControler().setmBeancount(size);
		}
		
		// 修改应用更新tab头上更新的数字
		GoLauncher.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME,
				IDiyMsgIds.APPS_MANAGEMENT_UPDATE_COUNT, size, null, null);

//		AppsManagementActivity.getApplicationManager().setUpdateAppsCount(size);
		
		//隐藏进度条
		mNetworkTipsTool.showNothing();

		if (size > 0) {
			//获取到更新的应用
			if (mAppsManageViewAdapter != null) {
				mAppsManageViewAdapter.setDataSet(mAppBeanList);
			}
			GoLauncher.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
					IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 1, null, null);
		} else {
			//没有更新的应用，显示无更新应用的提示
			if (mAppsManageViewAdapter != null) {
				mAppsManageViewAdapter.setDataSet(null);
			}
			mNetworkTipsTool.showNoUpdateDataTip();
			GoLauncher.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
					IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 0, null, null);
		}
	}

	// /**
	// * 过滤已经卸载的应用程序
	// *
	// * @param appListBeans
	// */
	// private void filterAppBeans(ArrayList<AppBean> appListBeans) {
	// if (appListBeans == null || appListBeans.size() <= 0) {
	// return;
	// }
	//
	// int size = appListBeans.size();
	// ArrayList<AppBean> tmpList = new ArrayList<AppBean>(size);
	// for (AppBean bean : appListBeans) {
	// boolean isExist = AppUtils.isAppExist(getContext(), bean.mPkgName);
	// if (!isExist) {
	// // 如果不存在，则加入临时列表
	// tmpList.add(bean);
	// }
	// }
	// for (AppBean item : tmpList) {
	// // 把已经卸载或不存在的应用程序从appListBeans中移除
	// appListBeans.remove(item);
	// }
	// }

	/**
	 * 过滤忽略更新的应用
	 */
	private void filterNoUpdateApp(ArrayList<AppBean> appListBeans) {
		// ArrayList<NoPromptUpdateInfo> noUpdateAppList =
		// mNoUpdateModel.getAllNoPromptUpdateApp();
		ArrayList<NoPromptUpdateInfo> noUpdateAppList = ApplicationManager
				.getInstance(getContext()).getAllNoPromptUpdateApp();
		mNoUpdateAppBeanList = new ArrayList<AppBean>();
		if (appListBeans != null && appListBeans.size() > 0) {
			if (noUpdateAppList != null && noUpdateAppList.size() > 0) {
				for (NoPromptUpdateInfo noUpdateInfo : noUpdateAppList) {
					String packageName = getPkgNameByIntent(noUpdateInfo.getIntent());
					if (packageName != null) {
						for (AppBean appBean : appListBeans) {
							if (packageName.equals(appBean.mPkgName)) {
								mNoUpdateAppBeanList.add(appBean);
								break;
							}
						}
					}
				}
			} else {
				mAppBeanList = appListBeans;
			}

			if (mAppBeanList != null && mAppBeanList.size() == 0) {
				for (AppBean appBean : appListBeans) {
					if (!mNoUpdateAppBeanList.contains(appBean)) {
						mAppBeanList.add(appBean);
					}
				}
			}
			// mAppBeanList.removeAll(mNoUpdateAppBeanList);
		}
	}

	/**
	 * 根据intent获取包名
	 * 
	 * @param intent
	 * @return
	 */
	private String getPkgNameByIntent(Intent intent) {
		String packageName = null;
		if (intent != null) {
			ComponentName comonentName = intent.getComponent();
			if (comonentName != null) {
				packageName = comonentName.getPackageName();
			}
		}
		return packageName;
	}

	// private void sortAppBeans(List<AppBean> appBeans) {
	// PackageManager pkgMgr = getContext().getPackageManager();
	// SortUtils.sort(appBeans, "getAppName",
	// new Class[] { PackageManager.class }, new Object[] { pkgMgr },
	// "ASC");
	// }

	public void setTipTool(NetworkTipsTool networkTipsTool) {
		mNetworkTipsTool = networkTipsTool;
	}

	//	public void updateItemStatus(int msgId, int id, Object obj, List objs) {
	//		List<AppBean> appBeans = getAppInfoData();
	//		if (appBeans != null) {
	//			ApplicationManager applicationManager = AppsManagementActivity.getApplicationManager();
	//			for (AppBean appBean : appBeans) {
	//				if (appBean.mAppId == id) {
	//					switch (msgId) {
	//						case IDiyMsgIds.APPS_MANAGEMENT_WAIT_FOR_DOWNLOAD :
	//							if (appBean.getStatus() == AppBean.STATUS_CANCELING) {
	//								applicationManager.cancelDownload(appBean);
	//							} else {
	//								appBean.setStatus(AppBean.STATUS_WAITING_DOWNLOAD);
	//							}
	//							break;
	//						case IDiyMsgIds.APPS_MANAGEMENT_START_DOWNLOAD :
	//							if (appBean.getStatus() == AppBean.STATUS_CANCELING) {
	//								applicationManager.cancelDownload(appBean);
	//							} else {
	//								appBean.setStatus(AppBean.STATUS_DOWNLOADING);
	//							}
	//							break;
	//						case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOADING :
	//							if (objs != null && objs.size() == 2) {
	//								long downloadSize = ( Long ) objs.get(0);
	//								int percent = ( Integer ) objs.get(1);
	//								appBean.setAlreadyDownloadSize(downloadSize);
	//								appBean.setAlreadyDownloadPercent(percent);
	//								appBean.setStatus(AppBean.STATUS_DOWNLOADING);
	//							}
	//							break;
	//
	//						case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_COMPLETED :
	//							String saveFilePath = ( String ) obj;
	//							// 对未来得及取消的任务做特殊处理，把下载好的文件删除，状态恢复为Normal
	//							// if (appBean.getStatus() ==
	//							// AppBean.STATUS_CANCELING)
	//							// {
	//							// File file = new File(saveFilePath);
	//							// if (file.exists()) {
	//							// file.delete();
	//							// }
	//							// appBean.setStatus(AppBean.STATUS_NORMAL);
	//							// appBean.setAlreadyDownloadSize(0);
	//							// appBean.setAlreadyDownloadPercent(0);
	//							// NotificationManager manager =
	//							// (NotificationManager)
	//							// getContext()
	//							// .getSystemService(Context.NOTIFICATION_SERVICE);
	//							// manager.cancel(NOTIFY_TAG, id);
	//							// } else {
	//							appBean.setFilePath(saveFilePath);
	//							appBean.setStatus(AppBean.STATUS_DOWNLOAD_COMPLETED);
	//							if (objs != null && objs.size() == 2) {
	//								long downloadSize = ( Long ) objs.get(0);
	//								int percent = ( Integer ) objs.get(1);
	//								appBean.setAlreadyDownloadSize(downloadSize);
	//								appBean.setAlreadyDownloadPercent(percent);
	//							}
	//							// AppsManagementActivity.getApplicationManager()
	//							// .installApp(new File(saveFilePath));
	//							// ((AppsManagementActivity)mContext).addInstallApp(saveFilePath,appBean.mPkgName);
	//							// }
	//							break;
	//						case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_CANCELED :
	//							appBean.setAlreadyDownloadSize(0);
	//							appBean.setAlreadyDownloadPercent(0);
	//							appBean.setStatus(AppBean.STATUS_NORMAL);
	//							break;
	//						case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_FAILED :
	//							appBean.setStatus(AppBean.STATUS_DOWNLOAD_FAILED);
	//							appBean.setAlreadyDownloadSize(0);
	//							appBean.setAlreadyDownloadPercent(0);
	//							DeskToast.makeText(getContext(),
	//									R.string.apps_management_network_error, Toast.LENGTH_SHORT)
	//									.show();
	//							break;
	//					}
	//					mAppsManageViewAdapter.notifyDataSetChanged(this, appBean);
	//					break;
	//				}
	//			}
	//		}
	//	}

	public void updateItemStatus(int downloadState, long id, DownloadTask downloadTask) {
		List<AppBean> appBeans = getAppInfoData();
		if (appBeans != null) {
			ApplicationManager applicationManager = AppsManagementActivity.getApplicationManager();
			for (AppBean appBean : appBeans) {
				if (appBean.mAppId == id) {
					switch (downloadState) {
						case IDiyMsgIds.APPS_MANAGEMENT_WAIT_FOR_DOWNLOAD :
							if (appBean.getStatus() == AppBean.STATUS_CANCELING) {
								applicationManager.cancelDownload(appBean);
							} else {
								appBean.setStatus(AppBean.STATUS_WAITING_DOWNLOAD);
							}
							break;
						case IDiyMsgIds.APPS_MANAGEMENT_START_DOWNLOAD :
							if (appBean.getStatus() == AppBean.STATUS_CANCELING) {
								applicationManager.cancelDownload(appBean);
							} else {
								appBean.setStatus(AppBean.STATUS_DOWNLOADING);
							}
							break;
						case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOADING :
							long downloadSize = downloadTask.getAlreadyDownloadSize();
							int percent = downloadTask.getAlreadyDownloadPercent();
							appBean.setAlreadyDownloadSize(downloadSize);
							appBean.setAlreadyDownloadPercent(percent);
							appBean.setStatus(AppBean.STATUS_DOWNLOADING);
							break;

						case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_COMPLETED :
							String saveFilePath = downloadTask.getSaveFilePath();
							appBean.setFilePath(saveFilePath);
							appBean.setStatus(AppBean.STATUS_DOWNLOAD_COMPLETED);

							appBean.setAlreadyDownloadSize(downloadTask.getAlreadyDownloadSize());
							appBean.setAlreadyDownloadPercent(downloadTask
									.getAlreadyDownloadPercent());
							// AppsManagementActivity.getApplicationManager()
							// .installApp(new File(saveFilePath));
							// ((AppsManagementActivity)mContext).addInstallApp(saveFilePath,appBean.mPkgName);
							// }
							break;
						case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_CANCELED :
							appBean.setAlreadyDownloadSize(0);
							appBean.setAlreadyDownloadPercent(0);
							appBean.setStatus(AppBean.STATUS_NORMAL);
							break;
						case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_FAILED :
							appBean.setStatus(AppBean.STATUS_DOWNLOAD_FAILED);
							appBean.setAlreadyDownloadSize(0);
							appBean.setAlreadyDownloadPercent(0);
							DeskToast.makeText(getContext(),
									R.string.apps_management_network_error, Toast.LENGTH_SHORT)
									.show();
							break;
					}
					mAppsManageViewAdapter.notifyDataSetChanged(this, appBean);
					break;
				}
			}
		}
	}

	public void updateItemStatus(AppBean appBean) {
		mAppsManageViewAdapter.notifyDataSetChanged(this, appBean);
	}
	/**
	 * 显示忽略更新的弹出框
	 * 
	 * @param appBean
	 * @param position
	 */
	public void showNoUpdateDialog(final AppBean appBean, final int position) {
		PackageManager pm = getContext().getPackageManager();
		AlertDialog.Builder builder = new DeskBuilder(getContext());

		builder.setTitle(appBean.getAppName(pm));

		CharSequence[] data = getContext().getResources().getTextArray(
				R.array.apps_no_prompt_update_dialog_style);
		if (null != data && data.length > 0) {
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(),
					R.layout.app_no_pro_list_item, R.id.apps_no_pro_text, data);
			builder.setAdapter(adapter, null);
		}

		builder.setSingleChoiceItems(R.array.select_sort_style, 0,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
						addNoUpdateApp(appBean.mPkgName, position);
					}
				});
		try {
			builder.show();
		} catch (Exception e) {
			try {
				DeskToast.makeText(getContext(), R.string.alerDialog_error, Toast.LENGTH_SHORT)
						.show();
			} catch (OutOfMemoryError error) {
			}
		}
	}

	/**
	 * 添加忽略更新的应用
	 * 
	 * @param packageName
	 */
	private void addNoUpdateApp(String packageName, int position) {
		ApplicationManager.getInstance(getContext()).addNoUpdateApp(packageName);
		moveListData(position, mAppBeanList, mNoUpdateAppBeanList);

		// 重新设置更新信息
		resetUpdateInfo();
	}

	/**
	 * 恢复更新的应用
	 * 
	 * @param packageName
	 */
	public void promptUpdateApp(String packageName, int position) {
		ApplicationManager.getInstance(getContext()).deleteNoUpdateApp(packageName);
		if (packageName == null) {
			// 恢复所有更新
			moveListData(mNoUpdateAppBeanList, mAppBeanList);
		} else {
			moveListData(position, mNoUpdateAppBeanList, mAppBeanList);
		}
		// 重新设置更新信息
		resetUpdateInfo();
	}

	/**
	 * 将制定位置的appBean，从removeFromList移动到addToList中
	 * 
	 * @param position
	 * @param removeFromList
	 * @param addToList
	 */
	public void moveListData(int position, ArrayList<AppBean> removeFromList,
			ArrayList<AppBean> addToList) {
		if (removeFromList != null) {
			if (position >= 0 && position < removeFromList.size()) {
				AppBean appBean = removeFromList.remove(position);
				if (addToList == null) {
					addToList = new ArrayList<AppBean>();
				}
				addToList.add(appBean);
			}
		}
	}

	public void moveListData(ArrayList<AppBean> removeFromList, ArrayList<AppBean> addToList) {
		if (addToList == null) {
			addToList = new ArrayList<AppBean>();
		}
		if (removeFromList != null) {
			addToList.addAll(removeFromList);
			removeFromList.clear();
		}
	}

	/**
	 * 安装，卸载和更新时，刷新更新列表，不再重新向服务器请求数据
	 * 
	 * @param packageName
	 */
	public void updateList(String packageName, boolean isInstall) {
		if (mAppBeanList != null && packageName != null) {
			int position = findAppInList(packageName, mAppBeanList);
			if (position != -1) {
				// 暂时没有时间处理
				// if (isInstall) {
				// //是安装或更新时，先判断下，本次安装的版本，是否是跟提示更新的版本一致，可能用户通过第三方软件来安装
				// } else {
				// //是卸载，直接将软件从更新列表中删除,如果更新包已下载，删除更新包
				// appBeans.remove(position);
				// adapter.notifyDataSetChanged();
				// }
				mAppBeanList.remove(position);
				resetUpdateInfo();
			} else {
				// 查找卸载的应用，是否在忽略更新里面
				if (mNoUpdateAppBeanList != null) {
					position = findAppInList(packageName, mNoUpdateAppBeanList);
					if (position != -1) {
						mNoUpdateAppBeanList.remove(position);
						ApplicationManager.getInstance(getContext()).deleteNoUpdateApp(packageName);
					}
				}
			}
		}
	}

	/**
	 * 从忽略更新页面返回时，刷新更新列表页面
	 */
	public void updateList() {
		if (mAppsManageViewAdapter != null) {
			mAppsManageViewAdapter.setDataSet(mAppBeanList);
			mAppsManageViewAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 重置更新信息
	 */
	private void resetUpdateInfo() {
		int size = 0;
		if (mAppBeanList != null) {
			size = mAppBeanList.size();
			// 保存现在可更新应用的个数
			if (AppFuncFrame.getFunControler() != null) {
				AppFuncFrame.getFunControler().setmBeancount(size);
			}
			// 好像功能表菜单键，应用管理的更新数据 是从这里获取的。
			AppsManagementActivity.getApplicationManager().setUpdateAppsCount(size);

			// //发消息给应用推荐，重新设置哪些应用可以更新
			// GoLauncher.sendMessage(this,
			// IDiyFrameIds.APPS_MANAGEMENT_RECOMMENDED_APP_FRAME,
			// IDiyMsgIds.APPS_MANAGEMENT_RECOMMENDED_APP, 0, null,
			// mAppBeanList);
		}
		if (size > 0) {
			if (mAppsManageViewAdapter != null) {
				if (mAppsManageViewAdapter.getAppBeanList() == null
						|| mAppsManageViewAdapter.getAppBeanList().size() == 0) {
					mAppsManageViewAdapter.setDataSet(mAppBeanList);
					if (mNetworkTipsTool != null) {
						mNetworkTipsTool.showNothing();
					}
				}
				mAppsManageViewAdapter.notifyDataSetChanged();
			}
		} else {
			if (mAppsManageViewAdapter != null) {
				mAppsManageViewAdapter.setDataSet(null);
				if (mNetworkTipsTool != null) {
					mNetworkTipsTool.showNoUpdateDataTip();
				}
			}
		}

		// 改变操作按钮上，可以更新的数据,这里为什么不直接把size发过去呢？？
		GoLauncher.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
				IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 1, null, null);

		// 发消息，该表应用更新tab栏上面的更新数据
		GoLauncher.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME,
				IDiyMsgIds.APPS_MANAGEMENT_UPDATE_COUNT, size, null, null);
	}

	/**
	 * 根据包名，找出应用在list中的位置
	 * 
	 * @param packageName
	 * @param appBeanList
	 * @return
	 */
	private int findAppInList(String packageName, ArrayList<AppBean> appBeanList) {
		int position = -1;
		int size = appBeanList.size();
		AppBean appBean = null;
		for (int i = 0; i < size; i++) {
			appBean = appBeanList.get(i);
			if (appBean.mPkgName.equals(packageName)) {
				position = i;
				break;
			}
		}
		return position;
	}

	public ArrayList<AppBean> getmNoUpdateAppBeanList() {
		return mNoUpdateAppBeanList;
	}

}