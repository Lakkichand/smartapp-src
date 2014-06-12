package com.jiubang.ggheart.apps.appmanagement.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.gau.util.unionprotocol.Util;
import com.go.util.device.Machine;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appmanagement.controler.ApplicationManager;
import com.jiubang.ggheart.apps.appmanagement.controler.ApplicationManager.IDownloadInvoker;
import com.jiubang.ggheart.apps.appmanagement.controler.AppsManageViewController;
import com.jiubang.ggheart.apps.appmanagement.help.NetworkTipsTool;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.BaseController;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-27]
 */
public class AppsUpdateViewContainer extends LinearLayout
		implements
			IModeChangeListener,
			IMessageHandler,
			ICleanable,
			IDownloadInvoker {
	private final static int OPERATION_TYPE_REFRESH = 0;
	private final static int OPERATION_TYPE_UPDATE_ALL = 1;
	private final static int OPERATION_TYPE_CANCEL_ALL = 2;
	private final static int OPERATION_TYPE_OPERATING_UPDATE = 3;
	private final static int OPERATION_TYPE_OPERATING_CANCEL = 4;
	private int mOperationType = OPERATION_TYPE_REFRESH;
	private AppsUpdateView mUpdateListView = null;
	private TextView mOperationButton; // 全部更新
	private TextView mUpdateInfoView; // 更新项
	private AppsManageViewController mAppsManageViewController = null;
	private NetworkTipsTool mNetworkTipsTool;

	public AppsUpdateViewContainer(Context context) {
		super(context);
		init();
	}

	public AppsUpdateViewContainer(Context context, AttributeSet attr) {
		super(context, attr);
		init();
	}

	private void init() {
		this.setOrientation(LinearLayout.VERTICAL);
		mAppsManageViewController = new AppsManageViewController(getContext()
				.getApplicationContext(), this);
		GoLauncher.registMsgHandler(this);
	}

	@Override
	public int getId() {
		return IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME;
	}

	@Override
	protected void onFinishInflate() {
		ViewGroup tipsView = (ViewGroup) findViewById(R.id.network_tips_view);
		mNetworkTipsTool = new NetworkTipsTool(tipsView);
		mUpdateListView = (AppsUpdateView) findViewById(R.id.upate_list_view);
		mUpdateListView.setTipTool(mNetworkTipsTool);
		mUpdateInfoView = (TextView) findViewById(R.id.update_info);
		initUpdateAllButton();
		// sendRequest();

		// sdcard不存在时，加载提示页面
		if (!Machine.isSDCardExist()) {
			showExceptionView();
		} else if (!Machine.isNetworkOK(getContext())) {
			// showExceptionView();
			if (mNetworkTipsTool != null) {
				mNetworkTipsTool.showErrorTip(true);
			}
		} else {
			sendRequest();
		}
	}

	private void initUpdateAllButton() {
		mOperationButton = (TextView) findViewById(R.id.operation_button);
		mOperationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (mOperationType) {
					case OPERATION_TYPE_UPDATE_ALL :
						if ("200".equals(GoStorePhoneStateUtil.getUid(getContext()))
								|| !Machine.isCnUser(getContext())) {
							// 统计：先保存界面入口
							AppManagementStatisticsUtil.getInstance().saveCurrentUIEnter(
									getContext(), AppManagementStatisticsUtil.UIENTRY_TYPE_LIST);
							if (!Util.gotoMarketMyApp(getContext())) {
								Toast.makeText(getContext(), R.string.no_googlemarket_tip,
										Toast.LENGTH_SHORT).show();
							} else {
								AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
										.getAdapter();
								if (adapter != null) {
									List<AppBean> appBeans = adapter.getAppBeanList();
									if (appBeans != null && !appBeans.isEmpty()) {
										for (AppBean appBean : appBeans) {
											String appName = appBean.getAppName(getContext()
													.getPackageManager());
											// 统计：应用更新--更新点击
											AppManagementStatisticsUtil.getInstance()
													.saveUpdataClick(getContext(),
															appBean.mPkgName, appBean.mAppId, 1);
										}
									}
								}
							}
						} else {
							showDialog(v.getContext());
						}
						break;
					case OPERATION_TYPE_CANCEL_ALL :
						changeOperationType(OPERATION_TYPE_OPERATING_CANCEL);
						AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
								.getAdapter();
						if (adapter != null) {
							List<AppBean> appBeans = adapter.getAppBeanList();
							if (appBeans != null && !appBeans.isEmpty()) {
								ApplicationManager applicationManager = AppsManagementActivity
										.getApplicationManager();
								ArrayList<Long> taskIdList = new ArrayList<Long>(appBeans.size());
								for (AppBean appBean : appBeans) {
									if (appBean.getStatus() == AppBean.STATUS_GET_READY
											|| appBean.getStatus() == AppBean.STATUS_WAITING_DOWNLOAD
											|| appBean.getStatus() == AppBean.STATUS_DOWNLOADING) {
										appBean.setStatus(AppBean.STATUS_CANCELING);
										if (mUpdateListView != null) {
											// mUpdateListView.updateItemStatus(-1,
											// appBean.mAppId, null, null);
											mUpdateListView.updateItemStatus(appBean);
											taskIdList.add(Long.valueOf(appBean.mAppId));
										}
										// applicationManager.cancelDownload(appBean);
									}
								}
								if (taskIdList.size() > 0) {
									applicationManager.cancelAllDownload(taskIdList);
								}

							}
						}
						break;
					case OPERATION_TYPE_REFRESH :
						requestData();
						break;
					default :
						break;
				}
			}
		});
	}

	private void showDialog(Context context) {
		new AlertDialog.Builder(context)
				.setTitle(R.string.apps_management_update_all_dialog_title)
				.setMessage(R.string.apps_management_update_all_dialog_message)
				.setPositiveButton(R.string.apps_management_dialog_button_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								changeOperationType(OPERATION_TYPE_OPERATING_UPDATE);
								//								new Thread(new Runnable() {
								//									public void run() {
								AppsManagementActivity.getApplicationManager().actionDownload(
										getContext(), AppsUpdateViewContainer.this);
								//									}
								//								}).start();
							}
						})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

	@Override
	public void onModleChanged(int action, int state, Object value) {
		switch (action) {
			case AppsManageViewController.ACTION_START_REQUEST : {
				mUpdateListView.handleRequestChange(state, value);
				if (value != null && value instanceof AppsBean) {
					AppsBean appsBean = (AppsBean) value;
					// GoLauncher.sendMessage(this,
					// IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME,
					// IDiyMsgIds.APPS_MANAGEMENT_UPDATE_COUNT,
					// appsBean.mListBeans.size(), null, null);

					setAppState(appsBean.mListBeans);
					
					GoLauncher.sendMessage(this,
							IDiyFrameIds.APPS_MANAGEMENT_RECOMMENDED_APP_FRAME,
							IDiyMsgIds.APPS_MANAGEMENT_RECOMMENDED_APP, 0, null,
							appsBean.mListBeans);

					HashMap<Integer, Byte> controlMap = appsBean.mControlcontrolMap;
					if (controlMap != null && !controlMap.isEmpty()) {
						setmControlInfo(controlMap.get(2), controlMap.get(3), controlMap.get(4));
					}
				} else {
					mOperationType = 0;
					mOperationButton.setClickable(true);
					if (state == BaseController.STATE_RESPONSE_ERR) {
						Toast.makeText(
								getContext(),
								getContext().getResources().getString(
										R.string.themestore_download_fail), Toast.LENGTH_SHORT)
								.show();
					}
				}
			}
				break;
		// case AppsManageViewController.ACTION_START_REQUEST
		}
	}

	/**
	 * <br>功能简述:进入应用更新时，可能之前已经点击了全部更新，此时，要将状态设置为下载状态
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param appBeanList
	 */
	private void setAppState(ArrayList<AppBean> appBeanList) {
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				Map<Long, DownloadTask> map = mDownloadController.getDownloadConcurrentHashMap();
				if (map != null) {
					for (DownloadTask task : map.values()) {
						if (task != null) {
							long id = task.getId();
							setAppState(id, appBeanList);
						}
					}
				}

			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void setAppState(long appId, ArrayList<AppBean> appBeanList) {
		if (appBeanList != null) {
			for (AppBean appBean : appBeanList) {
				if (appBean.mAppId == appId) {
					appBean.setStatus(AppBean.STATUS_DOWNLOADING);
					break;
				}
			}
		}
	}
	/**
	 * 保存应用更新是否提示开关信息
	 * 
	 * @param appIconControl
	 * @param gostoreControl
	 * @param appFuncMenuControl
	 */
	public void setmControlInfo(byte appIconControl, byte gostoreControl, byte appFuncMenuControl) {
		PreferencesManager preferences = new PreferencesManager(getContext(),
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);

		preferences.putInt(FunControler.APPICON_SHOW_MESSSAGE, appIconControl); // 功能表图标右上角
		preferences.putInt(FunControler.GOSTORE_SHOW_MESSAGE, gostoreControl); // GO
																			// Store功能表图标右上角
		preferences.putInt(FunControler.APPFUNC_APPMENU_SHOW_MESSAGE, appFuncMenuControl); // 功能表Menu菜单

		preferences.commit();
	}

	private void updateAll() {
		//进行统计
		StatisticsData.countStatData(getContext(), StatisticsData.STAT_KEY_ALLUPDATA);
		AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView.getAdapter();
		if (adapter != null) {
			List<AppBean> appBeans = adapter.getAppBeanList();
			if (appBeans != null && !appBeans.isEmpty()) {
				AppsManagementActivity.getApplicationManager().downloadAll(appBeans);
				adapter.notifyDataSetChanged();
			}
		}
	}

	/**
	 * 向服务器请求数据
	 */
	private void sendRequest() {
		changeOperationType(OPERATION_TYPE_OPERATING_UPDATE);
		((AppsUpdateViewAdapter) mUpdateListView.getAdapter()).setDataSet(null);
		mNetworkTipsTool.showProgress();

		// 请求网络数据
		mAppsManageViewController.sendRequest(AppsManageViewController.ACTION_START_REQUEST, null);
	}

	@Override
	public boolean handleMessage(Object who, int type, final int msgId, final int param,
			final Object object, final List objects) {
		switch (msgId) {
			case IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON :
				if (mOperationButton != null) {
					if (param == 1) {
						AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
								.getAdapter();
						int size = 0;
						if (adapter != null) {
							List<AppBean> appBeans = adapter.getAppBeanList();
							if (appBeans != null) {
								size = appBeans.size();
							}
						}

						if (size > 0) {
							changeOperationType(OPERATION_TYPE_UPDATE_ALL);
							String suffix = getResources().getString(
									R.string.apps_management_has_update_item);
							mUpdateInfoView.setText(size + " " + suffix);
						} else {
							changeOperationType(OPERATION_TYPE_REFRESH);

						}
					} else {
						changeOperationType(OPERATION_TYPE_REFRESH);
					}
				}
				break;
		//			case IDiyMsgIds.APPS_MANAGEMENT_WAIT_FOR_DOWNLOAD :
		//			case IDiyMsgIds.APPS_MANAGEMENT_START_DOWNLOAD :
		//			case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOADING :
		//			case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_COMPLETED :
		//			case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_FAILED :
		//			case IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_CANCELED :
		//				if (mUpdateListView != null) {
		//					final int id = param;
		//					mUpdateListView.updateItemStatus(msgId, id, object, objects);
		//				}
		//				break;
		}

		//		if (msgId == IDiyMsgIds.APPS_MANAGEMENT_WAIT_FOR_DOWNLOAD
		//				|| msgId == IDiyMsgIds.APPS_MANAGEMENT_START_DOWNLOAD) {
		//			// 点击全部更新按钮后
		//			if (mOperationType == OPERATION_TYPE_OPERATING_UPDATE) {
		//				boolean has = hasGettingReadyApps();
		//				if (!has) {
		//					changeOperationType(OPERATION_TYPE_CANCEL_ALL);
		//				}
		//			}
		//		} else if (msgId == IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_COMPLETED
		//				|| msgId == IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_FAILED
		//				|| msgId == IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_CANCELED) {
		//			// 点击取消全部更新按钮后
		//			if (mOperationType == OPERATION_TYPE_OPERATING_CANCEL
		//					|| mOperationType == OPERATION_TYPE_CANCEL_ALL) {
		//				boolean has = hasDownloadingAndWaitingApps();
		//				if (!has) {
		//					changeOperationType(OPERATION_TYPE_UPDATE_ALL);
		//				}
		//			}
		//		}
		return true;
	}

	public void updateDownloadState(int downloadState, DownloadTask downloadTask) {
		
		if (mUpdateListView != null) {
			mUpdateListView.updateItemStatus(downloadState, downloadTask.getId(), downloadTask);
		}
		
		if (downloadState == IDiyMsgIds.APPS_MANAGEMENT_WAIT_FOR_DOWNLOAD
				|| downloadState == IDiyMsgIds.APPS_MANAGEMENT_START_DOWNLOAD
				|| downloadState == IDiyMsgIds.APPS_MANAGEMENT_DOWNLOADING) {
			// 点击全部更新按钮后
			if (mOperationType == OPERATION_TYPE_OPERATING_UPDATE) {
				boolean has = hasGettingReadyApps();
				if (!has) {
					changeOperationType(OPERATION_TYPE_CANCEL_ALL);
				}
			}
		} else if (downloadState == IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_COMPLETED
				|| downloadState == IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_FAILED
				|| downloadState == IDiyMsgIds.APPS_MANAGEMENT_DOWNLOAD_CANCELED) {
			// 点击取消全部更新按钮后
			if (mOperationType == OPERATION_TYPE_OPERATING_CANCEL
					|| mOperationType == OPERATION_TYPE_CANCEL_ALL) {
				boolean has = hasDownloadingAndWaitingApps();
				if (!has) {
					changeOperationType(OPERATION_TYPE_UPDATE_ALL);
				}
			}
		}
	}

	public void requestData() {
		if (Machine.isNetworkOK(getContext())) {
			sendRequest();
		}
	}

	@Override
	public void cleanup() {
		if (mUpdateListView != null) {
			mUpdateListView.recycle();
		}
		GoLauncher.unRegistMsgHandler(this);
	}

	@Override
	public void invokeDownload() {
		updateAll();
	}

	/**
	 * 显示sd卡不可用的提示信息
	 */
	public void showExceptionView() {
		if (mNetworkTipsTool != null) {
			mNetworkTipsTool.showErrorTip(false);
		}
		if (mUpdateListView != null) {
			mUpdateListView.setVisibility(View.GONE);
		}
		// 改变颜色
		mOperationButton.setEnabled(false);
		mOperationButton.setTextColor(Color.parseColor("#acacac"));
		mOperationButton.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.appsmanagement_update_icon_gray, 0, 0, 0);
	}

	/**
	 * 显示可以更新的列表信息
	 */
	public void showUpdateView() {
		if (mNetworkTipsTool != null) {
			mNetworkTipsTool.showNothing();
		}

		if (mUpdateListView != null) {
			if (mUpdateListView.getVisibility() == View.GONE) {
				mUpdateListView.setVisibility(View.VISIBLE);
				requestData();
			}
		}
		mOperationButton.setEnabled(true);
		// 改变颜色
		mOperationButton.setTextColor(Color.parseColor("#8FBE00"));
		mOperationButton.setText(R.string.apps_management_operation_button_update_all_label);
		mOperationButton.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.appsmanagement_update_icon, 0, 0, 0);
	}

	private void changeOperationType(int type) {
		mOperationType = type;
		int drawableId = -1;
		switch (mOperationType) {
			case OPERATION_TYPE_UPDATE_ALL :
				drawableId = R.drawable.appsmanagement_update_icon;
				if (!Machine.isSDCardExist()) {
					drawableId = R.drawable.appsmanagement_update_icon_gray;
				}
				mOperationButton
						.setText(R.string.apps_management_operation_button_update_all_label);
				mOperationButton.setClickable(true);
				break;
			case OPERATION_TYPE_CANCEL_ALL :
				drawableId = R.drawable.appsmanagement_cancel_icon;
				mOperationButton
						.setText(R.string.apps_management_operation_button_cancel_all_label);
				mOperationButton.setClickable(true);
				break;
			case OPERATION_TYPE_REFRESH :
				drawableId = R.drawable.appsmanagement_refresh_icon;
				mOperationButton.setText(R.string.apps_management_operation_button_refresh_label);
				mUpdateInfoView.setText(R.string.apps_management_none_for_update);
				mOperationButton.setClickable(true);
				break;
			case OPERATION_TYPE_OPERATING_CANCEL :
			case OPERATION_TYPE_OPERATING_UPDATE :
				mOperationButton.setClickable(false);
				break;
		}
		if (drawableId > -1) {
			mOperationButton.setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
		}
	}

	private boolean hasGettingReadyApps() {
		boolean has = false;
		AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView.getAdapter();
		if (adapter != null) {
			List<AppBean> appBeans = adapter.getAppBeanList();
			if (appBeans != null) {
				for (AppBean appBean : appBeans) {
					if (appBean.getStatus() == AppBean.STATUS_GET_READY) {
						has = true;
						break;
					}
				}
			}
		}
		return has;
	}

	private boolean hasDownloadingAndWaitingApps() {
		boolean has = false;
		AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView.getAdapter();
		if (adapter != null) {
			List<AppBean> appBeans = adapter.getAppBeanList();
			if (appBeans != null) {
				for (AppBean appBean : appBeans) {
					if (appBean.getStatus() == AppBean.STATUS_DOWNLOADING
							|| appBean.getStatus() == AppBean.STATUS_WAITING_DOWNLOAD
							|| appBean.getStatus() == AppBean.STATUS_CANCELING) {
						has = true;
						break;
					}
				}
			}
		}
		return has;
	}

	public ArrayList<AppBean> getAppBeanList() {
		if (mUpdateListView != null) {
			return mUpdateListView.getmNoUpdateAppBeanList();
		}
		return null;
	}

	/**
	 * 恢复忽略更新的应用
	 * 
	 * @param packageName
	 * @param position
	 */
	public void promptUpdate(String packageName, int position) {
		if (mUpdateListView != null) {
			mUpdateListView.promptUpdateApp(packageName, position);
		}
	}

	/**
	 * 安装，卸载和更新时，刷新更新列表，不再重新向服务器请求数据
	 * 
	 * @param packageName
	 */
	public void updateList(String packageName, boolean isInstall) {
		if (mUpdateListView != null) {
			mUpdateListView.updateList(packageName, isInstall);
		}
	}

	/**
	 * 从忽略更新界面返回，刷新界面
	 */
	public void updateList() {
		if (mUpdateListView != null) {
			mUpdateListView.updateList();
		}
	}
}
