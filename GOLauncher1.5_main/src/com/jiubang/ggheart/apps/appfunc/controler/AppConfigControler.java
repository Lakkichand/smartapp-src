package com.jiubang.ggheart.apps.appfunc.controler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.go.util.SortUtils;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.Controler;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.info.AppConfigInfo;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.model.AppConfigDataModel;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * @author yangguanxiang
 *
 */
public class AppConfigControler extends Controler implements ICleanable {
	
	private static AppConfigControler sInstance;
	
	public final static int ADDHIDEITEM = 0;
	public final static int ADDHIDEITEMS = 1;
	public final static int DELETEHIDEITEM = 2;
	public final static int DELETEHIDEITEMS = 3; // TODO:
	public final static int HIDEAPPCHANG = 4;

	private AppDataEngine mAppDataEngine;
	private AppConfigDataModel mAppConfigDataModel;
	private ConcurrentHashMap<ComponentName, AppConfigInfo> mHideHashMap;

	public synchronized static AppConfigControler getInstance(Context context) {
		if (sInstance == null && context != null) {
			sInstance = new AppConfigControler(context);
		}
		return sInstance;
	}

	private AppConfigControler(Context context) {
		super(context);

		mAppDataEngine = GOLauncherApp.getAppDataEngine();
		mAppConfigDataModel = new AppConfigDataModel(context);

		// TODO:刚进入桌面是的安装卸载比对
		initHideHashMap();
	}

	/**
	 * 初始化隐藏程序数据
	 */
	private void initHideHashMap() {
		if (mHideHashMap != null) {
			mHideHashMap.clear();
			mHideHashMap = null;
		}
		mHideHashMap = new ConcurrentHashMap<ComponentName, AppConfigInfo>();

		ArrayList<AppConfigInfo> appConfigInfos = mAppConfigDataModel.getAllHideAppItems();
		if (appConfigInfos == null || appConfigInfos.size() <= 0) {
			return;
		}
		int size = appConfigInfos.size();
		for (int i = 0; i < size; i++) {
			AppConfigInfo info = appConfigInfos.get(i);
			Intent intent = info.getIntent();
			if (intent == null) {
				continue;
			}
			final ComponentName cn = intent.getComponent();
			if (cn != null) {
				mHideHashMap.put(cn, info);
			}
		}
		appConfigInfos.clear();
		appConfigInfos = null;
	}

	/**
	 * 添加隐藏的应用程序
	 * 
	 * @author guodanyang
	 * @param intent
	 * @throws DatabaseException
	 */
	public void addHideAppItem(final Intent intent, boolean notify) throws DatabaseException {
		if (intent == null) {
			return;
		}
		final ComponentName cn = intent.getComponent();
		if (cn == null || mHideHashMap.containsKey(cn)) {
			return;
		}
		AppConfigInfo info = mAppConfigDataModel.addHideAppItem(intent);
		if (info != null) {
			mHideHashMap.put(cn, info);
			if (notify) {
				// 通知
				broadCast(ADDHIDEITEM, 0, intent, null);
			}
			
		}
	}

	/**
	 * 取消隐藏的应用程序
	 * 
	 * @author guodanyang
	 * @param intent
	 * @throws DatabaseException
	 */
	public void delHideAppItem(final Intent intent, boolean notify) throws DatabaseException {
		if (intent == null) {
			return;
		}
		final ComponentName cn = intent.getComponent();
		if (cn == null) {
			return;
		}
		mAppConfigDataModel.delHideAppItem(intent);
		AppConfigInfo info = mHideHashMap.remove(cn);
		if (info != null) {
			info.setHide(false);
			info = null;
		}
		if (notify) {
			// 通知
			broadCast(DELETEHIDEITEM, 0, intent, null);
		}
	}

	/**
	 * 批量保存隐藏的应用程序
	 * 
	 * @author guodanyang
	 * @param appItems
	 * @throws DatabaseException
	 */
	public void addHideAppItems(final ArrayList<Intent> appItems, final boolean notify)
			throws DatabaseException {
		if (appItems == null) {
			return;
		}
		// 1.首先清除数据库中原有的部分
		// 2.重新初始化hashmap
		// 3.逐个添加新的intent
		Iterator<Entry<ComponentName, AppConfigInfo>> iter = mHideHashMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<ComponentName, AppConfigInfo> entry = iter.next();
			AppConfigInfo val = entry.getValue();
			delHideAppItem(val.getIntent(), false);
		}
		int size = appItems.size();
		for (int i = 0; i < size; i++) {
			Intent intent = appItems.get(i);
			addHideAppItem(intent, false);
		}
		
		if (notify) {
			// 通知
			broadCast(ADDHIDEITEMS, 0, null, appItems);
		}
	}

	/**
	 * 通过intent判定程序是否被设置成被隐藏
	 * 
	 * @author guodanyang
	 * @param intent
	 * @return
	 */
	public boolean isHideApp(final Intent intent) {
		boolean result = false;
		if (intent == null) {
			return result;
		}

		final ComponentName cn = intent.getComponent();
		if (cn == null) {
			return result;
		}
		AppConfigInfo appConfigInfo = mHideHashMap.get(cn);
		if (appConfigInfo != null) {
			return appConfigInfo.getHide();
		}

		return result;
	}
	
	public AppConfigInfo getHideInfo(Intent intent) {
		if (intent == null) {
			return null;
		}

		final ComponentName cn = intent.getComponent();
		if (cn == null) {
			return null;
		}
		return mHideHashMap.get(cn);
	}

	/**
	 * 获取所有应用程序，并按照在隐藏程序中的程序在前，不在隐藏程序中的程序在后的顺序来排列
	 * 
	 * @author guodanyang
	 * @return
	 */
	public ArrayList<AppItemInfo> getAllAppItemInfos() {
		ConcurrentHashMap<ComponentName, AppItemInfo> allAppsMap = new ConcurrentHashMap<ComponentName, AppItemInfo>();
		ConcurrentHashMap<ComponentName, AppItemInfo> tmpAllAppsMap = null;
		try {
			tmpAllAppsMap = mAppDataEngine.getAllAppHashMap();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}

		Iterator<Entry<ComponentName, AppItemInfo>> tmpIter = tmpAllAppsMap.entrySet().iterator();
		while (tmpIter.hasNext()) {
			Entry<ComponentName, AppItemInfo> entry = tmpIter.next();
			allAppsMap.put(entry.getKey(), entry.getValue());
		}

		ArrayList<AppItemInfo> itemInfos = new ArrayList<AppItemInfo>();

		// 首先从隐藏程序中获取程序信息
		Iterator<Entry<ComponentName, AppConfigInfo>> it = mHideHashMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<ComponentName, AppConfigInfo> entry = it.next();
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
			// TODO:弹出提示排序失败
			e.printStackTrace();
		}
		return itemInfos;
	}

	@Override
	public void cleanup() {
		clearAllObserver();
	}

	public AppConfigDataModel getAppConfigDataModel() {
		return mAppConfigDataModel;
	}
	
	public boolean isExistHideApp() {
		return !mHideHashMap.isEmpty();
	}
}
