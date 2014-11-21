package com.zhidian.wifibox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.zhidian.wifibox.data.ProcessDataBean;
import com.zhidian.wifibox.root.RootShell;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.LinuxUtils;
import com.zhidian.wifibox.util.Setting;

/**
 * 进程管理控制器
 * 
 * @author xiedezhi
 * 
 */
public class ProcessManagerController extends TACommand {
	/**
	 * 检测运行中的APP
	 */
	public static final String RUNNING_APP = "PROCESSMANAGERCONTROLLER_RUNNING_APP";
	/**
	 * 一键清理
	 */
	public static final String A_KEY_CLEAN_UP = "PROCESSMANAGERCONTROLLER_A_KEY_CLEAN_UP";
	/**
	 * 获取应用CPU使用率
	 */
	public static final String CPU_RATE = "PROCESSMANAGERCONTROLLER_CPU_RATE";

	private Handler mHandler = new Handler(Looper.getMainLooper());

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(RUNNING_APP)) {
			// 获得ActivityManager服务的对象
			ActivityManager activityManager = (ActivityManager) TAApplication
					.getApplication()
					.getSystemService(Context.ACTIVITY_SERVICE);
			PackageManager pm = TAApplication.getApplication()
					.getPackageManager();

			List<ActivityManager.RunningAppProcessInfo> list = activityManager
					.getRunningAppProcesses();
			// 包名对应的内存使用情况
			Map<String, ProcessDataBean> map = new HashMap<String, ProcessDataBean>();
			for (ActivityManager.RunningAppProcessInfo info : list) {
				int index = list.indexOf(info);
				sendRuntingMessage((int) (index * 1.0 / list.size() * 100.0 + 0.5));
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
						ProcessDataBean bean = map.get(pkg);
						bean.mMemory += aMemSize;
					} else {
						try {
							ApplicationInfo appInfo = pm.getApplicationInfo(
									pkg, 0);
							ProcessDataBean bean = new ProcessDataBean();
							bean.mInfo = appInfo;
							bean.mMemory = aMemSize;
							bean.mName = appInfo.loadLabel(pm).toString();
							if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
								bean.mIsSysApp = true;
							} else {
								bean.mIsSysApp = false;
							}
							map.put(pkg, bean);
						} catch (NameNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
			map.remove(TAApplication.getApplication().getPackageName());
			// 根据使用内存多少进行排序
			List<ProcessDataBean> retList = new ArrayList<ProcessDataBean>();
			retList.addAll(map.values());
			Collections.sort(retList);
			List<ProcessDataBean> retList_ = new ArrayList<ProcessDataBean>();
			for (ProcessDataBean bean : retList) {
				if (bean.mMemory > 0) {
					retList_.add(bean);
				}
			}
			Setting setting = new Setting(TAApplication.getApplication());
			String json = setting.getString(Setting.PROTECT_APP);
			JSONArray array = null;
			try {
				array = new JSONArray(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (array == null) {
				array = new JSONArray();
			}
			Set<String> protect = new HashSet<String>();
			for (int i = 0; i < array.length(); i++) {
				try {
					String packname = array.getString(i);
					protect.add(packname);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			for (ProcessDataBean bean : retList_) {
				if (protect.contains(bean.mInfo.packageName)) {
					bean.mIsProtection = true;
				} else {
					bean.mIsProtection = false;
				}
			}
			sendSuccessMessage(retList_);
		} else if (command.equals(CPU_RATE)) {
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
		} else if (command.equals(A_KEY_CLEAN_UP)) {
			// 先杀死其他应用程序的进程
			Setting setting = new Setting(TAApplication.getApplication());
			String json = setting.getString(Setting.PROTECT_APP);
			JSONArray array = null;
			try {
				array = new JSONArray(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (array == null) {
				array = new JSONArray();
			}
			Set<String> protect = new HashSet<String>();
			for (int i = 0; i < array.length(); i++) {
				try {
					String packname = array.getString(i);
					protect.add(packname);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			ActivityManager activityManager = (ActivityManager) TAApplication
					.getApplication()
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager
					.getRunningAppProcesses();
			// 清理前的内存使用
			MemoryInfo outInfo = new MemoryInfo();
			activityManager.getMemoryInfo(outInfo);
			long avi = (long) (outInfo.availMem / 1024.0 / 1024.0 + 0.5);
			long total = (long) (InfoUtil.getTotalRAM() / 1024.0 + 0.5);
			int count1 = runningAppProcesses.size();
			long used1 = total - avi;
			createFile();
			int userid = InfoUtil.getUserId();
			PackageManager pm = TAApplication.getApplication()
					.getPackageManager();
			// 已经杀死的程序，不用重复再杀
			Set<String> killedApp = new HashSet<String>();
			for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
				String[] pkgs = runningAppProcessInfo.pkgList;
				for (String pkg : pkgs) {
					if (pkg.equals(TAApplication.getApplication()
							.getPackageName())) {
						continue;
					}
					if (protect.contains(pkg)) {
						continue;
					}
					activityManager.restartPackage(pkg);
					activityManager.killBackgroundProcesses(pkg);
					try {
						ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
						if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
							// 系统进程不用强制杀死
							continue;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (killedApp.contains(pkg)) {
						continue;
					}
					// 强力杀进程
					killApp(pkg, userid);
					killedApp.add(pkg);
				}
			}
			// 重新获取当前运行的程序
			List<ActivityManager.RunningAppProcessInfo> list = activityManager
					.getRunningAppProcesses();
			// 包名对应的内存使用情况
			Map<String, ProcessDataBean> map = new HashMap<String, ProcessDataBean>();
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
						ProcessDataBean bean = map.get(pkg);
						bean.mMemory += aMemSize;
					} else {
						try {
							ApplicationInfo appInfo = pm.getApplicationInfo(
									pkg, 0);
							ProcessDataBean bean = new ProcessDataBean();
							bean.mInfo = appInfo;
							bean.mMemory = aMemSize;
							bean.mName = appInfo.loadLabel(pm).toString();
							if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
								bean.mIsSysApp = true;
							} else {
								bean.mIsSysApp = false;
							}
							map.put(pkg, bean);
						} catch (NameNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
			map.remove(TAApplication.getApplication().getPackageName());
			// 根据使用内存多少进行排序
			List<ProcessDataBean> retList = new ArrayList<ProcessDataBean>();
			retList.addAll(map.values());
			Collections.sort(retList);
			List<ProcessDataBean> retList_ = new ArrayList<ProcessDataBean>();
			for (ProcessDataBean bean : retList) {
				if (bean.mMemory > 0) {
					retList_.add(bean);
				}
			}
			for (ProcessDataBean bean : retList_) {
				if (protect.contains(bean.mInfo.packageName)) {
					bean.mIsProtection = true;
				} else {
					bean.mIsProtection = false;
				}
			}
			sendSuccessMessage(retList_);
			// 清理后的内存使用
			runningAppProcesses = activityManager.getRunningAppProcesses();
			outInfo = new MemoryInfo();
			activityManager.getMemoryInfo(outInfo);
			avi = (long) (outInfo.availMem / 1024.0 / 1024.0 + 0.5);
			total = (long) (InfoUtil.getTotalRAM() / 1024.0 + 0.5);
			int count2 = runningAppProcesses.size();
			long used2 = total - avi;
			final int kill = count1 - count2 > 0 ? count1 - count2 : 0;
			final long save = used1 - used2 > 0 ? used1 - used2 : 0;
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(TAApplication.getApplication(),
							"已经清理" + kill + "个进程，释放" + save + "MB内存",
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	/**
	 * 杀进程
	 */
	private void killApp(String pkg, int userid) {
		try {
			new RootShell.Command(String.format("export LD_LIBRARY_PATH=%s\n",
					System.getenv("LD_LIBRARY_PATH")).replace("$", "\\$"))
					.execute(RootShell.startShell());
			new RootShell.Command("export CLASSPATH="
					+ TAApplication.getApplication().getFilesDir()
							.getAbsolutePath() + "/tk.jar").execute(RootShell
					.startShell());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			new RootShell.Command(
					"/system/bin/app_process /system/bin com.zhidian.wifibox.root.tk.RootInternal  "
							+ pkg + "  " + userid).execute(RootShell
					.startShell());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建jar文件
	 */
	private void createFile() {
		InputStream inputStream = null;
		try {
			inputStream = TAApplication.getApplication().getResources()
					.getAssets().open("protocol");
			File file = TAApplication.getApplication().getFilesDir();
			if (!file.exists()) {
				file.mkdirs();
			}
			File newFile = new File(file.getAbsolutePath() + "/tk.jar");
			newFile.delete();
			FileOutputStream fileOutputStream = new FileOutputStream(
					newFile.getAbsolutePath());
			byte[] buffer = new byte[1024];
			int count = 0;
			while ((count = inputStream.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, count);
			}
			fileOutputStream.flush();
			fileOutputStream.close();
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
