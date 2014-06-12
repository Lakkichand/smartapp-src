package com.go.launcher.taskmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.go.util.AppUtils;
import com.go.util.SortUtils;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.Controler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunTaskAdditionalInfo;
import com.jiubang.ggheart.data.info.FunTaskItemInfo;
import com.jiubang.ggheart.data.model.FunTaskDataModel;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 进程管理控制器
 * 
 * @author guodanyang
 * 
 */
public class TaskMgrControler extends Controler implements ICleanable {

	// 进程管理的消息子类型
	/**
	 * 单独杀死一个进程
	 */
	public final static int TERMINATE_SINGLE = 0;
	/**
	 * 一键杀进程
	 */
	public final static int TERMINATE_ALL = 1;
	/**
	 * 添加一个程序进入白名单，一键杀进程不会杀死白名单里面的程序
	 */
	public final static int ADDWHITEITEM = 2;
	/**
	 * 添加多个程序进入白名单，一键杀进程不会杀死白名单里面的程序
	 */
	public final static int ADDWHITEITEMS = 3;
	/**
	 * 删除白名单里面的一个程序
	 */
	public final static int DELETEWHITEITEM = 4;

	private AppDataEngine mAppDataEngine;

	/**
	 * 白名单列表
	 */
	private ConcurrentHashMap<ComponentName, FunTaskAdditionalInfo> mTaskHashMap;

	private FunTaskDataModel mFunTaskDataModel;

	/**
	 * 进程监控
	 */
	private ITaskManager mITaskManager;

	public TaskMgrControler(Context context, AppDataEngine appDataEngine) {
		super(context);
		mAppDataEngine = appDataEngine;
		mFunTaskDataModel = new FunTaskDataModel(context);
		initTaskHashMap();
		// 先初始化
		getTaskManager();
	}

	public ITaskManager getTaskManager() {
		try {
			if (mITaskManager == null) {
				mITaskManager = TaskManager.getInstance(mContext);
			}
		} catch (StackOverflowError e) {
		}
		return mITaskManager;
	}

	/**
	 * 获取进程信息列表
	 * 
	 * @return 列表
	 */
	public List<_APPINFOR> retriveAppList() {
		return getTaskManager().retriveAppList();
	}

	/**
	 * 获取进程信息列表
	 * 
	 * @return 列表
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<FunTaskItemInfo> getProgresses() {
		if (null == mAppDataEngine) {
			return null;
		}

		ArrayList<_APPINFOR> tasks = (ArrayList<_APPINFOR>) getTaskManager().retriveAppList()
				.clone();
		if (null == tasks) {
			return null;
		}

		ArrayList<FunTaskItemInfo> apps = new ArrayList<FunTaskItemInfo>();
		AppItemInfo appItemInfo = null;
		FunTaskItemInfo funTaskItemInfo = null;
		_APPINFOR info = null;
		Intent intent = null;
		int size = tasks.size();
		for (int i = 0; i < size; ++i) {
			info = tasks.get(i);
			if (null == info) {
				continue;
			}

			// test
			if (null == info._infor) {
				continue;
			}
			intent = info._infor;
			if (null == intent) {
				continue;
			}

			appItemInfo = mAppDataEngine.getAppItemExceptHide(intent);
			if (appItemInfo != null) {
				funTaskItemInfo = new FunTaskItemInfo(appItemInfo);
				funTaskItemInfo.setPid(info._pid);
				funTaskItemInfo.setIsInWhiteList(isIgnoreTask(intent));
				apps.add(funTaskItemInfo);
			}
		}

		return apps;
	}

	/**
	 * 结束进程
	 * 
	 * @param pid
	 *            进程的唯一标识
	 */
	public void terminateApp(final int pid) {
		getTaskManager().terminateAppByPid(pid);
		// 通知
		broadCast(TERMINATE_SINGLE, pid, null, null);
	}

	private void terminateTaskItemInfos(ArrayList<FunTaskItemInfo> infos) {
		try {
			if (infos != null) {
				int size = infos.size();
				for (int i = 0; i < size; i++) {
					FunTaskItemInfo info = infos.get(i);
					if (info == null) {
						continue;
					}

					// 若是在白名单中
					if (info.isInWhiteList()) {
						continue;
					}
					getTaskManager().terminateAppByPid(info.getPid());
					// terminateApp(info.getPid());
				}
			}
		} catch (Error error) {
			// TODO: handle exception
		} catch (Exception exception) {
			// TODO: handle exception
		}
	}

	public void terminateAll(ArrayList<FunTaskItemInfo> infos) {
		terminateTaskItemInfos(infos);
		// 通知
		broadCast(TERMINATE_ALL, 0, null, null);
	}

	public void leavingRunningTab() {
		getTaskManager().clearTaskManager();
	}

	/**
	 * 结束所有进程
	 */
	public void terminateAll() {
		ArrayList<FunTaskItemInfo> infos = getProgresses();
		terminateTaskItemInfos(infos);
		// 通知
		broadCast(TERMINATE_ALL, 0, null, null);
	}

	/**
	 * 获取总内存
	 * 
	 * @return
	 */
	public long retriveTotalMemory() {
		return getTaskManager().retriveTotalMemory();
	}

	/**
	 * 获取可用内存
	 * 
	 * @return
	 */
	public long retriveAvailableMemory() {
		return getTaskManager().retriveAvailableMemory();
	}

	/**
	 * 初始化白名单数据
	 */
	private void initTaskHashMap() {
		if (mTaskHashMap != null) {
			mTaskHashMap.clear();
		} else {
			mTaskHashMap = new ConcurrentHashMap<ComponentName, FunTaskAdditionalInfo>();
		}

		// 获取白名单的程序
		ArrayList<FunTaskAdditionalInfo> funTaskAdditionalInfos = mFunTaskDataModel
				.getAllIgnoreTaskAppItems();
		if (funTaskAdditionalInfos == null || funTaskAdditionalInfos.size() <= 0) {
			return;
		}

		int size = funTaskAdditionalInfos.size();
		for (int i = 0; i < size; i++) {
			FunTaskAdditionalInfo info = funTaskAdditionalInfos.get(i);
			final Intent intent = info.getIntent();
			if (intent != null) {
				final ComponentName cn = intent.getComponent();
				if (cn != null) {
					mTaskHashMap.put(cn, info);
				}
			}
		}
	}

	/**
	 * 添加忽略的应用程序
	 * 
	 * @author huyong
	 * @param intent
	 */
	public void addIgnoreAppItem(final Intent intent) {
		if (intent == null) {
			return;
		}
		final ComponentName cn = intent.getComponent();
		if (cn == null || mTaskHashMap.containsKey(cn)) {
			return;
		}
		FunTaskAdditionalInfo info = mFunTaskDataModel.addIgnoreAppItem(intent);
		if (info != null) {
			mTaskHashMap.put(cn, info);
			// 通知
			broadCast(ADDWHITEITEM, 0, intent, null);
		}
		Log.i("pl", "in_addIgnoreAppItem");
	}

	/**
	 * 取消忽略的应用程序
	 * 
	 * @author huyong
	 * @param intent
	 */
	public void delIgnoreAppItem(final Intent intent) {
		if (intent == null) {
			return;
		}
		final ComponentName cn = intent.getComponent();
		if (cn == null) {
			return;
		}
		mFunTaskDataModel.delTaskAppItem(intent);
		mTaskHashMap.remove(cn);

		// 通知
		broadCast(DELETEWHITEITEM, 0, intent, null);
		Log.i("pl", "in_delIgnoreAppItem");
	}

	/**
	 * 批量保存忽略的应用程序
	 * 
	 * @author huyong
	 * @param appItems
	 */
	public void saveIgnoreAppItems(final ArrayList<Intent> appItems) {
		if (appItems == null) {
			return;
		}
		// 1.首先清除数据库中原有的部分
		// 2.重新初始化HashMap
		// 3.逐个添加新的intent
		mFunTaskDataModel.clearAllIgnoreTaskAppItems();
		initTaskHashMap();
		int size = appItems.size();
		for (int i = 0; i < size; i++) {
			Intent intent = appItems.get(i);
			addIgnoreAppItem(intent);
		}

		// 通知
		broadCast(ADDWHITEITEMS, 0, null, appItems);
	}

	/**
	 * 通过intent判定程序是否被设置成被忽略
	 * 
	 * @author huyong
	 * @param intent
	 * @return
	 */
	public boolean isIgnoreTask(final Intent intent) {
		boolean result = false;
		if (intent == null) {
			return result;
		}
		final ComponentName cn = intent.getComponent();
		if (cn == null) {
			return result;
		}
		FunTaskAdditionalInfo funTaskAdditionalInfo = mTaskHashMap.get(cn);
		if (funTaskAdditionalInfo != null) {
			if (funTaskAdditionalInfo.getIsIgnore() == 1) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * 获取所有应用程序，并按照在白名单中的程序在前，不在白名单中的程序在后的顺序来排列
	 * 
	 * @author huyong
	 * @return
	 */
	public ArrayList<AppItemInfo> getAllAppItemInfos() {
		if (null == mAppDataEngine) {
			return null;
		}

		ConcurrentHashMap<ComponentName, AppItemInfo> tmpAllAppsMap = null;
		try {
			tmpAllAppsMap = mAppDataEngine.getAllAppHashMap();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		HashMap<ComponentName, AppItemInfo> allAppsMap = new HashMap<ComponentName, AppItemInfo>(
				tmpAllAppsMap);
		ArrayList<AppItemInfo> itemInfos = new ArrayList<AppItemInfo>();

		// 首先从白名单中获取程序信息
		Iterator<Entry<ComponentName, FunTaskAdditionalInfo>> it = mTaskHashMap.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<ComponentName, FunTaskAdditionalInfo> entry = it.next();
			ComponentName cn = entry.getKey();
			AppItemInfo appItemInfo = allAppsMap.remove(cn);
			if (appItemInfo != null) {
				itemInfos.add(appItemInfo);
			}
		}

		int middle = itemInfos.size();
		Iterator<Entry<ComponentName, AppItemInfo>> iter = allAppsMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<ComponentName, AppItemInfo> entry = iter.next();
			AppItemInfo appItemInfo = entry.getValue();
			itemInfos.add(appItemInfo);
		}

		// 分别对两个区段进行排序
		try {
			String sortMethod = "getTitle";
			String order = "ASC";
			SortUtils.sort(itemInfos.subList(0, middle), sortMethod, null, null, order);
			SortUtils.sort(itemInfos.subList(middle, itemInfos.size()), sortMethod, null, null,
					order);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return itemInfos;
	}

	/**
	 * 通过包路径，启动程序info的信息页面
	 * 
	 * @param packageName
	 *            包路径
	 */
	public void skipAppInfobyPackageName(String packageName) {
		if (null == packageName) {
			return;
		}
		Intent in = new Intent(Intent.ACTION_VIEW);
		ComponentName com = new ComponentName("com.android.settings",
				"com.android.settings.InstalledAppDetails");
		in.setComponent(com);
		in.putExtra("com.android.settings.ApplicationPkgName", packageName);
		in.putExtra("pkg", packageName);
		in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// 这里可以考虑catch住
		try {
			mContext.startActivity(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 通过Intent，启动程序info的信息页面
	 * 
	 * @param intent
	 */
	public void skipAppInfobyIntent(Intent intent) {
		if (null == intent) {
			return;
		}
		String packageName = intent.getComponent().getPackageName();
		if (null == packageName) {
			return;
		}

		Intent in = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) { // 2.3（ApiLevel 9）以上，使用SDK提供的接口
								// 也可直接使用2.3sdk中的android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
			in.setAction(ICustomAction.ACTION_SETTINGS);
			Uri uri = Uri.fromParts("package", packageName, null);
			in.setData(uri);
		} else {
			in.setAction(Intent.ACTION_VIEW);
			ComponentName com = new ComponentName("com.android.settings",
					"com.android.settings.InstalledAppDetails");
			in.setComponent(com);
			in.putExtra("com.android.settings.ApplicationPkgName", packageName);
			in.putExtra("pkg", packageName);
		}
		in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// 这里可以考虑catch住
		AppUtils.safeStartActivity(mContext, in);
	}

	@Override
	public void cleanup() {
		clearAllObserver();
	}
	
	/**
	 * 清除所有正在运行程序（白名单程序除外）
	 */
	public void terminateAllProManageTask(ArrayList<FunAppItemInfo> infos) {
		try {
			if (infos != null) {
				int size = infos.size();
				for (int i = 0; i < size; i++) {
					FunAppItemInfo info = infos.get(i);
					if (info == null) {
						continue;
					}
					// 若是在白名单中
					if (info.isIgnore()) {
						continue;
					}
					getTaskManager().terminateAppByPid(info.getPid());
				}
			}
		} catch (Error error) {
		} catch (Exception ex) {
		}
	}
}
