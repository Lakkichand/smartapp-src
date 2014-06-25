package com.smartapp.autostartmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Debug;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;

/**
 * 逻辑控制器
 */
public class MainController extends TACommand {
	/**
	 * 开机启动action
	 */
	public static final String BOOTACTION = "android.intent.action.BOOT_COMPLETED";
	/**
	 * 后台启动action
	 */
	public static final String[] BACKGROUNDACTION = {
			"android.intent.action.PACKAGE_CHANGED",
			"android.intent.action.PACKAGE_RESTARTED",
			"android.intent.action.PACKAGE_REMOVED",
			"android.net.conn.CONNECTIVITY_CHANGE",
			"android.net.wifi.WIFI_STATE_CHANGED",
			"android.net.wifi.STATE_CHANGE",
			"android.intent.action.MEDIA_EJECT",
			"android.intent.action.USER_PRESENT",
			"android.intent.action.PHONE_STATE",
			"android.intent.action.PACKAGE_ADDED",
			"android.intent.action.MEDIA_UNMOUNTED",
			"android.intent.action.MEDIA_REMOVED",
			"android.intent.action.MEDIA_CHECKING",
			"android.intent.action.PACKAGE_REPLACED",
			"android.intent.action.NEW_OUTGOING_CALL",
			"android.bluetooth.adapter.action.STATE_CHANGED",
			"android.intent.action.TIME_SET",
			"android.intent.action.TIME_TICK",
			"android.intent.action.TIMEZONE_CHANGED",
			"android.intent.action.PACKAGE_DATA_CLEARED",
			"android.intent.action.UID_REMOVED",
			"android.intent.action.BATTERY_CHANGED",
			"android.intent.action.BATTERY_LOW",
			"android.intent.action.BATTERY_OKAY",
			"android.intent.action.ACTION_POWER_CONNECTED",
			"android.intent.action.ACTION_POWER_DISCONNECTED",
			"android.intent.action.ACTION_SHUTDOWN",
			"android.intent.action.CAMERA_BUTTON",
			"android.intent.action.MEDIA_MOUNTED",
			"android.intent.action.ANY_DATA_STATE" };

	/**
	 * 扫描自启动和后台启动项
	 */
	public static final String SCAN_COMMAND = "SCAN_COMMAND";
	/**
	 * 获取应用CPU使用率
	 */
	public static final String CPU_RATE = "CPU_RATE";
	/**
	 * 禁用某个软件的开机启动和后台启动
	 */
	public static final String DISABLE_APP = "DISABLE_APP";
	/**
	 * 启用某个软件的开机启动和后台启动
	 */
	public static final String ENABLE_APP = "ENABLE_APP";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(SCAN_COMMAND)) {
			Map<String, DataBean> map = new HashMap<String, DataBean>();
			PackageManager pm = TAApplication.getApplication()
					.getPackageManager();
			{
				Intent intent = new Intent(BOOTACTION);
				List<ResolveInfo> resolveInfoList = TAApplication
						.getApplication()
						.getPackageManager()
						.queryBroadcastReceivers(intent,
								PackageManager.GET_DISABLED_COMPONENTS);
				for (ResolveInfo rinfo : resolveInfoList) {
					String packname = rinfo.activityInfo.packageName;
					if (map.containsKey(packname)) {
						DataBean bean = map.get(packname);
						bean.mBootReceiver.add(rinfo);
					} else {
						try {
							DataBean bean = new DataBean();
							ApplicationInfo appInfo = pm.getApplicationInfo(
									packname, 0);
							bean.mInfo = appInfo;
							bean.mName = appInfo.loadLabel(pm).toString();
							if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
								bean.mIsSysApp = true;
							} else {
								bean.mIsSysApp = false;
							}
							bean.mBootReceiver.add(rinfo);
							map.put(packname, bean);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				sendRuntingMessage((int) (20.0 / (BACKGROUNDACTION.length + 1)));
			}
			for (int i = 0; i < BACKGROUNDACTION.length; i++) {
				String action = BACKGROUNDACTION[i];
				Intent intent = new Intent(action);
				List<ResolveInfo> resolveInfoList = TAApplication
						.getApplication()
						.getPackageManager()
						.queryBroadcastReceivers(intent,
								PackageManager.GET_DISABLED_COMPONENTS);
				for (ResolveInfo rinfo : resolveInfoList) {
					String packname = rinfo.activityInfo.packageName;
					if (map.containsKey(packname)) {
						DataBean bean = map.get(packname);
						bean.mBackgroundReceiver.add(rinfo);
					} else {
						try {
							DataBean bean = new DataBean();
							ApplicationInfo appInfo = pm.getApplicationInfo(
									packname, 0);
							bean.mInfo = appInfo;
							bean.mName = appInfo.loadLabel(pm).toString();
							if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
								bean.mIsSysApp = true;
							} else {
								bean.mIsSysApp = false;
							}
							bean.mBackgroundReceiver.add(rinfo);
							map.put(packname, bean);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				sendRuntingMessage((int) ((i + 2) * 20.0 / (BACKGROUNDACTION.length + 1)));
			}
			// 计算应用内存占用
			ActivityManager activityManager = (ActivityManager) TAApplication
					.getApplication()
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningAppProcessInfo> list = activityManager
					.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo info : list) {
				int pid = info.pid;
				// 获得该进程占用的内存
				int[] myMempid = new int[] { pid };
				// 此MemoryInfo位于android.os.Debug.MemoryInfo包中，用来统计进程的内存信息
				Debug.MemoryInfo[] memoryInfo = activityManager
						.getProcessMemoryInfo(myMempid);
				// 获取进程占内存用信息 kb单位
				int memSize = memoryInfo[0].dalvikPrivateDirty
						+ memoryInfo[0].dalvikSharedDirty
						+ memoryInfo[0].nativePrivateDirty
						+ memoryInfo[0].otherPrivateDirty;
				String[] pkgs = info.pkgList;
				int aMemSize = memSize / pkgs.length;
				for (String pkg : pkgs) {
					if (map.containsKey(pkg)) {
						DataBean bean = map.get(pkg);
						bean.mMemory += aMemSize;
					}
				}
				int index = list.indexOf(info);
				sendRuntingMessage((int) ((index + 1) * 80.0 / list.size()) + 20);
			}
			// 移除自己
			map.remove(TAApplication.getApplication().getPackageName());
			// 已经禁用的应用
			for (String packname : map.keySet()) {
				DataBean xb = map.get(packname);
				xb.mIsForbid = true;
				for (ResolveInfo rinfo : xb.mBootReceiver) {
					ComponentName mComponentName = new ComponentName(
							rinfo.activityInfo.packageName,
							rinfo.activityInfo.name);
					int state = pm.getComponentEnabledSetting(mComponentName);
					if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
						xb.mIsForbid = false;
						break;
					}
				}
				if (xb.mIsForbid) {
					for (ResolveInfo rinfo : xb.mBackgroundReceiver) {
						ComponentName mComponentName = new ComponentName(
								rinfo.activityInfo.packageName,
								rinfo.activityInfo.name);
						int state = pm
								.getComponentEnabledSetting(mComponentName);
						if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
							xb.mIsForbid = false;
							break;
						}
					}
				}
			}
			// 根据使用内存多少进行排序
			List<DataBean> retList = new ArrayList<DataBean>();
			retList.addAll(map.values());
			Collections.sort(retList);
			sendSuccessMessage(retList);
		} else if (command.equals(CPU_RATE)) {
			// cpu使用率
			// 获得ActivityManager服务的对象
			ActivityManager activityManager = (ActivityManager) TAApplication
					.getApplication()
					.getSystemService(Context.ACTIVITY_SERVICE);

			List<ActivityManager.RunningAppProcessInfo> list = activityManager
					.getRunningAppProcesses();
			LinuxUtils lutil = new LinuxUtils();
			Map<Integer, String[]> record1 = new HashMap<Integer, String[]>();
			for (ActivityManager.RunningAppProcessInfo info : list) {
				int pid = info.pid;
				String pidStat1 = lutil.readProcessStat(pid);
				String totalStat1 = lutil.readSystemStat();
				record1.put(pid, new String[] { pidStat1, totalStat1 });
			}
			try {
				Thread.sleep(1500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Map<Integer, String[]> record2 = new HashMap<Integer, String[]>();
			for (ActivityManager.RunningAppProcessInfo info : list) {
				int pid = info.pid;
				String pidStat2 = lutil.readProcessStat(pid);
				String totalStat2 = lutil.readSystemStat();
				record2.put(pid, new String[] { pidStat2, totalStat2 });
			}
			Map<Integer, Float> prate = new HashMap<Integer, Float>();
			for (int pid : record2.keySet()) {
				try {
					String pidStat1 = record1.get(pid)[0];
					String totalStat1 = record1.get(pid)[1];
					if (pidStat1 == null || totalStat1 == null) {
						prate.put(pid, 0.0f);
						continue;
					}
					String pidStat2 = record2.get(pid)[0];
					String totalStat2 = record2.get(pid)[1];
					if (pidStat2 == null || totalStat2 == null) {
						prate.put(pid, 0.0f);
						continue;
					}
					String[] toks = totalStat1.split(" ");
					long cpu1 = lutil.getSystemUptime(toks);

					toks = totalStat2.split(" ");
					long cpu2 = lutil.getSystemUptime(toks);

					float ret = lutil.getProcessCpuUsage(pidStat1, pidStat2,
							cpu2 - cpu1);
					if (ret < 0) {
						ret = 0;
					}
					prate.put(pid, ret);
				} catch (Exception e) {
					e.printStackTrace();
					prate.put(pid, 0.0f);
				}
			}
			Map<String, Float> retRate = new HashMap<String, Float>();
			for (ActivityManager.RunningAppProcessInfo info : list) {
				int pid = info.pid;
				float rate = prate.get(pid);
				String[] pkgs = info.pkgList;
				float arate = rate / pkgs.length;
				for (String pkg : pkgs) {
					if (retRate.containsKey(pkg)) {
						float f = retRate.get(pkg);
						retRate.put(pkg, f + arate);
					} else {
						retRate.put(pkg, arate);
					}
				}
			}
			retRate.remove(TAApplication.getApplication().getPackageName());
			sendSuccessMessage(retRate);
		} else if (command.equals(DISABLE_APP)) {
			DataBean bean = (DataBean) request.getData();
			for (ResolveInfo info : bean.mBootReceiver) {
				AppFreezer.disableClass(info.activityInfo.packageName,
						info.activityInfo.name);
			}
			for (ResolveInfo info : bean.mBackgroundReceiver) {
				AppFreezer.disableClass(info.activityInfo.packageName,
						info.activityInfo.name);
			}
			PackageManager pm = TAApplication.getApplication()
					.getPackageManager();
			boolean ret = true;
			for (ResolveInfo rinfo : bean.mBootReceiver) {
				ComponentName mComponentName = new ComponentName(
						rinfo.activityInfo.packageName, rinfo.activityInfo.name);
				int state = pm.getComponentEnabledSetting(mComponentName);
				if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
					ret = false;
					break;
				}
			}
			if (ret) {
				for (ResolveInfo rinfo : bean.mBackgroundReceiver) {
					ComponentName mComponentName = new ComponentName(
							rinfo.activityInfo.packageName,
							rinfo.activityInfo.name);
					int state = pm.getComponentEnabledSetting(mComponentName);
					if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
						ret = false;
						break;
					}
				}
			}
			if (ret) {
				// TODO 禁用成功，杀死程序进程
				ActivityManager activityManager = (ActivityManager) TAApplication
						.getApplication().getSystemService(
								Context.ACTIVITY_SERVICE);
				List<ActivityManager.RunningAppProcessInfo> list = activityManager
						.getRunningAppProcesses();
				for (ActivityManager.RunningAppProcessInfo info : list) {
					String[] pkgNames = info.pkgList;
					for (String pkg : pkgNames) {
						if (bean.mInfo.packageName.equals(pkg)) {
							RootShell rootShell = null;
							try {
								rootShell = RootShell.startShell();
							} catch (Exception e) {
								e.printStackTrace();
							}
							final String cmd = "kill -9 " + info.pid;
							new RootShell.Command(cmd).execute(rootShell);
						}
					}
				}
			}
			sendSuccessMessage(ret);
		} else if (command.equals(ENABLE_APP)) {
			DataBean bean = (DataBean) request.getData();
			for (ResolveInfo info : bean.mBootReceiver) {
				AppFreezer.enableClass(info.activityInfo.packageName,
						info.activityInfo.name);
			}
			for (ResolveInfo info : bean.mBackgroundReceiver) {
				AppFreezer.enableClass(info.activityInfo.packageName,
						info.activityInfo.name);
			}
			PackageManager pm = TAApplication.getApplication()
					.getPackageManager();
			boolean ret = true;
			for (ResolveInfo rinfo : bean.mBootReceiver) {
				ComponentName mComponentName = new ComponentName(
						rinfo.activityInfo.packageName, rinfo.activityInfo.name);
				int state = pm.getComponentEnabledSetting(mComponentName);
				if (state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
					ret = false;
					break;
				}
			}
			if (ret) {
				for (ResolveInfo rinfo : bean.mBackgroundReceiver) {
					ComponentName mComponentName = new ComponentName(
							rinfo.activityInfo.packageName,
							rinfo.activityInfo.name);
					int state = pm.getComponentEnabledSetting(mComponentName);
					if (state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
						ret = false;
						break;
					}
				}
			}
			sendSuccessMessage(ret);
		}
	}
}
