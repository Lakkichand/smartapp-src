/*
 * 文 件 名:  RunngingDownloadServiceConstance.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-10-22
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

import java.util.ArrayList;

/**
 * <br>类描述:判断下载服务器是否要停止的辅助类
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-10-22]
 */
public class RunngingDownloadServiceConstance {

	/**
	 * 进程列表，如果这些进程存活，下载服务器不能停止
	 */
	private String[] mProcessNameList = new String[] { "com.gau.go.launcherex:appcenter",
			"com.gau.go.launcherex:gamezone", "com.gau.go.launcherex:MyThemes",
			"com.gau.go.launcherex:gostore" };

	/**
	 * Activity列表，如果这些Activity存在，下载服务器不能停止
	 */
	private ArrayList<String> mActivityClassNameList = new ArrayList<String>();

	public boolean isProcessKeepAlive(String processName) {
		if (processName == null) {
			return false;
		}
		boolean flag = false;
		if (mProcessNameList == null) {
			return true;
		}
		for (int i = 0; i < mProcessNameList.length; i++) {
			if (processName.equals(mProcessNameList[i])) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public boolean isActivityKeepAlive(String activityClassName) {
		if (activityClassName == null) {
			return false;
		}
		boolean flag = false;
		if (mActivityClassNameList == null) {
			return true;
		}
		for (int i = 0; i < mActivityClassNameList.size(); i++) {
			if (activityClassName.equals(mActivityClassNameList.get(i))) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public void addActivityClassName(String className) {
		if (mActivityClassNameList != null && !mActivityClassNameList.contains(className)) {
			mActivityClassNameList.add(className);
		}
	}
}
