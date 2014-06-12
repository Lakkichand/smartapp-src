/*
 * 文 件 名:  DownloadCompleteManager.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-10-24
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.go.util.file.FileUtil;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppFileUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <br>类描述:下载完成的任务管理
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-10-24]
 */
public class DownloadCompleteManager {

	private Context mContext = null;
	//TODO:下载wangzhuobin mCompleteDownloadInfor、mCompleteTaskList、mInstallTaskPkgList这三个东西是否可以考虑合并在一起
	/**
	 * 已经完成下载的下载任务的信息结合
	 */
	private ConcurrentHashMap<Long, String> mCompleteDownloadInfor = null;
	/**
	 * 已经下载完成的任务
	 */
	//TODO:下载wangzhuobin 该值的修改是否要考虑线程同步，不应该由外部调用的人去保证
	private List<DownloadTask> mCompleteTaskList = null;
	/**
	 * 已经安装的下载任务的包名
	 */
	//TODO:下载wangzhuobin 该值的修改是否要考虑线程同步，不应该由外部调用的人去保证
	//TODO:下载wangzhuobin 在这里，紧通过包名去判断该APK是否已经安装是否不足够？同包名不同版本怎么办？
	//TODO:下载wangzhuobin 目前这个主要是在程序安装广播接收器中接收到有安装的广播，就记录这些安装程序的包名。
	//                    然后通过包名去找已经下载完成的任务的ID，然后通过ID去清除该下载任务对应的通知栏通知。
	//                    所以mInstallTaskPkgList和mCompleteDownloadInfor配合着用
	private List<String> mInstallTaskPkgList = null;

	public DownloadCompleteManager(Context context) {
		mContext = context;
		mCompleteDownloadInfor = new ConcurrentHashMap<Long, String>();
		mCompleteTaskList = Collections.synchronizedList(new ArrayList<DownloadTask>());
		mInstallTaskPkgList = Collections.synchronizedList(new ArrayList<String>());
	}

	/**
	 * 通过应用包命获取已经下载完成的下载任务ID集合的方法
	 * 
	 * @param packageName
	 * @return
	 */
	public ArrayList<Long> getCompleteIdsByPkgName(String packageName) {
		ArrayList<Long> ids = null;
		if (packageName != null && !"".equals(packageName.trim()) && mCompleteDownloadInfor != null
				&& mCompleteDownloadInfor.containsValue(packageName)) {
			ids = new ArrayList<Long>();
			Set<Long> keySet = mCompleteDownloadInfor.keySet();
			String value = null;
			for (Long id : keySet) {
				value = mCompleteDownloadInfor.get(id);
				if (value.equals(packageName)) {
					ids.add(id);
				}
			}
		}
		return ids;
	}

	public void setCompeleteDownloadInfo(Long taskId, String pkgName) {
		mCompleteDownloadInfor.put(taskId, pkgName);
	}

	/**
	 * 功能简述:每下载完成一个任务，将任务保存在下载完成队列中
	 * 功能详细描述:
	 * 注意:外部不能调用
	 * @param downloadTask
	 */
	public void addDownloadCompleteTask(DownloadTask downloadTask) {
		if (mCompleteTaskList != null) {
			//TODO:下载wangzhuobin 为什么要移除包名相同的任务？
			synchronized (mCompleteTaskList) {
				for (DownloadTask task : mCompleteTaskList) {
					if (downloadTask.getDownloadApkPkgName().equals(task.getDownloadApkPkgName())) {
						mCompleteTaskList.remove(task);
						break;
					}
				}
			}
			if (downloadTask.getIsApkFile()) {
				if (downloadTask.getDownloadApkPkgName() == null || downloadTask
						.getDownloadApkPkgName().equals("")) {
					// 桌面推荐的应用是没有包名的，所以在下载完成之后，需要从下载完的apk包解析出包名
					PackageManager pm = mContext.getPackageManager();
					if (pm != null) {
						PackageInfo info = pm.getPackageArchiveInfo(downloadTask.getSaveFilePath(),
								PackageManager.GET_ACTIVITIES);
						if (info != null) {
							downloadTask.setDownloadApkPkgName(info.packageName);
						}
					}
				}
				mCompleteTaskList.add(0, downloadTask);
			}
			// 下载完成的任务信息改变，就需要实时保存信息
			saveDownlaodCompleteTask();
		}
	}
	/**
	 * 功能简述:删除一个“已下载完成”任务
	 * 功能详细描述:
	 * 注意:提供给外部的接口
	 * @param taskId
	 */
	public void removeDownloadCompleteTask(long taskId) {
		if (mCompleteTaskList != null) {
			synchronized (mCompleteTaskList) {
				for (DownloadTask task : mCompleteTaskList) {
					if (task.getId() == taskId) {
						mCompleteTaskList.remove(task);
						break;
					}
				}
				// 下载完成的任务信息改变，就需要实时保存信息
				saveDownlaodCompleteTask();
			}
		}
	}

	public List<DownloadTask> getDownloadCompleteList() {
		return mCompleteTaskList;
	}

	public void addInstalledTask(String packageName) {
		mInstallTaskPkgList.add(packageName);
		// 已安装的下载任务信息改变，就需要实时保存信息
		saveInstalledTask();
	}

	public void removeInstalledTask(String packageName) {
		if (mInstallTaskPkgList.contains(packageName)) {
			mInstallTaskPkgList.remove(packageName);
			// 已安装的下载任务信息改变，就需要实时保存信息
			saveInstalledTask();
		}
	}

	public List<String> getInstalledTaskList() {
		return mInstallTaskPkgList;
	}

	//TODO:下载wangzhuobin 外部数据读写，考虑建一个下载数据保存的辅助工具类
	//---------------------------------------------数据保存-------------------------------------//

	// 下载服务退出的时候保存已经安装的下载任务的包名到SD卡
	public void saveInstalledTask() {
		JSONArray installedJsonArray = new JSONArray();
		if (mInstallTaskPkgList == null) {
			return;
		}
		synchronized (mInstallTaskPkgList) {
			for (String packageName : mInstallTaskPkgList) {
				JSONObject obj = new JSONObject();
				try {
					obj.put("packageName", packageName);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				installedJsonArray.put(obj);
			}
		}
		FileUtil.saveByteToSDFile(installedJsonArray.toString().getBytes(),
				LauncherEnv.Path.DOWNLOAD_AND_INSTALLED_PATH);
	}

	// 下载服务退出的时候保存已经下载完成的任务到SD卡
	public void saveDownlaodCompleteTask() {
		JSONArray completeArray = new JSONArray();
		synchronized (mCompleteTaskList) {
			for (DownloadTask task : mCompleteTaskList) {
				JSONObject obj = new JSONObject();
				try {
					obj.put("id", task.getId());
					obj.put("iconType", task.getIconType());
					obj.put("iconInfo", task.getIconInfo());
					obj.put("name", task.getDownloadName());
					obj.put("package", task.getDownloadApkPkgName());
					obj.put("path", task.getSaveFilePath());
					obj.put("totalSize", task.getTotalSize());
					obj.put("alreadyDownloadSize", task.getAlreadyDownloadSize());
					obj.put("alreadyDownloadPercent", task.getAlreadyDownloadPercent());
					obj.put("module", task.getModule());
					obj.put("state", task.getState());
					obj.put("downloadUrl", task.getDownloadUrl());
//					obj.put("treatment", task.getTreatment());
					completeArray.put(obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		FileUtil.saveByteToSDFile(completeArray.toString().getBytes(),
				LauncherEnv.Path.DOWNLOAD_COMPLETE_PATH);
	}

	//-------------------------------------------------缓存数据读取-----------------------------------//
	// 从SD卡获取已经安装的下载任务的包名
	public void getInstalledTaskFromSD() {
		String info = RecommAppFileUtil
				.readFileToString(LauncherEnv.Path.DOWNLOAD_AND_INSTALLED_PATH);
		try {
			if (info == null) {
				return;
			}
			JSONArray jsonArray = new JSONArray(info);
			mInstallTaskPkgList.clear();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				String packageName = obj.getString("packageName");
				mInstallTaskPkgList.add(packageName);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 下载服务启动的时候，从SD卡读取已经下载完成的任务
	public void getDownloadCompleteTask() {
		String info = RecommAppFileUtil.readFileToString(LauncherEnv.Path.DOWNLOAD_COMPLETE_PATH);
		try {
			if (info == null) {
				return;
			}
			JSONArray jsonArray = new JSONArray(info);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				long taskId = obj.optLong("id", System.currentTimeMillis());
				int iconType = obj.optInt("iconType");
				String iconInfo = obj.optString("iconInfo");
				String name = obj.optString("name");
				String packageName = obj.optString("package");
				String path = obj.optString("path");
				long totalSize = obj.optLong("totalSize");
				long alreadyDownloadSize = obj.optLong("alreadyDownloadSize");
				int alreadyDownloadPercent = obj.optInt("alreadyDownloadPercent");
				int state = obj.optInt("state");
				int module = obj.optInt("module");
				String downloadUrl = obj.optString("downloadUrl");
				int treatment = obj.optInt("treatment", 0);

				DownloadTask task = new DownloadTask(taskId, downloadUrl, name, path, packageName,
						iconType, iconInfo, module);
				task.setAlreadyDownloadPercent(alreadyDownloadPercent);
				task.setAlreadyDownloadSize(alreadyDownloadSize);
				task.setTotalSize(totalSize);
				task.setState(state);
//				task.setTreatment(treatment);
				File file = new File(path);
				//假如安装包在目录下存在，则认为这是一个下载完成的任务
				if (file.exists()) {
					mCompleteTaskList.add(task);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
