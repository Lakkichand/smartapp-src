package com.jiubang.ggheart.appgame.appcenter.contorler;

import java.lang.reflect.Field;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.go.util.SortUtils;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.appcenter.bean.AppInfo;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 应用管理-我的应用，数据处理类。 安装，卸载，排序，有无sdcard，将应用从手机移动到sdcard等操作后的数据，均在此类处理
 * 
 * @author zhoujun
 * 
 */
public class MyAppsDataManager {
	private static final String INTERNAL_PATH = Environment.getDataDirectory()
			.getAbsolutePath();
	private static MyAppsDataManager sInstantce;
	private Context mContext;
	private ArrayList<AppInfo> mAllAppsData;
	/**
	 * 数据加载完毕后，回调的监听器
	 */
	private ArrayList<DataLoadCompletedListenter> mListenterList;

	/**
	 * 是否要停掉加载数据的线程
	 */
	private boolean mStopThread = false;

	/**
	 * list中显示group
	 */
	public static final int ITEMTYPE_GROUP = 1;

	/**
	 * 当前已排序的应用程序信息
	 */
	private SortedAppInfo mSortedAppInfo;

	/**
	 * 是否已经加载了所有的数据。可能初次加载数据的时候，sdcard不存在，这时mAllAppsData的数据不全,需要重新加载。如果已经加载所有数据，
	 * 就不需要重复加载了。
	 */
	private boolean mHasLoadedAllData = false;

	/**
	 * 监听数据是否加载完毕
	 * 
	 * @author zhoujun
	 * 
	 */
	public interface DataLoadCompletedListenter {
		public void loadCompleted(SortedAppInfo sortedAppInfo);
	}

	/**
	 * <br>
	 * 类描述: 我的应用 封装排序后的数据集合 <br>
	 * 功能详细描述:
	 * 
	 * @author zhoujun
	 * @date [2012-9-11]
	 */
	public class SortedAppInfo {
		public ArrayList<AppInfo> mSortedAppsData;
		public int mSortType;
		public int mInternalAppsData;
		public int mExternalAppsData;
	}

	private MyAppsDataManager(Context context) {
		mContext = context;
	}

	public synchronized static MyAppsDataManager getInstance(Context context) {
		if (sInstantce == null) {
			sInstantce = new MyAppsDataManager(context);
		}
		return sInstantce;
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mAllAppsData = (ArrayList<AppInfo>) msg.obj;
				sortByType(mAllAppsData, getSortType());
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * 获取当前已排序的应用程序
	 * 
	 * @return
	 */
	public SortedAppInfo getData(DataLoadCompletedListenter dataListenter) {
		if (dataListenter != null) {
			addDataLoadComletedListenter(dataListenter);
		}
		if (mSortedAppInfo == null) {
			loadAppData();
		}
		return mSortedAppInfo;
	}

	/**
	 * <br>
	 * 功能简述: 获取所有应用程序的个数 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public int getAllAppSize() {
		if (mAllAppsData != null) {
			return mAllAppsData.size();
		}
		return 0;
	}

	/**
	 * 加载系统所安装的应用
	 */
	private void loadAppData() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				ArrayList<AppInfo> allApps = getAllInstalledApp();
				if (mHandler != null && allApps != null) {
					Message message = mHandler.obtainMessage(0);
					message.obj = allApps;
					mHandler.sendMessage(message);
				}
			}
		}).start();
	}

	/**
	 * 按指定类型进行排序
	 * 
	 * @param sortType
	 */
	public void sortedAppByType(int sortType) {
		if (mAllAppsData != null) {
			sortByType(mAllAppsData, sortType);
		}

		/*** 点击排序前，应该已经加载过数据的 ***/
		// else {
		// loadAppData(0);
		// }
	}

	/**
	 * sdcard关闭时，存放在sdcard上面的应用不可见，要过滤掉
	 */
	public void sdCardTurnOff() {
		if (mAllAppsData != null) {
			int count = mAllAppsData.size();
			AppInfo appInfo = null;
			ArrayList<AppInfo> internalApps = new ArrayList<AppInfo>();
			for (int i = 0; i < count; i++) {
				appInfo = mAllAppsData.get(i);
				if (appInfo.mIsInternal) {
					internalApps.add(appInfo);
				}
			}
			sortByType(internalApps, getSortType());
		}
	}

	/**
	 * sdcard打开时，如果之前已经加载过全部数据，那么只需要重新排序；否则，就要重新加载所有的数据
	 */
	public void sdCardTurnOn() {
		if (!mHasLoadedAllData) {
			loadAppData();
		} else {
			if (mAllAppsData != null) {
				sortByType(mAllAppsData, getSortType());
			}
		}
	}

	/**
	 * 卸载应用程序后，刷新数据,卸载程序时，不需要重新排序
	 * 
	 * @param packageName
	 */
	public void uninstallApp(String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return;
		}
		if (mSortedAppInfo != null) {
			ArrayList<AppInfo> showAppsData = mSortedAppInfo.mSortedAppsData;
			if (showAppsData != null) {
				int count = showAppsData.size();
				AppInfo appInfo = null;
				int position = -1;
				for (int i = 0; i < count; i++) {
					appInfo = showAppsData.get(i);
					if (packageName.equals(appInfo.mPackageName)) {
						position = i;
						break;
					}
				}
				if (position != -1) {
					appInfo = showAppsData.get(position);
					showAppsData.remove(appInfo);
					mAllAppsData.remove(appInfo);

					int internalAppSize = mSortedAppInfo.mInternalAppsData;
					int externalAppSize = mSortedAppInfo.mExternalAppsData;

					if (mSortedAppInfo.mSortType == 0) {
						if (position <= internalAppSize) {
							internalAppSize--;
						} else {
							externalAppSize--;
						}
					}
					onSortedCompleted(showAppsData, mSortedAppInfo.mSortType,
							internalAppSize, externalAppSize);
				}
			}
		}
	}

	/**
	 * 安装应用程序后排序
	 * 
	 * @param packageName
	 */
	public void installApp(String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return;
		}
		if (isExistApp(packageName)) {
			return;
		}
		if (mSortedAppInfo != null) {
			AppInfo appInfo = createAppInfoByPkg(packageName);
			if (appInfo != null) {
				if (mAllAppsData != null) {
					mAllAppsData.add(appInfo);
				}
				sortByType(mAllAppsData, mSortedAppInfo.mSortType);
			}
		} else {
			// 说明在加载数据或者设备上没有任何程序,暂不处理
		}

	}

	/**
	 * 查看当前安装的应用是否已经存在
	 * 
	 * @param packageName
	 * @return
	 */
	private boolean isExistApp(String packageName) {
		if (mAllAppsData != null && mAllAppsData.size() > 0) {
			for (int index = 0; index < mAllAppsData.size(); index++) {
				if (packageName.equals(mAllAppsData.get(index).mPackageName)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 应用程序安装位置移动
	 * 
	 * @param position
	 */
	public void moveLocation(int position) {
		if (mSortedAppInfo != null) {
			ArrayList<AppInfo> mSortedAppsData = mSortedAppInfo.mSortedAppsData;
			if (mSortedAppsData != null && position < mSortedAppsData.size()) {
				AppInfo appInfo = mSortedAppsData.get(position);
				AppInfo newAppInfo = createAppInfoByPkg(appInfo.mPackageName);
				if (newAppInfo != null) {
					if (newAppInfo.mIsInternal == appInfo.mIsInternal) {
						return;
					} else {
						// appInfo.mIsInternal = newAppInfo.mIsInternal;
						copy(appInfo, newAppInfo);
						if (mSortedAppInfo.mSortType > 0) {
							// sortItem(mAllAppsData, mCurrSortType);
							// 不需要重新排序，直接返回刷新页面，改变可移动到手机或sdcard的图片
							onSortedCompleted(mSortedAppsData,
									mSortedAppInfo.mSortType,
									mSortedAppInfo.mInternalAppsData,
									mSortedAppInfo.mExternalAppsData);
						} else {
							// 按安装位置排序的话，要重新排序
							sortByLocation(mAllAppsData);
						}
					}
				}
			}
		}
	}

	private void copy(AppInfo oldAppInfo, AppInfo newAppInfo) {
		if (oldAppInfo == null || newAppInfo == null) {
			return;
		}
		oldAppInfo.mLocation = newAppInfo.mLocation;
		oldAppInfo.mPackageName = newAppInfo.mPackageName;
		oldAppInfo.mTitle = newAppInfo.mTitle;
		oldAppInfo.mIsInternal = newAppInfo.mIsInternal;
		oldAppInfo.mAppSize = newAppInfo.mAppSize;
		oldAppInfo.mFirstInstallTime = newAppInfo.mFirstInstallTime;
	}

	/**
	 * 按安装位置排序
	 */
	private void sortByLocation(ArrayList<AppInfo> allAppsDataList) {
		if (allAppsDataList != null) {
			int count = allAppsDataList.size();
			AppInfo appInfo = null;
			ArrayList<AppInfo> internalApps = new ArrayList<AppInfo>();
			ArrayList<AppInfo> externalApps = new ArrayList<AppInfo>();
			for (int i = 0; i < count; i++) {
				appInfo = allAppsDataList.get(i);
				if (appInfo.mIsInternal) {
					internalApps.add(appInfo);
				} else {
					externalApps.add(appInfo);
				}
			}

			int internalAppSize = 0;
			int externalAppSize = 0;
			if (!internalApps.isEmpty()) {
				SortUtils.sort(internalApps, "getTitle", null, null, "ASC");
				internalAppSize = internalApps.size();
			}

			if (!externalApps.isEmpty()) {
				SortUtils.sort(externalApps, "getTitle", null, null, "ASC");
				externalAppSize = externalApps.size();
			}

			ArrayList<AppInfo> mMyAppsData = new ArrayList<AppInfo>(
					internalAppSize + externalAppSize + 2);

			mMyAppsData.add(createGroupAppInfo());
			if (internalAppSize > 0) {
				mMyAppsData.addAll(internalApps);
			}

			mMyAppsData.add(createGroupAppInfo());
			if (externalAppSize > 0) {
				mMyAppsData.addAll(externalApps);
			}
			onSortedCompleted(mMyAppsData, 0, internalAppSize, externalAppSize);
		}
	}

	/**
	 * 排序比较器，中英文按a~z进行混排
	 */
	private Comparator<AppInfo> mComparator = new Comparator<AppInfo>() {
		@Override
		public int compare(AppInfo object1, AppInfo object2) {
			int result = 0;
			String str1 = object1.getTitle();
			String str2 = object2.getTitle();
			str1 = SortUtils.changeChineseToSpell(mContext, str1);
			str2 = SortUtils.changeChineseToSpell(mContext, str2);
			Collator collator = null;
			if (Build.VERSION.SDK_INT < 16) {
				collator = Collator.getInstance(Locale.CHINESE);
			} else {
				collator = Collator.getInstance(Locale.ENGLISH);
			}

			if (collator == null) {
				collator = Collator.getInstance(Locale.getDefault());
			}
			result = collator.compare(str1.toUpperCase(), str2.toUpperCase());
			return result;
		}
	};

	/**
	 * 按安装时间，名称或大小来排序
	 * 
	 * @param allAppsDataList
	 * @param type
	 */
	private void sortByType(ArrayList<AppInfo> allAppsDataList, int type) {
		switch (type) {
		case 0:
			// sortByLocation(allAppsDataList);
			// return;
		case 2:
			// SortUtils.sort(allAppsDataList, "getTitle", null, null, "ASC");
			Collections.sort(allAppsDataList, mComparator);
			sortByGroup(allAppsDataList);
			return;
		case 1:
			SortUtils.sortByLong(allAppsDataList, "getInstallTime", null, null,
					"DESC");
			break;
		case 3:
			SortUtils
					.sortByLong(allAppsDataList, "getSize", null, null, "DESC");
			break;
		default:
			break;
		}
		onSortedCompleted(allAppsDataList, type, 0, 0);
	}

	private AppInfo createGroupAppInfoAZ(String title) {
		AppInfo groupAppInfo = new AppInfo();
		groupAppInfo.mTitle = title;
		groupAppInfo.mType = MyAppsDataManager.ITEMTYPE_GROUP;
		return groupAppInfo;
	}

	/**
	 * <br>
	 * 功能简述:对扫描出来的应用按A～Z进行分组,传递的参数不会改变，返回一个全新的list <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param list
	 * @return
	 */
	public void sortByGroup(final ArrayList<AppInfo> list) {
		ArrayList<AppInfo> tempList = (ArrayList<AppInfo>) list.clone();
		for (int i = 0; i < list.size(); i++) {
			char firstLetter = '#';
			if (list.get(i).getTitle() == null
					|| list.get(i).getTitle().equals("")) {
				firstLetter = '#';
			} else {
				firstLetter = SortUtils
						.changeChineseToSpell(mContext, list.get(i).getTitle())
						.toUpperCase().charAt(0);
			}
			if (firstLetter < 'A' || firstLetter > 'Z') {
				firstLetter = '#';
			}
			int index = isExistByName(tempList, String.valueOf(firstLetter));
			if (index != -1) {
				continue;
			} else {
				index = isExistByName(tempList, list.get(i).getTitle());
				AppInfo bean = createGroupAppInfoAZ(String.valueOf(firstLetter));
				tempList.add(index, bean);
			}
		}
		Log.i("zj", "tempList" + tempList.size());
		onSortedCompleted(tempList, 2, 0, 0);
	}

	/**
	 * <br>
	 * 功能简述:用APP名字查询这个APP是否存在于列表中 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param list
	 * @param groupName
	 * @return
	 */
	private int isExistByName(ArrayList<AppInfo> list, String groupName) {
		for (int i = 0; i < list.size(); i++) {
			AppInfo bean = list.get(i);
			if (bean.getTitle().equals(groupName)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 数据加载完毕并完成排序后，将数据返回给请求者
	 * 
	 * @param appsData
	 * @param sortType
	 * @param internalAppSize
	 * @param externalAppSize
	 */
	private void onSortedCompleted(ArrayList<AppInfo> appsData, int sortType,
			int internalAppSize, int externalAppSize) {
		if (sortType == -1) {
			sortType = getSortType();
		}
		mSortedAppInfo = createSortedAppInfo(appsData, sortType,
				internalAppSize, externalAppSize);
		if (mListenterList != null) {
			int listenCount = mListenterList.size();
			DataLoadCompletedListenter listenter = null;
			for (int i = 0; i < listenCount; i++) {
				listenter = mListenterList.get(i);
				listenter.loadCompleted(mSortedAppInfo);
			}
		}
	}

	/**
	 * 生成当前已排序好的数据类型
	 * 
	 * @param appsData
	 * @param sortType
	 * @param internalAppSize
	 * @param externalAppSize
	 * @return
	 */
	private SortedAppInfo createSortedAppInfo(ArrayList<AppInfo> appsData,
			int sortType, int internalAppSize, int externalAppSize) {
		SortedAppInfo appInfo = new SortedAppInfo();
		appInfo.mSortedAppsData = appsData;
		appInfo.mSortType = sortType;
		appInfo.mInternalAppsData = internalAppSize;
		appInfo.mExternalAppsData = externalAppSize;
		return appInfo;
	}

	/**
	 * 获取设备上所有已安装的应用
	 * 
	 * @return
	 */
	private ArrayList<AppInfo> getAllInstalledApp() {
		ArrayList<AppInfo> allApps = null;
		final PackageManager pkgmanager = mContext.getPackageManager();
		List<PackageInfo> packages = pkgmanager.getInstalledPackages(0);
		if (Machine.isSDCardExist()) {
			mHasLoadedAllData = true;
		}
		if (packages != null) {
			int appCount = packages.size();
			if (appCount > 0) {
				allApps = new ArrayList<AppInfo>(appCount);
				AppInfo appInfo = null;
				for (int i = 0; i < appCount; i++) {
					if (mStopThread) {
						return null;
					}
					appInfo = createAppInfo(packages.get(i), pkgmanager);
					if (appInfo != null) {
						allApps.add(appInfo);
					}
				}
			}
		}
		if (allApps == null) {
			allApps = new ArrayList<AppInfo>();
		}
		return allApps;
	}

	/**
	 * 获取指定包的安装位置
	 * 
	 * @param packageInfo
	 * @return
	 */
	private int getinstallLocation(PackageInfo packageInfo) {
		try {
			Field field = packageInfo.getClass().getField("installLocation");
			Object obj = field.get(packageInfo);
			return Integer.parseInt(obj.toString());
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 根据包名生成应用程序信息
	 * 
	 * @param packageName
	 * @return
	 */
	private AppInfo createAppInfoByPkg(String packageName) {
		PackageManager manager = mContext.getPackageManager();
		AppInfo appInfo = null;
		try {
			PackageInfo packageInfo = manager.getPackageInfo(packageName, 0);
			appInfo = createAppInfo(packageInfo, manager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appInfo;
	}

	private AppInfo createAppInfo(PackageInfo packageInfo,
			PackageManager pkgmanager) {
		if (packageInfo == null) {
			return null;
		}
		// 获取用户安装的应用程序
		if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			AppInfo appInfo = new AppInfo();
			appInfo.mLocation = getinstallLocation(packageInfo);
			appInfo.mPackageName = packageInfo.packageName;
			appInfo.mTitle = packageInfo.applicationInfo.loadLabel(pkgmanager)
					.toString().trim();
			appInfo.setAppInfo(packageInfo.applicationInfo.publicSourceDir,
					INTERNAL_PATH);
			return appInfo;
		}
		return null;
	}

	private AppInfo createGroupAppInfo() {
		AppInfo groupAppInfo = new AppInfo();
		groupAppInfo.mTitle = null;
		groupAppInfo.mType = ITEMTYPE_GROUP;
		return groupAppInfo;
	}

	/**
	 * 获取当前应用的排序方式
	 * 
	 * @return
	 */
	private int getSortType() {
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.APPS_ORDER_TYPE, Context.MODE_PRIVATE);
		return preferences.getInt("orderType", 2);
	}

	private void addDataLoadComletedListenter(
			DataLoadCompletedListenter dataListenter) {
		if (mListenterList == null) {
			mListenterList = new ArrayList<DataLoadCompletedListenter>();
		}
		if (!mListenterList.contains(dataListenter)) {
			mListenterList.add(dataListenter);
		}

	}

	public void removeDataLoadComletedListenter(
			DataLoadCompletedListenter dataListenter) {
		if (mListenterList != null) {
			mListenterList.remove(dataListenter);
		}
	}

	public void cleanData() {
		if (mListenterList != null) {
			mListenterList.clear();
			mListenterList = null;
		}
		if (mSortedAppInfo != null) {
			if (mSortedAppInfo.mSortedAppsData != null) {
				mSortedAppInfo.mSortedAppsData.clear();
			}
			mSortedAppInfo = null;
		}
		mStopThread = true;
		if (mAllAppsData != null) {
			mAllAppsData.clear();
			mAllAppsData = null;
		}
	}

	public synchronized void destory() {
		if (sInstantce != null) {
			sInstantce.cleanData();
			sInstantce = null;
		}
	}
}
