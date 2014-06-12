package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.gau.util.unionprotocol.Util;
import com.go.util.device.Machine;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.appgame.appcenter.contorler.ApplicationManager;
import com.jiubang.ggheart.appgame.appcenter.contorler.ApplicationManager.IDownloadInvoker;
import com.jiubang.ggheart.appgame.appcenter.contorler.AppsManageViewController;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.component.ContainerBuiler;
import com.jiubang.ggheart.appgame.base.component.IContainer;
import com.jiubang.ggheart.appgame.base.component.IMenuHandler;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 应用更新页面
 * 
 * @author
 * 
 */
public class AppsUpdateViewContainer extends LinearLayout implements
		IMessageHandler, IDownloadInvoker, IContainer, OnClickListener {
	/**
	 * container的分类id
	 */
	private int mTypeId = -1;

	private final static int OPERATION_TYPE_REFRESH = 0;
	private final static int OPERATION_TYPE_UPDATE_ALL = 1;
	private final static int OPERATION_TYPE_CANCEL_ALL = 2;
	private final static int OPERATION_TYPE_OPERATING_UPDATE = 3;
	private final static int OPERATION_TYPE_OPERATING_CANCEL = 4;
	private int mOperationType = OPERATION_TYPE_REFRESH;

	private AppsUpdateView mUpdateListView = null;
	private AppsNoUpdateViewContainer mAppsNoUpdateView = null;

	private LinearLayout mTabView = null;
	private TextView mUpdateApp;
	private TextView mIngoreApp;

	private Button mOperationButton; // 全部更新
	private TextView mUpdateInfoView; // 更新项
	private NetworkTipsTool mNetworkTipsTool;

	/**
	 * 当前view是否处于被激活状态
	 */
	private boolean mIsActive = false;

	/**
	 * 是否已经请求过更新数据的标志
	 */
	private boolean mIsAlreadyLoad = false;

	private List<DownloadTask> mDownloadTasks;
	/**
	 * 更新数据的状态
	 */
	private int mUpdateState = Integer.MIN_VALUE;

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
		// mAppsManageViewController = new AppsManageViewController(getContext()
		// .getApplicationContext(), this);
		AppsManagementActivity.registMsgHandler(this);
	}

	@Override
	public int getId() {
		return IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME;
	}

	private void initTwoTabButton() {
		mUpdateApp = (TextView) findViewById(R.id.update_app);
		mIngoreApp = (TextView) findViewById(R.id.ingore_app);
		mUpdateApp.setOnClickListener(this);
		mIngoreApp.setOnClickListener(this);

	}

	@Override
	protected void onFinishInflate() {
		ViewGroup tipsView = (ViewGroup) findViewById(R.id.network_tips_view);
		mNetworkTipsTool = new NetworkTipsTool(tipsView);
		mUpdateListView = (AppsUpdateView) findViewById(R.id.upate_list_view);
		mUpdateListView.setTipTool(mNetworkTipsTool, mRetryClickListener);
		mUpdateListView.setOnScrollListener(mScrollListener);
		mUpdateInfoView = (TextView) findViewById(R.id.update_info);
		mTabView = (LinearLayout) findViewById(R.id.tab_view);
		mAppsNoUpdateView = (AppsNoUpdateViewContainer) findViewById(R.id.no_update_view);
		initTwoTabButton();
		initUpdateAllButton();
	}

	private void initUpdateAllButton() {
		mOperationButton = (Button) findViewById(R.id.operation_button);
		mOperationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (mOperationType) {
				case OPERATION_TYPE_UPDATE_ALL:
					if ("200".equals(GoStorePhoneStateUtil.getUid(getContext()))
							|| !Machine.isCnUser(getContext())) {
						// 统计：先保存界面入口
						AppManagementStatisticsUtil
								.getInstance()
								.saveCurrentUIEnter(
										getContext(),
										AppManagementStatisticsUtil.UIENTRY_TYPE_LIST);
						if (!Util.gotoMarketMyApp(getContext())) {
							Toast.makeText(getContext(),
									R.string.no_googlemarket_tip,
									Toast.LENGTH_SHORT).show();
						} else {
							AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
									.getAdapter();
							if (adapter != null) {
								List<AppBean> appBeans = adapter
										.getAppBeanList();
								if (appBeans != null && !appBeans.isEmpty()) {
									for (AppBean appBean : appBeans) {
										String appName = appBean
												.getAppName(getContext()
														.getPackageManager());
										// 统计：应用更新--更新点击
										AppManagementStatisticsUtil
												.getInstance().saveUpdataClick(
														getContext(),
														appBean.mPkgName,
														appBean.mAppId, 1);
									}
								}
							}
						}
					} else {
						showDialog(v.getContext());
					}
					break;
				case OPERATION_TYPE_CANCEL_ALL:
					changeOperationType(OPERATION_TYPE_OPERATING_CANCEL);
					if (mUpdateListView != null) {
						AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
								.getAdapter();
						if (adapter != null) {
							List<AppBean> appBeans = adapter.getAppBeanList();
							if (appBeans != null && !appBeans.isEmpty()) {
								ApplicationManager applicationManager = AppsManagementActivity
										.getApplicationManager();
								ArrayList<Long> taskIdList = new ArrayList<Long>(
										appBeans.size());
								for (AppBean appBean : appBeans) {
									if (appBean.getStatus() == AppBean.STATUS_GET_READY
											|| appBean.getStatus() == AppBean.STATUS_WAITING_DOWNLOAD
											|| appBean.getStatus() == AppBean.STATUS_DOWNLOADING) {
										appBean.setStatus(AppBean.STATUS_CANCELING);
										if (mUpdateListView != null) {
											// mUpdateListView.updateItemStatus(-1,
											// appBean.mAppId, null, null);
											mUpdateListView
													.updateItemStatus(appBean);
											taskIdList
													.add(new Long(appBean.mAppId));
										}
										// applicationManager.cancelDownload(appBean);
									}
								}
	
								if (taskIdList.size() > 0) {
									applicationManager
											.cancelAllDownload(taskIdList);
								}
	
							}
						}
					}
					break;
				case OPERATION_TYPE_REFRESH:
					requestData();
					break;
				default:
					break;
				}
			}
		});
	}

	private void showDialog(Context context) {
		new AlertDialog.Builder(context)
				.setTitle(R.string.apps_management_update_all_dialog_title)
				.setMessage(R.string.apps_management_update_all_dialog_message)
				.setPositiveButton(
						R.string.apps_management_dialog_button_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (Machine.isNetworkOK(getContext())) {
									changeOperationType(OPERATION_TYPE_OPERATING_UPDATE);
								}
								AppsManagementActivity.getApplicationManager()
										.actionDownload(getContext(),
												AppsUpdateViewContainer.this);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
	}

	/**
	 * 进入应用更新界面时，可能有些应用在上次退出前已经点击下载，这时要将状态改为等待中
	 * 
	 * @param value
	 */
	private void setDownloadStatus(ArrayList<AppBean> allAppBeanList) {
		if (allAppBeanList != null && allAppBeanList.size() > 0) {
			int count = allAppBeanList.size();
			int size = mDownloadTasks.size();
			AppBean appBean = null;
			DownloadTask downloadTask = null;
			for (int i = 0; i < size; i++) {
				downloadTask = mDownloadTasks.get(i);
				for (int j = 0; j < count; j++) {
					appBean = allAppBeanList.get(j);
					;
					if (appBean.mAppId == downloadTask.getId()) {
						if (downloadTask.getState() == DownloadTask.STATE_WAIT) {
							appBean.setStatus(AppBean.STATUS_WAITING_DOWNLOAD);
							break;
						}
					}
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
	public void setmControlInfo(byte appIconControl, byte gostoreControl,
			byte appFuncMenuControl) {
		PreferencesManager preferences = new PreferencesManager(getContext(),
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE,
				Context.MODE_PRIVATE);
		// 功能表图标右上角
		preferences.putInt(FunControler.APPICON_SHOW_MESSSAGE, appIconControl);
		// GO Store功能表图标右上角
		preferences.putInt(FunControler.GOSTORE_SHOW_MESSAGE, gostoreControl);
		// 功能表Menu菜单
		preferences.putInt(FunControler.APPFUNC_APPMENU_SHOW_MESSAGE,
				appFuncMenuControl);
		preferences.commit();
	}

	private void updateAll() {
		AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
				.getAdapter();
		if (adapter != null) {
			List<AppBean> appBeans = adapter.getAppBeanList();
			if (appBeans != null && !appBeans.isEmpty()) {
				AppsManagementActivity.getApplicationManager().downloadAll(
						appBeans);
				adapter.notifyDataSetChanged();
			}
		}
	}

	private void sendRequest() {
		changeOperationType(OPERATION_TYPE_OPERATING_UPDATE);
		((AppsUpdateViewAdapter) mUpdateListView.getAdapter()).setDataSet(null);
		mNetworkTipsTool.showProgress();
		mUpdateListView.setVisibility(View.GONE);

		// // 请求网络数据
		// mAppsManageViewController.sendRequest(
		// AppsManageViewController.ACTION_START_REQUEST, null);
		AppsManagementActivity.sendMessage(this,
				IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
				IDiyMsgIds.REFRESH_UPDATE_DATA, 0, null, null);
	}

	/**
	 * 处理接收到的广播
	 * 
	 * @author xiedezhi
	 */
	private void handleBroadcasting(DownloadTask downlaodTask) {
		int msgId = downlaodTask.getState();
		switch (msgId) {
		case DownloadTask.STATE_WAIT:
		case DownloadTask.STATE_START:
		case DownloadTask.STATE_DOWNLOADING:
		case DownloadTask.STATE_FINISH:
		case DownloadTask.STATE_FAIL:
		case DownloadTask.STATE_DELETE:
		case DownloadTask.STATE_STOP:
			if (mUpdateListView != null) {
				if (!mIsActive && msgId == DownloadTask.STATE_DOWNLOADING) {
					return;
				}
				// final int id = (int)downlaodTask.getId();
				mUpdateListView.updateItemStatus(downlaodTask);
			}
			break;
		}
		if (msgId == DownloadTask.STATE_WAIT
				|| msgId == DownloadTask.STATE_START) {
			if (mOperationType == OPERATION_TYPE_OPERATING_UPDATE) {
				boolean has = hasGettingReadyApps();
				if (!has) {
					changeOperationType(OPERATION_TYPE_CANCEL_ALL);
				}
			}
		} else if (msgId == DownloadTask.STATE_FINISH
				|| msgId == DownloadTask.STATE_FAIL
				|| msgId == DownloadTask.STATE_DELETE) {

			if (mOperationType == OPERATION_TYPE_OPERATING_CANCEL
					|| mOperationType == OPERATION_TYPE_CANCEL_ALL) {
				boolean has = hasDownloadingAndWaitingApps();
				if (!has) {
					changeOperationType(OPERATION_TYPE_UPDATE_ALL);
				}
			}
		}
	}

	@Override
	public boolean handleMessage(Object who, int type1, final int msgId,
			final int param, final Object object, final List objects) {
		switch (msgId) {
		case IDiyMsgIds.TOP_OF_LISTVIEW:
			if (mUpdateListView != null && param >= 0) {
				mUpdateListView.setSelectionFromTop(param, 0);
			}
			break;
		case IDiyMsgIds.CHANGE_APPLIST_INFO:
				String  packageName = (String) object;
				// 从应用更新移到忽略更新列表
				if (mUpdateListView != null) {

					ApplicationManager.getInstance(getContext()).addNoUpdateApp(packageName);
					AppBean appBean = mUpdateAppBeanList.remove(param);
					appBean.mIsIngore = true;
					mIngoreAppBeanList.add(appBean);
					mUpdateListView.updateList(mUpdateAppBeanList);
					
					setIngoreCount(false);
				}
			break;
		case IDiyMsgIds.SEND_APP_TO_UPDATE_VIEW:
			if (object != null) {
				if (param == 1) {
					AppBean appBean = (AppBean) object;
					appBean.mIsIngore = false;
					appBean.mIsOpen = false;
					mUpdateAppBeanList.add(appBean);
					mIngoreAppBeanList.remove(appBean);
					ApplicationManager.getInstance(getContext()).deleteNoUpdateApp(
							appBean.mPkgName);
					if (mAppsNoUpdateView != null) {
						mAppsNoUpdateView.setmAppBeanList(mIngoreAppBeanList);
					}
				} else {
					ArrayList<AppBean> appBeanList = (ArrayList<AppBean>) object;
					for (AppBean bean : appBeanList) {
						bean.mIsOpen = false;
						bean.mIsIngore = false;
						mUpdateAppBeanList.add(bean);
					} 
					mIngoreAppBeanList.clear();
					ApplicationManager.getInstance(getContext()).deleteNoUpdateApp(null);
					if (mAppsNoUpdateView != null) {
						mAppsNoUpdateView.setmAppBeanList(mIngoreAppBeanList);
					}
				}
//				// 发消息，该表应用更新tab栏上面的更新数据
//				AppsManagementActivity.sendMessage(this,
//						IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME,
//						IDiyMsgIds.APPS_MANAGEMENT_UPDATE_COUNT,
//						mUpdateAppBeanList.size(), null, null);

				mUpdateListView.updateList(mUpdateAppBeanList);
				setIngoreCount(false);
			}
			if (hasChangeGetAllReadyState()) {
				changeOperationType(OPERATION_TYPE_REFRESH);
			} else {
				if (hasChangeCancelAllState()) {
					changeOperationType(OPERATION_TYPE_CANCEL_ALL);
				} else {
					changeOperationType(OPERATION_TYPE_UPDATE_ALL);
				}
			}

		case IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON:
			if (mOperationButton != null) {
				if (param == 1) {
					AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
							.getAdapter();
					int size = 0;
					List<AppBean> appBeans = null;
					if (adapter != null) {
						appBeans = adapter.getAppBeanList();
						if (appBeans != null) {
							size = appBeans.size();
						}
					}

					if (size > 0) {

						if (hasChangeGetAllReadyState()) {
							changeOperationType(OPERATION_TYPE_REFRESH);
						} else {
							if (hasChangeCancelAllState()) {
								changeOperationType(OPERATION_TYPE_CANCEL_ALL);
							} else {
								changeOperationType(OPERATION_TYPE_UPDATE_ALL);
							}
						}

						// if (object != null) {
						// if (object.equals(0)) {
						// changeOperationType(OPERATION_TYPE_CANCEL_ALL);
						//
						// } else {
						// changeOperationType(OPERATION_TYPE_UPDATE_ALL);
						// }
						// } else {
						// changeOperationType(OPERATION_TYPE_UPDATE_ALL);
						// }

						String suffix = getResources().getString(
								R.string.appgame_appsupdate_header_update);
						mUpdateInfoView.setText(suffix + "(" + size + ")");
						// mRefreshView.setVisibility(View.VISIBLE);
					} else {
						changeOperationType(OPERATION_TYPE_REFRESH);
						// mRefreshView.setVisibility(View.GONE);
					}
				} else if (param == 0) {
					changeOperationType(OPERATION_TYPE_REFRESH);
					// mRefreshView.setVisibility(View.GONE);
				} else if (param == 2) {
					AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
							.getAdapter();
					ArrayList<AppBean> appBeans = adapter.getAppBeanList();
					AppBean removeBean = (AppBean) object;

					// 标志是否显示文字为全部取消
					boolean flag = true;
					int count = 0;
					// 遍历所有应用的状态，因为点击更新后的应用无法立即改变状态，所以直接屏蔽掉当前选中的应用
					for (AppBean appBean : appBeans) {
						if (appBean.getStatus() == AppBean.STATUS_NORMAL
								&& appBean.mPkgName != null
								&& !appBean.mPkgName
										.equals(removeBean.mPkgName)) {
							flag = false;
						}
						if (appBean.getStatus() == AppBean.STATUS_DOWNLOAD_COMPLETED) {
							count ++;
						}
					}
					if (flag && count != appBeans.size()) {
						changeOperationType(OPERATION_TYPE_CANCEL_ALL);
					}

				}
			}
			break;
		}
		return true;
	}

	public void requestData() {
		if (Machine.isNetworkOK(getContext()) && Machine.isSDCardExist()) {
			if (!hasDownloadingAndWaitingApps()) {
				sendRequest();
			} else {
				Toast.makeText(
						getContext(),
						getContext().getString(
								R.string.apps_management_no_refresh_message),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void cleanup() {
		// unRegisterReceiver();
		if (mUpdateListView != null) {
			mUpdateListView.recycle();
		}
		if (mDownloadTasks != null) {
			mDownloadTasks = null;
		}
		AppsManagementActivity.unRegistMsgHandler(this);
	}

	@Override
	public void invokeDownload() {
		updateAll();
	}

	private void changeOperationType(int type) {
		mOperationType = type;
		switch (mOperationType) {
		case OPERATION_TYPE_UPDATE_ALL:
			// drawableId = R.drawable.appsmanagement_update_icon;
			// if (!Machine.isSDCardExist()) {
			// drawableId = R.drawable.appsmanagement_update_icon_gray;
			// }
			mOperationButton.setVisibility(VISIBLE);
			mOperationButton
					.setText(R.string.apps_management_operation_button_update_all_label);
			mOperationButton.setClickable(true);
			break;
		case OPERATION_TYPE_CANCEL_ALL:
			// drawableId = R.drawable.appsmanagement_cancel_icon;
			mOperationButton.setVisibility(VISIBLE);
			mOperationButton
					.setText(R.string.apps_management_operation_button_cancel_all_label);
			mOperationButton.setClickable(true);
			break;
		case OPERATION_TYPE_REFRESH:
			// drawableId = R.drawable.appsmanagement_refresh_icon;
			mOperationButton.setVisibility(GONE);
			mOperationButton
					.setText(R.string.apps_management_operation_button_refresh_label);
			mUpdateInfoView.setText(R.string.apps_management_none_for_update);
			mOperationButton.setClickable(true);
			break;
		case OPERATION_TYPE_OPERATING_CANCEL:
		case OPERATION_TYPE_OPERATING_UPDATE:
			mOperationButton.setVisibility(GONE);
			mOperationButton.setClickable(false);
			break;
		}
		// if (drawableId > -1) {
		// mOperationButton.setCompoundDrawablesWithIntrinsicBounds(
		// drawableId, 0, 0, 0);
		// }
	}

	private boolean hasGettingReadyApps() {
		boolean has = false;
		AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
				.getAdapter();
		if (adapter != null) {
			List<AppBean> appBeans = adapter.getAppBeanList();
			if (appBeans != null) {
				for (AppBean appBean : appBeans) {
					if (appBean.mAppId > 0
							&& appBean.getStatus() == AppBean.STATUS_GET_READY) {
						has = true;
						break;
					}
				}
			}
		}
		return has;
	}

	// 只要所有的状态都不为可更新，那么显示为全部取消
	private boolean hasChangeCancelAllState() {
		boolean has = true;
		AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
				.getAdapter();
		if (adapter != null) {
			List<AppBean> appBeans = adapter.getAppBeanList();
			if (appBeans != null) {
				for (AppBean appBean : appBeans) {
					if (appBean.getStatus() == AppBean.STATUS_NORMAL) {
						has = false;
						break;
					}
				}
			}
		}
		return has;
	}

	// 只要如果所有的状态都是安装，那么显示为刷新
	private boolean hasChangeGetAllReadyState() {
		boolean has = true;
		AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
				.getAdapter();
		if (adapter != null) {
			List<AppBean> appBeans = adapter.getAppBeanList();
			if (appBeans != null) {
				for (AppBean appBean : appBeans) {
					if (appBean.getStatus() != AppBean.STATUS_DOWNLOAD_COMPLETED) {
						has = false;
						break;
					}
				}
			}
		}
		return has;
	}

	// 只要有一个状态为可更新应用，那么就显示为全部更新
	private boolean hasChangeUpdateState() {
		boolean has = true;
		AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
				.getAdapter();
		if (adapter != null) {
			List<AppBean> appBeans = adapter.getAppBeanList();
			if (appBeans != null) {
				for (AppBean appBean : appBeans) {
					if (appBean.getStatus() == AppBean.STATUS_NORMAL) {
						has = false;
						break;
					}
				}
			}
		}
		return has;
	}

	/**
	 * <br>
	 * 功能简述: 点击全部取消时，查看是否所有下载任务都已取消 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	private boolean hasDownloadingAndWaitingApps() {
		boolean has = false;
		AppsUpdateViewAdapter adapter = (AppsUpdateViewAdapter) mUpdateListView
				.getAdapter();
		if (adapter != null) {
			List<AppBean> appBeans = adapter.getAppBeanList();
			if (appBeans != null) {
				for (AppBean appBean : appBeans) {
					if (appBean.mAppId == 0) {
						continue;
					}
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

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// menu.addSubMenu(0, IMenuHandler.MENU_ID_REFRESH, 0,
	// R.string.apps_mgr_menu_item_refresh);
	// return true;
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch( item.getItemId() ) {
	// case IMenuHandler.MENU_ID_REFRESH:
	// requestData();
	// return true;
	// }
	// return false;
	// }

	@Override
	public void sdCardTurnOff() {
		// 显示sd卡不可用的提示信息
		if (mNetworkTipsTool != null) {
			// mNetworkTipsTool.showErrorTip(NetworkTipsTool.TYPE_SDCARD_EXCEPTION);
			mNetworkTipsTool.showRetryErrorTip(null, false);
		}
		if (mUpdateListView != null) {
			mUpdateListView.setVisibility(View.GONE);
		}
		// if (mRefreshView != null) {
		// mRefreshView.setVisibility(View.GONE);
		// }
		// 改变颜色
		mOperationButton.setEnabled(false);
		// mOperationButton.setTextColor(Color.parseColor("#acacac"));
		// mOperationButton.setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.appsmanagement_update_icon_gray, 0, 0, 0);
	}

	@Override
	public void sdCardTurnOn() {
		if (mNetworkTipsTool != null) {
			mNetworkTipsTool.showNothing();
		}

		if (mUpdateListView != null) {
			if (mUpdateListView.getVisibility() == View.GONE) {
				mUpdateListView.setVisibility(View.VISIBLE);
				// requestData();
			}
		}
		mOperationButton.setEnabled(true);
		// 改变颜色
		// mOperationButton.setTextColor(Color.parseColor("#8FBE00"));
		mOperationButton
				.setText(R.string.apps_management_operation_button_update_all_label);
		// mOperationButton.setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.appsmanagement_update_icon, 0, 0, 0);
	}

	@Override
	public void onActiveChange(boolean isActive) {
		mIsActive = isActive;
		// 如果已经获取完更新数据，才在激活状态刷新界面
		if (mIsActive
				&& (mUpdateState == AppsManageViewController.MSG_ID_FINISH || mUpdateState == AppsManageViewController.MSG_ID_EXCEPTION)) {
			mUpdateListView.updateList(mUpdateAppBeanList);
		}
	}

	@Override
	public void onResume() {
		if (mUpdateListView != null) {
			mUpdateListView.updateList(mUpdateAppBeanList);
		}
	}

	@Override
	public void onAppAction(String packName, int appAction) {
		if (mUpdateListView != null) {
			// if (appAction == MainViewGroup.FLAG_INSTALL || appAction ==
			// MainViewGroup.FLAG_UNINSTALL) {
			// boolean status = false;
			// if (appAction == MainViewGroup.FLAG_INSTALL
			// || appAction == MainViewGroup.FLAG_UPDATE
			// || appAction == MainViewGroup.FLAG_UNINSTALL) {
			// status = true;
			// }
			// mUpdateListView.updateList(packName, status);
			AppBean appBean = findAppInList(packName, mAllAppBeanList);
			if (appBean != null) {
				if (appBean.mIsIngore) {
					mIngoreAppBeanList.remove(appBean);
					ApplicationManager.getInstance(getContext())
							.deleteNoUpdateApp(packName);
				} else {
					mUpdateAppBeanList.remove(appBean);
				}
				mAllAppBeanList.remove(appBean);
				
				mAppsNoUpdateView.setmAppBeanList(mIngoreAppBeanList);
				mUpdateListView.updateList(mUpdateAppBeanList);	
				setIngoreCount(false);
			}
			
		}
	}

	/**
	 * 根据包名，找出应用在list中的位置
	 * 
	 * @param packageName
	 * @param appBeanList
	 * @return
	 */
	private AppBean findAppInList(String packageName,
			ArrayList<AppBean> appBeanList) {
		int size = appBeanList.size();
		AppBean appBean = null;
		for (int i = 0; i < size; i++) {
			appBean = appBeanList.get(i);
			if (appBean.mAppId > 0 && appBean.mPkgName.equals(packageName)) {
				break;
			}
			appBean = null;
		}
		return appBean;
	}

	// /**
	// * 恢复忽略更新的应用
	// *
	// * @param packageName
	// * @param position
	// */
	// public void promptUpdate(String packageName, int position) {
	// if (mUpdateListView != null) {
	// mUpdateListView.promptUpdateApp(packageName, position);
	// }
	// }

	@Override
	public void onStop() {

	}

	@Override
	public void updateContent(ClassificationDataBean bean,
			boolean isPrevLoadRefresh) {
		if (bean == null) {
			return;
		}
		mTypeId = bean.typeId;

	}

	@Override
	public void initEntrance(int access) {

	}

	@Override
	public int getTypeId() {
		return mTypeId;
	}

	@Override
	public void onFinishAllUpdateContent() {
		if (!mIsAlreadyLoad) {
			// sdcard不存在时，加载提示页面
			if (!Machine.isSDCardExist()) {
				sdCardTurnOff();
			} else if (!Machine.isNetworkOK(getContext())) {
				showSettingTip();
				mUpdateListView.setVisibility(View.GONE);
			}
			mIsAlreadyLoad = true;
		}
	}

	/**
	 * 无网络时展示设置网络界面
	 */
	private void showSettingTip() {
		AppsManagementActivity.sendHandler("",
				IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
				IDiyMsgIds.REFRESH_WHEN_NETWORK_OK, -1, null, null);
		if (mNetworkTipsTool != null) {
			mNetworkTipsTool.showRetryErrorTip(mSettingListener, true);
			Button retryBtn = (Button) this.findViewById(R.id.retrybutton);
			retryBtn.setText(R.string.appgame_menu_item_setting);
			View tip = this.findViewById(R.id.appgame_error_nettip);
			tip.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 数据列表为空时的重试点击监听器，刷新当前tab
	 */
	private OnClickListener mRetryClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			requestData();
		}
	};

	/**
	 * 设置按钮点击事件
	 */
	private OnClickListener mSettingListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				getContext().startActivity(
						new Intent(android.provider.Settings.ACTION_SETTINGS));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void notifyDownloadState(DownloadTask downlaodTask) {
		handleBroadcasting(downlaodTask);
	}

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {
		mDownloadTasks = taskList;

	}

	@Override
	public boolean onPrepareOptionsMenu(AppGameMenu menu) {
		ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
		boolean isNeedDownloadManager = true;
		if (channelConfig != null) {
			isNeedDownloadManager = channelConfig.isNeedDownloadManager();
		}
		int resId[] = null;
		if (isNeedDownloadManager) {
			resId = new int[] { IMenuHandler.MENU_ITEM_SETTING,
					IMenuHandler.MENU_ITEM_DOWNLOAD_MANAGER,
					IMenuHandler.MENU_ITEM_FRESH,
					IMenuHandler.MENU_ITEM_FEEDBACK };
		} else {
			resId = new int[] { IMenuHandler.MENU_ITEM_SETTING,
					IMenuHandler.MENU_ITEM_FRESH,
					IMenuHandler.MENU_ITEM_FEEDBACK };
		}
		menu.setResourceId(resId);
		menu.show(this);
		return true;
	}

	@Override
	public boolean onOptionItemSelected(int id) {
		switch (id) {
		case IMenuHandler.MENU_ITEM_FRESH:
			requestData();
			return true;
		}
		return false;
	}

	@Override
	public void onTrafficSavingModeChange() {
		// do nothing
	}

	private ArrayList<AppBean> mAllAppBeanList = null;
	private ArrayList<AppBean> mUpdateAppBeanList = null;
	private ArrayList<AppBean> mIngoreAppBeanList = null;

	@Override
	public void setUpdateData(Object value, int state) {
		mUpdateState = state;
		if (mUpdateListView == null) {
			return;
		}

		mAllAppBeanList = new ArrayList<AppBean>();
		if (value != null && value instanceof AppsBean) {
			AppsBean mAppsBean = (AppsBean) value;
			// 对接口拿到的数据，不要直接操作，要生成一个新的列表存放数据 add by xiedezhi
			if (mAppsBean.mListBeans != null) {
				for (AppBean bean : mAppsBean.mListBeans) {
					mAllAppBeanList.add(bean);
				}
			}
			if (mDownloadTasks != null && mDownloadTasks.size() > 0) {
				setDownloadStatus(mAllAppBeanList);
			}
		}

		if ((mAllAppBeanList == null || mAllAppBeanList.size() <= 0)
				&& !Machine.isNetworkOK(getContext())) {
			showSettingTip();
			mUpdateListView.setVisibility(View.GONE);
		} else {
			if (state == AppsManageViewController.MSG_ID_FINISH) {
				filterNoUpdateApp(mAllAppBeanList);
				setIngoreCount(true);
			} 
			if (mAppsNoUpdateView != null) {
				mAppsNoUpdateView.setmAppBeanList(mIngoreAppBeanList);
			}
			mUpdateListView.handleRequestChange(state, mUpdateAppBeanList);
		}

		// mAllAppBeanList = new ArrayList<AppBean>();
		// if (value != null && value instanceof AppsBean) {
		// AppsBean mAppsBean = (AppsBean) value;
		// // 对接口拿到的数据，不要直接操作，要生成一个新的列表存放数据 add by xiedezhi
		// if (mAppsBean.mListBeans != null) {
		// for (AppBean bean : mAppsBean.mListBeans) {
		// mAllAppBeanList.add(bean);
		// }
		// }
		// if (mDownloadTasks != null && mDownloadTasks.size() > 0) {
		// setDownloadStatus(mAllAppBeanList);
		// }
		// }
		//
		// if (state == AppsManageViewController.MSG_ID_FINISH) {
		// ArrayList<NoPromptUpdateInfo> noUpdateAppList = getNoUpdateList();
		// if (noUpdateAppList != null) {
		// // String ingoreText = (String)
		// getContext().getText(R.string.appgame_ingore_myapp);
		// mIngoreCount = noUpdateAppList.size();
		// setIngoreCount(true);
		// }
		// }
		// if ((mAllAppBeanList == null || mAllAppBeanList.size() <= 0)
		// && !Machine.isNetworkOK(getContext())) {
		// showSettingTip();
		// mUpdateListView.setVisibility(View.GONE);
		// } else {
		// mUpdateListView.handleRequestChange(state, mAllAppBeanList);
		// }
	}

	/**
	 * 封装数据
	 * @param appListBeans
	 */
	public void filterNoUpdateApp(ArrayList<AppBean> appListBeans) {
		if (mUpdateAppBeanList == null) {
			mUpdateAppBeanList = new ArrayList<AppBean>();
		}
		mUpdateAppBeanList.clear();

		if (mIngoreAppBeanList == null) {
			mIngoreAppBeanList = new ArrayList<AppBean>();
		}
		mIngoreAppBeanList.clear();
		if (appListBeans != null && appListBeans.size() > 0) {
			for (AppBean appBean : appListBeans) {
				if (!appBean.mIsIngore) {
					mUpdateAppBeanList.add(appBean);
				} else {
					mIngoreAppBeanList.add(appBean);
				}
			}
		}

	}

	private void setIngoreCount(boolean isShowToast) {
		String ingoreText = (String) getContext().getText(
				R.string.appgame_ingore_myapp);
		int mIngoreCount = mIngoreAppBeanList == null ? 0 : mIngoreAppBeanList.size();
		if (mIngoreCount > 0) {
			if (isShowToast) {
				String text = getContext().getString(
						R.string.apps_management_some_for_no_update);
				Toast.makeText(getContext(), mIngoreCount + " " + text,
						Toast.LENGTH_SHORT).show();
			}
			mIngoreApp.setText(ingoreText + "(" + mIngoreCount + ")");
		} else {
			mIngoreApp.setText(ingoreText);
		}
	}

	private static final int COLOR_AGRONE = 255;
	private static final int COLOR_AGRTWO = 0x66;
	private static final int COLOR_AGRTHREE = 0x66;
	private static final int COLOR_AGRFOUR = 0x66;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.update_app:
			AsyncImageManager.getInstance().restore();
			mUpdateApp.setBackgroundResource(R.drawable.app_mgr_tab_left_light);
			mUpdateApp.setTextColor(Color.WHITE);
			mIngoreApp.setBackgroundResource(R.drawable.app_mgr_tab_right);
			mIngoreApp.setTextColor(Color.argb(COLOR_AGRONE, COLOR_AGRTWO,
					COLOR_AGRTHREE, COLOR_AGRFOUR));
			mAppsNoUpdateView.setVisibility(View.GONE);
			mUpdateListView.setVisibility(View.VISIBLE);
			mTabView.setVisibility(View.VISIBLE);

			if ((mUpdateAppBeanList == null || mUpdateAppBeanList.size() <= 0)
					&& !Machine.isNetworkOK(getContext())) {
				showSettingTip();
				mUpdateListView.setVisibility(View.GONE);
			} else {
				mUpdateListView.updateList(mUpdateAppBeanList);
			}

			break;
		case R.id.ingore_app:
			AsyncImageManager.getInstance().restore();
			mIngoreApp
					.setBackgroundResource(R.drawable.app_mgr_tab_right_light);
			mIngoreApp.setTextColor(Color.WHITE);

			mUpdateApp.setBackgroundResource(R.drawable.app_mgr_tab_left);
			mUpdateApp.setTextColor(Color.argb(COLOR_AGRONE, COLOR_AGRTWO,
					COLOR_AGRTHREE, COLOR_AGRFOUR));
			mUpdateListView.setVisibility(View.GONE);
			mTabView.setVisibility(View.GONE);

			mAppsNoUpdateView.setmAppBeanList(mIngoreAppBeanList);
			mAppsNoUpdateView.setVisibility(View.VISIBLE);

			break;
		default:
			break;
		}
	}

	// private ArrayList<NoPromptUpdateInfo> getNoUpdateList() {
	// ArrayList<NoPromptUpdateInfo> noUpdateAppList = ApplicationManager
	// .getInstance(getContext()).getAllNoPromptUpdateApp();
	// if (noUpdateAppList != null) {
	// return noUpdateAppList;
	//
	// } else {
	// return null;
	// }
	// }

	/**
	 * listview滑动监听器
	 */
	private OnScrollListener mScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE: {
				// 列表停止滚动时
				// 找出列表可见的第一项和最后一项
				int start = view.getFirstVisiblePosition();
				int end = view.getLastVisiblePosition();
				// 如果有添加HeaderView，要减去
				ListView lisView = null;
				if (view instanceof ListView) {
					lisView = (ListView) view;
				}
				if (lisView != null) {
					int headViewCount = lisView.getHeaderViewsCount();
					start -= headViewCount;
					end -= headViewCount;
				}
				if (end >= view.getCount()) {
					end = view.getCount() - 1;
				}
				// 对图片控制器进行位置限制设置
				AsyncImageManager.getInstance().setLimitPosition(start, end);
				// 然后解锁通知加载
				AsyncImageManager.getInstance().unlock();
			}
				break;
			case OnScrollListener.SCROLL_STATE_FLING: {
				// 列表在滚动，图片控制器加锁
				AsyncImageManager.getInstance().lock();
			}
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL: {
				// 列表在滚动，图片控制器加锁
				AsyncImageManager.getInstance().lock();
			}
				break;
			default:
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	};

	@Override
	public void fillupMultiContainer(List<CategoriesDataBean> cBeans,
			List<IContainer> containers) {
		// do nothing
	}

	@Override
	public void removeContainers() {
		// do nothing
	}

	@Override
	public List<IContainer> getSubContainers() {
		return null;
	}

	@Override
	public void onMultiVisiableChange(boolean visiable) {
		// do nothing
	}

	@Override
	public void prevLoading() {
		// do nothing
	}

	@Override
	public void prevLoadFinish() {
		// do nothing
	}
	
	@Override
	public void setBuilder(ContainerBuiler builder) {
		// do nothing
	}
}
