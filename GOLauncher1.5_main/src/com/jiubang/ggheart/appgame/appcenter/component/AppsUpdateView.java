package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.contorler.AppsManageViewController;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.components.DeskToast;

/**
 * 
 * <br>
 * 类描述: 应用更新界面 <br>
 * 功能详细描述:
 * 
 * @author zhoujun
 * @date [2012-10-11]
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
	//	private ArrayList<AppBean> mNoUpdateAppBeanList;
	private AppsUpdateViewAdapter mAppsManageViewAdapter = null;
	private NetworkTipsTool mNetworkTipsTool;
	private Context mContext;
	private OnClickListener mRetryClickListener;

	public AppsUpdateView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public AppsUpdateView(Context context, AttributeSet attr) {
		super(context, attr);
		mContext = context;
		init();

	}

	private void init() {
		initView();
	}

	// this.setOnItemLongClickListener(new OnItemLongClickListener() {
	// @Override
	// public boolean onItemLongClick(AdapterView<?> parent, View view,
	// int position, long id) {
	// if (view instanceof AppsUpdateInfoListItem) {
	// AppBean appBean = (AppBean) view.getTag();
	// if (appBean.getStatus() == AppBean.STATUS_NORMAL
	// || appBean.getStatus() == AppBean.STATUS_DOWNLOAD_COMPLETED) {
	//
	// // 显示弹出框，选择不提示更新
	// showNoUpdateDialog(appBean,position);
	// }
	// }
	// return false;
	// }
	// });
	// }

	/**
	 * 初始化列表的方法
	 */
	private void initView() {
		mAppsManageViewAdapter = new AppsUpdateViewAdapter(getContext(), getAppInfoData());
		mAppsManageViewAdapter.setDefaultIcon(getResources().getDrawable(R.drawable.default_icon));
		setAdapter(mAppsManageViewAdapter);
		// this.setSelector(R.drawable.recomm_app_list_item_selector);
	}

	// /**
	// * 初始化列表的方法
	// */
	// private void initView() {
	// // Resources resources = getResources();
	// // mItemSelector = resources
	// // .getDrawable(R.drawable.themestore_item_selected);
	// // mListDivLine = resources
	// // .getDrawable(R.drawable.themestore_list_item_line);
	// // setSelector(mItemSelector);
	// // setDivider(mListDivLine);
	// mAppsManageViewAdapter = new AppsUpdateViewAdapter(getContext(),
	// getAppInfoData());
	// setAdapter(mAppsManageViewAdapter);
	// // setOnItemClickListener(new OnItemClickListener() {
	// //
	// // @Override
	// // public void onItemClick(AdapterView<?> parent, View view,
	// // int position, long id) {
	// // }
	// // });
	// // this.setDivider(null);
	// this.setSelector(R.drawable.recomm_app_list_item_selector);
	// }

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
			// mAppBeanList = null;
		}
		//		if (mNoUpdateAppBeanList != null) {
		//			mNoUpdateAppBeanList.clear();
		//			// mNoUpdateAppBeanList = null;
		//		}
		setAdapter(null);
		setOnItemClickListener(null);
		mRetryClickListener = null;
	}

	protected void handleRequestChange(int state, ArrayList<AppBean> appBeanList) {
		switch (state) {

			case AppsManageViewController.MSG_ID_START : {
				this.setVisibility(View.GONE);
				mNetworkTipsTool.showProgress();
			}
				break;

			case AppsManageViewController.MSG_ID_FINISH : {
				onLoadIsFinished(appBeanList);
				break;
			}

			case AppsManageViewController.MSG_ID_EXCEPTION : {
				mNetworkTipsTool.dismissProgress();
				mNetworkTipsTool.showRetryErrorTip(mRetryClickListener, true);
				this.setVisibility(View.GONE);
				AppsManagementActivity.sendMessage(this,
						IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
						IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 0, null, null);
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
	private void onLoadIsFinished(ArrayList<AppBean> appBeanList) {
		if (appBeanList != null) {
			mAppBeanList = appBeanList;
			// ArrayList<AppBean> allAppBeanList = appBeanList;
			// if (allAppBeanList != null) {

			int size = mAppBeanList.size();
//			if (mAppBeanList != null) {
//				size = mAppBeanList.size();
//			}
//			// 保存现在可更新应用的个数
//			saveUpdateCount(size);
//			// 修改应用更新tab头上更新的数字
//			AppsManagementActivity.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME,
//					IDiyMsgIds.APPS_MANAGEMENT_UPDATE_COUNT, size, null, null);

			if (mAppsManageViewAdapter != null && size > 0) {
				mNetworkTipsTool.removeProgress();
				this.setVisibility(View.VISIBLE);
				mAppsManageViewAdapter.setDataSet(mAppBeanList);

				boolean flag = true;
				for (AppBean appBean : mAppBeanList) {
					if (appBean.getStatus() == AppBean.STATUS_NORMAL) {
						flag = false;
					}
				}
				if (flag) {
					AppsManagementActivity.sendMessage(this,
							IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
							IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 1, 0, null);
				} else {
					AppsManagementActivity.sendMessage(this,
							IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
							IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 1, 1, null);
				}
				return;
			} else {
				mNetworkTipsTool.showRetryErrorTip(mRetryClickListener,
						NetworkTipsTool.TYPE_NO_UPDATE);
				this.setVisibility(View.GONE);
				AppsManagementActivity.sendMessage(this,
						IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
						IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 0, null, null);
			}
		} else {
			mNetworkTipsTool.showRetryErrorTip(mRetryClickListener, NetworkTipsTool.TYPE_NO_UPDATE);
			this.setVisibility(View.GONE);
			AppsManagementActivity.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
					IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 0, null, null);
		}
		// mNetworkTipsTool.showNoUpdateDataTip();
		// AppsManagementActivity.sendMessage(this,
		// IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
		// IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 0, null,
		// null);
	}

	private void saveUpdateCount(int size) {
		// 保存现在可更新应用的个数
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		preferences.putInt(FunControler.GOSTORECOUNT, size);
		preferences.commit();
	}

	// /**
	// * 过滤忽略更新的应用
	// */
	// public void filterNoUpdateApp(ArrayList<AppBean> appListBeans) {
	// // ArrayList<NoPromptUpdateInfo> noUpdateAppList =
	// // mNoUpdateModel.getAllNoPromptUpdateApp();
	// ArrayList<NoPromptUpdateInfo> noUpdateAppList = ApplicationManager
	// .getInstance(getContext()).getAllNoPromptUpdateApp();
	// mNoUpdateAppBeanList = new ArrayList<AppBean>();
	// if (appListBeans != null && appListBeans.size() > 0) {
	// if (noUpdateAppList != null && noUpdateAppList.size() > 0) {
	// for (NoPromptUpdateInfo noUpdateInfo : noUpdateAppList) {
	// String packageName = getPkgNameByIntent(noUpdateInfo
	// .getIntent());
	// if (packageName != null) {
	// for (AppBean appBean : appListBeans) {
	// if (packageName.equals(appBean.mPkgName)) {
	// appBean.mIsIngore = true;
	// mNoUpdateAppBeanList.add(appBean);
	// break;
	// }
	// }
	// }
	// }
	// } else {
	// mAppBeanList = appListBeans;
	// }
	//
	// if (mAppBeanList.size() == 0) {
	// for (AppBean appBean : appListBeans) {
	// if (!mNoUpdateAppBeanList.contains(appBean)) {
	// appBean.mIsIngore = false;
	// mAppBeanList.add(appBean);
	// }
	// }
	// }
	// mAppBeanList.removeAll(mNoUpdateAppBeanList);
	// if (mAppBeanList.size() == 0) {
	// mNetworkTipsTool.showRetryErrorTip(mRetryClickListener,
	// NetworkTipsTool.TYPE_NO_UPDATE);
	// this.setVisibility(View.GONE);
	// AppsManagementActivity.sendMessage(this,
	// IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
	// IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 0, null,
	// null);
	// }
	//
	// }
	//
	// }

	//	/**
	//	 * 过滤忽略更新的应用
	//	 */
	//	public void filterNoUpdateApp(ArrayList<AppBean> appListBeans) {
	//		mNoUpdateAppBeanList = new ArrayList<AppBean>();
	//		if (mAppBeanList == null) {
	//			mAppBeanList = new ArrayList<AppBean>();
	//		}
	//		mAppBeanList.clear();
	//		if (appListBeans != null && appListBeans.size() > 0) {
	//			for (AppBean appBean : appListBeans) {
	//				if (appBean.mIsIngore) {
	//					mNoUpdateAppBeanList.add(appBean);
	//				} else {
	//					mAppBeanList.add(appBean);
	//				}
	//			}
	//
	//			if (mAppBeanList.size() == 0) {
	//				mNetworkTipsTool.showRetryErrorTip(mRetryClickListener,
	//						NetworkTipsTool.TYPE_NO_UPDATE);
	//				this.setVisibility(View.GONE);
	//				AppsManagementActivity.sendMessage(this,
	//						IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
	//						IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 0, null,
	//						null);
	//			}
	//
	//		}
	//
	//	}

//	/**
//	 * 根据intent获取包名
//	 * 
//	 * @param intent
//	 * @return
//	 */
//	private String getPkgNameByIntent(Intent intent) {
//		String packageName = null;
//		if (intent != null) {
//			ComponentName comonentName = intent.getComponent();
//			if (comonentName != null) {
//				packageName = comonentName.getPackageName();
//			}
//		}
//		return packageName;
//	}

	public void setTipTool(NetworkTipsTool networkTipsTool, OnClickListener listener) {
		mNetworkTipsTool = networkTipsTool;
		mRetryClickListener = listener;
	}

	public void updateItemStatus(DownloadTask downloadTask) {
		int msgId = downloadTask.getState();
		int id = (int) downloadTask.getId();

		List<AppBean> appBeans = getAppInfoData();
		if (appBeans != null) {
			for (AppBean appBean : appBeans) {
				if (appBean.mAppId == id) {
					switch (msgId) {
						case DownloadTask.STATE_WAIT :
							appBean.setStatus(AppBean.STATUS_WAITING_DOWNLOAD);
							break;
						case DownloadTask.STATE_DOWNLOADING :
							appBean.setStatus(AppBean.STATUS_DOWNLOADING);
							appBean.setAlreadyDownloadSize(downloadTask.getAlreadyDownloadSize());
							appBean.setAlreadyDownloadPercent(downloadTask
									.getAlreadyDownloadPercent());
							break;

						case DownloadTask.STATE_FINISH :
							String saveFilePath = downloadTask.getSaveFilePath();
							appBean.setFilePath(saveFilePath);
							appBean.setStatus(AppBean.STATUS_DOWNLOAD_COMPLETED);
							appBean.setAlreadyDownloadSize(downloadTask.getAlreadyDownloadSize());
							appBean.setAlreadyDownloadPercent(downloadTask
									.getAlreadyDownloadPercent());
							// 统计:应用更新--更新完成
							// 不需要在此处做下载完成统计，在DefaultDownloadManagerListener中已经做了统计，避免重复
							// AppManagementStatisticsUtil.getInstance()
							// .saveUpdataComplete(mContext, appBean.mPkgName,
							// String.valueOf(appBean.mAppId), 1);
							// AppsManagementActivity.sendHandler(this,
							// IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
							// IDiyMsgIds.APPS_MANAGEMENT_INSTALL_APP, 0,
							// saveFilePath,
							// null);
							break;
						case DownloadTask.STATE_DELETE :
							appBean.setAlreadyDownloadSize(0);
							appBean.setAlreadyDownloadPercent(0);
							appBean.setStatus(AppBean.STATUS_NORMAL);
							break;
						case DownloadTask.STATE_FAIL :
							appBean.setStatus(AppBean.STATUS_DOWNLOAD_FAILED);
							appBean.setAlreadyDownloadSize(0);
							appBean.setAlreadyDownloadPercent(0);
							DeskToast.makeText(getContext(),
									R.string.apps_management_network_error, Toast.LENGTH_SHORT)
									.show();
							break;
						case DownloadTask.STATE_STOP :
							appBean.setStatus(AppBean.STATUS_STOP);
							appBean.setAlreadyDownloadSize(downloadTask.getAlreadyDownloadSize());
							appBean.setAlreadyDownloadPercent(downloadTask
									.getAlreadyDownloadPercent());
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
	// public void showNoUpdateDialog(final AppBean appBean, final int position)
	// {
	// PackageManager pm = getContext().getPackageManager();
	// AlertDialog.Builder builder = new DeskBuilder(getContext());
	//
	// builder.setTitle(appBean.getAppName(pm));
	//
	// CharSequence[] data = getContext().getResources().getTextArray(
	// R.array.apps_no_prompt_update_dialog_style);
	// if (null != data && data.length > 0) {
	// ArrayAdapter<CharSequence> adapter = new
	// ArrayAdapter<CharSequence>(getContext(),
	// R.layout.app_no_pro_list_item, R.id.apps_no_pro_text, data);
	// builder.setAdapter(adapter, null);
	// }
	//
	// builder.setSingleChoiceItems(R.array.select_sort_style, 0,
	// new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int item) {
	// dialog.dismiss();
	// addNoUpdateApp(appBean.mPkgName, position);
	// }
	// });
	// try {
	// builder.show();
	// } catch (Exception e) {
	// try {
	// DeskToast.makeText(getContext(), R.string.alerDialog_error,
	// Toast.LENGTH_SHORT)
	// .show();
	// } catch (OutOfMemoryError error) {
	// }
	// }
	// }
	/**
	 * 显示忽略更新的弹出框
	 * 
	 * @param appBean
	 * @param position
	 */
	// public void showNoUpdateDialog(final AppBean appBean, final int position)
	// {
	// PackageManager pm = getContext().getPackageManager();
	// AlertDialog.Builder builder = new DeskBuilder(getContext());
	//
	// builder.setTitle(appBean.getAppName(pm));
	//
	// CharSequence[] data = getContext().getResources().getTextArray(
	// R.array.apps_no_prompt_update_dialog_style);
	// if (null != data && data.length > 0) {
	// ArrayAdapter<CharSequence> adapter = new
	// ArrayAdapter<CharSequence>(getContext(),
	// R.layout.app_no_pro_list_item, R.id.apps_no_pro_text, data);
	// builder.setAdapter(adapter, null);
	// }
	//
	// builder.setSingleChoiceItems(R.array.select_sort_style, 0,
	// new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int item) {
	// dialog.dismiss();
	// addNoUpdateApp(appBean.mPkgName, position);
	// }
	// });
	// try {
	// builder.show();
	// } catch (Exception e) {
	// try {
	// DeskToast.makeText(getContext(), R.string.alerDialog_error,
	// Toast.LENGTH_SHORT)
	// .show();
	// } catch (OutOfMemoryError error) {
	// }
	// }
	// }

	/**
	 * 添加忽略更新的应用
	 * 
	 * @param packageName
	 */
	//	public void addNoUpdateApp(String packageName, int position) {
	//		ApplicationManager.getInstance(getContext())
	//				.addNoUpdateApp(packageName);
	//
	//		moveListData(position, mAppBeanList, mNoUpdateAppBeanList);
	//
	//		// 重新设置更新信息
	//		resetUpdateInfo();
	//	}
	//	public void addNoUpdateApp(int position) {
	//		mAppBeanList.remove(position);
	////		moveListData(position, mAppBeanList, mNoUpdateAppBeanList);
	//		// 重新设置更新信息
	//		resetUpdateInfo();
	//	}

	//	/**
	//	 * 恢复更新的应用
	//	 * 
	//	 * @param packageName
	//	 */
	//	public void promptUpdateApp(String packageName, int position) {
	//		// ApplicationManager.getInstance(getContext()).deleteNoUpdateApp(
	//		// packageName);
	//		if (packageName == null) {
	//			// 恢复所有更新
	//
	//			moveListData(mNoUpdateAppBeanList, mAppBeanList);
	//
	//		} else {
	//
	//			moveListData(position, mNoUpdateAppBeanList, mAppBeanList);
	//
	//		}
	//		// 重新设置更新信息
	//		resetUpdateInfo();
	//	}

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
		resetUpdateInfo();
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
			}
			//			// 查找卸载的应用，是否在忽略更新里面
			//			if (mNoUpdateAppBeanList != null) {
			//				position = findAppInList(packageName, mNoUpdateAppBeanList);
			//				if (position != -1) {
			//					mNoUpdateAppBeanList.remove(position);
			//					ApplicationManager.getInstance(getContext())
			//							.deleteNoUpdateApp(packageName);
			//				}
			//			}
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
	 * 从忽略更新页面返回时，刷新更新列表页面
	 */
	public void updateList(ArrayList<AppBean> appBeanList) {
		if (mAppsManageViewAdapter != null) {
			mAppsManageViewAdapter.setDataSet(appBeanList);
			mAppsManageViewAdapter.notifyDataSetChanged();
		}
		resetUpdateInfo();
	}

	/**
	 * 重置更新信息
	 */
	private void resetUpdateInfo() {
		int size = 0;
		if (mAppBeanList != null) {
			size = mAppBeanList.size();
			// 保存现在可更新应用的个数
			saveUpdateCount(size);

			// 好像功能表菜单键，应用管理的更新数据 是从这里获取的。
			// AppsManagementActivity.getApplicationManager().setUpdateAppsCount(size);

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
					this.setVisibility(View.VISIBLE);
				}
				mAppsManageViewAdapter.notifyDataSetChanged();
			}
		} else {
			if (mAppsManageViewAdapter != null) {
				mAppsManageViewAdapter.setDataSet(null);
				if (mNetworkTipsTool != null) {
					mNetworkTipsTool.showRetryErrorTip(mRetryClickListener,
							NetworkTipsTool.TYPE_NO_UPDATE);
				}
				this.setVisibility(View.GONE);
			}
		}

		// 改变操作按钮上，可以更新的数据,这里为什么不直接把size发过去呢？？
		AppsManagementActivity.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
				IDiyMsgIds.APPS_MANAGEMENT_OPERATION_BUTTON, 1, null, null);

		// 发消息，该表应用更新tab栏上面的更新数据
		AppsManagementActivity.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME,
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
			if (appBean.mAppId > 0 && appBean.mPkgName.equals(packageName)) {
				position = i;
				break;
			}
		}
		return position;
	}

	public void setmRetryClickListener(OnClickListener retryClickListener) {
		mRetryClickListener = retryClickListener;
	}

	//	public ArrayList<AppBean> getmNoUpdateAppBeanList() {
	//		return mNoUpdateAppBeanList;
	//	}
}