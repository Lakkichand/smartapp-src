package com.jiubang.ggheart.common.bussiness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.bussiness.BaseBussiness;
import com.jiubang.ggheart.common.data.AppClassifyDataModel;
import com.jiubang.ggheart.data.info.AppItemInfo;

/**
 * 应用分类事务类
 */
public class AppClassifyBussiness extends BaseBussiness {
	public static final int NO_CLASSIFY_APP = -1;
	public static final int SYSTEM_APP = 11;
	
	private AppClassifyDataModel mDataModel;
//	private String[] mClassifyNames = mContext.getResources().getStringArray(R.array.folder_classify_name);

	public AppClassifyBussiness(Context context) {
		super(context);
		mDataModel = new AppClassifyDataModel(context);
	}
	
	/**
	 * 查询多个应用分类(初始化时分类)
	 * 注意：查询数据库进行匹配
	 * @param infoList
	 */
	@SuppressWarnings("unchecked")
	public void initAllAppClassify() {
		HashMap<String, Object> classifiedApps = mAppDataEngine.getAppsMap();
		classifyAppitemInfos(classifiedApps);
	}

	/**
	 * 查询一个应用info分类（安装新应用时调用）
	 * 注意：查询数据库进行匹配
	 * @param Info
	 */
	public void queryAppClassify(AppItemInfo Info) {
		String pkName = Info.mIntent.getComponent().getPackageName();
		if (pkName != null) {
			HashMap<String, Integer> result = mDataModel.getAppClassify(pkName);
			if (!result.isEmpty()) {
				Info.mClassification = result.get(pkName);
			}
		}
	}
	
	/**
	 * 查询一个应用分类（安装新应用时调用，速度相对较快）
	 * 注意：查询数据库进行匹配
	 * @param pkgName 包名
	 * @param infoList 该包名对应的info列表
	 */
	public void queryAppClassify(String pkgName, ArrayList<AppItemInfo> infoList) {
		if (pkgName != null) {
			HashMap<String, Integer> result = mDataModel.getAppClassify(pkgName);
			if (!result.isEmpty()) {
				for (AppItemInfo appItemInfo : infoList) {
					appItemInfo.mClassification = result.get(pkgName);
				}
			}
		}
	}
	
	/**
	 * 查询多个应用info的分类（安装新应用时调用）
	 * 注意：查询数据库进行匹配
	 * @param infoList
	 */
	public void queryAppsClassify(ArrayList<AppItemInfo> infoList) {
		// 把list转化成我们可供查询数据库的数据结构
		if (infoList != null) {
			HashMap<String, Object> pkgMap = new HashMap<String, Object>();
			for (AppItemInfo appItemInfo : infoList) {
				String pkg = appItemInfo.getAppPackageName();
				if (pkg != null) {
					if (pkgMap.containsKey(pkg)) {
						Object obj = pkgMap.get(pkg);
						if (obj instanceof ArrayList<?>) {
							((ArrayList<AppItemInfo>) obj).add(appItemInfo);
						} else if (obj instanceof AppItemInfo) {
							ArrayList<AppItemInfo> samePkgInfo = new ArrayList<AppItemInfo>();
							samePkgInfo.add((AppItemInfo) obj);
							samePkgInfo.add(appItemInfo);
							pkgMap.put(pkg, samePkgInfo);
						}
					} else {
						pkgMap.put(pkg, appItemInfo);
					}
				}
			}
			classifyAppitemInfos(pkgMap);
		}
	}

	/**
	 * 把转换好的map进行分类查询配对，并把结果设在appitemInfo的classification属性里
	 * @param pkgMap 以包名作为key的map
	 */
	private void classifyAppitemInfos(HashMap<String, Object> pkgMap) {
		if (!pkgMap.isEmpty()) {
			HashMap<String, Integer> result = mDataModel.getAllAppClassifyItems(pkgMap.keySet());
			if (result.isEmpty()) {
				return;
			}
			Iterator<Entry<String, Integer>> iter = result.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Integer> entry = iter.next();
				Integer classification = entry.getValue();
				String pkg = entry.getKey();
				Object obj = pkgMap.get(pkg);
				if (obj instanceof AppItemInfo) {
					((AppItemInfo) obj).mClassification = classification;
				} else if (obj instanceof ArrayList<?>) {
					ArrayList<AppItemInfo> info = (ArrayList<AppItemInfo>) obj;
					for (AppItemInfo appItemInfo : info) {
						appItemInfo.mClassification = classification;
					}
				}
			}
		}
	}
	/**
	 * 根据分类产生一个默认文件夹名称
	 * @return
	 */
	public String generateFolderName(ArrayList<AppItemInfo> infoList) {
		// 选择出现最多的类型名字
//		使用了稀疏数组，具体对该算法有速度的提升？
		SparseArray<ArrayList<AppItemInfo>> map = generateClassifyMap(infoList);
		int size = map.size();
		int max = 1;
		int classification = NO_CLASSIFY_APP;
		for (int i = 0; i < size; i++) {
			int cur = map.valueAt(i).size();
			if (cur > max) {
				max = cur;
				classification = map.keyAt(i);
			}
		}
		if (classification == NO_CLASSIFY_APP) {
			return mContext.getResources().getString(R.string.folder_name);
		}
		String[] classifyNames = mContext.getResources().getStringArray(R.array.folder_classify_name);
		if (classification < 0 || classifyNames.length <= classification) {
			Log.e("wuziyi", "爆数组了");
			return mContext.getResources().getString(R.string.folder_name);
		}
		return classifyNames[classification];
		// 普通hashmap算法
//		HashMap<Integer, ArrayList<AppItemInfo>> map = generateClassifyMap(infoList);
//		Iterator<Entry<Integer, ArrayList<AppItemInfo>>> iter = map.entrySet().iterator();
//		Entry<Integer, ArrayList<AppItemInfo>> max = null;
//		while (iter.hasNext()) {
//	       	Entry<Integer, ArrayList<AppItemInfo>> next = iter.next();
//	       	if (max == null || next.getValue().size() > max.getValue().size()) {
//	       		max = next;
//	       	}
//		}
//		if (max == null) {
//			return mContext.getResources().getString(R.string.folder_name);
//		}
		// 读取配置的数组，比较max中的key就获得默认名字
//		String[] classifyNames = mContext.getResources().getStringArray(R.array.folder_classify_name);
//		int key = max.getKey();
//		if (key < 0 || classifyNames.length <= key) {
//			Log.e("wuziyi", "爆数组了");
//			return mContext.getResources().getString(R.string.folder_name);
//		}
//		return classifyNames[max.getKey()];
	}

	/**
	 * 把传入列表分类并转化数据结构
	 * @param infoList
	 * @return map <BR/>
	 * ［分类名ID， 应用列表对象］ <BR/>
	 * ［游戏 ， ArrayList<AppItemInfo>］ <BR/>
	 * ［社交 ， ArrayList<AppItemInfo>］ <BR/>
	 * ［垃圾 ， ArrayList<AppItemInfo>］ <BR/>
	 */
	private SparseArray<ArrayList<AppItemInfo>> generateClassifyMap(ArrayList<AppItemInfo> infoList) {
		SparseArray<ArrayList<AppItemInfo>> map = new SparseArray<ArrayList<AppItemInfo>>();
//		HashMap<Integer, ArrayList<AppItemInfo>> map = new HashMap<Integer, ArrayList<AppItemInfo>>();
		for (AppItemInfo appItemInfo : infoList) {
			if (appItemInfo != null) {
				int classification = appItemInfo.mClassification;
				if (classification != NO_CLASSIFY_APP) { // -1表示没有分类
					ArrayList<AppItemInfo> sameClassifyApps = map.get(classification);
					if (sameClassifyApps != null) { 
						sameClassifyApps.add(appItemInfo);
			       		map.put(classification, sameClassifyApps);
			       	} else {
			       		sameClassifyApps = new ArrayList<AppItemInfo>();
			       		sameClassifyApps.add(appItemInfo);
			       		map.put(classification, sameClassifyApps);
					}
				}
			}
	    }
		return map;
	}
	
	/**
	 * 智能分类
	 * @param infoList
	 */
	public void intelligentClassification(ArrayList<AppItemInfo> infoList) {
		SparseArray<ArrayList<AppItemInfo>> map = generateClassifyMap(infoList);
	}
}
