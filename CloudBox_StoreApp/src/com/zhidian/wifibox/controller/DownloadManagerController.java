package com.zhidian.wifibox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InstallingValidator;

/**
 * 下载管理页控制器，为DownloadManagerContainer提供下载数据
 * 
 * 这个类直接向DownloadManager获取数据
 * 
 * @author xiedezhi
 * 
 */
public class DownloadManagerController extends TACommand {

	/**
	 * 获取下载任务
	 */
	public static final String GET_DOWNLOAD_TASK = "DOWNLOADMANAGERCONTROLLER_GET_DOWNLOAD_TASK";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String commad = (String) request.getTag();
		if (commad.equals(GET_DOWNLOAD_TASK)) {
			Map<String, DownloadTask> map = DownloadTaskRecorder.getInstance()
					.getDownloadTaskList();
			List<DownloadTask> downloadingList = new ArrayList<DownloadTask>();
			List<DownloadTask> downloadedList = new ArrayList<DownloadTask>();
			for (String key : map.keySet()) {
				String apkPath = DownloadUtil
						.getCApkFileFromUrl(map.get(key).url);
				String packName = map.get(key).packName;
				if (FileUtil.isFileExist(apkPath)
						|| (InstallingValidator.getInstance().isAppExist(
								TAApplication.getApplication(), packName)
								&& map.get(key).state != DownloadTask.DOWNLOADING && map
								.get(key).state != DownloadTask.PAUSING)) {
					// 存在APK包或者已经安装就放在下载历史列表
					downloadedList.add(map.get(key));
				} else {
					downloadingList.add(map.get(key));
				}
			}
			Object[] objs = { downloadingList, downloadedList };
			sendSuccessMessage(objs);
		}
	}
}
