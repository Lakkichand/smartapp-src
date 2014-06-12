package com.jiubang.ggheart.common.bussiness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.jiubang.ggheart.appgame.base.net.InstallCallbackManager;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.bussiness.BaseBussiness;
import com.jiubang.ggheart.common.data.RecommendAppDataModel;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 推荐应用业务逻辑类
 * @author yejijiong
 *
 */
public class RecommendAppBussiness extends BaseBussiness {

	private RecommendAppDataModel mDataModel;
	/**
	 * 保存所有需显示New标识的推荐应用包名
	 */
	private HashSet<String> mShowNewRecommendApps;
	/**
	 * 保存快捷方式对应的AppItemInfo队列
	 */
	private HashSet<AppItemInfo> mShortCutAppItemInfos;

	public RecommendAppBussiness(Context context) {
		super(context);
		mDataModel = new RecommendAppDataModel(context);
		mShowNewRecommendApps = new HashSet<String>();
		mShortCutAppItemInfos = new HashSet<AppItemInfo>();
	}

	/**
	 * 初始化，从数据库获取所有需要显示New标识的推荐应用
	 */
	public void initAllNewRecommendApps() {
		HashSet<Intent> recommendApps = mDataModel.getAllShowNewRecommendApps();
		HashMap<String, Object> appsMap = mAppDataEngine.getAppsMap();
		HashSet<Intent> deletedRecommendApps = new HashSet<Intent>();
		for (Intent intent : recommendApps) {
			String pkgName = intent.getComponent().getPackageName(); 
			if (appsMap.containsKey(pkgName)) {
				mShowNewRecommendApps.add(pkgName);
			} else {
				// 已经卸载的加入删除应用队列
				deletedRecommendApps.add(intent);
			}
		}
		if (deletedRecommendApps.size() > 0) { // 删除垃圾数据
			mDataModel.deleteRecommendApps(deletedRecommendApps);
		}
		
		for (String pkg : mShowNewRecommendApps) {
			if (appsMap.containsKey(pkg)) {
				Object obj = appsMap.get(pkg);
				if (obj instanceof AppItemInfo) {
					((AppItemInfo) obj).setIsNewRecommendApp(true);
				} else if (obj instanceof ArrayList<?>) {
					ArrayList<AppItemInfo> infoList = (ArrayList<AppItemInfo>) obj;
					for (AppItemInfo recommendPkg : infoList) {
						recommendPkg.setIsNewRecommendApp(true);
					}
				}
			}
		}
	}

	/**
	 * 检查新安装的应用是否是推荐应用，是则为其加上New标识并加入推荐应用表
	 * @return
	 */
	public boolean checkInstallAppIsRecommend(String pkg, ArrayList<AppItemInfo> appItemInfos) {
		boolean flag = false;
		PreferencesManager preferencesManager = new PreferencesManager(mContext,
				IPreferencesIds.ADVERT_SCREEN_DATA/*ADVERT_NEET_OPEN_DATA*/, Context.MODE_WORLD_READABLE);
		if (preferencesManager.contains(pkg)) { // 检查是否1，5屏推荐应用
			flag = true;
		} else {
			preferencesManager = new PreferencesManager(GOLauncherApp.getContext(),
					IPreferencesIds.APP_DATA, Context.MODE_PRIVATE);
			if (preferencesManager.contains(pkg)) { // 检查是否首屏广告
				flag = true;
			} else { // 检查是否应用中心，游戏中心推荐应用
				if (InstallCallbackManager.getIcbackurl(pkg) != null) { // 检查是否首屏，应用中心，游戏中心推荐应用
					flag = true;
				}
//				// TODO:这里由于调测，暂时直接读SharePrefence，以后直接调上面的方法
//				preferencesManager = new PreferencesManager(GOLauncherApp.getContext(),
//						InstallCallbackManager.INSTALL_CALLBACK_PREFERENCES_NAME,
//						Context.MODE_PRIVATE);
//				if (preferencesManager.contains(pkg)) { // 检查是否应用中心，游戏中心推荐应用
//					flag = true;
//				}
			}
		}
		
		if (flag) { // 安装了推荐应用，加上New标识，加入数据库
			HashMap<Intent, Boolean> map = new HashMap<Intent, Boolean>();
			for (AppItemInfo appItemInfo : appItemInfos) {
				appItemInfo.setIsNewRecommendApp(true);
				Intent intent = appItemInfo.mIntent;
//				mDataModel.addRecommendApp(intent, true);
				if (!map.containsKey(intent)) {
					map.put(intent, true);
				}
				mShowNewRecommendApps.add(intent.getComponent().getPackageName());
			}
			mDataModel.addRecommendApps(map);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 检查卸载的应用是否推荐应用，是则在表中删除
	 * @param pkg
	 * @param appItemInfos
	 */
	public boolean checkUnInstallAppIsRecommend(String pkg, ArrayList<AppItemInfo> appItemInfos) {
		if (mShowNewRecommendApps.contains(pkg)) {
			mShowNewRecommendApps.remove(pkg);
			HashSet<Intent> intentList = new HashSet<Intent>();			
			for (AppItemInfo appItemInfo : appItemInfos) {
				// 卸载了推荐应用，删除数据库
				Intent intent = appItemInfo.mIntent;
				intentList.add(intent);				
				Iterator<AppItemInfo> it = mShortCutAppItemInfos.iterator();
				while (it.hasNext()) { // 在队列删除卸载的程序的快捷方式
					AppItemInfo itemInfo = it.next();
					if (itemInfo.mIntent == appItemInfo.mIntent) {
						it.remove();
					}
				}
//				for (AppItemInfo itemInfo : mShortCutAppItemInfos) { // 在队列删除卸载的程序的快捷方式
//					if (itemInfo.mIntent == appItemInfo.mIntent) {
//						mShortCutAppItemInfos.remove(itemInfo);
//					}
//				}
			}
			mDataModel.deleteRecommendApps(intentList);
			return true;
		}
		return false;
	}
	
	/**
	 * 检查单个快捷方式是否推荐应用，是则为其改变mIsRecommendApp变量为true
	 * @return
	 */
	public boolean checkShortCutIsIsRecommend(AppItemInfo appItemInfo) {
		boolean flag = false;
		if (appItemInfo.mIntent != null) {
			ComponentName componentName = appItemInfo.mIntent.getComponent();
			if (componentName != null && mShowNewRecommendApps.contains(componentName.getPackageName())) {
				appItemInfo.setIsNewRecommendApp(true);
				flag = true;
				mShortCutAppItemInfos.add(appItemInfo);
			}
		}
		return flag;
	}

	/**
	 * 推荐应用被打开，更新数据库
	 * @param intent
	 */
	public void recommendAppBeOpen(Intent intent) {
		if (intent != null) {
			ComponentName componentName = intent.getComponent();
			if (componentName != null) {
				String pkg = componentName.getPackageName();
				if (mShowNewRecommendApps.contains(pkg)) {
					HashMap<Intent, Boolean> intentMap = new HashMap<Intent, Boolean>();
					HashMap<String, Object> appsMap = mAppDataEngine.getAppsMap();
					Object obj = appsMap.get(pkg);
					if (obj instanceof AppItemInfo) {
						AppItemInfo appItemInfo = (AppItemInfo) obj;
						appItemInfo.setIsNewRecommendApp(false);
						intentMap.put(appItemInfo.mIntent, false);
					} else if (obj instanceof ArrayList<?>) {
						ArrayList<AppItemInfo> infoList = (ArrayList<AppItemInfo>) obj;
						for (AppItemInfo appItemInfo : infoList) {
							appItemInfo.setIsNewRecommendApp(false);
							intentMap.put(appItemInfo.mIntent, false);
						}
					}
					mDataModel.updateRecommendApp(intentMap);
					mShowNewRecommendApps.remove(pkg);
					Iterator<AppItemInfo> it = mShortCutAppItemInfos.iterator();
					while (it.hasNext()) {
						AppItemInfo appItemInfo = it.next();
						ComponentName component = appItemInfo.mIntent.getComponent();
						if (component != null && component.getPackageName().equals(pkg)) {
							appItemInfo.setIsNewRecommendApp(false);
							it.remove();
						}
					}
					
//					for (AppItemInfo appItemInfo : mShortCutAppItemInfos) {
//						ComponentName component = appItemInfo.mIntent.getComponent();
//						if (component != null && component.getPackageName().equals(pkg)) {
//							appItemInfo.setIsRecommendApp(false);
//							mShortCutAppItemInfos.remove(appItemInfo);
//						}
//					}
				} else {
					try {
						//应用中心临时推荐图标点击后要去掉new标志
						AppItemInfo appItemInfo = AppDataEngine.getInstance(mContext).getAppItem(intent);
						if (appItemInfo != null && appItemInfo.mIsNewRecommendApp) {
							appItemInfo.setIsNewRecommendApp(false);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
